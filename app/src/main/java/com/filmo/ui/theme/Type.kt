package com.filmo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.filmo.R

val PretendardFontFamily = FontFamily(
    Font(R.font.pretendard_light, weight = FontWeight.Light),
    Font(R.font.pretendard_regular, weight = FontWeight.Normal),
    Font(R.font.pretendard_semibold, weight = FontWeight.SemiBold),
    Font(R.font.pretendard_bold, weight = FontWeight.Bold),
)

val Typography = Typography(
    displayLarge = filmoTextStyle(
        weight = FontWeight.Bold,
        size = 22,
        lineHeight = 30
    ),
    displayMedium = filmoTextStyle(
        weight = FontWeight.Bold,
        size = 20,
        lineHeight = 26
    ),
    displaySmall = filmoTextStyle(
        weight = FontWeight.Bold,
        size = 18,
        lineHeight = 24
    ),
    headlineLarge = filmoTextStyle(
        weight = FontWeight.Bold,
        size = 22,
        lineHeight = 30
    ),
    headlineMedium = filmoTextStyle(
        weight = FontWeight.Bold,
        size = 20,
        lineHeight = 26
    ),
    headlineSmall = filmoTextStyle(
        weight = FontWeight.Bold,
        size = 18,
        lineHeight = 24
    ),
    titleLarge = filmoTextStyle(
        weight = FontWeight.Bold,
        size = 17,
        lineHeight = 22
    ),
    titleMedium = filmoTextStyle(
        weight = FontWeight.SemiBold,
        size = 16,
        lineHeight = 21
    ),
    titleSmall = filmoTextStyle(
        weight = FontWeight.SemiBold,
        size = 15,
        lineHeight = 20
    ),
    bodyLarge = filmoTextStyle(
        weight = FontWeight.SemiBold,
        size = 16,
        lineHeight = 21
    ),
    bodyMedium = filmoTextStyle(
        weight = FontWeight.Medium,
        size = 14,
        lineHeight = 18
    ),
    bodySmall = filmoTextStyle(
        weight = FontWeight.Normal,
        size = 14,
        lineHeight = 18
    ),
    labelLarge = filmoTextStyle(
        weight = FontWeight.SemiBold,
        size = 14,
        lineHeight = 18
    ),
    labelMedium = filmoTextStyle(
        weight = FontWeight.Medium,
        size = 13,
        lineHeight = 17
    ),
    labelSmall = filmoTextStyle(
        weight = FontWeight.Medium,
        size = 10,
        lineHeight = 13
    ),
).applyFontFamily(PretendardFontFamily)

private fun filmoTextStyle(
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
): TextStyle {
    return TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = 0.sp
    )
}

fun Typography.applyFontFamily(fontFamily: FontFamily): Typography {
    return this.copy(
        displayLarge = this.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = this.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = this.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = this.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = this.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = this.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = this.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = this.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = this.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = this.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = this.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = this.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = this.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = this.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = this.labelSmall.copy(fontFamily = fontFamily)
    )
}
