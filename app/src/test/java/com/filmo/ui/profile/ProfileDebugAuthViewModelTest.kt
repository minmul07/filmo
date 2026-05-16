package com.filmo.ui.profile

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileDebugAuthViewModelTest {
    @Test
    fun runAuthTestUsesSelectedCredentialAndStoresSuccess() = runBlocking {
        val authClient = FakeDebugAuthTestClient(
            result = Result.success(
                DebugAuthTestResult(
                    loginId = "user2",
                    authData = "3"
                )
            )
        )
        val viewModel = ProfileDebugAuthViewModel(authClient)

        viewModel.runAuthTest(DebugAuthTestCredential("user2", "password2"))

        assertEquals(DebugAuthTestCredential("user2", "password2"), authClient.lastCredential)
        assertEquals(
            DebugAuthTestRequestState.Success("auth-test data: 3"),
            viewModel.uiState.value.requestStates["user2"]
        )
    }

    @Test
    fun runAuthTestStoresRecoverableErrorMessageOnFailure() = runBlocking {
        val viewModel = ProfileDebugAuthViewModel(
            FakeDebugAuthTestClient(Result.failure(IllegalStateException("boom")))
        )

        viewModel.runAuthTest(DebugAuthTestCredential("user1", "password1"))

        val state = viewModel.uiState.value.requestStates["user1"]
        assertTrue(state is DebugAuthTestRequestState.Error)
        assertEquals(
            "요청에 실패했어요. 서버 상태와 계정을 확인한 뒤 다시 시도해 주세요.",
            (state as DebugAuthTestRequestState.Error).message
        )
    }

    private class FakeDebugAuthTestClient(
        private val result: Result<DebugAuthTestResult>
    ) : DebugAuthTestClient {
        var lastCredential: DebugAuthTestCredential? = null
            private set

        override suspend fun runAuthTest(
            credential: DebugAuthTestCredential
        ): Result<DebugAuthTestResult> {
            lastCredential = credential
            return result
        }
    }
}
