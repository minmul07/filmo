package com.filmo.ui.movie

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filmo.ui.theme.FilmoTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterMovieScreen(
    viewModel: RegisterMovieViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RegisterMovieContent(
        uiState = uiState,
        modifier = modifier,
        onTitleChange = viewModel::updateTitle,
        onReleaseDateChange = viewModel::updateReleaseDateMillis,
        onGenreChange = viewModel::updateGenre,
        onDirectorChange = viewModel::updateDirector,
        onCastChange = viewModel::updateCast,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterMovieContent(
    uiState: RegisterMovieUiState,
    modifier: Modifier = Modifier,
    onTitleChange: (String) -> Unit = {},
    onReleaseDateChange: (Long?) -> Unit = {},
    onGenreChange: (String) -> Unit = {},
    onDirectorChange: (String) -> Unit = {},
    onCastChange: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var isDatePickerOpen by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.releaseDateMillis
    )

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "영화 등록",
                style = MaterialTheme.typography.headlineMedium
            )
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("영화 제목") },
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.releaseDateMillis.toDateText(),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("날짜") },
                readOnly = true,
                singleLine = true
            )
            Button(
                onClick = { isDatePickerOpen = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("날짜 선택")
            }
            OutlinedTextField(
                value = uiState.genre,
                onValueChange = onGenreChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("장르") },
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.director,
                onValueChange = onDirectorChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("감독") },
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.cast,
                onValueChange = onCastChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("출연") },
                minLines = 2
            )
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("뒤로")
            }
        }
    }

    if (isDatePickerOpen) {
        DatePickerDialog(
            onDismissRequest = { isDatePickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReleaseDateChange(datePickerState.selectedDateMillis)
                        isDatePickerOpen = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerOpen = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun Long?.toDateText(): String {
    return this?.let { millis ->
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
    }.orEmpty()
}

@Preview(showBackground = true)
@Composable
private fun RegisterMovieContentPreview() {
    FilmoTheme {
        RegisterMovieContent(
            uiState = RegisterMovieUiState(
                title = "괴물",
                releaseDateMillis = 1_609_459_200_000L,
                genre = "드라마",
                director = "봉준호",
                cast = "송강호, 변희봉"
            )
        )
    }
}
