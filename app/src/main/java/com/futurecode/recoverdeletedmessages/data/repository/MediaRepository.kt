package com.futurecode.recoverdeletedmessages.data.repository


import android.content.Context
import com.futurecode.recoverdeletedmessages.database.AppDatabase
import com.futurecode.recoverdeletedmessages.model.MediaItem
import com.futurecode.recoverdeletedmessages.model.MessageItem
import com.futurecode.recoverdeletedmessages.utils.Constants
import com.futurecode.recoverdeletedmessages.utils.FileUtils
import com.futurecode.recoverdeletedmessages.utils.SafManager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(private val db: AppDatabase) {
    private val mediaDao = db.mediaDao()
    private val messageDao = db.messageDao()

    fun getImages() = mediaDao.getByType(Constants.MEDIA_TYPE_IMAGE)
    fun getVideos() = mediaDao.getByType(Constants.MEDIA_TYPE_VIDEO)
    fun getAudios() = mediaDao.getByType(Constants.MEDIA_TYPE_AUDIO)
    fun getVoiceNotes() = mediaDao.getByType(Constants.MEDIA_TYPE_VOICE)
    fun getGifs() = mediaDao.getByType(Constants.MEDIA_TYPE_GIF)
    fun getStickers() = mediaDao.getByType(Constants.MEDIA_TYPE_STICKER)
    fun getDocuments() = mediaDao.getByType(Constants.MEDIA_TYPE_DOCUMENT)
    fun getAllMedia() = mediaDao.getAll()

    fun getAllMessages() = messageDao.getAll()
    fun getMessagesByContact(contact: String) = messageDao.getByContact(contact)
    fun getContacts() = messageDao.getContacts()

    suspend fun insertMessage(item: MessageItem) = messageDao.insert(item)
    suspend fun deleteMessage(item: MessageItem) = messageDao.delete(item)

    suspend fun insertMedia(item: MediaItem) = mediaDao.insert(item)
    suspend fun deleteMedia(item: MediaItem) = mediaDao.delete(item)

    suspend fun countByType(type: String): Int =
        if (type == Constants.MEDIA_TYPE_MESSAGE) messageDao.count()
        else mediaDao.countByType(type)

    // SAF-based scan for Android 11+ (uses DocumentFile from tree URI)
    suspend fun scanViaSAF(context: Context, waFolderName: String, mediaType: String) =
        withContext(Dispatchers.IO) {
            val files = SafManager.getWhatsAppMediaFiles(context, waFolderName)
            for (f in files) {
                if (!mediaDao.exists(f.uri)) {
                    mediaDao.insert(MediaItem(
                        filePath = f.uri,  // content:// URI stored as path
                        fileName = f.name,
                        mediaType = mediaType,
                        fileSize = 0L,
                        dateAdded = f.lastModified,
                        isNew = true
                    ))
                }
            }
        }

    // MediaStore-based scan (Android 10+) then fallback to direct file scan
    suspend fun scanMediaType(context: Context, dirPath: String, mediaType: String) =
        withContext(Dispatchers.IO) {
            // Primary: MediaStore query (works on Android 10+ without special perms)
            val msItems = FileUtils.scanViaMediaStore(context, mediaType)
            for (item in msItems) {
                if (!mediaDao.exists(item.filePath)) {
                    mediaDao.insert(item)
                }
            }

            // Also try SAF if permission granted (covers Android 11+ scoped storage)
            if (SafManager.hasSafPermission(context)) {
                val safFolder = when (mediaType) {
                    Constants.MEDIA_TYPE_IMAGE -> "WhatsApp Images"
                    Constants.MEDIA_TYPE_VIDEO -> "WhatsApp Video"
                    Constants.MEDIA_TYPE_AUDIO -> "WhatsApp Audio"
                    Constants.MEDIA_TYPE_VOICE -> "WhatsApp Voice Notes"
                    Constants.MEDIA_TYPE_GIF -> "WhatsApp Animated Gifs"
                    Constants.MEDIA_TYPE_STICKER -> "WhatsApp Stickers"
                    Constants.MEDIA_TYPE_DOCUMENT -> "WhatsApp Documents"
                    else -> return@withContext
                }
                scanViaSAF(context, safFolder, mediaType)
            }

            // Fallback: Direct file scan (works on older Android)
            if (msItems.isEmpty() && !SafManager.hasSafPermission(context)) {
                val files = FileUtils.getFilesFromDirectory(dirPath)
                for (file in files) {
                    val path = file.absolutePath
                    if (!mediaDao.exists(path)) {
                        mediaDao.insert(MediaItem(
                            filePath = path,
                            fileName = file.name,
                            mediaType = mediaType,
                            fileSize = file.length(),
                            dateAdded = file.lastModified(),
                            isNew = true
                        ))
                    }
                }
            }
        }

    // Legacy method kept for backward compat (used by MediaScanWorker without context)
    suspend fun scanAndSyncDirectory(dirPath: String, mediaType: String) =
        withContext(Dispatchers.IO) {
            val files = FileUtils.getFilesFromDirectory(dirPath)
            for (file in files) {
                val path = file.absolutePath
                if (!mediaDao.exists(path)) {
                    mediaDao.insert(MediaItem(
                        filePath = path,
                        fileName = file.name,
                        mediaType = mediaType,
                        fileSize = file.length(),
                        dateAdded = file.lastModified(),
                        isNew = true
                    ))
                }
            }
        }

    suspend fun scanAllWhatsAppMedia(context: Context? = null) = withContext(Dispatchers.IO) {
        if (context != null) {
            scanMediaType(context, Constants.WA_IMAGES_PATH, Constants.MEDIA_TYPE_IMAGE)
            scanMediaType(context, Constants.WA_VIDEOS_PATH, Constants.MEDIA_TYPE_VIDEO)
            scanMediaType(context, Constants.WA_AUDIO_PATH, Constants.MEDIA_TYPE_AUDIO)
            scanMediaType(context, Constants.WA_VOICE_PATH, Constants.MEDIA_TYPE_VOICE)
            if (SafManager.hasSafPermission(context)) {
                scanViaSAF(context, "WhatsApp Animated Gifs", Constants.MEDIA_TYPE_GIF)
                scanViaSAF(context, "WhatsApp Stickers", Constants.MEDIA_TYPE_STICKER)
                scanViaSAF(context, "WhatsApp Documents", Constants.MEDIA_TYPE_DOCUMENT)
            } else {
                scanAndSyncDirectory(Constants.WA_GIF_PATH, Constants.MEDIA_TYPE_GIF)
                scanAndSyncDirectory(Constants.WA_NEW_GIF_PATH, Constants.MEDIA_TYPE_GIF)
                scanAndSyncDirectory(Constants.WA_STICKERS_PATH, Constants.MEDIA_TYPE_STICKER)
                scanAndSyncDirectory(Constants.WA_NEW_STICKERS_PATH, Constants.MEDIA_TYPE_STICKER)
                scanAndSyncDirectory(Constants.WA_DOCUMENTS_PATH, Constants.MEDIA_TYPE_DOCUMENT)
                scanAndSyncDirectory(Constants.WA_NEW_DOCUMENTS_PATH, Constants.MEDIA_TYPE_DOCUMENT)
            }
        } else {
            scanAndSyncDirectory(Constants.WA_IMAGES_PATH, Constants.MEDIA_TYPE_IMAGE)
            scanAndSyncDirectory(Constants.WA_NEW_IMAGES_PATH, Constants.MEDIA_TYPE_IMAGE)
            scanAndSyncDirectory(Constants.WA_VIDEOS_PATH, Constants.MEDIA_TYPE_VIDEO)
            scanAndSyncDirectory(Constants.WA_NEW_VIDEOS_PATH, Constants.MEDIA_TYPE_VIDEO)
            scanAndSyncDirectory(Constants.WA_AUDIO_PATH, Constants.MEDIA_TYPE_AUDIO)
            scanAndSyncDirectory(Constants.WA_NEW_AUDIO_PATH, Constants.MEDIA_TYPE_AUDIO)
            scanAndSyncDirectory(Constants.WA_VOICE_PATH, Constants.MEDIA_TYPE_VOICE)
            scanAndSyncDirectory(Constants.WA_NEW_VOICE_PATH, Constants.MEDIA_TYPE_VOICE)
            scanAndSyncDirectory(Constants.WA_GIF_PATH, Constants.MEDIA_TYPE_GIF)
            scanAndSyncDirectory(Constants.WA_NEW_GIF_PATH, Constants.MEDIA_TYPE_GIF)
            scanAndSyncDirectory(Constants.WA_STICKERS_PATH, Constants.MEDIA_TYPE_STICKER)
            scanAndSyncDirectory(Constants.WA_NEW_STICKERS_PATH, Constants.MEDIA_TYPE_STICKER)
            scanAndSyncDirectory(Constants.WA_DOCUMENTS_PATH, Constants.MEDIA_TYPE_DOCUMENT)
            scanAndSyncDirectory(Constants.WA_NEW_DOCUMENTS_PATH, Constants.MEDIA_TYPE_DOCUMENT)
        }
    }
}