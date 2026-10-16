package com.sreecharan.quitvaping.di

import com.sreecharan.quitvaping.data.datastore.OnboardingRepository
import com.sreecharan.quitvaping.data.datastore.OnboardingRepositoryImpl
import com.sreecharan.quitvaping.data.datastore.SettingsRepository
import com.sreecharan.quitvaping.data.datastore.SettingsRepositoryImpl
import com.sreecharan.quitvaping.data.repository.CravingRepository
import com.sreecharan.quitvaping.data.repository.CravingRepositoryImpl
import com.sreecharan.quitvaping.data.repository.QuitRepository
import com.sreecharan.quitvaping.data.repository.QuitRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    abstract fun bindOnboardingRepository(impl: OnboardingRepositoryImpl): OnboardingRepository

    @Binds
    abstract fun bindQuitRepository(impl: QuitRepositoryImpl): QuitRepository

    @Binds
    abstract fun bindCravingRepository(impl: CravingRepositoryImpl): CravingRepository
}
