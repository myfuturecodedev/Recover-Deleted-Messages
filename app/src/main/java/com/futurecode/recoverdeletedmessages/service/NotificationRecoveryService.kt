package com.futurecode.recoverdeletedmessages.service

import android.app.Notification
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.futurecode.recoverdeletedmessages.data.MessageEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

class NotificationRecoveryService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        if (packageName != "com.whatsapp" && packageName != "com.whatsapp.w4b") return

        val extras: Bundle = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        // Filter out system utility states (e.g., "Checking for new messages")
        if (text.contains("new messages") || title == "WhatsApp") return

        val isBusiness = packageName == "com.whatsapp.w4b"

        // Match type assignments safely
        val messageType = determineMessageType(text)

        val liveTextMessage = MessageEntity(
            id = sbn.postTime,
            chatId = title.hashCode().toString(),
            senderName = title,
            textContent = text,
            timestamp = sbn.postTime,
            messageType = messageType, // Updated from hardcoded "TEXT" to parse media alerts correctly
            isPackageBusiness = isBusiness,
            isUnread = true
        )

        saveMessageToInternalStorage(liveTextMessage)
    }

    private fun determineMessageType(text: String): String {
        return when {
            text.contains("📷 Photo") || text.contains("Photo") -> "PHOTO"
            text.contains("🎥 Video") || text.contains("Video") -> "VIDEO"
            text.contains("🎙️ Voice message") || text.contains("Voice note") -> "VOICE"
            text.contains("GIF") -> "GIF"
            text.contains("Sticker") -> "STICKER"
            else -> "TEXT"
        }
    }

    /**
     * FIXED: Appends the message entity data into a clean local JSON file log matrix
     * instead of trying to look for a missing Room SQLite Database compiler model.
     */
    private fun saveMessageToInternalStorage(message: MessageEntity) {
        try {
            // Locate or create text_history.json inside app's secure private folder space
            val logFile = File(filesDir, "text_history.json")
            val jsonArray = if (logFile.exists()) {
                JSONArray(logFile.readText())
            } else {
                JSONArray()
            }

            // Convert entity fields into standard readable JSON object fields
            val messageJsonObject = JSONObject().apply {
                put("id", message.id)
                put("chatId", message.chatId)
                put("senderName", message.senderName)
                put("textContent", message.textContent)
                put("timestamp", message.timestamp)
                put("messageType", message.messageType)
                put("isPackageBusiness", message.isPackageBusiness)
                put("isUnread", message.isUnread)
            }

            jsonArray.put(messageJsonObject)

            // Write updated array block back to disk stream
            FileWriter(logFile).use { writer ->
                writer.write(jsonArray.toString())
            }

            Log.d("RecoveryEngine", "Successfully saved text logs locally to file system: ${message.senderName}")
        } catch (e: Exception) {
            Log.e("RecoveryEngine", "Failed writing structural log file data blocks onto disk stream", e)
        }
    }
}