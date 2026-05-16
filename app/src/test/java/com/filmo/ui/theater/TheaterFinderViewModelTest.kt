package com.filmo.ui.theater

import com.filmo.service.AppRepository
import com.filmo.service.MovieCatalogItem
import com.filmo.service.MovieDetail
import com.filmo.service.MovieTicket
import com.filmo.service.SampleItem
import com.filmo.service.SampleItemRequest
import com.filmo.service.Theater
import com.filmo.service.TheaterPage
import com.filmo.service.TicketCollection
import com.filmo.service.UpdateTicketRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TheaterFinderViewModelTest {
    @Test
    fun loadTheatersRequestsZeroBasedFirstPage() = runBlocking {
        val repository = FakeTheaterRepository()
        val viewModel = TheaterFinderViewModel(repository)

        viewModel.loadTheaters()

        assertEquals(listOf(0), repository.requestedPages)
        assertEquals(listOf(20), repository.requestedSizes)
        assertEquals(listOf("아트나인", "인디스페이스"), viewModel.uiState.value.theaters.map { it.name })
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun loadNextTheaterPageAppendsPage() = runBlocking {
        val repository = FakeTheaterRepository(
            pages = mapOf(
                0 to FakeTheaters.take(1),
                1 to FakeTheaters.drop(1)
            ),
            hasMoreByPage = mapOf(
                0 to true,
                1 to false
            )
        )
        val viewModel = TheaterFinderViewModel(repository)

        viewModel.loadTheaters()
        viewModel.loadNextTheaterPage()

        val state = viewModel.uiState.value
        assertEquals(listOf(0, 1), repository.requestedPages)
        assertEquals(listOf("아트나인", "인디스페이스"), state.theaters.map { it.name })
        assertFalse(state.canLoadMore)
        assertFalse(state.isAppending)
    }

    @Test
    fun loadNextTheaterPageDoesNotRequestWhenLastPageLoaded() = runBlocking {
        val repository = FakeTheaterRepository(
            pages = mapOf(0 to FakeTheaters),
            hasMoreByPage = mapOf(0 to false)
        )
        val viewModel = TheaterFinderViewModel(repository)

        viewModel.loadTheaters()
        viewModel.loadNextTheaterPage()

        assertEquals(listOf(0), repository.requestedPages)
        assertFalse(viewModel.uiState.value.canLoadMore)
    }

    @Test
    fun toggleSavedUpdatesSavedTheaterIds() {
        val viewModel = TheaterFinderViewModel(FakeTheaterRepository())

        viewModel.toggleSaved("artnine")
        assertTrue("artnine" in viewModel.uiState.value.savedTheaterIds)

        viewModel.toggleSaved("artnine")
        assertFalse("artnine" in viewModel.uiState.value.savedTheaterIds)
    }

    private class FakeTheaterRepository(
        private val pages: Map<Int, List<Theater>> = mapOf(0 to FakeTheaters),
        private val hasMoreByPage: Map<Int, Boolean> = mapOf(0 to false)
    ) : AppRepository {
        val requestedPages = mutableListOf<Int>()
        val requestedSizes = mutableListOf<Int>()

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

        override suspend fun fetchTheaters(
            keyword: String?,
            page: Int,
            size: Int
        ): Result<List<Theater>> {
            return Result.success(pages[page].orEmpty())
        }

        override suspend fun fetchTheaterPage(
            keyword: String?,
            page: Int,
            size: Int
        ): Result<TheaterPage> {
            requestedPages += page
            requestedSizes += size
            return Result.success(
                TheaterPage(
                    items = pages[page].orEmpty(),
                    hasMore = hasMoreByPage[page] ?: false
                )
            )
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

        override suspend fun deleteMyTicket(ticketId: String): Result<Unit> = Result.success(Unit)

        override suspend fun removeSavedTicket(ticketId: String): Result<Unit> = Result.success(Unit)
    }

    private companion object {
        val FakeTheaters = listOf(
            Theater(
                id = "artnine",
                name = "아트나인",
                screenName = "0관",
                screenType = "예술영화관",
                address = "사당동 147-53",
                phone = "02-536-0058",
                homepage = "https://www.artnine.co.kr",
                seatCount = 92,
                naverMapUrl = "https://map.naver.com"
            ),
            Theater(
                id = "indiespace",
                name = "인디스페이스",
                screenName = "1관",
                screenType = "독립영화전용관",
                address = "서울특별시 마포구 양화로 176",
                phone = "02-738-0366",
                homepage = "https://indiespace.kr",
                seatCount = 210,
                naverMapUrl = "https://map.naver.com"
            )
        )
    }
}
