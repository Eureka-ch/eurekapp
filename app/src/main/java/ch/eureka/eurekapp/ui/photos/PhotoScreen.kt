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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.camera.CameraViewModel
import kotlinx.coroutines.launch

// Portions of this code were generated with the help of Grok.

// Before using this screen make sure the app has the camera permissions using
@Composable
fun PhotoScreen(cameraViewModel: CameraViewModel) {
  val cameraState by cameraViewModel.photoState.collectAsState()

  val context = LocalContext.current
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
            modifier = Modifier.fillMaxSize())
      } else {
        AndroidView(
            factory = { context ->
              val previewView = PreviewView(context)
              cameraViewModel.getPreview().surfaceProvider = previewView.surfaceProvider
              previewView
            },
            modifier = Modifier.fillMaxSize())
      }

      FilledTonalButton(
          onClick = { cameraViewModel.viewModelScope.launch { cameraViewModel.takePhoto() } },
          modifier = Modifier.align(Alignment.BottomStart)) {
            Text(text = "Take photo")
          }

      FilledTonalButton(
          onClick = { cameraViewModel.viewModelScope.launch { cameraViewModel.deletePhoto() } },
          modifier = Modifier.align(Alignment.BottomCenter)) {
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
                style = MaterialTheme.typography.bodyLarge)
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
              Text("Grant permission")
            }
          }
    }
  }
}
