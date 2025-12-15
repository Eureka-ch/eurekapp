/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.screens.subscreens.projects.members

import android.text.format.DateUtils
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.project.MembersUiState
import ch.eureka.eurekapp.model.data.project.ProjectMembersViewModel
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.Timestamp

/** Test tags for [ProjectMembersScreen]. */
object ProjectMembersScreenTestTags {
  const val TITLE = "project_members_title"
  const val BACK_BUTTON = "project_members_back_button"
  const val REFRESH_BUTTON = "project_members_refresh_button"
  const val LOADING = "project_members_loading"
  const val ERROR = "project_members_error"
  const val EMPTY_STATE = "project_members_empty_state"
  const val MEMBERS_LIST = "project_members_list"
}

/** Size of the profile picture for each member in the list of members. */
private val PROFILE_PICTURE_SIZE = 48.dp

/**
 * Composable function that displays the Project Members screen.
 *
 * It shows a list of members associated with the given project, including their online status.
 *
 * @param projectId The unique identifier of the project.
 * @param onBackClick Callback function invoked when the back button is clicked.
 * @param projectMembersViewModel The [ProjectMembersViewModel] used to manage the state of this
 *   screen. Defaults to a new instance using the [ProjectMembersViewModel.Factory].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectMembersScreen(
    projectId: String,
    onBackClick: () -> Unit,
    projectMembersViewModel: ProjectMembersViewModel =
        viewModel(
            factory =
                ProjectMembersViewModel.Factory(
                    projectId = projectId,
                ))
) {
  val uiState by projectMembersViewModel.uiState.collectAsState()

  Scaffold(
      topBar = {
        EurekaTopBar(
            title =
                when (val state = uiState) {
                  is MembersUiState.Success -> state.projectName
                  is MembersUiState.Loading,
                  is MembersUiState.Error -> "Project members"
                },
            navigationIcon = {
              IconButton(
                  onClick = onBackClick,
                  modifier = Modifier.testTag(ProjectMembersScreenTestTags.BACK_BUTTON)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White)
                  }
            },
            actions = {
              IconButton(
                  onClick = { projectMembersViewModel.loadMembers() },
                  modifier = Modifier.testTag(ProjectMembersScreenTestTags.REFRESH_BUTTON)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                  }
            })
      }) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          when (val state = uiState) {
            is MembersUiState.Loading -> {
              CircularProgressIndicator(
                  modifier =
                      Modifier.align(Alignment.Center)
                          .testTag(ProjectMembersScreenTestTags.LOADING))
            }
            is MembersUiState.Error -> {
              Text(
                  text = state.message,
                  color = MaterialTheme.colorScheme.error,
                  modifier =
                      Modifier.align(Alignment.Center).testTag(ProjectMembersScreenTestTags.ERROR))
            }
            is MembersUiState.Success -> {
              if (state.members.isEmpty()) {
                Text(
                    text = "No members found in this project.",
                    modifier =
                        Modifier.align(Alignment.Center)
                            .testTag(ProjectMembersScreenTestTags.EMPTY_STATE))
              } else {
                MembersList(
                    members = state.members,
                    isUserOnline = { lastActive ->
                      projectMembersViewModel.isUserOnline(lastActive)
                    })
              }
            }
          }
        }
      }
}

/**
 * Composable function that renders the list of members.
 *
 * @param members The list of [User] objects to display.
 * @param isUserOnline Function that returns true if the user is online, false otherwise.
 */
@Composable
fun MembersList(members: List<User>, isUserOnline: (Timestamp) -> Boolean) {
  LazyColumn(modifier = Modifier.fillMaxSize().testTag(ProjectMembersScreenTestTags.MEMBERS_LIST)) {
    item {
      Text(
          text = "Members — ${members.size}",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp))
    }

    items(members) { user -> MemberItem(user = user, isUserOnline = isUserOnline) }
  }
}

/**
 * Composable function that renders a single member item in the list.
 *
 * Displays the user's profile picture, name, and online status.
 *
 * @param user The [User] object to display.
 * @param isUserOnline Function that returns true if the user is online, false otherwise.
 * @throws [IllegalArgumentException] if user's display name is blank
 */
@Composable
fun MemberItem(user: User, isUserOnline: (Timestamp) -> Boolean) {

  require(user.displayName.isNotBlank())

  val isOnline = remember(user.lastActive) { isUserOnline(user.lastActive) }

  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically) {
        // Profile Picture with Online Indicator
        Box(contentAlignment = Alignment.BottomEnd) {
          if (user.photoUrl.isNotEmpty()) {
            val fallbackPainter = rememberVectorPainter(Icons.Default.Person)
            val backgroundColor = MaterialTheme.colorScheme.primaryContainer
            AsyncImage(
                model =
                    ImageRequest.Builder(LocalContext.current)
                        .data(user.photoUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = "Profile picture of ${user.displayName}",
                error = fallbackPainter,
                placeholder = fallbackPainter,
                fallback = fallbackPainter,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier.size(PROFILE_PICTURE_SIZE)
                        .clip(CircleShape)
                        .background(backgroundColor))
          } else {
            // Fallback avatar
            Box(
                modifier =
                    Modifier.size(PROFILE_PICTURE_SIZE)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center) {
                  Icon(
                      Icons.Default.Person,
                      contentDescription = "Default person",
                      tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
          }

          // The Green/Grey Dot
          Box(
              modifier =
                  Modifier.size(14.dp) // Size of the dot
                      .clip(CircleShape)
                      .background(if (isOnline) Color.Green else Color.Gray)
                      .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
              text = user.displayName,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium,
              color =
                  if (isOnline) MaterialTheme.colorScheme.onSurface
                  else MaterialTheme.colorScheme.onSurfaceVariant)

          Text(
              text = getStatusText(user.lastActive, isOnline),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
}

/**
 * Helper to generate the status text string for display.
 *
 * @param lastActive The timestamp of the user's last activity.
 * @param isOnline Boolean indicating if the user is currently considered online.
 * @return A string representing the user's status.
 */
fun getStatusText(lastActive: Timestamp, isOnline: Boolean): String {
  if (isOnline) return "Online"

  if (lastActive == Timestamp(0, 0)) return "Never active"

  val now = System.currentTimeMillis()
  val time = lastActive.toDate().time

  return try {
    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS).toString()
  } catch (e: Exception) {
    Log.e("ProjectMemberScreen", "Fail to construct create status text.")
    throw e
  }
}
