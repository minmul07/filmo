package com.filmo.service

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject

class RemoteAppRepository @Inject constructor(
    private val apiService: ApiService
) : AppRepository {
    override suspend fun signUp(request: SignupRequest): Result<AuthSession> = runCatching {
        Timber.d(
            "RemoteAppRepository.signUp request loginIdLength=%d nicknameLength=%d",
            request.loginId.length,
            request.nickname.length
        )
        val response = apiService.signup(
            buildJsonRequestBody {
                put("loginId", request.loginId)
                put("password", request.password)
                put("nickname", request.nickname)
            }
        )
        val accessToken = JsonParser.parseToJsonElement(response.string())
            .jsonObject
            .dataObject()
            .string("accessToken")
        AuthSession(
            loginId = request.loginId,
            nickname = request.nickname,
            accessToken = accessToken
        )
    }.onSuccess {
        Timber.d("RemoteAppRepository.signUp success loginIdLength=%d", request.loginId.length)
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.signUp failed")
    }

    override suspend fun login(request: LoginRequest): Result<AuthSession> = runCatching {
        Timber.d("RemoteAppRepository.login request loginIdLength=%d", request.loginId.length)
        val response = apiService.login(
            buildJsonRequestBody {
                put("loginId", request.loginId)
                put("password", request.password)
            }
        )
        val accessToken = JsonParser.parseToJsonElement(response.string())
            .jsonObject
            .dataObject()
            .string("accessToken")
        val me = JsonParser.parseToJsonElement(
            apiService.fetchMe("Bearer $accessToken").string()
        )
            .jsonObject
            .dataObject()
        AuthSession(
            loginId = me.string("loginId").ifBlank { request.loginId },
            nickname = me.string("nickname"),
            accessToken = accessToken
        )
    }.onSuccess {
        Timber.d("RemoteAppRepository.login success loginIdLength=%d", request.loginId.length)
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.login failed")
    }

    override suspend fun fetchRandomNickname(): Result<String> = runCatching {
        Timber.d("RemoteAppRepository.fetchRandomNickname request")
        JsonParser.parseToJsonElement(apiService.fetchRandomNickname().string())
            .jsonObject
            .dataObject()
            .string("nickname")
            .ifBlank { error("Missing nickname") }
    }.onSuccess { nickname ->
        Timber.d("RemoteAppRepository.fetchRandomNickname success nicknameLength=%d", nickname.length)
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.fetchRandomNickname failed")
    }

    override suspend fun fetchMyProfile(): Result<UserProfile> = runCatching {
        Timber.d("RemoteAppRepository.fetchMyProfile request")
        JsonParser.parseToJsonElement(apiService.fetchMe().string())
            .jsonObject
            .dataObject()
            .toUserProfile()
    }.onSuccess { profile ->
        Timber.d("RemoteAppRepository.fetchMyProfile success profileId=%s", profile.id)
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.fetchMyProfile failed")
    }

    override suspend fun ping(): Result<String> = runCatching {
        Timber.d("RemoteAppRepository.ping request")
        apiService.ping().string()
    }.onSuccess { response ->
        Timber.d("RemoteAppRepository.ping success responseLength=%d", response.length)
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.ping failed")
    }

    override suspend fun fetchItems(): Result<List<SampleItem>> = runCatching {
        Timber.d("RemoteAppRepository.fetchItems request")
        val responseText = apiService.fetchItems().string()
        listOf(
            SampleItem(
                id = "remote-placeholder",
                title = "Remote response",
                description = responseText
            )
        )
    }.onSuccess { items ->
        Timber.d("RemoteAppRepository.fetchItems success itemCount=%d", items.size)
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.fetchItems failed")
    }

    override suspend fun submitItem(request: SampleItemRequest): Result<SampleItem> = runCatching {
        Timber.d(
            "RemoteAppRepository.submitItem request titleLength=%d descriptionLength=%d",
            request.title.length,
            request.description.length
        )
        val responseText = apiService.submitItem(
            title = request.title,
            description = request.description
        ).string()
        SampleItem(
            id = "remote-created-placeholder",
            title = request.title,
            description = responseText.ifBlank { request.description }
        )
    }.onSuccess { item ->
        Timber.d("RemoteAppRepository.submitItem success itemId=%s", item.id)
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.submitItem failed")
    }

    override suspend fun fetchMovieCatalog(
        keyword: String?,
        genre: String?,
        year: String?,
        page: Int,
        size: Int
    ): Result<List<MovieCatalogItem>> = fetchMovieCatalogPage(
        keyword = keyword,
        genre = genre,
        year = year,
        page = page,
        size = size
    ).map { it.items }

    override suspend fun fetchMovieCatalogPage(
        keyword: String?,
        genre: String?,
        year: String?,
        page: Int,
        size: Int
    ): Result<MovieCatalogPage> = runCatching {
        Timber.d(
            "RemoteAppRepository.fetchMovieCatalogPage request keywordBlank=%s genreBlank=%s yearBlank=%s page=%d size=%d",
            keyword.isNullOrBlank(),
            genre.isNullOrBlank(),
            year.isNullOrBlank(),
            page,
            size
        )
        val response = apiService.fetchMovies(
            keyword = keyword,
            genre = genre,
            year = year,
            page = page,
            size = size
        )
        val data = JsonParser.parseToJsonElement(response.string())
            .jsonObject
            .dataObject()
        val items = data
            .array("content")
            .map { it.jsonObject.toMovieCatalogItem() }
        MovieCatalogPage(
            items = items,
            hasMore = data.hasMore(itemsLoaded = items.size, requestedSize = size)
        )
    }.onSuccess { pageResult ->
        Timber.d(
            "RemoteAppRepository.fetchMovieCatalogPage success itemCount=%d hasMore=%s",
            pageResult.items.size,
            pageResult.hasMore
        )
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.fetchMovieCatalogPage failed page=%d size=%d", page, size)
    }

    override suspend fun fetchMovieDetail(movieId: String): Result<MovieDetail> = runCatching {
        Timber.d("RemoteAppRepository.fetchMovieDetail request movieId=%s", movieId)
        val seq = movieId.toLongOrNull() ?: error("Invalid movie id")
        JsonParser.parseToJsonElement(apiService.fetchMovie(seq).string())
            .jsonObject
            .dataObject()
            .toMovieDetail()
    }.onSuccess { detail ->
        Timber.d("RemoteAppRepository.fetchMovieDetail success movieId=%s detailId=%s", movieId, detail.id)
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.fetchMovieDetail failed movieId=%s", movieId)
    }

    override suspend fun fetchTheaters(
        keyword: String?,
        page: Int,
        size: Int
    ): Result<List<Theater>> = fetchTheaterPage(
        keyword = keyword,
        page = page,
        size = size
    ).map { it.items }

    override suspend fun fetchTheaterPage(
        keyword: String?,
        page: Int,
        size: Int
    ): Result<TheaterPage> = runCatching {
        Timber.d(
            "RemoteAppRepository.fetchTheaterPage request keywordBlank=%s page=%d size=%d",
            keyword.isNullOrBlank(),
            page,
            size
        )
        val response = apiService.fetchTheaters(
            keyword = keyword,
            page = page,
            size = size
        )
        val data = JsonParser.parseToJsonElement(response.string())
            .jsonObject
            .dataObject()
        val items = data
            .array("content")
            .map { it.jsonObject.toTheater() }
        TheaterPage(
            items = items,
            hasMore = data.hasMore(itemsLoaded = items.size, requestedSize = size)
        )
    }.onSuccess { pageResult ->
        Timber.d(
            "RemoteAppRepository.fetchTheaterPage success itemCount=%d hasMore=%s",
            pageResult.items.size,
            pageResult.hasMore
        )
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.fetchTheaterPage failed page=%d size=%d", page, size)
    }

    override suspend fun fetchTheater(theaterId: String): Result<Theater> = runCatching {
        Timber.d("RemoteAppRepository.fetchTheater request theaterId=%s", theaterId)
        JsonParser.parseToJsonElement(apiService.fetchTheater(theaterId).string())
            .jsonObject
            .dataObject()
            .toTheater()
    }.onSuccess { theater ->
        Timber.d("RemoteAppRepository.fetchTheater success theaterId=%s", theater.id)
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.fetchTheater failed theaterId=%s", theaterId)
    }

    override suspend fun saveTheater(theaterId: String): Result<Unit> = runCatching {
        Timber.d("RemoteAppRepository.saveTheater request theaterId=%s", theaterId)
        apiService.saveTheater(theaterId).close()
        Unit
    }.onSuccess {
        Timber.d("RemoteAppRepository.saveTheater success theaterId=%s", theaterId)
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.saveTheater failed theaterId=%s", theaterId)
    }

    override suspend fun removeSavedTheater(theaterId: String): Result<Unit> = runCatching {
        Timber.d("RemoteAppRepository.removeSavedTheater request theaterId=%s", theaterId)
        apiService.removeSavedTheater(theaterId).close()
        Unit
    }.onSuccess {
        Timber.d("RemoteAppRepository.removeSavedTheater success theaterId=%s", theaterId)
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.removeSavedTheater failed theaterId=%s", theaterId)
    }

    override suspend fun fetchTicketCollection(): Result<TicketCollection> = runCatching {
        Timber.d("RemoteAppRepository.fetchTicketCollection request")
        val myTickets = JsonParser.parseToJsonElement(apiService.fetchTickets().string())
            .jsonObject
            .dataArray()
            .map { it.jsonObject.toMovieTicket(ownedByMe = true) }
        // Swagger exposes my tickets, but no saved-ticket collection endpoint.
        // Keep the collection screen usable until that API is added.
        TicketCollection(
            myTickets = myTickets,
            savedTickets = emptyList()
        )
    }.onSuccess { collection ->
        Timber.d(
            "RemoteAppRepository.fetchTicketCollection success myCount=%d savedCount=%d",
            collection.myTickets.size,
            collection.savedTickets.size
        )
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.fetchTicketCollection failed")
    }

    override suspend fun fetchPublicTickets(sort: String): Result<List<PublicTicket>> = runCatching {
        Timber.d("RemoteAppRepository.fetchPublicTickets request sort=%s", sort)
        JsonParser.parseToJsonElement(apiService.fetchPublicTickets(sort = sort).string())
            .jsonObject
            .dataArray()
            .map { it.jsonObject.toPublicTicket() }
    }.onSuccess { tickets ->
        Timber.d("RemoteAppRepository.fetchPublicTickets success count=%d", tickets.size)
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.fetchPublicTickets failed")
    }

    override suspend fun createTicket(request: CreateTicketRequest): Result<Unit> = runCatching {
        Timber.d(
            "RemoteAppRepository.createTicket request movieId=%s watchedDateLength=%d cinemaLength=%d reviewLength=%d",
            request.movieId,
            request.watchedDate.length,
            request.cinema.length,
            request.review.length
        )
        val movieSeq = request.movieId.toLongOrNull() ?: error("Invalid movie id")
        apiService.createTicket(
            buildJsonRequestBody {
                put("movieSeq", movieSeq)
                put("watchedDate", request.watchedDate)
                put("watchedTime", request.watchedTime)
                put("cinema", request.cinema)
                put("review", request.review)
            }
        ).close()
        Unit
    }.onSuccess {
        Timber.d("RemoteAppRepository.createTicket success movieId=%s", request.movieId)
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.createTicket failed movieId=%s", request.movieId)
    }

    override suspend fun updateMyTicket(request: UpdateTicketRequest): Result<MovieTicket> = runCatching {
        Timber.d(
            "RemoteAppRepository.updateMyTicket request ticketId=%s theaterLength=%d watchedDateLength=%d rating=%d reviewLength=%d",
            request.ticketId,
            request.theaterName.length,
            request.watchedDate.length,
            request.rating,
            request.review.length
        )
        error("Ticket update API is not specified in Swagger.")
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.updateMyTicket failed ticketId=%s", request.ticketId)
    }

    override suspend fun deleteMyTicket(ticketId: String): Result<Unit> = runCatching {
        Timber.d("RemoteAppRepository.deleteMyTicket request ticketId=%s", ticketId)
        error("Ticket delete API is not specified in Swagger.")
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.deleteMyTicket failed ticketId=%s", ticketId)
    }

    override suspend fun removeSavedTicket(ticketId: String): Result<Unit> = runCatching {
        Timber.d("RemoteAppRepository.removeSavedTicket request ticketId=%s", ticketId)
        error("Saved ticket delete API is not specified in Swagger.")
    }.onFailure {
        Timber.w(it, "RemoteAppRepository.removeSavedTicket failed ticketId=%s", ticketId)
    }

    private fun JsonObject.toMovieCatalogItem(): MovieCatalogItem {
        val imagePath = string("imagePath")
        Timber.d(
            "RemoteAppRepository.toMovieCatalogItem movieId=%s rawImagePath=%s",
            string("seq"),
            imagePath
        )
        return MovieCatalogItem(
            id = string("seq"),
            title = string("korTitle"),
            releaseYear = intFromString("productionYear"),
            director = string("director"),
            genre = string("genreName"),
            englishTitle = string("engTitle"),
            imagePath = imagePath
        )
    }

    private fun JsonObject.toMovieDetail(): MovieDetail {
        val imagePath = string("imagePath")
        Timber.d(
            "RemoteAppRepository.toMovieDetail movieId=%s rawImagePath=%s",
            string("seq"),
            imagePath
        )
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
            imagePath = imagePath,
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

    private fun JsonObject.toUserProfile(): UserProfile {
        return UserProfile(
            id = string("id"),
            loginId = string("loginId"),
            nickname = string("nickname"),
            intro = string("intro"),
            ticketCount = int("ticketCount"),
            savedTicketCount = int("savedTicketCount"),
            savedTheaterCount = int("savedTheaterCount")
        )
    }

    private suspend fun JsonObject.toMovieTicket(ownedByMe: Boolean): MovieTicket {
        val movieSeq = string("movieSeq")
        return MovieTicket(
            id = string("id"),
            movieTitle = string("movieTitle")
                .ifBlank { string("korTitle") }
                .ifBlank { fetchMovieTitle(movieSeq) },
            theaterName = string("cinema"),
            watchedDate = string("watchedDate"),
            rating = int("rating").coerceIn(MIN_TICKET_RATING, MAX_TICKET_RATING),
            review = string("review"),
            ownedByMe = ownedByMe,
            savedByMe = booleanOrNull("savedByMe") == true
        )
    }

    private suspend fun JsonObject.toPublicTicket(): PublicTicket {
        val movieSeq = string("movieSeq")
        val movieDetail = fetchMovieDetailOrNull(movieSeq)
        return PublicTicket(
            id = string("id"),
            movieSeq = movieSeq,
            movieTitle = movieDetail?.title?.ifBlank { "영화 #$movieSeq" } ?: "영화 #$movieSeq",
            genre = movieDetail?.genre.orEmpty(),
            director = movieDetail?.director.orEmpty(),
            releaseYear = movieDetail?.releaseYear ?: 0,
            duration = movieDetail?.duration.orEmpty(),
            posterImagePath = movieDetail?.imagePath.orEmpty(),
            theaterName = string("cinema"),
            watchedDate = string("watchedDate"),
            watchedTime = string("watchedTime"),
            review = string("review")
        )
    }

    private suspend fun fetchMovieTitle(movieSeq: String): String {
        val seq = movieSeq.toLongOrNull() ?: return "제목 없음"
        return runCatching {
            JsonParser.parseToJsonElement(apiService.fetchMovie(seq).string())
                .jsonObject
                .dataObject()
                .toMovieDetail()
                .title
                .ifBlank { "영화 #$movieSeq" }
        }.getOrElse {
            Timber.w(it, "RemoteAppRepository.fetchMovieTitle failed movieSeq=%s", movieSeq)
            "영화 #$movieSeq"
        }
    }

    private suspend fun fetchMovieDetailOrNull(movieSeq: String): MovieDetail? {
        val seq = movieSeq.toLongOrNull() ?: return null
        return runCatching {
            JsonParser.parseToJsonElement(apiService.fetchMovie(seq).string())
                .jsonObject
                .dataObject()
                .toMovieDetail()
        }.onFailure {
            Timber.w(it, "RemoteAppRepository.fetchMovieDetailOrNull failed movieSeq=%s", movieSeq)
        }.getOrNull()
    }

    private fun JsonObject.dataObject(): JsonObject {
        return this["data"]?.jsonObject ?: error("Missing data")
    }

    private fun JsonObject.dataArray(): JsonArray {
        return this["data"]?.jsonArray ?: JsonArray(emptyList())
    }

    private fun JsonObject.array(name: String): JsonArray {
        return this[name]?.jsonArray ?: JsonArray(emptyList())
    }

    private fun JsonObject.hasMore(itemsLoaded: Int, requestedSize: Int): Boolean {
        val hasNext = booleanOrNull("hasNext")
        if (hasNext != null) return hasNext

        val last = booleanOrNull("last")
        if (last != null) return !last

        return itemsLoaded >= requestedSize.coerceAtLeast(1)
    }

    private fun JsonObject.booleanOrNull(name: String): Boolean? {
        val value = this[name] ?: return null
        if (value is JsonNull) return null
        return value.jsonPrimitive.booleanOrNull
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
        const val MIN_TICKET_RATING = 0
        const val MAX_TICKET_RATING = 5
        val JsonContentType = "application/json; charset=utf-8".toMediaType()
        val JsonParser = Json { ignoreUnknownKeys = true }

        fun buildJsonRequestBody(builder: JsonObjectBuilderScope.() -> Unit): RequestBody {
            val scope = JsonObjectBuilderScope()
            scope.builder()
            return scope.build().toString().toRequestBody(JsonContentType)
        }
    }
}

private class JsonObjectBuilderScope {
    private val entries = mutableListOf<Pair<String, JsonValue>>()

    fun put(key: String, value: String) {
        entries += key to JsonValue.StringValue(value)
    }

    fun put(key: String, value: Long) {
        entries += key to JsonValue.LongValue(value)
    }

    fun build(): JsonObject {
        return buildJsonObject {
            entries.forEach { (key, value) ->
                when (value) {
                    is JsonValue.LongValue -> put(key, value.value)
                    is JsonValue.StringValue -> put(key, value.value)
                }
            }
        }
    }

    private sealed interface JsonValue {
        data class LongValue(val value: Long) : JsonValue
        data class StringValue(val value: String) : JsonValue
    }
}
