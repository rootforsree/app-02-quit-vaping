package com.sreecharan.quitvaping.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        QuitProfileEntity::class,
        CheckInEntity::class,
        CravingEntity::class,
        MilestoneEntity::class,
        SlipEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quitProfileDao(): QuitProfileDao

    abstract fun checkInDao(): CheckInDao

    abstract fun cravingDao(): CravingDao

    abstract fun milestoneDao(): MilestoneDao

    abstract fun slipDao(): SlipDao
}
