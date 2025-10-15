package ch.eureka.eurekapp.ui.camera

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import ch.eureka.eurekapp.screens.PhotoScreenTestTags

@Composable
fun LocalPhotoViewer(uri: Uri, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context -> ImageView(context).apply { setImageURI(uri) } },
        modifier = modifier)
}