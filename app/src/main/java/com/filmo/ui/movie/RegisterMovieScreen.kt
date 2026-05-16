package com.filmo.ui.movie

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filmo.service.MovieCatalogItem
import com.filmo.ui.theme.FilmoTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun RegisterMovieScreen(
    viewModel: RegisterMovieViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateToCollection: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.loadMovieCatalog()
    }

    BackHandler {
        val handledByStep = viewModel.goBack()
        if (!handledByStep) {
            onBack()
        }
    }

    RegisterMovieContent(
        uiState = uiState,
        modifier = modifier,
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
        onMovieClick = viewModel::selectMovie,
        onReleaseDateChange = viewModel::updateReleaseDateMillis,
        onRatingChange = viewModel::updateRating,
        onReviewChange = viewModel::updateReview,
        onNext = viewModel::goToNextStep,
        onBack = {
            val handledByStep = viewModel.goBack()
            if (!handledByStep) {
                onBack()
            }
        },
        onShareClick = {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, toTicketShareText(uiState))
            }
            context.startActivity(Intent.createChooser(shareIntent, "티켓 공유"))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterMovieContent(
    uiState: RegisterMovieUiState,
    modifier: Modifier = Modifier,
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
    var isDatePickerOpen by remember { mutableStateOf(false) }
    val posterImageCache = rememberMoviePosterBitmapSessionCache()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.releaseDateMillis,
        selectableDates = rememberPastOrTodaySelectableDates()
    )

    LaunchedEffect(uiState.releaseDateMillis) {
        if (datePickerState.selectedDateMillis != uiState.releaseDateMillis) {
            datePickerState.selectedDateMillis = uiState.releaseDateMillis
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            RegisterMovieHeader(
                step = uiState.step,
                onBack = onBack
            )

            when (uiState.step) {
                RegisterMovieStep.MovieSearch -> MovieSearchStep(
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

                RegisterMovieStep.MovieInfo -> MovieInfoStep(
                    uiState = uiState,
                    errorMessage = uiState.errorMessage,
                    posterImageCache = posterImageCache,
                    onReleaseDateClick = { isDatePickerOpen = true },
                    onRatingChange = onRatingChange,
                    onReviewChange = onReviewChange,
                    onNext = onNext
                )

                RegisterMovieStep.Share -> TicketShareStep(
                    uiState = uiState,
                    posterImageCache = posterImageCache,
                    onShareClick = onShareClick
                )
            }
        }
    }

    if (isDatePickerOpen) {
        DatePickerDialog(
            onDismissRequest = { isDatePickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReleaseDateChange(datePickerState.selectedDateMillis)
                        isDatePickerOpen = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerOpen = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun RegisterMovieHeader(
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
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.MoreHoriz,
                    contentDescription = "더보기"
                )
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberPastOrTodaySelectableDates(): SelectableDates {
    val todayEndMillis = remember {
        LocalDate.now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli() - 1L
    }

    return remember(todayEndMillis) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= todayEndMillis
            }
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

private fun toTicketShareText(uiState: RegisterMovieUiState): String {
    val titleText = uiState.title.ifBlank { uiState.selectedMovie?.title.orEmpty() }.ifBlank { "영화" }
    val watchedDateText = uiState.releaseDateMillis.toWatchedDateWithYearText().ifBlank { "관람일 미입력" }
    val ratingText = uiState.rating?.let { "$it/5점" } ?: "별점 미입력"
    val reviewText = uiState.review.ifBlank { "관람 후기를 남기지 않았어요." }

    return listOf(
        "FILMO 티켓",
        titleText,
        watchedDateText,
        ratingText,
        reviewText
    ).joinToString(separator = "\n")
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
                theaterName = "아트나인",
                rating = 5,
                review = "겨울 공기와 편지의 여운이 좋았다."
            )
        )
    }
}
