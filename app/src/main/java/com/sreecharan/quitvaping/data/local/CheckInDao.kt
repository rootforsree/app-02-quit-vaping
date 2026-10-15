package com.sreecharan.quitvaping.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins ORDER BY dateEpochDay DESC")
    fun observeCheckIns(): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE dateEpochDay = :epochDay")
    fun observeCheckInForDay(epochDay: Long): Flow<CheckInEntity?>

    @Query("SELECT * FROM check_ins WHERE dateEpochDay = :epochDay")
    suspend fun getCheckInForDay(epochDay: Long): CheckInEntity?

    @Upsert
    suspend fun upsert(checkIn: CheckInEntity)

    @Query("DELETE FROM check_ins")
    suspend fun clear()
}
