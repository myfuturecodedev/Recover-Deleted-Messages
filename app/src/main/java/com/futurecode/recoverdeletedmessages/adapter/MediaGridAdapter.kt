package com.futurecode.recoverdeletedmessages.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.data.MessageEntity
import com.futurecode.recoverdeletedmessages.databinding.ItemMediaGridCardBinding
import java.io.File

class MediaGridAdapter(
    private val onCardClicked: (MessageEntity) -> Unit,
    private val onCardLongPressed: (MessageEntity) -> Unit
) : ListAdapter<MessageEntity, MediaGridAdapter.MediaViewHolder>(MediaDiffCallback()) {

    private val selectedFilePathsSet = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MediaViewHolder(ItemMediaGridCardBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val item = getItem(position)
        // Explicitly evaluate selection state at execution pass level to avoid cache data lagging
        val isSelected = selectedFilePathsSet.contains(item.localMediaUri ?: "")
        holder.bind(item, isSelected)
    }

    fun updateSelectionCache(updatedSet: Set<String>) {
        selectedFilePathsSet.clear()
        selectedFilePathsSet.addAll(updatedSet)
        notifyItemRangeChanged(0, itemCount)
    }

    inner class MediaViewHolder(private val binding: ItemMediaGridCardBinding) : RecyclerView.ViewHolder(binding.root) {

        // FIXED: Re-mapped variable parameters matching core Room Database schemas
        fun bind(item: MessageEntity, isSelected: Boolean) {
            val localPath = item.localMediaUri ?: ""
            val rawText = item.messageText

            // Render imagery thumbnails off physical device storage streams safely using Glide
            Glide.with(binding.ivMediaThumbnail.context)
                .load(File(localPath))
                .placeholder(R.color.figma_close_btn_bg)
                .error(R.color.figma_close_btn_bg) // Fallback layer if picture was permanently removed
                .into(binding.ivMediaThumbnail)

            // =========================================================================
            // FIXED TYPE DETECTION LAYER VIA STRING MATCHING SIGNATURES
            // =========================================================================
            val isVideo = rawText.contains("🎥 Video") || rawText.equals("Video", ignoreCase = true)
            binding.ivCenterVideoPlay.visibility = if (isVideo) View.VISIBLE else View.GONE
            binding.tvVideoDurationStamp.visibility = if (isVideo) View.VISIBLE else View.GONE
            if (isVideo) {
                binding.tvVideoDurationStamp.text = "0:03" // Standard mock metadata runtime placeholder
            }

            // FIXED: Toggles "NEW" notification alert visibility using updated structural states
            val isNewIncomingText = item.isDeleted == 0
            binding.tvBadgeNew.visibility = if (isNewIncomingText) View.VISIBLE else View.GONE
            // =========================================================================

            // Selection state mapping controls
            if (isSelected) {
                binding.ivSelectionIndicator.setImageResource(R.drawable.ic_checkbox_selected)
                binding.cardMediaRoot.setStrokeColor(binding.root.context.getColor(R.color.figma_selected_blue))
                binding.cardMediaRoot.strokeWidth = 3
            } else {
                binding.ivSelectionIndicator.setImageResource(R.drawable.ic_checkbox_unselected)
                binding.cardMediaRoot.strokeWidth = 0
            }

            binding.root.setOnClickListener { onCardClicked(item) }
            binding.root.setOnLongClickListener {
                onCardLongPressed(item)
                true
            }
        }
    }

    class MediaDiffCallback : DiffUtil.ItemCallback<MessageEntity>() {
        override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean = oldItem == newItem
    }
}