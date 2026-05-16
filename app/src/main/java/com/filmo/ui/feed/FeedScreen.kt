package com.filmo.ui.feed

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filmo.service.PublicTicket
import com.filmo.ui.movie.MoviePosterImage
import com.filmo.ui.movie.rememberMoviePosterBitmapSessionCache
import com.filmo.ui.movie.toMovieImageUrl
import com.filmo.ui.theme.FilmoTheme
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.loadPublicTickets()
    }

    FeedContent(
        uiState = uiState,
        modifier = modifier,
        onRetryClick = {
            coroutineScope.launch {
                viewModel.loadPublicTickets()
            }
        }
    )
}

@Composable
private fun FeedContent(
    uiState: FeedUiState,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val posterImageCache = rememberMoviePosterBitmapSessionCache()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 18.dp,
                top = 28.dp,
                end = 18.dp,
                bottom = 24.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item(
                span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }
            ) {
                FeedHeader()
            }

            if (uiState.errorMessage != null && uiState.tickets.isNotEmpty()) {
                item(
                    span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }
                ) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            when {
                uiState.isLoading -> item(
                    span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                uiState.errorMessage != null && uiState.tickets.isEmpty() -> item(
                    span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }
                ) {
                    FeedPlaceholder(
                        title = "공개 티켓을 불러오지 못했어요",
                        body = "잠시 후 다시 시도해 주세요.",
                        action = {
                            OutlinedButton(onClick = onRetryClick) {
                                Text("다시 시도")
                            }
                        }
                    )
                }

                uiState.tickets.isEmpty() -> item(
                    span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }
                ) {
                    FeedPlaceholder(
                        title = "아직 공개된 티켓이 없어요",
                        body = "다른 사용자의 독립영화 기록이 곧 표시됩니다."
                    )
                }

                else -> items(
                    items = uiState.tickets,
                    key = { it.feedKey },
                    contentType = { "public-ticket" }
                ) { ticket ->
                    PublicTicketCard(
                        ticket = ticket,
                        posterImageCache = posterImageCache
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedHeader() {
    Column(
        modifier = Modifier.padding(bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "공개 티켓",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "다른 사람들이 공유한 독립영화 기록",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PublicTicketCard(
    ticket: PublicTicket,
    posterImageCache: com.filmo.ui.movie.MoviePosterBitmapSessionCache,
    modifier: Modifier = Modifier
) {
    val posterUrl = remember(ticket.posterImagePath) {
        ticket.posterImagePath.toMovieImageUrl()
    }
    val metaText = remember(
        ticket.genre,
        ticket.director,
        ticket.releaseYear,
        ticket.duration
    ) {
        listOfNotNull(
            ticket.genre.takeIf { it.isNotBlank() },
            ticket.director.takeIf { it.isNotBlank() }?.let { "$it 감독" },
            ticket.releaseYear.takeIf { it > 0 }?.let { "${it}년" },
            ticket.duration.takeIf { it.isNotBlank() }
        ).joinToString(" · ")
    }
    val watchedText = remember(ticket.watchedDate, ticket.watchedTime) {
        listOf(ticket.watchedDate, ticket.watchedTime)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "관람일 미입력" }
    }
    val ticketShape = remember {
        FeedTicketShape(
            cornerCutout = 10.dp,
            sideNotchRadius = 10.dp,
            perforationFraction = FeedTicketPerforationFraction
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(FeedTicketAspectRatio)
            .semantics(mergeDescendants = true) {
                contentDescription = "${ticket.movieTitle} 공개 티켓, $watchedText, ${ticket.theaterName}"
            },
        shape = ticketShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(FeedTicketPerforationFraction)
                ) {
                    MoviePosterImage(
                        imageUrl = posterUrl,
                        title = ticket.movieTitle,
                        imageCache = posterImageCache,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(0.dp),
                        showBorder = false
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = ticket.movieTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = metaText.ifBlank { "독립영화" },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                PublicTicketDetailArea(
                    theaterName = ticket.theaterName.ifBlank { "영화관 미입력" },
                    watchedText = watchedText,
                    review = ticket.review,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f - FeedTicketPerforationFraction)
                )
            }
            FeedTicketPerforationLine(
                modifier = Modifier.fillMaxSize(),
                fraction = FeedTicketPerforationFraction
            )
        }
    }
}

@Composable
private fun PublicTicketDetailArea(
    theaterName: String,
    watchedText: String,
    review: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 10.dp, top = 22.dp, end = 10.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FeedTicketInfoBlock(
                icon = Icons.Filled.LocationOn,
                label = "영화관",
                value = theaterName,
                modifier = Modifier.weight(1f)
            )
            FeedTicketInfoBlock(
                label = "관람일",
                value = watchedText,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = review.ifBlank { "남긴 관람 후기가 없어요." },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FeedTicketInfoBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FeedTicketPerforationLine(
    fraction: Float,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    Canvas(modifier = modifier) {
        val y = size.height * fraction
        drawLine(
            color = lineColor,
            start = androidx.compose.ui.geometry.Offset(22.dp.toPx(), y),
            end = androidx.compose.ui.geometry.Offset(size.width - 22.dp.toPx(), y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 7.dp.toPx()))
        )
    }
}

private class FeedTicketShape(
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

@Composable
private fun FeedPlaceholder(
    title: String,
    body: String,
    action: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = body,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (action != null) {
                Box(modifier = Modifier.padding(top = 18.dp)) {
                    action()
                }
            }
        }
    }
}

private val PublicTicket.feedKey: String
    get() = listOf(id, movieSeq, watchedDate, watchedTime, theaterName)
        .filter { it.isNotBlank() }
        .joinToString("-")

private const val FeedTicketAspectRatio = 0.57f
private const val FeedTicketPerforationFraction = 0.69f

@Preview(showBackground = true)
@Composable
private fun FeedContentPreview() {
    FilmoTheme {
        FeedContent(
            uiState = FeedUiState(
                tickets = listOf(
                    PublicTicket(
                        id = "preview-1",
                        movieSeq = "1001",
                        movieTitle = "테스트 영화",
                        genre = "드라마",
                        director = "테스트 감독",
                        releaseYear = 2024,
                        duration = "100분",
                        posterImagePath = "",
                        theaterName = "인디스페이스",
                        watchedDate = "2026-05-16",
                        watchedTime = "19:30",
                        review = "작고 단단한 영화였어요"
                    ),
                    PublicTicket(
                        id = "preview-2",
                        movieSeq = "1002",
                        movieTitle = "윤희에게",
                        genre = "드라마",
                        director = "임대형",
                        releaseYear = 2019,
                        duration = "105분",
                        posterImagePath = "",
                        theaterName = "아트나인",
                        watchedDate = "2026-05-15",
                        watchedTime = "",
                        review = "겨울 공기가 오래 남았다."
                    )
                )
            ),
            onRetryClick = {}
        )
    }
}
