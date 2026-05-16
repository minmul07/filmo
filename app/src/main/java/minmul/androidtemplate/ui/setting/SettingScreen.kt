package minmul.androidtemplate.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import minmul.androidtemplate.ui.main.MainUiState

@Composable
fun SettingScreen(
    mainUiState: MainUiState,
    modifier: Modifier = Modifier,
    onNavigateToTest: () -> Unit = {},
    onBack: () -> Unit = {}
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
                text = "2. Setting",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = mainUiState.homeMessage,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = onNavigateToTest,
                modifier = Modifier.widthIn(min = ButtonMinWidth)
            ) {
                Text(text = "2 -> 3")
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.widthIn(min = ButtonMinWidth)
            ) {
                Text(text = "2 -> 1")
            }
        }
    }
}

private val ButtonMinWidth = 160.dp
