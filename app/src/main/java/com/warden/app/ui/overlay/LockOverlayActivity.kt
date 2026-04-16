package com.warden.app.ui.overlay

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.warden.app.R
import com.warden.app.data.repository.WardenPreferences
import com.warden.app.databinding.ActivityLockOverlayBinding
import com.warden.app.service.WardenAccessibilityService
import com.warden.app.ui.unlock.CameraUnlockActivity

class LockOverlayActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
        const val EXTRA_BLOCKED_URL = "blocked_url"
    }

    private lateinit var binding: ActivityLockOverlayBinding
    private lateinit var prefs: WardenPreferences
    private var breakCountdown: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make this a true full-screen overlay
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding = ActivityLockOverlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = WardenPreferences(applicationContext)

        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""
        val blockedUrl = intent.getStringExtra(EXTRA_BLOCKED_URL) ?: ""

        setupUI(blockedPackage, blockedUrl)
        checkBreakStatus()
    }

    private fun setupUI(blockedPackage: String, blockedUrl: String) {
        // Show URL info if it was a URL block
        if (blockedUrl.isNotBlank()) {
            binding.tvBlockedInfo.text = "BLOCKED: $blockedUrl"
            binding.tvBlockedInfo.visibility = View.VISIBLE
        } else {
            binding.tvBlockedInfo.visibility = View.GONE
        }

        // Go back button — sends user to home screen
        binding.btnGoBack.setOnClickListener {
            goHome()
        }

        // Proof of work unlock
        binding.btnProofOfWork.setOnClickListener {
            val intent = Intent(this, CameraUnlockActivity::class.java)
            startActivityForResult(intent, REQUEST_PROOF_OF_WORK)
        }
    }

    private fun checkBreakStatus() {
        if (prefs.isBreakActive()) {
            showBreakMode()
        }
    }

    private fun showBreakMode() {
        val remaining = prefs.remainingBreakSeconds()
        binding.tvBreakMode.visibility = View.VISIBLE
        binding.tvBreakMode.text = "BREAK: ${remaining}S REMAINING"

        breakCountdown = object : CountDownTimer(remaining * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvBreakMode.text = "BREAK: ${millisUntilFinished / 1000}S REMAINING"
            }

            override fun onFinish() {
                prefs.isBreakMode = false
                binding.tvBreakMode.visibility = View.GONE
            }
        }.start()
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        // Don't finish — overlay stays until user exits blocked app
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PROOF_OF_WORK && resultCode == RESULT_OK) {
            val breakMinutes = data?.getIntExtra(CameraUnlockActivity.EXTRA_BREAK_MINUTES, 5) ?: 5
            prefs.startBreak(breakMinutes)
            WardenAccessibilityService.instance?.dismissLock()
            showBreakMode()
        }
    }

    // Intercept back button — cannot dismiss overlay with back press
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goHome()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Refresh with new blocked package/url info
        intent?.let {
            val blockedPackage = it.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""
            val blockedUrl = it.getStringExtra(EXTRA_BLOCKED_URL) ?: ""
            setupUI(blockedPackage, blockedUrl)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        breakCountdown?.cancel()
    }

    companion object {
        private const val REQUEST_PROOF_OF_WORK = 1001
    }
}
