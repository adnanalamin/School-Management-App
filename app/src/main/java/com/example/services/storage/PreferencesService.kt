package com.example.services.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.constants.AppConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "edumanage_prefs")

/**
 * Service to persist client-side preferences (active tenant ID, theme mode, locale).
 */
class PreferencesService(private val context: Context) {

    private val KEY_ACTIVE_SCHOOL_ID = stringPreferencesKey(AppConstants.PREFS_KEY_ACTIVE_SCHOOL_ID)
    private val KEY_THEME_MODE = stringPreferencesKey(AppConstants.PREFS_KEY_APP_THEME)

    val activeSchoolId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_ACTIVE_SCHOOL_ID]
    }

    val themeMode: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE]
    }

    suspend fun saveActiveSchoolId(schoolId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACTIVE_SCHOOL_ID] = schoolId
        }
    }

    suspend fun saveThemeMode(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = theme
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_ACTIVE_SCHOOL_ID)
        }
    }
}
