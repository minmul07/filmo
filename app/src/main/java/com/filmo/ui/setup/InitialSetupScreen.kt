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
        when (uiState.step) {
            InitialSetupStep.EntryChoice -> InitialSetupEntryChoiceScreen(
                onAutoNicknameClick = onAutoNicknameClick,
                onManualNicknameClick = onManualNicknameClick,
                onLoginClick = onLoginClick,
                modifier = modifier
            )

            InitialSetupStep.Signup -> AuthFormScreen(
                uiState = uiState,
                title = "계정 만들기",
                description = "아이디와 비밀번호는 4자리 이상 입력해 주세요.",
                submitText = if (uiState.isSaving) "가입 중..." else "시작하기",
                onLoginIdChange = onLoginIdChange,
                onPasswordChange = onPasswordChange,
                onNicknameChange = onNicknameChange,
                onRegenerateNicknameClick = onRegenerateNicknameClick,
                onBackToEntryChoice = onBackToEntryChoice,
                onSaveClick = onSaveClick,
                modifier = modifier
            )

            InitialSetupStep.Login -> AuthFormScreen(
                uiState = uiState,
                title = "로그인",
                description = "이미 만든 계정으로 독립영화 티켓북을 이어가세요.",
                submitText = if (uiState.isSaving) "로그인 중..." else "로그인",
                onLoginIdChange = onLoginIdChange,
                onPasswordChange = onPasswordChange,
                onNicknameChange = onNicknameChange,
                onRegenerateNicknameClick = onRegenerateNicknameClick,
                onBackToEntryChoice = onBackToEntryChoice,
                onSaveClick = onSaveClick,
                modifier = modifier
            )
        }
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
