package com.futurecode.recoverdeletedmessages.service

import android.app.Notification
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.data.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

import androidx.core.app.NotificationCompat
import com.futurecode.recoverdeletedmessages.R


//class NotificationRecoveryService : NotificationListenerService() {
//
//    private val TAG = "NotificationService_Log"
//    private val CHANNEL_ID = "RecoveredMessagesChannel"
//    private val NOTIFICATION_ID = 1001
//
//    override fun onNotificationPosted(sbn: StatusBarNotification) {
//        super.onNotificationPosted(sbn)
//
//        val packageName = sbn.packageName
//        if (packageName != "com.whatsapp" && packageName != "com.whatsapp.w4b") return
//
//        val extras: Bundle = sbn.notification.extras
//        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
//        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return
//
//        if (text.contains("new messages") || title == "WhatsApp" || text.isEmpty()) return
//
//        val isBusiness = packageName == "com.whatsapp.w4b"
//
//        // =========================================================================
//        // 1. CATCH DELETED MESSAGE TRIGGER & SHOW USER ALERT
//        // =========================================================================
//        if (text.contains("this message was deleted", ignoreCase = true) ||
//            text.contains("this message was deleted.", ignoreCase = true) ||
//            text.contains("message deleted", ignoreCase = true)) {
//
//            Log.e(TAG, "Deletion Caught! Sender: $title")
//
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    (application as MyApplication).repository.updateAsDeleted(title)
//
//                    // 🔥 TRIGGER CUSTOM POPUP NOTIFICATION (Just like reference app)
//                    showCustomPushNotification(
//                        senderName = title,
//                        alertText = "✨ Messages are now accessible..."
//                    )
//
//                } catch (e: Exception) {
//                    Log.e(TAG, "Room database update execution failed", e)
//                }
//            }
//            return
//        }
//
//        // =========================================================================
//        // 2. REGULAR INCOMING MESSAGE LOGGING PIPELINE
//        // =========================================================================
//        val uniqueMsgId = "${sbn.id}_${sbn.postTime}"
//
//        val liveTextMessage = MessageEntity(
//            id = 0,
//            messageId = uniqueMsgId,
//            senderName = title,
//            messageText = text,
//            timestamp = sbn.postTime,
//            isBusiness = isBusiness,
//            isDeleted = 0,
//            localMediaUri = null // Default value for pure text messages. For attachments, pass real path string here.
//
//        )
//
//        saveMessageToInternalStorage(liveTextMessage)
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                (application as MyApplication).repository.saveMessage(liveTextMessage)
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed inserting entity into Room", e)
//            }
//        }
//    }
//
//    /**
//     * 🔥 NEW METHOD: Generates a system push notification banner matching your screenshot reference.
//     */
//    private fun showCustomPushNotification(senderName: String, alertText: String) {
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        // Create the Notification Channel for Android Oreo (API 26) and above
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                CHANNEL_ID,
//                "Recovered Deleted Messages Alerts",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Notifies when a contact deletes a message"
//            }
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        // Target action intent: Launcher activity screen context opens when user taps "Check"
//        // Dynamic routing string parsing to handle backstack safely
//        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            putExtra("isBusinessMode", false)
//        }
//
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            0,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        // Building the notification layout mirror matching reference rules
//        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_wa_group_fallback) // Replace with your app logo icon asset
//            .setContentTitle("Recover Deleted Messages")
//            .setContentText(alertText)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setAutoCancel(true)
//            // ADD ACTION BUTTON: Mimics the green "Check" button seen in image_2de3b8.jpg
//            .addAction(R.drawable.ic_select_all_list, "Check", pendingIntent)
//            .setContentIntent(pendingIntent)
//
//        // Dispatch notification onto device hardware shelf
//        notificationManager.notify(NOTIFICATION_ID, builder.build())
//    }
//
//    private fun saveMessageToInternalStorage(message: MessageEntity) {
//        try {
//            val logFile = File(filesDir, "text_history.json")
//            val jsonArray = if (logFile.exists()) JSONArray(logFile.readText()) else JSONArray()
//
//            val messageJsonObject = JSONObject().apply {
//                put("id", message.timestamp)
//                put("chatId", message.senderName.hashCode().toString())
//                put("senderName", message.senderName)
//                put("textContent", message.messageText)
//                put("timestamp", message.timestamp)
//                put("messageType", if (message.isBusiness) "BUSINESS_TEXT" else "TEXT")
//                put("isPackageBusiness", message.isBusiness)
//                put("isUnread", true)
//            }
//            jsonArray.put(messageJsonObject)
//            FileWriter(logFile).use { writer -> writer.write(jsonArray.toString()) }
//        } catch (e: Exception) { e.printStackTrace() }
//    }
//
//    override fun onNotificationRemoved(sbn: StatusBarNotification) {
//        super.onNotificationRemoved(sbn)
//    }
//}


class NotificationRecoveryService : NotificationListenerService() {

    private val TAG = "NotificationService_Log"

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val packageName = sbn.packageName
        if (packageName != "com.whatsapp" && packageName != "com.whatsapp.w4b") return

        val extras: Bundle = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        if (text.contains("new messages") || title == "WhatsApp" || text.isEmpty()) return

        val isBusiness = packageName == "com.whatsapp.w4b"

        // Handle Deletion Trigger
        if (text.contains("this message was deleted", ignoreCase = true) ||
            text.contains("message deleted", ignoreCase = true)) {

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    (application as MyApplication).repository.updateAsDeleted(title)
                } catch (e: Exception) { Log.e(TAG, "DB Update failed", e) }
            }
            return
        }

        // Determine Category dynamically from incoming notification text signature
        val category = when {
            text.contains("📷 Photo") || text.equals("Photo", ignoreCase = true) -> "PHOTO"
            text.contains("🎥 Video") || text.equals("Video", ignoreCase = true) -> "VIDEO"
            text.contains("🎙️ Voice message") || text.contains("Voice note", ignoreCase = true) -> "VOICE"
            text.contains("🎵 Audio") || text.equals("Audio", ignoreCase = true) -> "AUDIO"
            text.contains("GIF", ignoreCase = true) -> "GIF"
            text.contains("Sticker", ignoreCase = true) -> "STICKER"
            text.contains("📄 Document") || text.contains("Document", ignoreCase = true) -> "DOCUMENT"
            else -> "MESSAGE" // Regular plain conversation text thread
        }

        val uniqueMsgId = "${sbn.id}_${sbn.postTime}"

        val liveMessage = MessageEntity(
            id = 0,
            messageId = uniqueMsgId,
            senderName = title,
            messageText = text,
            timestamp = sbn.postTime,
            isBusiness = isBusiness,
            isDeleted = 0,
            localMediaUri = null, // Will be updated when file observer saves attachment onto internal storage
            mediaCategory = category
        )

        // Stream into Room DB instantly
        CoroutineScope(Dispatchers.IO).launch {
            try {
                (application as MyApplication).repository.saveMessage(liveMessage)
                Log.d(TAG, "Successfully captured & categorized $category into Room Database.")
            } catch (e: Exception) { Log.e(TAG, "Failed inserting entity into Room", e) }
        }
    }
}