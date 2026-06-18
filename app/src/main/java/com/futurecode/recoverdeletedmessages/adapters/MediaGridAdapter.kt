package com.futurecode.recoverdeletedmessages.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.net.Uri
import com.bumptech.glide.Glide
import com.futurecode.recoverdeletedmessages.databinding.ItemMediaGridBinding

import com.futurecode.recoverdeletedmessages.model.MediaItem
import com.futurecode.recoverdeletedmessages.utils.Constants
import com.futurecode.recoverdeletedmessages.utils.FileUtils
import java.io.File
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.databinding.ItemNativeAdPlaceholderBinding // Ensure this name matches your language ad layout binding


//class MediaGridAdapter(
//    // Clean handlers to communicate interactions safely with WAStickerFragment / WAVideoFragment
//    private val onCardClicked: (MediaItem) -> Unit,
//    private val onCardLongPressed: (MediaItem) -> Unit
//) : ListAdapter<MediaItem, MediaGridAdapter.MediaViewHolder>(DIFF_CALLBACK) {
//
//    // Set storage node to efficiently track multiple selected item IDs
//    private val selectedItems = mutableSetOf<Long>()
//
//    inner class MediaViewHolder(val binding: ItemMediaGridBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
//        val binding = ItemMediaGridBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return MediaViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
//        val item = getItem(position)
//        with(holder.binding) {
//
//            // 1. Image Routing / Binding Management via Glide Engine
//            val imageSource: Any = if (item.filePath.startsWith("content://")) {
//                Uri.parse(item.filePath)
//            } else {
//                File(item.filePath)
//            }
//
//            Glide.with(root.context)
//                .load(imageSource)
//                .centerCrop()
//                .placeholder(android.R.drawable.ic_menu_gallery)
//                .into(ivThumbnail)
//
//            // 2. Conditional Badge Overlays Rendering
//            tvNewBadge.visibility = if (item.isNew) View.VISIBLE else View.GONE
//
//            when (item.mediaType) {
//                Constants.MEDIA_TYPE_VIDEO -> {
//                    ivPlayIcon.visibility = View.VISIBLE
//                    tvDuration.visibility = if (item.duration > 0) View.VISIBLE else View.GONE
//                    tvDuration.text = FileUtils.formatDuration(item.duration)
//                    tvGifBadge.visibility = View.GONE
//                }
//                Constants.MEDIA_TYPE_GIF -> {
//                    ivPlayIcon.visibility = View.GONE
//                    tvDuration.visibility = View.GONE
//                    tvGifBadge.visibility = View.VISIBLE
//                }
//                else -> {
//                    ivPlayIcon.visibility = View.GONE
//                    tvDuration.visibility = View.GONE
//                    tvGifBadge.visibility = View.GONE
//                }
//            }
//
//            // =========================================================================
//            // ✅ PREMIUM SELECTION MODEL & CUSTOM DRAWABLES MUTATION ENGINE
//            // =========================================================================
//            val isSelected = selectedItems.contains(item.id)
//
//            if (isSelected) {
//                selectionOverlay.visibility = View.VISIBLE
//                ivSelected.setImageResource(R.drawable.ic_check_circle_filled)
//            } else {
//                selectionOverlay.visibility = View.GONE
//                ivSelected.setImageResource(R.drawable.ic_radio_unselected)
//            }
//
//            // 3. Smart Tap Gestures Processing Logic Pipeline
//            root.setOnClickListener {
//                if (hasSelection()) {
//                    // If selection mode active, single tap behaves natively as a toggle checkbox selection
//                    toggleSelection(item)
//                    onCardLongPressed(item) // Updates dynamic footer UI counter inside active fragments
//                } else {
//                    onCardClicked(item)
//                }
//            }
//
//            // 4. Contextual Long Press Event Trigger Action
//            root.setOnLongClickListener {
//                toggleSelection(item)
//                onCardLongPressed(item) // Signal state shifts to container wrappers instantly
//                true
//            }
//        }
//    }
//
//    /**
//     * Toggles layout selection checkboxes indices for targeted items safely.
//     */
//    fun toggleSelection(item: MediaItem) {
//        if (selectedItems.contains(item.id)) {
//            selectedItems.remove(item.id)
//        } else {
//            selectedItems.add(item.id)
//        }
//        // Force element index refreshing directly to render animation transitions flawlessly
//        notifyItemChanged(currentList.indexOf(item))
//    }
//
//    /**
//     * Filters list content to retrieve checked data models payload array.
//     */
//    fun getSelectedItems(): List<MediaItem> = currentList.filter { selectedItems.contains(it.id) }
//
//    /**
//     * Purges runtime cached layout indexes and resets selections states smoothly.
//     */
//    fun clearSelection() {
//        selectedItems.clear()
//        notifyDataSetChanged()
//    }
//
//    fun toggleSelectAll() {
//        if (selectedItems.size == currentList.size) {
//            // Agar saare items selected hain, toh sabko clear kar do
//            selectedItems.clear()
//        } else {
//            // Varna saare items ki unique IDs ko set mein add kar do
//            currentList.forEach { item ->
//                selectedItems.add(item.id)
//            }
//        }
//        // Pure layout matrix ko refresh karein taaki checkmarks instantly dikhein
//        notifyDataSetChanged()
//    }
//
//    /**
//     * Verifies if any item is currently holding active selection properties.
//     */
//    fun hasSelection() = selectedItems.isNotEmpty()
//
//    companion object {
//        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MediaItem>() {
//            override fun areItemsTheSame(old: MediaItem, new: MediaItem) = old.id == new.id
//            override fun areContentsTheSame(old: MediaItem, new: MediaItem) = old == new
//        }
//    }
//}




