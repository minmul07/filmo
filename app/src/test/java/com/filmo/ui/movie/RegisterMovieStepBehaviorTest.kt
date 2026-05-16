package com.filmo.ui.movie

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterMovieStepBehaviorTest {
    @Test
    fun shareStepStartsTicketCreationWhenEntered() {
        assertTrue(
            shouldCreateTicketWhenStepEntered(
                step = RegisterMovieStep.Share,
                isTicketCreated = false
            )
        )
    }

    @Test
    fun viewingInfoStepDoesNotStartTicketCreationWhenEntered() {
        assertFalse(
            shouldCreateTicketWhenStepEntered(
                step = RegisterMovieStep.MovieInfo,
                isTicketCreated = false
            )
        )
    }

    @Test
    fun shareStepDoesNotStartTicketCreationAgainAfterTicketCreated() {
        assertFalse(
            shouldCreateTicketWhenStepEntered(
                step = RegisterMovieStep.Share,
                isTicketCreated = true
            )
        )
    }

    @Test
    fun shareStepUsesCloseActionInsteadOfBackAction() {
        assertFalse(shouldShowRegisterMovieBackButton(RegisterMovieStep.Share))
        assertTrue(shouldShowRegisterMovieCloseButton(RegisterMovieStep.Share))
    }

    @Test
    fun viewingInfoStepKeepsBackAction() {
        assertTrue(shouldShowRegisterMovieBackButton(RegisterMovieStep.MovieInfo))
        assertFalse(shouldShowRegisterMovieCloseButton(RegisterMovieStep.MovieInfo))
    }
}
