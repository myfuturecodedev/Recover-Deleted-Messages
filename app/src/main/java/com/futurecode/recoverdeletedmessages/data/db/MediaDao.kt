package com.futurecode.recoverdeletedmessages.data.db
import androidx.room.*
import com.futurecode.recoverdeletedmessages.model.MediaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Query("SELECT * FROM media_items WHERE mediaType = :type ORDER BY dateAdded DESC")
    fun getByType(type: String): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items ORDER BY dateAdded DESC")
    fun getAll(): Flow<List<MediaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MediaItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaItem>)

    @Delete
    suspend fun delete(item: MediaItem)

    @Query("DELETE FROM media_items WHERE filePath = :path")
    suspend fun deleteByPath(path: String)

    @Query("SELECT COUNT(*) FROM media_items WHERE mediaType = :type")
    suspend fun countByType(type: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM media_items WHERE filePath = :path)")
    suspend fun exists(path: String): Boolean

    @Query("UPDATE media_items SET isNew = 0 WHERE mediaType = :type")
    suspend fun markAllSeen(type: String)

    @Query("DELETE FROM media_items WHERE mediaType = :type")
    suspend fun deleteAllByType(type: String)
}