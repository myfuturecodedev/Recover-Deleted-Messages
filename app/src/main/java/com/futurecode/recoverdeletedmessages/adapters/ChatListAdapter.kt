package com.futurecode.recoverdeletedmessages.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.futurecode.recoverdeletedmessages.databinding.ItemChatBinding
import com.futurecode.recoverdeletedmessages.model.MessageItem
import com.futurecode.recoverdeletedmessages.utils.Constants
import com.futurecode.recoverdeletedmessages.utils.FileUtils

data class ChatContact(
    val contactName: String,
    val lastMessage: MessageItem,
    val messageCount: Int = 1,
    val hasNew: Boolean = false
)

class ChatListAdapter(
    private val onContactClick: (String) -> Unit
) : ListAdapter<ChatContact, ChatListAdapter.ChatViewHolder>(DIFF_CALLBACK) {

    inner class ChatViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvContactName.text = item.contactName
            tvTime.text = FileUtils.formatTime(item.lastMessage.timestamp)
            tvLastMessage.text = getMessagePreview(item.lastMessage)
            tvNewBadge.visibility = if (item.hasNew) View.VISIBLE else View.GONE
            root.setOnClickListener { onContactClick(item.contactName) }
        }
    }

    private fun getMessagePreview(msg: MessageItem): String = when (msg.messageType) {
        Constants.MEDIA_TYPE_IMAGE -> "📷 Sent a photo"
        Constants.MEDIA_TYPE_VIDEO -> "🎥 Sent a video"
        Constants.MEDIA_TYPE_VOICE -> "🎤 Voice message"
        Constants.MEDIA_TYPE_GIF -> "🎬 Sent a GIF"
        Constants.MEDIA_TYPE_STICKER -> "🔖 Sent a sticker"
        Constants.MEDIA_TYPE_DOCUMENT -> "📄 Sent a document"
        else -> if (msg.messageText.isNotBlank()) msg.messageText else "Message"
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChatContact>() {
            override fun areItemsTheSame(old: ChatContact, new: ChatContact) =
                old.contactName == new.contactName
            override fun areContentsTheSame(old: ChatContact, new: ChatContact) = old == new
        }
    }
}
