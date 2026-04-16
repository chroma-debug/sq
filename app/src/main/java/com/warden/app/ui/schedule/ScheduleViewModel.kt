package com.warden.app.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.warden.app.data.model.ScheduleBlock
import com.warden.app.data.repository.WardenPreferences
import com.warden.app.data.repository.WardenRepository
import kotlinx.coroutines.launch

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WardenRepository(application)
    private val prefs = WardenPreferences(application)

    val scheduleBlocks: LiveData<List<ScheduleBlock>> = repository.getAllScheduleBlocks().asLiveData()

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    var isScheduleEnabled: Boolean
        get() = prefs.isScheduleEnabled
        set(value) { prefs.isScheduleEnabled = value }

    fun saveBlockForDay(
        day: Int,
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int,
        breakInterval: Int, breakDuration: Int
    ) {
        viewModelScope.launch {
            repository.deleteScheduleForDay(day)
            val block = ScheduleBlock(
                dayOfWeek = day,
                startMinute = startHour * 60 + startMinute,
                endMinute = endHour * 60 + endMinute,
                breakIntervalMinutes = breakInterval,
                breakDurationMinutes = breakDuration
            )
            repository.saveScheduleBlock(block)
            _saveSuccess.postValue(true)
        }
    }

    fun deleteBlockForDay(day: Int) {
        viewModelScope.launch {
            repository.deleteScheduleForDay(day)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAllSchedule()
        }
    }
}
