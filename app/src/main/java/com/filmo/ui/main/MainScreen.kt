package com.filmo.ui.main

import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            FilmoBottomBar(
                currentDestination = currentDestination,
                onDestinationClick = navigateToTopLevelDestination
            )
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
                    RegisterMovieScreen()
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
    onDestinationClick: (ScreenDestination) -> Unit
) {
    NavigationBar {
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
        label = "영화관 찾기",
        icon = Icons.Filled.LocationOn
    ),
    TopLevelDestination(
        destination = ScreenDestination.Collection,
        label = "내 컬렉션",
        icon = Icons.Filled.Person
    )
)
