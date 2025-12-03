package ch.eureka.eurekapp.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.model.camera.CameraViewModel
import ch.eureka.eurekapp.ui.camera.PhotoViewer
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

object CameraScreenTestTags {
  const val PHOTO = "photo"
  const val PREVIEW = "preview"
  const val TAKE_PHOTO = "take_photo"
  const val DELETE_PHOTO = "delete_photo"
  const val SAVE_PHOTO = "save_photo"
  const val NO_PERMISSION = "no_permission"
  const val GRANT_PERMISSION = "grant_permission"
  const val BACK_BUTTON = "back_button"
}

// Portions of this code (and KDoc) were generated with the help of Grok.
/**
 * A composable screen for capturing and managing photos using the device's camera. This screen
 * displays a live camera preview or the captured photo image.
 *
 * @param navigationController The NavHostController for handling navigation actions.
 * @param cameraViewModel The CameraViewModel instance responsible for managing camera state.
 */
@Composable
fun CameraScreen(
    navigationController: NavHostController,
    cameraViewModel: CameraViewModel,
) {
  val cameraState by cameraViewModel.photoState.collectAsState()
  val cameraPreview by cameraViewModel.preview.collectAsState()
  val context = LocalContext.current
  val previewView = remember { PreviewView(context) }
  var hasPermission by remember {
    mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED)
  }

  Scaffold(
      topBar = {
        EurekaTopBar(
            title = "Camera",
            navigationIcon = {
              BackButton(
                  onClick = { navigationController.popBackStack() },
                  modifier = Modifier.testTag(CameraScreenTestTags.BACK_BUTTON))
            })
      },
      content = { paddingValues ->
        if (hasPermission) {
          Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (cameraState.picture != null) {
              cameraState.picture?.let { uri ->
                PhotoViewer(
                    uri, modifier = Modifier.fillMaxSize().testTag(CameraScreenTestTags.PHOTO))
              }
              OutlinedButton(
                  onClick = { cameraViewModel.deletePhoto() },
                  colors = EurekaStyles.outlinedButtonColors(),
                  modifier =
                      Modifier.align(Alignment.BottomStart)
                          .testTag(CameraScreenTestTags.DELETE_PHOTO)) {
                    Text(text = "Delete photo")
                  }
              Button(
                  onClick = {
                    navigationController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("photoUri", cameraState.picture.toString())
                    navigationController.popBackStack()
                  },
                  colors = EurekaStyles.primaryButtonColors(),
                  modifier =
                      Modifier.align(Alignment.BottomEnd)
                          .testTag(CameraScreenTestTags.SAVE_PHOTO)) {
                    Text(text = "Save photo")
                  }
            } else if (cameraPreview != null) {
              AndroidView(
                  factory = { previewView },
                  modifier = Modifier.fillMaxSize().testTag(CameraScreenTestTags.PREVIEW)) {
                    cameraViewModel.getPreview().surfaceProvider = previewView.surfaceProvider
                  }
              OutlinedButton(
                  onClick = { cameraViewModel.takePhoto() },
                  colors = EurekaStyles.outlinedButtonColors(),
                  modifier =
                      Modifier.align(Alignment.BottomCenter)
                          .testTag(CameraScreenTestTags.TAKE_PHOTO)) {
                    Text(text = "Take photo")
                  }
            }
          }
        } else {
          val launcher =
              rememberLauncherForActivityResult(
                  contract = ActivityResultContracts.RequestPermission()) { isGranted ->
                    hasPermission = isGranted
                  }
          Box(
              modifier = Modifier.fillMaxSize().padding(paddingValues),
              contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                      Text(
                          text = "Camera permission is required.",
                          style = MaterialTheme.typography.bodyLarge,
                          modifier = Modifier.testTag(CameraScreenTestTags.NO_PERMISSION))
                      Button(
                          onClick = { launcher.launch(Manifest.permission.CAMERA) },
                          colors = EurekaStyles.primaryButtonColors(),
                          modifier = Modifier.testTag(CameraScreenTestTags.GRANT_PERMISSION)) {
                            Text("Grant permission")
                          }
                    }
              }
        }
      })
}

@Composable
fun Camera(
    navigationController: NavHostController = rememberNavController(),
) {
  val viewModel: CameraViewModel = viewModel()
  val lifecycleOwner = LocalLifecycleOwner.current
  val context = LocalContext.current

  CameraScreen(navigationController, cameraViewModel = viewModel)
  DisposableEffect(lifecycleOwner) {
    viewModel.startCamera(context, lifecycleOwner)
    onDispose { viewModel.unbindCamera() }
  }
}
