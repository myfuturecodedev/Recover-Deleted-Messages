package com.futurecode.recoverdeletedmessages.data.db

import androidx.room.*
import com.futurecode.recoverdeletedmessages.model.MessageItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAll(): Flow<List<MessageItem>>

    @Query("SELECT * FROM messages WHERE contactName = :contact ORDER BY timestamp ASC")
    fun getByContact(contact: String): Flow<List<MessageItem>>

    @Query("SELECT contactName FROM messages GROUP BY contactName ORDER BY MAX(timestamp) DESC")
    fun getContacts(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MessageItem): Long

    @Delete
    suspend fun delete(item: MessageItem)

    @Query("SELECT COUNT(*) FROM messages")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM messages WHERE contactName = :contact")
    suspend fun countByContact(contact: String): Int

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}