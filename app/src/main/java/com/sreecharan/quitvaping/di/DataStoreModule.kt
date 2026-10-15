package com.sreecharan.quitvaping.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.sreecharan.quitvaping.data.datastore.SettingsProto
import com.sreecharan.quitvaping.data.datastore.SettingsSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    private val Context.settingsDataStore by dataStore(
        fileName = "settings.pb",
        serializer = SettingsSerializer,
    )

    private val Context.onboardingDataStore by preferencesDataStore(
        name = "onboarding",
    )

    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<SettingsProto> = context.settingsDataStore

    @Provides
    @Singleton
    @Named("onboarding")
    fun provideOnboardingDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.onboardingDataStore
}
