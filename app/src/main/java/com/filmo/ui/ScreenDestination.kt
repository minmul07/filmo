package com.filmo.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ScreenDestination : NavKey {
    @Serializable
    data object Home : ScreenDestination

    @Serializable
    data object Setting : ScreenDestination

    @Serializable
    data object Test : ScreenDestination
}
