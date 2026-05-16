package com.filmo.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.filmo.ui.theme.FilmoTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Filmo",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        CircularProgressIndicator()
    }
}

@Composable
fun InitialSetupScreen(
    uiState: InitialSetupUiState,
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

        InitialSetupCompletedScreen(modifier = modifier)
    } else {
        AnonymousProfileSetupScreen(
            uiState = uiState,
            onNicknameChange = onNicknameChange,
            onSaveClick = onSaveClick,
            modifier = modifier
        )
    }
}

@Composable
private fun AnonymousProfileSetupScreen(
    uiState: InitialSetupUiState,
    onNicknameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "익명 프로필 생성",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "독립영화 취향을 공유할 닉네임을 설정해 주세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.nickname,
            onValueChange = onNicknameChange,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth(),
            enabled = !uiState.isSaving,
            singleLine = true,
            isError = uiState.hasSaveError,
            label = {
                Text(text = "닉네임")
            },
            supportingText = {
                if (uiState.hasSaveError) {
                    Text(text = "닉네임 저장에 실패했습니다. 다시 시도해 주세요.")
                }
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSaveClick,
            modifier = Modifier.widthIn(min = 160.dp),
            enabled = uiState.canSave
        ) {
            Text(text = if (uiState.isSaving) "저장 중..." else "저장")
        }
    }
}

@Composable
private fun InitialSetupCompletedScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "완료되었습니다!",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InitialSetupScreenPreview() {
    FilmoTheme {
        InitialSetupScreen(
            uiState = InitialSetupUiState(nickname = "User1234"),
            onNicknameChange = {},
            onSaveClick = {},
            onFinished = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InitialSetupCompletedScreenPreview() {
    FilmoTheme {
        InitialSetupCompletedScreen()
    }
}
