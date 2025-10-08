package ch.eureka.eurekapp.designsystem.tokens

import ch.eureka.eurekapp.ui.designsystem.tokens.EShapes
import org.junit.Assert.assertNotNull
import org.junit.Test

class ShapesTest {
    
    @Test
    fun `small shape is defined`() {
        val smallShape = EShapes.value.small
        assertNotNull("Small shape should be defined", smallShape)
    }
    
    @Test
    fun `medium shape is defined`() {
        val mediumShape = EShapes.value.medium
        assertNotNull("Medium shape should be defined", mediumShape)
    }
    
    @Test
    fun `large shape is defined`() {
        val largeShape = EShapes.value.large
        assertNotNull("Large shape should be defined", largeShape)
    }
    
    @Test
    fun `shapes object is properly configured`() {
        val shapes = EShapes.value
        assertNotNull("Shapes object should be defined", shapes)
        assertNotNull("Small shape should be accessible", shapes.small)
        assertNotNull("Medium shape should be accessible", shapes.medium)
        assertNotNull("Large shape should be accessible", shapes.large)
    }
}