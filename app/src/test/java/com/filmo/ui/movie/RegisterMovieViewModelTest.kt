package com.filmo.ui.movie

import com.filmo.service.AppRepository
import com.filmo.service.MovieCatalogItem
import com.filmo.service.MovieTicket
import com.filmo.service.SampleItem
import com.filmo.service.SampleItemRequest
import com.filmo.service.TicketCollection
import com.filmo.service.UpdateTicketRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterMovieViewModelTest {
    @Test
    fun loadMovieCatalogStoresRepositoryMovies() = runBlocking {
        val viewModel = RegisterMovieViewModel(FakeAppRepository())

        viewModel.loadMovieCatalog()

        val state = viewModel.uiState.value
        assertEquals(listOf("헤어질 결심", "윤희에게", "벌새", "소공녀"), state.movies.map { it.title })
        assertEquals(false, state.isMovieCatalogLoading)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun selectingMovieMovesToViewingInfoStepAndStoresMovie() {
        val viewModel = RegisterMovieViewModel(FakeAppRepository())

        viewModel.selectMovie(FakeMovies.first())

        val state = viewModel.uiState.value
        assertEquals(RegisterMovieStep.MovieInfo, state.step)
        assertEquals("헤어질 결심", state.title)
        assertEquals("박찬욱", state.director)
        assertEquals("로맨스/드라마", state.genre)
    }

    @Test
    fun updateFieldsReflectsViewingInputState() {
        val viewModel = RegisterMovieViewModel(FakeAppRepository())

        viewModel.selectMovie(FakeMovies.first())
        viewModel.updateTheaterName("아트나인")
        viewModel.updateReleaseDateMillis(1_609_459_200_000L)
        viewModel.updateRating(5)
        viewModel.updateReview("영화의 여운이 길게 남았다.")

        val state = viewModel.uiState.value
        assertEquals("아트나인", state.theaterName)
        assertEquals(1_609_459_200_000L, state.releaseDateMillis)
        assertEquals(5, state.rating)
        assertEquals("영화의 여운이 길게 남았다.", state.review)
    }

    @Test
    fun nextFromInfoInputRequiresViewingInfo() {
        val viewModel = RegisterMovieViewModel(FakeAppRepository())

        viewModel.selectMovie(FakeMovies.first())
        viewModel.goToNextStep()

        assertEquals(RegisterMovieStep.MovieInfo, viewModel.uiState.value.step)
        assertEquals("영화관, 관람일, 별점, 관람 후기를 입력해 주세요.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun viewingInfoIsRequiredForTicketTemplateStep() {
        val viewModel = RegisterMovieViewModel(FakeAppRepository())

        viewModel.selectMovie(FakeMovies.first())
        viewModel.updateTheaterName("아트나인")
        viewModel.updateReleaseDateMillis(
            releaseDateMillis = 1_609_459_200_000L,
            nowMillis = 1_609_545_600_000L
        )
        viewModel.updateRating(4)
        viewModel.updateReview("다시 곱씹게 되는 영화.")
        viewModel.goToNextStep()

        assertEquals(RegisterMovieStep.TicketTemplate, viewModel.uiState.value.step)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun futureReleaseDateIsRejected() {
        val viewModel = RegisterMovieViewModel(FakeAppRepository())

        viewModel.updateReleaseDateMillis(
            releaseDateMillis = 1_609_545_600_000L,
            nowMillis = 1_609_459_200_000L
        )

        assertEquals(null, viewModel.uiState.value.releaseDateMillis)
        assertEquals("미래 날짜는 선택할 수 없어요.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun publishRequiresTemplateSelection() {
        val viewModel = RegisterMovieViewModel(FakeAppRepository())

        viewModel.selectMovie(FakeMovies.first())
        viewModel.updateTheaterName("아트나인")
        viewModel.updateReleaseDateMillis(
            releaseDateMillis = 1_609_459_200_000L,
            nowMillis = 1_609_545_600_000L
        )
        viewModel.updateRating(5)
        viewModel.updateReview("좋았다.")
        viewModel.goToNextStep()
        viewModel.goToNextStep(nowMillis = 1_609_545_600_000L)

        assertEquals(RegisterMovieStep.TicketTemplate, viewModel.uiState.value.step)
        assertEquals("티켓 디자인을 선택해 주세요.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun selectingTemplateAndPublishingStoresStartTime() {
        val viewModel = RegisterMovieViewModel(FakeAppRepository())
        val now = 1_609_545_600_000L

        viewModel.selectMovie(FakeMovies.first())
        viewModel.updateTheaterName("아트나인")
        viewModel.updateReleaseDateMillis(1_609_459_200_000L, nowMillis = now)
        viewModel.updateRating(5)
        viewModel.updateReview("좋았다.")
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
        val viewModel = RegisterMovieViewModel(FakeAppRepository())
        val now = 1_609_545_600_000L

        viewModel.selectMovie(FakeMovies.first())
        viewModel.updateTheaterName("아트나인")
        viewModel.updateReleaseDateMillis(1_609_459_200_000L, nowMillis = now)
        viewModel.updateRating(5)
        viewModel.updateReview("좋았다.")
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
        val viewModel = RegisterMovieViewModel(FakeAppRepository())

        viewModel.updateSearchQuery("괴물")
        viewModel.updateTitle("괴물")
        viewModel.selectTicketTemplate(TicketTemplateOption.Minimal)
        viewModel.reset()

        assertEquals(RegisterMovieUiState(), viewModel.uiState.value)
    }

    private class FakeAppRepository : AppRepository {
        override suspend fun ping(): Result<String> = Result.success("pong")

        override suspend fun fetchItems(): Result<List<SampleItem>> = Result.success(emptyList())

        override suspend fun submitItem(request: SampleItemRequest): Result<SampleItem> {
            return Result.success(
                SampleItem(
                    id = "created",
                    title = request.title,
                    description = request.description
                )
            )
        }

        override suspend fun fetchMovieCatalog(): Result<List<MovieCatalogItem>> {
            return Result.success(FakeMovies)
        }

        override suspend fun fetchTicketCollection(): Result<TicketCollection> {
            return Result.success(TicketCollection(emptyList(), emptyList()))
        }

        override suspend fun updateMyTicket(request: UpdateTicketRequest): Result<MovieTicket> {
            return Result.failure(UnsupportedOperationException("Not needed in this test"))
        }

        override suspend fun deleteMyTicket(ticketId: String): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun removeSavedTicket(ticketId: String): Result<Unit> {
            return Result.success(Unit)
        }
    }

    private companion object {
        val FakeMovies = listOf(
            MovieCatalogItem("movie-1", "헤어질 결심", 2022, "박찬욱", "로맨스/드라마"),
            MovieCatalogItem("movie-2", "윤희에게", 2019, "임대형", "드라마"),
            MovieCatalogItem("movie-3", "벌새", 2018, "김보라", "드라마"),
            MovieCatalogItem("movie-4", "소공녀", 2017, "전고운", "드라마")
        )
    }
}
