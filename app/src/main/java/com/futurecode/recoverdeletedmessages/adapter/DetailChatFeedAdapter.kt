package com.futurecode.recoverdeletedmessages.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.data.MessageEntity
import com.futurecode.recoverdeletedmessages.databinding.ItemDetailBubbleMediaBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailChatFeedAdapter : ListAdapter<MessageEntity, DetailChatFeedAdapter.BubbleViewHolder>(BubbleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BubbleViewHolder {
        val binding = ItemDetailBubbleMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BubbleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BubbleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BubbleViewHolder(private val binding: ItemDetailBubbleMediaBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: MessageEntity) {
            val context = binding.root.context

            // Format epoch timestamp into readable clock text (e.g. "10:52 AM")
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            binding.tvBubbleTimestamp.text = formatter.format(Date(message.timestamp))

            // Reset visibility settings on recycled views before binding new states
            binding.cardBubbleWrapper.visibility = View.VISIBLE
            binding.layoutVariantStandardFile.visibility = View.GONE
            binding.layoutVariantVoiceNote.visibility = View.GONE
            binding.tvVariantPlainText.visibility = View.GONE
            binding.ivVariantLargeMedia.visibility = View.GONE
            binding.tvBubblePillBadge.visibility = View.GONE
            binding.tvBubbleTimestamp.visibility = View.VISIBLE

            // =========================================================================
            // FIXED: DYNAMIC CONDITION BASED ON ROOM ENTITY FIELDS
            // =========================================================================
            val rawText = message.messageText

            // 1. First priority condition: Check if the message was deleted for everyone
            if (message.isDeleted == 1) {
                binding.tvVariantPlainText.visibility = View.VISIBLE
                binding.tvVariantPlainText.text = "🚫 This message was deleted"
                binding.tvVariantPlainText.setTextColor(ContextCompat.getColor(context, R.color.action_delete_red))
                return
            }

            // Reset default text color for active normal messages
            binding.tvVariantPlainText.setTextColor(ContextCompat.getColor(context, android.R.color.black))

            // 2. Evaluate view types layout variants based on text rules signature keywords
            when {
                rawText.contains("📷 Photo") || rawText.equals("Photo", ignoreCase = true) -> {
                    binding.layoutVariantStandardFile.visibility = View.VISIBLE
                    binding.tvBubbleMainLabel.text = "Sent a photo"
                    binding.tvBubbleSubLabel.text = "Image file"
                    binding.ivBubbleTypeIcon.setImageResource(R.drawable.ic_send_navigation)
                    binding.layoutBubbleIconFrame.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bubble_tint_photo_blue)
                    binding.ivBubbleTypeIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.figma_selected_blue)
                }
                rawText.contains("🎥 Video") || rawText.equals("Video", ignoreCase = true) -> {
                    binding.layoutVariantStandardFile.visibility = View.VISIBLE
                    binding.tvBubbleMainLabel.text = "Sent a video"
                    binding.tvBubbleSubLabel.text = "MP4 Video"
                    binding.tvBubblePillBadge.visibility = View.VISIBLE
                    binding.tvBubblePillBadge.text = "0:03"
                    binding.ivBubbleTypeIcon.setImageResource(R.drawable.ic_video_play_overlay)
                    binding.layoutBubbleIconFrame.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bubble_tint_video_purple)
                    binding.ivBubbleTypeIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.action_footer_text)
                }
                rawText.contains("🎙️ Voice message") || rawText.contains("Voice note", ignoreCase = true) || rawText.equals("Audio", ignoreCase = true) -> {
                    binding.layoutVariantVoiceNote.visibility = View.VISIBLE
                }
                rawText.contains("GIF", ignoreCase = true) -> {
                    binding.layoutVariantStandardFile.visibility = View.VISIBLE
                    binding.tvBubbleMainLabel.text = "Sent a GIF"
                    binding.tvBubbleSubLabel.text = "Animation"
                    binding.ivBubbleTypeIcon.setImageResource(R.drawable.ic_select_all_list)
                    binding.layoutBubbleIconFrame.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bubble_tint_gif_pink)
                    binding.ivBubbleTypeIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.action_delete_red)
                }
                rawText.contains("Sticker", ignoreCase = true) -> {
                    binding.layoutVariantStandardFile.visibility = View.VISIBLE
                    binding.tvBubbleMainLabel.text = "Sent a sticker"
                    binding.tvBubbleSubLabel.text = "Sticker pack"
                    binding.ivBubbleTypeIcon.setImageResource(R.drawable.ic_help_question)
                    binding.layoutBubbleIconFrame.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bubble_tint_sticker_blue)
                    binding.ivBubbleTypeIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.chat_unread_indicator)
                }
                else -> {
                    // Default Fallback: Renders plain regular incoming text string messages cleanly
                    binding.tvVariantPlainText.visibility = View.VISIBLE
                    binding.tvVariantPlainText.text = message.messageText
                }
            }
        }
    }

    class BubbleDiffCallback : DiffUtil.ItemCallback<MessageEntity>() {
        override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean = oldItem == newItem
    }
}