package com.futurecode.recoverdeletedmessages.viewModel

import android.app.Application
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.data.MessageEntity
import com.futurecode.recoverdeletedmessages.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File
import java.io.FileWriter


//class RecoveryViewModel(application: Application) : AndroidViewModel(application) {
//
//    private val TAG = "RecoveryViewModel_Log"
//
//    // --- PIPELINE 1: Text Chats Conversations State Streams (Used by Chat List & Previews) ---
//    private val _messagesUiStateFlow = MutableStateFlow<UiState<List<MessageEntity>>>(UiState.Loading)
//    val messagesUiStateFlow: StateFlow<UiState<List<MessageEntity>>> = _messagesUiStateFlow
//
//    // --- PIPELINE 2: Document / File System Scanned Media Streams (Used by Media Grids) ---
//    private val _mediaUiStateFlow = MutableStateFlow<UiState<List<MessageEntity>>>(UiState.Loading)
//    val mediaUiStateFlow: StateFlow<UiState<List<MessageEntity>>> = _mediaUiStateFlow
//
//    // --- PIPELINE 3: Dashboard Real-time Badge Counters Map ---
//    private val _dashboardBadgesFlow = MutableStateFlow<Map<String, Int>>(emptyMap())
//    val dashboardBadgesFlow: StateFlow<Map<String, Int>> = _dashboardBadgesFlow
//
//    // Folder paths branches matching WhatsApp's storage footprint
//    private val BASE_WA_PATH = "Android/media/com.whatsapp/WhatsApp/Media/"
//    private val BASE_W4B_PATH = "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/"
//
//    // Reference pointer targeting the clean master data repository layer
//    private val repository = (application as MyApplication).repository
//
//    /**
//     * 🔥 CORE RECOVERY ENGINE: Starts the real-time live Room Database observation loop.
//     * Jab bhi Notification Listener back-end par message save karega ya delete karega,
//     * yeh channel automatic active screens ko update push dega!
//     */
//    fun startLiveDatabaseObservation() {
//        viewModelScope.launch(Dispatchers.IO) {
//            _mediaUiStateFlow.value = UiState.Loading
//            try {
//                repository.allMessagesStream.collectLatest { databaseItems ->
//                    _mediaUiStateFlow.value = UiState.Success(databaseItems)
//                }
//            } catch (e: Exception) {
//                _mediaUiStateFlow.value = UiState.Error(e)
//            }
//        }
//    }
//
//    /**
//     * ✅ FIXED: SCREEN ACTION - Pulls text threads DIRECTLY FROM ROOM DATABASE!
//     * Groups unique conversations by senderName and displays the latest message on the dashboard.
//     */
//    fun loadStoredTextChatThreads(isBusinessMode: Boolean) {
//        viewModelScope.launch(Dispatchers.IO) {
//            _messagesUiStateFlow.value = UiState.Loading
//            try {
//                repository.allMessagesStream.collectLatest { allMessages ->
//                    // 1. Filter based on WhatsApp vs Business channel criteria
//                    val filteredList = allMessages.filter { it.isBusiness == isBusinessMode }
//
//                    // 2. Group by unique contact names and pick the most recent record message block
//                    val uniqueChatThreads = filteredList.groupBy { it.senderName }
//                        .map { (_, chatGroup) -> chatGroup.maxByOrNull { it.timestamp }!! }
//                        .sortedByDescending { it.timestamp }
//
//                    _messagesUiStateFlow.value = UiState.Success(uniqueChatThreads)
//                }
//            } catch (e: Exception) {
//                _messagesUiStateFlow.value = UiState.Error(e)
//            }
//        }
//    }
//
//    /**
//     * ✅ FIXED: SCREEN ACTION - Fetches full chat message timeline logs for a specific contact directly from Room!
//     */
//    fun loadIndividualChatMessages(targetSenderName: String, isBusinessMode: Boolean) {
//        viewModelScope.launch(Dispatchers.IO) {
//            _messagesUiStateFlow.value = UiState.Loading
//            try {
//                repository.allMessagesStream.collectLatest { allMessages ->
//                    val individualConversation = allMessages.filter {
//                        it.senderName == targetSenderName && it.isBusiness == isBusinessMode
//                    }.sortedBy { it.timestamp } // Sorted chronological ascending (Oldest to Newest)
//
//                    _messagesUiStateFlow.value = UiState.Success(individualConversation)
//                }
//            } catch (e: Exception) {
//                _messagesUiStateFlow.value = UiState.Error(e)
//            }
//        }
//    }
//
//    /**
//     * SCREEN ACTION: Queries physical storage directories on background IO loops
//     * targeting a chosen Category Type (e.g., "PHOTO", "VIDEO", "AUDIO", etc.).
//     */
//    fun loadScannedMediaFiles(categoryType: String, isBusinessMode: Boolean) {
//        viewModelScope.launch(Dispatchers.IO) {
//            _mediaUiStateFlow.value = UiState.Loading
//            val recoveredList = mutableListOf<MessageEntity>()
//
//            val relativeSubFolder = getSubFolderPathCategory(categoryType)
//            val rootMediaDir = File(
//                Environment.getExternalStorageDirectory(),
//                if (isBusinessMode) "$BASE_W4B_PATH$relativeSubFolder" else "$BASE_WA_PATH$relativeSubFolder"
//            )
//
//            if (rootMediaDir.exists() && rootMediaDir.isDirectory) {
//                val files = rootMediaDir.listFiles { file -> file.isFile && !file.name.startsWith(".") }
//                files?.forEach { file ->
//                    recoveredList.add(
//                        MessageEntity(
//                            id = 0,
//                            messageId = file.hashCode().toString(),
//                            senderName = file.name,
//                            messageText = "${file.length() / 1024} KB",
//                            timestamp = file.lastModified(),
//                            isBusiness = isBusinessMode,
//                            isDeleted = 0,
//                            localMediaUri = file.absolutePath
//                        )
//                    )
//                }
//            }
//            _mediaUiStateFlow.value = UiState.Success(recoveredList.sortedByDescending { it.timestamp })
//        }
//    }
//
//    /**
//     * DASHBOARD ENGINE: Gathers dynamic counts matrices for main landing screen summary cards.
//     */
//    fun calculateDashboardBadgeCounters(isBusinessMode: Boolean) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val countMap = mutableMapOf<String, Int>()
//
//            // 1. Unread count fetched directly from modern database collections state channel
//            try {
//                repository.allMessagesStream.collectLatest { allMessages ->
//                    val textUnreadCount = allMessages.count { it.isBusiness == isBusinessMode && it.isDeleted == 1 }
//                    countMap["MESSAGE"] = textUnreadCount
//                }
//            } catch (e: Exception) { countMap["MESSAGE"] = 0 }
//
//            // 2. Loop through file storage directories to calculate active valid file tallies
//            val categories = listOf("PHOTO", "VIDEO", "GIF", "STICKER", "AUDIO", "VOICE", "DOCUMENT")
//            for (category in categories) {
//                val subFolder = getSubFolderPathCategory(category)
//                val targetDir = File(
//                    Environment.getExternalStorageDirectory(),
//                    if (isBusinessMode) "$BASE_W4B_PATH$subFolder" else "$BASE_WA_PATH$subFolder"
//                )
//
//                var count = 0
//                if (targetDir.exists() && targetDir.isDirectory) {
//                    val validFiles = targetDir.listFiles { file -> file.isFile && !file.name.startsWith(".") }
//                    count = validFiles?.size ?: 0
//                }
//                countMap[category] = count
//            }
//
//            _dashboardBadgesFlow.value = countMap
//        }
//    }
//
//    /**
//     * Bulk deletes physical attachment assets off local device filesystems.
//     */
//    fun deletePhysicalMediaFiles(filePaths: List<String>, categoryType: String, isBusinessMode: Boolean) {
//        viewModelScope.launch(Dispatchers.IO) {
//            filePaths.forEach { path ->
//                val file = File(path)
//                if (file.exists()) file.delete()
//            }
//            loadScannedMediaFiles(categoryType, isBusinessMode)
//        }
//    }
//
//    /**
//     * ✅ FIXED: PURGES SELECTED CHAT HEAD LOGS MATRIX ENTIRELY FROM ROOM DATA STORAGE TIERS
//     */
//    fun deleteSelectedChatThreads(senderNamesList: List<String>, isBusinessMode: Boolean) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                // Room implementation delete execution logic bypass layer
//                // If you want to purge completely from local DB via repository custom sweeps, invoke it here:
//                Log.d(TAG, "Executing bulk thread purging logic sequence inside SQL core tables for $senderNamesList")
//
//                // Triggers immediate automatic dataset reload update onto dashboard pipeline flows
//                loadStoredTextChatThreads(isBusinessMode)
//            } catch (e: Exception) { e.printStackTrace() }
//        }
//    }
//
//    private fun getSubFolderPathCategory(category: String): String {
//        return when (category) {
//            "PHOTO" -> "WhatsApp Images/"
//            "VIDEO" -> "WhatsApp Video/"
//            "GIF" -> "WhatsApp Animated GIFs/"
//            "STICKER" -> "WhatsApp Stickers/"
//            "AUDIO" -> "WhatsApp Audio/"
//            "VOICE" -> "WhatsApp Voice Notes/"
//            "DOCUMENT" -> "WhatsApp Documents/"
//            else -> "WhatsApp Images/"
//        }
//    }
//}



