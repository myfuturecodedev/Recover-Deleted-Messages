package com.futurecode.recoverdeletedmessages.adapter
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.futurecode.recoverdeletedmessages.R
//import com.futurecode.recoverdeletedmessages.databinding.ItemChatListRowBinding
//import com.futurecode.recoverdeletedmessages.data.MessageEntity
//
//class ChatListAdapter(
//    private val onChatClicked: (MessageEntity) -> Unit,
//    private val onChatLongPressed: (MessageEntity) -> Unit
//) : ListAdapter<MessageEntity, ChatListAdapter.ChatViewHolder>(ChatDiffCallback()) {
//
//    // FIXED: Changed set token to String to strictly cache unique senderName profiles
//    private val selectedChatIdsSet = mutableSetOf<String>()
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        return ChatViewHolder(ItemChatListRowBinding.inflate(inflater, parent, false))
//    }
//
//    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
//        val item = getItem(position)
//
//        // FIXED: Using item.senderName instead of the unresolved reference 'chatId'
//        val isSelected = selectedChatIdsSet.contains(item.senderName)
//
//        holder.bind(item, isSelected)
//
//        holder.itemView.setOnClickListener {
//            onChatClicked(item)
//        }
//
//        holder.itemView.setOnLongClickListener {
//            onChatLongPressed(item)
//            true
//        }
//    }
//
//    fun submitActiveSelectionsList(updatedSet: Set<String>) {
//        selectedChatIdsSet.clear()
//        selectedChatIdsSet.addAll(updatedSet)
//        notifyItemRangeChanged(0, itemCount)
//    }
//
//    class ChatViewHolder(private val binding: ItemChatListRowBinding) : RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(message: MessageEntity, isSelected: Boolean) {
//            val context = binding.root.context
//
//            // 1. Bind Text Metadata Contents
//            binding.tvProfileName.text = message.senderName
//
//            // FIXED: Changed message.textContent to message.messageText to match your Room entity schema
//            binding.tvMessagePreview.text = message.messageText
//
//            // 2. Avatar Handling
//            // Since localMediaUri isn't stored for standard plain texts inside your core Room DB fields,
//            // we safely default to fallback layout assets matching standard UI behaviors.
//            binding.ivUserAvatar.setImageResource(R.drawable.ic_wa_group_fallback)
//
//            // 3. Handle Active Unread / Deleted Notification UI States
//            // FIXED: Using dynamic properties evaluation layer from core models (isDeleted context check)
//            val isUnreadState = message.isDeleted == 1 // Marks deleted for everyone states instantly if required
//
//            if (isUnreadState) {
//                binding.cardChatRoot.setCardBackgroundColor(context.getColor(R.color.chat_unread_bg_tint))
//                binding.viewOnlineIndicator.visibility = View.VISIBLE
//                binding.tvUnreadCounterBadge.visibility = View.VISIBLE
//            } else {
//                binding.cardChatRoot.setCardBackgroundColor(context.getColor(android.R.color.white))
//                binding.viewOnlineIndicator.visibility = View.GONE
//                binding.tvUnreadCounterBadge.visibility = View.GONE
//            }
//
//            // 4. Handle Structural Multi-Select Card Overlays (Checkbox takes rendering priority)
//            if (isSelected) {
//                binding.ivRowCheckedMarker.visibility = View.VISIBLE
//                binding.tvUnreadCounterBadge.visibility = View.GONE
//                binding.cardChatRoot.setStrokeColor(context.getColor(R.color.chat_unread_indicator))
//                binding.cardChatRoot.strokeWidth = 3
//            } else {
//                binding.ivRowCheckedMarker.visibility = View.GONE
//                binding.cardChatRoot.strokeWidth = 0
//            }
//        }
//    }
//
//    class ChatDiffCallback : DiffUtil.ItemCallback<MessageEntity>() {
//        override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean = oldItem.id == newItem.id
//        override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean = oldItem == newItem
//    }
//}