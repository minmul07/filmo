package com.filmo.service

import kotlinx.coroutines.delay
import javax.inject.Inject

class MockAppRepository @Inject constructor() : AppRepository {
    private val myTickets = mutableListOf(
        MovieTicket(
            id = "my-ticket-1",
            movieTitle = "과속스캔들",
            theaterName = "서울아트시네마",
            watchedDate = "2024-03-20",
            rating = 4,
            review = "유쾌하고 따뜻한 영화였어요",
            ownedByMe = true,
            savedByMe = false
        ),
        MovieTicket(
            id = "my-ticket-2",
            movieTitle = "윤희에게",
            theaterName = "아트나인",
            watchedDate = "2024-02-18",
            rating = 5,
            review = "겨울 공기와 편지의 여운이 오래 남았다.",
            ownedByMe = true,
            savedByMe = false
        )
    )
    private val savedTickets = mutableListOf(
        MovieTicket(
            id = "saved-ticket-1",
            movieTitle = "헤어질 결심",
            theaterName = "인디스페이스",
            watchedDate = "2024-03-15",
            rating = 5,
            review = "영상미가 압도적이었습니다",
            ownedByMe = false,
            savedByMe = true
        ),
        MovieTicket(
            id = "saved-ticket-2",
            movieTitle = "벌새",
            theaterName = "라이카시네마",
            watchedDate = "2024-01-09",
            rating = 4,
            review = "작은 순간들이 오래 기억에 남았다.",
            ownedByMe = false,
            savedByMe = true
        )
    )

    override suspend fun ping(): Result<String> = withMockDelay {
        "mock-pong"
    }

    override suspend fun fetchItems(): Result<List<SampleItem>> = withMockDelay {
        listOf(
            SampleItem(
                id = "mock-1",
                title = "Mock item",
                description = "Template item from MockAppRepository"
            ),
            SampleItem(
                id = "mock-2",
                title = "Second mock item",
                description = "Use this data while the backend is not ready"
            )
        )
    }

    override suspend fun submitItem(request: SampleItemRequest): Result<SampleItem> = withMockDelay {
        SampleItem(
            id = "mock-created",
            title = request.title,
            description = request.description
        )
    }

    override suspend fun fetchMovieCatalog(): Result<List<MovieCatalogItem>> = withMockDelay {
        listOf(
            MovieCatalogItem(
                id = "decision-to-leave",
                title = "헤어질 결심",
                releaseYear = 2022,
                director = "박찬욱",
                genre = "로맨스/드라마"
            ),
            MovieCatalogItem(
                id = "moonlit-winter",
                title = "윤희에게",
                releaseYear = 2019,
                director = "임대형",
                genre = "드라마"
            ),
            MovieCatalogItem(
                id = "house-of-hummingbird",
                title = "벌새",
                releaseYear = 2018,
                director = "김보라",
                genre = "드라마"
            ),
            MovieCatalogItem(
                id = "microhabitat",
                title = "소공녀",
                releaseYear = 2017,
                director = "전고운",
                genre = "드라마"
            )
        )
    }

    override suspend fun fetchTicketCollection(): Result<TicketCollection> = withMockDelay {
        TicketCollection(
            myTickets = myTickets.toList(),
            savedTickets = savedTickets.toList()
        )
    }

    override suspend fun updateMyTicket(request: UpdateTicketRequest): Result<MovieTicket> = withMockDelay {
        val index = myTickets.indexOfFirst { it.id == request.ticketId }
        require(index >= 0) { "Ticket not found" }

        val updatedTicket = myTickets[index].copy(
            theaterName = request.theaterName,
            watchedDate = request.watchedDate,
            rating = request.rating.coerceIn(MIN_RATING, MAX_RATING),
            review = request.review
        )
        myTickets[index] = updatedTicket
        updatedTicket
    }

    override suspend fun deleteMyTicket(ticketId: String): Result<Unit> = withMockDelay {
        myTickets.removeAll { it.id == ticketId }
        Unit
    }

    override suspend fun removeSavedTicket(ticketId: String): Result<Unit> = withMockDelay {
        savedTickets.removeAll { it.id == ticketId }
        Unit
    }

    private suspend fun <T> withMockDelay(block: () -> T): Result<T> = runCatching {
        delay(MOCK_DELAY_MILLIS)
        block()
    }

    private companion object {
        const val MOCK_DELAY_MILLIS = 250L
        const val MIN_RATING = 1
        const val MAX_RATING = 5
    }
}
