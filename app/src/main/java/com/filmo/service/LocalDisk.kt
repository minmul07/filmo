package com.filmo.service

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore by preferencesDataStore(name = "testapp")

class LocalDisk @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        val TEST_VAL = stringPreferencesKey("test_val")
        val INITIAL_SETUP_FINISHED = booleanPreferencesKey("initial_setup_finished")
    }

    suspend fun setTestVal(string: String) {
        context.dataStore.edit { preferences ->
            preferences[TEST_VAL] = string
        }
    }

    val isInitialSetupFinished: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[INITIAL_SETUP_FINISHED] ?: false
        }
}