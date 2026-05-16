package com.filmo.ui.setup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.filmo.ui.theme.FilmoTheme

@Composable
internal fun InitialSetupEntryChoiceScreen(
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

@Preview(showBackground = true)
@Composable
private fun InitialSetupEntryChoiceScreenPreview() {
    FilmoTheme {
        InitialSetupEntryChoiceScreen(
            onAutoNicknameClick = {},
            onManualNicknameClick = {},
            onLoginClick = {}
        )
    }
}
