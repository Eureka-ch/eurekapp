/* Portions of this code were generated with the help of Gemini and chatGPT (GPT-5). */
package ch.eureka.eurekapp.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectSelectionScreenViewModel
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/** Object holding test tags for UI testing on ProjectSelectionScreen. */
object ProjectSelectionScreenTestTags {
  const val CREATE_PROJECT_BUTTON = "create project button"
  const val INPUT_TOKEN_BUTTON = "input token button"

  fun getShowMembersButtonTestTag(projectId: String): String {
    return "Show members button test tag for $projectId"
  }

  fun getInviteButtonTestTag(projectId: String): String {
    return "Invite button test tag for $projectId"
  }
}

/**
 * Main composable for the Project Selection Screen. Displays a "Create Project" button at the top
 * and a scrollable list of ProjectCards.
 *
 * @param onCreateProjectRequest lambda triggered when the create button is clicked.
 * @param projectSelectionScreenViewModel optional ViewModel to fetch project data.
 * @param onGenerateInviteRequest lambda triggered when the user wants to generate an invite
 * @param onSeeProjectMembers callback triggered to navigate to the project members screen for the
 *   corresponding project.
 */
@Composable
fun ProjectSelectionScreen(
    onCreateProjectRequest: () -> Unit = {},
    onInputTokenRequest: () -> Unit = {},
    onGenerateInviteRequest: (String) -> Unit = {},
    onSeeProjectMembers: (String) -> Unit = {},
    projectSelectionScreenViewModel: ProjectSelectionScreenViewModel = viewModel()
) {
  val context = LocalContext.current
  var hasNotificationsPermission by remember {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      mutableStateOf(
          ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
              PackageManager.PERMISSION_GRANTED)
    } else {
      mutableStateOf(true)
    }
  }

  val currentUser =
      remember { projectSelectionScreenViewModel.getCurrentUser() }.collectAsState(null)

  val projectsList =
      remember { projectSelectionScreenViewModel.getProjectsForUser() }.collectAsState(listOf())

  val launcher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
          isGranted ->
        hasNotificationsPermission = isGranted
        if (isGranted) {
          Toast.makeText(context, "Notifications permission granted!", Toast.LENGTH_SHORT).show()
        } else {
          Toast.makeText(
                  context, "You will not be able to receive notifications!", Toast.LENGTH_SHORT)
              .show()
        }
      }

  LaunchedEffect(Unit) {
    if (!hasNotificationsPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  val listState = rememberLazyListState()

  Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
    // Header section with action buttons
    Column(
        modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
          Button(
              onClick = { onCreateProjectRequest() },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(ProjectSelectionScreenTestTags.CREATE_PROJECT_BUTTON),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.primary,
                      contentColor = Color.White)) {
                Text(
                    "+ Create Project",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
              }
          OutlinedButton(
              onClick = { onInputTokenRequest() },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(ProjectSelectionScreenTestTags.INPUT_TOKEN_BUTTON),
              colors =
                  ButtonDefaults.outlinedButtonColors(
                      contentColor = MaterialTheme.colorScheme.primary)) {
                Text(
                    "Input Project Token",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
              }
        }

    // Projects list
    if (projectsList.value.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
              Text(
                  "No projects yet",
                  style = MaterialTheme.typography.titleLarge,
                  color = Color(0xFF0F172A),
                  fontWeight = FontWeight.Bold)
              Text(
                  "Create your first project to get started",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color(0xFF64748B))
            }
      }
    } else {
      LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = Spacing.lg),
          verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            items(projectsList.value) { project ->
              ProjectCard(
                  project,
                  projectSelectionScreenViewModel,
                  onSeeProjectMembers,
                  onGenerateInviteRequest,
                  currentUser.value)
            }
          }
    }
  }
}

/**
 * Composable representing a single project card. Displays project name, description, users, status,
 * and a show project members button.
 *
 * @param project the Project object to display.
 * @param projectSelectionScreenViewModel ViewModel used to fetch users for the project.
 * @param onSeeProjectMembers lambda triggered when the show project members button is clicked.
 * @param onGenerateInviteRequest lambda triggered when the user requests to generate an invite
 * @param currentUser the user who using the app
 */
