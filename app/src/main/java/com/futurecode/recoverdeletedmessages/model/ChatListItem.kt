package com.futurecode.recoverdeletedmessages.model

sealed interface ChatListItem {
    data class ActiveChat(
        val chatId: String,
        val profileName: String,
        val lastMessageSnippet: String,
        val timestampString: String,
        val unreadCount: Int,
        val isOnline: Boolean = false,
        val isSelected: Boolean = false
    ) : ChatListItem

    data class AppPromoBanner(
        val promoId: String,
        val promoTitle: String,
        val promoSubtitle: String
    ) : ChatListItem
}