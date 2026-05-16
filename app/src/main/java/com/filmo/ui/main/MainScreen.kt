package com.filmo.ui.main

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.filmo.ui.ScreenDestination
import com.filmo.ui.collection.CollectionScreen
import com.filmo.ui.feed.FeedScreen
import com.filmo.ui.movie.RegisterMovieScreen
import com.filmo.ui.theater.TheaterFinderScreen

@Composable
fun MainScreen(
    startDestination: ScreenDestination = ScreenDestination.Feed,
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
    val isRecordDestination = currentDestination == ScreenDestination.Record
    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            FilmoBottomBar(
                currentDestination = currentDestination,
                hidden = isRecordDestination,
                onDestinationClick = navigateToTopLevelDestination
            )
        }
    ) { innerPadding ->
        val contentPadding = if (isRecordDestination) {
            PaddingValues(
                start = innerPadding.calculateStartPadding(layoutDirection),
                top = innerPadding.calculateTopPadding(),
                end = innerPadding.calculateEndPadding(layoutDirection),
                bottom = 0.dp
            )
        } else {
            innerPadding
        }

        NavDisplay(
            backStack = backStack,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            onBack = navigateBack,
            entryProvider = entryProvider {
                entry<ScreenDestination.Feed> {
                    FeedScreen()
                }

                entry<ScreenDestination.Record> {
                    RegisterMovieScreen(
                        onBack = { navigateToTopLevelDestination(ScreenDestination.Feed) },
                        onNavigateToCollection = {
                            navigateToTopLevelDestination(ScreenDestination.Collection)
                        }
                    )
                }

                entry<ScreenDestination.TheaterFinder> {
                    TheaterFinderScreen()
                }

                entry<ScreenDestination.Collection> {
                    CollectionScreen()
                }
            }
        )
    }
}

@Composable
private fun FilmoBottomBar(
    currentDestination: ScreenDestination,
    hidden: Boolean,
    onDestinationClick: (ScreenDestination) -> Unit
) {
    var barHeightPx by remember { mutableIntStateOf(0) }
    val offsetY by animateIntAsState(
        targetValue = if (hidden) barHeightPx else 0,
        animationSpec = tween(durationMillis = 300),
        label = "bottom_bar_offset"
    )

    NavigationBar(
        modifier = Modifier
            .onSizeChanged { barHeightPx = it.height }
            .offset { IntOffset(x = 0, y = offsetY) }
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

private val TopLevelDestinations = listOf(
    TopLevelDestination(
        destination = ScreenDestination.Feed,
        label = "피드",
        icon = Icons.Filled.Home
    ),
    TopLevelDestination(
        destination = ScreenDestination.Record,
        label = "기록하기",
        icon = Icons.Filled.Add
    ),
    TopLevelDestination(
        destination = ScreenDestination.TheaterFinder,
        label = "영화관",
        icon = Icons.Filled.LocationOn
    ),
    TopLevelDestination(
        destination = ScreenDestination.Collection,
        label = "내 컬렉션",
        icon = Icons.Filled.Person
    )
)
