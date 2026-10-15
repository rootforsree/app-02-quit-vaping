package com.sreecharan.quitvaping.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CravingDao {
    @Query("SELECT * FROM cravings ORDER BY timestampEpochMs DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<CravingEntity>>

    @Query("SELECT * FROM cravings ORDER BY timestampEpochMs DESC")
    fun observeAll(): Flow<List<CravingEntity>>

    @Query("SELECT COUNT(*) FROM cravings WHERE resisted = 1")
    fun observeResistedCount(): Flow<Int>

    @Insert
    suspend fun insert(craving: CravingEntity): Long

    @Query("DELETE FROM cravings")
    suspend fun clear()
}
