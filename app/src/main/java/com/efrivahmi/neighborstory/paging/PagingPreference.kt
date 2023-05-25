package com.efrivahmi.neighborstory.paging

import android.util.Log
import androidx.paging.*
import androidx.room.withTransaction
import com.efrivahmi.neighborstory.data.api.ConfigApi
import com.efrivahmi.neighborstory.data.response.ListStoryItem
import com.efrivahmi.neighborstory.local.entity.RemoteKeys
import com.efrivahmi.neighborstory.local.preferences.NeighborPreference
import com.efrivahmi.neighborstory.local.room.NeighborDatabase
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PagingPreference (
    private val database: NeighborDatabase,
    private val apiService: ConfigApi,
    private val preferences: NeighborPreference
) : RemoteMediator<Int, ListStoryItem>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryItem>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val token = preferences.getNeighborSession().first().token
            val responseData = apiService.storyNeighbor(token, page, state.config.pageSize)
            val endOfPaginationReached = responseData.listStory.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteDao().deleteRemoteKeys()
                    database.neighborDao().deleteAllStory()
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = responseData.listStory.map {
                    RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                val list = mutableListOf<ListStoryItem>()

                responseData.listStory.forEach {
                    val story = ListStoryItem(
                        it.photoUrl,
                        it.createdAt,
                        it.name,
                        it.description,
                        it.lon,
                        it.id,
                        it.lat
                    )
                    list.add(story)
                }
                database.remoteDao().insertAll(keys)

                database.neighborDao().insertStory(list)
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: HttpException) {
            Log.e("PagingSource", "HttpException: " + e.message)
            return MediatorResult.Error(e)
        } catch (e: IOException) {
            Log.e("PagingSource", "IOException: " + e.message)
            return MediatorResult.Error(e)
        } catch (e: Exception) {
            Log.e("PagingSource", "Exception: " + e.message)
            return MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            database.remoteDao().getRemoteId(data.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            database.remoteDao().getRemoteId(data.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteDao().getRemoteId(id)
            }
        }
    }

}