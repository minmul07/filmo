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
        assertFalse(
            shouldReserveBottomBarSpace(
                currentDestination = ScreenDestination.RecordViewingInfo
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
    fun topLevelDestinationsPlaceBottomBarAboveContent() {
        assertTrue(shouldPlaceBottomBarAboveContent(ScreenDestination.Feed))
        assertTrue(shouldPlaceBottomBarAboveContent(ScreenDestination.Record))
        assertTrue(shouldPlaceBottomBarAboveContent(ScreenDestination.TheaterFinder))
        assertTrue(shouldPlaceBottomBarAboveContent(ScreenDestination.Collection))
        assertTrue(shouldPlaceBottomBarAboveContent(ScreenDestination.Profile))
    }

    @Test
    fun subDestinationsDoNotPlaceBottomBarAboveContent() {
        assertFalse(
            shouldPlaceBottomBarAboveContent(
                ScreenDestination.RecordViewingInfo
            )
        )
        assertFalse(
            shouldPlaceBottomBarAboveContent(
                ScreenDestination.CollectionDetail(ticketId = "ticket-1")
            )
        )
        assertFalse(
            shouldPlaceBottomBarAboveContent(
                ScreenDestination.TheaterDetail(
                    theaterId = "theater-1",
                    initiallySaved = false
                )
            )
        )
    }

    @Test
    fun subDestinationsDoNotReserveBottomBarSpace() {
        assertFalse(
            shouldReserveBottomBarSpace(
                currentDestination = ScreenDestination.RecordViewingInfo
            )
        )
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
        assertFalse(
            shouldResetRecordMovieState(
                currentDestination = ScreenDestination.RecordViewingInfo
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
