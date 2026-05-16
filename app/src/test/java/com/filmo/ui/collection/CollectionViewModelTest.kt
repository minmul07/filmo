package com.filmo.ui.collection

import com.filmo.service.AppRepository
import com.filmo.service.MovieCatalogItem
import com.filmo.service.MovieDetail
import com.filmo.service.MovieTicket
import com.filmo.service.TicketCollection
import com.filmo.service.UpdateTicketRequest
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class CollectionViewModelTest {
    @Test
    fun loadTicketsStoresMyAndSavedTickets() = runBlocking {
        val viewModel = CollectionViewModel(FakeCollectionRepository())

        viewModel.loadTickets()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("과속스캔들"), state.myTickets.map { it.movieTitle })
        assertEquals(listOf("헤어질 결심"), state.savedTickets.map { it.movieTitle })
        assertNull(state.errorMessage)
    }

    @Test
    fun selectedTabControlsVisibleTickets() = runBlocking {
        val viewModel = CollectionViewModel(FakeCollectionRepository())

        viewModel.loadTickets()
        viewModel.selectTab(CollectionTicketTab.SavedTickets)

        assertEquals(CollectionTicketTab.SavedTickets, viewModel.uiState.value.selectedTab)
        assertEquals(listOf("헤어질 결심"), viewModel.uiState.value.visibleTickets.map { it.movieTitle })
    }

    @Test
    fun confirmingDeleteOnMyTicketRemovesItFromMyCollection() = runBlocking {
        val viewModel = CollectionViewModel(FakeCollectionRepository())

        viewModel.loadTickets()
        viewModel.requestDeleteTicket("my-1")
        viewModel.confirmDeleteTicket()

        val state = viewModel.uiState.value
        assertTrue(state.myTickets.isEmpty())
        assertNull(state.pendingDeleteTicket)
        assertNull(state.errorMessage)
    }

    @Test
    fun confirmingDeleteOnSavedTicketRemovesItFromSavedCollection() = runBlocking {
        val viewModel = CollectionViewModel(FakeCollectionRepository())

        viewModel.loadTickets()
        viewModel.selectTab(CollectionTicketTab.SavedTickets)
        viewModel.requestDeleteTicket("saved-1")
        viewModel.confirmDeleteTicket()

        val state = viewModel.uiState.value
        assertTrue(state.savedTickets.isEmpty())
        assertNull(state.pendingDeleteTicket)
    }

    @Test
    fun saveEditedTicketUpdatesMyTicket() = runBlocking {
        val viewModel = CollectionViewModel(FakeCollectionRepository())

        viewModel.loadTickets()
        viewModel.startEditingTicket("my-1")
        viewModel.updateEditingWatchedDate("2024-03-21")
        viewModel.updateEditingRating(5)
        viewModel.updateEditingReview("다시 봐도 따뜻했다.")
        viewModel.saveEditedTicket()

        val ticket = viewModel.uiState.value.myTickets.single()
        assertEquals("서울아트시네마", ticket.theaterName)
        assertEquals("2024-03-21", ticket.watchedDate)
        assertEquals(5, ticket.rating)
        assertEquals("다시 봐도 따뜻했다.", ticket.review)
        assertNull(viewModel.uiState.value.editingTicket)
    }

    @Test
    fun toggleTicketLikeUpdatesCollectionTicketAndCallsRepository() = runBlocking {
        val repository = FakeCollectionRepository()
        val viewModel = CollectionViewModel(repository)

        viewModel.loadTickets()
        viewModel.toggleTicketLike("my-1")

        val ticket = viewModel.uiState.value.myTickets.single()
        assertTrue(ticket.liked)
        assertEquals(1, ticket.likeCount)
        assertEquals(listOf("my-1" to true), repository.likeRequests)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun toggleTicketLikeRollsBackCollectionTicketWhenRepositoryFails() = runBlocking {
        val repository = FakeCollectionRepository(
            setTicketLikedResult = Result.failure(IllegalStateException("boom"))
        )
        val viewModel = CollectionViewModel(repository)

        viewModel.loadTickets()
        viewModel.toggleTicketLike("my-1")

        val ticket = viewModel.uiState.value.myTickets.single()
        assertFalse(ticket.liked)
        assertEquals(0, ticket.likeCount)
        assertEquals("좋아요를 변경하지 못했어요. 다시 시도해 주세요.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun toggleTicketLikeUsesToastMessageForOwnPostWhenRepositoryReturnsBadRequest() = runBlocking {
        val repository = FakeCollectionRepository(
            setTicketLikedResult = Result.failure(httpException(400))
        )
        val viewModel = CollectionViewModel(repository)

        viewModel.loadTickets()
        viewModel.toggleTicketLike("my-1")

        val ticket = viewModel.uiState.value.myTickets.single()
        assertFalse(ticket.liked)
        assertEquals(0, ticket.likeCount)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals("나의 게시물에는 좋아요를 누를 수 없습니다.", viewModel.uiState.value.toastMessage)
    }

    @Test
    fun loadTicketDetailRefreshesLikedStateBeforeToggle() = runBlocking {
        val repository = FakeCollectionRepository(
            detailTicketResult = Result.success(
                MovieTicket(
                    id = "my-1",
                    movieTitle = "과속스캔들",
                    theaterName = "서울아트시네마",
                    watchedDate = "2024-03-20",
                    rating = 4,
                    review = "유쾌하고 따뜻한 영화였어요",
                    ownedByMe = true,
                    savedByMe = false,
                    liked = true,
                    likeCount = 3
                )
            )
        )
        val viewModel = CollectionViewModel(repository)

        viewModel.loadTickets()
        viewModel.loadTicketDetail("my-1")
        viewModel.toggleTicketLike("my-1")

        val ticket = viewModel.uiState.value.myTickets.single()
        assertFalse(ticket.liked)
        assertEquals(2, ticket.likeCount)
        assertEquals(listOf("my-1" to false), repository.likeRequests)
        assertEquals(listOf("my-1" to true), repository.detailRequests)
    }

    private class FakeCollectionRepository(
        private val setTicketLikedResult: Result<Unit> = Result.success(Unit),
        private val detailTicketResult: Result<MovieTicket>? = null
    ) : AppRepository {
        val likeRequests = mutableListOf<Pair<String, Boolean>>()
        val detailRequests = mutableListOf<Pair<String, Boolean>>()

        private val myTickets = mutableListOf(
            MovieTicket(
                id = "my-1",
                movieTitle = "과속스캔들",
                theaterName = "서울아트시네마",
                watchedDate = "2024-03-20",
                rating = 4,
                review = "유쾌하고 따뜻한 영화였어요",
                ownedByMe = true,
                savedByMe = false,
                liked = false,
                likeCount = 0
            )
        )
        private val savedTickets = mutableListOf(
            MovieTicket(
                id = "saved-1",
                movieTitle = "헤어질 결심",
                theaterName = "인디스페이스",
                watchedDate = "2024-03-15",
                rating = 5,
                review = "영상미가 압도적이었습니다",
                ownedByMe = false,
                savedByMe = true,
                liked = false,
                likeCount = 0
            )
        )

        override suspend fun ping(): Result<String> = Result.success("pong")

        override suspend fun fetchMovieCatalog(
            keyword: String?,
            page: Int,
            size: Int
        ): Result<List<MovieCatalogItem>> {
            return Result.success(emptyList())
        }

        override suspend fun fetchMovieDetail(movieId: String): Result<MovieDetail> {
            return Result.failure(UnsupportedOperationException("Not needed in this test"))
        }

        override suspend fun fetchTicketCollection(): Result<TicketCollection> {
            return Result.success(
                TicketCollection(
                    myTickets = myTickets.toList(),
                    savedTickets = savedTickets.toList()
                )
            )
        }

        override suspend fun fetchTicketDetail(ticketId: String, ownedByMe: Boolean): Result<MovieTicket> {
            detailRequests += ticketId to ownedByMe
            return detailTicketResult ?: Result.failure(UnsupportedOperationException("Not needed in this test"))
        }

        override suspend fun updateMyTicket(request: UpdateTicketRequest): Result<MovieTicket> {
            val index = myTickets.indexOfFirst { it.id == request.ticketId }
            val updated = myTickets[index].copy(
                watchedDate = request.watchedDate,
                rating = request.rating,
                review = request.review
            )
            myTickets[index] = updated
            return Result.success(updated)
        }

        override suspend fun setTicketLiked(ticketId: String, liked: Boolean): Result<Unit> {
            likeRequests += ticketId to liked
            return setTicketLikedResult
        }

        override suspend fun deleteMyTicket(ticketId: String): Result<Unit> {
            myTickets.removeAll { it.id == ticketId }
            return Result.success(Unit)
        }

        override suspend fun removeSavedTicket(ticketId: String): Result<Unit> {
            savedTickets.removeAll { it.id == ticketId }
            return Result.success(Unit)
        }
    }

    private companion object {
        fun httpException(code: Int): HttpException {
            val body = """{"message":"bad request"}"""
                .toResponseBody("application/json".toMediaType())
            return HttpException(Response.error<Unit>(code, body))
        }
    }
}
