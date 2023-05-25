package com.efrivahmi.neighborstory.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import com.efrivahmi.neighborstory.data.api.ApiNeighbor
import com.efrivahmi.neighborstory.data.repository.SourceData
import com.efrivahmi.neighborstory.local.preferences.NeighborPreference
import com.efrivahmi.neighborstory.local.room.NeighborDatabase

private val Context.dataStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore("token")

object Injection {
    fun neighborRepo(context: Context): SourceData {
        val neighborDatabase = NeighborDatabase.getDatabase(context)
        val preferences = NeighborPreference.getInstance(context.dataStore)
        val apiService = ApiNeighbor.getConfigApi()
        return SourceData.getInstance(preferences, apiService, neighborDatabase)
    }
}
