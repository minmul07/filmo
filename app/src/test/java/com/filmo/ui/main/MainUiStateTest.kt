package com.filmo.ui.main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainUiStateTest {
    @Test
    fun pingLoadingMarksRequestInProgressAndClearsPreviousMessage() {
        val state = MainUiState(
            isPingLoading = false,
            pingMessage = "previous message"
        )

        val result = state.toPingLoading()

        assertTrue(result.isPingLoading)
        assertEquals(null, result.pingMessage)
    }

    @Test
    fun pingSuccessShowsServerResponse() {
        val state = MainUiState(isPingLoading = true)

        val result = state.toPingSuccess("pong")

        assertFalse(result.isPingLoading)
        assertEquals("서버 응답: pong", result.pingMessage)
    }

    @Test
    fun blankPingSuccessShowsFallbackMessage() {
        val state = MainUiState(isPingLoading = true)

        val result = state.toPingSuccess("")

        assertFalse(result.isPingLoading)
        assertEquals("서버 ping 성공", result.pingMessage)
    }

    @Test
    fun pingFailureShowsRecoverableMessage() {
        val state = MainUiState(isPingLoading = true)

        val result = state.toPingFailure()

        assertFalse(result.isPingLoading)
        assertEquals("서버 ping에 실패했습니다. 잠시 후 다시 시도해 주세요.", result.pingMessage)
    }
}
