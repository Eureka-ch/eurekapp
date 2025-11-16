package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.task.determinePriority
import ch.eureka.eurekapp.model.data.task.getDaysUntilDue
import ch.eureka.eurekapp.model.data.task.getDueDateTag
import ch.eureka.eurekapp.screens.formatDueDate
import ch.eureka.eurekapp.ui.components.EurekaTaskCard
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.ui.tasks.AutoAssignResultViewModel
import ch.eureka.eurekapp.ui.tasks.ProposedAssignment
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay

// Part of this code and documentation were generated with the help of AI.
/**
 * Screen displaying auto-assignment results with task cards and accept/reject actions.
 *
 * @param navigationController Navigation controller for back navigation
 * @param viewModel ViewModel for managing auto-assignment results
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoAssignResultScreen(
    navigationController: NavHostController,
    viewModel: AutoAssignResultViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Auto-Assign Results", fontWeight = FontWeight.Bold) },
            navigationIcon = {
              IconButton(onClick = { navigationController.popBackStack() }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            })
      }) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = Spacing.md)) {
              when {
                uiState.isLoading -> LoadingState()
                uiState.error != null -> ErrorState(uiState.error, navigationController)
                uiState.proposedAssignments.isEmpty() -> EmptyState(navigationController)
                else -> {
                  // Success state - show proposed assignments
                  val acceptedCount = uiState.proposedAssignments.count { it.isAccepted }
                  val totalCount = uiState.proposedAssignments.size

                  // Header with summary and actions
                  Column(modifier = Modifier.padding(vertical = Spacing.md)) {
                    Text(
                        text = "Review Assignments",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "$acceptedCount of $totalCount assignments selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(Spacing.md))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                          OutlinedButton(
                              onClick = { viewModel.acceptAll() }, modifier = Modifier.weight(1f)) {
                                Text("Accept All")
                              }
                          OutlinedButton(
                              onClick = { viewModel.rejectAll() }, modifier = Modifier.weight(1f)) {
                                Text("Reject All")
                              }
                        }
                  }

                  Spacer(modifier = Modifier.height(Spacing.md))

                  // List of proposed assignments
                  LazyColumn(
                      verticalArrangement = Arrangement.spacedBy(Spacing.md),
                      modifier = Modifier.weight(1f)) {
                        items(uiState.proposedAssignments, key = { it.task.taskID }) { assignment ->
                          ProposedAssignmentCard(
                              assignment = assignment,
                              onAccept = { viewModel.acceptAssignment(assignment) },
                              onReject = { viewModel.rejectAssignment(assignment) })
                        }
                      }

                  // Apply button
                  Spacer(modifier = Modifier.height(Spacing.md))
                  Button(
                      onClick = { viewModel.applyAcceptedAssignments() },
                      enabled = !uiState.isApplying && acceptedCount > 0,
                      modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.md)) {
                        if (uiState.isApplying) {
                          CircularProgressIndicator(
                              modifier = Modifier.size(16.dp),
                              color = MaterialTheme.colorScheme.onPrimary)
                          Spacer(modifier = Modifier.width(Spacing.sm))
                          Text("Applying...")
                        } else {
                          Text("Apply Selected Assignments ($acceptedCount)")
                        }
                      }

                  // Show result message if applied
                  if (uiState.appliedCount > 0) {
                    LaunchedEffect(uiState.appliedCount) {
                      // Navigate back after a short delay
                      delay(1500)
                      navigationController.popBackStack()
                    }
                    Text(
                        text = "✓ ${uiState.appliedCount} assignments applied successfully!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = Spacing.md))
                  }

                  // Show error if any
                  uiState.error?.let { error ->
                    Text(
                        text = "Error: $error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = Spacing.md))
                  }
                }
              }
            }
      }
}

@Composable
private fun LoadingState() {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      CircularProgressIndicator()
      Spacer(modifier = Modifier.height(Spacing.md))
      Text(
          text = "Calculating assignments...",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun ErrorState(error: String?, navigationController: NavHostController) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(Spacing.lg)) {
          Text(
              text = "Error",
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.error)
          Spacer(modifier = Modifier.height(Spacing.md))
          Text(
              text = error ?: "Unknown error",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(Spacing.lg))
          OutlinedButton(onClick = { navigationController.popBackStack() }) { Text("Go Back") }
        }
  }
}

@Composable
private fun EmptyState(navigationController: NavHostController) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(Spacing.lg)) {
          Text(
              text = "No assignments to review",
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(Spacing.md))
          Text(
              text = "All tasks are already assigned or no unassigned tasks found.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(Spacing.lg))
          OutlinedButton(onClick = { navigationController.popBackStack() }) { Text("Go Back") }
        }
  }
}

/** Card displaying a proposed assignment with accept/reject buttons. */
@Composable
private fun ProposedAssignmentCard(
    assignment: ProposedAssignment,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
  val task = assignment.task
  val now = Timestamp.now()
  val daysUntilDue = getDaysUntilDue(task, now)
  val dueDateTag = getDueDateTag(task, now)
  val progressValue =
      when (task.status) {
        TaskStatus.COMPLETED -> 1.0f
        TaskStatus.IN_PROGRESS -> 0.5f
        TaskStatus.TODO -> 0.0f
        else -> 0.0f
      }

  Column {
    // Task card
    EurekaTaskCard(
        title = task.title,
        assignee = "→ ${assignment.proposedAssignee.displayName}",
        progressText = "${(progressValue * 100).toInt()}%",
        progressValue = progressValue,
        isCompleted = task.status == TaskStatus.COMPLETED,
        dueDate = daysUntilDue?.let { formatDueDate(it) } ?: "No due date",
        dueDateTag = dueDateTag,
        priority = determinePriority(task, now),
        onToggleComplete = {},
        onClick = {},
        modifier = Modifier.fillMaxWidth())

    // Accept/Reject buttons
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
          Button(
              onClick = onAccept,
              enabled = !assignment.isAccepted && !assignment.isRejected,
              modifier = Modifier.weight(1f),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor =
                          if (assignment.isAccepted) {
                            MaterialTheme.colorScheme.primaryContainer
                          } else {
                            MaterialTheme.colorScheme.primary
                          })) {
                Text(if (assignment.isAccepted) "Accepted" else "Accept")
              }
          OutlinedButton(
              onClick = onReject,
              enabled = !assignment.isAccepted && !assignment.isRejected,
              modifier = Modifier.weight(1f)) {
                Text(if (assignment.isRejected) "Rejected" else "Reject")
              }
        }
  }
}
