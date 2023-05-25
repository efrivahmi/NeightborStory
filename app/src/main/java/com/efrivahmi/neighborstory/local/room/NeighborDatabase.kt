package com.efrivahmi.neighborstory.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.efrivahmi.neighborstory.data.response.ListStoryItem
import com.efrivahmi.neighborstory.local.entity.RemoteKeys

@Database(entities = [ListStoryItem::class, RemoteKeys::class], version = 2, exportSchema = false)
abstract class NeighborDatabase : RoomDatabase() {
    abstract fun neighborDao(): NeighborDao
    abstract fun remoteDao(): RemoteDao

    companion object {
        @Volatile
        private var INSTANCE: NeighborDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): NeighborDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NeighborDatabase::class.java, "neighbor_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}