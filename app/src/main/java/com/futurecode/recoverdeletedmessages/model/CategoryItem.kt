package com.futurecode.recoverdeletedmessages.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class CategoryItem(
    val title: String,
    val subtitle: String,
    val iconResName: String,
    @ColorRes val iconColorRes: Int,
    @ColorRes val bgColorRes: Int,
    val mediaType: String,
    var count: Int = 0
)
