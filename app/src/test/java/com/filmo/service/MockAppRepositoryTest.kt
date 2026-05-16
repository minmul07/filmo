package com.filmo.service

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockAppRepositoryTest {
    @Test
    fun signUpReturnsMockAccessTokenAfterMockDelay() = runBlocking {
        val repository = MockAppRepository()

        val result = repository.signUp(
            SignupRequest(
                loginId = "user1",
                password = "1234",
                nickname = "시네필"
            )
        )

        assertTrue(result.isSuccess)
        assertEquals("user1", result.getOrThrow().loginId)
        assertEquals("시네필", result.getOrThrow().nickname)
        assertTrue(result.getOrThrow().accessToken.isNotBlank())
    }

    @Test
    fun loginReturnsMockAccessTokenAfterMockDelay() = runBlocking {
        val repository = MockAppRepository()

        val result = repository.login(LoginRequest(loginId = "user1", password = "1234"))

        assertTrue(result.isSuccess)
        assertEquals("user1", result.getOrThrow().loginId)
        assertTrue(result.getOrThrow().nickname.isNotBlank())
        assertTrue(result.getOrThrow().accessToken.isNotBlank())
    }

    @Test
    fun fetchRandomNicknameReturnsServerLikeNicknameAfterMockDelay() = runBlocking {
        val repository = MockAppRepository()

        val result = repository.fetchRandomNickname()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isNotBlank())
    }

    @Test
    fun pingReturnsSuccessAfterMockDelay() = runBlocking {
        val repository = MockAppRepository()

        val startedAt = System.currentTimeMillis()
        val result = repository.ping()
        val elapsedMillis = System.currentTimeMillis() - startedAt

        assertTrue(result.isSuccess)
        assertTrue(elapsedMillis >= 250)
    }

    @Test
    fun fetchItemsReturnsTemplateItemsAfterMockDelay() = runBlocking {
        val repository = MockAppRepository()

        val startedAt = System.currentTimeMillis()
        val result = repository.fetchItems()
        val elapsedMillis = System.currentTimeMillis() - startedAt

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isNotEmpty())
        assertTrue(elapsedMillis >= 250)
    }

    @Test
    fun fetchMovieCatalogReturnsFourIndependentFilmsAfterMockDelay() = runBlocking {
        val repository = MockAppRepository()

        val startedAt = System.currentTimeMillis()
        val result = repository.fetchMovieCatalog()
        val elapsedMillis = System.currentTimeMillis() - startedAt

        assertTrue(result.isSuccess)
        assertEquals(
            listOf("헤어질 결심", "윤희에게", "벌새", "소공녀"),
            result.getOrThrow().map { it.title }
        )
        assertTrue(elapsedMillis >= 250)
    }

    @Test
    fun fetchMovieDetailReturnsCatalogDetailsAfterMockDelay() = runBlocking {
        val repository = MockAppRepository()

        val startedAt = System.currentTimeMillis()
        val result = repository.fetchMovieDetail("decision-to-leave")
        val elapsedMillis = System.currentTimeMillis() - startedAt

        assertTrue(result.isSuccess)
        assertEquals("헤어질 결심", result.getOrThrow().title)
        assertEquals("박찬욱", result.getOrThrow().director)
        assertTrue(elapsedMillis >= 250)
    }

    @Test
    fun fetchTheatersReturnsIndependentFilmTheatersAfterMockDelay() = runBlocking {
        val repository = MockAppRepository()

        val startedAt = System.currentTimeMillis()
        val result = repository.fetchTheaters()
        val elapsedMillis = System.currentTimeMillis() - startedAt

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().any { it.name == "인디스페이스" })
        assertTrue(result.getOrThrow().all { it.id.isNotBlank() })
        assertTrue(elapsedMillis >= 250)
    }

    @Test
    fun fetchTheaterReturnsMatchingTheaterAfterMockDelay() = runBlocking {
        val repository = MockAppRepository()

        val result = repository.fetchTheater("indiespace")

        assertTrue(result.isSuccess)
        assertEquals("인디스페이스", result.getOrThrow().name)
    }

    @Test
    fun submitItemReturnsCreatedItemAfterMockDelay() = runBlocking {
        val repository = MockAppRepository()
        val request = SampleItemRequest(
            title = "Mock title",
            description = "Mock description"
        )

        val startedAt = System.currentTimeMillis()
        val result = repository.submitItem(request)
        val elapsedMillis = System.currentTimeMillis() - startedAt

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().title == request.title)
        assertTrue(elapsedMillis >= 250)
    }
}
