package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "electrotrack_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val ONLINE_SELLING_ENABLED = booleanPreferencesKey("online_selling_enabled")
        private val APP_ACCESS_ENABLED = booleanPreferencesKey("app_access_enabled")
        private val CURRENT_PERIOD_END = stringPreferencesKey("current_period_end")
        private val PUSH_NOTIFICATIONS_ENABLED = booleanPreferencesKey("push_notifications_enabled")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[ACCESS_TOKEN]
    }

    val refreshToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[REFRESH_TOKEN]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_EMAIL]
    }

    val userName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_NAME]
    }

    val onlineSellingEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONLINE_SELLING_ENABLED] ?: false
    }

    val appAccessEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[APP_ACCESS_ENABLED] ?: false
    }

    val currentPeriodEnd: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[CURRENT_PERIOD_END]
    }

    val pushNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PUSH_NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveUser(
        email: String,
        name: String,
        onlineSellingEnabled: Boolean,
        appAccessEnabled: Boolean,
        currentPeriodEnd: String?
    ) {
        context.dataStore.edit { prefs ->
            prefs[USER_EMAIL] = email
            prefs[USER_NAME] = name
            prefs[ONLINE_SELLING_ENABLED] = onlineSellingEnabled
            prefs[APP_ACCESS_ENABLED] = appAccessEnabled
            currentPeriodEnd?.let { prefs[CURRENT_PERIOD_END] = it }
        }
    }

    suspend fun setPushNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PUSH_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
            prefs.remove(REFRESH_TOKEN)
            prefs.remove(USER_EMAIL)
            prefs.remove(USER_NAME)
            prefs.remove(ONLINE_SELLING_ENABLED)
            prefs.remove(APP_ACCESS_ENABLED)
            prefs.remove(CURRENT_PERIOD_END)
        }
    }
}
