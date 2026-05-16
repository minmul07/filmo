package com.filmo.service

import kotlinx.coroutines.runBlocking
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import timber.log.Timber

class RemoteAppRepositoryTest {
    @After
    fun tearDown() {
        Timber.uprootAll()
    }

    @Test
    fun signUpPostsSignupRequestAndMapsAccessToken() = runBlocking {
        val apiService = FakeApiService(
            signupResponse = """
                {
                  "code": 200,
                  "message": "OK",
                  "data": {
                    "accessToken": "signup-token"
                  }
                }
            """.trimIndent()
        )
        val repository = RemoteAppRepository(apiService)

        val result = repository.signUp(
            SignupRequest(
                loginId = "user1",
                password = "1234",
                nickname = "서버닉네임"
            )
        )

        assertTrue(result.isSuccess)
        assertEquals(
            """{"loginId":"user1","password":"1234","nickname":"서버닉네임"}""",
            apiService.signupBody
        )
        assertEquals(AuthSession("user1", "서버닉네임", "signup-token"), result.getOrThrow())
    }

    @Test
    fun signUpLogsRawAccessTokenFromResponse() = runBlocking {
        val tree = CapturingTimberTree()
        Timber.plant(tree)
        val repository = RemoteAppRepository(
            FakeApiService(
                signupResponse = """{"data":{"accessToken":"signup-token-raw"}}"""
            )
        )

        val result = repository.signUp(
            SignupRequest(
                loginId = "user1",
                password = "1234",
                nickname = "서버닉네임"
            )
        )

        assertTrue(result.isSuccess)
        assertTrue(
            tree.messages.any { message ->
                message.contains("rawAccessToken=signup-token-raw")
            }
        )
    }

    @Test
    fun loginPostsLoginRequestAndMapsAccessToken() = runBlocking {
        val apiService = FakeApiService(
            loginResponse = """
                {
                  "code": 200,
                  "message": "OK",
                  "data": {
                    "accessToken": "login-token"
                  }
                }
            """.trimIndent(),
            meResponse = """
                {
                  "code": 200,
                  "message": "OK",
                  "data": {
                    "loginId": "user1",
                    "nickname": "로그인닉네임"
                  }
                }
            """.trimIndent()
        )
        val repository = RemoteAppRepository(apiService)

        val result = repository.login(
            LoginRequest(
                loginId = "user1",
                password = "1234"
            )
        )

        assertTrue(result.isSuccess)
        assertEquals("""{"loginId":"user1","password":"1234"}""", apiService.loginBody)
        assertEquals("Bearer login-token", apiService.meAuthorization)
        assertEquals(AuthSession("user1", "로그인닉네임", "login-token"), result.getOrThrow())
    }

    @Test
    fun fetchRandomNicknameMapsSwaggerNicknameResponse() = runBlocking {
        val repository = RemoteAppRepository(
            FakeApiService(
                randomNicknameResponse = """
                    {
                      "code": 200,
                      "message": "OK",
                      "data": {
                        "nickname": "시네필여행자965"
                      }
                    }
                """.trimIndent()
            )
        )

        val result = repository.fetchRandomNickname()

        assertTrue(result.isSuccess)
        assertEquals("시네필여행자965", result.getOrThrow())
    }

    @Test
    fun fetchMovieCatalogMapsSwaggerMovieListResponse() = runBlocking {
        val repository = RemoteAppRepository(
            FakeApiService(
                moviesResponse = """
                    {
                      "code": 200,
                      "message": "OK",
                      "data": {
                        "content": [
                          {
                            "seq": 1001,
                            "korTitle": "테스트 영화",
                            "engTitle": "Test Film",
                            "director": "테스트 감독",
                            "productionYear": "2024",
                            "genreName": "드라마",
                            "imagePath": "poster.jpg"
                          }
                        ]
                      }
                    }
                """.trimIndent()
            )
        )

        val result = repository.fetchMovieCatalog(keyword = "테스트")

        assertTrue(result.isSuccess)
        assertEquals(
            MovieCatalogItem(
                id = "1001",
                title = "테스트 영화",
                releaseYear = 2024,
                director = "테스트 감독",
                genre = "드라마",
                englishTitle = "Test Film",
                imagePath = "poster.jpg"
            ),
            result.getOrThrow().single()
        )
    }

