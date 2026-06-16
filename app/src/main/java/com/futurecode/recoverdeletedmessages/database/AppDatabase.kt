package com.futurecode.recoverdeletedmessages.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.futurecode.recoverdeletedmessages.data.db.MediaDao
import com.futurecode.recoverdeletedmessages.data.db.MessageDao
import com.futurecode.recoverdeletedmessages.model.MediaItem
import com.futurecode.recoverdeletedmessages.model.MessageItem

@Database(
    entities = [MediaItem::class, MessageItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "rdm_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}