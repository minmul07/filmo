package com.filmo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.filmo.ui.theme.LocalFilmoSpacing

@Composable
internal fun FilmoTopBar(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
    contentAlignment: Alignment = Alignment.CenterStart,
    content: @Composable BoxScope.() -> Unit
) {
    val spacing = LocalFilmoSpacing.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.topBarHeight)
                .padding(horizontal = horizontalPadding),
            contentAlignment = contentAlignment,
            content = content
        )
    }
}
