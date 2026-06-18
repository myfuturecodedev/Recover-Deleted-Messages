package com.futurecode.recoverdeletedmessages.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.futurecode.recoverdeletedmessages.databinding.ItemDocumentBinding
import com.futurecode.recoverdeletedmessages.model.MediaItem
import com.futurecode.recoverdeletedmessages.utils.FileUtils
import java.io.File
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.databinding.ItemNativeAdPlaceholderBinding // Ensure this import matches your ad placeholder binding layout name

//class DocumentAdapter(
//    private val onItemClick: (MediaItem) -> Unit,
//    private val onCardLongPressed: ((MediaItem) -> Unit)? = null
//) : ListAdapter<MediaItem, DocumentAdapter.DocumentViewHolder>(DIFF_CALLBACK) {
//
//    // Persistent storage set to track chosen items ids indices
//    private val selectedItems = mutableSetOf<Long>()
//
//    inner class DocumentViewHolder(val binding: ItemDocumentBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
//        val binding = ItemDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return DocumentViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
//        val item = getItem(position)
//        with(holder.binding) {
//            tvFileName.text = item.fileName
//            tvFileInfo.text = "${FileUtils.formatFileSize(item.fileSize)} • ${FileUtils.formatDate(item.dateAdded)}"
//
//            // =========================================================================
//            // ✅ DYNAMIC SELECTION ASSETS VISUAL SYSTEM
//            // =========================================================================
//            val isSelected = selectedItems.contains(item.id)
//            if (isSelected) {
//                ivSelected.setImageResource(R.drawable.ic_check_circle_filled)
//            } else {
//                ivSelected.setImageResource(R.drawable.ic_radio_unselected)
//            }
//
//            // AAPKA PURANA SHARE LOGIC (UNTOUCHED)
//            ivShare.setOnClickListener {
//                if (!hasSelection()) {
//                    val file = File(item.filePath)
//                    if (file.exists()) {
//                        val ctx = root.context
//                        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", file)
//                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
//                            type = FileUtils.getMimeType(file)
//                            putExtra(Intent.EXTRA_STREAM, uri)
//                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                        }
//                        ctx.startActivity(Intent.createChooser(shareIntent, "Share via"))
//                    }
//                } else {
//                    // If selection mode active, share button intercepts click to select/unselect item
//                    toggleSelection(item)
//                    onCardLongPressed?.invoke(item)
//                }
//            }
//
//            // Smart row click listener
//            root.setOnClickListener {
//                if (hasSelection()) {
//                    toggleSelection(item)
//                    onCardLongPressed?.invoke(item)
//                } else {
//                    onItemClick(item)
//                }
//            }
//
//            // Long click gesture listener activation
//            root.setOnLongClickListener {
//                toggleSelection(item)
//                onCardLongPressed?.invoke(item)
//                true
//            }
//        }
//    }
//
//    // =========================================================================
//    // ✅ SELECTION ENGINE CONTROLS
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


class DocumentAdapter(
    private val onItemClick: (MediaItem) -> Unit,
    private val onCardLongPressed: ((MediaItem) -> Unit)? = null,
    // ✅ NEW: Callback stream to securely pass ad views rendering mechanics to the fragment layer
    private val onAdBindingTriggered: (binding: ItemNativeAdPlaceholderBinding) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    // Persistent storage set to track chosen items ids indices
    private val selectedItems = mutableSetOf<Long>()

    companion object {
        private const val TYPE_DOCUMENT_ITEM = 0
        private const val TYPE_AD_ITEM = 1

        // ✅ Inject a native ad view layout token every 6 items interval
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
            is MediaItem -> TYPE_DOCUMENT_ITEM
            is String -> TYPE_AD_ITEM
            else -> throw IllegalArgumentException("Invalid Object Processing Type mapping inside documents data model matrix")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_DOCUMENT_ITEM -> {
                val binding = ItemDocumentBinding.inflate(inflater, parent, false)
                DocumentViewHolder(binding)
            }
            TYPE_AD_ITEM -> {
                val binding = ItemNativeAdPlaceholderBinding.inflate(inflater, parent, false)
                AdViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unsupported Layout State processing metrics for Document node items")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DocumentViewHolder -> {
                val item = getItem(position) as MediaItem
                holder.bind(item, holder.bindingAdapterPosition)
            }
            is AdViewHolder -> {
                holder.bind()
            }
        }
    }

    // =========================================================================
    // 📄 HOLDER 1: DOCUMENT ROW VIEW HOLDER
    // =========================================================================
    inner class DocumentViewHolder(val binding: ItemDocumentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediaItem, position: Int) {
            with(binding) {
                tvFileName.text = item.fileName
                tvFileInfo.text = "${FileUtils.formatFileSize(item.fileSize)} • ${FileUtils.formatDate(item.dateAdded)}"

                // DYNAMIC SELECTION ASSETS VISUAL SYSTEM
                val isSelected = selectedItems.contains(item.id)
                if (isSelected) {
                    ivSelected.setImageResource(R.drawable.ic_check_circle_filled)
                } else {
                    ivSelected.setImageResource(R.drawable.ic_radio_unselected)
                }

                // AAPKA PURANA SHARE LOGIC (UNTOUCHED - safely checks selection states)
                ivShare.setOnClickListener {
                    if (!hasSelection()) {
                        val file = File(item.filePath)
                        if (file.exists()) {
                            val ctx = root.context
                            val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", file)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = FileUtils.getMimeType(file)
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            ctx.startActivity(Intent.createChooser(shareIntent, "Share via"))
                        }
                    } else {
                        toggleSelection(item, position)
                        onCardLongPressed?.invoke(item)
                    }
                }

                // Smart row click listener
                root.setOnClickListener {
                    if (hasSelection()) {
                        toggleSelection(item, position)
                        onCardLongPressed?.invoke(item)
                    } else {
                        onItemClick(item)
                    }
                }

                // Long click gesture listener activation
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
    // ⚙️ SELECTION ENGINE CONTROLS
    // =========================================================================

    // ✅ FIXED: Safely takes adapter view position parameter context to completely protect from index crashes
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
        val allDocs = currentList.filterIsInstance<MediaItem>()
        if (selectedItems.size >= allDocs.size && allDocs.isNotEmpty()) {
            selectedItems.clear()
        } else {
            allDocs.forEach { item -> selectedItems.add(item.id) }
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
     * ✅ NEW METHOD: Dynamic data streaming mapper injection to insert ad placeholders strings safely
     */
    fun submitDocumentsWithAds(documentList: List<MediaItem>) {
        val combinedList = mutableListOf<Any>()
        var docCount = 0

        documentList.forEach { item ->
            combinedList.add(item)
            docCount++

            if (docCount % AD_INTERVAL == 0) {
                combinedList.add("DOC_AD_PLACEHOLDER_${docCount}")
            }
        }
        submitList(combinedList)
    }
}