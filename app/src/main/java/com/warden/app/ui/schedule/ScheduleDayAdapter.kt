package com.warden.app.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.warden.app.data.model.ScheduleBlock
import com.warden.app.databinding.ItemScheduleDayBinding
import com.warden.app.service.ScheduleChecker

class ScheduleDayAdapter(
    private val onSaveDay: (day: Int, startH: Int, startM: Int, endH: Int, endM: Int, breakInterval: Int, breakDuration: Int) -> Unit,
    private val onClearDay: (day: Int) -> Unit
) : RecyclerView.Adapter<ScheduleDayAdapter.DayViewHolder>() {

    private val dayNames = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    private val blocksByDay = Array<ScheduleBlock?>(7) { null }

    fun updateBlocks(blocks: List<ScheduleBlock>) {
        blocksByDay.fill(null)
        for (block in blocks) {
            if (block.dayOfWeek in 1..7) {
                blocksByDay[block.dayOfWeek - 1] = block
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemScheduleDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(position + 1, dayNames[position], blocksByDay[position])
    }

    override fun getItemCount() = 7

    inner class DayViewHolder(private val binding: ItemScheduleDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(day: Int, dayName: String, block: ScheduleBlock?) {
            binding.tvDayName.text = dayName

            if (block != null) {
                binding.switchDayEnabled.isChecked = true
                binding.layoutTimeConfig.visibility = View.VISIBLE
                binding.tvCurrentSchedule.visibility = View.VISIBLE
                binding.tvCurrentSchedule.text =
                    "${ScheduleChecker.formatMinutes(block.startMinute)} — ${ScheduleChecker.formatMinutes(block.endMinute)}" +
                            " | BREAK EVERY ${block.breakIntervalMinutes}MIN FOR ${block.breakDurationMinutes}MIN"

                // Populate pickers
                binding.npStartHour.value = block.startMinute / 60
                binding.npStartMinute.value = block.startMinute % 60
                binding.npEndHour.value = block.endMinute / 60
                binding.npEndMinute.value = block.endMinute % 60
                binding.npBreakInterval.value = block.breakIntervalMinutes
                binding.npBreakDuration.value = block.breakDurationMinutes
            } else {
                binding.switchDayEnabled.isChecked = false
                binding.layoutTimeConfig.visibility = View.GONE
                binding.tvCurrentSchedule.visibility = View.GONE
            }

            // Setup number pickers
            setupNumberPickers()

            binding.switchDayEnabled.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.layoutTimeConfig.visibility = View.VISIBLE
                } else {
                    binding.layoutTimeConfig.visibility = View.GONE
                    onClearDay(day)
                }
            }

            binding.btnSaveDay.setOnClickListener {
                val startH = binding.npStartHour.value
                val startM = binding.npStartMinute.value
                val endH = binding.npEndHour.value
                val endM = binding.npEndMinute.value
                val breakInterval = binding.npBreakInterval.value
                val breakDuration = binding.npBreakDuration.value

                if (endH * 60 + endM <= startH * 60 + startM) {
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = "END TIME MUST BE AFTER START TIME"
                    return@setOnClickListener
                }
                binding.tvError.visibility = View.GONE
                onSaveDay(day, startH, startM, endH, endM, breakInterval, breakDuration)
            }
        }

        private fun setupNumberPickers() {
            binding.npStartHour.apply {
                minValue = 0; maxValue = 23
                displayedValues = (0..23).map { String.format("%02d", it) }.toTypedArray()
            }
            binding.npStartMinute.apply {
                minValue = 0; maxValue = 59
                displayedValues = (0..59).map { String.format("%02d", it) }.toTypedArray()
            }
            binding.npEndHour.apply {
                minValue = 0; maxValue = 23
                displayedValues = (0..23).map { String.format("%02d", it) }.toTypedArray()
            }
            binding.npEndMinute.apply {
                minValue = 0; maxValue = 59
                displayedValues = (0..59).map { String.format("%02d", it) }.toTypedArray()
            }
            binding.npBreakInterval.apply {
                minValue = 15; maxValue = 120
                value = 60
            }
            binding.npBreakDuration.apply {
                minValue = 5; maxValue = 30
                value = 10
            }
        }
    }
}
