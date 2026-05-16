package com.filmo.service

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class MockAppRepositoryTest {
    @Test
    @Ignore("Signup is intentionally unused in the login-only MVP.")
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

}
