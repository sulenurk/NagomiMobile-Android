package com.sklabs.nagomi.timer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PomodoroEngineTest {
    @Test
    fun countdownUsesAbsoluteTimestamp() {
        val engine = PomodoroEngine(PomodoroSettings(focusMinutes = 1))
        engine.start(nowMillis = 1_000L)

        assertFalse(engine.sync(nowMillis = 30_000L))
        assertEquals(31, engine.remainingSeconds)
        assertTrue(engine.sync(nowMillis = 61_000L))
        assertEquals(0, engine.remainingSeconds)
    }

    @Test
    fun longBreakStartsAtConfiguredInterval() {
        val engine = PomodoroEngine(
            PomodoroSettings(focusMinutes = 1, longBreakAfter = 2, focusCount = 0),
        )

        engine.start(0)
        engine.sync(60_000)
        engine.finishCurrentSession()
        assertEquals(TimerMode.SHORT_BREAK, engine.mode)

        engine.skip()
        engine.start(0)
        engine.sync(60_000)
        engine.finishCurrentSession()
        assertEquals(TimerMode.LONG_BREAK, engine.mode)
    }

    @Test
    fun cycleStopsAtFocusCount() {
        val engine = PomodoroEngine(PomodoroSettings(focusMinutes = 1, focusCount = 1))
        engine.start(0)
        engine.sync(60_000)

        val result = engine.finishCurrentSession()

        assertTrue(result.cycleCompleted)
        assertTrue(engine.cycleCompleted)
        assertEquals(0, engine.remainingSeconds)
    }

    @Test
    fun restoredRunningTimerKeepsItsAbsoluteEndTime() {
        val engine = PomodoroEngine(PomodoroSettings(focusMinutes = 1))
        engine.restore(
            PomodoroSnapshot(
                mode = TimerMode.FOCUS,
                completedFocusCount = 2,
                remainingSeconds = 45,
                endTimestampMillis = 46_000L,
                isRunning = true,
                isPaused = false,
                cycleCompleted = false,
            ),
        )

        assertFalse(engine.sync(16_000L))
        assertEquals(30, engine.remainingSeconds)
        assertEquals(2, engine.completedFocusCount)
        assertTrue(engine.isRunning)
    }

    @Test
    fun autoStartChoiceFollowsTheNextMode() {
        val engine = PomodoroEngine(
            PomodoroSettings(
                focusMinutes = 1,
                shortBreakMinutes = 1,
                focusCount = 0,
                autoStartBreak = true,
                autoStartFocus = false,
            ),
        )

        engine.start(0L)
        engine.sync(60_000L)
        val focusResult = engine.finishCurrentSession()
        assertEquals(TimerMode.SHORT_BREAK, engine.mode)
        assertTrue(focusResult.shouldAutoStart)

        engine.start(60_000L)
        engine.sync(120_000L)
        val breakResult = engine.finishCurrentSession()
        assertEquals(TimerMode.FOCUS, engine.mode)
        assertFalse(breakResult.shouldAutoStart)
    }
}
