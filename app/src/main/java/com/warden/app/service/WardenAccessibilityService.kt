package com.warden.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.warden.app.data.repository.WardenPreferences
import com.warden.app.data.repository.WardenRepository
import com.warden.app.ui.overlay.LockOverlayActivity
import kotlinx.coroutines.*

class WardenAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "WardenAccessibility"
        private const val CHROME_PACKAGE = "com.android.chrome"
        private const val CHROME_STABLE = "com.chrome.beta"
        private const val CHROME_DEV = "com.chrome.dev"
        private const val FIREFOX_PACKAGE = "org.mozilla.firefox"
        private const val BRAVE_PACKAGE = "com.brave.browser"

        // Chrome URL bar resource IDs
        private val URL_BAR_IDS = listOf(
            "com.android.chrome:id/url_bar",
            "com.chrome.beta:id/url_bar",
            "com.chrome.dev:id/url_bar",
            "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
            "com.brave.browser:id/url_bar"
        )

        var instance: WardenAccessibilityService? = null
    }

    private lateinit var prefs: WardenPreferences
    private lateinit var repository: WardenRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var currentForegroundPackage: String = ""
    private var lastBlockedPackage: String = ""
    private var lastBlockedUrl: String = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        prefs = WardenPreferences(applicationContext)
        repository = WardenRepository(applicationContext)
        Log.d(TAG, "Warden Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            val packageName = event.packageName?.toString() ?: return

            // Skip our own app to avoid feedback loops
            if (packageName == applicationContext.packageName) return

            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                currentForegroundPackage = packageName
                checkAppBlocking(packageName)
            }

            // Check URL bar for browser packages
            if (isBrowserPackage(packageName)) {
                checkUrlBlocking(event, packageName)
            }
        }
    }

    private fun checkAppBlocking(packageName: String) {
        serviceScope.launch {
            try {
                if (!shouldEnforceBlocking()) return@launch

                val isBlocked = repository.isAppBlocked(packageName)
                if (isBlocked && packageName != lastBlockedPackage) {
                    lastBlockedPackage = packageName
                    Log.d(TAG, "Blocking app: $packageName")
                    triggerLockOverlay(packageName, null)
                } else if (!isBlocked) {
                    if (lastBlockedPackage == packageName) {
                        lastBlockedPackage = ""
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking app blocking", e)
            }
        }
    }

    private fun checkUrlBlocking(event: AccessibilityEvent, packageName: String) {
        serviceScope.launch {
            try {
                if (!shouldEnforceBlocking()) return@launch

                val rootNode = rootInActiveWindow ?: return@launch
                val url = extractUrlFromBrowser(rootNode, packageName) ?: return@launch

                if (url.isBlank() || url == lastBlockedUrl) return@launch

                val isBlocked = repository.isUrlBlocked(url)
                if (isBlocked) {
                    lastBlockedUrl = url
                    Log.d(TAG, "Blocking URL: $url in $packageName")
                    triggerLockOverlay(packageName, url)
                } else {
                    lastBlockedUrl = ""
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking URL blocking", e)
            }
        }
    }

    private suspend fun shouldEnforceBlocking(): Boolean {
        // Not in an active session
        if (!prefs.isSessionActive) return false

        // Break mode is active
        if (prefs.isBreakActive()) return false

        // Check schedule if enabled
        if (prefs.isScheduleEnabled) {
            val blocks = repository.getAllScheduleBlocksList()
            if (blocks.isNotEmpty()) {
                if (!ScheduleChecker.isInSchedule(blocks)) return false
                if (ScheduleChecker.isInBreak(blocks)) return false
            }
        }

        return true
    }

    private fun extractUrlFromBrowser(rootNode: AccessibilityNodeInfo, packageName: String): String? {
        for (resourceId in URL_BAR_IDS) {
            if (!resourceId.startsWith(packageName)) continue
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(resourceId)
            if (nodes.isNotEmpty()) {
                val text = nodes[0].text?.toString()
                if (!text.isNullOrBlank()) return text
            }
        }

        // Fallback: search by class name for EditText in URL bar area
        return findUrlBarText(rootNode)
    }

    private fun findUrlBarText(node: AccessibilityNodeInfo?): String? {
        node ?: return null
        if (node.className?.contains("EditText") == true && node.text != null) {
            val text = node.text.toString()
            if (text.contains(".") && (text.startsWith("http") || !text.contains(" "))) {
                return text
            }
        }
        for (i in 0 until node.childCount) {
            val result = findUrlBarText(node.getChild(i))
            if (result != null) return result
        }
        return null
    }

    private fun isBrowserPackage(packageName: String): Boolean {
        return packageName == CHROME_PACKAGE ||
                packageName == CHROME_STABLE ||
                packageName == CHROME_DEV ||
                packageName == FIREFOX_PACKAGE ||
                packageName == BRAVE_PACKAGE
    }

    private fun triggerLockOverlay(packageName: String, blockedUrl: String?) {
        val intent = Intent(applicationContext, LockOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(LockOverlayActivity.EXTRA_BLOCKED_PACKAGE, packageName)
            putExtra(LockOverlayActivity.EXTRA_BLOCKED_URL, blockedUrl ?: "")
        }
        applicationContext.startActivity(intent)
    }

    fun dismissLock() {
        lastBlockedPackage = ""
        lastBlockedUrl = ""
    }

    override fun onInterrupt() {
        Log.d(TAG, "Warden Accessibility Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()
    }
}
