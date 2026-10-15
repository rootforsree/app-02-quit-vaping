package com.sreecharan.quitvaping.data.repository

import com.sreecharan.quitvaping.data.local.CheckInDao
import com.sreecharan.quitvaping.data.local.CheckInEntity
import com.sreecharan.quitvaping.data.local.CravingDao
import com.sreecharan.quitvaping.data.local.MilestoneDao
import com.sreecharan.quitvaping.data.local.MilestoneEntity
import com.sreecharan.quitvaping.data.local.QuitProfileDao
import com.sreecharan.quitvaping.data.local.QuitProfileEntity
import com.sreecharan.quitvaping.data.local.SlipDao
import com.sreecharan.quitvaping.data.model.CheckIn
import com.sreecharan.quitvaping.data.model.DeviceType
import com.sreecharan.quitvaping.data.model.Milestone
import com.sreecharan.quitvaping.data.model.QuitProfile
import com.sreecharan.quitvaping.data.model.QuitStats
import com.sreecharan.quitvaping.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Quit plan, daily check-ins, milestones and aggregate stats.
 * Room is the single source of truth; the UI observes Flows.
 */
interface QuitRepository {
    fun observeProfile(): Flow<QuitProfile?>

    suspend fun saveProfile(profile: QuitProfile)

    fun observeCheckIns(): Flow<List<CheckIn>>

    fun observeCheckInForDay(epochDay: Long): Flow<CheckIn?>

    suspend fun recordPledge(epochDay: Long, atEpochMs: Long)

    suspend fun recordEveningReview(epochDay: Long, review: String, rating: Int)

    fun observeMilestones(): Flow<List<Milestone>>

    /** Marks milestones whose offset has passed since the quit datetime. */
    suspend fun refreshMilestones(nowEpochMs: Long)

    fun observeStats(): Flow<QuitStats>

    /** Full reset (Settings > Reset). Clears profile, check-ins, milestones, cravings, slips. */
    suspend fun resetAll()
}

