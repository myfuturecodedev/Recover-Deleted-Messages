package com.futurecode.recoverdeletedmessages.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.futurecode.recoverdeletedmessages.data.MessageEntity
import com.futurecode.recoverdeletedmessages.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File
import java.io.FileWriter
import android.os.Environment

//class RecoveryViewModel(application: Application) : AndroidViewModel(application) {
//
//    private val _messagesUiStateFlow = MutableStateFlow<UiState<List<MessageEntity>>>(UiState.Loading)
//    val messagesUiStateFlow: StateFlow<UiState<List<MessageEntity>>> = _messagesUiStateFlow
//
//    /**
//     * Reads text_history.json securely off internal device storage file links.
//     */
//    fun loadStoredTextChatThreads(isBusinessMode: Boolean) {
//        viewModelScope.launch(Dispatchers.IO) {
//            _messagesUiStateFlow.value = UiState.Loading
//            val logFile = File(getApplication<Application>().filesDir, "text_history.json")
//
//            if (!logFile.exists()) {
//                _messagesUiStateFlow.value = UiState.Success(emptyList())
//                return@launch
//            }
//
//            try {
//                val parsedList = mutableListOf<MessageEntity>()
//                val jsonArray = JSONArray(logFile.readText())
//
//                for (i in 0 until jsonArray.length()) {
//                    val obj = jsonArray.getJSONObject(i)
//                    val matchMode = obj.getBoolean("isPackageBusiness")
//
//                    if (matchMode == isBusinessMode) {
//                        parsedList.add(
//                            MessageEntity(
//                                id = obj.getLong("id"),
//                                chatId = obj.getString("chatId"),
//                                senderName = obj.getString("senderName"),
//                                textContent = obj.getString("textContent"),
//                                timestamp = obj.getLong("timestamp"),
//                                messageType = obj.getString("messageType"),
//                                isPackageBusiness = matchMode,
//                                isUnread = obj.getBoolean("isUnread")
//                            )
//                        )
//                    }
//                }
//
//                // Group individual rows together cleanly by thread owners
//                val filteredUniqueThreads = parsedList.groupBy { it.chatId }
//                    .map { (_, threadGroup) -> threadGroup.maxByOrNull { it.timestamp }!! }
//                    .sortedByDescending { it.timestamp }
//
//                _messagesUiStateFlow.value = UiState.Success(filteredUniqueThreads)
//            } catch (e: Exception) {
//                _messagesUiStateFlow.value = UiState.Error(e)
//            }
//        }
//    }
//
//    /**
//     * Erases target unique checked thread item index profiles out of the JSON log map completely.
//     */
//    fun deleteSelectedChatThreads(chatIds: List<String>, isBusinessMode: Boolean) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val logFile = File(getApplication<Application>().filesDir, "text_history.json")
//            if (!logFile.exists()) return@launch
//
//            try {
//                val baseArray = JSONArray(logFile.readText())
//                val updatedArray = JSONArray()
//
//                for (i in 0 until baseArray.length()) {
//                    val obj = baseArray.getJSONObject(i)
//                    if (!chatIds.contains(obj.getString("chatId"))) {
//                        updatedArray.put(obj)
//                    }
//                }
//
//                FileWriter(logFile).use { it.write(updatedArray.toString()) }
//                loadStoredTextChatThreads(isBusinessMode)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//}


class RecoveryViewModel(application: Application) : AndroidViewModel(application) {

    // --- PIPELINE 1: Text Chats Conversations State Streams ---
    private val _messagesUiStateFlow = MutableStateFlow<UiState<List<MessageEntity>>>(UiState.Loading)
    val messagesUiStateFlow: StateFlow<UiState<List<MessageEntity>>> = _messagesUiStateFlow

    // --- PIPELINE 2: Document / File System Scanned Media Streams ---
    private val _mediaUiStateFlow = MutableStateFlow<UiState<List<MessageEntity>>>(UiState.Loading)
    val mediaUiStateFlow: StateFlow<UiState<List<MessageEntity>>> = _mediaUiStateFlow

    // --- PIPELINE 3: Dashboard Real-time Badge Counters Map Matrix State ---
    private val _dashboardBadgesFlow = MutableStateFlow<Map<String, Int>>(emptyMap())
    val dashboardBadgesFlow: StateFlow<Map<String, Int>> = _dashboardBadgesFlow

    // Constants representing folder path branches matching WhatsApp's storage footprint
    private val BASE_WA_PATH = "Android/media/com.whatsapp/WhatsApp/Media/"
    private val BASE_W4B_PATH = "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/"

