package com.sreecharan.quitvaping.data.reminders

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private object Keys {
    val QUIT_DATE = longPreferencesKey("quit_date_epoch_millis")
    val PLEDGE_ENABLED = booleanPreferencesKey("pledge_enabled")
    val PLEDGE_HOUR = intPreferencesKey("pledge_hour")
    val PLEDGE_MINUTE = intPreferencesKey("pledge_minute")
    val HIGH_RISK_TIMES = stringPreferencesKey("high_risk_times") // "8:0,13:30"
    val EVENING_ENABLED = booleanPreferencesKey("evening_enabled")
    val EVENING_HOUR = intPreferencesKey("evening_hour")
    val EVENING_MINUTE = intPreferencesKey("evening_minute")
    val WEEKLY_SPEND_CENTS = longPreferencesKey("weekly_spend_cents")
    val FIRED_MONEY = stringPreferencesKey("fired_money_thresholds") // "5000,10000"
    val FIRED_MILESTONES = stringSetPreferencesKey("fired_milestones")
}

interface ReminderSettingsRepository {
    val settings: Flow<ReminderSettings>

    suspend fun setQuitDate(epochMillis: Long)

    suspend fun setPledge(enabled: Boolean, hour: Int, minute: Int)

    suspend fun setHighRiskTimes(times: List<Pair<Int, Int>>)

    suspend fun setEveningReflection(enabled: Boolean, hour: Int, minute: Int)

    suspend fun setWeeklySpendCents(cents: Long)

    suspend fun markMoneyThresholdFired(thresholdCents: Long)

    suspend fun markMilestoneFired(milestoneId: String)

    suspend fun clearAll()
}

@Singleton
class ReminderSettingsRepositoryImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : ReminderSettingsRepository {
        override val settings: Flow<ReminderSettings> =
            dataStore.data
                .catch { e ->
                    if (e is IOException) emit(emptyPreferences()) else throw e
                }
                .map { prefs -> prefs.toSettings() }

        override suspend fun setQuitDate(epochMillis: Long) {
            dataStore.edit { it[Keys.QUIT_DATE] = epochMillis }
        }

        override suspend fun setPledge(enabled: Boolean, hour: Int, minute: Int) {
            dataStore.edit {
                it[Keys.PLEDGE_ENABLED] = enabled
                it[Keys.PLEDGE_HOUR] = hour
                it[Keys.PLEDGE_MINUTE] = minute
            }
        }

        override suspend fun setHighRiskTimes(times: List<Pair<Int, Int>>) {
            val encoded = times.take(3).joinToString(",") { "${it.first}:${it.second}" }
            dataStore.edit { it[Keys.HIGH_RISK_TIMES] = encoded }
        }

        override suspend fun setEveningReflection(enabled: Boolean, hour: Int, minute: Int) {
            dataStore.edit {
                it[Keys.EVENING_ENABLED] = enabled
                it[Keys.EVENING_HOUR] = hour
                it[Keys.EVENING_MINUTE] = minute
            }
        }

        override suspend fun setWeeklySpendCents(cents: Long) {
            dataStore.edit { it[Keys.WEEKLY_SPEND_CENTS] = cents }
        }

        override suspend fun markMoneyThresholdFired(thresholdCents: Long) {
            dataStore.edit { prefs ->
                val current = prefs[Keys.FIRED_MONEY].orEmpty()
                val updated =
                    (current.split(",").filter { it.isNotBlank() } + thresholdCents.toString())
                        .distinct()
                        .joinToString(",")
                prefs[Keys.FIRED_MONEY] = updated
            }
        }

        override suspend fun markMilestoneFired(milestoneId: String) {
            dataStore.edit { prefs ->
                prefs[Keys.FIRED_MILESTONES] = prefs[Keys.FIRED_MILESTONES].orEmpty() + milestoneId
            }
        }

        override suspend fun clearAll() {
            dataStore.edit { it.clear() }
        }

        private fun Preferences.toSettings(): ReminderSettings {
            val highRisk =
                this[Keys.HIGH_RISK_TIMES]
                    .orEmpty()
                    .split(",")
                    .mapNotNull { part ->
                        val nums = part.split(":").mapNotNull { it.toIntOrNull() }
                        val h = nums.getOrNull(0)
                        val m = nums.getOrNull(1)
                        if (h != null && m != null && h in 0..23 && m in 0..59) h to m else null
                    }.take(3)
            val firedMoney =
                this[Keys.FIRED_MONEY]
                    .orEmpty()
                    .split(",")
                    .mapNotNull { it.toLongOrNull() }
                    .toSet()
            return ReminderSettings(
                quitDateEpochMillis = this[Keys.QUIT_DATE],
                pledgeEnabled = this[Keys.PLEDGE_ENABLED] ?: true,
                pledgeHour = this[Keys.PLEDGE_HOUR] ?: 8,
                pledgeMinute = this[Keys.PLEDGE_MINUTE] ?: 0,
                highRiskTimes = highRisk,
                eveningReflectionEnabled = this[Keys.EVENING_ENABLED] ?: false,
                eveningHour = this[Keys.EVENING_HOUR] ?: 21,
                eveningMinute = this[Keys.EVENING_MINUTE] ?: 0,
                weeklySpendCents = this[Keys.WEEKLY_SPEND_CENTS] ?: 0L,
                firedMoneyThresholds = firedMoney,
                firedMilestones = this[Keys.FIRED_MILESTONES].orEmpty(),
            )
        }

    }
