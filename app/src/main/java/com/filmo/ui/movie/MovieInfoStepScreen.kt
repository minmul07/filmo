package com.filmo.ui.movie

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun MovieInfoStep(
    uiState: RegisterMovieUiState,
    errorMessage: String?,
    posterImageCache: MoviePosterBitmapSessionCache,
    onReleaseDateClick: () -> Unit,
    onRatingChange: (Int) -> Unit,
    onReviewChange: (String) -> Unit,
    onNext: () -> Unit
) {
    StepContent(
        action = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            ) {
                ErrorText(errorMessage = errorMessage)
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "완료",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    ) {
        MovieInfoSummary(
            uiState = uiState,
            posterImageCache = posterImageCache
        )
        FormSectionTitle(text = "관람일")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateValueBox(
                text = uiState.releaseDateMillis.toWatchedYearText(),
                placeholder = "연도",
                onClick = onReleaseDateClick,
                modifier = Modifier.weight(1f)
            )
            DateValueBox(
                text = uiState.releaseDateMillis.toWatchedDateText(),
                placeholder = "날짜",
                onClick = onReleaseDateClick,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = "별점",
            style = MaterialTheme.typography.titleMedium
        )
        RatingSelector(
            rating = uiState.rating,
            onRatingChange = onRatingChange
        )
        FormSectionTitle(text = "관람 후기")
        ReviewTextField(
            value = uiState.review,
            onValueChange = onReviewChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
        )
    }
}

@Composable
private fun MovieInfoSummary(
    uiState: RegisterMovieUiState,
    posterImageCache: MoviePosterBitmapSessionCache,
    modifier: Modifier = Modifier
) {
    val title = uiState.title.ifBlank { uiState.selectedMovie?.title.orEmpty() }
    val posterUrl = remember(
        uiState.selectedMovie?.imagePath,
        uiState.selectedMovieDetail?.imagePath
    ) {
        uiState.selectedMovieDetail?.imagePath
            ?.takeIf { it.isNotBlank() }
            ?.toMovieImageUrl()
            ?: uiState.selectedMovie?.imageUrl().orEmpty()
    }
    val releaseYear = uiState.selectedMovieDetail?.releaseYear
        ?.takeIf { it > 0 }
        ?: uiState.selectedMovie?.releaseYear
    val metaText = listOfNotNull(
        uiState.genre.takeIf { it.isNotBlank() },
        uiState.director.takeIf { it.isNotBlank() }?.let { "$it 감독" },
        releaseYear?.takeIf { it > 0 }?.let { "${it}년" },
        uiState.selectedMovieDetail?.duration?.takeIf { it.isNotBlank() }
    ).joinToString(" · ")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FormSectionTitle(text = "영화")
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            MoviePosterImage(
                imageUrl = posterUrl,
                title = title.ifBlank { "선택한 영화" },
                imageCache = posterImageCache,
                modifier = Modifier
                    .width(142.dp)
                    .height(200.dp),
                shape = RoundedCornerShape(0.dp),
                showBorder = false
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = metaText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (uiState.isMovieDetailLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun FormSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun DateValueBox(
    text: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.ifBlank { placeholder },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RatingSelector(
    rating: Int?,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { score ->
            val selected = rating != null && score <= rating
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .clickable(onClick = { onRatingChange(score) })
                    .semantics { role = Role.Button },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "$score 점",
                    modifier = Modifier.size(24.dp),
                    tint = if (selected) {
                        RatingSelectedColor
                    } else {
                        RatingUnselectedColor
                    }
                )
            }
        }
    }
}

@Composable
private fun ReviewTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxSize(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    if (value.isBlank()) {
                        Text(
                            text = "내용 입력 공백 포함 최대 100자",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}
