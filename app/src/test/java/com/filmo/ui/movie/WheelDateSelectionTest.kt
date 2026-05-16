package com.filmo.ui.movie

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class WheelDateSelectionTest {
    @Test
    fun changingMonthClampsDayToLastDayOfMonth() {
        val selection = WheelDateSelection(year = 2026, month = 1, day = 31)

        val result = selection.withMonth(2)

        assertEquals(WheelDateSelection(year = 2026, month = 2, day = 28), result)
    }

    @Test
    fun changingYearPreservesLeapDayOnlyForLeapYear() {
        val selection = WheelDateSelection(year = 2024, month = 2, day = 29)

        val result = selection.withYear(2025)

        assertEquals(WheelDateSelection(year = 2025, month = 2, day = 28), result)
    }

    @Test
    fun convertsSelectionToStartOfLocalDateMillis() {
        val zoneId = ZoneId.of("Asia/Seoul")
        val selection = WheelDateSelection(year = 2026, month = 5, day = 17)

        val result = selection.toStartOfDayMillis(zoneId)

        assertEquals(
            LocalDate.of(2026, 5, 17)
                .atStartOfDay(zoneId)
                .toInstant()
                .toEpochMilli(),
            result
        )
    }

    @Test
    fun createsSelectionFromExistingMillis() {
        val zoneId = ZoneId.of("Asia/Seoul")
        val millis = LocalDate.of(2026, 5, 17)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        val result = millis.toWheelDateSelection(zoneId)

        assertEquals(WheelDateSelection(year = 2026, month = 5, day = 17), result)
    }
}
