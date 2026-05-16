package com.filmo.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeTokenTest {
    @Test
    fun colorsMatchFigmaDesignSystem() {
        assertEquals(Color(0xFFF1F1F1), FilmoPrimary50)
        assertEquals(Color(0xFF1A1A1A), FilmoPrimary900)
        assertEquals(Color(0xFF4D68B8), FilmoSecondary500)
        assertEquals(Color(0xFFC51D23), FilmoPointRed)
        assertEquals(Color(0xFFFFFFFF), FilmoWhite)
        assertEquals(Color(0xFF222222), FilmoTextBlack)
    }

    @Test
    fun spacingMatchesMobileLayoutGrid() {
        val spacing = FilmoSpacing()

        assertEquals(4.dp, spacing.xs)
        assertEquals(8.dp, spacing.sm)
        assertEquals(12.dp, spacing.gridGutter)
        assertEquals(16.dp, spacing.screenHorizontal)
        assertEquals(24.dp, spacing.lg)
        assertEquals(56.dp, spacing.topBarHeight)
        assertEquals(90.dp, spacing.bottomBarHeight)
    }

    @Test
    fun typographyMatchesFigmaScale() {
        assertEquals(22.sp, Typography.displayLarge.fontSize)
        assertEquals(FontWeight.Bold, Typography.displayLarge.fontWeight)

        assertEquals(22.sp, Typography.headlineLarge.fontSize)
        assertEquals(30.sp, Typography.headlineLarge.lineHeight)
        assertEquals(FontWeight.Bold, Typography.headlineLarge.fontWeight)

        assertEquals(16.sp, Typography.bodyLarge.fontSize)
        assertEquals(21.sp, Typography.bodyLarge.lineHeight)
        assertEquals(FontWeight.SemiBold, Typography.bodyLarge.fontWeight)

        assertEquals(13.sp, Typography.labelMedium.fontSize)
        assertEquals(17.sp, Typography.labelMedium.lineHeight)
        assertEquals(FontWeight.Medium, Typography.labelMedium.fontWeight)
    }
}
