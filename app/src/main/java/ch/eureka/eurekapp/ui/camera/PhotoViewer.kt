package ch.eureka.eurekapp.ui.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage

// Portions of this code were generated with the help of Grok.
@Composable
fun PhotoViewer(image: Any, modifier: Modifier = Modifier) {
  SubcomposeAsyncImage(
      model = image,
      contentDescription = "Remote Photo",
      contentScale = ContentScale.Fit,
      modifier = modifier,
      loading = {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      },
      error = {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text("Failed to load image")
        }
      })
}
