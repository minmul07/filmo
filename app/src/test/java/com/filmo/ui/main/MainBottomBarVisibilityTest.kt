package com.filmo.ui.main

import com.filmo.ui.ScreenDestination
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainBottomBarVisibilityTest {
    @Test
    fun topLevelDestinationsReserveBottomBarSpace() {
        assertTrue(
            shouldReserveBottomBarSpace(
                currentDestination = ScreenDestination.Feed
            )
        )
    }

    @Test
    fun recordRouteDoesNotReserveBottomBarSpaceAtNavDisplayLevel() {
        assertFalse(
            shouldReserveBottomBarSpace(
                currentDestination = ScreenDestination.Record
            )
        )
    }

    @Test
    fun recordRouteDoesNotReserveBottomBarSpaceWhenRegisterStepCoversBottomBar() {
        assertFalse(
            shouldReserveBottomBarSpace(
                currentDestination = ScreenDestination.Record,
                recordCoversBottomBar = true
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

    @Test
    fun recordMovieStateIsKeptWhileRecordRouteStaysActive() {
        assertFalse(
            shouldResetRecordMovieState(
                currentDestination = ScreenDestination.Record
            )
        )
    }

    @Test
    fun recordMovieStateIsResetAfterLeavingRecordRoute() {
        assertTrue(
            shouldResetRecordMovieState(
                currentDestination = ScreenDestination.Feed
            )
        )
        assertTrue(
            shouldResetRecordMovieState(
                currentDestination = ScreenDestination.Collection
            )
        )
    }
}
