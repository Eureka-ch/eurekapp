package ch.eureka.eurekapp.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

// Portions of this code were generated with the help of Grok.
@Composable
fun PhotoViewer(image: Any, modifier: Modifier = Modifier) {
  AsyncImage(
      model = image,
      contentDescription = "Remote Photo",
      contentScale = ContentScale.Fit,
      modifier = modifier)
}
