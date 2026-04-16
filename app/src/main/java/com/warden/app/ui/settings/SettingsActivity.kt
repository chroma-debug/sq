package com.warden.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.warden.app.databinding.ActivitySettingsBinding
import com.warden.app.data.repository.SecurePreferences

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var securePrefs: SecurePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        securePrefs = SecurePreferences(applicationContext)

        setupUI()
        loadSavedKeys()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnSaveKeys.setOnClickListener {
            saveKeys()
        }

        binding.btnGrantOverlay.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        binding.btnGrantAccessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        binding.btnGrantUsageStats.setOnClickListener {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    private fun loadSavedKeys() {
        val apiKey = securePrefs.getApiKey()
        val baseUrl = securePrefs.getApiBaseUrl()

        if (apiKey.isNotBlank()) {
            // Show masked key
            binding.etApiKey.setText("••••••••${apiKey.takeLast(4)}")
        }
        binding.etApiBaseUrl.setText(baseUrl)
    }

    private fun saveKeys() {
        val apiKeyInput = binding.etApiKey.text?.toString()?.trim() ?: ""
        val baseUrlInput = binding.etApiBaseUrl.text?.toString()?.trim() ?: ""

        // Only save API key if it's not the masked placeholder
        if (apiKeyInput.isNotBlank() && !apiKeyInput.startsWith("••")) {
            securePrefs.saveApiKey(apiKeyInput)
        }

        if (baseUrlInput.isNotBlank()) {
            securePrefs.saveApiBaseUrl(baseUrlInput)
        }

        Toast.makeText(this, getString(com.warden.app.R.string.keys_saved), Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        // Overlay permission
        val hasOverlay = Settings.canDrawOverlays(this)
        binding.tvOverlayStatus.text = if (hasOverlay) "GRANTED ✓" else "NOT GRANTED"
        binding.tvOverlayStatus.setTextColor(
            if (hasOverlay) getColor(com.warden.app.R.color.accent_green)
            else getColor(com.warden.app.R.color.accent_red)
        )

        // Accessibility permission (check if our service is enabled)
        val accessibilityEnabled = isAccessibilityServiceEnabled()
        binding.tvAccessibilityStatus.text = if (accessibilityEnabled) "GRANTED ✓" else "NOT GRANTED"
        binding.tvAccessibilityStatus.setTextColor(
            if (accessibilityEnabled) getColor(com.warden.app.R.color.accent_green)
            else getColor(com.warden.app.R.color.accent_red)
        )
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
