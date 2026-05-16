package com.filmo.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.filmo.ui.ScreenDestination
import com.filmo.ui.collection.CollectionDetailScreen
import com.filmo.ui.collection.CollectionEditScreen
import com.filmo.ui.collection.CollectionScreen
import com.filmo.ui.movie.RegisterMovieScreen
import com.filmo.ui.movie.RegisterMovieViewModel
import com.filmo.ui.movie.ViewingInfoRoute
import com.filmo.ui.ticket.TicketViewScreen

@Composable
fun MainScreen(
    startDestination: ScreenDestination = ScreenDestination.Collection,
) {
    val backStack = rememberNavBackStack(startDestination)
    val currentDestination = backStack.lastOrNull() as? ScreenDestination ?: startDestination
    val navigateToTopLevelDestination: (ScreenDestination) -> Unit = { destination ->
        if (currentDestination != destination) {
            backStack.clear()
            backStack.add(destination)
        }
    }
    val navigateBack = {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }
    var bottomBarHeightPx by remember { mutableIntStateOf(0) }
    val bottomBarHeight = with(LocalDensity.current) { bottomBarHeightPx.toDp() }
    val navDisplayBottomPadding = if (shouldReserveBottomBarSpace(currentDestination)) {
        bottomBarHeight
    } else {
        0.dp
    }
    val placeBottomBarAboveContent = shouldPlaceBottomBarAboveContent(
        currentDestination = currentDestination
    )
    val registerMovieViewModel: RegisterMovieViewModel = hiltViewModel()
    val layoutDirection = LocalLayoutDirection.current
    val safeDrawingPadding = WindowInsets.safeDrawing.asPaddingValues()

    LaunchedEffect(currentDestination, registerMovieViewModel) {
        if (shouldResetRecordMovieState(currentDestination)) {
            registerMovieViewModel.reset()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FilmoBottomBar(
            currentDestination = currentDestination,
            onDestinationClick = navigateToTopLevelDestination,
            onHeightChanged = { bottomBarHeightPx = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .zIndex(if (placeBottomBarAboveContent) 2f else 0f)
        )
        NavDisplay(
            backStack = backStack,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = safeDrawingPadding.calculateStartPadding(layoutDirection),
                    top = 0.dp,
                    end = safeDrawingPadding.calculateEndPadding(layoutDirection),
                    bottom = navDisplayBottomPadding
                )
                .zIndex(1f),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            onBack = navigateBack,
            entryProvider = entryProvider {
                entry<ScreenDestination.TicketView> {
                    TicketViewScreen(
                        bottomContentPadding = bottomBarHeight
                    )
                }

                entry<ScreenDestination.Record> {
                    RegisterMovieScreen(
                        viewModel = registerMovieViewModel,
                        searchBottomPadding = bottomBarHeight,
                        onNavigateToViewingInfo = {
                            backStack.add(ScreenDestination.RecordViewingInfo)
                        },
                        onBack = { navigateToTopLevelDestination(ScreenDestination.Collection) }
                    )
                }

                entry<ScreenDestination.RecordViewingInfo> {
                    ViewingInfoRoute(
                        viewModel = registerMovieViewModel,
                        onBack = navigateBack,
                        onNavigateToCollection = {
                            navigateToTopLevelDestination(ScreenDestination.Collection)
                        }
                    )
                }

                entry<ScreenDestination.Collection> {
                    CollectionScreen(
                        bottomContentPadding = bottomBarHeight,
                        onTicketClick = { ticketId ->
                            backStack.add(ScreenDestination.CollectionDetail(ticketId))
                        },
                        onEditTicket = { ticketId ->
                            backStack.add(ScreenDestination.CollectionEdit(ticketId))
                        }
                    )
                }

                entry<ScreenDestination.CollectionDetail> {
                    CollectionDetailScreen(
                        ticketId = it.ticketId,
                        onBack = navigateBack,
                        onEditTicket = { ticketId ->
                            backStack.add(ScreenDestination.CollectionEdit(ticketId))
                        }
                    )
                }

                entry<ScreenDestination.CollectionEdit> {
                    CollectionEditScreen(
                        ticketId = it.ticketId,
                        onBack = navigateBack,
                        onSaved = { navigateToTopLevelDestination(ScreenDestination.Collection) }
                    )
                }
            }
        )
    }
}

internal fun shouldResetRecordMovieState(
    currentDestination: ScreenDestination
): Boolean {
    return currentDestination !is ScreenDestination.Record &&
        currentDestination !is ScreenDestination.RecordViewingInfo
}

internal fun shouldReserveBottomBarSpace(
    currentDestination: ScreenDestination,
    recordCoversBottomBar: Boolean = false
): Boolean {
    return false
}

internal fun shouldPlaceBottomBarAboveContent(
    currentDestination: ScreenDestination
): Boolean {
    return currentDestination is ScreenDestination.TicketView ||
        currentDestination is ScreenDestination.Record ||
        currentDestination is ScreenDestination.Collection
}

@Composable
private fun FilmoBottomBar(
    currentDestination: ScreenDestination,
    onDestinationClick: (ScreenDestination) -> Unit,
    onHeightChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .onSizeChanged {
                onHeightChanged(it.height)
            }
    ) {
        TopLevelDestinations.forEach { item ->
            NavigationBarItem(
                selected = currentDestination == item.destination,
                onClick = { onDestinationClick(item.destination) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(text = item.label) }
            )
        }
    }
}

private data class TopLevelDestination(
    val destination: ScreenDestination,
    val label: String,
    val icon: ImageVector
)

internal data class BottomBarDestinationSpec(
    val destination: ScreenDestination,
    val label: String
)

internal fun mainTopLevelDestinationSpecs(): List<BottomBarDestinationSpec> {
    return TopLevelDestinations.map { item ->
        BottomBarDestinationSpec(
            destination = item.destination,
            label = item.label
        )
    }
}

private val TopLevelDestinations = listOf(
    TopLevelDestination(
        destination = ScreenDestination.Collection,
        label = "컬렉션",
        icon = Icons.Outlined.Bookmark
    ),
    TopLevelDestination(
        destination = ScreenDestination.Record,
        label = "기록하기",
        icon = Icons.Filled.Add
    ),
    TopLevelDestination(
        destination = ScreenDestination.TicketView,
        label = "티켓보기",
        icon = Icons.AutoMirrored.Filled.Label
    )
)
