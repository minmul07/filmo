package com.filmo.ui.movie

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
    onBack: () -> Unit = {},
    onNavigateToCollection: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val handleBack = {
        val handledByStep = viewModel.goBack()
        if (!handledByStep) {
            onBack()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.loadMovieCatalog()
    }

    BackHandler {
        handleBack()
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
        },
        onReleaseDateChange = viewModel::updateReleaseDateMillis,
        onRatingChange = viewModel::updateRating,
        onReviewChange = viewModel::updateReview,
        onNext = viewModel::goToNextStep,
        onBack = handleBack,
        onShareClick = {
            coroutineScope.launch {
                if (viewModel.createTicket()) {
                    onNavigateToCollection()
                }
            }
        }
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
    onReleaseDateChange: (Long?) -> Unit = {},
    onRatingChange: (Int) -> Unit = {},
    onReviewChange: (String) -> Unit = {},
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    val posterImageCache = rememberMoviePosterBitmapSessionCache()
    val fullScreenStep = uiState.step.takeIf(::isFullScreenStepVisible)
    var latestFullScreenStep by remember { mutableStateOf(fullScreenStep) }
    val fullScreenStepTransitionState = remember {
        MutableTransitionState(initialState = fullScreenStep != null)
    }

    LaunchedEffect(fullScreenStep) {
        if (fullScreenStep != null) {
            latestFullScreenStep = fullScreenStep
        }
        fullScreenStepTransitionState.targetState = fullScreenStep != null
    }
    val isFullScreenStepMounted = fullScreenStep != null ||
        fullScreenStepTransitionState.currentState ||
        fullScreenStepTransitionState.targetState

    LaunchedEffect(isFullScreenStepMounted) {
        onFullScreenStepVisibilityChange(isFullScreenStepMounted)
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
                    .padding(horizontal = 24.dp)
            ) {
                RegisterMovieHeader(
                    step = RegisterMovieStep.MovieSearch,
                    onBack = onBack
                )
                MovieSearchStep(
                    query = uiState.searchQuery,
                    movies = uiState.filteredMovies,
                    isLoading = uiState.isMovieCatalogLoading,
                    isAppending = uiState.isMovieCatalogAppendLoading,
                    canLoadMore = uiState.canLoadMoreMovies,
                    errorMessage = uiState.errorMessage,
                    posterImageCache = posterImageCache,
                    onQueryChange = onSearchQueryChange,
                    onMovieClick = onMovieClick,
                    onRetryClick = onLoadMovies,
                    onLoadNextMovies = onLoadNextMovies
                )
            }

            if (isFullScreenStepMounted) {
                AnimatedVisibility(
                    visibleState = fullScreenStepTransitionState,
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f),
                    enter = slideInVertically(
                        animationSpec = tween(
                            durationMillis = FullScreenStepEnterDurationMillis,
                            easing = FullScreenStepEnterEasing
                        ),
                        initialOffsetY = ::fullScreenStepEnterOffsetY
                    ),
                    exit = slideOutVertically(
                        animationSpec = tween(
                            durationMillis = FullScreenStepExitDurationMillis,
                            easing = FullScreenStepExitEasing
                        ),
                        targetOffsetY = ::fullScreenStepExitOffsetY
                    ),
                    label = "register_movie_step"
                ) {
                    val step = fullScreenStep ?: latestFullScreenStep
                    if (step != null) {
                        RegisterMovieFullScreenStep(
                            step = step,
                            uiState = uiState,
                            posterImageCache = posterImageCache,
                            onBack = onBack,
                            onReleaseDateChange = onReleaseDateChange,
                            onRatingChange = onRatingChange,
                            onReviewChange = onReviewChange,
                            onNext = onNext,
                            onShareClick = onShareClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterMovieFullScreenStep(
    step: RegisterMovieStep,
    uiState: RegisterMovieUiState,
    posterImageCache: MoviePosterBitmapSessionCache,
    onBack: () -> Unit,
    onReleaseDateChange: (Long?) -> Unit,
    onRatingChange: (Int) -> Unit,
    onReviewChange: (String) -> Unit,
    onNext: () -> Unit,
    onShareClick: () -> Unit
) {
    when (step) {
        RegisterMovieStep.MovieSearch -> Unit
        RegisterMovieStep.MovieInfo -> ViewingInfoScreen(
            uiState = uiState,
            posterImageCache = posterImageCache,
            onBack = onBack,
            onReleaseDateChange = onReleaseDateChange,
            onRatingChange = onRatingChange,
            onReviewChange = onReviewChange,
            onNext = onNext
        )

        RegisterMovieStep.Share -> TicketShareScreen(
            uiState = uiState,
            posterImageCache = posterImageCache,
            onBack = onBack,
            onShareClick = onShareClick
        )
    }
}

@Composable
private fun TicketShareScreen(
    uiState: RegisterMovieUiState,
    posterImageCache: MoviePosterBitmapSessionCache,
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
            posterImageCache = posterImageCache,
            onShareClick = onShareClick
        )
    }
}

internal fun isViewingInfoScreenStep(step: RegisterMovieStep): Boolean {
    return step == RegisterMovieStep.MovieInfo
}

internal fun shouldCoverBottomBar(step: RegisterMovieStep): Boolean {
    return isViewingInfoScreenStep(step) || step == RegisterMovieStep.Share
}

internal fun isFullScreenStepVisible(step: RegisterMovieStep): Boolean {
    return shouldCoverBottomBar(step)
}

internal fun fullScreenStepEnterOffsetY(fullHeight: Int): Int {
    return fullHeight
}

internal fun fullScreenStepExitOffsetY(fullHeight: Int): Int {
    return fullHeight
}

@Composable
internal fun RegisterMovieHeader(
    step: RegisterMovieStep,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기"
            )
        }
        Text(
            text = step.title,
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

private val RegisterMovieStep.title: String
    get() = when (this) {
        RegisterMovieStep.MovieSearch -> "영화 검색"
        RegisterMovieStep.MovieInfo -> "관람 정보 입력"
        RegisterMovieStep.Share -> "관람 정보 입력"
    }

private val RegisterMovieStep.index: Int
    get() = when (this) {
        RegisterMovieStep.MovieSearch -> 1
        RegisterMovieStep.MovieInfo -> 2
        RegisterMovieStep.Share -> 2
    }

private const val FullScreenStepEnterDurationMillis = 400
private const val FullScreenStepExitDurationMillis = 300
private val FullScreenStepEnterEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private val FullScreenStepExitEasing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

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
                theaterName = "아트나인",
                rating = 5,
                review = "겨울 공기와 편지의 여운이 좋았다."
            )
        )
    }
}
