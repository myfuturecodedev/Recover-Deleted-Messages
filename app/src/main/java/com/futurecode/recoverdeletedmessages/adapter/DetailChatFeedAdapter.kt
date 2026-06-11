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

            // Evaluate view variants based on the dynamic incoming messageType
            when (message.messageType.uppercase()) {
                "PHOTO" -> {
                    binding.layoutVariantStandardFile.visibility = View.VISIBLE
                    binding.tvBubbleMainLabel.text = "Sent a photo"
                    binding.tvBubbleSubLabel.text = "Image file"
                    binding.ivBubbleTypeIcon.setImageResource(R.drawable.ic_send_navigation) // Camera/Gallery vector link
                    binding.layoutBubbleIconFrame.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bubble_tint_photo_blue)
                    binding.ivBubbleTypeIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.figma_selected_blue)
                }
                "VIDEO" -> {
                    binding.layoutVariantStandardFile.visibility = View.VISIBLE
                    binding.tvBubbleMainLabel.text = "Sent a video"
                    binding.tvBubbleSubLabel.text = "MP4 Video"
                    binding.tvBubblePillBadge.visibility = View.VISIBLE
                    binding.tvBubblePillBadge.text = "0:03" // Runtime mock placeholder
                    binding.ivBubbleTypeIcon.setImageResource(R.drawable.ic_video_play_overlay)
                    binding.layoutBubbleIconFrame.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bubble_tint_video_purple)
                    binding.ivBubbleTypeIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.action_footer_text)
                }
                "VOICE", "AUDIO" -> {
                    binding.layoutVariantVoiceNote.visibility = View.VISIBLE
                }
                "GIF" -> {
                    binding.layoutVariantStandardFile.visibility = View.VISIBLE
                    binding.tvBubbleMainLabel.text = "Sent a GIF"
                    binding.tvBubbleSubLabel.text = "Animation"
                    binding.ivBubbleTypeIcon.setImageResource(R.drawable.ic_select_all_list)
                    binding.layoutBubbleIconFrame.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bubble_tint_gif_pink)
                    binding.ivBubbleTypeIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.action_delete_red)
                }
                "STICKER" -> {
                    binding.layoutVariantStandardFile.visibility = View.VISIBLE
                    binding.tvBubbleMainLabel.text = "Sent a sticker"
                    binding.tvBubbleSubLabel.text = "Sticker pack"
                    binding.ivBubbleTypeIcon.setImageResource(R.drawable.ic_help_question) // Smiley face asset
                    binding.layoutBubbleIconFrame.backgroundTintList = ContextCompat.getColorStateList(context, R.color.bubble_tint_sticker_blue)
                    binding.ivBubbleTypeIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.chat_unread_indicator)
                }
                "TEXT" -> {
                    binding.tvVariantPlainText.visibility = View.VISIBLE
                    binding.tvVariantPlainText.text = message.textContent
                }
                "LARGE_IMAGE" -> {
                    // Variant 7: Direct Full Image Card Rendering layer seen at the bottom edge of screenshot
                    binding.cardBubbleWrapper.visibility = View.GONE
                    binding.tvBubbleTimestamp.visibility = View.GONE // Timestamp hidden or embedded inside image edge bounds
                    binding.ivVariantLargeMedia.visibility = View.VISIBLE

                    com.bumptech.glide.Glide.with(context)
                        .load(message.localMediaUri)
                        .placeholder(R.drawable.ic_wa_group_fallback)
                        .into(binding.ivVariantLargeMedia)
                }
                else -> {
                    binding.tvVariantPlainText.visibility = View.VISIBLE
                    binding.tvVariantPlainText.text = message.textContent
                }
            }
        }
    }

    class BubbleDiffCallback : DiffUtil.ItemCallback<MessageEntity>() {
        override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean = oldItem == newItem
    }
}