package com.filmo.ui.movie

import com.filmo.service.AppRepository
import com.filmo.service.CreateTicketRequest
import com.filmo.service.MovieCatalogItem
import com.filmo.service.MovieCatalogPage
import com.filmo.service.MovieDetail
import com.filmo.service.MovieTicket
import com.filmo.service.SampleItem
import com.filmo.service.SampleItemRequest
import com.filmo.service.Theater
import com.filmo.service.TicketCollection
import com.filmo.service.UpdateTicketRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun loadMovieCatalogRequestsFirstPageWithTwentyItems() = runBlocking {
        val repository = FakeAppRepository()
        val viewModel = RegisterMovieViewModel(repository)

        viewModel.loadMovieCatalog()

        assertEquals(listOf(1), repository.requestedMoviePages)
        assertEquals(listOf(20), repository.requestedMovieSizes)
    }

    @Test
    fun loadNextMovieCatalogPageAppendsRepositoryMovies() = runBlocking {
        val repository = FakeAppRepository(
            moviePages = mapOf(
                1 to FakeMovies.take(2),
                2 to FakeMovies.drop(2)
            ),
            hasMoreByPage = mapOf(
                1 to true,
                2 to false
            )
        )
        val viewModel = RegisterMovieViewModel(repository)

        viewModel.loadMovieCatalog()
        viewModel.loadNextMovieCatalogPage()

        val state = viewModel.uiState.value
        assertEquals(listOf(1, 2), repository.requestedMoviePages)
        assertEquals(listOf("헤어질 결심", "윤희에게", "벌새", "소공녀"), state.movies.map { it.title })
        assertFalse(state.isMovieCatalogLoading)
        assertFalse(state.isMovieCatalogAppendLoading)
        assertFalse(state.canLoadMoreMovies)
    }

    @Test
    fun loadNextMovieCatalogPageDoesNotRequestWhenLastPageLoaded() = runBlocking {
        val repository = FakeAppRepository(
            moviePages = mapOf(1 to FakeMovies),
            hasMoreByPage = mapOf(1 to false)
        )
        val viewModel = RegisterMovieViewModel(repository)

        viewModel.loadMovieCatalog()
        viewModel.loadNextMovieCatalogPage()

        assertEquals(listOf(1), repository.requestedMoviePages)
        assertFalse(viewModel.uiState.value.canLoadMoreMovies)
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
    fun viewingInfoMovesDirectlyToShareStep() {
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

        assertEquals(RegisterMovieStep.Share, viewModel.uiState.value.step)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun backFromViewingInfoReturnsToMovieSearch() {
        val viewModel = RegisterMovieViewModel(FakeAppRepository())

        viewModel.selectMovie(FakeMovies.first())

        assertEquals(true, viewModel.goBack())
        assertEquals(RegisterMovieStep.MovieSearch, viewModel.uiState.value.step)
    }

    @Test
    fun backFromShareReturnsToViewingInfo() {
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

        assertEquals(true, viewModel.goBack())
        assertEquals(RegisterMovieStep.MovieInfo, viewModel.uiState.value.step)
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
    fun shareStepDoesNotRequireTemplateSelection() {
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
        viewModel.goToNextStep()

        val state = viewModel.uiState.value
        assertEquals(RegisterMovieStep.Share, state.step)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun createTicketFromShareStepSendsTicketRequestToRepository() = runBlocking {
        val repository = FakeAppRepository()
        val viewModel = RegisterMovieViewModel(repository)

        viewModel.selectMovie(FakeMovies.first().copy(id = "1001"))
        viewModel.updateTheaterName("인디스페이스")
        viewModel.updateReleaseDateMillis(
            releaseDateMillis = 1_778_889_600_000L,
            nowMillis = 1_778_976_000_000L
        )
        viewModel.updateRating(5)
        viewModel.updateReview("작고 단단한 영화였어요")
        viewModel.goToNextStep()

        val result = viewModel.createTicket()

        assertEquals(true, result)
        assertEquals(
            CreateTicketRequest(
                movieId = "1001",
                watchedDate = "2026-05-16",
                watchedTime = "00:00",
                cinema = "인디스페이스",
                review = "작고 단단한 영화였어요"
            ),
            repository.createdTicketRequests.single()
        )
        assertEquals(false, viewModel.uiState.value.isTicketCreateLoading)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun resetReturnsToInitialSearchStep() {
        val viewModel = RegisterMovieViewModel(FakeAppRepository())

        viewModel.updateSearchQuery("괴물")
        viewModel.updateTitle("괴물")
        viewModel.reset()

        assertEquals(RegisterMovieUiState(), viewModel.uiState.value)
    }

    private class FakeAppRepository(
        private val moviePages: Map<Int, List<MovieCatalogItem>> = mapOf(1 to FakeMovies),
        private val hasMoreByPage: Map<Int, Boolean> = mapOf(1 to false)
    ) : AppRepository {
        val requestedMoviePages = mutableListOf<Int>()
        val requestedMovieSizes = mutableListOf<Int>()
        val createdTicketRequests = mutableListOf<CreateTicketRequest>()

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

        override suspend fun fetchMovieCatalog(
            keyword: String?,
            genre: String?,
            year: String?,
            page: Int,
            size: Int
        ): Result<List<MovieCatalogItem>> {
            requestedMoviePages += page
            requestedMovieSizes += size
            return Result.success(moviePages[page].orEmpty())
        }

        override suspend fun fetchMovieCatalogPage(
            keyword: String?,
            genre: String?,
            year: String?,
            page: Int,
            size: Int
        ): Result<MovieCatalogPage> {
            requestedMoviePages += page
            requestedMovieSizes += size
            return Result.success(
                MovieCatalogPage(
                    items = moviePages[page].orEmpty(),
                    hasMore = hasMoreByPage[page] ?: false
                )
            )
        }

        override suspend fun fetchMovieDetail(movieId: String): Result<MovieDetail> {
            return Result.failure(UnsupportedOperationException("Not needed in this test"))
        }

        override suspend fun fetchTheaters(
            keyword: String?,
            page: Int,
            size: Int
        ): Result<List<Theater>> {
            return Result.success(emptyList())
        }

        override suspend fun fetchTheater(theaterId: String): Result<Theater> {
            return Result.failure(UnsupportedOperationException("Not needed in this test"))
        }

        override suspend fun fetchTicketCollection(): Result<TicketCollection> {
            return Result.success(TicketCollection(emptyList(), emptyList()))
        }

        override suspend fun updateMyTicket(request: UpdateTicketRequest): Result<MovieTicket> {
            return Result.failure(UnsupportedOperationException("Not needed in this test"))
        }

        override suspend fun createTicket(request: CreateTicketRequest): Result<Unit> {
            createdTicketRequests += request
            return Result.success(Unit)
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
