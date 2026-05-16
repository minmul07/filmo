package com.filmo.service

import kotlinx.coroutines.runBlocking
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteAppRepositoryTest {
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
    fun fetchTheatersMapsSwaggerTheaterPageResponse() = runBlocking {
        val repository = RemoteAppRepository(
            FakeApiService(
                theatersResponse = """
                    {
                      "code": 200,
                      "message": "OK",
                      "data": {
                        "content": [
                          {
                            "theaCd": "T001",
                            "theaName": "인디스페이스",
                            "scrnName": "1관",
                            "screenGb": "독립영화관",
                            "address": "서울",
                            "phone": "02-000-0000",
                            "homepage": "https://indiespace.kr",
                            "seatCount": 100,
                            "naverMapUrl": "https://map.naver.com"
                          }
                        ]
                      }
                    }
                """.trimIndent()
            )
        )

        val result = repository.fetchTheaters(keyword = "인디")

        assertTrue(result.isSuccess)
        assertEquals("T001", result.getOrThrow().single().id)
        assertEquals("인디스페이스", result.getOrThrow().single().name)
        assertEquals(100, result.getOrThrow().single().seatCount)
    }

    @Test
    fun fetchTheaterMapsSwaggerTheaterResponse() = runBlocking {
        val repository = RemoteAppRepository(
            FakeApiService(
                theaterResponse = """
                    {
                      "code": 200,
                      "message": "OK",
                      "data": {
                        "theaCd": "T001",
                        "theaName": "인디스페이스",
                        "scrnName": "1관",
                        "screenGb": "독립영화관",
                        "address": "서울",
                        "phone": "02-000-0000",
                        "homepage": "https://indiespace.kr",
                        "seatCount": 100,
                        "naverMapUrl": "https://map.naver.com"
                      }
                    }
                """.trimIndent()
            )
        )

        val result = repository.fetchTheater("T001")

        assertTrue(result.isSuccess)
        assertEquals("인디스페이스", result.getOrThrow().name)
        assertEquals("https://map.naver.com", result.getOrThrow().naverMapUrl)
    }

    private class FakeApiService(
        private val moviesResponse: String = """{"data":{"content":[]}}""",
        private val movieDetailResponse: String = """{"data":{}}""",
        private val theatersResponse: String = """{"data":{"content":[]}}""",
        private val theaterResponse: String = """{"data":{}}"""
    ) : ApiService {
        override suspend fun ping(): ResponseBody = """{"data":null}""".toResponseBody()

        override suspend fun fetchItems(): ResponseBody = """{"data":[]}""".toResponseBody()

        override suspend fun submitItem(title: String, description: String): ResponseBody =
            """{"data":null}""".toResponseBody()

        override suspend fun login(body: RequestBody): ResponseBody =
            """{"data":{"accessToken":"token"}}""".toResponseBody()

        override suspend fun authTest(authorization: String): ResponseBody =
            """{"data":"ok"}""".toResponseBody()

        override suspend fun fetchMovies(
            keyword: String?,
            genre: String?,
            year: String?,
            page: Int,
            size: Int,
            sort: List<String>?
        ): ResponseBody = moviesResponse.toResponseBody()

        override suspend fun fetchMovie(seq: Long): ResponseBody =
            movieDetailResponse.toResponseBody()

        override suspend fun fetchMovieImage(imagePath: String): ResponseBody =
            """image""".toResponseBody()

        override suspend fun fetchTheaters(
            keyword: String?,
            page: Int,
            size: Int,
            sort: List<String>?
        ): ResponseBody = theatersResponse.toResponseBody()

        override suspend fun fetchTheater(theaCd: String): ResponseBody =
            theaterResponse.toResponseBody()
    }
}
