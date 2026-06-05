package com.futurecode.recoverdeletedmessages.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.databinding.ItemLanguageCardBinding
import com.futurecode.recoverdeletedmessages.databinding.ItemNativeAdPlaceholderBinding
import com.futurecode.recoverdeletedmessages.model.LanguageModel

//class LanguageAdapter(
//    private val items: List<LanguageModel>,
//    private val onLanguageSelected: (LanguageModel) -> Unit
//) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {
//
//    private var selectedPosition: Int = items.indexOfFirst { it.isSelected }
//
//    inner class LanguageViewHolder(val binding: ItemLanguageCardBinding) : RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
//        val binding = ItemLanguageCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return LanguageViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
//        val item = items[position]
//        val context = holder.itemView.context
//
//        holder.binding.tvLanguageName.text = item.displayLanguage
//
//        if (position == selectedPosition) {
//            // Render Selected Active Style Matrix
//            holder.binding.layoutContainer.setBackgroundResource(R.drawable.bg_lang_card_selected)
//            holder.binding.tvLanguageName.setTextColor(ContextCompat.getColor(context, R.color.lang_text_selected))
//            holder.binding.ivSelectionIndicator.setImageResource(R.drawable.ic_check_circle_filled) // Green check mark
//        } else {
//            // Render Standard Flattened Neutral Style Matrix
//            holder.binding.layoutContainer.setBackgroundResource(R.drawable.bg_lang_card_unselected)
//            holder.binding.tvLanguageName.setTextColor(ContextCompat.getColor(context, R.color.lang_text_unselected))
//            holder.binding.ivSelectionIndicator.setImageResource(R.drawable.ic_radio_unselected) // Simple hollow grey loop
//        }
//
//        holder.itemView.setOnClickListener {
//            val previousSelected = selectedPosition
//            selectedPosition = holder.adapterPosition
//
//            // Sync selection status over raw model items
//            items.forEachIndexed { index, model -> model.isSelected = (index == selectedPosition) }
//
//            notifyItemChanged(previousSelected)
//            notifyItemChanged(selectedPosition)
//            onLanguageSelected(item)
//        }
//    }
//
//    override fun getItemCount(): Int = items.size
//}





class LanguageAdapter(
    private val activityContext: Activity,
    private val onLanguageSelected: (LanguageModel) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(LanguageDiffCallback()) {

    companion object {
        private const val TYPE_LANGUAGE_ITEM = 0
        private const val TYPE_AD_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is LanguageModel -> TYPE_LANGUAGE_ITEM
            is String -> TYPE_AD_ITEM
            else -> throw IllegalArgumentException("Invalid Object Processing Type mapping")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_LANGUAGE_ITEM -> {
                val binding = ItemLanguageCardBinding.inflate(inflater, parent, false)
                LanguageItemViewHolder(binding)
            }
            TYPE_AD_ITEM -> {
                val binding = ItemNativeAdPlaceholderBinding.inflate(inflater, parent, false)
                AdItemViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unsupported Layout State processing metrics")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LanguageItemViewHolder -> {
                val model = getItem(position) as LanguageModel
                holder.bind(model)
            }
            is AdItemViewHolder -> {
                holder.bind()
            }
        }
    }

    inner class LanguageItemViewHolder(private val binding: ItemLanguageCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LanguageModel) {
            val context = binding.root.context
            binding.tvLanguageName.text = item.displayLanguage

            // Render identical pixel states matching the original selection criteria
            if (item.isSelected) {
                binding.layoutContainer.setBackgroundResource(R.drawable.bg_lang_card_selected)
                binding.tvLanguageName.setTextColor(ContextCompat.getColor(context, R.color.lang_text_selected))
                binding.ivSelectionIndicator.setImageResource(R.drawable.ic_check_circle_filled)
            } else {
                binding.layoutContainer.setBackgroundResource(R.drawable.bg_lang_card_unselected)
                binding.tvLanguageName.setTextColor(ContextCompat.getColor(context, R.color.lang_text_unselected))
                binding.ivSelectionIndicator.setImageResource(R.drawable.ic_radio_unselected)
            }

            binding.root.setOnClickListener {
                onLanguageSelected(item)
            }
        }
    }

    inner class AdItemViewHolder(private val binding: ItemNativeAdPlaceholderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            // Dynamic evaluation hook directly linked into your BaseFragment's core showAds structure
            if (activityContext is com.futurecode.recoverdeletedmessages.activity.MainActivity) {
                // Adjust call references dynamically depending on hosting setups
                // activityContext.showNativeAd(binding.flNativeAdContainer)
            }
        }
    }

    class LanguageDiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is LanguageModel && newItem is LanguageModel) {
                oldItem.languageCode == newItem.languageCode
            } else oldItem is String && newItem is String && oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            // True Kotlin data equality check handles nested properties natively now that instances change references
            return oldItem == newItem
        }
    }
}