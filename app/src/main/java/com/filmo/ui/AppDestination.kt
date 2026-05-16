package com.filmo.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AppDestination : NavKey {
    @Serializable
    data object Splash : AppDestination

    @Serializable
    data object InitialSetup : AppDestination

    @Serializable
    data object Main : AppDestination
}

internal fun MutableList<NavKey>.replaceRoot(destination: AppDestination) {
    if (size == 1 && firstOrNull() == destination) return

    clear()
    add(destination)
}

internal fun MutableList<NavKey>.replaceWithMain() {
    replaceRoot(AppDestination.Main)
}
