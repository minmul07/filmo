package com.filmo.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.filmo.ui.main.MainUiState
import com.filmo.ui.theme.FilmoTheme

@Composable
fun HomeScreen(
    mainUiState: MainUiState,
    modifier: Modifier = Modifier,
    onNavigateToSetting: () -> Unit = {},
    onPingClick: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = mainUiState.homeMessage,
                style = MaterialTheme.typography.headlineMedium
            )
            Button(
                onClick = onNavigateToSetting,
                modifier = Modifier.widthIn(min = ButtonMinWidth)
            ) {
                Text(text = "1 -> 2")
            }
            Button(
                onClick = onPingClick,
                enabled = !mainUiState.isPingLoading,
                modifier = Modifier.widthIn(min = ButtonMinWidth)
            ) {
                Text(
                    text = if (mainUiState.isPingLoading) {
                        "Ping 보내는 중..."
                    } else {
                        "서버 Ping"
                    }
                )
            }
            mainUiState.pingMessage?.let { pingMessage ->
                Text(
                    text = pingMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private val ButtonMinWidth = 160.dp

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    FilmoTheme {
        HomeScreen(
            mainUiState = MainUiState()
        )
    }
}
