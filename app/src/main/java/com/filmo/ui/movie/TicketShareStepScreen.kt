package com.filmo.ui.movie

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
internal fun TicketShareStep(
    uiState: RegisterMovieUiState
) {
    StepContent(
        action = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                ErrorText(errorMessage = uiState.errorMessage)
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            MovieShareTicket(
                uiState = uiState,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MovieShareTicket(
    uiState: RegisterMovieUiState,
    modifier: Modifier = Modifier
) {
    val title = uiState.title.ifBlank { uiState.selectedMovie?.title.orEmpty() }.ifBlank { "영화 제목" }
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
    val watchedDate = uiState.releaseDateMillis.toWatchedDateWithYearText().ifBlank { "관람일" }
    val ratingText = uiState.rating?.let { "$it/5점" } ?: "-/5점"
    val ticketShape = remember {
        TicketShape( // 티켓 모서리 컷아웃
            cornerCutout = 20.dp,
            sideNotchRadius = 20.dp,
            perforationFraction = TicketPerforationFraction
        )
    }

    Surface(
        modifier = modifier
            .aspectRatio(TicketAspectRatio)
            .semantics(mergeDescendants = true) {
                contentDescription = "$title 티켓, 별점 $ratingText, 관람일 $watchedDate"
        },
        shape = ticketShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(TicketImageFraction)
                ) {
                    MoviePosterImage(
                        imageUrl = posterUrl,
                        title = title,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(0.dp),
                        showBorder = false
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = metaText.ifBlank { "독립영화" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                TicketDetailArea(
                    ratingText = ratingText,
                    watchedDate = watchedDate,
                    review = uiState.review,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f - TicketImageFraction)
                )
            }
            TicketPerforationLine(
                modifier = Modifier.fillMaxSize(),
                fraction = TicketPerforationFraction
            )
        }
    }
}

@Composable
private fun TicketDetailArea(
    ratingText: String,
    watchedDate: String,
    review: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(start = 16.dp, top = 40.dp, end = 16.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            TicketInfoBlock(
                label = "별점",
                value = ratingText,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = RatingSelectedColor
                    )
                },
                modifier = Modifier.width(56.dp)
            )
            TicketInfoBlock(
                label = "관람일",
                value = watchedDate,
                modifier = Modifier.width(131.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "관람 후기",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = review.ifBlank { "남긴 관람 후기가 없어요." },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TicketInfoBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.invoke()
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TicketPerforationLine(
    fraction: Float,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    Canvas(modifier = modifier) {
        val y = size.height * fraction
        drawLine(
            color = lineColor,
            start = androidx.compose.ui.geometry.Offset(42.dp.toPx(), y),
            end = androidx.compose.ui.geometry.Offset(size.width - 42.dp.toPx(), y),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18.dp.toPx(), 14.dp.toPx()))
        )
    }
}

private class TicketShape(
    private val cornerCutout: Dp,
    private val sideNotchRadius: Dp,
    private val perforationFraction: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val corner = with(density) { cornerCutout.toPx() }.coerceAtMost(size.minDimension / 6f)
        val notch = with(density) { sideNotchRadius.toPx() }.coerceAtMost(size.minDimension / 6f)
        val perforationY = (size.height * perforationFraction).coerceIn(
            corner + notch,
            size.height - corner - notch
        )
        val path = Path().apply {
            moveTo(corner, 0f)
            lineTo(size.width - corner, 0f)
            arcTo(
                rect = Rect(
                    left = size.width - corner,
                    top = -corner,
                    right = size.width + corner,
                    bottom = corner
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )
            lineTo(size.width, perforationY - notch)
            arcTo(
                rect = Rect(
                    left = size.width - notch,
                    top = perforationY - notch,
                    right = size.width + notch,
                    bottom = perforationY + notch
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(size.width, size.height - corner)
            arcTo(
                rect = Rect(
                    left = size.width - corner,
                    top = size.height - corner,
                    right = size.width + corner,
                    bottom = size.height + corner
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )
            lineTo(corner, size.height)
            arcTo(
                rect = Rect(
                    left = -corner,
                    top = size.height - corner,
                    right = corner,
                    bottom = size.height + corner
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )
            lineTo(0f, perforationY + notch)
            arcTo(
                rect = Rect(
                    left = -notch,
                    top = perforationY - notch,
                    right = notch,
                    bottom = perforationY + notch
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(0f, corner)
            arcTo(
                rect = Rect(
                    left = -corner,
                    top = -corner,
                    right = corner,
                    bottom = corner
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )
            close()
        }
        return Outline.Generic(path)
    }
}

private const val TicketAspectRatio = 328f / 572f
private const val TicketImageFraction = 382f / 572f
private const val TicketPerforationFraction = 402f / 572f