    @Test
    fun fetchMovieCatalogPageMapsPaginationMetadataAndRequestsZeroBasedPage() = runBlocking {
        val apiService = FakeApiService(
            moviesResponse = """
                {
                  "code": 200,
                  "message": "OK",
                  "data": {
                    "content": [
                      {
                        "seq": 1001,
                        "korTitle": "테스트 영화",
                        "engTitle": "Test Film",
                        "director": "테스트 감독",
                        "productionYear": "2024",
                        "genreName": "드라마",
                        "imagePath": "https://filmo-api.log8.kr/api/movies/image/poster_png"
                      }
                    ],
                    "last": false
                  }
                }
            """.trimIndent()
        )
        val repository = RemoteAppRepository(apiService)

        val result = repository.fetchMovieCatalogPage(page = 0, size = 20)

        assertTrue(result.isSuccess)
        assertEquals(0, apiService.requestedMoviePage)
        assertEquals(20, apiService.requestedMovieSize)
        assertEquals(true, result.getOrThrow().hasMore)
        assertEquals(
            "https://filmo-api.log8.kr/api/movies/image/poster_png",
            result.getOrThrow().items.single().imagePath
        )
    }

    @Test
    fun fetchMovieCatalogPageLogsRawImagePathFromResponse() = runBlocking {
        val tree = CapturingTimberTree()
        Timber.plant(tree)
        val rawImagePath = "fileFolder/ed1f4847-cdb9-43f2-9823-80de68d7571c_jpg"
        val repository = RemoteAppRepository(
            FakeApiService(
                moviesResponse = """
                    {
                      "code": 200,
                      "message": "OK",
                      "data": {
                        "content": [
                          {
                            "seq": 1001,
                            "korTitle": "테스트 영화",
                            "engTitle": "Test Film",
                            "director": "테스트 감독",
                            "productionYear": "2024",
                            "genreName": "드라마",
                            "imagePath": "$rawImagePath"
                          }
                        ],
                        "last": true
                      }
                    }
                """.trimIndent()
            )
        )

        val result = repository.fetchMovieCatalogPage(page = 1, size = 20)

        assertTrue(result.isSuccess)
        assertTrue(
            tree.messages.any { message ->
                message.contains("rawImagePath=$rawImagePath")
            }
        )
    }

    @Test
    fun fetchMovieDetailMapsSwaggerMovieDetailResponse() = runBlocking {
        val repository = RemoteAppRepository(
            FakeApiService(
                movieDetailResponse = """
                    {
                      "code": 200,
                      "message": "OK",
                      "data": {
                        "seq": 1001,
                        "korTitle": "테스트 영화",
                        "engTitle": "Test Film",
                        "director": "테스트 감독",
                        "actors": "배우 A",
                        "productionYear": "2024",
                        "genreName": "드라마",
                        "companyNm": "제작사",
                        "distributorNm": "배급사",
                        "imagePath": "poster.jpg",
                        "duration": "100분",
                        "rating": "12세이상관람가",
                        "colorType": "Color",
                        "synopsis": "시놉시스",
                        "screenwriter": "각본가",
                        "producer": "프로듀서",
                        "releaseDate": "2024-01-01",
                        "keywords": "독립"
                      }
                    }
                """.trimIndent()
            )
        )

        val result = repository.fetchMovieDetail("1001")

        assertTrue(result.isSuccess)
        assertEquals("테스트 영화", result.getOrThrow().title)
        assertEquals("배우 A", result.getOrThrow().actors)
        assertEquals("시놉시스", result.getOrThrow().synopsis)
    }

