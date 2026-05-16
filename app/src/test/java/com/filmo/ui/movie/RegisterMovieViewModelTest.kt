package com.filmo.ui.movie

import org.junit.Assert.assertEquals
import org.junit.Test

class RegisterMovieViewModelTest {
    @Test
    fun updateFieldsReflectsMovieInputState() {
        val viewModel = RegisterMovieViewModel()

        viewModel.updateTitle("괴물")
        viewModel.updateReleaseDateMillis(1_609_459_200_000L)
        viewModel.updateGenre("드라마")
        viewModel.updateDirector("봉준호")
        viewModel.updateCast("송강호, 변희봉")

        val state = viewModel.uiState.value
        assertEquals("괴물", state.title)
        assertEquals(1_609_459_200_000L, state.releaseDateMillis)
        assertEquals("드라마", state.genre)
        assertEquals("봉준호", state.director)
        assertEquals("송강호, 변희봉", state.cast)
    }
}
