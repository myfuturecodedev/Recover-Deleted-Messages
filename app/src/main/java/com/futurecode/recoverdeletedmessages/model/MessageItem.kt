package com.futurecode.recoverdeletedmessages.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactName: String,
    val contactNumber: String = "",
    val messageText: String = "",
    val messageType: String = "text",
    val mediaPath: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val appPackage: String = "com.whatsapp",
    val isNew: Boolean = true
)
