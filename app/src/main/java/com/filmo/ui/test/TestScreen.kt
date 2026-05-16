package com.filmo.ui.test

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.filmo.ui.theme.FilmoTheme

@Composable
fun TestScreen(
    modifier: Modifier = Modifier,
    onResetToHome: () -> Unit = {},
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
                text = "3. Test",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(
                onClick = onResetToHome,
                modifier = Modifier.widthIn(min = ButtonMinWidth)
            ) {
                Text(text = "3 -> 1")
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.widthIn(min = ButtonMinWidth)
            ) {
                Text(text = "3 -> 2")
            }
        }
    }
}

private val ButtonMinWidth = 160.dp

@Preview(showBackground = true)
@Composable
private fun TestScreenPreview() {
    FilmoTheme {
        TestScreen()
    }
}
