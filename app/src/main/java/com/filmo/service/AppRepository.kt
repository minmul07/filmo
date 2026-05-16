package com.filmo.service

interface AppRepository {
    suspend fun ping(): Result<String>

    suspend fun fetchItems(): Result<List<SampleItem>>

    suspend fun submitItem(request: SampleItemRequest): Result<SampleItem>

    suspend fun fetchMovieCatalog(
        keyword: String? = null,
        genre: String? = null,
        year: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<List<MovieCatalogItem>>

    suspend fun fetchMovieDetail(movieId: String): Result<MovieDetail>

    suspend fun fetchTheaters(
        keyword: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<List<Theater>>

    suspend fun fetchTheater(theaterId: String): Result<Theater>

    suspend fun fetchTicketCollection(): Result<TicketCollection>

    suspend fun updateMyTicket(request: UpdateTicketRequest): Result<MovieTicket>

    suspend fun deleteMyTicket(ticketId: String): Result<Unit>

    suspend fun removeSavedTicket(ticketId: String): Result<Unit>
}

data class SampleItem(
    val id: String,
    val title: String,
    val description: String
)

data class SampleItemRequest(
    val title: String,
    val description: String
)

data class MovieCatalogItem(
    val id: String,
    val title: String,
    val releaseYear: Int,
    val director: String,
    val genre: String,
    val englishTitle: String = "",
    val imagePath: String = ""
)

data class MovieDetail(
    val id: String,
    val title: String,
    val englishTitle: String,
    val director: String,
    val actors: String,
    val releaseYear: Int,
    val genre: String,
    val companyName: String,
    val distributorName: String,
    val imagePath: String,
    val duration: String,
    val rating: String,
    val colorType: String,
    val synopsis: String,
    val screenwriter: String,
    val producer: String,
    val releaseDate: String,
    val keywords: String
)

data class Theater(
    val id: String,
    val name: String,
    val screenName: String,
    val screenType: String,
    val address: String,
    val phone: String,
    val homepage: String,
    val seatCount: Int,
    val naverMapUrl: String
)

data class MovieTicket(
    val id: String,
    val movieTitle: String,
    val theaterName: String,
    val watchedDate: String,
    val rating: Int,
    val review: String,
    val ownedByMe: Boolean,
    val savedByMe: Boolean
)

data class TicketCollection(
    val myTickets: List<MovieTicket>,
    val savedTickets: List<MovieTicket>
)

data class UpdateTicketRequest(
    val ticketId: String,
    val theaterName: String,
    val watchedDate: String,
    val rating: Int,
    val review: String
)
