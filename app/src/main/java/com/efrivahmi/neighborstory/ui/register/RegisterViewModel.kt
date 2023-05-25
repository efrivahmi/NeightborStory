package com.efrivahmi.neighborstory.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efrivahmi.neighborstory.data.repository.SourceData
import com.efrivahmi.neighborstory.data.response.Register
import com.efrivahmi.neighborstory.utils.HelperToast
import com.efrivahmi.neighborstory.utils.Result
import kotlinx.coroutines.launch

class RegisterViewModel(private val sourceData: SourceData) : ViewModel() {
    val regis: LiveData<Result<Register?>> = sourceData.register
    val isLoading: MutableLiveData<Result<Boolean>> = sourceData.isLoading
    val toast: LiveData<HelperToast<String>> = sourceData.toastText

    fun uploadRegisData(name: String, email: String, password: String) {
        viewModelScope.launch {
            sourceData.uploadRegister(name, email, password)
        }
    }
}

