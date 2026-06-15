package com.futurecode.recoverdeletedmessages.database


import com.futurecode.recoverdeletedmessages.data.MessageEntity
import kotlinx.coroutines.flow.Flow

//class MessageRepository(private val dao: MessageRecoveryDao) {
//
//    // Pipe connector maps database data streams smoothly
//    val allMessagesStream: Flow<List<MessageEntity>> = dao.getAllMessagesFlow()
//
//    suspend fun saveMessage(message: MessageEntity) {
//        dao.insertIncomingMessage(message)
//    }
//
//    suspend fun updateAsDeleted(senderName: String) {
//        dao.markMessageAsDeleted(senderName)
//    }
//}


class MessageRepository(val dao: MessageRecoveryDao) { // Added 'val' to make dao accessible from outside

    val allMessagesStream: Flow<List<MessageEntity>> = dao.getAllMessagesFlow()

    suspend fun saveMessage(message: MessageEntity) {
        dao.insertIncomingMessage(message)
    }

    suspend fun updateAsDeleted(senderName: String) {
        dao.markMessageAsDeleted(senderName)
    }
}