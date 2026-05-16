package com.filmo.ui.setup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.filmo.ui.theme.FilmoTheme

@Composable
internal fun AuthFormScreen(
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
    modifier: Modifier = Modifier,
    showBackToEntryChoice: Boolean = true
) {
    BackHandler(enabled = showBackToEntryChoice && !uiState.isSaving && !uiState.isCompleted) {
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

        if (showBackToEntryChoice) {
            TextButton(
                onClick = onBackToEntryChoice,
                enabled = !uiState.isSaving
            ) {
                Text(text = "처음으로")
            }
        }
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
