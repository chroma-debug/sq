package com.warden.app.ui.apps

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.warden.app.data.model.AppInfo
import com.warden.app.data.repository.WardenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppBlacklistViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WardenRepository(application)
    private val pm = application.packageManager

    private val _apps = MutableLiveData<List<AppInfo>>()
    val apps: LiveData<List<AppInfo>> = _apps

    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private var allApps: List<AppInfo> = emptyList()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            val blockedPackages = repository.getBlockedAppsList().map { it.packageName }.toSet()

            val installedApps = withContext(Dispatchers.IO) {
                pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { appInfo ->
                        // Filter to user-installed and launchable apps only
                        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        val hasLauncher = pm.getLaunchIntentForPackage(appInfo.packageName) != null
                        !isSystem || hasLauncher
                    }
                    .map { appInfo ->
                        AppInfo(
                            packageName = appInfo.packageName,
                            appName = pm.getApplicationLabel(appInfo).toString(),
                            icon = try { pm.getApplicationIcon(appInfo.packageName) } catch (e: Exception) { null },
                            isBlocked = appInfo.packageName in blockedPackages
                        )
                    }
                    .sortedWith(compareByDescending<AppInfo> { it.isBlocked }.thenBy { it.appName })
            }

            allApps = installedApps
            _apps.value = installedApps
            _isLoading.value = false
        }
    }

    fun toggleApp(appInfo: AppInfo) {
        viewModelScope.launch {
            val newBlocked = !appInfo.isBlocked
            repository.setAppBlocked(appInfo.packageName, appInfo.appName, newBlocked)
            appInfo.isBlocked = newBlocked
            _apps.value = _apps.value?.toMutableList()
        }
    }

    fun filter(query: String) {
        val filtered = if (query.isBlank()) {
            allApps
        } else {
            allApps.filter {
                it.appName.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
            }
        }
        _apps.value = filtered
    }
}
