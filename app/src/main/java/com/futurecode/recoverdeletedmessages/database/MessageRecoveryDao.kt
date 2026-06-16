package com.futurecode.recoverdeletedmessages.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.futurecode.recoverdeletedmessages.data.MessageEntity
import kotlinx.coroutines.flow.Flow

//@Dao
//interface MessageRecoveryDao {
//
//    // Returns structural Flow array streams to automatically force layout updates when database alters
//    @Query("SELECT * FROM messages_table ORDER BY timestamp DESC")
//    fun getAllMessagesFlow(): Flow<List<MessageEntity>>
//
//    // FIXED: Changed 'onConflictStrategy' to 'onConflict'
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertIncomingMessage(message: MessageEntity)
//
//    // Tracks WhatsApp status change triggers instantly when delete strings are caught
//    @Query("UPDATE messages_table SET isDeleted = 1 WHERE senderName = :name AND isDeleted = 0")
//    suspend fun markMessageAsDeleted(name: String)
//}

@Dao
interface MessageRecoveryDao {

    @Query("SELECT * FROM messages_table ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>

    // 1. Get Live Data Stream for a specific Category (e.g., Only PHOTOS or Only VIDEOS)
    @Query("SELECT * FROM messages_table WHERE mediaCategory = :category AND isBusiness = :isBusiness ORDER BY timestamp DESC")
    fun getMessagesByCategoryFlow(category: String, isBusiness: Boolean): Flow<List<MessageEntity>>

    // 2. Direct count query for Dashboard Badges to keep it lightning fast
    @Query("SELECT COUNT(*) FROM messages_table WHERE mediaCategory = :category AND isBusiness = :isBusiness AND isDeleted = 0")
    suspend fun getCategoryCount(category: String, isBusiness: Boolean): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncomingMessage(message: MessageEntity)

    @Query("UPDATE messages_table SET isDeleted = 1 WHERE senderName = :name AND isDeleted = 0")
    suspend fun markMessageAsDeleted(name: String)

}