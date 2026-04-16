package com.warden.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a scheduled focus block for a given day of the week.
 * dayOfWeek: 1=Monday, 2=Tuesday, ... 7=Sunday
 * startMinute / endMinute: minutes since midnight (0-1439)
 */
@Entity(tableName = "schedule_blocks")
data class ScheduleBlock(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dayOfWeek: Int,         // 1=Mon ... 7=Sun
    val startMinute: Int,       // e.g. 9*60 = 540 for 9:00 AM
    val endMinute: Int,         // e.g. 17*60 = 1020 for 5:00 PM
    val breakIntervalMinutes: Int = 60,   // break every N minutes
    val breakDurationMinutes: Int = 10    // break lasts N minutes
)