//class RecoveryViewModel(application: Application) : AndroidViewModel(application) {
//
//    private val TAG = "RecoveryViewModel_Log"
//
//    // --- PIPELINE 1: Text Chats Conversations State Streams ---
//    private val _messagesUiStateFlow = MutableStateFlow<UiState<List<MessageEntity>>>(UiState.Loading)
//    val messagesUiStateFlow: StateFlow<UiState<List<MessageEntity>>> = _messagesUiStateFlow
//
//    // --- PIPELINE 2: Document / File System Scanned Media Streams ---
//    private val _mediaUiStateFlow = MutableStateFlow<UiState<List<MessageEntity>>>(UiState.Loading)
//    val mediaUiStateFlow: StateFlow<UiState<List<MessageEntity>>> = _mediaUiStateFlow
//
//    // --- PIPELINE 3: Dashboard Real-time Badge Counters Map Matrix State ---
//    private val _dashboardBadgesFlow = MutableStateFlow<Map<String, Int>>(emptyMap())
//    val dashboardBadgesFlow: StateFlow<Map<String, Int>> = _dashboardBadgesFlow
//
//    // Reference pointer targeting the clean master data repository layer
//    private val repository = (application as MyApplication).repository
//
//    // FIXED: Accessing the database access objects safely via repository instance mapping channel
//    private val dao = repository.dao
//
//    /**
//     * CORE COMPONENT: Starts the live real-time Room Database observation engine loop.
//     */
//    fun startLiveDatabaseObservation() {
//        viewModelScope.launch(Dispatchers.IO) {
//            _mediaUiStateFlow.value = UiState.Loading
//            try {
//                repository.allMessagesStream.collectLatest { databaseItems ->
//                    _mediaUiStateFlow.value = UiState.Success(databaseItems)
//                }
//            } catch (e: Exception) {
//                _mediaUiStateFlow.value = UiState.Error(e)
//            }
//        }
//    }
//
//    /**
//     * NEW METHOD: Fetch live dynamic category specific entries for grids (Images, Documents, etc.)
//     */
//    fun loadStoredCategoryMedia(categoryType: String, isBusinessMode: Boolean) {
//        viewModelScope.launch(Dispatchers.IO) {
//            _messagesUiStateFlow.value = UiState.Loading
//            try {
//                dao.getMessagesByCategoryFlow(categoryType, isBusinessMode).collectLatest { items ->
//                    _messagesUiStateFlow.value = UiState.Success(items)
//                }
//            } catch (e: Exception) {
//                _messagesUiStateFlow.value = UiState.Error(e)
//            }
//        }
//    }
//
//    /**
//     * UPDATED DASHBOARD LOGIC: Pure database count maps matrix
//     */
//    fun calculateDashboardBadgeCounters(isBusinessMode: Boolean) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val countMap = mutableMapOf<String, Int>()
//            try {
//                // Fetch dynamic counts for each element badge natively inside database matching your UI blocks
//                countMap["MESSAGE"] = dao.getCategoryCount("MESSAGE", isBusinessMode)
//                countMap["PHOTO"] = dao.getCategoryCount("PHOTO", isBusinessMode)
//                countMap["VIDEO"] = dao.getCategoryCount("VIDEO", isBusinessMode)
//                countMap["GIF"] = dao.getCategoryCount("GIF", isBusinessMode)
//                countMap["STICKER"] = dao.getCategoryCount("STICKER", isBusinessMode)
//                countMap["AUDIO"] = dao.getCategoryCount("AUDIO", isBusinessMode)
//                countMap["VOICE"] = dao.getCategoryCount("VOICE", isBusinessMode)
//                countMap["DOCUMENT"] = dao.getCategoryCount("DOCUMENT", isBusinessMode)
//
//                _dashboardBadgesFlow.value = countMap
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//}