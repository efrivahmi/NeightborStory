package com.efrivahmi.neighborstory.ui.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efrivahmi.neighborstory.data.model.NeighborModel
import com.efrivahmi.neighborstory.data.repository.SourceData
import com.efrivahmi.neighborstory.data.response.AddStory
import com.efrivahmi.neighborstory.utils.HelperToast
import com.efrivahmi.neighborstory.utils.Result
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddViewModel(private val sourceData: SourceData) : ViewModel() {
    val upload: LiveData<Result<AddStory?>> = sourceData.createNew
    val isLoading: MutableLiveData<Result<Boolean>> = sourceData.isLoading
    val toast: LiveData<HelperToast<String>> = sourceData.toastText

    fun uploadStory(token: String, file: MultipartBody.Part, description: RequestBody) {
        viewModelScope.launch {
            sourceData.uploadNewStory(token, file, description)
        }
    }

    fun getNeighbor(): LiveData<Result<NeighborModel>> {
        return sourceData.getNeighbor()
    }
}