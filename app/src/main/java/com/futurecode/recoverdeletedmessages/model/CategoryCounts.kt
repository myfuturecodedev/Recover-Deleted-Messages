package com.futurecode.recoverdeletedmessages.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futurecode.recoverdeletedmessages.data.repository.MediaRepository
import com.futurecode.recoverdeletedmessages.utils.Constants
import kotlinx.coroutines.launch

data class CategoryCounts(
    val messages: Int = 0,
    val images: Int = 0,
    val videos: Int = 0,
    val gifs: Int = 0,
    val stickers: Int = 0,
    val audios: Int = 0,
    val voices: Int = 0,
    val documents: Int = 0
)

class HomeViewModel(private val repository: MediaRepository) : ViewModel() {

    private val _counts = MutableLiveData(CategoryCounts())
    val counts: LiveData<CategoryCounts> = _counts

    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> = _isScanning

    fun loadCounts() {
        viewModelScope.launch {
            _counts.value = CategoryCounts(
                messages = repository.countByType(Constants.MEDIA_TYPE_MESSAGE),
                images = repository.countByType(Constants.MEDIA_TYPE_IMAGE),
                videos = repository.countByType(Constants.MEDIA_TYPE_VIDEO),
                gifs = repository.countByType(Constants.MEDIA_TYPE_GIF),
                stickers = repository.countByType(Constants.MEDIA_TYPE_STICKER),
                audios = repository.countByType(Constants.MEDIA_TYPE_AUDIO),
                voices = repository.countByType(Constants.MEDIA_TYPE_VOICE),
                documents = repository.countByType(Constants.MEDIA_TYPE_DOCUMENT)
            )
        }
    }

    fun scanAll(context: Context) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                repository.scanAllWhatsAppMedia(context)
                loadCounts()
            } finally {
                _isScanning.value = false
            }
        }
    }
}