    @Test
    fun fetchTicketCollectionMapsMyTicketsAndSavedTicketsFromSwaggerEndpoints() = runBlocking {
        val repository = RemoteAppRepository(
            FakeApiService(
                ticketsResponse = """
                    {
                      "code": 200,
                      "message": "OK",
                      "data": [
                        {
                          "id": 101,
                          "movieSeq": 1001,
                          "watchedDate": "2026-05-16",
                          "watchedTime": "19:30",
                          "rating": 4,
                          "review": "작고 단단한 영화였어요",
                          "showYn": true,
                          "createdAt": "2026-05-16T13:51:52.335Z",
                          "updatedAt": "2026-05-16T13:51:52.335Z"
                        }
                      ]
                    }
                """.trimIndent(),
                collectionsResponse = """
                    {
                      "code": 200,
                      "message": "OK",
                      "data": [
                        {
                          "id": 999,
                          "ticketId": 201,
                          "movieSeq": 1002,
                          "watchedDate": "2026-05-17",
                          "watchedTime": "20:00",
                          "rating": 5,
                          "review": "저장해 두고 다시 보고 싶은 티켓",
                          "showYn": true,
                          "createdAt": "2026-05-17T13:51:52.335Z",
                          "updatedAt": "2026-05-17T13:51:52.335Z",
                          "ownerNickname": "다른관객"
                        }
                      ]
                    }
                """.trimIndent(),
                movieDetailResponse = """
                    {
                      "code": 200,
                      "message": "OK",
                      "data": {
                        "seq": 1001,
                        "korTitle": "테스트 영화",
                        "engTitle": "Test Film",
                        "director": "테스트 감독",
                        "productionYear": "2024",
                        "genreName": "드라마",
                        "imagePath": "poster.jpg"
                      }
                    }
                """.trimIndent()
            )
        )

        val result = repository.fetchTicketCollection()

        assertTrue(result.isSuccess)
        val collection = result.getOrThrow()
        assertEquals(
            MovieTicket(
                id = "101",
                movieTitle = "테스트 영화",
                theaterName = "",
                watchedDate = "2026-05-16",
                rating = 4,
                review = "작고 단단한 영화였어요",
                ownedByMe = true,
                savedByMe = false,
                posterImagePath = "poster.jpg",
                genre = "드라마",
                director = "테스트 감독",
                releaseYear = 2024
            ),
            collection.myTickets.single()
        )
        assertEquals(
            MovieTicket(
                id = "201",
                movieTitle = "테스트 영화",
                theaterName = "",
                watchedDate = "2026-05-17",
                rating = 5,
                review = "저장해 두고 다시 보고 싶은 티켓",
                ownedByMe = false,
                savedByMe = true,
                posterImagePath = "poster.jpg",
                genre = "드라마",
                director = "테스트 감독",
                releaseYear = 2024
            ),
            collection.savedTickets.single()
        )
    }

    @Test
    fun fetchTicketDetailUsesTicketEndpointForOwnedTicketAndMapsLikeState() = runBlocking {
        val apiService = FakeApiService(
            ticketDetailResponse = """
                {
                  "code": 200,
                  "message": "OK",
                  "data": {
                    "id": 101,
                    "movieSeq": 1001,
                    "watchedDate": "2026-05-17",
                    "watchedTime": "20:00",
                    "rating": 5,
                    "review": "상세 좋아요 상태",
                    "likeCount": 4,
                    "liked": true
                  }
                }
            """.trimIndent(),
            movieDetailResponse = """
                {
                  "code": 200,
                  "message": "OK",
                  "data": {
                    "seq": 1001,
                    "korTitle": "테스트 영화",
                    "director": "테스트 감독",
                    "productionYear": "2024",
                    "genreName": "드라마"
                  }
                }
            """.trimIndent()
        )
        val repository = RemoteAppRepository(apiService)

        val result = repository.fetchTicketDetail(ticketId = "101", ownedByMe = true)

        assertTrue(result.isSuccess)
        assertEquals(101L, apiService.requestedTicketDetailId)
        assertEquals(null, apiService.requestedCollectionDetailId)
        assertEquals(true, result.getOrThrow().liked)
        assertEquals(4, result.getOrThrow().likeCount)
    }