@Singleton
class QuitRepositoryImpl
    @Inject
    constructor(
        private val profileDao: QuitProfileDao,
        private val checkInDao: CheckInDao,
        private val cravingDao: CravingDao,
        private val milestoneDao: MilestoneDao,
        private val slipDao: SlipDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : QuitRepository {
        override fun observeProfile(): Flow<QuitProfile?> =
            profileDao
                .observeProfile()
                .map { it?.toDomain() }
                .flowOn(ioDispatcher)

        override suspend fun saveProfile(profile: QuitProfile) {
            withContext(ioDispatcher) {
                val now = System.currentTimeMillis()
                profileDao.upsert(profile.toEntity(now))
                milestoneDao.insertAll(
                    MilestoneCatalog.definitions.mapIndexed { index, def ->
                        MilestoneEntity(
                            key = def.key,
                            title = def.title,
                            description = def.description,
                            sortOrder = index,
                            achievedAtEpochMs = null,
                        )
                    },
                )
                refreshMilestones(now)
            }
        }

        override fun observeCheckIns(): Flow<List<CheckIn>> =
            checkInDao
                .observeCheckIns()
                .map { list -> list.map { it.toDomain() } }
                .flowOn(ioDispatcher)

        override fun observeCheckInForDay(epochDay: Long): Flow<CheckIn?> =
            checkInDao
                .observeCheckInForDay(epochDay)
                .map { it?.toDomain() }
                .flowOn(ioDispatcher)

        override suspend fun recordPledge(epochDay: Long, atEpochMs: Long) {
            withContext(ioDispatcher) {
                val existing = checkInDao.getCheckInForDay(epochDay)
                checkInDao.upsert(
                    (existing ?: CheckInEntity(dateEpochDay = epochDay, pledgedAtEpochMs = atEpochMs, eveningReview = null, eveningRating = null))
                        .copy(pledgedAtEpochMs = atEpochMs),
                )
            }
        }

        override suspend fun recordEveningReview(epochDay: Long, review: String, rating: Int) {
            withContext(ioDispatcher) {
                val existing = checkInDao.getCheckInForDay(epochDay)
                checkInDao.upsert(
                    (existing ?: CheckInEntity(dateEpochDay = epochDay, pledgedAtEpochMs = System.currentTimeMillis(), eveningReview = null, eveningRating = null))
                        .copy(eveningReview = review, eveningRating = rating.coerceIn(1, 5)),
                )
            }
        }

        override fun observeMilestones(): Flow<List<Milestone>> =
            milestoneDao
                .observeMilestones()
                .map { list -> list.map { it.toDomain() } }
                .flowOn(ioDispatcher)

        override suspend fun refreshMilestones(nowEpochMs: Long) {
            withContext(ioDispatcher) {
                val profile = profileDao.getProfile() ?: return@withContext
                MilestoneCatalog.definitions.forEach { def ->
                    if (nowEpochMs >= profile.quitDateEpochMs + def.offsetMs) {
                        milestoneDao.markAchieved(def.key, profile.quitDateEpochMs + def.offsetMs)
                    }
                }
            }
        }

        override fun observeStats(): Flow<QuitStats> =
            combine(
                profileDao.observeProfile(),
                cravingDao.observeResistedCount(),
                slipDao.observeSlips(),
                checkInDao.observeCheckIns(),
            ) { profile, cravingsBeaten, slips, checkIns ->
                computeStats(profile, cravingsBeaten, slips.map { it.timestampEpochMs }, checkIns.map { it.dateEpochDay })
            }.flowOn(ioDispatcher)

        override suspend fun resetAll() {
            withContext(ioDispatcher) {
                profileDao.clear()
                checkInDao.clear()
                cravingDao.clear()
                milestoneDao.clear()
                slipDao.clear()
            }
        }

        private fun computeStats(
            profile: QuitProfileEntity?,
            cravingsBeaten: Int,
            slipTimestamps: List<Long>,
            pledgeDays: List<Long>,
        ): QuitStats {
            if (profile == null) {
                return QuitStats(0, 0, 0, 0, 0, 0, 0)
            }
            val now = System.currentTimeMillis()
            val daysQuit = maxOf(0, ChronoUnit.DAYS.between(dayOf(profile.quitDateEpochMs), dayOf(now)))
            val slipDays = slipTimestamps.map { epochDayOf(it) }.toSet()
            val totalCleanDays = maxOf(0, daysQuit - slipDays.size)
            val lastSlipDay = slipDays.maxOrNull()
            val currentStreakDays =
                if (lastSlipDay == null) {
                    daysQuit
                } else {
                    maxOf(0, ChronoUnit.DAYS.between(LocalDate.ofEpochDay(lastSlipDay), dayOf(now)))
                }
            // Money saved accrues only on clean days (relapse-forgiving, still honest).
            val moneySavedCents = (profile.weeklySpendCents.toLong() * totalCleanDays) / 7
            val puffsAvoided = profile.puffsPerDay.toLong() * totalCleanDays
            return QuitStats(
                daysQuit = daysQuit,
                currentStreakDays = currentStreakDays,
                totalCleanDays = totalCleanDays,
                moneySavedCents = moneySavedCents,
                puffsAvoided = puffsAvoided,
                cravingsBeaten = cravingsBeaten,
                pledgeStreakDays = consecutivePledgeStreak(pledgeDays),
            )
        }

        /** Counts back-to-back pledged days ending today or yesterday. */
        private fun consecutivePledgeStreak(pledgeDays: List<Long>): Int {
            if (pledgeDays.isEmpty()) return 0
            val sorted = pledgeDays.sortedDescending()
            val today = LocalDate.now(ZoneOffset.UTC).toEpochDay()
            var cursor = if (sorted[0] >= today - 1) sorted[0] else return 0
            var streak = 0
            for (day in sorted) {
                if (day == cursor) {
                    streak++
                    cursor--
                } else if (day < cursor) {
                    break
                }
            }
            return streak
        }

        private fun dayOf(epochMs: Long): LocalDate =
            Instant.ofEpochMilli(epochMs).atZone(ZoneOffset.UTC).toLocalDate()

        private fun epochDayOf(epochMs: Long): Long = dayOf(epochMs).toEpochDay()
    }

private fun QuitProfileEntity.toDomain(): QuitProfile =
    QuitProfile(
        quitDateEpochMs = quitDateEpochMs,
        deviceType = runCatching { DeviceType.valueOf(deviceType) }.getOrDefault(DeviceType.DISPOSABLE),
        nicotineMgPerMl = nicotineMgPerMl,
        puffsPerDay = puffsPerDay,
        weeklySpendCents = weeklySpendCents,
        costPerUnitCents = costPerUnitCents,
        triggers = runCatching { JSONArray(triggersJson).let { arr -> List(arr.length()) { i -> arr.getString(i) } } }.getOrDefault(emptyList()),
    )

private fun QuitProfile.toEntity(nowEpochMs: Long): QuitProfileEntity =
    QuitProfileEntity(
        quitDateEpochMs = quitDateEpochMs,
        deviceType = deviceType.name,
        nicotineMgPerMl = nicotineMgPerMl,
        puffsPerDay = puffsPerDay,
        weeklySpendCents = weeklySpendCents,
        costPerUnitCents = costPerUnitCents,
        triggersJson = JSONArray(triggers).toString(),
        createdAtEpochMs = nowEpochMs,
    )

private fun CheckInEntity.toDomain(): CheckIn =
    CheckIn(
        id = id,
        dateEpochDay = dateEpochDay,
        pledgedAtEpochMs = pledgedAtEpochMs,
        eveningReview = eveningReview,
        eveningRating = eveningRating,
    )

private fun MilestoneEntity.toDomain(): Milestone =
    Milestone(
        key = key,
        title = title,
        description = description,
        achievedAtEpochMs = achievedAtEpochMs,
    )
