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
    fun fetchMovieCatalogPageMapsPaginationMetadataAndRequestsOneBasedPage() = runBlocking {
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

        val result = repository.fetchMovieCatalogPage(page = 1, size = 20)

        assertTrue(result.isSuccess)
        assertEquals(1, apiService.requestedMoviePage)
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
    fun fetchTicketCollectionReturnsMyTicketsWhenSavedTicketEndpointIsMissing() = runBlocking {
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
                          "cinema": "인디스페이스",
                          "review": "작고 단단한 영화였어요",
                          "showYn": true,
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
                        "genreName": "드라마"
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
                theaterName = "인디스페이스",
                watchedDate = "2026-05-16",
                rating = 0,
                review = "작고 단단한 영화였어요",
                ownedByMe = true,
                savedByMe = false
            ),
            collection.myTickets.single()
        )
        assertTrue(collection.savedTickets.isEmpty())
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
                      "cinema": "인디스페이스",
                      "review": "작고 단단한 영화였어요",
                      "ownerNickname": "독립영화user1234",
                      "showYn": true,
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
                theaterName = "인디스페이스",
                watchedDate = "2026-05-16",
                watchedTime = "19:30",
                rating = 3,
                ownerNickname = "독립영화user1234",
                review = "작고 단단한 영화였어요"
            ),
            result.getOrThrow().single()
        )
    }

    @Test
    fun createTicketPostsSwaggerTicketRequest() = runBlocking {
        val apiService = FakeApiService()
        val repository = RemoteAppRepository(apiService)

        val result = repository.createTicket(
            CreateTicketRequest(
                movieId = "1001",
                watchedDate = "2026-05-16",
                watchedTime = "00:00",
                cinema = "인디스페이스",
                review = "작고 단단한 영화였어요"
            )
        )

        assertTrue(result.isSuccess)
        assertEquals(
            """{"movieSeq":1001,"watchedDate":"2026-05-16","watchedTime":"00:00","cinema":"인디스페이스","review":"작고 단단한 영화였어요"}""",
            apiService.createdTicketBody
        )
    }

    private class FakeApiService(
        private val signupResponse: String = """{"data":{"accessToken":"signup-token"}}""",
        private val loginResponse: String = """{"data":{"accessToken":"login-token"}}""",
        private val randomNicknameResponse: String = """{"data":{"nickname":"랜덤닉네임"}}""",
        private val meResponse: String = """{"data":{"loginId":"user1","nickname":"로그인닉네임"}}""",
        private val moviesResponse: String = """{"data":{"content":[]}}""",
        private val movieDetailResponse: String = """{"data":{}}""",
        private val ticketsResponse: String = """{"data":[]}""",
        private val publicTicketsResponse: String = """{"data":[]}"""
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

        override suspend fun ping(): ResponseBody = """{"data":null}""".toResponseBody()

        override suspend fun fetchItems(): ResponseBody = """{"data":[]}""".toResponseBody()

        override suspend fun submitItem(title: String, description: String): ResponseBody =
            """{"data":null}""".toResponseBody()

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
            genre: String?,
            year: String?,
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

        override suspend fun fetchPublicTickets(sort: String): ResponseBody {
            requestedPublicTicketSort = sort
            return publicTicketsResponse.toResponseBody()
        }

        override suspend fun createTicket(body: RequestBody): ResponseBody {
            val buffer = Buffer()
            body.writeTo(buffer)
            createdTicketBody = buffer.readUtf8()
            return """{"code":200,"message":"OK","data":null}""".toResponseBody()
        }
    }

    private class CapturingTimberTree : Timber.Tree() {
        val messages = mutableListOf<String>()

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            messages += message
        }
    }
}
