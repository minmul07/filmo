package minmul.androidtemplate.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class TypeTest {
    @Test
    fun typographyUsesPretendardFontFamily() {
        val typography = Typography

        assertEquals(PretendardFontFamily, typography.displayLarge.fontFamily)
        assertEquals(PretendardFontFamily, typography.displayMedium.fontFamily)
        assertEquals(PretendardFontFamily, typography.displaySmall.fontFamily)
        assertEquals(PretendardFontFamily, typography.headlineLarge.fontFamily)
        assertEquals(PretendardFontFamily, typography.headlineMedium.fontFamily)
        assertEquals(PretendardFontFamily, typography.headlineSmall.fontFamily)
        assertEquals(PretendardFontFamily, typography.titleLarge.fontFamily)
        assertEquals(PretendardFontFamily, typography.titleMedium.fontFamily)
        assertEquals(PretendardFontFamily, typography.titleSmall.fontFamily)
        assertEquals(PretendardFontFamily, typography.bodyLarge.fontFamily)
        assertEquals(PretendardFontFamily, typography.bodyMedium.fontFamily)
        assertEquals(PretendardFontFamily, typography.bodySmall.fontFamily)
        assertEquals(PretendardFontFamily, typography.labelLarge.fontFamily)
        assertEquals(PretendardFontFamily, typography.labelMedium.fontFamily)
        assertEquals(PretendardFontFamily, typography.labelSmall.fontFamily)
    }
}
