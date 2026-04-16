package com.warden.app.ui.schedule

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.warden.app.databinding.ActivityScheduleBinding
import com.warden.app.data.model.ScheduleBlock
import com.warden.app.service.ScheduleChecker

class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding
    private val viewModel: ScheduleViewModel by viewModels()
    private lateinit var adapter: ScheduleDayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupScheduleToggle()
        observeData()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = ScheduleDayAdapter(
            onSaveDay = { day, startH, startM, endH, endM, breakInterval, breakDuration ->
                viewModel.saveBlockForDay(day, startH, startM, endH, endM, breakInterval, breakDuration)
            },
            onClearDay = { day ->
                viewModel.deleteBlockForDay(day)
            }
        )
        binding.rvSchedule.layoutManager = LinearLayoutManager(this)
        binding.rvSchedule.adapter = adapter
    }

    private fun setupScheduleToggle() {
        binding.switchSchedule.isChecked = viewModel.isScheduleEnabled
        binding.switchSchedule.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isScheduleEnabled = isChecked
            val msg = if (isChecked) "SCHEDULE ENFORCEMENT ENABLED" else "SCHEDULE ENFORCEMENT DISABLED"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeData() {
        viewModel.scheduleBlocks.observe(this) { blocks ->
            adapter.updateBlocks(blocks)
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "SCHEDULE SAVED", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
