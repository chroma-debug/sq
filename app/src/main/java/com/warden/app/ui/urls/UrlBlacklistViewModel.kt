package com.warden.app.ui.urls

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.warden.app.data.model.BlockedUrl
import com.warden.app.data.repository.WardenRepository
import kotlinx.coroutines.launch

class UrlBlacklistViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WardenRepository(application)

    val urls: LiveData<List<BlockedUrl>> = repository.getAllUrls().asLiveData()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun addUrl(domain: String) {
        val cleaned = domain.trim()
            .lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")

        if (cleaned.isBlank()) {
            _error.value = "DOMAIN CANNOT BE EMPTY"
            return
        }

        if (!cleaned.contains(".")) {
            _error.value = "ENTER A VALID DOMAIN (e.g. reddit.com)"
            return
        }

        _error.value = null
        viewModelScope.launch {
            repository.addUrl(cleaned)
        }
    }

    fun removeUrl(url: BlockedUrl) {
        viewModelScope.launch {
            repository.removeUrl(url)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
