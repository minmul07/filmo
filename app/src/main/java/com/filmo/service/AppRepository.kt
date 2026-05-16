package com.filmo.service

interface AppRepository {
    suspend fun ping(): Result<String>

    suspend fun fetchItems(): Result<List<SampleItem>>

    suspend fun submitItem(request: SampleItemRequest): Result<SampleItem>

    suspend fun fetchMovieCatalog(): Result<List<MovieCatalogItem>>

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
    val genre: String
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
