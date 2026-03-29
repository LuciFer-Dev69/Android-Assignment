package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val STEP_GOAL = intPreferencesKey("step_goal")
        val USERNAME = stringPreferencesKey("username")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val PROFILE_IMAGE_URI = stringPreferencesKey("profile_image_uri")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val IS_ADMIN = booleanPreferencesKey("is_admin")
        val USER_ID = intPreferencesKey("user_id")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] ?: false
        }

    val stepGoal: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.STEP_GOAL] ?: 5000
        }

    val username: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.USERNAME] }

    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.USER_EMAIL] }

    val profileImageUri: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.PROFILE_IMAGE_URI] }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.IS_LOGGED_IN] ?: false }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true }

    val isAdmin: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.IS_ADMIN] ?: false }

    val userId: Flow<Int?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.USER_ID] }

    suspend fun updateDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDarkMode
        }
    }

    suspend fun updateStepGoal(stepGoal: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STEP_GOAL] = stepGoal
        }
    }

    suspend fun saveUserData(id: Int, username: String, email: String, isAdmin: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = id
            preferences[PreferencesKeys.USERNAME] = username
            preferences[PreferencesKeys.USER_EMAIL] = email
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
            preferences[PreferencesKeys.IS_ADMIN] = isAdmin
        }
    }

    suspend fun updateProfileImage(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROFILE_IMAGE_URI] = uri
        }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = loggedIn
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_ID)
            preferences.remove(PreferencesKeys.USERNAME)
            preferences.remove(PreferencesKeys.USER_EMAIL)
            preferences.remove(PreferencesKeys.PROFILE_IMAGE_URI)
            preferences.remove(PreferencesKeys.IS_ADMIN)
            preferences[PreferencesKeys.IS_LOGGED_IN] = false
        }
    }
}
