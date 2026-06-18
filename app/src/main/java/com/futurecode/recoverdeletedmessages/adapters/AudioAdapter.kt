package com.futurecode.recoverdeletedmessages.adapters

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.databinding.ItemAudioBinding
import com.futurecode.recoverdeletedmessages.model.MediaItem
import com.futurecode.recoverdeletedmessages.utils.FileUtils
import com.futurecode.recoverdeletedmessages.databinding.ItemNativeAdPlaceholderBinding // Ensure this name matches your ad placeholder layout binding name
import java.io.File

//class AudioAdapter(
//    // Dono parameters ko standard callbacks ke liye maintain rakha hai bhai
//    private val onItemClick: (MediaItem) -> Unit,
//    private val onCardLongPressed: ((MediaItem) -> Unit)? = null
//) : ListAdapter<MediaItem, AudioAdapter.AudioViewHolder>(DIFF_CALLBACK) {
//
//    // 1. AAPKA PURANA ARTIFACTS LOGIC (KUCH NAHI HATAYA)
//    private var currentMediaPlayer: MediaPlayer? = null
//    private var currentPlayingPath: String? = null
//
//    // 2. NEW: MULTI-SELECTION STATE COLLECTION
//    private val selectedItems = mutableSetOf<Long>()
//
//    inner class AudioViewHolder(val binding: ItemAudioBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
//        val binding = ItemAudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return AudioViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
//        val item = getItem(position)
//        with(holder.binding) {
//            tvFileName.text = item.fileName
//            tvDate.text = FileUtils.formatDate(item.dateAdded)
//
//            if (item.duration > 0) {
//                tvDuration.text = FileUtils.formatDuration(item.duration)
//            } else {
//                tvDuration.text = "--:--"
//            }
//
//            // =========================================================================
//            // 🔄 AAPKA PURANA PLAYER CONDITIONAL DRAWABLES ENGINE (SAME AS BEFORE)
//            // =========================================================================
//            val isPlaying = currentPlayingPath == item.filePath
//
//            ivPlayStop.setImageResource(
//                if (isPlaying) R.drawable.mp3
//                else R.drawable.mp3
//            )
//
//            // =========================================================================
//            // ✅ NEW: MULTI-SELECTION DRAWABLES STATE MUTATOR
//            // =========================================================================
//            val isSelected = selectedItems.contains(item.id)
//            if (isSelected) {
//                ivSelected.setImageResource(R.drawable.ic_check_circle_filled)
//            } else {
//                ivSelected.setImageResource(R.drawable.ic_radio_unselected)
//            }
//
//            // =========================================================================
//            // 🛠️ SMART SELECTION & PLAY GESTURE CONTROLLERS
//            // =========================================================================
//
//            // 1. Play Button Click (Drives player logic OR selection toggle if active)
//            ivPlayStop.setOnClickListener {
//                if (hasSelection()) {
//                    toggleSelection(item)
//                    onCardLongPressed?.invoke(item)
//                } else {
//                    if (isPlaying) {
//                        stopAudio()
//                    } else {
//                        onItemClick(item)
//                    }
//                    notifyDataSetChanged()
//                }
//            }
//
//            // 2. Core Row Layout Tap (Drives viewer layout OR selection toggle if active)
//            root.setOnClickListener {
//                if (hasSelection()) {
//                    toggleSelection(item)
//                    onCardLongPressed?.invoke(item)
//                } else {
//                    onItemClick(item)
//                }
//            }
//
//            // 3. Long Press Event Gesture to activate selection triggers
//            root.setOnLongClickListener {
//                toggleSelection(item)
//                onCardLongPressed?.invoke(item)
//                true
//            }
//        }
//    }
//
//    // =========================================================================
//    // 🔄 AAPKA PURANA PLAYER LIFECYCLE CONTROLS (UNTOUCHED)
//    // =========================================================================
//    private fun stopAudio() {
//        currentMediaPlayer?.stop()
//        currentMediaPlayer?.release()
//        currentMediaPlayer = null
//        currentPlayingPath = null
//    }
//
//    fun releasePlayer() = stopAudio()
//
//    // =========================================================================
//    // ✅ NEW: ADDED SELECTION LAYER UTILITIES
//    // =========================================================================
//    fun toggleSelection(item: MediaItem) {
//        if (selectedItems.contains(item.id)) {
//            selectedItems.remove(item.id)
//        } else {
//            selectedItems.add(item.id)
//        }
//        notifyItemChanged(currentList.indexOf(item))
//    }
//
//    fun toggleSelectAll() {
//        if (selectedItems.size == currentList.size) {
//            selectedItems.clear()
//        } else {
//            currentList.forEach { item -> selectedItems.add(item.id) }
//        }
//        notifyDataSetChanged()
//    }
//
//    fun getSelectedItems(): List<MediaItem> = currentList.filter { selectedItems.contains(it.id) }
//
//    fun clearSelection() {
//        selectedItems.clear()
//        notifyDataSetChanged()
//    }
//
//    fun hasSelection() = selectedItems.isNotEmpty()
//
//    companion object {
//        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MediaItem>() {
//            override fun areItemsTheSame(old: MediaItem, new: MediaItem) = old.id == new.id
//            override fun areContentsTheSame(old: MediaItem, new: MediaItem) = old == new
//        }
//    }
//}






