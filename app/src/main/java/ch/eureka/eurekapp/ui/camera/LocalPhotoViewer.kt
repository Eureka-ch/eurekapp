package ch.eureka.eurekapp.ui.camera

import android.net.Uri
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

// Portions of this code were generated with the help of ChatGPT.
@Composable
fun LocalPhotoViewer(uri: Uri, modifier: Modifier = Modifier) {
  AndroidView(
      factory = { context -> ImageView(context).apply { setImageURI(uri) } },
      modifier = modifier,
      update = { imageView ->
        imageView.setImageURI(null)
        imageView.setImageURI(uri)
      })
}
