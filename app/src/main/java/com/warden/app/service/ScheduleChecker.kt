package com.warden.app.service

import com.warden.app.data.model.ScheduleBlock
import java.util.Calendar

object ScheduleChecker {

    /**
     * Returns true if the current time falls within any of the provided schedule blocks.
     * dayOfWeek: Calendar.MONDAY=2 ... we normalize to 1=Mon..7=Sun
     */
    fun isInSchedule(blocks: List<ScheduleBlock>): Boolean {
        val cal = Calendar.getInstance()
        val calDay = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...7=Sat
        val normalizedDay = if (calDay == Calendar.SUNDAY) 7 else calDay - 1 // 1=Mon..7=Sun
        val currentMinute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        return blocks.any { block ->
            block.dayOfWeek == normalizedDay &&
                    currentMinute >= block.startMinute &&
                    currentMinute < block.endMinute
        }
    }

    /**
     * Returns true if the current time is within a scheduled break window.
     * A break occurs at regular intervals within the focus block.
     */
    fun isInBreak(blocks: List<ScheduleBlock>): Boolean {
        val cal = Calendar.getInstance()
        val calDay = cal.get(Calendar.DAY_OF_WEEK)
        val normalizedDay = if (calDay == Calendar.SUNDAY) 7 else calDay - 1
        val currentMinute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        for (block in blocks) {
            if (block.dayOfWeek != normalizedDay) continue
            if (currentMinute < block.startMinute || currentMinute >= block.endMinute) continue

            val minutesIntoBlock = currentMinute - block.startMinute
            val cycleLength = block.breakIntervalMinutes + block.breakDurationMinutes
            val positionInCycle = minutesIntoBlock % cycleLength

            if (positionInCycle >= block.breakIntervalMinutes) {
                return true // We are in a break window
            }
        }
        return false
    }

    fun formatMinutes(totalMinutes: Int): String {
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return String.format("%02d:%02d", h, m)
    }
}
