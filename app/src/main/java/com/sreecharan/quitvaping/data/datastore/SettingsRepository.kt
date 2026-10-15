package com.sreecharan.quitvaping.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sreecharan.quitvaping.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Domain-level app settings: theme, haptics and the master notifications toggle.
 *
 * Uses Preferences DataStore (simple key-value, no proto codegen).
 *
 * Deliberately narrow: onboarding completion is owned by [OnboardingRepository],
 * the Play Billing entitlement cache by the billing agent, and per-type
 * reminder prefs by the reminders agent. One source of truth each.
 */
data class AppSettings(
    val themeMode: ThemeMode,
    val hapticsEnabled: Boolean,
    val notificationsEnabled: Boolean,
)

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setHapticsEnabled(enabled: Boolean)

    suspend fun setNotificationsEnabled(enabled: Boolean)
}

@Singleton
class SettingsRepositoryImpl
    @Inject
    constructor(
        @Named("settings") private val dataStore: DataStore<Preferences>,
    ) : SettingsRepository {
        private object Keys {
            val THEME_MODE = stringPreferencesKey("theme_mode")
            val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
            val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        }

        override val settings: Flow<AppSettings> =
            dataStore.data.map { prefs ->
                AppSettings(
                    themeMode = prefs[Keys.THEME_MODE]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM,
                    hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
                    notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                )
            }

        override suspend fun setThemeMode(mode: ThemeMode) {
            dataStore.edit { it[Keys.THEME_MODE] = mode.name }
        }

        override suspend fun setHapticsEnabled(enabled: Boolean) {
            dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
        }

        override suspend fun setNotificationsEnabled(enabled: Boolean) {
            dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
        }
    }
