package com.efrivahmi.neighborstory.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.efrivahmi.neighborstory.data.repository.SourceData
import com.efrivahmi.neighborstory.ui.add.AddViewModel
import com.efrivahmi.neighborstory.ui.login.LoginViewModel
import com.efrivahmi.neighborstory.ui.main.MainViewModel
import com.efrivahmi.neighborstory.ui.register.RegisterViewModel

class NeighborFactory(private val pref: SourceData) : ViewModelProvider.NewInstanceFactory() {

    companion object {
        @Volatile
        private var instance: NeighborFactory? = null
        fun getInstance(context: Context): NeighborFactory {
            return instance ?: synchronized(this) {
                instance ?: NeighborFactory(Injection.neighborRepo(context))
            }.also { instance = it }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(pref) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(pref) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(pref) as T
            }
            modelClass.isAssignableFrom(AddViewModel::class.java) -> {
                AddViewModel(pref) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}