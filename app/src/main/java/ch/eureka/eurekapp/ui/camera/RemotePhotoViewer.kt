package ch.eureka.eurekapp.ui.camera

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

// Portions of this code were generated with the help of Grok.

@Composable
fun RemotePhotoViewer(url: String) {
    AsyncImage(
        model = url,
        contentDescription = "Remote Photo",
        contentScale = ContentScale.Fit,  // Scales image to fit without cropping
    )
}
