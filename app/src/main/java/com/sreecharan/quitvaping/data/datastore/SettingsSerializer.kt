package com.sreecharan.quitvaping.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

/**
 * Reads and writes [SettingsProto] for the Proto DataStore.
 * Defaults: system theme, haptics on, notifications on.
 *
 * Ownership note: onboarding completion lives in [OnboardingRepository]
 * (Preferences), the Play Billing entitlement cache lives in the billing
 * agent's store, and per-type reminder prefs live in the reminders agent's
 * quit_prefs. This proto holds only theme, haptics and the master
 * notifications toggle.
 */
object SettingsSerializer : Serializer<SettingsProto> {
    override val defaultValue: SettingsProto =
        SettingsProto
            .newBuilder()
            .setThemeMode(ThemeModeProto.SYSTEM)
            .setHapticsEnabled(true)
            .setNotificationsEnabled(true)
            .build()

    override suspend fun readFrom(input: InputStream): SettingsProto =
        try {
            SettingsProto.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read settings proto.", e)
        }

    override suspend fun writeTo(
        t: SettingsProto,
        output: OutputStream,
    ) {
        t.writeTo(output)
    }
}
