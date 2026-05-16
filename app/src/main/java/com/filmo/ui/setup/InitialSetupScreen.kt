package com.filmo.ui.setup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    onAutoNicknameClick: () -> Unit,
    onManualNicknameClick: () -> Unit,
    onRegenerateNicknameClick: () -> Unit,
    onBackToEntryChoice: () -> Unit,
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

        AnonymousProfileSetupScreen(
            uiState = uiState,
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
                modifier = modifier
            )

            InitialSetupStep.AnonymousProfile -> AnonymousProfileSetupScreen(
                uiState = uiState,
                onNicknameChange = onNicknameChange,
                onRegenerateNicknameClick = onRegenerateNicknameClick,
                onBackToEntryChoice = onBackToEntryChoice,
                onSaveClick = onSaveClick,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun InitialSetupEntryChoiceScreen(
    onAutoNicknameClick: () -> Unit,
    onManualNicknameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Filmo",
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "당신의 영화 여정을 기록하세요",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(76.dp))

            SetupChoiceCard(
                title = "자동 닉네임 생성",
                description = "랜덤 닉네임으로 빠르게 시작하기",
                onClick = onAutoNicknameClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            SetupChoiceCard(
                title = "닉네임 직접 입력",
                description = "나만의 닉네임 만들기",
                onClick = onManualNicknameClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun SetupChoiceCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(128.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AnonymousProfileSetupScreen(
    uiState: InitialSetupUiState,
    onNicknameChange: (String) -> Unit,
    onRegenerateNicknameClick: () -> Unit,
    onBackToEntryChoice: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = !uiState.isSaving && !uiState.isCompleted) {
        onBackToEntryChoice()
    }

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

        if (uiState.shouldShowRegenerateButton) {
            OutlinedButton(
                onClick = onRegenerateNicknameClick,
                enabled = !uiState.isSaving
            ) {
                Text(text = "다시 만들기")
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onSaveClick,
            modifier = Modifier.widthIn(min = 160.dp),
            enabled = uiState.canSave
        ) {
            Text(text = if (uiState.isSaving) "저장 중..." else "저장")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InitialSetupScreenPreview() {
    FilmoTheme {
        InitialSetupScreen(
            uiState = InitialSetupUiState(nickname = "User1234"),
            onAutoNicknameClick = {},
            onManualNicknameClick = {},
            onRegenerateNicknameClick = {},
            onBackToEntryChoice = {},
            onNicknameChange = {},
            onSaveClick = {},
            onFinished = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnonymousProfileSetupScreenPreview() {
    FilmoTheme {
        AnonymousProfileSetupScreen(
            uiState = InitialSetupUiState(nickname = "User1234").toAutomaticNickname(),
            onNicknameChange = {},
            onRegenerateNicknameClick = {},
            onBackToEntryChoice = {},
            onSaveClick = {}
        )
    }
}

@Composable
private fun InitialSetupCompletedPopup(
    uiState: InitialSetupUiState
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(38.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "프로필 생성 완료!",
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = uiState.completionMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InitialSetupCompletedScreenPreview() {
    FilmoTheme {
        InitialSetupCompletedPopup(
            uiState = InitialSetupUiState(nickname = "시네필여행자965").toSaved()
        )
    }
}
