package com.warden.app.ui.unlock

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.warden.app.data.repository.SecurePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraUnlockViewModel(application: Application) : AndroidViewModel(application) {

    private val securePrefs = SecurePreferences(application)
    private val analyzer = ProofOfWorkAnalyzer(securePrefs)

    sealed class UnlockState {
        object Idle : UnlockState()
        object Analyzing : UnlockState()
        data class Accepted(val breakMinutes: Int, val reason: String) : UnlockState()
        data class Rejected(val reason: String) : UnlockState()
        object NoApiKey : UnlockState()
    }

    private val _state = MutableLiveData<UnlockState>(UnlockState.Idle)
    val state: LiveData<UnlockState> = _state

    fun hasApiKey(): Boolean = securePrefs.hasApiKey()

    fun analyzePhoto(bitmap: Bitmap) {
        if (!securePrefs.hasApiKey()) {
            _state.value = UnlockState.NoApiKey
            return
        }

        _state.value = UnlockState.Analyzing

        viewModelScope.launch(Dispatchers.IO) {
            val result = analyzer.analyze(bitmap)
            if (result.message == "NO_API_KEY") {
                _state.postValue(UnlockState.NoApiKey)
            } else if (result.isAccepted) {
                _state.postValue(UnlockState.Accepted(result.breakMinutes, result.message))
            } else {
                _state.postValue(UnlockState.Rejected(result.message))
            }
        }
    }

    fun reset() {
        _state.value = UnlockState.Idle
    }
}