class AudioAdapter(
    private val onItemClick: (MediaItem) -> Unit,
    private val onCardLongPressed: ((MediaItem) -> Unit)? = null,
    // ✅ NEW: Added ad callback tracker delegate to separate view handling from adapter logic
    private val onAdBindingTriggered: (binding: ItemNativeAdPlaceholderBinding) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    // 1. AAPKA PURANA ARTIFACTS LOGIC (SAME AS BEFORE)
    private var currentMediaPlayer: MediaPlayer? = null
    private var currentPlayingPath: String? = null

    // 2. MULTI-SELECTION STATE COLLECTION
    private val selectedItems = mutableSetOf<Long>()

    companion object {
        private const val TYPE_AUDIO_ITEM = 0
        private const val TYPE_AD_ITEM = 1

        // ✅ Every 6 rows interval, inject a native ad layout token
        private const val AD_INTERVAL = 6

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return if (oldItem is MediaItem && newItem is MediaItem) {
                    oldItem.id == newItem.id
                } else oldItem is String && newItem is String && oldItem == newItem
            }
            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MediaItem -> TYPE_AUDIO_ITEM
            is String -> TYPE_AD_ITEM
            else -> throw IllegalArgumentException("Invalid Object Processing Type mapping")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_AUDIO_ITEM -> {
                val binding = ItemAudioBinding.inflate(inflater, parent, false)
                AudioViewHolder(binding)
            }
            TYPE_AD_ITEM -> {
                val binding = ItemNativeAdPlaceholderBinding.inflate(inflater, parent, false)
                AdViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unsupported Layout State processing metrics")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AudioViewHolder -> {
                val item = getItem(position) as MediaItem
                holder.bind(item, holder.bindingAdapterPosition)
            }
            is AdViewHolder -> {
                holder.bind()
            }
        }
    }

    // =========================================================================
    // 🔊 HOLDER 1: AUDIO DATA ROW VIEW HOLDER
    // =========================================================================
    inner class AudioViewHolder(val binding: ItemAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediaItem, position: Int) {
            with(binding) {
                tvFileName.text = item.fileName
                tvDate.text = FileUtils.formatDate(item.dateAdded)

                if (item.duration > 0) {
                    tvDuration.text = FileUtils.formatDuration(item.duration)
                } else {
                    tvDuration.text = "--:--"
                }

                // AAPKA PURANA PLAYER CONDITIONAL DRAWABLES ENGINE
                val isPlaying = currentPlayingPath == item.filePath
                ivPlayStop.setImageResource(
                    if (isPlaying) R.drawable.mp3
                    else R.drawable.mp3
                )

                // MULTI-SELECTION DRAWABLES STATE MUTATOR
                val isSelected = selectedItems.contains(item.id)
                if (isSelected) {
                    ivSelected.setImageResource(R.drawable.ic_check_circle_filled)
                } else {
                    ivSelected.setImageResource(R.drawable.ic_radio_unselected)
                }

                // 1. Play Button Click (Drives player logic OR selection toggle if active)
                ivPlayStop.setOnClickListener {
                    if (hasSelection()) {
                        toggleSelection(item, position)
                        onCardLongPressed?.invoke(item)
                    } else {
                        if (isPlaying) {
                            stopAudio()
                        } else {
                            onItemClick(item)
                        }
                        notifyDataSetChanged()
                    }
                }

                // 2. Core Row Layout Tap (Drives viewer layout OR selection toggle if active)
                root.setOnClickListener {
                    if (hasSelection()) {
                        toggleSelection(item, position)
                        onCardLongPressed?.invoke(item)
                    } else {
                        onItemClick(item)
                    }
                }

                // 3. Long Press Event Gesture to activate selection triggers
                root.setOnLongClickListener {
                    toggleSelection(item, position)
                    onCardLongPressed?.invoke(item)
                    true
                }
            }
        }
    }

    // =========================================================================
    // 📢 HOLDER 2: NATIVE AD VIEW HOLDER
    // =========================================================================
    inner class AdViewHolder(private val binding: ItemNativeAdPlaceholderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var isAdInitialized = false

        fun bind() {
            if (!isAdInitialized) {
                onAdBindingTriggered(binding)
                isAdInitialized = true
            }
        }
    }

    // =========================================================================
    // 🔄 AAPKA PURANA PLAYER LIFECYCLE CONTROLS (UNTOUCHED)
    // =========================================================================
    private fun stopAudio() {
        currentMediaPlayer?.stop()
        currentMediaPlayer?.release()
        currentMediaPlayer = null
        currentPlayingPath = null
    }

    fun releasePlayer() = stopAudio()

    // =========================================================================
    // ✅ ADDED SELECTION LAYER UTILITIES (FIXED FOR MIXED AD POSITIONS)
    // =========================================================================

    // Take runtime binding position context to completely protect from index crashes
    fun toggleSelection(item: MediaItem, position: Int) {
        if (selectedItems.contains(item.id)) {
            selectedItems.remove(item.id)
        } else {
            selectedItems.add(item.id)
        }
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position)
        }
    }

    fun toggleSelectAll() {
        val allAudioItems = currentList.filterIsInstance<MediaItem>()
        if (selectedItems.size >= allAudioItems.size && allAudioItems.isNotEmpty()) {
            selectedItems.clear()
        } else {
            allAudioItems.forEach { item -> selectedItems.add(item.id) }
        }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<MediaItem> = currentList.filterIsInstance<MediaItem>().filter { selectedItems.contains(it.id) }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun hasSelection() = selectedItems.isNotEmpty()

    /**
     * ✅ NEW METHOD: Dynamically maps media data elements list and injects Ad layout token references every 6 items
     */
    fun submitAudioWithAds(audioList: List<MediaItem>) {
        val combinedList = mutableListOf<Any>()
        var audioCount = 0

        audioList.forEach { item ->
            combinedList.add(item)
            audioCount++

            if (audioCount % AD_INTERVAL == 0) {
                combinedList.add("AUDIO_LIST_AD_${audioCount}")
            }
        }
        submitList(combinedList)
    }
}