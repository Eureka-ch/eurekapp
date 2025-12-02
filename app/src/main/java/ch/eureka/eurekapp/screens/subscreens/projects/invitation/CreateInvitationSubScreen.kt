package ch.eureka.eurekapp.screens.subscreens.projects.invitation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.invitation.CreateInvitationViewModel
import ch.eureka.eurekapp.model.data.invitation.Invitation
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.BorderGrayColor
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.GrayTextColor2
import ch.eureka.eurekapp.ui.theme.LightColorScheme
import ch.eureka.eurekapp.ui.theme.Typography

object CreateInvitationSubScreen {
  const val CREATE_INVITATION_BUTTON = "create invitation button"
  const val COPY_INVITATION_BUTTON = "copy invitation button"
}

@Composable
fun CreateInvitationSubscreen(
    projectId: String,
    createInvitationViewModel: CreateInvitationViewModel = viewModel(),
    onInvitationCreate: () -> Unit
) {

  val createInvitationToken = remember { mutableStateOf<Invitation?>(null) }
  val createdInvitation = remember { mutableStateOf(false) }

  val copyToClipBoard = remember { derivedStateOf { createdInvitation.value } }

  val errorText = remember { mutableStateOf("") }

  val context = LocalContext.current
  Column(
      modifier = Modifier.fillMaxSize().background(color = LightColorScheme.background),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top) {
        Column(modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp).weight(2f)) {
          Text(
              text = "Create Invitation Token",
              style = Typography.titleLarge,
              fontWeight = FontWeight(600))
          Text(
              text = "Invite collaborators with a role, permissions and expiration rules.",
              style = Typography.titleMedium,
              color = GrayTextColor2)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(8f)) {
              // project creation
              Surface(
                  modifier =
                      Modifier.border(
                              border = BorderStroke(width = 1.dp, color = BorderGrayColor),
                              shape = RoundedCornerShape(16.dp))
                          .fillMaxWidth(0.95f)
                          .fillMaxHeight(0.6f),
                  shadowElevation = 3.dp,
                  color = Color.White,
                  shape = RoundedCornerShape(16.dp)) {
                    Column(
                        modifier = Modifier.padding(vertical = 5.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                          Row(
                              modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                              horizontalArrangement = Arrangement.Center,
                              verticalAlignment = Alignment.CenterVertically) {
                                if (createInvitationToken.value != null) {
                                  Text(
                                      createInvitationToken.value!!.token,
                                      textAlign = TextAlign.Center,
                                      style = Typography.titleLarge,
                                      fontWeight = FontWeight(600))
                                }
                              }

                          Row {
                            FilledTonalButton(
                                modifier =
                                    Modifier.width(190.dp)
                                        .height(80.dp)
                                        .padding(horizontal = 15.dp)
                                        .testTag(
                                            CreateInvitationSubScreen.CREATE_INVITATION_BUTTON),
                                shape = RoundedCornerShape(7.dp),
                                enabled = !createdInvitation.value,
                                colors =
                                    ButtonDefaults.filledTonalButtonColors(
                                        containerColor = LightColorScheme.primary),
                                onClick = {
                                  val invitationToCreate =
                                      Invitation(
                                          token = IdGenerator.generateUniqueToken(),
                                          projectId = projectId,
                                      )
                                  createInvitationViewModel.createInvitation(
                                      invitationToCreate,
                                      onFailureCallback = { error ->
                                        errorText.value = error.message.toString()
                                      },
                                      onSuccessCallback = {
                                        createdInvitation.value = true
                                        createInvitationToken.value = invitationToCreate
                                        onInvitationCreate()
                                      })
                                }) {
                                  Text(
                                      "Create invitation",
                                      style = Typography.titleSmall,
                                      fontWeight = FontWeight(500),
                                      color = LightColorScheme.surface,
                                      textAlign = TextAlign.Center)
                                }

                            FilledTonalButton(
                                modifier =
                                    Modifier.width(190.dp)
                                        .height(80.dp)
                                        .padding(horizontal = 10.dp)
                                        .testTag(CreateInvitationSubScreen.COPY_INVITATION_BUTTON),
                                shape = RoundedCornerShape(7.dp),
                                enabled = copyToClipBoard.value,
                                colors =
                                    ButtonDefaults.filledTonalButtonColors(
                                        containerColor = LightColorScheme.primary),
                                onClick = {
                                  if (createInvitationToken.value != null) {
                                    copyToClipboard(context, createInvitationToken.value!!.token)
                                    Toast.makeText(
                                            context, "Copied to clipboard!", Toast.LENGTH_SHORT)
                                        .show()
                                  }
                                }) {
                                  Text(
                                      "Copy to clipboard",
                                      style = Typography.titleSmall,
                                      fontWeight = FontWeight(500),
                                      color = LightColorScheme.surface,
                                      textAlign = TextAlign.Center)
                                }
                          }

                          Row { Text(errorText.value) }
                        }
                  }
            }
      }
}

fun copyToClipboard(context: Context, text: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  val clip = ClipData.newPlainText("label", text)
  clipboard.setPrimaryClip(clip)
}
