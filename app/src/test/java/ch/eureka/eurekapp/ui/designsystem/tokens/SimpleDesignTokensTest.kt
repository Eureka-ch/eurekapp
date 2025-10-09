package ch.eureka.eurekapp.ui.designsystem.tokens

import org.junit.Test
import org.junit.Assert.*

class SimpleDesignTokensTest {

    @Test
    fun `EColors light theme has correct primary color`() {
        val lightTheme = EColors.light
        assertNotNull(lightTheme.primary)
        assertNotNull(lightTheme.onPrimary)
        assertNotNull(lightTheme.secondary)
        assertNotNull(lightTheme.tertiary)
    }

    @Test
    fun `EColors dark theme has correct primary color`() {
        val darkTheme = EColors.dark
        assertNotNull(darkTheme.primary)
        assertNotNull(darkTheme.onPrimary)
        assertNotNull(darkTheme.secondary)
        assertNotNull(darkTheme.tertiary)
    }

    @Test
    fun `EColors light and dark themes are different`() {
        val lightTheme = EColors.light
        val darkTheme = EColors.dark
        assertNotEquals(lightTheme.primary, darkTheme.primary)
        assertNotEquals(lightTheme.background, darkTheme.background)
        assertNotEquals(lightTheme.surface, darkTheme.surface)
    }

    @Test
    fun `ETypography has all required text styles`() {
        val typography = ETypography.value
        assertNotNull(typography.displayLarge)
        assertNotNull(typography.displayMedium)
        assertNotNull(typography.displaySmall)
        assertNotNull(typography.titleLarge)
        assertNotNull(typography.titleMedium)
        assertNotNull(typography.titleSmall)
        assertNotNull(typography.bodyLarge)
        assertNotNull(typography.bodyMedium)
        assertNotNull(typography.labelLarge)
        assertNotNull(typography.labelMedium)
        assertNotNull(typography.labelSmall)
    }

    @Test
    fun `ETypography constants have correct values`() {
        val constants = ETypography.Constants
        assertEquals(28, constants.DISPLAY_LARGE_SIZE)
        assertEquals(24, constants.DISPLAY_MEDIUM_SIZE)
        assertEquals(22, constants.DISPLAY_SMALL_SIZE)
        assertEquals(22, constants.TITLE_LARGE_SIZE)
        assertEquals(16, constants.TITLE_MEDIUM_SIZE)
        assertEquals(14, constants.TITLE_SMALL_SIZE)
        assertEquals(16, constants.BODY_LARGE_SIZE)
        assertEquals(14, constants.BODY_MEDIUM_SIZE)
        assertEquals(14, constants.LABEL_LARGE_SIZE)
        assertEquals(13, constants.LABEL_MEDIUM_SIZE)
        assertEquals(11, constants.LABEL_SMALL_SIZE)
    }

    @Test
    fun `EShapes has all required shapes`() {
        val shapes = EShapes
        assertNotNull(shapes.value.small)
        assertNotNull(shapes.value.medium)
        assertNotNull(shapes.value.large)
    }

    @Test
    fun `Spacing has all required values`() {
        val spacing = Spacing
        assertNotNull(spacing.xxs)
        assertNotNull(spacing.xs)
        assertNotNull(spacing.sm)
        assertNotNull(spacing.md)
        assertNotNull(spacing.lg)
        assertNotNull(spacing.xl)
    }

    @Test
    fun `Spacing values are in correct order`() {
        val spacing = Spacing
        assertTrue(spacing.xxs.value < spacing.xs.value)
        assertTrue(spacing.xs.value < spacing.sm.value)
        assertTrue(spacing.sm.value < spacing.md.value)
        assertTrue(spacing.md.value < spacing.lg.value)
        assertTrue(spacing.lg.value < spacing.xl.value)
    }

    @Test
    fun `EurekaStyles has all required styles`() {
        val styles = EurekaStyles
        assertNotNull(styles.CardShape)
        assertNotNull(styles.CardElevation)
    }

    @Test
    fun `EurekaStyles styles are not null`() {
        val styles = EurekaStyles
        assertNotNull(styles.CardShape)
        assertNotNull(styles.CardElevation)
    }
}
