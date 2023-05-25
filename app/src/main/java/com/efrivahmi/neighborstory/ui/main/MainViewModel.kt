package com.efrivahmi.neighborstory.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.efrivahmi.neighborstory.data.model.NeighborModel
import com.efrivahmi.neighborstory.data.repository.SourceData
import com.efrivahmi.neighborstory.data.response.ListStoryItem
import com.efrivahmi.neighborstory.data.response.Story
import com.efrivahmi.neighborstory.utils.Result
import kotlinx.coroutines.launch

class MainViewModel (private val sourceData: SourceData) : ViewModel() {
    val listNeighbor: LiveData<Result<Story?>> = sourceData.list
    val getListStories: LiveData<PagingData<ListStoryItem>> =
        sourceData.writeStory().cachedIn(viewModelScope)

    fun createStory(token: String) {
        viewModelScope.launch {
            sourceData.writeStoryWithLocation(token)
        }
        Log.e("token", "onResponse: $token")
    }


    fun getNeighbor(): LiveData<Result<NeighborModel>> {
        return sourceData.getNeighbor()
    }

    fun neighborLogout() {
        viewModelScope.launch {
            sourceData.logout()
        }
    }
}
