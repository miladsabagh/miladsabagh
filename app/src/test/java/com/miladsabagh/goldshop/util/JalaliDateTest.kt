package com.miladsabagh.goldshop.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class JalaliDateTest {

    /** Builds epoch millis for a Gregorian date using the JVM's default time zone, matching [JalaliDate]'s own usage. */
    private fun gregorianMillis(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(year, month - 1, day, 12, 0, 0)
        return cal.timeInMillis
    }

    // Reference values cross-checked against the Python `jdatetime` library.
    @Test
    fun `converts Nowruz 1403 correctly`() {
        val ymd = JalaliDate.fromEpochMillis(gregorianMillis(2024, 3, 20))
        assertEquals(1403, ymd.year)
        assertEquals(1, ymd.month)
        assertEquals(1, ymd.day)
    }

    @Test
    fun `converts Nowruz 1402 correctly`() {
        val ymd = JalaliDate.fromEpochMillis(gregorianMillis(2023, 3, 21))
        assertEquals(1402, ymd.year)
        assertEquals(1, ymd.month)
        assertEquals(1, ymd.day)
    }

    @Test
    fun `converts a known historical date correctly`() {
        val ymd = JalaliDate.fromEpochMillis(gregorianMillis(1979, 2, 11))
        assertEquals(1357, ymd.year)
        assertEquals(11, ymd.month)
        assertEquals(22, ymd.day)
    }

    @Test
    fun `converts year 2000 correctly`() {
        val ymd = JalaliDate.fromEpochMillis(gregorianMillis(2000, 1, 1))
        assertEquals(1378, ymd.year)
        assertEquals(10, ymd.month)
        assertEquals(11, ymd.day)
    }

    @Test
    fun `formatNumeric zero-pads month and day`() {
        val formatted = JalaliDate.formatNumeric(gregorianMillis(2024, 3, 20), persianDigits = false)
        assertEquals("1403/01/01", formatted)
    }
}
