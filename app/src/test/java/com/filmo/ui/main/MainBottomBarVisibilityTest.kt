package com.filmo.ui.main

import com.filmo.ui.ScreenDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainBottomBarVisibilityTest {
    @Test
    fun bottomBarUsesCollectionRecordAndTicketViewInOrder() {
        assertEquals(
            listOf(
                BottomBarDestinationSpec(
                    destination = ScreenDestination.Collection,
                    label = "컬렉션"
                ),
                BottomBarDestinationSpec(
                    destination = ScreenDestination.Record,
                    label = "기록하기"
                ),
                BottomBarDestinationSpec(
                    destination = ScreenDestination.TicketView,
                    label = "티켓보기"
                )
            ),
            mainTopLevelDestinationSpecs()
        )
    }

    @Test
    fun navDisplayDoesNotReserveBottomBarSpaceForTopLevelDestinations() {
        assertFalse(shouldReserveBottomBarSpace(ScreenDestination.Collection))
        assertFalse(shouldReserveBottomBarSpace(ScreenDestination.Record))
        assertFalse(shouldReserveBottomBarSpace(ScreenDestination.TicketView))
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
        assertTrue(shouldPlaceBottomBarAboveContent(ScreenDestination.TicketView))
        assertTrue(shouldPlaceBottomBarAboveContent(ScreenDestination.Record))
        assertTrue(shouldPlaceBottomBarAboveContent(ScreenDestination.Collection))
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
                currentDestination = ScreenDestination.TicketView
            )
        )
        assertTrue(
            shouldResetRecordMovieState(
                currentDestination = ScreenDestination.Collection
            )
        )
    }
}
