package com.filmo.service

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore by preferencesDataStore(name = "testapp")

interface TheaterBookmarkStore {
    val savedTheaterIds: Flow<Set<String>>

    suspend fun saveTheaterId(theaterId: String)

    suspend fun removeTheaterId(theaterId: String)
}

class LocalDisk @Inject constructor(
    @param:ApplicationContext private val context: Context
) : TheaterBookmarkStore {
    companion object {
        val TEST_VAL = stringPreferencesKey("test_val")
        val INITIAL_SETUP_FINISHED = booleanPreferencesKey("initial_setup_finished")
        val LOGIN_ID = stringPreferencesKey("login_id")
        val NICKNAME = stringPreferencesKey("nickname")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val SAVED_THEATER_IDS = stringSetPreferencesKey("saved_theater_ids")
    }

    suspend fun setTestVal(string: String) {
        context.dataStore.edit { preferences ->
            preferences[TEST_VAL] = string
        }
    }

    suspend fun setNickname(nickname: String) {
        context.dataStore.edit { preferences ->
            preferences[NICKNAME] = nickname
        }
    }

    suspend fun setAuthSession(session: AuthSession) {
        context.dataStore.edit { preferences ->
            preferences[LOGIN_ID] = session.loginId
            preferences[NICKNAME] = session.nickname
            preferences[ACCESS_TOKEN] = session.accessToken
            preferences[INITIAL_SETUP_FINISHED] = true
        }
    }

    val isInitialSetupFinished: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[INITIAL_SETUP_FINISHED] ?: false
        }

    val nickname: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[NICKNAME]
        }

    val accessToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[ACCESS_TOKEN]
        }

    override val savedTheaterIds: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[SAVED_THEATER_IDS].orEmpty()
        }

    override suspend fun saveTheaterId(theaterId: String) {
        if (theaterId.isBlank()) return
        context.dataStore.edit { preferences ->
            preferences[SAVED_THEATER_IDS] = preferences[SAVED_THEATER_IDS].orEmpty() + theaterId
        }
    }

    override suspend fun removeTheaterId(theaterId: String) {
        context.dataStore.edit { preferences ->
            preferences[SAVED_THEATER_IDS] = preferences[SAVED_THEATER_IDS].orEmpty() - theaterId
        }
    }
}
