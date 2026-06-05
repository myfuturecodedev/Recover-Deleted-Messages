package com.futurecode.recoverdeletedmessages.model

data class LanguageModel(
    val languageCode: String,
    val displayLanguage: String,
    var isSelected: Boolean = false
)