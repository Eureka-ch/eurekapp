package ch.eureka.eurekapp.screens.subscreens.tasks.editing

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.tasks.EditTaskState
import ch.eureka.eurekapp.model.tasks.EditTaskViewModel
import ch.eureka.eurekapp.navigation.Route
import ch.eureka.eurekapp.screens.subscreens.tasks.AttachmentsList
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.ProjectSelectionField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskDependenciesSelectionField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskDescriptionField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskDueDateField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskReminderField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskTitleField
import ch.eureka.eurekapp.screens.subscreens.tasks.UserAssignmentField
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

object EditTaskScreenTestTags {
    const val DELETE_TASK = "delete_task"
    const val STATUS_BUTTON = "task_status"
    const val CONFIRM_DELETE = "confirm_delete"
    const val CANCEL_DELETE = "cancel_delete"
}

const val EDIT_SCREEN_SMALL_BUTTON_SIZE = 0.3f

@Composable
fun EditTaskScreen(
    projectId: String,
    taskId: String,
    navigationController: NavHostController = rememberNavController(),
    editTaskViewModel: EditTaskViewModel = viewModel(),
) {
    val editTaskState by editTaskViewModel.uiState.collectAsState()
    val inputValid by editTaskViewModel.inputValid.collectAsState()
    val errorMsg = editTaskState.errorMsg
    val availableTasks by editTaskViewModel.availableTasks.collectAsState()
    val cycleError by editTaskViewModel.cycleError.collectAsState()
    var hasTouchedTitle by remember { mutableStateOf(false) }
    var hasTouchedDescription by remember { mutableStateOf(false) }
    var hasTouchedDate by remember { mutableStateOf(false) }
    val savedStateHandle = navigationController.currentBackStackEntry?.savedStateHandle
    val photoUri by savedStateHandle?.getStateFlow("photoUri", "")?.collectAsState()
        ?: remember { mutableStateOf("") }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var isNavigatingToCamera by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LoadTask(taskId, editTaskState, editTaskViewModel, projectId)
    LoadAvailableTasks(editTaskState.projectId, editTaskViewModel)
    ShowErrorToast(errorMsg, editTaskViewModel, context)
    AddAttachment(photoUri, editTaskViewModel)
    HandleTaskSaved(editTaskState.taskSaved, navigationController, editTaskViewModel)
    HandleTaskDeleted(editTaskState.taskDeleted, navigationController, editTaskViewModel)
    CleanupAttachmentsOnDispose(isNavigatingToCamera, editTaskState.attachmentUris, editTaskViewModel, context)

    Scaffold(content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TaskTitleField(
                value = editTaskState.title,
                onValueChange = { editTaskViewModel.setTitle(it) },
                hasTouched = hasTouchedTitle,
                onFocusChanged = { hasTouchedTitle = true })

            TaskDescriptionField(
                value = editTaskState.description,
                onValueChange = { editTaskViewModel.setDescription(it) },
                hasTouched = hasTouchedDescription,
                onFocusChanged = { hasTouchedDescription = true })

            TaskDueDateField(
                value = editTaskState.dueDate,
                onValueChange = { editTaskViewModel.setDueDate(it) },
                hasTouched = hasTouchedDate,
                onFocusChanged = { hasTouchedDate = true },
                dateRegex = editTaskViewModel.dateRegex)

            TaskReminderField(
                value = editTaskState.reminderTime,
                onValueChange = { editTaskViewModel.setReminderTime(it) })

            ProjectSelectionField(
                projects = editTaskState.availableProjects,
                selectedProjectId = editTaskState.projectId,
                onProjectSelected = { projectId -> editTaskViewModel.setProjectId(projectId) })

            UserAssignmentField(
                availableUsers = editTaskState.availableUsers,
                selectedUserIds = editTaskState.selectedAssignedUserIds,
                onUserToggled = { userId -> editTaskViewModel.toggleUserAssignment(userId) },
                enabled = editTaskState.projectId.isNotEmpty())

            DependenciesSelectionSection(
                projectId = editTaskState.projectId,
                availableTasks = availableTasks,
                dependingOnTasks = editTaskState.dependingOnTasks,
                currentTaskId = editTaskState.taskId,
                cycleError = cycleError,
                editTaskViewModel = editTaskViewModel
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = {
                        isNavigatingToCamera = true
                        navigationController.navigate(Route.Camera)
                    },
                    colors = EurekaStyles.outlinedButtonColors(),
                    modifier = Modifier.fillMaxWidth(EDIT_SCREEN_SMALL_BUTTON_SIZE)
                        .testTag(CommonTaskTestTags.ADD_PHOTO)) {
                    Text("Add Photo")
                }
                OutlinedButton(
                    onClick = {
                        editTaskViewModel.setStatus(getNextStatus(editTaskState.status))
                    },
                    modifier = Modifier.fillMaxWidth().testTag(EditTaskScreenTestTags.STATUS_BUTTON)) {
                    Text(text = editTaskState.status.name.replace("_", " "))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    enabled = !editTaskState.isSaving && !editTaskState.isDeleting,
                    modifier = Modifier.fillMaxWidth(EDIT_SCREEN_SMALL_BUTTON_SIZE)
                        .testTag(EditTaskScreenTestTags.DELETE_TASK),
                    colors = EurekaStyles.outlinedButtonColors()) {
                    Text(if (editTaskState.isDeleting) "Deleting..." else "Delete Task")
                }

                Button(
                    onClick = { editTaskViewModel.editTask(context) },
                    enabled = inputValid && !editTaskState.isSaving && !editTaskState.isDeleting,
                    modifier = Modifier.fillMaxWidth().testTag(CommonTaskTestTags.SAVE_TASK),
                    colors = EurekaStyles.primaryButtonColors()) {
                    Text(if (editTaskState.isSaving) "Saving..." else "Save")
                }
            }

            val allAttachments = editTaskState.attachmentUrls + editTaskState.attachmentUris
            AttachmentsList(
                attachments = allAttachments,
                onDelete = { index -> editTaskViewModel.removeAttachmentAndDelete(context, index) })
        }
    })

    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete the task?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        editTaskViewModel.deleteTask(projectId, taskId)
                    },
                    modifier = Modifier.testTag(EditTaskScreenTestTags.CONFIRM_DELETE)) {
                    Text("Yes")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.testTag(EditTaskScreenTestTags.CANCEL_DELETE)) {
                    Text("No")
                }
            })
    }
}

