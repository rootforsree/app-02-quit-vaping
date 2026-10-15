package com.sreecharan.quitvaping.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones ORDER BY sortOrder ASC")
    fun observeMilestones(): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones WHERE achievedAtEpochMs IS NULL ORDER BY sortOrder ASC LIMIT 1")
    suspend fun nextUnachieved(): MilestoneEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(milestones: List<MilestoneEntity>)

    @Query("UPDATE milestones SET achievedAtEpochMs = :achievedAt WHERE `key` = :key AND achievedAtEpochMs IS NULL")
    suspend fun markAchieved(key: String, achievedAt: Long)

    @Query("DELETE FROM milestones")
    suspend fun clear()
}
