package com.warden.app.ui.urls

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.warden.app.databinding.ActivityUrlBlacklistBinding

class UrlBlacklistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUrlBlacklistBinding
    private val viewModel: UrlBlacklistViewModel by viewModels()
    private lateinit var adapter: UrlBlacklistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUrlBlacklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupAddUrl()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = UrlBlacklistAdapter { url ->
            viewModel.removeUrl(url)
        }
        binding.rvUrls.layoutManager = LinearLayoutManager(this)
        binding.rvUrls.adapter = adapter
    }

    private fun setupAddUrl() {
        binding.btnAddUrl.setOnClickListener {
            addUrl()
        }

        binding.etUrlInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addUrl()
                true
            } else false
        }
    }

    private fun addUrl() {
        val input = binding.etUrlInput.text?.toString() ?: ""
        viewModel.addUrl(input)
        binding.etUrlInput.text?.clear()
    }

    private fun observeViewModel() {
        viewModel.urls.observe(this) { urls ->
            adapter.submitList(urls)
            binding.tvSubtitle.text = "${urls.size} DOMAINS BLOCKED"
            binding.tvEmptyState.visibility = if (urls.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                binding.tilUrlInput.error = error
            } else {
                binding.tilUrlInput.error = null
            }
        }
    }
}
