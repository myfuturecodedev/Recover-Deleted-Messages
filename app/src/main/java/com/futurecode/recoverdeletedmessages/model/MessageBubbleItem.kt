package com.futurecode.recoverdeletedmessages.model


sealed interface MessageBubbleItem {
    val messageId: String
    val timestamp: String

    data class TextMessage(
        override val messageId: String,
        override val timestamp: String,
        val textContent: String
    ) : MessageBubbleItem

    data class PhotoAttachment(
        override val messageId: String,
        override val timestamp: String,
        val mediaCaptionText: String,
        val mediaSubtitle: String = "Image file"
    ) : MessageBubbleItem

    data class VideoAttachment(
        override val messageId: String,
        override val timestamp: String,
        val durationLabel: String,
        val fileMetaLabel: String = "MP4 Video"
    ) : MessageBubbleItem

    data class VoiceNoteAttachment(
        override val messageId: String,
        override val timestamp: String,
        val audioDurationLabel: String,
        val waveFormPoints: List<Int>
    ) : MessageBubbleItem
}