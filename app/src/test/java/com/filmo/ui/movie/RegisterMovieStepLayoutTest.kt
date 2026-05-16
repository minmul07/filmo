package com.filmo.ui.movie

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterMovieStepLayoutTest {
    @Test
    fun searchStepDoesNotCoverBottomBar() {
        assertFalse(shouldCoverBottomBar(RegisterMovieStep.MovieSearch))
    }

    @Test
    fun registerStepsDoNotCoverBottomBar() {
        assertFalse(shouldCoverBottomBar(RegisterMovieStep.MovieSearch))
    }

    @Test
    fun registerScreenDoesNotMountViewingInfoAsInternalStep() {
        assertFalse(isFullScreenStepVisible(RegisterMovieStep.MovieSearch))
        assertFalse(isFullScreenStepVisible(RegisterMovieStep.MovieInfo))
        assertFalse(isFullScreenStepVisible(RegisterMovieStep.Share))
    }

    @Test
    fun viewingInfoRouteSlidesInFromBottomAndOutToBottom() {
        assertEquals(320, viewingInfoRouteEnterOffsetY(320))
        assertEquals(320, viewingInfoRouteExitOffsetY(320))
    }

    @Test
    fun onlyMovieInfoStepUsesViewingInfoScreen() {
        assertFalse(isViewingInfoScreenStep(RegisterMovieStep.MovieSearch))
        assertTrue(isViewingInfoScreenStep(RegisterMovieStep.MovieInfo))
        assertFalse(isViewingInfoScreenStep(RegisterMovieStep.Share))
    }
}
