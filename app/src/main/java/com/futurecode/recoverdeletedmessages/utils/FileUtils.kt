package com.futurecode.recoverdeletedmessages.utils


import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.futurecode.recoverdeletedmessages.model.MediaItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {

    // Scans both legacy and new WA paths, returns unique files by name
    fun getFilesFromDirectory(dirPath: String): List<File> {
        val results = mutableMapOf<String, File>()

        // Legacy path: /sdcard/WhatsApp/Media/...
        val legacyDir = File(Environment.getExternalStorageDirectory(), dirPath)
        legacyDir.listFiles()
            ?.filter { it.isFile && !it.name.startsWith(".") }
            ?.forEach { results[it.name] = it }

        // New scoped-storage path: /sdcard/Android/media/com.whatsapp/...
        if (dirPath.startsWith("WhatsApp/Media/")) {
            val sub = dirPath.removePrefix("WhatsApp/Media/")
            val newDir = File(Environment.getExternalStorageDirectory(),
                "Android/media/com.whatsapp/WhatsApp/Media/$sub")
            newDir.listFiles()
                ?.filter { it.isFile && !it.name.startsWith(".") }
                ?.forEach { results[it.name] = it }

            // WA Business path
            val bizDir = File(Environment.getExternalStorageDirectory(),
                "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/$sub")
            bizDir.listFiles()
                ?.filter { it.isFile && !it.name.startsWith(".") }
                ?.forEach { results["biz_${it.name}"] = it }
        }

        return results.values.sortedByDescending { it.lastModified() }
    }

    // MediaStore-based scan — reliable on Android 10+ without MANAGE_EXTERNAL_STORAGE
    fun scanViaMediaStore(context: Context, mediaType: String): List<MediaItem> {
        val items = mutableListOf<MediaItem>()

        when (mediaType) {
            Constants.MEDIA_TYPE_IMAGE, Constants.MEDIA_TYPE_GIF, Constants.MEDIA_TYPE_STICKER -> {
                val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val projection = arrayOf(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.DATE_ADDED
                )
                val sel = "${MediaStore.Images.Media.DATA} LIKE ? OR ${MediaStore.Images.Media.DATA} LIKE ?"
                val args = arrayOf("%WhatsApp%", "%whatsapp%")
                context.contentResolver.query(uri, projection, sel, args,
                    "${MediaStore.Images.Media.DATE_ADDED} DESC")?.use { cursor ->
                    val dataIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                    val dateIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                    while (cursor.moveToNext()) {
                        val path = cursor.getString(dataIdx) ?: continue
                        val name = cursor.getString(nameIdx) ?: continue
                        val size = cursor.getLong(sizeIdx)
                        val date = cursor.getLong(dateIdx) * 1000L
                        val type = when {
                            name.endsWith(".gif", true) || path.contains("Animated Gif", true) -> Constants.MEDIA_TYPE_GIF
                            path.contains("Sticker", true) -> Constants.MEDIA_TYPE_STICKER
                            else -> Constants.MEDIA_TYPE_IMAGE
                        }
                        if (type == mediaType) {
                            items.add(MediaItem(filePath = path, fileName = name,
                                mediaType = type, fileSize = size, dateAdded = date, isNew = true))
                        }
                    }
                }
            }
            Constants.MEDIA_TYPE_VIDEO -> {
                val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                val projection = arrayOf(
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATE_ADDED,
                    MediaStore.Video.Media.DURATION
                )
                val sel = "${MediaStore.Video.Media.DATA} LIKE ? OR ${MediaStore.Video.Media.DATA} LIKE ?"
                val args = arrayOf("%WhatsApp%", "%whatsapp%")
                context.contentResolver.query(uri, projection, sel, args,
                    "${MediaStore.Video.Media.DATE_ADDED} DESC")?.use { cursor ->
                    val dataIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                    val nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                    val dateIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                    val durIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                    while (cursor.moveToNext()) {
                        val path = cursor.getString(dataIdx) ?: continue
                        val name = cursor.getString(nameIdx) ?: continue
                        items.add(MediaItem(filePath = path, fileName = name,
                            mediaType = Constants.MEDIA_TYPE_VIDEO,
                            fileSize = cursor.getLong(sizeIdx),
                            dateAdded = cursor.getLong(dateIdx) * 1000L,
                            duration = cursor.getLong(durIdx), isNew = true))
                    }
                }
            }
            Constants.MEDIA_TYPE_AUDIO, Constants.MEDIA_TYPE_VOICE -> {
                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val projection = arrayOf(
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.DURATION
                )
                val sel = "${MediaStore.Audio.Media.DATA} LIKE ? OR ${MediaStore.Audio.Media.DATA} LIKE ?"
                val args = arrayOf("%WhatsApp%", "%whatsapp%")
                context.contentResolver.query(uri, projection, sel, args,
                    "${MediaStore.Audio.Media.DATE_ADDED} DESC")?.use { cursor ->
                    val dataIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                    val nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                    val dateIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                    val durIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    while (cursor.moveToNext()) {
                        val path = cursor.getString(dataIdx) ?: continue
                        val name = cursor.getString(nameIdx) ?: continue
                        val type = if (path.contains("Voice Note", true) ||
                            name.startsWith("PTT", true) || name.endsWith(".opus", true))
                            Constants.MEDIA_TYPE_VOICE else Constants.MEDIA_TYPE_AUDIO
                        if (type == mediaType) {
                            items.add(MediaItem(filePath = path, fileName = name,
                                mediaType = type,
                                fileSize = cursor.getLong(sizeIdx),
                                dateAdded = cursor.getLong(dateIdx) * 1000L,
                                duration = cursor.getLong(durIdx), isNew = true))
                        }
                    }
                }
            }
        }
        return items
    }

    fun getMimeType(file: File): String {
        val ext = MimeTypeMap.getFileExtensionFromUrl(file.name.replace(" ", "%20"))
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext?.lowercase()) ?: "*/*"
    }

    fun formatFileSize(sizeBytes: Long): String = when {
        sizeBytes < 1024 -> "$sizeBytes B"
        sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
        else -> String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0))
    }

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))

    fun formatTime(timestamp: Long): String =
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))

    fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    fun isImage(file: File): Boolean {
        val mime = getMimeType(file)
        return mime.startsWith("image/") && !mime.contains("gif")
    }

    fun isVideo(file: File): Boolean = getMimeType(file).startsWith("video/")

    fun isGif(file: File): Boolean {
        val mime = getMimeType(file)
        val name = file.name.lowercase()
        return mime.contains("gif") || name.endsWith(".gif")
    }

    fun isAudio(file: File): Boolean = getMimeType(file).startsWith("audio/")

    fun isDocument(file: File): Boolean {
        val name = file.name.lowercase()
        return name.endsWith(".pdf") || name.endsWith(".doc") || name.endsWith(".docx") ||
                name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".ppt") ||
                name.endsWith(".pptx") || name.endsWith(".zip") || name.endsWith(".txt")
    }

    fun getDocumentIcon(fileName: String): String = when {
        fileName.endsWith(".pdf", true) -> "pdf"
        fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) -> "doc"
        fileName.endsWith(".xls", true) || fileName.endsWith(".xlsx", true) -> "xls"
        fileName.endsWith(".ppt", true) || fileName.endsWith(".pptx", true) -> "ppt"
        fileName.endsWith(".zip", true) -> "zip"
        else -> "file"
    }
}