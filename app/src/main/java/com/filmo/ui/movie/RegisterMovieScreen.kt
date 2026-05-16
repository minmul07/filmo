package com.filmo.ui.movie

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filmo.BuildConfig
import com.filmo.service.MovieCatalogItem
import com.filmo.ui.theme.FilmoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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

@Composable
private fun MovieSearchStep(
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
            text = "영화 검색",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "관람한 영화를 검색하세요",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            label = { Text("영화 제목으로 검색") },
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

@Composable
private fun MoviePoster(
    movie: MovieCatalogItem,
    imageCache: MoviePosterBitmapSessionCache,
    modifier: Modifier = Modifier
) {
    MoviePosterImage(
        imageUrl = remember(movie.imagePath) { movie.imageUrl() },
        title = movie.title,
        imageCache = imageCache,
        modifier = modifier
    )
}

@Composable
private fun MoviePosterImage(
    imageUrl: String,
    title: String,
    imageCache: MoviePosterBitmapSessionCache,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(10.dp),
    showBorder: Boolean = true
) {
    val bitmap by produceState<ImageBitmap?>(
        initialValue = imageCache[imageUrl],
        imageUrl,
        imageCache
    ) {
        value = imageCache[imageUrl]
        if (value != null) {
            Timber.d("MoviePosterImage.loadImage memory_cache_hit urlType=%s", imageUrl.urlTypeForLog())
            return@produceState
        }
        if (imageUrl.isBlank()) {
            Timber.d("MoviePosterImage.loadImage skipped reason=blank_url titleLength=%d", title.length)
            return@produceState
        }

        Timber.d(
            "MoviePosterImage.loadImage request urlType=%s urlLength=%d titleLength=%d",
            imageUrl.urlTypeForLog(),
            imageUrl.length,
            title.length
        )

        val loadedBitmap = withContext(Dispatchers.IO) {
            runCatching {
                URL(imageUrl).openStream().use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
                }
            }.onSuccess { imageBitmap ->
                Timber.d(
                    "MoviePosterImage.loadImage success urlType=%s decoded=%s",
                    imageUrl.urlTypeForLog(),
                    imageBitmap != null
                )
            }.onFailure {
                Timber.w(
                    it,
                    "MoviePosterImage.loadImage failed urlType=%s urlLength=%d",
                    imageUrl.urlTypeForLog(),
                    imageUrl.length
                )
            }.getOrNull()
        }
        if (loadedBitmap != null) {
            imageCache[imageUrl] = loadedBitmap
        }
        value = loadedBitmap
    }

    Surface(
        modifier = modifier.clip(shape),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = if (showBorder) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        } else {
            null
        }
    ) {
        if (bitmap == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Movie,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Image(
                bitmap = bitmap!!,
                contentDescription = "$title 포스터",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun MovieInfoStep(
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
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("티켓 만들기")
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
            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
        OutlinedTextField(
            value = uiState.review,
            onValueChange = onReviewChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "placeholder\n공백 포함 100자",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            minLines = 5,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp)
        )
        ErrorText(errorMessage = errorMessage)
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    .width(186.dp)
                    .height(262.dp),
                shape = RoundedCornerShape(0.dp),
                showBorder = false
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = metaText,
                style = MaterialTheme.typography.bodyLarge,
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
        style = MaterialTheme.typography.titleMedium,
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
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.ifBlank { placeholder },
                style = MaterialTheme.typography.titleMedium,
                color = if (text.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
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
            IconButton(
                onClick = { onRatingChange(score) },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "$score 점",
                    modifier = Modifier.size(34.dp),
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
private fun TicketShareStep(
    uiState: RegisterMovieUiState,
    posterImageCache: MoviePosterBitmapSessionCache,
    onShareClick: () -> Unit
) {
    StepContent(
        action = {
            Button(
                onClick = onShareClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            ) {
                Text(
                    text = "공유하기",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            MovieShareTicket(
                uiState = uiState,
                posterImageCache = posterImageCache,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MovieShareTicket(
    uiState: RegisterMovieUiState,
    posterImageCache: MoviePosterBitmapSessionCache,
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
        TicketShape(
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
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(TicketPerforationFraction)
                ) {
                    MoviePosterImage(
                        imageUrl = posterUrl,
                        title = title,
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
                            .padding(horizontal = 22.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
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
                        .weight(1f - TicketPerforationFraction)
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
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 22.dp, top = 48.dp, end = 22.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            TicketInfoBlock(
                label = "별점",
                value = ratingText,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = RatingSelectedColor
                    )
                },
                modifier = Modifier.weight(0.8f)
            )
            TicketInfoBlock(
                label = "관람일",
                value = watchedDate,
                modifier = Modifier.weight(1.4f)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "관람 후기",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = review.ifBlank { "남긴 관람 후기가 없어요." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
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
            style = MaterialTheme.typography.titleSmall,
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
    val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
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

@Composable
private fun StepContent(
    action: @Composable () -> Unit,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = if (scrollable) {
                Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            } else {
                Modifier.weight(1f)
            },
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
        action()
    }
}

@Composable
private fun PlaceholderPanel(
    title: String,
    body: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Movie,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = body,
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorText(errorMessage: String?) {
    if (errorMessage == null) return

    Text(
        text = errorMessage,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
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

private fun Long?.toWatchedYearText(): String {
    return this?.let { millis ->
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("yyyy년", Locale.KOREAN))
    }.orEmpty()
}

private fun Long?.toWatchedDateText(): String {
    return this?.let { millis ->
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("MM월 dd일(E)", Locale.KOREAN))
    }.orEmpty()
}

private fun Long?.toWatchedDateWithYearText(): String {
    return this?.let { millis ->
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (E)", Locale.KOREAN))
    }.orEmpty()
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

@Composable
private fun rememberMoviePosterBitmapSessionCache(): MoviePosterBitmapSessionCache {
    return remember {
        MoviePosterBitmapSessionCache()
    }
}

@Stable
private class MoviePosterBitmapSessionCache {
    private val cache = mutableMapOf<String, ImageBitmap>()

    operator fun get(imageUrl: String): ImageBitmap? {
        return if (imageUrl.isBlank()) {
            null
        } else {
            cache.get(imageUrl)
        }
    }

    operator fun set(imageUrl: String, bitmap: ImageBitmap) {
        if (imageUrl.isNotBlank()) {
            cache.put(imageUrl, bitmap)
        }
    }
}

private val MovieCatalogItem.listKey: String
    get() = id.ifBlank { "$title-$releaseYear-$director" }

private fun MovieCatalogItem.imageUrl(): String {
    return imagePath.toMovieImageUrl()
}

private fun String.toMovieImageUrl(): String {
    val path = trim()
    if (path.isBlank()) {
        Timber.d("MovieImageUrl.resolve skipped reason=blank_path")
        return ""
    }
    if (path.startsWith("http://") || path.startsWith("https://")) {
        Timber.d("MovieImageUrl.resolve absolute pathLength=%d", path.length)
        return path
    }

    Timber.d("MovieImageUrl.resolve api pathLength=%d", path.length)
    return "${BuildConfig.API_BASE_URL.trimEnd('/')}/api/movies/image/${path.trimStart('/')}"
}

private fun String.urlTypeForLog(): String {
    return when {
        startsWith("https://") -> "https"
        startsWith("http://") -> "http"
        else -> "relative_or_unknown"
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

private val RatingSelectedColor = Color(0xFF7887CF)
private val RatingUnselectedColor = Color(0xFFDCDCDC)
private const val MovieCatalogPrefetchThreshold = 5
private const val TicketAspectRatio = 0.57f
private const val TicketPerforationFraction = 0.69f

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
