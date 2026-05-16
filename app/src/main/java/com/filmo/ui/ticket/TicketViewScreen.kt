package com.filmo.ui.ticket

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.filmo.ui.movie.toMovieImageUrl
import com.filmo.ui.theme.FilmoTheme
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun TicketViewScreen(
    viewModel: TicketViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.loadPublicTickets()
    }

    TicketViewContent(
        uiState = uiState,
        modifier = modifier,
        onRetryClick = {
            coroutineScope.launch {
                viewModel.loadPublicTickets()
            }
        },
        onTicketLikeClick = { ticketId ->
            coroutineScope.launch {
                viewModel.toggleTicketLike(ticketId)
            }
        }
    )
}

@Composable
private fun TicketViewContent(
    uiState: TicketViewUiState,
    onRetryClick: () -> Unit,
    onTicketLikeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TicketViewTopBar()

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (uiState.errorMessage != null && uiState.tickets.isNotEmpty()) {
                item {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            when {
                uiState.isLoading -> item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                uiState.errorMessage != null && uiState.tickets.isEmpty() -> item {
                    TicketViewPlaceholder(
                        title = "공개 티켓을 불러오지 못했어요",
                        body = "잠시 후 다시 시도해 주세요.",
                        action = {
                            OutlinedButton(onClick = onRetryClick) {
                                Text("다시 시도")
                            }
                        }
                    )
                }

                uiState.tickets.isEmpty() -> item {
                    TicketViewPlaceholder(
                        title = "아직 공개된 티켓이 없어요",
                        body = "다른 사용자의 독립영화 기록이 곧 표시됩니다."
                    )
                }

                else -> items(
                    items = uiState.tickets,
                    key = { it.ticketViewKey },
                    contentType = { "public-ticket" }
                ) { ticket ->
                    PublicTicketCard(
                        ticket = ticket,
                        onLikeClick = { onTicketLikeClick(ticket.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketViewTopBar(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "티켓보기",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun PublicTicketCard(
    ticket: PublicTicket,
    onLikeClick: () -> Unit,
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
    val watchedDateText = remember(ticket.watchedDate) {
        ticket.watchedDate.toTicketViewWatchedDateText()
    }
    val ownerNickname = ticket.ownerNickname.ifBlank {
        "익명 시네필"
    }
    val ticketShape = remember {
        TicketViewTicketShape(
            cornerCutout = 10.dp,
            sideNotchRadius = 10.dp,
            perforationFraction = TicketViewTicketPerforationFraction
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(TicketViewTicketAspectRatio)
            .semantics(mergeDescendants = true) {
                contentDescription = "${ownerNickname}님의 ${ticket.movieTitle} 공개 티켓, 별점 ${ticket.rating}/5점, 관람일 $watchedDateText"
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
                        .weight(TicketViewTicketPerforationFraction)
                ) {
                    MoviePosterImage(
                        imageUrl = posterUrl,
                        title = ticket.movieTitle,
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ticket.movieTitle,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            TicketViewLikeButton(
                                liked = ticket.liked,
                                likeCount = ticket.likeCount,
                                onClick = onLikeClick
                            )
                        }
                        Text(
                            text = metaText.ifBlank { "독립영화" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                PublicTicketDetailArea(
                    rating = ticket.rating,
                    watchedDateText = watchedDateText,
                    ownerNickname = ownerNickname,
                    review = ticket.review,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f - TicketViewTicketPerforationFraction)
                )
            }
            TicketViewTicketPerforationLine(
                modifier = Modifier.fillMaxSize(),
                fraction = TicketViewTicketPerforationFraction
            )
        }
    }
}

@Composable
private fun TicketViewLikeButton(
    liked: Boolean,
    likeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = likeCount.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1
        )
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (liked) "좋아요 취소" else "좋아요",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PublicTicketDetailArea(
    rating: Int,
    watchedDateText: String,
    ownerNickname: String,
    review: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 16.dp, top = 34.dp, end = 16.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.Top
        ) {
            TicketViewTicketInfoBlock(
                icon = Icons.Filled.Star,
                label = "별점",
                value = "${rating.coerceIn(0, 5)}/5점",
                modifier = Modifier.size(width = 56.dp, height = 42.dp)
            )
            TicketViewTicketInfoBlock(
                label = "관람일",
                value = watchedDateText,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "관람 후기",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = ownerNickname,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = review.ifBlank { "남긴 관람 후기가 없어요." },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TicketViewTicketInfoBlock(
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
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TicketViewTicketPerforationLine(
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

private class TicketViewTicketShape(
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
private fun TicketViewPlaceholder(
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

private val PublicTicket.ticketViewKey: String
    get() = listOf(id, movieSeq, watchedDate, watchedTime, theaterName)
        .filter { it.isNotBlank() }
        .joinToString("-")

private fun String.toTicketViewWatchedDateText(): String {
    if (isBlank()) return "관람일 미입력"

    return runCatching {
        val date = LocalDate.parse(this)
        val dayOfWeek = KoreanDayOfWeekLabels[date.dayOfWeek.value - 1]
        "%04d년 %02d월 %02d일 (%s)".format(
            date.year,
            date.monthValue,
            date.dayOfMonth,
            dayOfWeek
        )
    }.getOrElse {
        this
    }
}

private val KoreanDayOfWeekLabels = listOf("월", "화", "수", "목", "금", "토", "일")

private const val TicketViewTicketAspectRatio = 328f / 624f
private const val TicketViewTicketPerforationFraction = 402f / 624f

@Preview(showBackground = true)
@Composable
private fun TicketViewContentPreview() {
    FilmoTheme {
        TicketViewContent(
            uiState = TicketViewUiState(
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
            onRetryClick = {},
            onTicketLikeClick = {}
        )
    }
}
