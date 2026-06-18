package com.futurecode.recoverdeletedmessages.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.futurecode.recoverdeletedmessages.databinding.ItemChatMessageBinding
import com.futurecode.recoverdeletedmessages.model.MessageItem
import com.futurecode.recoverdeletedmessages.utils.Constants
import com.futurecode.recoverdeletedmessages.utils.FileUtils
import java.io.File

class ChatMessageAdapter(
    private val onMediaClick: (MessageItem) -> Unit
) : ListAdapter<MessageItem, ChatMessageAdapter.MessageViewHolder>(DIFF_CALLBACK) {

    inner class MessageViewHolder(val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvTime.text = FileUtils.formatTime(item.timestamp)
            tvMessageText.text = if (item.messageText.isNotBlank()) item.messageText
                                  else "Recover Deleted Messages…"

            when (item.messageType) {
                Constants.MEDIA_TYPE_IMAGE, Constants.MEDIA_TYPE_GIF, Constants.MEDIA_TYPE_STICKER -> {
                    mediaContainer.visibility = View.VISIBLE
                    voiceContainer.visibility = View.GONE
                    if (item.mediaPath.isNotBlank()) {
                        Glide.with(root.context).load(File(item.mediaPath)).into(ivThumbnail)
                    }
                    ivPlayIcon.visibility = View.GONE
                    tvGifBadge.visibility = if (item.messageType == Constants.MEDIA_TYPE_GIF) View.VISIBLE else View.GONE
                    mediaContainer.setOnClickListener { onMediaClick(item) }
                }
                Constants.MEDIA_TYPE_VIDEO -> {
                    mediaContainer.visibility = View.VISIBLE
                    voiceContainer.visibility = View.GONE
                    ivPlayIcon.visibility = View.VISIBLE
                    if (item.mediaPath.isNotBlank()) {
                        Glide.with(root.context).load(File(item.mediaPath)).into(ivThumbnail)
                    }
                    mediaContainer.setOnClickListener { onMediaClick(item) }
                }
                Constants.MEDIA_TYPE_VOICE, Constants.MEDIA_TYPE_AUDIO -> {
                    mediaContainer.visibility = View.GONE
                    voiceContainer.visibility = View.VISIBLE
                    voiceContainer.setOnClickListener { onMediaClick(item) }
                }
                else -> {
                    mediaContainer.visibility = View.GONE
                    voiceContainer.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MessageItem>() {
            override fun areItemsTheSame(old: MessageItem, new: MessageItem) = old.id == new.id
            override fun areContentsTheSame(old: MessageItem, new: MessageItem) = old == new
        }
    }
}
