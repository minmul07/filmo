package com.filmo.ui.setup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

@Composable
private fun InitialSetupEntryChoiceScreen(
    onAutoNicknameClick: () -> Unit,
    onManualNicknameClick: () -> Unit,
    onLoginClick: () -> Unit,
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
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.MovieCreation,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "독립영화 티켓북",
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

            Spacer(modifier = Modifier.height(72.dp))

            SetupChoiceCard(
                title = "자동 닉네임 생성",
                description = "랜덤 닉네임으로 빠르게 시작",
                onClick = onAutoNicknameClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            SetupChoiceCard(
                title = "닉네임 직접 입력",
                description = "나만의 닉네임으로 시작",
                onClick = onManualNicknameClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(42.dp))

            TextButton(onClick = onLoginClick) {
                Text(text = "이미 계정이 있어요 → 로그인")
            }
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AuthFormScreen(
    uiState: InitialSetupUiState,
    title: String,
    description: String,
    submitText: String,
    onLoginIdChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (uiState.step == InitialSetupStep.Login) {
                Icons.AutoMirrored.Outlined.Login
            } else {
                Icons.Outlined.MovieCreation
            },
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.loginId,
            onValueChange = onLoginIdChange,
            modifier = Modifier
                .widthIn(max = 340.dp)
                .fillMaxWidth(),
            enabled = !uiState.isSaving,
            singleLine = true,
            isError = uiState.loginId.isNotEmpty() && !uiState.isLoginIdValid,
            label = { Text(text = "아이디") },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Person, contentDescription = null)
            },
            supportingText = {
                if (uiState.loginId.isNotEmpty() && !uiState.isLoginIdValid) {
                    Text(text = "아이디는 4자리 이상 입력해 주세요.")
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            modifier = Modifier
                .widthIn(max = 340.dp)
                .fillMaxWidth(),
            enabled = !uiState.isSaving,
            singleLine = true,
            isError = uiState.password.isNotEmpty() && !uiState.isPasswordValid,
            label = { Text(text = "비밀번호") },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Lock, contentDescription = null)
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            supportingText = {
                if (uiState.password.isNotEmpty() && !uiState.isPasswordValid) {
                    Text(text = "비밀번호는 4자리 이상 입력해 주세요.")
                }
            }
        )

        if (uiState.step == InitialSetupStep.Signup) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.nickname,
                onValueChange = onNicknameChange,
                modifier = Modifier
                    .widthIn(max = 340.dp)
                    .fillMaxWidth(),
                enabled = !uiState.isSaving && !uiState.isNicknameLoading && !uiState.isAutomaticNickname,
                singleLine = true,
                isError = uiState.hasNicknameLoadError || (uiState.nickname.isNotEmpty() && !uiState.isNicknameValid),
                label = { Text(text = "닉네임") },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)
                },
                supportingText = {
                    when {
                        uiState.isNicknameLoading -> Text(text = "서버에서 랜덤 닉네임을 가져오는 중입니다.")
                        uiState.hasNicknameLoadError -> Text(text = "닉네임을 가져오지 못했습니다. 다시 시도해 주세요.")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.shouldShowRegenerateButton) {
            OutlinedButton(
                onClick = onRegenerateNicknameClick,
                enabled = !uiState.isSaving && !uiState.isNicknameLoading
            ) {
                Text(text = if (uiState.hasNicknameLoadError) "닉네임 다시 가져오기" else "다시 만들기")
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onSaveClick,
            modifier = Modifier.widthIn(min = 168.dp),
            enabled = uiState.canSave
        ) {
            Text(text = submitText)
        }

        if (uiState.hasSaveError) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (uiState.step == InitialSetupStep.Login) {
                    "로그인에 실패했습니다. 아이디와 비밀번호를 확인해 주세요."
                } else {
                    "가입에 실패했습니다. 입력값을 확인한 뒤 다시 시도해 주세요."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onBackToEntryChoice,
            enabled = !uiState.isSaving
        ) {
            Text(text = "처음으로")
        }
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
                    text = "완료!",
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

@Preview(showBackground = true)
@Composable
private fun SignupScreenPreview() {
    FilmoTheme {
        AuthFormScreen(
            uiState = InitialSetupUiState(
                loginId = "user1",
                password = "1234",
                nickname = "시네필여행자965",
                step = InitialSetupStep.Signup,
                isAutomaticNickname = true
            ),
            title = "계정 만들기",
            description = "아이디와 비밀번호는 4자리 이상 입력해 주세요.",
            submitText = "시작하기",
            onLoginIdChange = {},
            onPasswordChange = {},
            onNicknameChange = {},
            onRegenerateNicknameClick = {},
            onBackToEntryChoice = {},
            onSaveClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    FilmoTheme {
        AuthFormScreen(
            uiState = InitialSetupUiState(
                loginId = "user1",
                password = "1234",
                step = InitialSetupStep.Login
            ),
            title = "로그인",
            description = "이미 만든 계정으로 독립영화 티켓북을 이어가세요.",
            submitText = "로그인",
            onLoginIdChange = {},
            onPasswordChange = {},
            onNicknameChange = {},
            onRegenerateNicknameClick = {},
            onBackToEntryChoice = {},
            onSaveClick = {}
        )
    }
}