    @Test
    fun fetchTicketDetailUsesCollectionEndpointForSavedTicketAndMapsLikeState() = runBlocking {
        val apiService = FakeApiService(
            collectionDetailResponse = """
                {
                  "code": 200,
                  "message": "OK",
                  "data": {
                    "id": 201,
                    "movieSeq": 1001,
                    "watchedDate": "2026-05-17",
                    "watchedTime": "20:00",
                    "rating": 5,
                    "review": "저장 상세 좋아요 상태",
                    "likeCount": 9,
                    "liked": true
                  }
                }
            """.trimIndent(),
            movieDetailResponse = """
                {
                  "code": 200,
                  "message": "OK",
                  "data": {
                    "seq": 1001,
                    "korTitle": "테스트 영화",
                    "director": "테스트 감독",
                    "productionYear": "2024",
                    "genreName": "드라마"
                  }
                }
            """.trimIndent()
        )
        val repository = RemoteAppRepository(apiService)

        val result = repository.fetchTicketDetail(ticketId = "201", ownedByMe = false)

        assertTrue(result.isSuccess)
        assertEquals(null, apiService.requestedTicketDetailId)
        assertEquals(201L, apiService.requestedCollectionDetailId)
        assertEquals(true, result.getOrThrow().liked)
        assertEquals(9, result.getOrThrow().likeCount)
    }

    @Test
    fun fetchPublicTicketsMapsPublicTicketResponseAndRequestsLatestSort() = runBlocking {
        val apiService = FakeApiService(
            publicTicketsResponse = """
                {
                  "code": 200,
                  "message": "OK",
                  "data": [
                    {
                      "id": 201,
                      "movieSeq": 1001,
                      "watchedDate": "2026-05-16",
                      "watchedTime": "19:30",
                      "rating": 3,
                      "review": "작고 단단한 영화였어요",
                      "showYn": true,
                      "likeCount": 7,
                      "liked": true,
                      "createdAt": "2026-05-16T13:51:52.335Z",
                      "updatedAt": "2026-05-16T13:51:52.335Z"
                    }
                  ]
                }
            """.trimIndent(),
            movieDetailResponse = """
                {
                  "code": 200,
                  "message": "OK",
                  "data": {
                    "seq": 1001,
                    "korTitle": "테스트 영화",
                    "engTitle": "Test Film",
                    "director": "테스트 감독",
                    "productionYear": "2024",
                    "genreName": "드라마",
                    "imagePath": "poster.jpg",
                    "duration": "100분"
                  }
                }
            """.trimIndent()
        )
        val repository = RemoteAppRepository(apiService)

        val result = repository.fetchPublicTickets()

        assertTrue(result.isSuccess)
        assertEquals("latest", apiService.requestedPublicTicketSort)
        assertEquals(
            PublicTicket(
                id = "201",
                movieSeq = "1001",
                movieTitle = "테스트 영화",
                genre = "드라마",
                director = "테스트 감독",
                releaseYear = 2024,
                duration = "100분",
                posterImagePath = "poster.jpg",
                theaterName = "",
                watchedDate = "2026-05-16",
                watchedTime = "19:30",
                rating = 3,
                ownerNickname = "",
                review = "작고 단단한 영화였어요",
                liked = true,
                likeCount = 7
            ),
            result.getOrThrow().single()
        )
    }

    @Test
    fun setTicketLikedCallsPostWhenLikedAndDeleteWhenUnliked() = runBlocking {
        val apiService = FakeApiService()
        val repository = RemoteAppRepository(apiService)

        val likeResult = repository.setTicketLiked(ticketId = "101", liked = true)
        val unlikeResult = repository.setTicketLiked(ticketId = "101", liked = false)

        assertTrue(likeResult.isSuccess)
        assertTrue(unlikeResult.isSuccess)
        assertEquals(101L, apiService.likedTicketId)
        assertEquals(101L, apiService.unlikedTicketId)
    }

    @Test
    fun createTicketPostsSwaggerTicketRequestAndReturnsCreatedTicketId() = runBlocking {
        val apiService = FakeApiService(
            createTicketResponse = """{"code":200,"message":"OK","data":"101"}"""
        )
        val repository = RemoteAppRepository(apiService)

        val result = repository.createTicket(
            CreateTicketRequest(
                movieId = "1001",
                watchedDate = "2026-05-16",
                watchedTime = "00:00",
                rating = 4,
                review = "작고 단단한 영화였어요"
            )
        )

        assertTrue(result.isSuccess)
        assertEquals("101", result.getOrThrow())
        assertEquals(
            """{"movieSeq":1001,"watchedDate":"2026-05-16","watchedTime":"00:00","rating":4,"review":"작고 단단한 영화였어요"}""",
            apiService.createdTicketBody
        )
    }

