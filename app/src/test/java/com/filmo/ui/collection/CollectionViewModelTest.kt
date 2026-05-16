package com.filmo.ui.collection

import com.filmo.service.AppRepository
import com.filmo.service.MovieCatalogItem
import com.filmo.service.MovieDetail
import com.filmo.service.MovieTicket
import com.filmo.service.TicketCollection
import com.filmo.service.UpdateTicketRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

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

    private class FakeCollectionRepository : AppRepository {
        private val myTickets = mutableListOf(
            MovieTicket(
                id = "my-1",
                movieTitle = "과속스캔들",
                theaterName = "서울아트시네마",
                watchedDate = "2024-03-20",
                rating = 4,
                review = "유쾌하고 따뜻한 영화였어요",
                ownedByMe = true,
                savedByMe = false
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
                savedByMe = true
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

        override suspend fun deleteMyTicket(ticketId: String): Result<Unit> {
            myTickets.removeAll { it.id == ticketId }
            return Result.success(Unit)
        }

        override suspend fun removeSavedTicket(ticketId: String): Result<Unit> {
            savedTickets.removeAll { it.id == ticketId }
            return Result.success(Unit)
        }
    }
}
