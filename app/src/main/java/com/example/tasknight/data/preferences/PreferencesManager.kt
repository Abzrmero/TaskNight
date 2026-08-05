package com.example.tasknight.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tasknight_prefs")

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {

        private val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val IS_GUEST = booleanPreferencesKey("is_guest")
        private val GUEST_ID = stringPreferencesKey("guest_id")


        private val GUEST_TASKS = stringPreferencesKey("guest_tasks")
        private val GUEST_STREAK = intPreferencesKey("guest_streak")
        private val GUEST_LAST_ACTIVE_DATE = stringPreferencesKey("guest_last_active_date")
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val MAX_TASKS = intPreferencesKey("max_tasks")
    }



    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING] = true
        }
    }

    fun hasCompletedOnboarding(): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING] ?: false
        }



    suspend fun saveLoginState(userId: String, email: String?, name: String?, isGuest: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_ID] = userId
            preferences[IS_GUEST] = isGuest

            if (email != null && email.isNotEmpty()) {
                preferences[USER_EMAIL] = email
            }
            if (name != null && name.isNotEmpty()) {
                preferences[USER_NAME] = name
            } else {
                preferences[USER_NAME] = if (isGuest) "Guest User" else "User"
            }

            if (isGuest) {
                preferences[GUEST_ID] = userId
            }
        }
    }

    suspend fun clearLoginState() {
        context.dataStore.edit { preferences ->
            preferences.remove(IS_LOGGED_IN)
            preferences.remove(USER_ID)
            preferences.remove(USER_EMAIL)
            preferences.remove(USER_NAME)


        }
    }

    suspend fun logoutAndClearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()

            preferences[HAS_COMPLETED_ONBOARDING] = true
        }
    }

    fun isLoggedIn(): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }

    fun isGuest(): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_GUEST] ?: false
        }

    fun getUserEmail(): Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_EMAIL]
        }

    fun getUserName(): Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_NAME] ?: "User"
        }

    fun getUserId(): Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID]
        }

    fun getGuestId(): Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[GUEST_ID]
        }



    suspend fun saveGuestTasks(tasksJson: String) {
        context.dataStore.edit { preferences ->
            preferences[GUEST_TASKS] = tasksJson
        }
    }

    fun getGuestTasks(): Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[GUEST_TASKS]
        }

    suspend fun saveGuestStreak(streak: Int) {
        context.dataStore.edit { preferences ->
            preferences[GUEST_STREAK] = streak
        }
    }

    fun getGuestStreak(): Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[GUEST_STREAK] ?: 0
        }

    suspend fun saveGuestLastActiveDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[GUEST_LAST_ACTIVE_DATE] = date
        }
    }

    fun getGuestLastActiveDate(): Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[GUEST_LAST_ACTIVE_DATE]
        }



    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }

    fun isDarkMode(): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE] ?: true 
        }

    suspend fun setMaxTasks(maxTasks: Int) {
        context.dataStore.edit { preferences ->
            preferences[MAX_TASKS] = maxTasks
        }
    }

    fun getMaxTasks(): Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[MAX_TASKS] ?: 5
        }
}