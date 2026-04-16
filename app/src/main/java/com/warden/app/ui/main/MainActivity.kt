package com.warden.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.warden.app.databinding.ActivityMainBinding
import com.warden.app.service.WardenForegroundService
import com.warden.app.ui.apps.AppBlacklistActivity
import com.warden.app.ui.schedule.ScheduleActivity
import com.warden.app.ui.settings.SettingsActivity
import com.warden.app.ui.urls.UrlBlacklistActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshState()
        updatePermissionWarnings()
    }

    private fun setupButtons() {
        binding.btnToggleSession.setOnClickListener {
            if (viewModel.isSessionActive.value == true) {
                stopSession()
            } else {
                startSession()
            }
        }

        binding.btnAppBlacklist.setOnClickListener {
            startActivity(Intent(this, AppBlacklistActivity::class.java))
        }

        binding.btnUrlBlacklist.setOnClickListener {
            startActivity(Intent(this, UrlBlacklistActivity::class.java))
        }

        binding.btnSchedule.setOnClickListener {
            startActivity(Intent(this, ScheduleActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun startSession() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "OVERLAY PERMISSION REQUIRED. GO TO SETTINGS.", Toast.LENGTH_LONG).show()
            return
        }
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(this, "ACCESSIBILITY SERVICE REQUIRED. GO TO SETTINGS.", Toast.LENGTH_LONG).show()
            return
        }
        WardenForegroundService.start(this)
        viewModel.setSessionActive(true)
    }

    private fun stopSession() {
        WardenForegroundService.stop(this)
        viewModel.setSessionActive(false)
    }

    private fun observeViewModel() {
        viewModel.isSessionActive.observe(this) { active ->
            updateSessionUI(active)
        }

        viewModel.blockedAppCount.observe(this) { count ->
            binding.tvAppCount.text = "$count APPS BLOCKED"
        }

        viewModel.blockedUrlCount.observe(this) { count ->
            binding.tvUrlCount.text = "$count URLS BLOCKED"
        }
    }

    private fun updateSessionUI(active: Boolean) {
        if (active) {
            binding.tvSessionStatus.text = getString(com.warden.app.R.string.session_active)
            binding.tvSessionStatus.setTextColor(getColor(com.warden.app.R.color.accent_green))
            binding.btnToggleSession.text = getString(com.warden.app.R.string.stop_session)
            binding.btnToggleSession.setBackgroundColor(getColor(com.warden.app.R.color.accent_red))
            binding.viewStatusIndicator.setBackgroundColor(getColor(com.warden.app.R.color.accent_green))
        } else {
            binding.tvSessionStatus.text = getString(com.warden.app.R.string.session_inactive)
            binding.tvSessionStatus.setTextColor(getColor(com.warden.app.R.color.gray_light))
            binding.btnToggleSession.text = getString(com.warden.app.R.string.start_session)
            binding.btnToggleSession.setBackgroundColor(getColor(com.warden.app.R.color.black))
            binding.viewStatusIndicator.setBackgroundColor(getColor(com.warden.app.R.color.gray_mid))
        }
    }

    private fun updatePermissionWarnings() {
        val hasOverlay = Settings.canDrawOverlays(this)
        val hasAccessibility = isAccessibilityServiceEnabled()

        if (!hasOverlay || !hasAccessibility) {
            binding.tvPermissionWarning.text = buildString {
                if (!hasOverlay) append("⚠ OVERLAY PERMISSION MISSING  ")
                if (!hasAccessibility) append("⚠ ACCESSIBILITY PERMISSION MISSING")
            }
            binding.tvPermissionWarning.visibility = android.view.View.VISIBLE
        } else {
            binding.tvPermissionWarning.visibility = android.view.View.GONE
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName = "$packageName/com.warden.app.service.WardenAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(serviceName)
    }
}
