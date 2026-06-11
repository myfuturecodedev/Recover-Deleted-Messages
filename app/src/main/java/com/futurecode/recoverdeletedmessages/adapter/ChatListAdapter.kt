package com.futurecode.recoverdeletedmessages.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.databinding.ItemChatListRowBinding
import com.futurecode.recoverdeletedmessages.model.ChatListItem
import com.futurecode.recoverdeletedmessages.data.MessageEntity

class ChatListAdapter(
    private val onChatClicked: (MessageEntity) -> Unit,
    private val onChatLongPressed: (MessageEntity) -> Unit
) : ListAdapter<MessageEntity, ChatListAdapter.ChatViewHolder>(ChatDiffCallback()) {

    private val selectedChatIdsSet = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ChatViewHolder(ItemChatListRowBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = getItem(position)
        val isSelected = selectedChatIdsSet.contains(item.chatId)

        holder.bind(item, isSelected)

        // Root element row matrix trigger configuration pass
        holder.itemView.setOnClickListener {
            onChatClicked(item)
        }

        holder.itemView.setOnLongClickListener {
            onChatLongPressed(item)
            true
        }
    }

    fun submitActiveSelectionsList(updatedSet: Set<String>) {
        selectedChatIdsSet.clear()
        selectedChatIdsSet.addAll(updatedSet)
        notifyItemRangeChanged(0, itemCount)
    }

    class ChatViewHolder(private val binding: ItemChatListRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: MessageEntity, isSelected: Boolean) {
            val context = binding.root.context

            // 1. Bind Text Metadata Contents
            binding.tvProfileName.text = message.senderName
            binding.tvMessagePreview.text = message.textContent

            // 2. Exact Dynamic Avatar Rules Matching "image_e2b3dc.jpg"
            // FIXED: String "null" aur white-spaces validation wrapper clean check
            val rawUri = message.localMediaUri
            val cleanProfileUri = if (rawUri != null && rawUri.isNotBlank() && rawUri.trim().lowercase() != "null") {
                rawUri.trim()
            } else {
                null
            }

            if (cleanProfileUri != null) {
                // Case A: Load real contact picture / company brand logo cleanly
                com.bumptech.glide.Glide.with(context)
                    .load(cleanProfileUri)
                    .placeholder(R.drawable.ic_wa_group_fallback) // Smooth layout mapping loading transition
                    .error(R.drawable.ic_wa_group_fallback)       // Fallback circle frame asset fallback
                    .circleCrop()                                 // Ensures perfect circle layout container behavior
                    .into(binding.ivUserAvatar)
            } else {
                // Case B: Explicit slate-blue group silhouette fallback seen in "image_e2b3dc.jpg"
                binding.ivUserAvatar.setImageResource(R.drawable.ic_wa_group_fallback)
            }

            // 3. Handle Active Unread Notification UI States
            if (message.isUnread) {
                binding.cardChatRoot.setCardBackgroundColor(context.getColor(R.color.chat_unread_bg_tint))
                binding.viewOnlineIndicator.visibility = View.VISIBLE
                binding.tvUnreadCounterBadge.visibility = View.VISIBLE
            } else {
                binding.cardChatRoot.setCardBackgroundColor(context.getColor(android.R.color.white))
                binding.viewOnlineIndicator.visibility = View.GONE
                binding.tvUnreadCounterBadge.visibility = View.GONE
            }

            // 4. Handle Structural Multi-Select Card Overlays (Checkbox takes rendering priority)
            if (isSelected) {
                binding.ivRowCheckedMarker.visibility = View.VISIBLE
                binding.tvUnreadCounterBadge.visibility = View.GONE
                binding.cardChatRoot.setStrokeColor(context.getColor(R.color.chat_unread_indicator))
                binding.cardChatRoot.strokeWidth = 3
            } else {
                binding.ivRowCheckedMarker.visibility = View.GONE
                binding.cardChatRoot.strokeWidth = 0
            }
        }
    }
    class ChatDiffCallback : DiffUtil.ItemCallback<MessageEntity>() {
        override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean = oldItem == newItem
    }
}