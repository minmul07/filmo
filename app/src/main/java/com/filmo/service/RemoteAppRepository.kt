package com.filmo.service

import javax.inject.Inject

class RemoteAppRepository @Inject constructor(
    private val apiService: ApiService
) : AppRepository {
    override suspend fun ping(): Result<String> = runCatching {
        apiService.ping().string()
    }

    override suspend fun fetchItems(): Result<List<SampleItem>> = runCatching {
        val responseText = apiService.fetchItems().string()
        listOf(
            SampleItem(
                id = "remote-placeholder",
                title = "Remote response",
                description = responseText
            )
        )
    }

    override suspend fun submitItem(request: SampleItemRequest): Result<SampleItem> = runCatching {
        val responseText = apiService.submitItem(
            title = request.title,
            description = request.description
        ).string()
        SampleItem(
            id = "remote-created-placeholder",
            title = request.title,
            description = responseText.ifBlank { request.description }
        )
    }

    override suspend fun fetchMovieCatalog(): Result<List<MovieCatalogItem>> = runCatching {
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

    override suspend fun fetchTicketCollection(): Result<TicketCollection> = runCatching {
        // TODO: Replace with backend endpoint when the ticket collection API is specified.
        TicketCollection(
            myTickets = listOf(
                MovieTicket(
                    id = "my-ticket-1",
                    movieTitle = "과속스캔들",
                    theaterName = "서울아트시네마",
                    watchedDate = "2024-03-20",
                    rating = 4,
                    review = "유쾌하고 따뜻한 영화였어요",
                    ownedByMe = true,
                    savedByMe = false
                )
            ),
            savedTickets = listOf(
                MovieTicket(
                    id = "saved-ticket-1",
                    movieTitle = "헤어질 결심",
                    theaterName = "인디스페이스",
                    watchedDate = "2024-03-15",
                    rating = 5,
                    review = "영상미가 압도적이었습니다",
                    ownedByMe = false,
                    savedByMe = true
                )
            )
        )
    }

    override suspend fun updateMyTicket(request: UpdateTicketRequest): Result<MovieTicket> = runCatching {
        // TODO: Replace with backend endpoint when the ticket update API is specified.
        MovieTicket(
            id = request.ticketId,
            movieTitle = "과속스캔들",
            theaterName = request.theaterName,
            watchedDate = request.watchedDate,
            rating = request.rating,
            review = request.review,
            ownedByMe = true,
            savedByMe = false
        )
    }

    override suspend fun deleteMyTicket(ticketId: String): Result<Unit> = runCatching {
        // TODO: Replace with backend endpoint when the ticket delete API is specified.
        Unit
    }

    override suspend fun removeSavedTicket(ticketId: String): Result<Unit> = runCatching {
        // TODO: Replace with backend endpoint when the saved ticket API is specified.
        Unit
    }
}
