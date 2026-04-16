package com.warden.app.ui.apps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.warden.app.data.model.AppInfo
import com.warden.app.databinding.ItemAppBinding

class AppBlacklistAdapter(
    private val onToggle: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppBlacklistAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppViewHolder(private val binding: ItemAppBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appInfo: AppInfo) {
            binding.tvAppName.text = appInfo.appName.uppercase()
            binding.tvPackageName.text = appInfo.packageName
            binding.ivAppIcon.setImageDrawable(appInfo.icon)
            binding.switchBlocked.isChecked = appInfo.isBlocked

            // Update visual state
            updateBlockedState(appInfo.isBlocked)

            binding.switchBlocked.setOnCheckedChangeListener(null)
            binding.switchBlocked.setOnCheckedChangeListener { _, _ ->
                onToggle(appInfo)
                updateBlockedState(!appInfo.isBlocked)
            }

            binding.root.setOnClickListener {
                binding.switchBlocked.toggle()
            }
        }

        private fun updateBlockedState(isBlocked: Boolean) {
            val context = binding.root.context
            if (isBlocked) {
                binding.tvBlockedLabel.text = "BLOCKED"
                binding.tvBlockedLabel.setTextColor(context.getColor(com.warden.app.R.color.accent_red))
                binding.root.alpha = 1.0f
            } else {
                binding.tvBlockedLabel.text = "ALLOWED"
                binding.tvBlockedLabel.setTextColor(context.getColor(com.warden.app.R.color.gray_light))
                binding.root.alpha = 0.7f
            }
        }
    }

    class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo) =
            oldItem.packageName == newItem.packageName

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo) =
            oldItem.isBlocked == newItem.isBlocked
    }
}
