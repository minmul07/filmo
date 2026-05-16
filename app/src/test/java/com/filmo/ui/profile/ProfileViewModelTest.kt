package com.filmo.ui.profile

import com.filmo.service.AppRepository
import com.filmo.service.MovieCatalogItem
import com.filmo.service.MovieDetail
import com.filmo.service.MovieTicket
import com.filmo.service.SampleItem
import com.filmo.service.SampleItemRequest
import com.filmo.service.Theater
import com.filmo.service.TicketCollection
import com.filmo.service.UpdateTicketRequest
import com.filmo.service.UserProfile
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileViewModelTest {
    @Test
    fun loadProfileStoresMeApiProfile() = runBlocking {
        val profile = UserProfile(
            id = "7",
            loginId = "user7",
            nickname = "인디콜렉터",
            intro = "작은 극장을 자주 찾습니다.",
            ticketCount = 12,
            savedTicketCount = 8,
            savedTheaterCount = 5
        )
        val viewModel = ProfileViewModel(FakeProfileRepository(listOf(Result.success(profile))))

        viewModel.loadProfile()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(profile, state.profile)
        assertNull(state.errorMessage)
    }

    @Test
    fun loadProfileFailureShowsRecoverableMessage() = runBlocking {
        val viewModel = ProfileViewModel(
            FakeProfileRepository(listOf(Result.failure(IllegalStateException("boom"))))
        )

        viewModel.loadProfile()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.profile)
        assertEquals("프로필을 불러오지 못했어요. 다시 시도해 주세요.", state.errorMessage)
    }

    @Test
    fun retryClearsPreviousErrorAndReloadsProfile() = runBlocking {
        val profile = UserProfile(
            id = "8",
            loginId = "user8",
            nickname = "다시시네마",
            intro = "",
            ticketCount = 1,
            savedTicketCount = 2,
            savedTheaterCount = 3
        )
        val repository = FakeProfileRepository(
            listOf(
                Result.failure(IllegalStateException("first")),
                Result.success(profile)
            )
        )
        val viewModel = ProfileViewModel(repository)

        viewModel.loadProfile()
        viewModel.loadProfile()

        val state = viewModel.uiState.value
        assertEquals(profile, state.profile)
        assertNull(state.errorMessage)
        assertEquals(2, repository.requestCount)
    }

    private class FakeProfileRepository(
        private val results: List<Result<UserProfile>>
    ) : AppRepository {
        var requestCount = 0
            private set

        override suspend fun fetchMyProfile(): Result<UserProfile> {
            val index = requestCount.coerceAtMost(results.lastIndex)
            requestCount += 1
            return results[index]
        }

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
        ): Result<List<MovieCatalogItem>> = Result.success(emptyList())

        override suspend fun fetchMovieDetail(movieId: String): Result<MovieDetail> {
            return Result.failure(UnsupportedOperationException("Not needed in this test"))
        }

        override suspend fun fetchTheaters(
            keyword: String?,
            page: Int,
            size: Int
        ): Result<List<Theater>> = Result.success(emptyList())

        override suspend fun fetchTheater(theaterId: String): Result<Theater> {
            return Result.failure(UnsupportedOperationException("Not needed in this test"))
        }

        override suspend fun fetchTicketCollection(): Result<TicketCollection> {
            return Result.success(TicketCollection(myTickets = emptyList(), savedTickets = emptyList()))
        }

        override suspend fun updateMyTicket(request: UpdateTicketRequest): Result<MovieTicket> {
            return Result.failure(UnsupportedOperationException("Not needed in this test"))
        }

        override suspend fun deleteMyTicket(ticketId: String): Result<Unit> {
            return Result.failure(UnsupportedOperationException("Not needed in this test"))
        }

        override suspend fun removeSavedTicket(ticketId: String): Result<Unit> {
            return Result.failure(UnsupportedOperationException("Not needed in this test"))
        }
    }
}
