package com.sreecharan.quitvaping.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SlipDao {
    @Query("SELECT * FROM slips ORDER BY timestampEpochMs DESC")
    fun observeSlips(): Flow<List<SlipEntity>>

    @Query("SELECT * FROM slips ORDER BY timestampEpochMs DESC LIMIT 1")
    suspend fun mostRecent(): SlipEntity?

    @Insert
    suspend fun insert(slip: SlipEntity): Long

    @Query("DELETE FROM slips")
    suspend fun clear()
}
