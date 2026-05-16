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
    fun infoAndShareStepsCoverBottomBar() {
        assertTrue(shouldCoverBottomBar(RegisterMovieStep.MovieInfo))
        assertTrue(shouldCoverBottomBar(RegisterMovieStep.Share))
    }

    @Test
    fun fullScreenStepSlidesInFromBottomAndOutToBottom() {
        assertTrue(isFullScreenStepVisible(RegisterMovieStep.MovieInfo))
        assertFalse(isFullScreenStepVisible(RegisterMovieStep.MovieSearch))
        assertEquals(320, fullScreenStepEnterOffsetY(320))
        assertEquals(320, fullScreenStepExitOffsetY(320))
    }

    @Test
    fun onlyMovieInfoStepUsesViewingInfoScreen() {
        assertFalse(isViewingInfoScreenStep(RegisterMovieStep.MovieSearch))
        assertTrue(isViewingInfoScreenStep(RegisterMovieStep.MovieInfo))
        assertFalse(isViewingInfoScreenStep(RegisterMovieStep.Share))
    }
}
