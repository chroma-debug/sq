package com.warden.app.ui.urls

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.warden.app.data.model.BlockedUrl
import com.warden.app.databinding.ItemUrlBinding

class UrlBlacklistAdapter(
    private val onRemove: (BlockedUrl) -> Unit
) : ListAdapter<BlockedUrl, UrlBlacklistAdapter.UrlViewHolder>(UrlDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrlViewHolder {
        val binding = ItemUrlBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UrlViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UrlViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UrlViewHolder(private val binding: ItemUrlBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(url: BlockedUrl) {
            binding.tvDomain.text = url.domain.uppercase()
            binding.btnRemove.setOnClickListener {
                onRemove(url)
            }
        }
    }

    class UrlDiffCallback : DiffUtil.ItemCallback<BlockedUrl>() {
        override fun areItemsTheSame(oldItem: BlockedUrl, newItem: BlockedUrl) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BlockedUrl, newItem: BlockedUrl) =
            oldItem.domain == newItem.domain
    }
}
