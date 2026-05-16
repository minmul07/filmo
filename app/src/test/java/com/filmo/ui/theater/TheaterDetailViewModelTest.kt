package com.filmo.ui.theater

import com.filmo.service.AppRepository
import com.filmo.service.MovieCatalogItem
import com.filmo.service.MovieDetail
import com.filmo.service.MovieTicket
import com.filmo.service.SampleItem
import com.filmo.service.SampleItemRequest
import com.filmo.service.Theater
import com.filmo.service.TheaterBookmarkStore
import com.filmo.service.TheaterPage
import com.filmo.service.TicketCollection
import com.filmo.service.UpdateTicketRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TheaterDetailViewModelTest {
    @Test
    fun loadTheaterFetchesDetail() = runBlocking {
        val repository = FakeTheaterRepository()
        val viewModel = TheaterDetailViewModel(repository, FakeTheaterBookmarkStore())

        viewModel.loadTheater("artnine")

        val state = viewModel.uiState.value
        assertEquals("artnine", repository.requestedTheaterIds.single())
        assertEquals("아트나인", state.theater?.name)
        assertFalse(state.isLoading)
    }

    @Test
    fun saveAndRemoveSavedTheaterUpdateSavedState() = runBlocking {
        val bookmarkStore = FakeTheaterBookmarkStore()
        val viewModel = TheaterDetailViewModel(FakeTheaterRepository(), bookmarkStore)

        viewModel.saveTheater("artnine")
        assertTrue(viewModel.uiState.value.isSaved)
        assertTrue("artnine" in bookmarkStore.currentSavedTheaterIds)

        viewModel.removeSavedTheater("artnine")
        assertFalse(viewModel.uiState.value.isSaved)
        assertFalse("artnine" in bookmarkStore.currentSavedTheaterIds)
    }

    @Test
    fun loadTheaterRestoresSavedStateFromBookmarkStore() = runBlocking {
        val bookmarkStore = FakeTheaterBookmarkStore(initialSavedTheaterIds = setOf("artnine"))
        val viewModel = TheaterDetailViewModel(FakeTheaterRepository(), bookmarkStore)

        viewModel.loadTheater("artnine", initiallySaved = false)

        assertTrue(viewModel.uiState.value.isSaved)
    }

    private class FakeTheaterRepository : AppRepository {
        val requestedTheaterIds = mutableListOf<String>()

        override suspend fun ping(): Result<String> = Result.success("pong")

        override suspend fun fetchItems(): Result<List<SampleItem>> = Result.success(emptyList())

        override suspend fun submitItem(request: SampleItemRequest): Result<SampleItem> {
            return Result.success(SampleItem("created", request.title, request.description))
        }

        override suspend fun fetchMovieCatalog(
            keyword: String?,
            genre: String?,
            year: String?,
            page: Int,
            size: Int
        ): Result<List<MovieCatalogItem>> {
            return Result.success(emptyList())
        }

        override suspend fun fetchMovieDetail(movieId: String): Result<MovieDetail> {
            return Result.failure(UnsupportedOperationException("Not needed in this test"))
        }

        override suspend fun fetchTheaters(keyword: String?, page: Int, size: Int): Result<List<Theater>> {
            return Result.success(emptyList())
        }

        override suspend fun fetchTheaterPage(keyword: String?, page: Int, size: Int): Result<TheaterPage> {
            return Result.success(TheaterPage(emptyList(), hasMore = false))
        }

        override suspend fun fetchTheater(theaterId: String): Result<Theater> {
            requestedTheaterIds += theaterId
            return Result.success(FakeTheater)
        }

        override suspend fun saveTheater(theaterId: String): Result<Unit> = Result.success(Unit)

        override suspend fun removeSavedTheater(theaterId: String): Result<Unit> = Result.success(Unit)

        override suspend fun fetchTicketCollection(): Result<TicketCollection> {
            return Result.success(TicketCollection(emptyList(), emptyList()))
        }

        override suspend fun updateMyTicket(request: UpdateTicketRequest): Result<MovieTicket> {
            return Result.failure(UnsupportedOperationException("Not needed in this test"))
        }

        override suspend fun deleteMyTicket(ticketId: String): Result<Unit> = Result.success(Unit)

        override suspend fun removeSavedTicket(ticketId: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeTheaterBookmarkStore(
        initialSavedTheaterIds: Set<String> = emptySet()
    ) : TheaterBookmarkStore {
        private val savedTheaterIdsState = MutableStateFlow(initialSavedTheaterIds)
        override val savedTheaterIds: Flow<Set<String>> = savedTheaterIdsState
        val currentSavedTheaterIds: Set<String>
            get() = savedTheaterIdsState.value

        override suspend fun saveTheaterId(theaterId: String) {
            savedTheaterIdsState.value = savedTheaterIdsState.value + theaterId
        }

        override suspend fun removeTheaterId(theaterId: String) {
            savedTheaterIdsState.value = savedTheaterIdsState.value - theaterId
        }
    }

    private companion object {
        val FakeTheater = Theater(
            id = "artnine",
            name = "아트나인",
            screenName = "0관",
            screenType = "예술영화관",
            address = "사당동 147-53",
            phone = "02-536-0058",
            homepage = "https://www.artnine.co.kr",
            seatCount = 92,
            naverMapUrl = "https://map.naver.com"
        )
    }
}