fun getNextStatus(currentStatus: TaskStatus): TaskStatus {
    return when (currentStatus) {
        TaskStatus.TODO -> TaskStatus.IN_PROGRESS
        TaskStatus.IN_PROGRESS -> TaskStatus.COMPLETED
        TaskStatus.COMPLETED -> TaskStatus.CANCELLED
        TaskStatus.CANCELLED -> TaskStatus.TODO
    }
}

@Composable
private fun LoadTask(taskId: String, editTaskState: EditTaskState, editTaskViewModel: EditTaskViewModel, projectId: String) {
    LaunchedEffect(taskId) {
        if (!editTaskState.isDeleting && !editTaskState.taskDeleted) {
            editTaskViewModel.loadTask(projectId, taskId)
        }
    }
}

@Composable
private fun LoadAvailableTasks(projectId: String, editTaskViewModel: EditTaskViewModel) {
    LaunchedEffect(projectId) {
        if (projectId.isNotEmpty()) {
            editTaskViewModel.loadAvailableTasks(projectId)
        }
    }
}

@Composable
private fun ShowErrorToast(errorMsg: String?, editTaskViewModel: EditTaskViewModel, context: android.content.Context) {
    LaunchedEffect(errorMsg) {
        if (errorMsg != null) {
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            editTaskViewModel.clearErrorMsg()
        }
    }
}

@Composable
private fun AddAttachment(photoUri: String, editTaskViewModel: EditTaskViewModel) {
    LaunchedEffect(photoUri) {
        if (photoUri.isNotEmpty()) {
            editTaskViewModel.addAttachment(photoUri.toUri())
        }
    }
}

@Composable
private fun HandleTaskSaved(taskSaved: Boolean, navigationController: NavHostController, editTaskViewModel: EditTaskViewModel) {
    LaunchedEffect(taskSaved) {
        if (taskSaved) {
            navigationController.popBackStack()
            editTaskViewModel.resetSaveState()
        }
    }
}

@Composable
private fun HandleTaskDeleted(taskDeleted: Boolean, navigationController: NavHostController, editTaskViewModel: EditTaskViewModel) {
    LaunchedEffect(taskDeleted) {
        if (taskDeleted) {
            navigationController.popBackStack()
            editTaskViewModel.resetDeleteState()
        }
    }
}

@Composable
private fun CleanupAttachmentsOnDispose(
    isNavigatingToCamera: Boolean,
    attachmentUris: List<Uri>,
    editTaskViewModel: EditTaskViewModel,
    context: android.content.Context
) {
    DisposableEffect(Unit) {
        onDispose {
            if (!isNavigatingToCamera) {
                editTaskViewModel.deletePhotosOnDispose(context, attachmentUris)
            }
        }
    }
}

@Composable
private fun DependenciesSelectionSection(
    projectId: String,
    availableTasks: List<Task>,
    dependingOnTasks: List<String>,
    currentTaskId: String,
    cycleError: String?,
    editTaskViewModel: EditTaskViewModel
) {
    if (projectId.isNotEmpty()) {
        TaskDependenciesSelectionField(
            availableTasks = availableTasks,
            selectedDependencyIds = dependingOnTasks,
            onDependencyAdded = { taskId -> editTaskViewModel.addDependency(taskId) },
            onDependencyRemoved = { taskId -> editTaskViewModel.removeDependency(taskId) },
            currentTaskId = currentTaskId,
            cycleError = cycleError
        )
    }
}
