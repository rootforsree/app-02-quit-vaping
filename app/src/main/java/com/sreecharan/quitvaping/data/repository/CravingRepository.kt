package com.sreecharan.quitvaping.data.repository

import com.sreecharan.quitvaping.data.local.CravingDao
import com.sreecharan.quitvaping.data.local.CravingEntity
import com.sreecharan.quitvaping.data.local.SlipDao
import com.sreecharan.quitvaping.data.local.SlipEntity
import com.sreecharan.quitvaping.data.model.Craving
import com.sreecharan.quitvaping.data.model.Slip
import com.sreecharan.quitvaping.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Craving events and slip log. The SOS flow writes here; the Cravings tab reads here.
 */
interface CravingRepository {
    fun observeRecentCravings(limit: Int = 50): Flow<List<Craving>>

    fun observeCravingsBeatenCount(): Flow<Int>

    suspend fun logCraving(
        timestampEpochMs: Long,
        intensity: Int,
        trigger: String,
        resisted: Boolean,
        notes: String?,
    )

    fun observeSlips(): Flow<List<Slip>>

    suspend fun logSlip(timestampEpochMs: Long, trigger: String?, notes: String?)
}

@Singleton
class CravingRepositoryImpl
    @Inject
    constructor(
        private val cravingDao: CravingDao,
        private val slipDao: SlipDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : CravingRepository {
        override fun observeRecentCravings(limit: Int): Flow<List<Craving>> =
            cravingDao
                .observeRecent(limit)
                .map { list -> list.map { it.toDomain() } }
                .flowOn(ioDispatcher)

        override fun observeCravingsBeatenCount(): Flow<Int> =
            cravingDao.observeResistedCount().flowOn(ioDispatcher)

        override suspend fun logCraving(
            timestampEpochMs: Long,
            intensity: Int,
            trigger: String,
            resisted: Boolean,
            notes: String?,
        ) {
            withContext(ioDispatcher) {
                cravingDao.insert(
                    CravingEntity(
                        timestampEpochMs = timestampEpochMs,
                        intensity = intensity.coerceIn(1, 10),
                        trigger = trigger,
                        resisted = resisted,
                        notes = notes,
                    ),
                )
            }
        }

        override fun observeSlips(): Flow<List<Slip>> =
            slipDao
                .observeSlips()
                .map { list -> list.map { it.toDomain() } }
                .flowOn(ioDispatcher)

        override suspend fun logSlip(timestampEpochMs: Long, trigger: String?, notes: String?) {
            withContext(ioDispatcher) {
                slipDao.insert(
                    SlipEntity(
                        timestampEpochMs = timestampEpochMs,
                        trigger = trigger,
                        notes = notes,
                    ),
                )
            }
        }
    }

private fun CravingEntity.toDomain(): Craving =
    Craving(
        id = id,
        timestampEpochMs = timestampEpochMs,
        intensity = intensity,
        trigger = trigger,
        resisted = resisted,
        notes = notes,
    )

private fun SlipEntity.toDomain(): Slip =
    Slip(
        id = id,
        timestampEpochMs = timestampEpochMs,
        trigger = trigger,
        notes = notes,
    )
