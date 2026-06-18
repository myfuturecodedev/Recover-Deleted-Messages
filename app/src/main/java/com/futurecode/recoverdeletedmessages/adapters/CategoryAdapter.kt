package com.futurecode.recoverdeletedmessages.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.futurecode.recoverdeletedmessages.databinding.ItemCategoryBinding
import com.futurecode.recoverdeletedmessages.model.CategoryItem

class CategoryAdapter(
    private val categories: List<CategoryItem>,
    private val onCategoryClick: (CategoryItem) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = categories[position]
        val ctx = holder.binding.root.context
        with(holder.binding) {
            tvCategoryName.text = item.title
            tvCategorySubtitle.text = item.subtitle
            tvCount.text = if (item.count > 0) "${item.count} Files" else "0 Files"
            iconBg.backgroundTintList = ContextCompat.getColorStateList(ctx, item.bgColorRes)
            ivCategoryIcon.imageTintList = ContextCompat.getColorStateList(ctx, item.iconColorRes)
            root.setOnClickListener { onCategoryClick(item) }
        }
    }

    override fun getItemCount() = categories.size

    fun updateCount(mediaType: String, count: Int) {
        val idx = categories.indexOfFirst { it.mediaType == mediaType }
        if (idx >= 0) {
            categories[idx].count = count
            notifyItemChanged(idx)
        }
    }
}
