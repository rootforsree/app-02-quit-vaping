package com.sreecharan.quitvaping.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface QuitProfileDao {
    @Query("SELECT * FROM quit_profile WHERE id = 1")
    fun observeProfile(): Flow<QuitProfileEntity?>

    @Query("SELECT * FROM quit_profile WHERE id = 1")
    suspend fun getProfile(): QuitProfileEntity?

    @Upsert
    suspend fun upsert(profile: QuitProfileEntity)

    @Query("DELETE FROM quit_profile")
    suspend fun clear()
}