    /**
     * SCREEN ACTION: Pulls message thread timelines out of your local JSON log.
     */
    fun loadStoredTextChatThreads(isBusinessMode: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _messagesUiStateFlow.value = UiState.Loading
            val logFile = File(getApplication<Application>().filesDir, "text_history.json")

            if (!logFile.exists()) {
                _messagesUiStateFlow.value = UiState.Success(emptyList())
                return@launch
            }

            try {
                val parsedList = mutableListOf<MessageEntity>()
                val jsonArray = JSONArray(logFile.readText())

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val matchMode = obj.getBoolean("isPackageBusiness")

                    if (matchMode == isBusinessMode) {
                        parsedList.add(
                            MessageEntity(
                                id = obj.getLong("id"),
                                chatId = obj.getString("chatId"),
                                senderName = obj.getString("senderName"),
                                textContent = obj.getString("textContent"),
                                timestamp = obj.getLong("timestamp"),
                                messageType = obj.getString("messageType"),
                                isPackageBusiness = matchMode,
                                isUnread = obj.getBoolean("isUnread")
                            )
                        )
                    }
                }

                val filteredUniqueThreads = parsedList.groupBy { it.chatId }
                    .map { (_, threadGroup) -> threadGroup.maxByOrNull { it.timestamp }!! }
                    .sortedByDescending { it.timestamp }

                _messagesUiStateFlow.value = UiState.Success(filteredUniqueThreads)
            } catch (e: Exception) {
                _messagesUiStateFlow.value = UiState.Error(e)
            }
        }
    }

    /**
     * SCREEN ACTION: Queries physical storage directories on background IO loops
     * targeting a chosen Category Type (e.g., "PHOTO", "VIDEO", "AUDIO", etc.).
     */
    fun loadScannedMediaFiles(categoryType: String, isBusinessMode: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _mediaUiStateFlow.value = UiState.Loading
            val recoveredList = mutableListOf<MessageEntity>()

            val relativeSubFolder = getSubFolderPathCategory(categoryType)
            val rootMediaDir = File(
                Environment.getExternalStorageDirectory(),
                if (isBusinessMode) "$BASE_W4B_PATH$relativeSubFolder" else "$BASE_WA_PATH$relativeSubFolder"
            )

            if (rootMediaDir.exists() && rootMediaDir.isDirectory) {
                val files = rootMediaDir.listFiles { file -> file.isFile && !file.name.startsWith(".") }
                files?.forEach { file ->
                    recoveredList.add(
                        MessageEntity(
                            id = file.hashCode().toLong(),
                            chatId = file.parentFile?.name ?: "Unknown",
                            senderName = file.name,
                            textContent = "${file.length() / 1024} KB", // File size metadata text placeholder
                            timestamp = file.lastModified(),
                            messageType = categoryType,
                            localMediaUri = file.absolutePath,
                            isPackageBusiness = isBusinessMode,
                            isUnread = false
                        )
                    )
                }
            }
            _mediaUiStateFlow.value = UiState.Success(recoveredList.sortedByDescending { it.timestamp })
        }
    }

    /**
     * DASHBOARD ENGINE: Simultaneously scans all folder trees to gather count
     * badges displayed inside your main screen cards.
     */
    fun calculateDashboardBadgeCounters(isBusinessMode: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val countMap = mutableMapOf<String, Int>()

            // 1. Calculate Message Text badge count from internal log
            val logFile = File(getApplication<Application>().filesDir, "text_history.json")
            var unreadTextCount = 0
            if (logFile.exists()) {
                try {
                    val jsonArray = JSONArray(logFile.readText())
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        if (obj.getBoolean("isPackageBusiness") == isBusinessMode && obj.getBoolean("isUnread")) {
                            unreadTextCount++
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            countMap["MESSAGE"] = unreadTextCount

            // 2. Loop through file storage directories to calculate item counts
            val categories = listOf("PHOTO", "VIDEO", "GIF", "STICKER", "AUDIO", "VOICE", "DOCUMENT")
            for (category in categories) {
                val subFolder = getSubFolderPathCategory(category)
                val targetDir = File(
                    Environment.getExternalStorageDirectory(),
                    if (isBusinessMode) "$BASE_W4B_PATH$subFolder" else "$BASE_WA_PATH$subFolder"
                )

                var count = 0
                if (targetDir.exists() && targetDir.isDirectory) {
                    val validFiles = targetDir.listFiles { file -> file.isFile && !file.name.startsWith(".") }
                    count = validFiles?.size ?: 0
                }
                countMap[category] = count
            }

            _dashboardBadgesFlow.value = countMap
        }
    }

    /**
     * CORE COMPONENT: Bulk deletes checked items off local filesystems.
     */
    fun deletePhysicalMediaFiles(filePaths: List<String>, categoryType: String, isBusinessMode: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            filePaths.forEach { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
            loadScannedMediaFiles(categoryType, isBusinessMode)
        }
    }

    fun deleteSelectedChatThreads(chatIds: List<String>, isBusinessMode: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val logFile = File(getApplication<Application>().filesDir, "text_history.json")
            if (!logFile.exists()) return@launch

            try {
                val baseArray = JSONArray(logFile.readText())
                val updatedArray = JSONArray()

                for (i in 0 until baseArray.length()) {
                    val obj = baseArray.getJSONObject(i)
                    if (!chatIds.contains(obj.getString("chatId"))) {
                        updatedArray.put(obj)
                    }
                }

                FileWriter(logFile).use { it.write(updatedArray.toString()) }
                loadStoredTextChatThreads(isBusinessMode)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    /**
     * Map category layout tokens to exact WhatsApp physical device path hierarchies
     */
    private fun getSubFolderPathCategory(category: String): String {
        return when (category) {
            "PHOTO" -> "WhatsApp Images/"
            "VIDEO" -> "WhatsApp Video/"
            "GIF" -> "WhatsApp Animated GIFs/"
            "STICKER" -> "WhatsApp Stickers/"
            "AUDIO" -> "WhatsApp Audio/"
            "VOICE" -> "WhatsApp Voice Notes/"
            "DOCUMENT" -> "WhatsApp Documents/"
            else -> "WhatsApp Images/"
        }
    }
}


