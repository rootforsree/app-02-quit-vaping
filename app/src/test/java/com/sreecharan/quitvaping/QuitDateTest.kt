package com.sreecharan.quitvaping

import com.sreecharan.quitvaping.notifications.Milestones
import com.sreecharan.quitvaping.notifications.QuitDate
import com.sreecharan.quitvaping.notifications.ReminderScheduler
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Pure-JVM tests for quit math. No Android framework needed. */
class QuitDateTest {
    @Test
    fun `quit day counts as day 1`() {
        val now = System.currentTimeMillis()
        assertEquals(1, QuitDate.daysClean(now, now))
    }

    @Test
    fun `null quit date is zero days`() {
        assertEquals(0, QuitDate.daysClean(null))
    }

    @Test
    fun `future quit date is zero days`() {
        val now = System.currentTimeMillis()
        assertEquals(0, QuitDate.daysClean(now + 86_400_000L, now))
    }

    @Test
    fun `money saved prorates weekly spend`() {
        // $70/week, 7 days clean -> $70.00
        assertEquals(7_000L, QuitDate.moneySavedCents(7_000L, 7))
        // $70/week, 1 day clean -> $10.00
        assertEquals(1_000L, QuitDate.moneySavedCents(7_000L, 1))
    }

    @Test
    fun `money formatting`() {
        assertEquals("$10.00", QuitDate.formatMoney(1_000L))
        assertEquals("$1,000.50".replace(",", ""), QuitDate.formatMoney(100_050L).replace(",", ""))
    }

    @Test
    fun `milestones fire in order and once`() {
        val quit = Instant.now().minus(Duration.ofDays(8)).toEpochMilli()
        val due = Milestones.due(quit, emptySet())
        val ids = due.map { it.id }
        assertTrue("m_20min" in ids)
        assertTrue("m_1w" in ids)
        // 2-week milestone not due yet
        assertTrue("m_2w" !in ids)
        // Already-fired milestones are excluded
        val dueAgain = Milestones.due(quit, ids.toSet())
        assertTrue(dueAgain.isEmpty())
    }

    @Test
    fun `next daily trigger is strictly in the future`() {
        val now = System.currentTimeMillis()
        val trigger = ReminderScheduler.nextDailyTrigger(0, 0, now)
        assertTrue(trigger > now)
        assertTrue(trigger - now <= 24 * 60 * 60 * 1000L)
    }
}