@Composable
private fun ProjectCard(
    project: Project,
    projectSelectionScreenViewModel: ProjectSelectionScreenViewModel,
    onSeeProjectMembers: (String) -> Unit,
    onGenerateInviteRequest: (String) -> Unit,
    currentUser: User?
) {
  var isPressed by remember { mutableStateOf(false) }
  val scale by
      animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f, animationSpec = tween(150))

  val projectUsers =
      projectSelectionScreenViewModel
          .getProjectUsersInformation(project.projectId)
          .collectAsState(listOf())

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .scale(scale)
              .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp)),
      shape = RoundedCornerShape(24.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(24.dp))
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        Color.White,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(1000f, 1000f)))) {
              Column(modifier = Modifier.padding(24.dp)) {
                // Header: Title + Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold)
                      }
                      ProjectStatusDisplay(project.status)
                    }

                Spacer(modifier = Modifier.height(16.dp))

                // Description with icon
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()) {
                      Box(
                          modifier =
                              Modifier.size(36.dp)
                                  .background(
                                      color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                      shape = RoundedCornerShape(10.dp)),
                          contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp))
                          }
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.description.ifEmpty { "No description provided" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Medium)
                      }
                    }

                Spacer(modifier = Modifier.height(16.dp))

                // Members preview
                if (projectUsers.value.isNotEmpty()) {
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier =
                                Modifier.size(32.dp)
                                    .background(
                                        color = Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center) {
                              Icon(
                                  imageVector = Icons.Default.Person,
                                  contentDescription = null,
                                  tint = Color(0xFF64748B),
                                  modifier = Modifier.size(16.dp))
                            }
                        Text(
                            text = "${projectUsers.value.size} members",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.SemiBold)
                      }
                  Spacer(modifier = Modifier.height(16.dp))
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      TextButton(
                          onClick = { onSeeProjectMembers(project.projectId) },
                          modifier =
                              Modifier.testTag(
                                  ProjectSelectionScreenTestTags.getShowMembersButtonTestTag(
                                      project.projectId)),
                          colors =
                              ButtonDefaults.textButtonColors(
                                  contentColor = MaterialTheme.colorScheme.primary)) {
                            Text(
                                "View Members",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold)
                          }
                      if (currentUser?.uid.equals(project.createdBy)) {
                        IconButton(
                            onClick = { onGenerateInviteRequest(project.projectId) },
                            modifier =
                                Modifier.testTag(
                                    ProjectSelectionScreenTestTags.getInviteButtonTestTag(
                                        project.projectId))) {
                              Icon(
                                  imageVector = Icons.Default.PersonAdd,
                                  contentDescription = "Generate Invite",
                                  tint = MaterialTheme.colorScheme.primary,
                                  modifier = Modifier.size(24.dp))
                            }
                      }
                    }
              }
            }
      }
}

/** Displays the status of a project in a modern badge style. */
@Composable
fun ProjectStatusDisplay(projectStatus: ProjectStatus) {
  val (backgroundColor, textColor) =
      when (projectStatus) {
        ProjectStatus.OPEN -> Color(0xFFF0FDF4) to Color(0xFF16A34A) // Light green
        ProjectStatus.IN_PROGRESS -> Color(0xFFFEF3C7) to Color(0xFFD97706) // Light yellow
        ProjectStatus.ARCHIVED -> Color(0xFFF1F5F9) to Color(0xFF475569) // Light gray
        ProjectStatus.COMPLETED -> Color(0xFFFEF2F2) to Color(0xFFDC2626) // Light red
      }

  Surface(
      color = backgroundColor,
      modifier = Modifier.height(28.dp),
      shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  projectStatus.name.replace("_", " "),
                  style = MaterialTheme.typography.labelMedium,
                  color = textColor,
                  fontWeight = FontWeight.SemiBold)
            }
      }
}
