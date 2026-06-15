package com.futurecode.recoverdeletedmessages.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages_table")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val messageId: String,
    val senderName: String,
    val messageText: String,       // Text content ya file size metadata
    val timestamp: Long,
    val isBusiness: Boolean,       // WhatsApp (false) vs Business (true)
    val isDeleted: Int = 0,        // 0 = Active, 1 = Deleted for Everyone
    val localMediaUri: String? = null, // Physical device par file ka actual path

    // NEW FIELD: Dynamic filtering ke liye (MESSAGE, PHOTO, VIDEO, AUDIO, VOICE, GIF, STICKER, DOCUMENT)
    val mediaCategory: String = "MESSAGE"
)