    @Test
    fun createTicketFallsBackToMyTicketsWhenSwaggerCreateResponseHasNullData() = runBlocking {
        val apiService = FakeApiService(
            createTicketResponse = """{"code":200,"message":"OK","data":null}""",
            ticketsResponse = """
                {
                  "code": 200,
                  "message": "OK",
                  "data": [
                    {
                      "id": 101,
                      "movieSeq": 1001,
                      "watchedDate": "2026-05-16",
                      "watchedTime": "00:00",
                      "rating": 4,
                      "review": "작고 단단한 영화였어요",
                      "showYn": false,
                      "createdAt": "2026-05-16T13:51:52.335Z",
                      "updatedAt": "2026-05-16T13:51:52.335Z"
                    }
                  ]
                }
            """.trimIndent()
        )
        val repository = RemoteAppRepository(apiService)

        val result = repository.createTicket(
            CreateTicketRequest(
                movieId = "1001",
                watchedDate = "2026-05-16",
                watchedTime = "00:00",
                rating = 4,
                review = "작고 단단한 영화였어요"
            )
        )

        assertTrue(result.isSuccess)
        assertEquals("101", result.getOrThrow())
    }

    @Test
    fun createTicketFallsBackToLatestMovieTicketWhenExactCreatedTicketFieldsDiffer() = runBlocking {
        val apiService = FakeApiService(
            createTicketResponse = """{"code":200,"message":"OK","data":null}""",
            ticketsResponse = """
                {
                  "code": 200,
                  "message": "OK",
                  "data": [
                    {
                      "id": 100,
                      "movieSeq": 9999,
                      "watchedDate": "2026-05-15",
                      "watchedTime": "00:00",
                      "rating": 5,
                      "review": "다른 티켓",
                      "createdAt": "2026-05-15T13:51:52.335Z"
                    },
                    {
                      "id": 101,
                      "movieSeq": 1001,
                      "watchedDate": "2026-05-16",
                      "watchedTime": "19:30",
                      "rating": 4,
                      "review": "서버에서 달라진 값",
                      "createdAt": "2026-05-16T13:51:52.335Z"
                    }
                  ]
                }
            """.trimIndent()
        )
        val repository = RemoteAppRepository(apiService)

        val result = repository.createTicket(
            CreateTicketRequest(
                movieId = "1001",
                watchedDate = "2026-05-16",
                watchedTime = "00:00",
                rating = 4,
                review = "작고 단단한 영화였어요"
            )
        )

        assertTrue(result.isSuccess)
        assertEquals("101", result.getOrThrow())
    }

