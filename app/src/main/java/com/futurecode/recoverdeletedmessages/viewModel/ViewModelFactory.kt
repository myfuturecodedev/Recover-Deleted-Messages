package com.futurecode.recoverdeletedmessages.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.futurecode.recoverdeletedmessages.data.repository.MediaRepository
import com.futurecode.recoverdeletedmessages.model.HomeViewModel

class ViewModelFactory(private val repository: MediaRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(MediaViewModel::class.java) -> MediaViewModel(repository) as T
        modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository) as T
        modelClass.isAssignableFrom(MessageViewModel::class.java) -> MessageViewModel(repository) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
