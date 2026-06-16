package com.futurecode.recoverdeletedmessages.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futurecode.recoverdeletedmessages.data.repository.MediaRepository
import com.futurecode.recoverdeletedmessages.model.MessageItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MessageViewModel(private val repository: MediaRepository) : ViewModel() {

    val allMessages: StateFlow<List<MessageItem>> = repository.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val contacts: StateFlow<List<String>> = repository.getContacts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getMessagesByContact(contact: String): StateFlow<List<MessageItem>> =
        repository.getMessagesByContact(contact)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun deleteMessage(item: MessageItem) = viewModelScope.launch {
        repository.deleteMessage(item)
    }
}
