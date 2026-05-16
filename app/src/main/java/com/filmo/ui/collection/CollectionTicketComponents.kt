package com.filmo.ui.collection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.filmo.service.MovieTicket
import com.filmo.ui.movie.MoviePosterImage
import com.filmo.ui.movie.toMovieImageUrl
import java.time.LocalDate

@Composable
internal fun TicketCard(
    ticket: MovieTicket,
    showOwnerActions: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "MOVIE TICKET",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = ticket.movieTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = ticket.theaterName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = ticket.watchedDate,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                RatingStars(
                    rating = ticket.rating,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = ticket.review,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (showOwnerActions) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onEditClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("수정")
                        }
                        Button(
                            onClick = onDeleteClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                contentColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("삭제")
                        }
                    }
                } else {
                    Button(
                        onClick = onDeleteClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Icon(Icons.Filled.Bookmark, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("저장 취소")
                    }
                }
            }
        }
    }
}

@Composable
internal fun CollectionDetailTicketCard(
    ticket: MovieTicket,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val posterUrl = remember(ticket.posterImagePath) {
        ticket.posterImagePath.toMovieImageUrl()
    }
    val watchedDateText = remember(ticket.watchedDate) {
        ticket.watchedDate.toCollectionDetailWatchedDateText()
    }
    val movieMetadataText = remember(ticket.genre, ticket.director, ticket.releaseYear, ticket.duration) {
        ticket.toCollectionDetailMovieMetadataText()
    }
    val ticketShape = remember {
        CollectionTicketShape(
            cornerCutout = 10.dp,
            sideNotchRadius = 10.dp,
            perforationFraction = CollectionDetailTicketPerforationFraction
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(CollectionDetailTicketAspectRatio)
            .semantics(mergeDescendants = true) {
                contentDescription = "${ticket.movieTitle} 티켓 상세, 별점 ${ticket.rating}/5점, 관람일 $watchedDateText"
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
                        .weight(CollectionDetailTicketPerforationFraction)
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                            CollectionTicketLikeButton(
                                liked = ticket.liked,
                                onClick = onLikeClick
                            )
                        }
                        Text(
                            text = movieMetadataText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                CollectionDetailTicketInfoArea(
                    rating = ticket.rating,
                    watchedDateText = watchedDateText,
                    review = ticket.review,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f - CollectionDetailTicketPerforationFraction)
                )
            }
            CollectionDetailTicketPerforationLine(
                modifier = Modifier.fillMaxSize(),
                fraction = CollectionDetailTicketPerforationFraction
            )
        }
    }
}

@Composable
internal fun CollectionTicketCard(
    ticket: MovieTicket,
    onClick: () -> Unit,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val posterUrl = remember(ticket.posterImagePath) {
        ticket.posterImagePath.toMovieImageUrl()
    }
    val watchedDateText = remember(ticket.watchedDate) {
        ticket.watchedDate.toCollectionTicketDateText()
    }
    val ticketShape = remember {
        CollectionTicketShape(
            cornerCutout = 10.dp,
            includeSideNotches = false
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(CollectionTicketAspectRatio)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "${ticket.movieTitle} 티켓, 별점 ${ticket.rating}/5점, 관람일 $watchedDateText"
            },
        shape = ticketShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                    .background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.86f))
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ticket.movieTitle,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    CollectionTicketLikeButton(
                        liked = ticket.liked,
                        onClick = onLikeClick,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "${ticket.rating}/5점",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "∙",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = watchedDateText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionTicketLikeButton(
    liked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (liked) "좋아요 취소" else "좋아요",
            modifier = Modifier.size(18.dp),
            tint = if (liked) contentColor else MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun CollectionDetailTicketInfoArea(
    rating: Int,
    watchedDateText: String,
    review: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 16.dp, top = 32.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.Top
        ) {
            CollectionDetailTicketInfoBlock(
                icon = Icons.Filled.Star,
                label = "별점",
                value = "${rating.coerceIn(0, 5)}/5점",
                modifier = Modifier.size(width = 56.dp, height = 42.dp)
            )
            CollectionDetailTicketInfoBlock(
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
                text = review.ifBlank { "남긴 관람 후기가 없어요." },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CollectionDetailTicketInfoBlock(
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
            style = MaterialTheme.typography.labelMedium,
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
private fun CollectionDetailTicketPerforationLine(
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

@Composable
private fun RatingStars(
    rating: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (index < rating) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

@Composable
internal fun RatingEditor(
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        (1..5).forEach { score ->
            IconButton(onClick = { onRatingChange(score) }) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "$score 점",
                    tint = if (score <= rating) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            }
        }
    }
}

private class CollectionTicketShape(
    private val cornerCutout: Dp,
    private val sideNotchRadius: Dp = 0.dp,
    private val perforationFraction: Float = 0f,
    private val includeSideNotches: Boolean = true
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val corner = with(density) { cornerCutout.toPx() }.coerceAtMost(size.minDimension / 6f)
        val notch = if (includeSideNotches) {
            with(density) { sideNotchRadius.toPx() }.coerceAtMost(size.minDimension / 6f)
        } else {
            0f
        }
        val perforationY = if (includeSideNotches) {
            (size.height * perforationFraction).coerceIn(
                corner + notch,
                size.height - corner - notch
            )
        } else {
            0f
        }
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
            if (includeSideNotches) {
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
            }
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
            if (includeSideNotches) {
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
            }
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

private fun String.toCollectionTicketDateText(): String {
    val parts = split("-")
    return if (parts.size == 3 && parts[0].length >= 2) {
        "${parts[0].takeLast(2)}/${parts[1]}/${parts[2]}"
    } else {
        ifBlank { "관람일" }
    }
}

internal fun String.toCollectionDetailWatchedDateText(): String {
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

internal fun MovieTicket.toCollectionDetailMovieMetadataText(): String {
    return listOfNotNull(
        genre.takeIf { it.isNotBlank() },
        director.takeIf { it.isNotBlank() }?.let { "$it 감독" },
        releaseYear.takeIf { it > 0 }?.let { "${it}년" },
        duration.takeIf { it.isNotBlank() }
    ).joinToString(" · ").ifBlank { "독립영화" }
}

private val KoreanDayOfWeekLabels = listOf("월", "화", "수", "목", "금", "토", "일")

private const val CollectionTicketAspectRatio = 160f / 210f
private const val CollectionTicketOverlayFraction = 150f / 210f
private const val CollectionDetailTicketAspectRatio = 328f / 604f
private const val CollectionDetailTicketPerforationFraction = 402f / 604f