    @Test
    fun createTicketSucceedsWithNullIdWhenCreatedTicketCannotBeResolved() = runBlocking {
        val apiService = FakeApiService(
            createTicketResponse = """{"code":200,"message":"OK","data":null}""",
            ticketsResponse = """{"code":200,"message":"OK","data":[]}"""
        )
        val repository = RemoteAppRepository(apiService)

        val result = repository.createTicket(
            CreateTicketRequest(
                movieId = "1001",
                watchedDate = "2026-05-16",
                watchedTime = "00:00",
                rating = 4,
                review = "작고 단단한 영화였어요"
            )
        )

        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrThrow())
    }

    @Test
    fun updateTicketShareCallsSwaggerShareEndpointWithPublicQuery() = runBlocking {
        val apiService = FakeApiService()
        val repository = RemoteAppRepository(apiService)

        val result = repository.updateTicketShare(ticketId = "101", isPublic = true)

        assertTrue(result.isSuccess)
        assertEquals(101L, apiService.sharedTicketId)
        assertEquals(true, apiService.sharedTicketShowYn)
    }

    @Test
    fun updateMyTicketPatchesSwaggerTicketRequestAndMapsUpdatedTicket() = runBlocking {
        val apiService = FakeApiService(
            ticketDetailResponse = """
                {
                  "code": 200,
                  "message": "OK",
                  "data": {
                    "id": 101,
                    "movieSeq": 1001,
                    "watchedDate": "2026-05-17",
                    "watchedTime": "20:00",
                    "rating": 5,
                    "review": "수정한 감상평",
                    "showYn": true,
                    "createdAt": "2026-05-16T13:51:52.335Z",
                    "updatedAt": "2026-05-17T13:51:52.335Z",
                    "likeCount": 0,
                    "comments": [],
                    "liked": false,
                    "collected": false
                  }
                }
            """.trimIndent(),
            movieDetailResponse = """
                {
                  "code": 200,
                  "message": "OK",
                  "data": {
                    "seq": 1001,
                    "korTitle": "테스트 영화",
                    "engTitle": "Test Film",
                    "director": "테스트 감독",
                    "productionYear": "2024",
                    "genreName": "드라마",
                    "imagePath": "poster.jpg"
                  }
                }
            """.trimIndent()
        )
        val repository = RemoteAppRepository(apiService)

        val result = repository.updateMyTicket(
            UpdateTicketRequest(
                ticketId = "101",
                watchedDate = "2026-05-17",
                watchedTime = "20:00",
                rating = 5,
                review = "수정한 감상평"
            )
        )

        assertTrue(result.isSuccess)
        assertEquals(101L, apiService.updatedTicketId)
        assertEquals(
            """{"watchedDate":"2026-05-17","watchedTime":"20:00","rating":5,"review":"수정한 감상평"}""",
            apiService.updatedTicketBody
        )
        assertEquals(
            MovieTicket(
                id = "101",
                movieTitle = "테스트 영화",
                theaterName = "",
                watchedDate = "2026-05-17",
                rating = 5,
                review = "수정한 감상평",
                ownedByMe = true,
                savedByMe = false,
                posterImagePath = "poster.jpg",
                genre = "드라마",
                director = "테스트 감독",
                releaseYear = 2024
            ),
            result.getOrThrow()
        )
    }

    @Test
    fun deleteMyTicketCallsSwaggerDeleteTicketEndpoint() = runBlocking {
        val apiService = FakeApiService()
        val repository = RemoteAppRepository(apiService)

        val result = repository.deleteMyTicket("101")

        assertTrue(result.isSuccess)
        assertEquals(101L, apiService.deletedTicketId)
    }

    @Test
    fun removeSavedTicketCallsSwaggerDeleteCollectionEndpoint() = runBlocking {
        val apiService = FakeApiService()
        val repository = RemoteAppRepository(apiService)

        val result = repository.removeSavedTicket("201")

        assertTrue(result.isSuccess)
        assertEquals(201L, apiService.removedCollectionTicketId)
    }

    private class FakeApiService(
        private val signupResponse: String = """{"data":{"accessToken":"signup-token"}}""",
        private val loginResponse: String = """{"data":{"accessToken":"login-token"}}""",
        private val randomNicknameResponse: String = """{"data":{"nickname":"랜덤닉네임"}}""",
        private val meResponse: String = """{"data":{"loginId":"user1","nickname":"로그인닉네임"}}""",
        private val moviesResponse: String = """{"data":{"content":[]}}""",
        private val movieDetailResponse: String = """{"data":{}}""",
        private val ticketDetailResponse: String = """{"data":{}}""",
        private val ticketsResponse: String = """{"data":[]}""",
        private val collectionsResponse: String = """{"data":[]}""",
        private val collectionDetailResponse: String = """{"data":{}}""",
        private val publicTicketsResponse: String = """{"data":[]}""",
        private val createTicketResponse: String = """{"code":200,"message":"OK","data":null}"""
    ) : ApiService {
        var signupBody: String? = null
            private set
        var loginBody: String? = null
            private set
        var meAuthorization: String? = null
            private set
        var requestedMoviePage: Int? = null
            private set
        var requestedMovieSize: Int? = null
            private set
        var createdTicketBody: String? = null
            private set
        var requestedPublicTicketSort: String? = null
            private set
        var updatedTicketId: Long? = null
            private set
        var updatedTicketBody: String? = null
            private set
        var deletedTicketId: Long? = null
            private set
        var removedCollectionTicketId: Long? = null
            private set
        var requestedTicketDetailId: Long? = null
            private set
        var requestedCollectionDetailId: Long? = null
            private set
        var sharedTicketId: Long? = null
            private set
        var sharedTicketShowYn: Boolean? = null
            private set
        var likedTicketId: Long? = null
            private set
        var unlikedTicketId: Long? = null
            private set

        override suspend fun ping(): ResponseBody = """{"data":null}""".toResponseBody()

        override suspend fun signup(body: RequestBody): ResponseBody {
            val buffer = Buffer()
            body.writeTo(buffer)
            signupBody = buffer.readUtf8()
            return signupResponse.toResponseBody()
        }

        override suspend fun login(body: RequestBody): ResponseBody =
            loginResponse.also {
                val buffer = Buffer()
                body.writeTo(buffer)
                loginBody = buffer.readUtf8()
            }.toResponseBody()

        override suspend fun fetchRandomNickname(): ResponseBody =
            randomNicknameResponse.toResponseBody()

        override suspend fun fetchMe(authorization: String?): ResponseBody {
            meAuthorization = authorization
            return meResponse.toResponseBody()
        }

        override suspend fun fetchMovies(
            keyword: String?,
            page: Int,
            size: Int,
            sort: List<String>?
        ): ResponseBody {
            requestedMoviePage = page
            requestedMovieSize = size
            return moviesResponse.toResponseBody()
        }

        override suspend fun fetchMovie(seq: Long): ResponseBody =
            movieDetailResponse.toResponseBody()

        override suspend fun fetchMovieImage(imagePath: String): ResponseBody =
            """image""".toResponseBody()

        override suspend fun fetchTickets(): ResponseBody =
            ticketsResponse.toResponseBody()

        override suspend fun fetchTicket(ticketId: Long): ResponseBody {
            requestedTicketDetailId = ticketId
            return ticketDetailResponse.toResponseBody()
        }

        override suspend fun fetchCollections(): ResponseBody =
            collectionsResponse.toResponseBody()

        override suspend fun fetchCollection(ticketId: Long): ResponseBody {
            requestedCollectionDetailId = ticketId
            return collectionDetailResponse.toResponseBody()
        }

        override suspend fun fetchPublicTickets(sort: String): ResponseBody {
            requestedPublicTicketSort = sort
            return publicTicketsResponse.toResponseBody()
        }

        override suspend fun createTicket(body: RequestBody): ResponseBody {
            val buffer = Buffer()
            body.writeTo(buffer)
            createdTicketBody = buffer.readUtf8()
            return createTicketResponse.toResponseBody()
        }

        override suspend fun updateTicketShare(ticketId: Long, showYn: Boolean): ResponseBody {
            sharedTicketId = ticketId
            sharedTicketShowYn = showYn
            return """{"code":200,"message":"OK","data":"OK"}""".toResponseBody()
        }

        override suspend fun updateTicket(ticketId: Long, body: RequestBody): ResponseBody {
            updatedTicketId = ticketId
            val buffer = Buffer()
            body.writeTo(buffer)
            updatedTicketBody = buffer.readUtf8()
            return """{"code":200,"message":"OK","data":"OK"}""".toResponseBody()
        }

        override suspend fun deleteTicket(ticketId: Long): ResponseBody {
            deletedTicketId = ticketId
            return """{"code":200,"message":"OK","data":"OK"}""".toResponseBody()
        }

        override suspend fun removeCollection(ticketId: Long): ResponseBody {
            removedCollectionTicketId = ticketId
            return """{"code":200,"message":"OK","data":"OK"}""".toResponseBody()
        }

        override suspend fun addLike(ticketId: Long): ResponseBody {
            likedTicketId = ticketId
            return """{"code":200,"message":"OK","data":"OK"}""".toResponseBody()
        }

        override suspend fun removeLike(ticketId: Long): ResponseBody {
            unlikedTicketId = ticketId
            return """{"code":200,"message":"OK","data":"OK"}""".toResponseBody()
        }
    }

    private class CapturingTimberTree : Timber.Tree() {
        val messages = mutableListOf<String>()

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            messages += message
        }
    }
}
