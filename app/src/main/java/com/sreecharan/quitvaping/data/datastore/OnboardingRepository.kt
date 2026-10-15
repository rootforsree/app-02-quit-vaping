package com.sreecharan.quitvaping.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.sreecharan.quitvaping.data.model.DeviceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Named

/**
 * Everything collected during the 7-screen onboarding flow. Persisted to
 * Preferences DataStore so the home/counter agent can read quit date and
 * weekly spend without touching this flow again.
 *
 * Field contract (for cross-agent use):
 * - quitDateEpochMs: start of the quit day in device-local millis. The day
 *   counter counts from here.
 * - weeklySpendCents: estimated weekly vape spend in cents. Money-saved =
 *   weeklySpendCents / 100 / 7 * daysQuit.
 * - puffsPerDay: -1 means "not sure".
 * - nicotineMgPerMl: 0f means "not sure".
 * - triggers: raw trigger labels, e.g. {"Stress", "Boredom"}.
 * - pledgeHour/pledgeMinute: local time for the daily pledge reminder.
 */
data class OnboardingData(
    val quitDateEpochMs: Long = defaultQuitDate(),
    val deviceType: DeviceType = DeviceType.DISPOSABLE,
    val unitsPerWeek: Int = 2,
    val costPerUnitCents: Int = 1500,
    val puffsPerDay: Int = -1,
    val nicotineMgPerMl: Float = 0f,
    val triggers: Set<String> = emptySet(),
    val pledgeHour: Int = 8,
    val pledgeMinute: Int = 0,
    val milestoneCelebrations: Boolean = true,
    val onboardingCompleted: Boolean = false,
) {
    /** Estimated yearly savings in dollars, updated live on screen 3. */
    val yearlySavingsDollars: Double
        get() = unitsPerWeek * (costPerUnitCents / 100.0) * 52
}

private fun defaultQuitDate(): Long =
    LocalDate
        .now()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

interface OnboardingRepository {
    val onboardingData: Flow<OnboardingData>
    val isOnboardingCompleted: Flow<Boolean>

    suspend fun saveOnboarding(data: OnboardingData)

    suspend fun setOnboardingCompleted()
}

class OnboardingRepositoryImpl
    @Inject
    constructor(
        @Named("onboarding") private val dataStore: DataStore<Preferences>,
    ) : OnboardingRepository {
        private object Keys {
            val QUIT_DATE = longPreferencesKey("onboarding_quit_date_ms")
            val DEVICE_TYPE = stringPreferencesKey("onboarding_device_type")
            val UNITS_PER_WEEK = intPreferencesKey("onboarding_units_per_week")
            val COST_PER_UNIT_CENTS = intPreferencesKey("onboarding_cost_per_unit_cents")
            val PUFFS_PER_DAY = intPreferencesKey("onboarding_puffs_per_day")
            val NICOTINE_MG = floatPreferencesKey("onboarding_nicotine_mg")
            val TRIGGERS = stringSetPreferencesKey("onboarding_triggers")
            val PLEDGE_HOUR = intPreferencesKey("onboarding_pledge_hour")
            val PLEDGE_MINUTE = intPreferencesKey("onboarding_pledge_minute")
            val MILESTONE_CELEBRATIONS = booleanPreferencesKey("onboarding_milestone_celebrations")
            val COMPLETED = booleanPreferencesKey("onboarding_completed")
        }

        override val onboardingData: Flow<OnboardingData> =
            dataStore.data
                .catch { e ->
                    if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e
                }.map { prefs ->
                    val defaults = OnboardingData()
                    OnboardingData(
                        quitDateEpochMs = prefs[Keys.QUIT_DATE] ?: defaults.quitDateEpochMs,
                        deviceType =
                            prefs[Keys.DEVICE_TYPE]?.let {
                                runCatching { DeviceType.valueOf(it) }.getOrDefault(defaults.deviceType)
                            } ?: defaults.deviceType,
                        unitsPerWeek = prefs[Keys.UNITS_PER_WEEK] ?: defaults.unitsPerWeek,
                        costPerUnitCents = prefs[Keys.COST_PER_UNIT_CENTS] ?: defaults.costPerUnitCents,
                        puffsPerDay = prefs[Keys.PUFFS_PER_DAY] ?: defaults.puffsPerDay,
                        nicotineMgPerMl = prefs[Keys.NICOTINE_MG] ?: defaults.nicotineMgPerMl,
                        triggers = prefs[Keys.TRIGGERS] ?: defaults.triggers,
                        pledgeHour = prefs[Keys.PLEDGE_HOUR] ?: defaults.pledgeHour,
                        pledgeMinute = prefs[Keys.PLEDGE_MINUTE] ?: defaults.pledgeMinute,
                        milestoneCelebrations = prefs[Keys.MILESTONE_CELEBRATIONS] ?: defaults.milestoneCelebrations,
                        onboardingCompleted = prefs[Keys.COMPLETED] ?: false,
                    )
                }

        override val isOnboardingCompleted: Flow<Boolean> =
            onboardingData.map { it.onboardingCompleted }

        override suspend fun saveOnboarding(data: OnboardingData) {
            dataStore.edit { prefs ->
                prefs[Keys.QUIT_DATE] = data.quitDateEpochMs
                prefs[Keys.DEVICE_TYPE] = data.deviceType.name
                prefs[Keys.UNITS_PER_WEEK] = data.unitsPerWeek
                prefs[Keys.COST_PER_UNIT_CENTS] = data.costPerUnitCents
                prefs[Keys.PUFFS_PER_DAY] = data.puffsPerDay
                prefs[Keys.NICOTINE_MG] = data.nicotineMgPerMl
                prefs[Keys.TRIGGERS] = data.triggers
                prefs[Keys.PLEDGE_HOUR] = data.pledgeHour
                prefs[Keys.PLEDGE_MINUTE] = data.pledgeMinute
                prefs[Keys.MILESTONE_CELEBRATIONS] = data.milestoneCelebrations
                prefs[Keys.COMPLETED] = true
            }
        }

        override suspend fun setOnboardingCompleted() {
            dataStore.edit { prefs ->
                prefs[Keys.COMPLETED] = true
            }
        }
    }
