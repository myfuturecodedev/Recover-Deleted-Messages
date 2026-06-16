package com.futurecode.recoverdeletedmessages.utils

object Constants {
    // Legacy WA paths (Android 9 and below, or old installs)
    const val WA_IMAGES_PATH = "WhatsApp/Media/WhatsApp Images"
    const val WA_VIDEOS_PATH = "WhatsApp/Media/WhatsApp Video"
    const val WA_AUDIO_PATH = "WhatsApp/Media/WhatsApp Audio"
    const val WA_VOICE_PATH = "WhatsApp/Media/WhatsApp Voice Notes"
    const val WA_GIF_PATH = "WhatsApp/Media/WhatsApp Animated Gifs"
    const val WA_STICKERS_PATH = "WhatsApp/Media/WhatsApp Stickers"
    const val WA_DOCUMENTS_PATH = "WhatsApp/Media/WhatsApp Documents"

    // New WA paths (Android 10+ scoped storage)
    private const val WA_NEW_BASE = "Android/media/com.whatsapp/WhatsApp/Media"
    const val WA_NEW_IMAGES_PATH = "$WA_NEW_BASE/WhatsApp Images"
    const val WA_NEW_VIDEOS_PATH = "$WA_NEW_BASE/WhatsApp Video"
    const val WA_NEW_AUDIO_PATH = "$WA_NEW_BASE/WhatsApp Audio"
    const val WA_NEW_VOICE_PATH = "$WA_NEW_BASE/WhatsApp Voice Notes"
    const val WA_NEW_GIF_PATH = "$WA_NEW_BASE/WhatsApp Animated Gifs"
    const val WA_NEW_STICKERS_PATH = "$WA_NEW_BASE/WhatsApp Stickers"
    const val WA_NEW_DOCUMENTS_PATH = "$WA_NEW_BASE/WhatsApp Documents"

    // WA Business paths (Android 10+)
    private const val WA_BIZ_BASE = "Android/media/com.whatsapp.w4b/WhatsApp Business/Media"
    const val WA_BIZ_IMAGES_PATH = "$WA_BIZ_BASE/WhatsApp Images"
    const val WA_BIZ_VIDEOS_PATH = "$WA_BIZ_BASE/WhatsApp Video"
    const val WA_BIZ_AUDIO_PATH = "$WA_BIZ_BASE/WhatsApp Audio"
    const val WA_BIZ_VOICE_PATH = "$WA_BIZ_BASE/WhatsApp Voice Notes"
    const val WA_BIZ_GIF_PATH = "$WA_BIZ_BASE/WhatsApp Animated Gifs"
    const val WA_BIZ_STICKERS_PATH = "$WA_BIZ_BASE/WhatsApp Stickers"
    const val WA_BIZ_DOCUMENTS_PATH = "$WA_BIZ_BASE/WhatsApp Documents"

    const val WA_PACKAGE = "com.whatsapp"
    const val WA_BUSINESS_PACKAGE = "com.whatsapp.w4b"

    const val CHANNEL_ID = "rdm_monitoring_channel"
    const val CHANNEL_NAME = "Media Recovery"

    const val MEDIA_TYPE_IMAGE = "image"
    const val MEDIA_TYPE_VIDEO = "video"
    const val MEDIA_TYPE_AUDIO = "audio"
    const val MEDIA_TYPE_VOICE = "voice"
    const val MEDIA_TYPE_GIF = "gif"
    const val MEDIA_TYPE_STICKER = "sticker"
    const val MEDIA_TYPE_DOCUMENT = "document"
    const val MEDIA_TYPE_MESSAGE = "message"

    const val ARG_CONTACT_NAME = "contactName"
    const val ARG_CONTACT_NUMBER = "contactNumber"
    const val ARG_MEDIA_PATH = "mediaPath"
    const val ARG_MEDIA_TYPE = "mediaType"
    const val ARG_AUDIO_PATH = "audioPath"
    const val ARG_AUDIO_TYPE = "audioType"

    const val NOTIFICATION_PERMISSION_REQUEST = 1001
    const val STORAGE_PERMISSION_REQUEST = 1002
    const val FOLDER_PICKER_REQUEST = 1003
}
