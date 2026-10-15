package com.sreecharan.quitvaping.backup

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.sreecharan.quitvaping.data.reminders.ReminderSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manual JSON export/import via MediaStore (Downloads, no permissions needed
 * on API 29+). Belt-and-braces alongside Auto Backup: covers the cases Auto
 * Backup misses (disabled by user/OEM, skipped restore on new phone).
 *
 * Backup is free and never paywalled.
 */
@Singleton
class BackupManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val settingsRepository: ReminderSettingsRepository,
    ) {
        @Serializable
        data class BackupPayload(
            val version: Int = 1,
            val quitDateEpochMillis: Long?,
            val pledgeHour: Int,
            val pledgeMinute: Int,
            val pledgeEnabled: Boolean,
            val highRiskTimes: List<String>,
            val eveningEnabled: Boolean,
            val eveningHour: Int,
            val eveningMinute: Int,
            val weeklySpendCents: Long,
            val firedMoneyThresholds: List<Long>,
            val firedMilestones: List<String>,
            val exportedAt: String,
        )

        private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

        /** Writes a timestamped JSON file to Downloads. Returns the file Uri. */
        suspend fun export(): Uri {
            val settings = settingsRepository.settings.first()
            val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
            val fileName = "quitvaping-backup-$stamp.json"
            val payload =
                BackupPayload(
                    quitDateEpochMillis = settings.quitDateEpochMillis,
                    pledgeHour = settings.pledgeHour,
                    pledgeMinute = settings.pledgeMinute,
                    pledgeEnabled = settings.pledgeEnabled,
                    highRiskTimes = settings.highRiskTimes.map { "${it.first}:${it.second}" },
                    eveningEnabled = settings.eveningReflectionEnabled,
                    eveningHour = settings.eveningHour,
                    eveningMinute = settings.eveningMinute,
                    weeklySpendCents = settings.weeklySpendCents,
                    firedMoneyThresholds = settings.firedMoneyThresholds.toList(),
                    firedMilestones = settings.firedMilestones.toList(),
                    exportedAt = LocalDateTime.now().toString(),
                )
            val bytes = json.encodeToString(BackupPayload.serializer(), payload).toByteArray()

            val values =
                ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/json")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                }
            val resolver = context.contentResolver
            val uri =
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: throw IOException("Could not create backup file")
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: throw IOException("Could not write backup file")
            return uri
        }

        /** Restores settings from a previously exported Uri. */
        suspend fun import(uri: Uri) {
            val bytes =
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IOException("Could not read backup file")
            val payload = json.decodeFromString(BackupPayload.serializer(), bytes.decodeToString())

            payload.quitDateEpochMillis?.let { settingsRepository.setQuitDate(it) }
            settingsRepository.setPledge(payload.pledgeEnabled, payload.pledgeHour, payload.pledgeMinute)
            val times =
                payload.highRiskTimes.mapNotNull { part ->
                    val nums = part.split(":").mapNotNull { it.toIntOrNull() }
                    val h = nums.getOrNull(0)
                    val m = nums.getOrNull(1)
                    if (h != null && m != null && h in 0..23 && m in 0..59) h to m else null
                }
            settingsRepository.setHighRiskTimes(times)
            settingsRepository.setEveningReflection(
                payload.eveningEnabled,
                payload.eveningHour,
                payload.eveningMinute,
            )
            settingsRepository.setWeeklySpendCents(payload.weeklySpendCents)
            payload.firedMoneyThresholds.forEach { settingsRepository.markMoneyThresholdFired(it) }
            payload.firedMilestones.forEach { settingsRepository.markMilestoneFired(it) }
        }
    }
