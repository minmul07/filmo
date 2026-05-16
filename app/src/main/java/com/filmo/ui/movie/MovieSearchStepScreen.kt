package com.filmo.ui.movie

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.filmo.service.MovieCatalogItem

@Composable
internal fun MovieSearchStep(
    query: String,
    movies: List<MovieCatalogItem>,
    isLoading: Boolean,
    isAppending: Boolean,
    canLoadMore: Boolean,
    errorMessage: String?,
    posterImageCache: MoviePosterBitmapSessionCache,
    onQueryChange: (String) -> Unit,
    onMovieClick: (MovieCatalogItem) -> Unit,
    onRetryClick: () -> Unit,
    onLoadNextMovies: () -> Unit
) {
    val gridState = rememberLazyGridState()
    val movieCount = movies.size
    val shouldLoadMore by remember(gridState, movieCount) {
        derivedStateOf {
            val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            movieCount > 0 && lastVisibleIndex >= movieCount - MovieCatalogPrefetchThreshold
        }
    }

    LaunchedEffect(
        shouldLoadMore,
        canLoadMore,
        isLoading,
        isAppending,
        movies.size
    ) {
        if (shouldLoadMore && canLoadMore && !isLoading && !isAppending) {
            onLoadNextMovies()
        }
    }

    StepContent(
        action = {},
        scrollable = false
    ) {
        Text(
            text = "기록하기",
            style = MaterialTheme.typography.headlineSmall
        )
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null
                )
            },
            label = { Text("관람한 영화를 검색하세요") },
            singleLine = true
        )
        when {
            isLoading -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            errorMessage != null && movies.isEmpty() -> {
                PlaceholderPanel(
                    title = "영화 목록을 불러오지 못했어요",
                    body = "잠시 후 다시 시도해 주세요."
                )
                OutlinedButton(
                    onClick = onRetryClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("다시 시도")
                }
            }

            movies.isEmpty() -> {
                PlaceholderPanel(
                    title = "검색 결과가 없어요",
                    body = "다른 제목이나 감독명으로 검색해 주세요."
                )
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    state = gridState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = movies,
                        key = { it.listKey },
                        contentType = { "movie_result" }
                    ) { movie ->
                        MovieResultCard(
                            movie = movie,
                            posterImageCache = posterImageCache,
                            onClick = { onMovieClick(movie) }
                        )
                    }
                    item(
                        key = "movie_catalog_footer",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = "movie_catalog_footer"
                    ) {
                        MovieCatalogFooter(
                            isAppending = isAppending,
                            errorMessage = errorMessage,
                            canLoadMore = canLoadMore,
                            onRetryClick = onLoadNextMovies
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieResultCard(
    movie: MovieCatalogItem,
    posterImageCache: MoviePosterBitmapSessionCache,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MoviePoster(
            movie = movie,
            imageCache = posterImageCache,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
        )
        Text(
            text = movie.title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = RatingUnselectedColor
            )
            Text(
                text = "4.9(21)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MovieCatalogFooter(
    isAppending: Boolean,
    errorMessage: String?,
    canLoadMore: Boolean,
    onRetryClick: () -> Unit
) {
    when {
        isAppending -> {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        errorMessage != null -> {
            OutlinedButton(
                onClick = onRetryClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("더 불러오기")
            }
        }

        canLoadMore -> {
            Spacer(modifier = Modifier.height(0.dp))
        }
    }
}

private const val MovieCatalogPrefetchThreshold = 5
