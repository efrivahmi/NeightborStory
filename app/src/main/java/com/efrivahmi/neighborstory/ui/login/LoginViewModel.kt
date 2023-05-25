package com.efrivahmi.neighborstory.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.efrivahmi.neighborstory.data.model.NeighborModel
import com.efrivahmi.neighborstory.data.repository.SourceData
import com.efrivahmi.neighborstory.data.response.Login
import com.efrivahmi.neighborstory.utils.HelperToast
import com.efrivahmi.neighborstory.utils.Result
import kotlinx.coroutines.launch

class LoginViewModel(private val sourceData: SourceData) : ViewModel() {
    val login: LiveData<Result<Login?>> = sourceData.login
    val isLoading: MutableLiveData<Result<Boolean>> = sourceData.isLoading
    val toast: LiveData<HelperToast<String>> = sourceData.toastText

    fun uploadLoginData(email: String, password: String) {
        viewModelScope.launch {
            sourceData.uploadLogin(email, password)
        }
    }

    fun saveSession(session: NeighborModel) {
        viewModelScope.launch {
            sourceData.saveUser(session)
        }
    }

    fun login() {
        viewModelScope.launch {
            sourceData.login()
        }
    }
}
