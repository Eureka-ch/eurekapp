package ch.eureka.eurekapp.ui.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Rounded corner sizes aligned with card and button radii.
 */
object EShapes {
    
    val value = Shapes(
        small = RoundedCornerShape(10.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(20.dp)
    )
}
