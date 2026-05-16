package com.filmo.ui.movie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.ZoneId

@Composable
internal fun ViewingInfoScreen(
    uiState: RegisterMovieUiState,
    posterImageCache: MoviePosterBitmapSessionCache,
    onBack: () -> Unit,
    onReleaseDateChange: (Long?) -> Unit,
    onRatingChange: (Int) -> Unit,
    onReviewChange: (String) -> Unit,
    onNext: () -> Unit
) {
    var isDatePickerOpen by remember { mutableStateOf(false) }
    val zoneId = remember { ZoneId.systemDefault() }
    var wheelDateSelection by remember {
        mutableStateOf(
            (uiState.releaseDateMillis ?: System.currentTimeMillis()).toWheelDateSelection(zoneId)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        RegisterMovieHeader(
            step = RegisterMovieStep.MovieInfo,
            onBack = onBack
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            MovieInfoStep(
                uiState = uiState,
                errorMessage = uiState.errorMessage,
                posterImageCache = posterImageCache,
                onReleaseDateClick = {
                    val selectedMillis = uiState.releaseDateMillis ?: System.currentTimeMillis()
                    wheelDateSelection = selectedMillis.toWheelDateSelection(zoneId)
                    isDatePickerOpen = true
                },
                onRatingChange = onRatingChange,
                onReviewChange = onReviewChange,
                onNext = onNext
            )
        }
    }

    if (isDatePickerOpen) {
        WheelDatePickerSheet(
            selection = wheelDateSelection,
            onSelectionChange = { wheelDateSelection = it },
            onDismissRequest = { isDatePickerOpen = false },
            onConfirm = {
                onReleaseDateChange(wheelDateSelection.toStartOfDayMillis(zoneId))
                isDatePickerOpen = false
            }
        )
    }
}
