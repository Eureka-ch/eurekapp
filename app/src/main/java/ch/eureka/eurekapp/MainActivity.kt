/*
Portions of the code in this file are copy-pasted from the Bootcamp provided by the SwEnt staff.
 */
package ch.eureka.eurekapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import ch.eureka.eurekapp.model.authentication.AuthRepository
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.navigation.NavigationMenu
import ch.eureka.eurekapp.resources.C
import ch.eureka.eurekapp.services.notifications.LocalFirestoreMessagingServiceCompanion
import ch.eureka.eurekapp.ui.authentication.SignInScreen
import ch.eureka.eurekapp.ui.notifications.NotificationPreferencesScreen
import ch.eureka.eurekapp.ui.theme.EurekappTheme
import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient

/**
 * Provide an OkHttpClient client for network requests.
 *
 * Property `client` is mutable for testing purposes.
 *
 * @property client the Http client.
 */
object HttpClientProvider {
  var client: OkHttpClient = OkHttpClient()
}

class MainActivity : ComponentActivity() {
  private lateinit var auth: FirebaseAuth
  private lateinit var authRepository: AuthRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ConnectivityObserverProvider.initialize(this)
    setContent {
      EurekappTheme {
        val notificationType = intent.getStringExtra(LocalFirestoreMessagingServiceCompanion
            .intentNotificationTypeString)
        val notificationId = intent.getStringExtra(LocalFirestoreMessagingServiceCompanion
            .intentNotificationIdString)
        val notificationProjectId = intent.getStringExtra(LocalFirestoreMessagingServiceCompanion
            .intentNotificationProjectId)
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              Eurekapp(
                notificationType = notificationType,
                notificationId = notificationId,
                notificationProjectId = notificationProjectId
              )
            }
      }
    }
  }
}

@Composable
fun Eurekapp(
    context: Context = LocalContext.current,
    credentialManager: CredentialManager = CredentialManager.create(context),
    notificationType: String? = null,
    notificationId: String? = null,
    notificationProjectId: String? = null
) {

  var signedIn by remember { mutableStateOf(false) }
  if (!signedIn) {
    SignInScreen(credentialManager = credentialManager, onSignedIn = { signedIn = true })
  } else {
    NavigationMenu(
      notificationType = notificationType,
      notificationId = notificationId,
      notificationProjectId = notificationProjectId
    )
      //NotificationPreferencesScreen(onFinishedSettingNotifications = {})
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
