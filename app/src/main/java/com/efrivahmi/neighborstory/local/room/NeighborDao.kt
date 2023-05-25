package com.efrivahmi.neighborstory.local.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.efrivahmi.neighborstory.data.response.ListStoryItem

@Dao
interface NeighborDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(listStoryItem: List<ListStoryItem>)

    @Query("SELECT * FROM story")
    fun getAllStory(): PagingSource<Int, ListStoryItem>

    @Query("DELETE FROM story")
    suspend fun deleteAllStory()
}
