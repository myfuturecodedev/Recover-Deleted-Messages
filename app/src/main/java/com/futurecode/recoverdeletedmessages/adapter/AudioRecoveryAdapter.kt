package com.futurecode.recoverdeletedmessages.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.data.MessageEntity
import com.futurecode.recoverdeletedmessages.databinding.ItemAudioListRowBinding

class AudioRecoveryAdapter(
    private val onPlayTriggered: (MessageEntity, Int) -> Unit,
    private val onRowLongPressed: (MessageEntity) -> Unit
) : ListAdapter<MessageEntity, AudioRecoveryAdapter.AudioViewHolder>(AudioDiffCallback()) {

    private val checkedAudioIdsSet = mutableSetOf<Long>()
    private var currentlyPlayingIndexPosition = -1
    private var currentPlayingProgressPercentage = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AudioViewHolder(ItemAudioListRowBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    fun updateSelectionCache(updatedSet: Set<Long>) {
        checkedAudioIdsSet.clear()
        checkedAudioIdsSet.addAll(updatedSet)
        notifyItemRangeChanged(0, itemCount)
    }

    fun setPlaybackProgressUpdate(position: Int, progress: Int) {
        currentlyPlayingIndexPosition = position
        currentPlayingProgressPercentage = progress
        notifyItemChanged(position)
    }

    fun clearPlaybackProgressTracking() {
        val previousPosition = currentlyPlayingIndexPosition
        currentlyPlayingIndexPosition = -1
        currentPlayingProgressPercentage = 0
        if (previousPosition != -1) notifyItemChanged(previousPosition)
    }

    inner class AudioViewHolder(private val binding: ItemAudioListRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageEntity, position: Int) {
            binding.tvAudioFileName.text = item.senderName
            binding.tvAudioTimestamp.text = "10:52 AM"

            val isSelected = checkedAudioIdsSet.contains(item.id)
            if (isSelected) {
                binding.layoutAudioCard.setBackgroundResource(R.drawable.bg_chat_row_selected)
                binding.ivAudioSelectionCheck.visibility = View.VISIBLE
                binding.bgAudioIconCircle.visibility = View.GONE
                binding.ivAudioTypeIcon.visibility = View.GONE
            } else {
                binding.layoutAudioCard.setBackgroundResource(R.color.chat_row_unselected)
                binding.ivAudioSelectionCheck.visibility = View.GONE
                binding.bgAudioIconCircle.visibility = View.VISIBLE
                binding.ivAudioTypeIcon.visibility = View.VISIBLE
            }

            // Direct mapping tracking for custom media player loops
            if (position == currentlyPlayingIndexPosition) {
                binding.ivAudioTypeIcon.setImageResource(R.drawable.ic_cross) // Click again to stop audio tracking pass
                binding.seekbarAudioProgress.visibility = View.VISIBLE
                binding.seekbarAudioProgress.progress = currentPlayingProgressPercentage
            } else {
                binding.ivAudioTypeIcon.setImageResource(R.drawable.ic_play_arrow)
                binding.seekbarAudioProgress.visibility = View.GONE
            }

            binding.bgAudioIconCircle.setOnClickListener { onPlayTriggered(item, position) }
            binding.root.setOnClickListener {
                if (checkedAudioIdsSet.isNotEmpty()) onRowLongPressed(item) else onPlayTriggered(item, position)
            }
            binding.root.setOnLongClickListener {
                onRowLongPressed(item)
                true
            }
        }
    }

    class AudioDiffCallback : DiffUtil.ItemCallback<MessageEntity>() {
        override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean = oldItem == newItem
    }
}