package ch.eureka.eurekapp.ui.authentication

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

// Portions of this code were generated with the help of Grok.

/** Test tags for UI testing. */
object TokenEntryScreenTestTags {
  const val GREETING_TEXT = "greetingText"
  const val TOKEN_INPUT_FIELD = "tokenInputField"
  const val VALIDATE_BUTTON = "validateButton"
  const val HELP_LINK = "helpLink"
  const val ERROR_TEXT = "errorText"
  const val BACK_BUTTON = "back button"
}

/**
 * Token entry screen for users to enter an invitation token after authentication.
 *
 * This screen provides a simplified interface for entering and validating invitation tokens. Upon
 * successful validation, the user is redirected to the overview screen.
 *
 * @param tokenEntryViewModel The ViewModel managing token validation logic.
 * @param onTokenValidated Callback invoked when the token is successfully validated.
 */
@Composable
fun TokenEntryScreen(
    tokenEntryViewModel: TokenEntryViewModel = viewModel(),
    navigationController: NavHostController = rememberNavController(),
    onTokenValidated: () -> Unit = {}
) {
  val context = LocalContext.current
  val uiState by tokenEntryViewModel.uiState.collectAsState()

  // Show error message as toast
  LaunchedEffect(uiState.errorMessage) {
    uiState.errorMessage?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      tokenEntryViewModel.clearError()
    }
  }

  // Navigate to overview on successful validation
  LaunchedEffect(uiState.validationSuccess) {
    if (uiState.validationSuccess) {
      Toast.makeText(context, "Welcome to the project!", Toast.LENGTH_SHORT).show()
      onTokenValidated()
    }
  }

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        EurekaTopBar(
            navigationIcon = {
              BackButton(
                  onClick = { navigationController.popBackStack() },
                  modifier = Modifier.testTag(TokenEntryScreenTestTags.BACK_BUTTON))
            })
      },
  ) { padding ->
    Column(
        modifier =
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
          // Greeting
          Text(
              text = "Hello ${uiState.userName} 👋",
              style =
                  MaterialTheme.typography.headlineMedium.copy(
                      fontSize = 28.sp, fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onBackground,
              modifier = Modifier.testTag(TokenEntryScreenTestTags.GREETING_TEXT))

          Spacer(modifier = Modifier.height(8.dp))

          // Instructions
          Text(
              text = "Enter Access Token",
              style =
                  MaterialTheme.typography.titleLarge.copy(
                      fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onBackground)

          Text(
              text = "Paste the token you received to join a group or unlock features.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Start)

          Spacer(modifier = Modifier.height(8.dp))

          // Token input field
          OutlinedTextField(
              value = uiState.token,
              onValueChange = { tokenEntryViewModel.updateToken(it) },
              label = { Text("Token") },
              placeholder = { Text("Ex: 7F4A-93KD-XX12") },
              singleLine = true,
              enabled = !uiState.isLoading,
              modifier =
                  Modifier.fillMaxWidth().testTag(TokenEntryScreenTestTags.TOKEN_INPUT_FIELD),
              colors =
                  OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = MaterialTheme.colorScheme.primary,
                      unfocusedBorderColor = MaterialTheme.colorScheme.outline))

          // Validate button
          Button(
              onClick = { tokenEntryViewModel.validateToken() },
              enabled = !uiState.isLoading && uiState.token.isNotBlank(),
              modifier =
                  Modifier.fillMaxWidth()
                      .height(48.dp)
                      .testTag(TokenEntryScreenTestTags.VALIDATE_BUTTON),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.primary,
                      contentColor = MaterialTheme.colorScheme.onPrimary),
              shape = RoundedCornerShape(8.dp)) {
                if (uiState.isLoading) {
                  CircularProgressIndicator(
                      color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                  Text(
                      text = "Validate",
                      style =
                          MaterialTheme.typography.labelLarge.copy(
                              fontSize = 16.sp, fontWeight = FontWeight.Medium))
                }
              }

          // Help link
          HelpLink(modifier = Modifier.testTag(TokenEntryScreenTestTags.HELP_LINK))
        }
  }
}

/** Help link for users who need assistance with tokens. */
@Composable
private fun HelpLink(modifier: Modifier = Modifier) {
  val annotatedString = buildAnnotatedString {
    append("Need help? ")
    pushStringAnnotation(tag = "HELP", annotation = "help")
    withStyle(
        style =
            SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)) {
          append("How to get a token")
        }
    pop()
  }

  ClickableText(
      text = annotatedString,
      onClick = { offset ->
        annotatedString
            .getStringAnnotations(tag = "HELP", start = offset, end = offset)
            .firstOrNull()
            ?.let {
              // TODO: Navigate to help screen or show help dialog
            }
      },
      style =
          MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant),
      modifier = modifier)
}
