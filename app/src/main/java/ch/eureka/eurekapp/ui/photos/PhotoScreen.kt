package ch.eureka.eurekapp.ui.photos

import android.Manifest
import android.content.pm.PackageManager
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.camera.CameraViewModel
import kotlinx.coroutines.launch

object PhotoScreenTestTags {
  const val PHOTO = "photo"
  const val PREVIEW = "preview"
  const val TAKE_PHOTO = "take_photo"
  const val DELETE_PHOTO = "delete_photo"
  const val NO_PERMISSION = "no_permission"
  const val GRANT_PERMISSION = "grant_permission"
}

// Portions of this code were generated with the help of Grok.
@Composable
fun PhotoScreen(cameraViewModel: CameraViewModel) {
  val cameraState by cameraViewModel.photoState.collectAsState()
  val cameraPreview by cameraViewModel.preview.collectAsState()
  val context = LocalContext.current
  val previewView = remember { PreviewView(context) }
  var hasPermission by remember {
    mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED)
  }
  if (hasPermission) {
    Box(modifier = Modifier.fillMaxSize()) {
      if (cameraState.picture != null) {
        AndroidView(
            factory = { context -> ImageView(context).apply { setImageURI(cameraState.picture) } },
            modifier = Modifier.fillMaxSize().testTag(PhotoScreenTestTags.PHOTO))
      } else if (cameraPreview != null) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize().testTag(PhotoScreenTestTags.PREVIEW)) {
              cameraViewModel.getPreview().surfaceProvider = previewView.surfaceProvider
            }
      }

      FilledTonalButton(
          onClick = { cameraViewModel.viewModelScope.launch { cameraViewModel.takePhoto() } },
          modifier =
              Modifier.align(Alignment.BottomStart).testTag(PhotoScreenTestTags.TAKE_PHOTO)) {
            Text(text = "Take photo")
          }

      FilledTonalButton(
          onClick = { cameraViewModel.viewModelScope.launch { cameraViewModel.deletePhoto() } },
          modifier =
              Modifier.align(Alignment.BottomCenter).testTag(PhotoScreenTestTags.DELETE_PHOTO)) {
            Text(text = "Delete photo")
          }
    }
  } else {
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
            isGranted ->
          hasPermission = isGranted
        }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Camera permission is required to use this app.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag(PhotoScreenTestTags.NO_PERMISSION))
            Button(
                onClick = { launcher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.testTag(PhotoScreenTestTags.GRANT_PERMISSION)) {
                  Text("Grant permission")
                }
          }
    }
  }
}
