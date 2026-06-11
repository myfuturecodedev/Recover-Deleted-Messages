package com.futurecode.recoverdeletedmessages.data

/**
 * Universal data token mapping both intercepted text messages and
 * local device media attachments cleanly across the app profile.
 */
data class MessageEntity(
    val id: Long,                     // Unique hash identity calculated from file paths or notification keys
    val chatId: String,               // Unique thread bucket key (e.g., sender name hash or folder group)
    val senderName: String,           // Display name or structural filename string (e.g., "AUD-20260605.opus")
    val textContent: String,          // The intercepted chat text content or calculated size string (e.g., "412 KB")
    val timestamp: Long,              // Epoch millisecond tracking for list sorting passes
    val messageType: String,          // Categorization tag matrix: TEXT, PHOTO, VIDEO, AUDIO, GIF, STICKER
    val localMediaUri: String? = null, // Direct on-device file storage link path string
    val isPackageBusiness: Boolean = false, // Toggle flag separating standard WA from WA Business structures
    val isUnread: Boolean = false     // Notification highlight badge flag controller
)