class MediaGridAdapter(
    private val onCardClicked: (MediaItem) -> Unit,
    private val onCardLongPressed: (MediaItem) -> Unit,
    private val onAdBindingTriggered: (binding: ItemNativeAdPlaceholderBinding) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private val selectedItems = mutableSetOf<Long>()

    companion object {
        private const val TYPE_MEDIA_ITEM = 0
        private const val TYPE_AD_ITEM = 1

        // Dynamic Interval Configuration Node
        private const val AD_INTERVAL = 3

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
            is MediaItem -> TYPE_MEDIA_ITEM
            is String -> TYPE_AD_ITEM
            else -> throw IllegalArgumentException("Invalid Object Processing Type mapping")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_MEDIA_ITEM -> {
                val binding = ItemMediaGridBinding.inflate(inflater, parent, false)
                MediaViewHolder(binding)
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
            is MediaViewHolder -> {
                val item = getItem(position) as MediaItem
                holder.bind(item)
            }
            is AdViewHolder -> {
                holder.bind()
            }
        }
    }

    // =========================================================================
    // 📸 HOLDER 1: MEDIA DATA VIEW HOLDER
    // =========================================================================
    inner class MediaViewHolder(val binding: ItemMediaGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediaItem) {
            with(binding) {
                val imageSource: Any = if (item.filePath.startsWith("content://")) {
                    Uri.parse(item.filePath)
                } else {
                    File(item.filePath)
                }

                Glide.with(root.context)
                    .load(imageSource)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivThumbnail)

                tvNewBadge.visibility = if (item.isNew) View.VISIBLE else View.GONE

                when (item.mediaType) {
                    Constants.MEDIA_TYPE_VIDEO -> {
                        ivPlayIcon.visibility = View.VISIBLE
                        tvDuration.visibility = if (item.duration > 0) View.VISIBLE else View.GONE
                        tvDuration.text = FileUtils.formatDuration(item.duration)
                        tvGifBadge.visibility = View.GONE
                    }
                    Constants.MEDIA_TYPE_GIF -> {
                        ivPlayIcon.visibility = View.GONE
                        tvDuration.visibility = View.GONE
                        tvGifBadge.visibility = View.VISIBLE
                    }
                    else -> {
                        ivPlayIcon.visibility = View.GONE
                        tvDuration.visibility = View.GONE
                        tvGifBadge.visibility = View.GONE
                    }
                }

                val isSelected = selectedItems.contains(item.id)
                if (isSelected) {
                    selectionOverlay.visibility = View.VISIBLE
                    ivSelected.setImageResource(R.drawable.ic_check_circle_filled)
                } else {
                    selectionOverlay.visibility = View.GONE
                    ivSelected.setImageResource(R.drawable.ic_radio_unselected)
                }

                root.setOnClickListener {
                    if (hasSelection()) {
                        // ✅ FIXED: Now safely passing the adapter tracking instance context
                        toggleSelection(item, bindingAdapterPosition)
                        onCardLongPressed(item)
                    } else {
                        onCardClicked(item)
                    }
                }

                root.setOnLongClickListener {
                    toggleSelection(item, bindingAdapterPosition)
                    onCardLongPressed(item)
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
    // ⚙️ SELECTION LAYER UTILITIES
    // =========================================================================

    // ✅ FIXED: Takes explicit adapter position parameter to avoid -1 or mixed data row index crashes
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

    fun getSelectedItems(): List<MediaItem> {
        return currentList.filterIsInstance<MediaItem>().filter { selectedItems.contains(it.id) }
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun toggleSelectAll() {
        val allMediaItems = currentList.filterIsInstance<MediaItem>()
        // ✅ FIXED: Enhanced logic check to ensure seamless selection updates
        if (selectedItems.size >= allMediaItems.size && allMediaItems.isNotEmpty()) {
            selectedItems.clear()
        } else {
            allMediaItems.forEach { item ->
                selectedItems.add(item.id)
            }
        }
        notifyDataSetChanged()
    }

    fun hasSelection() = selectedItems.isNotEmpty()

    fun submitMediaWithAds(mediaList: List<MediaItem>) {
        val combinedList = mutableListOf<Any>()
        var mediaCount = 0

        mediaList.forEach { item ->
            combinedList.add(item)
            mediaCount++

            if (mediaCount % AD_INTERVAL == 0) {
                combinedList.add("AD_PLACEHOLDER_${mediaCount}")
            }
        }
        submitList(combinedList)
    }
}