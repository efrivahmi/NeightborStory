package com.efrivahmi.neighborstory.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.efrivahmi.neighborstory.data.model.NeighborModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NeighborPreference private constructor(private val dataBase: DataStore<Preferences>) {


    suspend fun login() {
        dataBase.edit { preferences ->
            preferences[STATE_KEY] = true
        }
    }

    suspend fun logout() {
        dataBase.edit { preferences ->
            preferences.clear()
        }
    }
    fun getNeighborSession(): Flow<NeighborModel> {
        return dataBase.data.map { preferences ->
            NeighborModel(
                preferences[NAME_KEY] ?: "",
                preferences[TOKEN_KEY] ?: "",
                preferences[STATE_KEY] ?: false
            )
        }
    }

    suspend fun saveNeighborSession(session: NeighborModel) {
        dataBase.edit { preferences ->
            preferences[NAME_KEY] = session.name
            preferences[TOKEN_KEY] = session.token
            preferences[STATE_KEY] = session.isLogin
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: NeighborPreference? = null
        private val NAME_KEY = stringPreferencesKey("name")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val STATE_KEY = booleanPreferencesKey("state")

        fun getInstance(dataStore: DataStore<Preferences>): NeighborPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = NeighborPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}