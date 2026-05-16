package com.filmo.service

interface AppRepository {
    suspend fun signUp(request: SignupRequest): Result<AuthSession> {
        return Result.failure(UnsupportedOperationException("Auth API is not implemented."))
    }

    suspend fun login(request: LoginRequest): Result<AuthSession> {
        return Result.failure(UnsupportedOperationException("Auth API is not implemented."))
    }

    suspend fun fetchRandomNickname(): Result<String> {
        return Result.failure(UnsupportedOperationException("Random nickname API is not implemented."))
    }

    suspend fun fetchMyProfile(): Result<UserProfile> {
        return Result.failure(UnsupportedOperationException("Profile API is not implemented."))
    }

    suspend fun ping(): Result<String>

    suspend fun fetchItems(): Result<List<SampleItem>>

    suspend fun submitItem(request: SampleItemRequest): Result<SampleItem>

    suspend fun fetchMovieCatalog(
        keyword: String? = null,
        genre: String? = null,
        year: String? = null,
        page: Int = 1,
        size: Int = 20
    ): Result<List<MovieCatalogItem>>

    suspend fun fetchMovieCatalogPage(
        keyword: String? = null,
        genre: String? = null,
        year: String? = null,
        page: Int = 1,
        size: Int = 20
    ): Result<MovieCatalogPage> {
        return fetchMovieCatalog(
            keyword = keyword,
            genre = genre,
            year = year,
            page = page,
            size = size
        ).map { movies ->
            MovieCatalogPage(
                items = movies,
                hasMore = movies.size >= size
            )
        }
    }

    suspend fun fetchMovieDetail(movieId: String): Result<MovieDetail>

    suspend fun fetchTheaters(
        keyword: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<List<Theater>>

    suspend fun fetchTheaterPage(
        keyword: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<TheaterPage> {
        return fetchTheaters(
            keyword = keyword,
            page = page,
            size = size
        ).map { theaters ->
            TheaterPage(
                items = theaters,
                hasMore = theaters.size >= size.coerceAtLeast(1)
            )
        }
    }

    suspend fun fetchTheater(theaterId: String): Result<Theater>

    suspend fun saveTheater(theaterId: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Theater save API is not implemented."))
    }

    suspend fun removeSavedTheater(theaterId: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Saved theater delete API is not implemented."))
    }

    suspend fun fetchTicketCollection(): Result<TicketCollection>

    suspend fun createTicket(request: CreateTicketRequest): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Ticket create API is not implemented."))
    }

    suspend fun updateMyTicket(request: UpdateTicketRequest): Result<MovieTicket>

    suspend fun deleteMyTicket(ticketId: String): Result<Unit>

    suspend fun removeSavedTicket(ticketId: String): Result<Unit>
}

data class SignupRequest(
    val loginId: String,
    val password: String,
    val nickname: String
)

data class LoginRequest(
    val loginId: String,
    val password: String
)

data class AuthSession(
    val loginId: String,
    val nickname: String,
    val accessToken: String
)

data class UserProfile(
    val id: String,
    val loginId: String,
    val nickname: String,
    val intro: String,
    val ticketCount: Int,
    val savedTicketCount: Int,
    val savedTheaterCount: Int
)

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

data class MovieCatalogPage(
    val items: List<MovieCatalogItem>,
    val hasMore: Boolean
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

data class TheaterPage(
    val items: List<Theater>,
    val hasMore: Boolean
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

data class CreateTicketRequest(
    val movieId: String,
    val watchedDate: String,
    val watchedTime: String,
    val cinema: String,
    val review: String
)

data class UpdateTicketRequest(
    val ticketId: String,
    val theaterName: String,
    val watchedDate: String,
    val rating: Int,
    val review: String
)
