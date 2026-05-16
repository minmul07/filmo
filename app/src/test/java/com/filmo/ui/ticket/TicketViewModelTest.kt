package com.filmo.ui.ticket

import com.filmo.service.AppRepository
import com.filmo.service.MovieCatalogItem
import com.filmo.service.MovieDetail
import com.filmo.service.PublicTicket
import com.filmo.service.TicketCollection
import com.filmo.service.UpdateTicketRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TicketViewModelTest {
    @Test
    fun loadPublicTicketsStoresRepositoryTickets() = runBlocking {
        val viewModel = TicketViewModel(FakeTicketViewRepository())

        viewModel.loadPublicTickets()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("테스트 영화"), state.tickets.map { it.movieTitle })
        assertNull(state.errorMessage)
    }

    @Test
    fun loadPublicTicketsShowsRecoverableErrorWhenRepositoryFails() = runBlocking {
        val viewModel = TicketViewModel(
            FakeTicketViewRepository(
                publicTicketsResult = Result.failure(IllegalStateException("boom"))
            )
        )

        viewModel.loadPublicTickets()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.tickets.isEmpty())
        assertEquals("공개 티켓을 불러오지 못했어요. 다시 시도해 주세요.", state.errorMessage)
    }

    private class FakeTicketViewRepository(
        private val publicTicketsResult: Result<List<PublicTicket>> = Result.success(
            listOf(
                PublicTicket(
                    id = "public-1",
                    movieSeq = "1001",
                    movieTitle = "테스트 영화",
                    genre = "드라마",
                    director = "테스트 감독",
                    releaseYear = 2024,
                    duration = "100분",
                    posterImagePath = "poster.jpg",
                    theaterName = "인디스페이스",
                    watchedDate = "2026-05-16",
                    watchedTime = "19:30",
                    review = "작고 단단한 영화였어요"
                )
            )
        )
    ) : AppRepository {
        override suspend fun ping(): Result<String> = Result.success("pong")

        override suspend fun fetchMovieCatalog(
            keyword: String?,
            page: Int,
            size: Int
        ): Result<List<MovieCatalogItem>> = Result.success(emptyList())

        override suspend fun fetchMovieDetail(movieId: String): Result<MovieDetail> {
            return Result.failure(UnsupportedOperationException("Not needed in this test"))
        }

        override suspend fun fetchTicketCollection(): Result<TicketCollection> {
            return Result.success(TicketCollection(myTickets = emptyList(), savedTickets = emptyList()))
        }

        override suspend fun fetchPublicTickets(sort: String): Result<List<PublicTicket>> {
            return publicTicketsResult
        }

        override suspend fun updateMyTicket(request: UpdateTicketRequest) =
            Result.failure<com.filmo.service.MovieTicket>(UnsupportedOperationException("Not needed in this test"))

        override suspend fun deleteMyTicket(ticketId: String): Result<Unit> = Result.success(Unit)

        override suspend fun removeSavedTicket(ticketId: String): Result<Unit> = Result.success(Unit)
    }
}
