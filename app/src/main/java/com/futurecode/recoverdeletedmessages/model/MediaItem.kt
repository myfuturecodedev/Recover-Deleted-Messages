package com.futurecode.recoverdeletedmessages.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val mediaType: String,
    val fileSize: Long = 0L,
    val duration: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val contactName: String = "",
    val isNew: Boolean = true,
    val thumbnailPath: String = ""
)