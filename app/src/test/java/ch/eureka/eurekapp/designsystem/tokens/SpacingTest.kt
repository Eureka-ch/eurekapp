package ch.eureka.eurekapp.designsystem.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.designsystem.tokens.LocalSpacing
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SpacingTest {
    
    @Test
    fun `default spacing values are correct`() {
        val spacing = Spacing()
        
        assertEquals("XS spacing should be 4dp", 4.dp, spacing.xs)
        assertEquals("SM spacing should be 8dp", 8.dp, spacing.sm)
        assertEquals("MD spacing should be 12dp", 12.dp, spacing.md)
        assertEquals("LG spacing should be 16dp", 16.dp, spacing.lg)
        assertEquals("XL spacing should be 24dp", 24.dp, spacing.xl)
        assertEquals("XXL spacing should be 32dp", 32.dp, spacing.xxl)
    }
    
    @Test
    fun `LocalSpacing is properly defined`() {
        // Verify that LocalSpacing is properly defined
        assertNotNull("LocalSpacing should be defined", LocalSpacing)
        
        // Test that we can create a Spacing instance with default values
        val spacing = Spacing()
        assertNotNull("Spacing instance should be created", spacing)
        
        // Verify default values match expected spacing
        assertEquals("Default XS should be 4dp", 4.dp, spacing.xs)
        assertEquals("Default SM should be 8dp", 8.dp, spacing.sm)
        assertEquals("Default MD should be 12dp", 12.dp, spacing.md)
        assertEquals("Default LG should be 16dp", 16.dp, spacing.lg)
        assertEquals("Default XL should be 24dp", 24.dp, spacing.xl)
        assertEquals("Default XXL should be 32dp", 32.dp, spacing.xxl)
    }
    
    @Test
    fun `spacing values are in ascending order`() {
        val spacing = Spacing()
        
        // Verify spacing values increase in order
        assert(spacing.xs < spacing.sm) { "XS should be smaller than SM" }
        assert(spacing.sm < spacing.md) { "SM should be smaller than MD" }
        assert(spacing.md < spacing.lg) { "MD should be smaller than LG" }
        assert(spacing.lg < spacing.xl) { "LG should be smaller than XL" }
        assert(spacing.xl < spacing.xxl) { "XL should be smaller than XXL" }
    }
}
