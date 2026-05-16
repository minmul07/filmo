package com.filmo.ui.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ProfileDebugAuthViewModel @Inject constructor(
    private val authTestClient: DebugAuthTestClient
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileDebugAuthUiState())
    val uiState: StateFlow<ProfileDebugAuthUiState> = _uiState.asStateFlow()

    suspend fun runAuthTest(credential: DebugAuthTestCredential) {
        _uiState.update {
            it.copy(
                requestStates = it.requestStates + (
                    credential.loginId to DebugAuthTestRequestState.Loading
                    )
            )
        }

        val result = authTestClient.runAuthTest(credential)
        _uiState.update { state ->
            state.copy(
                requestStates = state.requestStates + (
                    credential.loginId to result.fold(
                        onSuccess = { DebugAuthTestRequestState.Success("auth-test data: ${it.authData}") },
                        onFailure = {
                            DebugAuthTestRequestState.Error(
                                "요청에 실패했어요. 서버 상태와 계정을 확인한 뒤 다시 시도해 주세요."
                            )
                        }
                    )
                    )
            )
        }
    }
}

interface DebugAuthTestClient {
    suspend fun runAuthTest(credential: DebugAuthTestCredential): Result<DebugAuthTestResult>
}

data class DebugAuthTestCredential(
    val loginId: String,
    val password: String
)

data class DebugAuthTestResult(
    val loginId: String,
    val authData: String
)

data class ProfileDebugAuthUiState(
    val requestStates: Map<String, DebugAuthTestRequestState> = emptyMap()
)

sealed interface DebugAuthTestRequestState {
    data object Idle : DebugAuthTestRequestState
    data object Loading : DebugAuthTestRequestState
    data class Success(val message: String) : DebugAuthTestRequestState
    data class Error(val message: String) : DebugAuthTestRequestState
}

val DebugAuthTestCredentials = listOf(
    DebugAuthTestCredential("user1", "password1"),
    DebugAuthTestCredential("user2", "password2"),
    DebugAuthTestCredential("user3", "password3")
)
