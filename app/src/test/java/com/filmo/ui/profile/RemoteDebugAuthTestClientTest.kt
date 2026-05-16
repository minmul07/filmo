package com.filmo.ui.profile

import com.filmo.service.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteDebugAuthTestClientTest {
    @Test
    fun runAuthTestLogsInAndSendsBearerAccessToken() = runBlocking {
        val apiService = FakeApiService()
        val client = RemoteDebugAuthTestClient(apiService)

        val result = client.runAuthTest(DebugAuthTestCredential("user3", "password3"))

        assertTrue(result.isSuccess)
        assertEquals("""{"loginId":"user3","password":"password3"}""", apiService.loginBody)
        assertEquals("Bearer access-token-3", apiService.authorization)
        assertEquals("3", result.getOrThrow().authData)
    }

    private class FakeApiService : ApiService {
        var loginBody: String? = null
            private set
        var authorization: String? = null
            private set

        override suspend fun ping(): ResponseBody {
            error("Not needed in this test")
        }

        override suspend fun fetchItems(): ResponseBody {
            error("Not needed in this test")
        }

        override suspend fun submitItem(
            title: String,
            description: String
        ): ResponseBody {
            error("Not needed in this test")
        }

        override suspend fun signup(body: RequestBody): ResponseBody {
            error("Not needed in this test")
        }

        override suspend fun login(body: RequestBody): ResponseBody {
            val buffer = Buffer()
            body.writeTo(buffer)
            loginBody = buffer.readUtf8()
            return """
                {
                  "code": 200,
                  "message": "success",
                  "data": {
                    "accessToken": "access-token-3"
                  }
                }
            """.trimIndent().toJsonResponseBody()
        }

        override suspend fun fetchRandomNickname(): ResponseBody {
            error("Not needed in this test")
        }

        override suspend fun fetchMe(authorization: String): ResponseBody {
            error("Not needed in this test")
        }

        override suspend fun authTest(authorization: String): ResponseBody {
            this.authorization = authorization
            return """
                {
                  "code": 200,
                  "message": "success",
                  "data": 3
                }
            """.trimIndent().toJsonResponseBody()
        }

        override suspend fun fetchMovies(
            keyword: String?,
            genre: String?,
            year: String?,
            page: Int,
            size: Int,
            sort: List<String>?
        ): ResponseBody {
            error("Not needed in this test")
        }

        override suspend fun fetchMovie(seq: Long): ResponseBody {
            error("Not needed in this test")
        }

        override suspend fun fetchMovieImage(imagePath: String): ResponseBody {
            error("Not needed in this test")
        }

        override suspend fun fetchTheaters(
            keyword: String?,
            page: Int,
            size: Int,
            sort: List<String>?
        ): ResponseBody {
            error("Not needed in this test")
        }

        override suspend fun fetchTheater(theaCd: String): ResponseBody {
            error("Not needed in this test")
        }

        override suspend fun saveTheater(theaCd: String): ResponseBody {
            error("Not needed in this test")
        }

        override suspend fun removeSavedTheater(theaCd: String): ResponseBody {
            error("Not needed in this test")
        }

        override suspend fun fetchTickets(): ResponseBody {
            error("Not needed in this test")
        }
    }

    private companion object {
        val JsonContentType = "application/json; charset=utf-8".toMediaType()

        fun String.toJsonResponseBody(): ResponseBody {
            return toResponseBody(JsonContentType)
        }
    }
}
