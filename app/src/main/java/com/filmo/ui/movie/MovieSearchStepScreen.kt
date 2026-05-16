package com.filmo.ui.movie

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filmo.service.MovieCatalogItem
import com.filmo.ui.theme.FilmoPrimary300

@Composable
internal fun MovieSearchStep(
    query: String,
    movies: List<MovieCatalogItem>,
    isLoading: Boolean,
    isAppending: Boolean,
    canLoadMore: Boolean,
    hasLoadedMovieCatalog: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        MovieSearchField(
            query = query,
            onQueryChange = onQueryChange
        )
        Spacer(modifier = Modifier.height(20.dp))
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

            shouldShowMovieSearchEmptyResults(
                movies = movies,
                isLoading = isLoading,
                hasLoadedMovieCatalog = hasLoadedMovieCatalog
            ) -> {
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
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = movies,
                        key = { it.listKey },
                        contentType = { "movie_result" }
                    ) { movie ->
                        MovieResultCard(
                            movie = movie,
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

internal fun shouldShowMovieSearchEmptyResults(
    movies: List<MovieCatalogItem>,
    isLoading: Boolean,
    hasLoadedMovieCatalog: Boolean
): Boolean {
    return !isLoading && hasLoadedMovieCatalog && movies.isEmpty()
}

@Composable
private fun MovieSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchTextStyle = MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 21.sp
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 12.dp)
                .semantics {
                    contentDescription = "관람한 영화 검색"
                },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = searchTextStyle.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isBlank()) {
                            Text(
                                text = "관람한 영화를 검색하세요",
                                style = searchTextStyle,
                                color = FilmoPrimary300
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = FilmoPrimary300
            )
        }
    }
}

@Composable
private fun MovieResultCard(
    movie: MovieCatalogItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        MoviePoster(
            movie = movie,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(MoviePosterAspectRatio),
            shape = RoundedCornerShape(0.dp),
            showBorder = false
        )
        Text(
            text = movie.title,
            style = movieResultTitleTextStyle(),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun movieResultTitleTextStyle(): TextStyle {
    return MaterialTheme.typography.labelMedium.copy(
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
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
private const val MoviePosterAspectRatio = 104f / 146f
