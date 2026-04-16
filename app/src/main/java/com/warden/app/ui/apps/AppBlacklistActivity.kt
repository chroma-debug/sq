package com.warden.app.ui.apps

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.warden.app.databinding.ActivityAppBlacklistBinding

class AppBlacklistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppBlacklistBinding
    private val viewModel: AppBlacklistViewModel by viewModels()
    private lateinit var adapter: AppBlacklistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppBlacklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = AppBlacklistAdapter { appInfo ->
            viewModel.toggleApp(appInfo)
        }
        binding.rvApps.layoutManager = LinearLayoutManager(this)
        binding.rvApps.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filter(newText ?: "")
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.rvApps.visibility = if (loading) View.GONE else View.VISIBLE
        }

        viewModel.apps.observe(this) { apps ->
            adapter.submitList(apps.toMutableList())
            binding.tvSubtitle.text = "${apps.count { it.isBlocked }} APPS BLOCKED"
        }
    }
}
