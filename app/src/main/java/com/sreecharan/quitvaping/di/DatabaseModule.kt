package com.sreecharan.quitvaping.di

import android.content.Context
import androidx.room.Room
import com.sreecharan.quitvaping.data.local.AppDatabase
import com.sreecharan.quitvaping.data.local.CheckInDao
import com.sreecharan.quitvaping.data.local.CravingDao
import com.sreecharan.quitvaping.data.local.MilestoneDao
import com.sreecharan.quitvaping.data.local.QuitProfileDao
import com.sreecharan.quitvaping.data.local.SlipDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room
            .databaseBuilder(context, AppDatabase::class.java, "app.db")
            // Pre-release schema: version 1 -> 2 replaced the template notes table
            // with the quit-tracking tables. No shipped users, so destructive is safe.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideQuitProfileDao(database: AppDatabase): QuitProfileDao = database.quitProfileDao()

    @Provides
    fun provideCheckInDao(database: AppDatabase): CheckInDao = database.checkInDao()

    @Provides
    fun provideCravingDao(database: AppDatabase): CravingDao = database.cravingDao()

    @Provides
    fun provideMilestoneDao(database: AppDatabase): MilestoneDao = database.milestoneDao()

    @Provides
    fun provideSlipDao(database: AppDatabase): SlipDao = database.slipDao()
}
