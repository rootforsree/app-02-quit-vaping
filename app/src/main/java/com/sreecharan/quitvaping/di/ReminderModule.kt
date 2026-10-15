package com.sreecharan.quitvaping.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.sreecharan.quitvaping.data.reminders.ReminderSettingsRepository
import com.sreecharan.quitvaping.data.reminders.ReminderSettingsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.quitPrefsDataStore by preferencesDataStore(name = "quit_prefs")

/**
 * Wires the notifications/widget/backup data layer. The Preferences
 * DataStore file is "quit_prefs", matching the backup allow-list in
 * data_extraction_rules.xml so reminder settings survive a restore.
 */
@Module
@InstallIn(SingletonComponent::class)
object ReminderModule {
    @Provides
    @Singleton
    fun provideQuitPrefsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.quitPrefsDataStore

    @Provides
    @Singleton
    fun provideReminderSettingsRepository(
        dataStore: DataStore<Preferences>,
    ): ReminderSettingsRepository = ReminderSettingsRepositoryImpl(dataStore)
}
