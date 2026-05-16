package com.filmo.ui.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.filmo.ui.theme.FilmoTheme
import kotlinx.coroutines.delay

@Composable
fun InitialSetupScreen(
    uiState: InitialSetupUiState,
    onAutoNicknameClick: () -> Unit,
    onManualNicknameClick: () -> Unit,
    onRegenerateNicknameClick: () -> Unit,
    onBackToEntryChoice: () -> Unit,
    onLoginClick: () -> Unit,
    onLoginIdChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnFinished by rememberUpdatedState(onFinished)

    if (uiState.isCompleted) {
        LaunchedEffect(uiState.isCompleted) {
            delay(1_000L)
            currentOnFinished()
        }

        AuthFormScreen(
            uiState = uiState,
            title = "처리 완료",
            description = "잠시 후 메인 화면으로 이동합니다.",
            submitText = "완료",
            onLoginIdChange = onLoginIdChange,
            onPasswordChange = onPasswordChange,
            onNicknameChange = onNicknameChange,
            onRegenerateNicknameClick = onRegenerateNicknameClick,
            onBackToEntryChoice = onBackToEntryChoice,
            onSaveClick = onSaveClick,
            modifier = modifier
        )

        InitialSetupCompletedPopup(uiState = uiState)
    } else {
        // Signup and entry-choice pages are intentionally unused in the login-only MVP.
        AuthFormScreen(
            uiState = uiState.copy(
                nickname = "",
                step = InitialSetupStep.Login,
                isAutomaticNickname = false,
                isNicknameLoading = false,
                hasNicknameLoadError = false
            ),
            title = "로그인",
            description = "이미 만든 계정으로 독립영화 티켓북을 이어가세요.",
            submitText = if (uiState.isSaving) "로그인 중..." else "로그인",
            onLoginIdChange = onLoginIdChange,
            onPasswordChange = onPasswordChange,
            onNicknameChange = onNicknameChange,
            onRegenerateNicknameClick = onRegenerateNicknameClick,
            onBackToEntryChoice = onBackToEntryChoice,
            onSaveClick = onSaveClick,
            showBackToEntryChoice = false,
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InitialSetupScreenPreview() {
    FilmoTheme {
        InitialSetupScreen(
            uiState = InitialSetupUiState(),
            onAutoNicknameClick = {},
            onManualNicknameClick = {},
            onRegenerateNicknameClick = {},
            onBackToEntryChoice = {},
            onLoginClick = {},
            onLoginIdChange = {},
            onPasswordChange = {},
            onNicknameChange = {},
            onSaveClick = {},
            onFinished = {}
        )
    }
}
