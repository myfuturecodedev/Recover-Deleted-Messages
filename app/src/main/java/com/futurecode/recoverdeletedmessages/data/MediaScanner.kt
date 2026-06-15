package com.futurecode.recoverdeletedmessages.data


import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File

object MediaScanner {

    private const val TAG = "MediaScanner_Debug"
    private const val WA_AUDIO_PATH = "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Audio/"
    private const val WA_VOICE_PATH = "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes/"

    fun scanRecoveredAudioFiles(context: Context, isBusiness: Boolean): List<MessageEntity> {
        Log.d(TAG, "Starting directory file system scan process loop...")
        val recoveredList = mutableListOf<MessageEntity>()
        val targetPaths = listOf(WA_AUDIO_PATH, WA_VOICE_PATH)
        val storageDir = Environment.getExternalStorageDirectory()

        for (path in targetPaths) {
            val directory = File(storageDir, path)

            // LOG FOLDER TARGET VALIDATIONS
            Log.d(TAG, "Checking directory path location target: ${directory.absolutePath}")
            Log.d(TAG, "Directory details -> exists: ${directory.exists()}, isDirectory: ${directory.isDirectory}")

            if (directory.exists() && directory.isDirectory) {
                val files = directory.listFiles { file ->
                    file.isFile && (file.name.endsWith(".mp3") || file.name.endsWith(".opus") || file.name.endsWith(".m4a"))
                }

                Log.d(TAG, "Found ${files?.size ?: 0} supported file formats inside this target sub-folder.")

                files?.forEach { file ->
                    recoveredList.add(
                        MessageEntity(
                            // Primary key 'id' autoGenerate=true hai, isliye hum isko 0 bhejenge
                            // taaki Room ise SQLite tables mein unique handle kare bina duplicates crash ke.
                            id = 0,

                            // File ka hashcode messageId string ban jayega unique track ke liye
                            messageId = file.hashCode().toString(),

                            // File name text mapping parameter (e.g., "AUD-2026.mp3")
                            senderName = file.name,

                            // FIXED: textContent badal kar messageText kiya (Stores file size calculation)
                            messageText = "${file.length() / 1024} KB",

                            // File ka actual creation/modification timestamp
                            timestamp = file.lastModified(),

                            // FIXED: isPackageBusiness badal kar isBusiness kiya
                            isBusiness = isBusiness,

                            // 0 = Normal active storage file asset mapping pass
                            isDeleted = 0,

                            // Storage local device internal structural absolute file path directory identity
                            localMediaUri = file.absolutePath
                        )
                    )
                }
            }
        }
        Log.d(TAG, "Scan Completed. Returning total of ${recoveredList.size} objects back into view model pipeline.")
        return recoveredList.sortedByDescending { it.timestamp }
    }
}