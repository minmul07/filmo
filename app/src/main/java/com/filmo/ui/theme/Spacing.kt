package com.filmo.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class FilmoSpacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val gridGutter: Dp = 12.dp,
    val screenHorizontal: Dp = 16.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 40.dp,
    val topBarHeight: Dp = 56.dp,
    val bottomBarHeight: Dp = 90.dp,
)

val LocalFilmoSpacing = staticCompositionLocalOf { FilmoSpacing() }
