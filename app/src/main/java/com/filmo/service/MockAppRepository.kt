package com.filmo.service

import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

class MockAppRepository @Inject constructor() : AppRepository {
    private val movieDetails = listOf(
        MovieDetail(
            id = "decision-to-leave",
            title = "헤어질 결심",
            englishTitle = "Decision to Leave",
            director = "박찬욱",
            actors = "탕웨이, 박해일",
            releaseYear = 2022,
            genre = "로맨스/드라마",
            companyName = "모호필름",
            distributorName = "CJ ENM",
            imagePath = "decision-to-leave.jpg",
            duration = "138분",
            rating = "15세이상관람가",
            colorType = "Color",
            synopsis = "산 정상에서 추락한 남자의 사건을 맡은 형사와 사망자의 아내가 만나는 이야기.",
            screenwriter = "정서경, 박찬욱",
            producer = "박찬욱",
            releaseDate = "2022-06-29",
            keywords = "미스터리, 로맨스"
        ),
        MovieDetail(
            id = "moonlit-winter",
            title = "윤희에게",
            englishTitle = "Moonlit Winter",
            director = "임대형",
            actors = "김희애, 김소혜, 성유빈",
            releaseYear = 2019,
            genre = "드라마",
            companyName = "영화사 달리기",
            distributorName = "리틀빅픽처스",
            imagePath = "moonlit-winter.jpg",
            duration = "105분",
            rating = "12세이상관람가",
            colorType = "Color",
            synopsis = "한 통의 편지를 계기로 오래 묻어둔 마음을 찾아 떠나는 겨울 여행.",
            screenwriter = "임대형",
            producer = "박두희",
            releaseDate = "2019-11-14",
            keywords = "겨울, 편지"
        ),
        MovieDetail(
            id = "house-of-hummingbird",
            title = "벌새",
            englishTitle = "House of Hummingbird",
            director = "김보라",
            actors = "박지후, 김새벽",
            releaseYear = 2018,
            genre = "드라마",
            companyName = "에피파니",
            distributorName = "엣나인필름",
            imagePath = "house-of-hummingbird.jpg",
            duration = "138분",
            rating = "15세이상관람가",
            colorType = "Color",
            synopsis = "1994년 서울, 중학생 은희가 자기만의 세계를 통과하는 이야기.",
            screenwriter = "김보라",
            producer = "김보라",
            releaseDate = "2019-08-29",
            keywords = "성장, 가족"
        ),
        MovieDetail(
            id = "microhabitat",
            title = "소공녀",
            englishTitle = "Microhabitat",
            director = "전고운",
            actors = "이솜, 안재홍",
            releaseYear = 2017,
            genre = "드라마",
            companyName = "광화문시네마",
            distributorName = "CGV아트하우스",
            imagePath = "microhabitat.jpg",
            duration = "106분",
            rating = "15세이상관람가",
            colorType = "Color",
            synopsis = "집은 없어도 취향은 지키고 싶은 미소의 도시 생활기.",
            screenwriter = "전고운",
            producer = "김태곤",
            releaseDate = "2018-03-22",
            keywords = "취향, 청춘"
        )
    )
    private val theaters = listOf(
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
        ),
        Theater(
            id = "seoulartcinema",
            name = "서울아트시네마",
            screenName = "시네마테크",
            screenType = "예술영화관",
            address = "서울특별시 중구 정동길 3",
            phone = "02-741-9782",
            homepage = "https://cinematheque.seoul.kr",
            seatCount = 150,
            naverMapUrl = "https://map.naver.com"
        ),
        Theater(
            id = "artnine",
            name = "아트나인",
            screenName = "0관",
            screenType = "예술영화관",
            address = "서울특별시 동작구 동작대로 89",
            phone = "02-536-0058",
            homepage = "https://www.artnine.co.kr",
            seatCount = 92,
            naverMapUrl = "https://map.naver.com"
        )
    )
    private val savedTheaterIds = mutableSetOf<String>()
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

    override suspend fun ping(): Result<String> = withMockDelay("ping") {
        "mock-pong"
    }

    override suspend fun signUp(request: SignupRequest): Result<AuthSession> = withMockDelay(
        "signUp(loginIdLength=${request.loginId.length}, nicknameLength=${request.nickname.length})"
    ) {
        AuthSession(
            loginId = request.loginId,
            nickname = request.nickname,
            accessToken = "mock-signup-token-${request.loginId}"
        )
    }

    override suspend fun login(request: LoginRequest): Result<AuthSession> = withMockDelay(
        "login(loginIdLength=${request.loginId.length})"
    ) {
        AuthSession(
            loginId = request.loginId,
            nickname = "모크사용자",
            accessToken = "mock-login-token-${request.loginId}"
        )
    }

    override suspend fun fetchRandomNickname(): Result<String> = withMockDelay("fetchRandomNickname") {
        "시네필여행자${(100..999).random()}"
    }

    override suspend fun fetchMyProfile(): Result<UserProfile> = withMockDelay("fetchMyProfile") {
        UserProfile(
            id = "mock-user",
            loginId = "mock-user",
            nickname = "모크사용자",
            intro = "독립영화와 작은 극장을 기록하고 있어요.",
            ticketCount = myTickets.size,
            savedTicketCount = savedTickets.size,
            savedTheaterCount = savedTheaterIds.size
        )
    }

    override suspend fun fetchItems(): Result<List<SampleItem>> = withMockDelay("fetchItems") {
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

    override suspend fun submitItem(request: SampleItemRequest): Result<SampleItem> = withMockDelay(
        "submitItem(titleLength=${request.title.length}, descriptionLength=${request.description.length})"
    ) {
        SampleItem(
            id = "mock-created",
            title = request.title,
            description = request.description
        )
    }

    override suspend fun fetchMovieCatalog(
        keyword: String?,
        genre: String?,
        year: String?,
        page: Int,
        size: Int
    ): Result<List<MovieCatalogItem>> = withMockDelay(
        "fetchMovieCatalog(keywordBlank=${keyword.isNullOrBlank()}, genreBlank=${genre.isNullOrBlank()}, yearBlank=${year.isNullOrBlank()}, page=$page, size=$size)"
    ) {
        movieDetails
            .filter { movie ->
                keyword.isNullOrBlank() ||
                    movie.title.contains(keyword, ignoreCase = true) ||
                    movie.englishTitle.contains(keyword, ignoreCase = true) ||
                    movie.director.contains(keyword, ignoreCase = true)
            }
            .filter { movie ->
                genre.isNullOrBlank() || movie.genre.contains(genre, ignoreCase = true)
            }
            .filter { movie ->
                year.isNullOrBlank() || movie.releaseYear.toString() == year
            }
            .drop((page - 1).coerceAtLeast(0) * size.coerceAtLeast(1))
            .take(size.coerceAtLeast(1))
            .map { it.toCatalogItem() }
    }

    override suspend fun fetchMovieDetail(movieId: String): Result<MovieDetail> = withMockDelay("fetchMovieDetail(movieId=$movieId)") {
        movieDetails.firstOrNull { it.id == movieId }
            ?: error("Movie not found")
    }

    override suspend fun fetchTheaters(
        keyword: String?,
        page: Int,
        size: Int
    ): Result<List<Theater>> = withMockDelay(
        "fetchTheaters(keywordBlank=${keyword.isNullOrBlank()}, page=$page, size=$size)"
    ) {
        theaters
            .filter { theater ->
                keyword.isNullOrBlank() ||
                    theater.name.contains(keyword, ignoreCase = true) ||
                    theater.address.contains(keyword, ignoreCase = true)
            }
            .drop(page.coerceAtLeast(0) * size.coerceAtLeast(1))
            .take(size.coerceAtLeast(1))
    }

    override suspend fun fetchTheater(theaterId: String): Result<Theater> = withMockDelay("fetchTheater(theaterId=$theaterId)") {
        theaters.firstOrNull { it.id == theaterId }
            ?: error("Theater not found")
    }

    override suspend fun saveTheater(theaterId: String): Result<Unit> = withMockDelay("saveTheater(theaterId=$theaterId)") {
        require(theaters.any { it.id == theaterId }) { "Theater not found" }
        savedTheaterIds += theaterId
        Unit
    }

    override suspend fun removeSavedTheater(theaterId: String): Result<Unit> = withMockDelay("removeSavedTheater(theaterId=$theaterId)") {
        savedTheaterIds -= theaterId
        Unit
    }

    override suspend fun fetchTicketCollection(): Result<TicketCollection> = withMockDelay("fetchTicketCollection") {
        TicketCollection(
            myTickets = myTickets.toList(),
            savedTickets = savedTickets.toList()
        )
    }

    override suspend fun fetchPublicTickets(sort: String): Result<List<PublicTicket>> = withMockDelay(
        "fetchPublicTickets(sort=$sort)"
    ) {
        (myTickets + savedTickets).map { ticket ->
            val detail = movieDetails.firstOrNull { it.title == ticket.movieTitle }
            PublicTicket(
                id = ticket.id,
                movieSeq = detail?.id ?: ticket.id,
                movieTitle = ticket.movieTitle,
                genre = detail?.genre.orEmpty(),
                director = detail?.director.orEmpty(),
                releaseYear = detail?.releaseYear ?: 0,
                duration = detail?.duration.orEmpty(),
                posterImagePath = detail?.imagePath.orEmpty(),
                theaterName = ticket.theaterName,
                watchedDate = ticket.watchedDate,
                watchedTime = "19:30",
                review = ticket.review
            )
        }
    }

    override suspend fun createTicket(request: CreateTicketRequest): Result<Unit> = withMockDelay(
        "createTicket(movieId=${request.movieId}, watchedDateLength=${request.watchedDate.length}, cinemaLength=${request.cinema.length}, reviewLength=${request.review.length})"
    ) {
        val movieTitle = movieDetails.firstOrNull { it.id == request.movieId }?.title
            ?: "영화 #${request.movieId}"
        myTickets += MovieTicket(
            id = "my-ticket-${myTickets.size + 1}",
            movieTitle = movieTitle,
            theaterName = request.cinema,
            watchedDate = request.watchedDate,
            rating = 0,
            review = request.review,
            ownedByMe = true,
            savedByMe = false
        )
        Unit
    }

    override suspend fun updateMyTicket(request: UpdateTicketRequest): Result<MovieTicket> = withMockDelay(
        "updateMyTicket(ticketId=${request.ticketId}, theaterLength=${request.theaterName.length}, watchedDateLength=${request.watchedDate.length}, rating=${request.rating}, reviewLength=${request.review.length})"
    ) {
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

    override suspend fun deleteMyTicket(ticketId: String): Result<Unit> = withMockDelay("deleteMyTicket(ticketId=$ticketId)") {
        myTickets.removeAll { it.id == ticketId }
        Unit
    }

    override suspend fun removeSavedTicket(ticketId: String): Result<Unit> = withMockDelay("removeSavedTicket(ticketId=$ticketId)") {
        savedTickets.removeAll { it.id == ticketId }
        Unit
    }

    private suspend fun <T> withMockDelay(operation: String, block: () -> T): Result<T> {
        Timber.d("MockAppRepository.%s request", operation)
        return runCatching {
            delay(MOCK_DELAY_MILLIS)
            block()
        }.onSuccess {
            Timber.d("MockAppRepository.%s success", operation)
        }.onFailure {
            Timber.w(it, "MockAppRepository.%s failed", operation)
        }
    }

    private fun MovieDetail.toCatalogItem(): MovieCatalogItem {
        return MovieCatalogItem(
            id = id,
            title = title,
            releaseYear = releaseYear,
            director = director,
            genre = genre,
            englishTitle = englishTitle,
            imagePath = imagePath
        )
    }

    private companion object {
        const val MOCK_DELAY_MILLIS = 250L
        const val MIN_RATING = 1
        const val MAX_RATING = 5
    }
}
