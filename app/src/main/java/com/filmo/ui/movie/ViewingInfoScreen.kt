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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import java.time.ZoneId

@Composable
internal fun ViewingInfoRoute(
    viewModel: RegisterMovieViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigateToCollection: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val posterImageCache = rememberMoviePosterBitmapSessionCache()
    val coroutineScope = rememberCoroutineScope()
    val routeVisibleState = remember {
        MutableTransitionState(initialState = false).apply {
            targetState = true
        }
    }
    val handleBack = {
        val stepBeforeBack = uiState.step
        val handledByStep = viewModel.goBack()
        if (!handledByStep || stepBeforeBack == RegisterMovieStep.MovieInfo) {
            onBack()
        }
    }

    BackHandler {
        handleBack()
    }

    AnimatedVisibility(
        visibleState = routeVisibleState,
        modifier = modifier.fillMaxSize(),
        enter = slideInVertically(
            animationSpec = tween(
                durationMillis = ViewingInfoRouteEnterDurationMillis,
                easing = ViewingInfoRouteEnterEasing
            ),
            initialOffsetY = ::viewingInfoRouteEnterOffsetY
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                durationMillis = ViewingInfoRouteExitDurationMillis,
                easing = ViewingInfoRouteExitEasing
            ),
            targetOffsetY = ::viewingInfoRouteExitOffsetY
        ),
        label = "viewing_info_route"
    ) {
        when (uiState.step) {
            RegisterMovieStep.MovieSearch -> {
                LaunchedEffect(Unit) {
                    onBack()
                }
            }

            RegisterMovieStep.MovieInfo -> ViewingInfoScreen(
                uiState = uiState,
                posterImageCache = posterImageCache,
                onBack = handleBack,
                onReleaseDateChange = viewModel::updateReleaseDateMillis,
                onRatingChange = viewModel::updateRating,
                onReviewChange = viewModel::updateReview,
                onNext = viewModel::goToNextStep
            )

            RegisterMovieStep.Share -> TicketShareScreen(
                uiState = uiState,
                posterImageCache = posterImageCache,
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
    }
}

@Composable
internal fun ViewingInfoScreen(
    uiState: RegisterMovieUiState,
    posterImageCache: MoviePosterBitmapSessionCache,
    onBack: () -> Unit,
    onReleaseDateChange: (Long?) -> Unit,
    onRatingChange: (Int) -> Unit,
    onReviewChange: (String) -> Unit,
    onNext: () -> Unit
) {
    var isDatePickerOpen by remember { mutableStateOf(false) }
    val zoneId = remember { ZoneId.systemDefault() }
    var wheelDateSelection by remember {
        mutableStateOf(
            (uiState.releaseDateMillis ?: System.currentTimeMillis()).toWheelDateSelection(zoneId)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        RegisterMovieHeader(
            step = RegisterMovieStep.MovieInfo,
            onBack = onBack
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            MovieInfoStep(
                uiState = uiState,
                errorMessage = uiState.errorMessage,
                posterImageCache = posterImageCache,
                onReleaseDateClick = {
                    val selectedMillis = uiState.releaseDateMillis ?: System.currentTimeMillis()
                    wheelDateSelection = selectedMillis.toWheelDateSelection(zoneId)
                    isDatePickerOpen = true
                },
                onRatingChange = onRatingChange,
                onReviewChange = onReviewChange,
                onNext = onNext
            )
        }
    }

    if (isDatePickerOpen) {
        WheelDatePickerSheet(
            selection = wheelDateSelection,
            onSelectionChange = { wheelDateSelection = it },
            onDismissRequest = { isDatePickerOpen = false },
            onConfirm = {
                onReleaseDateChange(wheelDateSelection.toStartOfDayMillis(zoneId))
                isDatePickerOpen = false
            }
        )
    }
}

internal fun viewingInfoRouteEnterOffsetY(fullHeight: Int): Int {
    return fullHeight
}

internal fun viewingInfoRouteExitOffsetY(fullHeight: Int): Int {
    return fullHeight
}

private const val ViewingInfoRouteEnterDurationMillis = 400
private const val ViewingInfoRouteExitDurationMillis = 300
private val ViewingInfoRouteEnterEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private val ViewingInfoRouteExitEasing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
