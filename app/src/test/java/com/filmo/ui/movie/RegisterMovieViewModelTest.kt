package com.filmo.ui.movie

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun nextFromInfoInputRequiresTitleDateAndGenre() {
        val viewModel = RegisterMovieViewModel()

        viewModel.goToNextStep()
        viewModel.goToNextStep()

        assertEquals(RegisterMovieStep.MovieInfo, viewModel.uiState.value.step)
        assertEquals("영화 제목, 관람 날짜, 장르를 입력해 주세요.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun directorAndCastAreOptionalForTicketTemplateStep() {
        val viewModel = RegisterMovieViewModel()

        viewModel.updateTitle("괴물")
        viewModel.updateReleaseDateMillis(
            releaseDateMillis = 1_609_459_200_000L,
            nowMillis = 1_609_545_600_000L
        )
        viewModel.updateGenre("드라마")
        viewModel.goToNextStep()
        viewModel.goToNextStep()

        assertEquals(RegisterMovieStep.TicketTemplate, viewModel.uiState.value.step)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun futureReleaseDateIsRejected() {
        val viewModel = RegisterMovieViewModel()

        viewModel.updateReleaseDateMillis(
            releaseDateMillis = 1_609_545_600_000L,
            nowMillis = 1_609_459_200_000L
        )

        assertEquals(null, viewModel.uiState.value.releaseDateMillis)
        assertEquals("미래 날짜는 선택할 수 없어요.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun publishRequiresTemplateSelection() {
        val viewModel = RegisterMovieViewModel()

        viewModel.updateTitle("괴물")
        viewModel.updateReleaseDateMillis(
            releaseDateMillis = 1_609_459_200_000L,
            nowMillis = 1_609_545_600_000L
        )
        viewModel.updateGenre("드라마")
        viewModel.goToNextStep()
        viewModel.goToNextStep()
        viewModel.goToNextStep(nowMillis = 1_609_545_600_000L)

        assertEquals(RegisterMovieStep.TicketTemplate, viewModel.uiState.value.step)
        assertEquals("티켓 디자인을 선택해 주세요.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun selectingTemplateAndPublishingStoresStartTime() {
        val viewModel = RegisterMovieViewModel()
        val now = 1_609_545_600_000L

        viewModel.updateTitle("괴물")
        viewModel.updateReleaseDateMillis(1_609_459_200_000L, nowMillis = now)
        viewModel.updateGenre("드라마")
        viewModel.goToNextStep()
        viewModel.goToNextStep()
        viewModel.selectTicketTemplate(TicketTemplateOption.Classic)
        viewModel.goToNextStep(nowMillis = now)

        val state = viewModel.uiState.value
        assertEquals(RegisterMovieStep.Publishing, state.step)
        assertEquals(PublishingStatus.Publishing, state.publishingStatus)
        assertEquals(now, state.publishStartedAtMillis)
        assertFalse(state.isPublishComplete)
    }

    @Test
    fun publishingProgressIsCalculatedFromStartTime() {
        val startedAt = 1_609_545_600_000L

        assertEquals(0, publishingProgressPercent(startedAt, startedAt))
        assertEquals(50, publishingProgressPercent(startedAt, startedAt + 1_000L))
        assertEquals(100, publishingProgressPercent(startedAt, startedAt + 2_000L))
        assertEquals(100, publishingProgressPercent(startedAt, startedAt + 3_000L))
    }

    @Test
    fun markPublishingCompleteSetsCompleteStatus() {
        val viewModel = RegisterMovieViewModel()
        val now = 1_609_545_600_000L

        viewModel.updateTitle("괴물")
        viewModel.updateReleaseDateMillis(1_609_459_200_000L, nowMillis = now)
        viewModel.updateGenre("드라마")
        viewModel.goToNextStep()
        viewModel.goToNextStep()
        viewModel.selectTicketTemplate(TicketTemplateOption.Poster)
        viewModel.goToNextStep(nowMillis = now)
        viewModel.markPublishingComplete(nowMillis = now + 2_000L)

        val state = viewModel.uiState.value
        assertEquals(PublishingStatus.Complete, state.publishingStatus)
        assertEquals(now + 2_000L, state.publishCompletedAtMillis)
        assertTrue(state.isPublishComplete)
    }

    @Test
    fun resetReturnsToInitialSearchStep() {
        val viewModel = RegisterMovieViewModel()

        viewModel.updateSearchQuery("괴물")
        viewModel.updateTitle("괴물")
        viewModel.selectTicketTemplate(TicketTemplateOption.Minimal)
        viewModel.reset()

        assertEquals(RegisterMovieUiState(), viewModel.uiState.value)
    }
}
