package com.sreecharan.quitvaping.data.datastore

import androidx.datastore.core.DataStore
import com.sreecharan.quitvaping.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/**
 * Domain-level app settings: theme, haptics and the master notifications toggle.
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

class SettingsRepositoryImpl
    @Inject
    constructor(
        private val dataStore: DataStore<SettingsProto>,
    ) : SettingsRepository {
        override val settings: Flow<AppSettings> =
            dataStore.data
                .catch { e ->
                    if (e is IOException) {
                        emit(SettingsSerializer.defaultValue)
                    } else {
                        throw e
                    }
                }.map { proto ->
                    AppSettings(
                        themeMode = proto.themeMode.toDomain(),
                        hapticsEnabled = proto.hapticsEnabled,
                        notificationsEnabled = proto.notificationsEnabled,
                    )
                }

        override suspend fun setThemeMode(mode: ThemeMode) {
            dataStore.updateData { current ->
                current.toBuilder().setThemeMode(mode.toProto()).build()
            }
        }

        override suspend fun setHapticsEnabled(enabled: Boolean) {
            dataStore.updateData { current ->
                current.toBuilder().setHapticsEnabled(enabled).build()
            }
        }

        override suspend fun setNotificationsEnabled(enabled: Boolean) {
            dataStore.updateData { current ->
                current.toBuilder().setNotificationsEnabled(enabled).build()
            }
        }

        private fun ThemeModeProto.toDomain(): ThemeMode =
            when (this) {
                ThemeModeProto.THEME_MODE_PROTO_UNSPECIFIED -> ThemeMode.SYSTEM
                ThemeModeProto.SYSTEM -> ThemeMode.SYSTEM
                ThemeModeProto.LIGHT -> ThemeMode.LIGHT
                ThemeModeProto.DARK -> ThemeMode.DARK
                ThemeModeProto.UNRECOGNIZED -> ThemeMode.SYSTEM
            }

        private fun ThemeMode.toProto(): ThemeModeProto =
            when (this) {
                ThemeMode.SYSTEM -> ThemeModeProto.SYSTEM
                ThemeMode.LIGHT -> ThemeModeProto.LIGHT
                ThemeMode.DARK -> ThemeModeProto.DARK
            }
    }
