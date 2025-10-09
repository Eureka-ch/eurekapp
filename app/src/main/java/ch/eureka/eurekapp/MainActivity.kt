package ch.eureka.eurekapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.camera.CameraViewModel
import ch.eureka.eurekapp.navigation.NavigationMenu
import ch.eureka.eurekapp.resources.C
import ch.eureka.eurekapp.ui.photos.PhotoScreen
import ch.eureka.eurekapp.ui.theme.EurekappTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      EurekappTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              NavigationMenu()
            }
      }
    }
  }
}

@Composable
fun Camera() {
  val viewModel: CameraViewModel = viewModel()
  val lifecycleOwner = LocalLifecycleOwner.current
  PhotoScreen(viewModel)
  DisposableEffect(lifecycleOwner) {
    viewModel.startCamera(lifecycleOwner)
    onDispose { viewModel.unbindCamera() }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier.semantics { testTag = C.Tag.greeting })
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  EurekappTheme { Greeting("Android") }
}
