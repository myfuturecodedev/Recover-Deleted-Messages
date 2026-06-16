package com.futurecode.recoverdeletedmessages.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futurecode.recoverdeletedmessages.data.repository.MediaRepository
import com.futurecode.recoverdeletedmessages.model.MediaItem
import com.futurecode.recoverdeletedmessages.utils.Constants
import com.futurecode.recoverdeletedmessages.utils.SafManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MediaViewModel(private val repository: MediaRepository) : ViewModel() {

    val images: StateFlow<List<MediaItem>> = repository.getImages()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val videos: StateFlow<List<MediaItem>> = repository.getVideos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val audios: StateFlow<List<MediaItem>> = repository.getAudios()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val voiceNotes: StateFlow<List<MediaItem>> = repository.getVoiceNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val gifs: StateFlow<List<MediaItem>> = repository.getGifs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val stickers: StateFlow<List<MediaItem>> = repository.getStickers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val documents: StateFlow<List<MediaItem>> = repository.getDocuments()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun scanImages(context: Context) = viewModelScope.launch {
        repository.scanMediaType(context, Constants.WA_IMAGES_PATH, Constants.MEDIA_TYPE_IMAGE)
    }

    fun scanVideos(context: Context) = viewModelScope.launch {
        repository.scanMediaType(context, Constants.WA_VIDEOS_PATH, Constants.MEDIA_TYPE_VIDEO)
    }

    fun scanAudios(context: Context) = viewModelScope.launch {
        repository.scanMediaType(context, Constants.WA_AUDIO_PATH, Constants.MEDIA_TYPE_AUDIO)
    }

    fun scanVoice(context: Context) = viewModelScope.launch {
        repository.scanMediaType(context, Constants.WA_VOICE_PATH, Constants.MEDIA_TYPE_VOICE)
    }

    fun scanGifs(context: Context) = viewModelScope.launch {
        if (SafManager.hasSafPermission(context)) {
            repository.scanViaSAF(context, "WhatsApp Animated Gifs", Constants.MEDIA_TYPE_GIF)
        } else {
            repository.scanAndSyncDirectory(Constants.WA_GIF_PATH, Constants.MEDIA_TYPE_GIF)
            repository.scanAndSyncDirectory(Constants.WA_NEW_GIF_PATH, Constants.MEDIA_TYPE_GIF)
        }
    }

    fun scanStickers(context: Context) = viewModelScope.launch {
        if (SafManager.hasSafPermission(context)) {
            repository.scanViaSAF(context, "WhatsApp Stickers", Constants.MEDIA_TYPE_STICKER)
        } else {
            repository.scanAndSyncDirectory(Constants.WA_STICKERS_PATH, Constants.MEDIA_TYPE_STICKER)
            repository.scanAndSyncDirectory(Constants.WA_NEW_STICKERS_PATH, Constants.MEDIA_TYPE_STICKER)
        }
    }

    fun scanDocuments(context: Context) = viewModelScope.launch {
        if (SafManager.hasSafPermission(context)) {
            repository.scanViaSAF(context, "WhatsApp Documents", Constants.MEDIA_TYPE_DOCUMENT)
        } else {
            repository.scanAndSyncDirectory(Constants.WA_DOCUMENTS_PATH, Constants.MEDIA_TYPE_DOCUMENT)
            repository.scanAndSyncDirectory(Constants.WA_NEW_DOCUMENTS_PATH, Constants.MEDIA_TYPE_DOCUMENT)
        }
    }

    fun scanAll(context: Context) = viewModelScope.launch {
        repository.scanAllWhatsAppMedia(context)
    }

    fun deleteMedia(item: MediaItem) = viewModelScope.launch {
        repository.deleteMedia(item)
    }
}
