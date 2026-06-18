package com.futurecode.recoverdeletedmessages.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.futurecode.recoverdeletedmessages.R

import org.intellij.lang.annotations.Language

//class LanguageAdapter(
//    private var languages: List<Language>,
//    private val onLanguageSelected: (Language) -> Unit
//) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {
//
//    inner class LanguageViewHolder(val binding: ItemLanguageBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
//        val binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return LanguageViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
//        val language = languages[position]
//        with(holder.binding) {
//            tvLanguageName.text = language.name
//            if (language.isSelected) {
//                languageContainer.setBackgroundResource(R.drawable.bg_language_selected)
//                ivSelected.visibility = android.view.View.VISIBLE
//            } else {
//                languageContainer.setBackgroundResource(R.drawable.bg_language_normal)
//                ivSelected.visibility = android.view.View.GONE
//            }
//            root.setOnClickListener {
//                languages.forEach { it.isSelected = false }
//                language.isSelected = true
//                notifyDataSetChanged()
//                onLanguageSelected(language)
//            }
//        }
//    }
//
//    override fun getItemCount() = languages.size
//}
