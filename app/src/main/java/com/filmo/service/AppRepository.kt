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

    suspend fun ping(): Result<String>

    suspend fun fetchMovieCatalog(
        keyword: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<List<MovieCatalogItem>>

    suspend fun fetchMovieCatalogPage(
        keyword: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<MovieCatalogPage> {
        return fetchMovieCatalog(
            keyword = keyword,
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

    suspend fun fetchTicketCollection(): Result<TicketCollection>

    suspend fun fetchPublicTickets(sort: String = "latest"): Result<List<PublicTicket>> {
        return Result.failure(UnsupportedOperationException("Public ticket API is not implemented."))
    }

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

data class MovieTicket(
    val id: String,
    val movieTitle: String,
    val theaterName: String,
    val watchedDate: String,
    val rating: Int,
    val review: String,
    val ownedByMe: Boolean,
    val savedByMe: Boolean,
    val posterImagePath: String = ""
)

data class TicketCollection(
    val myTickets: List<MovieTicket>,
    val savedTickets: List<MovieTicket>
)

data class PublicTicket(
    val id: String,
    val movieSeq: String,
    val movieTitle: String,
    val genre: String,
    val director: String,
    val releaseYear: Int,
    val duration: String,
    val posterImagePath: String,
    val theaterName: String,
    val watchedDate: String,
    val watchedTime: String,
    val review: String,
    val rating: Int = 0,
    val ownerNickname: String = ""
)

data class CreateTicketRequest(
    val movieId: String,
    val watchedDate: String,
    val watchedTime: String,
    val rating: Int,
    val review: String
)

data class UpdateTicketRequest(
    val ticketId: String,
    val watchedDate: String,
    val watchedTime: String,
    val rating: Int,
    val review: String
)
