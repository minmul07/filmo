package com.filmo.service

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

    override suspend fun fetchMovieCatalog(
        keyword: String?,
        genre: String?,
        year: String?,
        page: Int,
        size: Int
    ): Result<List<MovieCatalogItem>> = runCatching {
        val response = apiService.fetchMovies(
            keyword = keyword,
            genre = genre,
            year = year,
            page = page,
            size = size
        )
        JsonParser.parseToJsonElement(response.string())
            .jsonObject
            .dataObject()
            .array("content")
            .map { it.jsonObject.toMovieCatalogItem() }
    }

    override suspend fun fetchMovieDetail(movieId: String): Result<MovieDetail> = runCatching {
        val seq = movieId.toLongOrNull() ?: error("Invalid movie id")
        JsonParser.parseToJsonElement(apiService.fetchMovie(seq).string())
            .jsonObject
            .dataObject()
            .toMovieDetail()
    }

    override suspend fun fetchTheaters(
        keyword: String?,
        page: Int,
        size: Int
    ): Result<List<Theater>> = runCatching {
        JsonParser.parseToJsonElement(
            apiService.fetchTheaters(
                keyword = keyword,
                page = page,
                size = size
            ).string()
        )
            .jsonObject
            .dataObject()
            .array("content")
            .map { it.jsonObject.toTheater() }
    }

    override suspend fun fetchTheater(theaterId: String): Result<Theater> = runCatching {
        JsonParser.parseToJsonElement(apiService.fetchTheater(theaterId).string())
            .jsonObject
            .dataObject()
            .toTheater()
    }

    override suspend fun fetchTicketCollection(): Result<TicketCollection> = runCatching {
        error("Ticket collection API is not specified in Swagger.")
    }

    override suspend fun updateMyTicket(request: UpdateTicketRequest): Result<MovieTicket> = runCatching {
        error("Ticket update API is not specified in Swagger.")
    }

    override suspend fun deleteMyTicket(ticketId: String): Result<Unit> = runCatching {
        error("Ticket delete API is not specified in Swagger.")
    }

    override suspend fun removeSavedTicket(ticketId: String): Result<Unit> = runCatching {
        error("Saved ticket delete API is not specified in Swagger.")
    }

    private fun JsonObject.toMovieCatalogItem(): MovieCatalogItem {
        return MovieCatalogItem(
            id = string("seq"),
            title = string("korTitle"),
            releaseYear = intFromString("productionYear"),
            director = string("director"),
            genre = string("genreName"),
            englishTitle = string("engTitle"),
            imagePath = string("imagePath")
        )
    }

    private fun JsonObject.toMovieDetail(): MovieDetail {
        return MovieDetail(
            id = string("seq"),
            title = string("korTitle"),
            englishTitle = string("engTitle"),
            director = string("director"),
            actors = string("actors"),
            releaseYear = intFromString("productionYear"),
            genre = string("genreName"),
            companyName = string("companyNm"),
            distributorName = string("distributorNm"),
            imagePath = string("imagePath"),
            duration = string("duration"),
            rating = string("rating"),
            colorType = string("colorType"),
            synopsis = string("synopsis"),
            screenwriter = string("screenwriter"),
            producer = string("producer"),
            releaseDate = string("releaseDate"),
            keywords = string("keywords")
        )
    }

    private fun JsonObject.toTheater(): Theater {
        return Theater(
            id = string("theaCd"),
            name = string("theaName"),
            screenName = string("scrnName"),
            screenType = string("screenGb"),
            address = string("address"),
            phone = string("phone"),
            homepage = string("homepage"),
            seatCount = int("seatCount"),
            naverMapUrl = string("naverMapUrl")
        )
    }

    private fun JsonObject.dataObject(): JsonObject {
        return this["data"]?.jsonObject ?: error("Missing data")
    }

    private fun JsonObject.array(name: String): JsonArray {
        return this[name]?.jsonArray ?: JsonArray(emptyList())
    }

    private fun JsonObject.string(name: String): String {
        return this[name]?.jsonPrimitive?.contentOrNull.orEmpty()
    }

    private fun JsonObject.int(name: String): Int {
        return string(name).toIntOrNull() ?: 0
    }

    private fun JsonObject.intFromString(name: String): Int {
        return string(name).toIntOrNull() ?: 0
    }

    private companion object {
        val JsonParser = Json { ignoreUnknownKeys = true }
    }
}
