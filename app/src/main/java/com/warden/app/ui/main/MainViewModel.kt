package com.warden.app.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.warden.app.data.repository.WardenPreferences
import com.warden.app.data.repository.WardenRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WardenRepository(application)
    private val prefs = WardenPreferences(application)

    private val _isSessionActive = MutableLiveData<Boolean>()
    val isSessionActive: LiveData<Boolean> = _isSessionActive

    val blockedAppCount: LiveData<Int> = repository.getBlockedApps()
        .map { it.size }
        .asLiveData()

    val blockedUrlCount: LiveData<Int> = repository.getAllUrls()
        .map { it.size }
        .asLiveData()

    init {
        refreshState()
    }

    fun refreshState() {
        _isSessionActive.value = prefs.isSessionActive
    }

    fun setSessionActive(active: Boolean) {
        prefs.isSessionActive = active
        _isSessionActive.value = active
    }
}
