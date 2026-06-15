package com.futurecode.recoverdeletedmessages.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.data.MessageEntity
import com.futurecode.recoverdeletedmessages.databinding.ItemDocumentHeaderBinding
import com.futurecode.recoverdeletedmessages.databinding.ItemDocumentRowBinding
import java.text.SimpleDateFormat
import java.util.Locale

class DocumentListAdapter(
    private val onDocClicked: (MessageEntity) -> Unit,
    private val onDocLongPressed: (MessageEntity) -> Unit,
    private val onInlineShareClicked: (MessageEntity) -> Unit
) : ListAdapter<MessageEntity, RecyclerView.ViewHolder>(DocDiffCallback()) {

    private val selectedFilePathsSet = mutableSetOf<String>()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_DOCUMENT_ROW = 1
    }

    override fun getItemViewType(position: Int): Int {
        // Items flagged with a negative ID token represent programmatic text timeline headers
        return if (getItem(position).id < 0) TYPE_HEADER else TYPE_DOCUMENT_ROW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(ItemDocumentHeaderBinding.inflate(inflater, parent, false))
        } else {
            DocRowViewHolder(ItemDocumentRowBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is HeaderViewHolder) {
            // FIXED: Changed item.textContent to item.messageText
            holder.binding.tvTimelineSectionTitle.text = item.messageText
        } else if (holder is DocRowViewHolder) {
            holder.bind(item)
        }
    }

    fun updateSelectionCache(updatedSet: Set<String>) {
        selectedFilePathsSet.clear()
        selectedFilePathsSet.addAll(updatedSet)
        notifyItemRangeChanged(0, itemCount)
    }

    inner class HeaderViewHolder(val binding: ItemDocumentHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class DocRowViewHolder(private val binding: ItemDocumentRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageEntity) {
            // Using messageText as fallback for safety if localMediaUri path is null
            val path = item.localMediaUri ?: item.messageText
            val extension =
                FileUtils.getFileExtension(item.senderName).uppercase(Locale.getDefault())

            binding.tvDocTitleName.text = FileUtils.getFileNameWithoutExtension(item.senderName)

            val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

            // FIXED: Changed item.textContent to item.messageText
            binding.tvDocFileDetails.text =
                "${item.messageText} • ${dateFormatter.format(item.timestamp)}"

            // FIXED: Evaluated isUnread matching criteria via core system fields (e.g. check regular active entry states)
            val isUnreadState = item.isDeleted == 0 // Custom logic filter fallback matching model properties
            binding.tvDocNewBadge.visibility = if (isUnreadState) View.VISIBLE else View.GONE
            binding.viewUnreadDotIndicator.visibility = if (isUnreadState) View.VISIBLE else View.GONE

            // Bind thematic palettes based on specific file extensions
            applyExtensionThematicStyles(extension)

            // Setup multi-select check bounds state attributes
            val isSelected = selectedFilePathsSet.contains(path)
            if (isSelected) {
                binding.ivSelectionCheckIndicator.visibility = View.VISIBLE
                binding.ivSelectionCheckIndicator.setImageResource(R.drawable.ic_checkbox_selected)
                binding.btnDocInlineShare.visibility = View.GONE
                binding.cardDocRoot.setStrokeColor(binding.root.context.getColor(R.color.figma_selected_blue))
                binding.cardDocRoot.strokeWidth = 3
            } else {
                binding.ivSelectionCheckIndicator.visibility = View.GONE
                binding.btnDocInlineShare.visibility = View.VISIBLE
                binding.cardDocRoot.strokeWidth = 0
            }

            binding.btnDocInlineShare.setOnClickListener { onInlineShareClicked(item) }
            binding.root.setOnClickListener { onDocClicked(item) }
            binding.root.setOnLongClickListener {
                onDocLongPressed(item)
                true
            }
        }

        private fun applyExtensionThematicStyles(ext: String) {
            val context = binding.root.context
            when (ext) {
                "PDF" -> {
                    binding.cardExtThumbBg.setCardBackgroundColor(context.getColor(R.color.bg_ext_pdf))
                    binding.ivExtIconGraphic.setImageResource(R.drawable.ic_file_pdf)
                    binding.ivExtIconGraphic.imageTintList =
                        context.getColorStateList(R.color.accent_ext_pdf)
                    binding.tvExtBadgeFlag.text = "PDF"
                    binding.tvExtBadgeFlag.setBackgroundColor(context.getColor(R.color.accent_ext_pdf))
                }

                "XLS", "XLSX", "CSV" -> {
                    binding.cardExtThumbBg.setCardBackgroundColor(context.getColor(R.color.bg_ext_xls))
                    binding.ivExtIconGraphic.setImageResource(R.drawable.ic_file_excel)
                    binding.ivExtIconGraphic.imageTintList =
                        context.getColorStateList(R.color.accent_ext_xls)
                    binding.tvExtBadgeFlag.text = "XLS"
                    binding.tvExtBadgeFlag.setBackgroundColor(context.getColor(R.color.accent_ext_xls))
                }

                "ZIP", "RAR", "7Z" -> {
                    binding.cardExtThumbBg.setCardBackgroundColor(context.getColor(R.color.bg_ext_zip))
                    binding.ivExtIconGraphic.setImageResource(R.drawable.ic_file_zip)
                    binding.ivExtIconGraphic.imageTintList =
                        context.getColorStateList(R.color.accent_ext_zip)
                    binding.tvExtBadgeFlag.text = "ZIP"
                    binding.tvExtBadgeFlag.setBackgroundColor(context.getColor(R.color.accent_ext_zip))
                }

                "MP3", "WAV", "OGG", "M4A" -> {
                    binding.cardExtThumbBg.setCardBackgroundColor(context.getColor(R.color.bg_ext_audio))
                    binding.ivExtIconGraphic.setImageResource(R.drawable.ic_music_node)
                    binding.ivExtIconGraphic.imageTintList =
                        context.getColorStateList(R.color.accent_ext_audio)
                    binding.tvExtBadgeFlag.text = "MP3"
                    binding.tvExtBadgeFlag.setBackgroundColor(context.getColor(R.color.accent_ext_audio))
                }

                else -> { // Default Fallback (JPG/PNG/DOC standard assets)
                    binding.cardExtThumbBg.setCardBackgroundColor(context.getColor(R.color.bg_ext_image))
                    binding.ivExtIconGraphic.setImageResource(R.drawable.ic_file_image)
                    binding.ivExtIconGraphic.imageTintList =
                        context.getColorStateList(R.color.accent_ext_image)
                    binding.tvExtBadgeFlag.text = if (ext.isNotEmpty()) ext.take(3) else "DOC"
                    binding.tvExtBadgeFlag.setBackgroundColor(context.getColor(R.color.accent_ext_image))
                }
            }
        }
    }

    class DocDiffCallback : DiffUtil.ItemCallback<MessageEntity>() {
        override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean =
            oldItem == newItem
    }
}

// Inline File Extension Utility Helpers Matrix
object FileUtils {
    fun getFileExtension(fileName: String): String = fileName.substringAfterLast('.', "")
    fun getFileNameWithoutExtension(fileName: String): String =
        fileName.substringBeforeLast('.', fileName)
}