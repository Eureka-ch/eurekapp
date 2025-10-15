package ch.eureka.eurekapp.ui.camera

import android.net.Uri
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun LocalPhotoViewer(uri: Uri, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context -> ImageView(context).apply { setImageURI(uri) } },
        modifier = modifier)
}