package com.filmo.ui.movie

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filmo.service.MovieCatalogItem
import com.filmo.ui.theme.FilmoTheme
import kotlinx.coroutines.launch

@Composable
fun RegisterMovieScreen(
    viewModel: RegisterMovieViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    searchBottomPadding: Dp = 0.dp,
    onFullScreenStepVisibilityChange: (Boolean) -> Unit = {},
    onNavigateToViewingInfo: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.loadMovieCatalogIfNeeded()
    }

    BackHandler {
        onBack()
    }

    RegisterMovieContent(
        uiState = uiState,
        modifier = modifier,
        searchBottomPadding = searchBottomPadding,
        onFullScreenStepVisibilityChange = onFullScreenStepVisibilityChange,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onLoadMovies = {
            coroutineScope.launch {
                viewModel.loadMovieCatalog()
            }
        },
        onLoadNextMovies = {
            coroutineScope.launch {
                viewModel.loadNextMovieCatalogPage()
            }
        },
        onMovieClick = { movie ->
            viewModel.selectMovie(movie)
            onNavigateToViewingInfo()
        },
        onBack = onBack
    )
}

@Composable
private fun RegisterMovieContent(
    uiState: RegisterMovieUiState,
    modifier: Modifier = Modifier,
    searchBottomPadding: Dp = 0.dp,
    onFullScreenStepVisibilityChange: (Boolean) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onLoadMovies: () -> Unit = {},
    onLoadNextMovies: () -> Unit = {},
    onMovieClick: (MovieCatalogItem) -> Unit = {},
    onBack: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        onFullScreenStepVisibilityChange(false)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = searchBottomPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
            ) {
                RegisterMovieHeader(
                    step = RegisterMovieStep.MovieSearch,
                    onBack = onBack
                )
                MovieSearchStep(
                    query = uiState.searchQuery,
                    movies = uiState.movies,
                    isLoading = uiState.isMovieCatalogLoading,
                    isAppending = uiState.isMovieCatalogAppendLoading,
                    canLoadMore = uiState.canLoadMoreMovies,
                    hasLoadedMovieCatalog = uiState.hasLoadedMovieCatalog,
                    errorMessage = uiState.errorMessage,
                    modifier = Modifier.weight(1f),
                    onQueryChange = onSearchQueryChange,
                    onMovieClick = onMovieClick,
                    onRetryClick = onLoadMovies,
                    onLoadNextMovies = onLoadNextMovies
                )
            }
        }
    }
}

@Composable
internal fun TicketShareScreen(
    uiState: RegisterMovieUiState,
    onBack: () -> Unit,
    onShareClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        RegisterMovieHeader(
            step = RegisterMovieStep.Share,
            onBack = onBack
        )

        TicketShareStep(
            uiState = uiState,
            onShareClick = onShareClick
        )
    }
}

internal fun isViewingInfoScreenStep(step: RegisterMovieStep): Boolean {
    return step == RegisterMovieStep.MovieInfo
}

internal fun shouldCoverBottomBar(step: RegisterMovieStep): Boolean {
    return false
}

internal fun isFullScreenStepVisible(step: RegisterMovieStep): Boolean {
    return false
}

@Composable
internal fun RegisterMovieHeader(
    step: RegisterMovieStep,
    onBack: () -> Unit
) {
    if (step == RegisterMovieStep.MovieSearch) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = registerMovieHeaderTitle(step),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기"
            )
        }
        Text(
            text = registerMovieHeaderTitle(step),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        if (step == RegisterMovieStep.MovieInfo || step == RegisterMovieStep.Share) {
            Box(modifier = Modifier.width(48.dp))
        } else {
            Text(
                text = "${step.index}/4",
                modifier = Modifier.width(48.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

internal fun registerMovieHeaderTitle(step: RegisterMovieStep): String {
    return when (step) {
        RegisterMovieStep.MovieSearch -> "기록하기"
        RegisterMovieStep.MovieInfo -> "관람 정보 입력"
        RegisterMovieStep.Share -> "티켓 발행"
    }
}

private val RegisterMovieStep.index: Int
    get() = when (this) {
        RegisterMovieStep.MovieSearch -> 1
        RegisterMovieStep.MovieInfo -> 2
        RegisterMovieStep.Share -> 2
    }

@Preview(showBackground = true)
@Composable
private fun RegisterMovieContentPreview() {
    FilmoTheme {
        RegisterMovieContent(
            uiState = RegisterMovieUiState(
                step = RegisterMovieStep.MovieInfo,
                selectedMovie = MovieCatalogItem(
                    id = "preview-movie",
                    title = "윤희에게",
                    releaseYear = 2019,
                    director = "임대형",
                    genre = "드라마",
                    imagePath = ""
                ),
                title = "윤희에게",
                releaseDateMillis = 1_609_459_200_000L,
                genre = "드라마",
                director = "임대형",
                rating = 5,
                review = "겨울 공기와 편지의 여운이 좋았다."
            )
        )
    }
}
