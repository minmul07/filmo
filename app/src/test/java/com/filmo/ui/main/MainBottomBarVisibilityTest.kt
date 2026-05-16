package com.filmo.ui.main

import com.filmo.ui.ScreenDestination
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainBottomBarVisibilityTest {
    @Test
    fun topLevelDestinationsExceptRecordReserveBottomBarSpace() {
        assertTrue(
            shouldReserveBottomBarSpace(
                currentDestination = ScreenDestination.Feed
            )
        )
    }

    @Test
    fun recordDoesNotReserveOuterBottomBarSpace() {
        assertFalse(
            shouldReserveBottomBarSpace(
                currentDestination = ScreenDestination.Record
            )
        )
    }

    @Test
    fun subDestinationsDoNotReserveBottomBarSpace() {
        assertFalse(
            shouldReserveBottomBarSpace(
                currentDestination = ScreenDestination.CollectionDetail(ticketId = "ticket-1")
            )
        )
        assertFalse(
            shouldReserveBottomBarSpace(
                currentDestination = ScreenDestination.TheaterDetail(
                    theaterId = "theater-1",
                    initiallySaved = false
                )
            )
        )
    }
}
