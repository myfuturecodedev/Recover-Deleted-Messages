package com.futurecode.recoverdeletedmessages.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.model.MessageItem
import com.futurecode.recoverdeletedmessages.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
class WANotificationListenerService : NotificationListenerService() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        private const val TAG = "WANotificationListener"

        fun isNotificationAccessGranted(context: Context): Boolean {
            val enabledListeners = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val componentName = ComponentName(context, WANotificationListenerService::class.java)
            return enabledListeners.contains(componentName.flattenToString())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        val packageName = sbn.packageName
        if (packageName != Constants.WA_PACKAGE && packageName != Constants.WA_BUSINESS_PACKAGE) return

        try {
            val extras: Bundle = sbn.notification.extras ?: return

            // Try multiple title sources (handles grouped/bundled notifications)
            val title = (extras.getCharSequence(Notification.EXTRA_TITLE)
                ?: extras.getCharSequence("android.title.big")
                ?: extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE))
                ?.toString()?.trim() ?: return

            val text = (extras.getCharSequence(Notification.EXTRA_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT))
                ?.toString()?.trim() ?: ""

            // Skip summary/grouped notification shells (not actual messages)
            val isGroupSummary = (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0
            if (isGroupSummary && text.isBlank()) return

            // Skip system notifications that aren't actual messages
            if (title == "WhatsApp" || title == "WhatsApp Business") return

            val messageType = determineMessageType(text)

            // Check for bundled messages (multiple notifications packed)
            val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
            if (messages != null && messages.isNotEmpty()) {
                for (msg in messages) {
                    if (msg is Bundle) {
                        val sender = msg.getCharSequence("sender")?.toString() ?: title
                        val msgText = msg.getCharSequence("text")?.toString() ?: ""
                        val timestamp = msg.getLong("time", sbn.postTime)
                        if (msgText.isNotBlank()) {
                            saveMessage(sender, msgText, timestamp, packageName)
                        }
                    }
                }
            } else if (text.isNotBlank() || messageType != "text") {
                saveMessage(title, text, sbn.postTime, packageName)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }

    private fun saveMessage(contactName: String, text: String, timestamp: Long, pkg: String) {
        val type = determineMessageType(text)
        val item = MessageItem(
            contactName = cleanContactName(contactName),
            messageText = text,
            messageType = type,
            timestamp = timestamp,
            isDeleted = false,
            appPackage = pkg,
            isNew = true
        )
        serviceScope.launch {
            try {
                MyApplication.app.database.messageDao().insert(item)
                Log.d(TAG, "Saved: $contactName → $text")
            } catch (e: Exception) {
                Log.e(TAG, "DB insert failed", e)
            }
        }
    }

    // Strips group info like "Contact @ Group Name"
    private fun cleanContactName(raw: String): String =
        if (raw.contains("@")) raw.substringBefore("@").trim() else raw

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // We already stored the message when it was posted, so nothing needed here
    }

    private fun determineMessageType(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("photo") || lower.contains("image") || lower.endsWith(".jpg") || lower.endsWith(".png") -> Constants.MEDIA_TYPE_IMAGE
            lower.contains("video") || lower.endsWith(".mp4") -> Constants.MEDIA_TYPE_VIDEO
            lower.contains("voice message") || lower.contains("ptt") || lower.endsWith(".opus") -> Constants.MEDIA_TYPE_VOICE
            lower.contains("audio") || lower.endsWith(".mp3") -> Constants.MEDIA_TYPE_AUDIO
            lower.contains("gif") -> Constants.MEDIA_TYPE_GIF
            lower.contains("sticker") -> Constants.MEDIA_TYPE_STICKER
            lower.contains("document") || lower.contains("pdf") || lower.contains(".doc") -> Constants.MEDIA_TYPE_DOCUMENT
            else -> "text"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
