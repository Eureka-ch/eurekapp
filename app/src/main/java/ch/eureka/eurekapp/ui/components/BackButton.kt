package ch.eureka.eurekapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.eureka.eurekapp.R

// Portions of this code were generated with the help of Grok.

/**
 * Reusable back button component for navigation.
 *
 * @param onClick Callback invoked when the back button is clicked.
 * @param modifier Modifier to be applied to the IconButton.
 */
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  IconButton(onClick = onClick, modifier = modifier) {
    Icon(
        Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = stringResource(R.string.back_button_content_description))
  }
}
