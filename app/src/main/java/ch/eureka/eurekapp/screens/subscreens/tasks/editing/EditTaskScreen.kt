// Co-Authored-By: Claude Opus 4.5 and Grok
package ch.eureka.eurekapp.screens.subscreens.tasks.editing

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.tasks.EditTaskState
import ch.eureka.eurekapp.model.tasks.EditTaskViewModel
import ch.eureka.eurekapp.navigation.Route
import ch.eureka.eurekapp.screens.subscreens.tasks.AttachmentsList
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.FieldInteractionMode
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskDependenciesSelectionField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskDescriptionField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskDueDateField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskReminderField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskTitleField
import ch.eureka.eurekapp.screens.subscreens.tasks.TemplateFieldsSection
import ch.eureka.eurekapp.screens.subscreens.tasks.UserAssignmentField
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

object EditTaskScreenTestTags {
  const val DELETE_TASK = "delete_task"
  const val STATUS_BUTTON = "task_status"
  const val CONFIRM_DELETE = "confirm_delete"
  const val CANCEL_DELETE = "cancel_delete"
  const val ADD_FILE = "add_file"
}

const val EDIT_SCREEN_SMALL_BUTTON_SIZE = 0.3f
const val EDIT_SCREEN_EQUAL_WEIGHT = 1f

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
  val photoUri by
      savedStateHandle?.getStateFlow("photoUri", "")?.collectAsState()
          ?: remember { mutableStateOf("") }

  val context = LocalContext.current
  val scrollState = rememberScrollState()
  var showDeleteDialog by remember { mutableStateOf(false) }
  val isConnected = true // Assume online for edit screen

  // File picker launcher for any file type
  val filePickerLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let { editTaskViewModel.addAttachment(it) }
      }

  LoadTask(taskId, editTaskState, editTaskViewModel, projectId)
  LoadAvailableTasks(editTaskState.projectId, editTaskViewModel)
  ShowErrorToast(errorMsg, editTaskViewModel, context)
  AddAttachment(photoUri, editTaskViewModel)
  HandleTaskSaved(editTaskState.taskSaved, navigationController, editTaskViewModel)
  HandleTaskDeleted(editTaskState.taskDeleted, navigationController, editTaskViewModel)
  CleanupAttachmentsOnDispose(
      editTaskState.temporaryPhotoUris, editTaskViewModel, context, navigationController)

  Scaffold(
      topBar = {
        EurekaTopBar(
            title = stringResource(R.string.edit_task_title),
            navigationIcon = {
              BackButton(
                  onClick = { navigationController.popBackStack() },
                  modifier = Modifier.testTag(CommonTaskTestTags.BACK_BUTTON))
            })
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues)
                    .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

              UserAssignmentField(
                  availableUsers = editTaskState.availableUsers,
                  selectedUserIds = editTaskState.selectedAssignedUserIds,
                  onUserToggled = { userId -> editTaskViewModel.toggleUserAssignment(userId) },
                  enabled = editTaskState.projectId.isNotEmpty())

              if (editTaskState.selectedTemplate != null) {
                TemplateFieldsSection(
                    template = editTaskState.selectedTemplate,
                    customData = editTaskState.customData,
                    onFieldValueChange = { fieldId, value ->
                      editTaskViewModel.updateCustomFieldValue(fieldId, value)
                    },
                    mode = FieldInteractionMode.EditOnly)
              }

              DependenciesSelectionSection(
                  projectId = editTaskState.projectId,
                  availableTasks = availableTasks,
                  dependingOnTasks = editTaskState.dependingOnTasks,
                  currentTaskId = editTaskState.taskId,
                  cycleError = cycleError,
                  editTaskViewModel = editTaskViewModel)

              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { navigationController.navigate(Route.Camera) },
                        colors = EurekaStyles.outlinedButtonColors(),
                        modifier =
                            Modifier.weight(EDIT_SCREEN_EQUAL_WEIGHT)
                                .testTag(CommonTaskTestTags.ADD_PHOTO)) {
                          Text(stringResource(R.string.edit_task_button_add_photo))
                        }

                    OutlinedButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        colors = EurekaStyles.outlinedButtonColors(),
                        modifier =
                            Modifier.weight(EDIT_SCREEN_EQUAL_WEIGHT)
                                .testTag(EditTaskScreenTestTags.ADD_FILE)) {
                          Text(stringResource(R.string.edit_task_button_add_file))
                        }
                  }

              AttachmentsList(
                  attachments = editTaskState.effectiveAttachments,
                  onDelete = { index ->
                    editTaskViewModel.removeAttachmentAndDelete(context, index)
                  },
                  isReadOnly = false,
                  isConnected = isConnected,
                  downloadedUrls = editTaskState.downloadedAttachmentUrls)

              Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { editTaskViewModel.setStatus(getNextStatus(editTaskState.status)) },
                    modifier =
                        Modifier.fillMaxWidth().testTag(EditTaskScreenTestTags.STATUS_BUTTON),
                    colors = EurekaStyles.outlinedButtonColors()) {
                      Text(text = editTaskState.status.name.replace("_", " "))
                    }
              }

              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !editTaskState.isSaving && !editTaskState.isDeleting,
                        modifier =
                            Modifier.fillMaxWidth(EDIT_SCREEN_SMALL_BUTTON_SIZE)
                                .testTag(EditTaskScreenTestTags.DELETE_TASK),
                        colors = EurekaStyles.outlinedButtonColors()) {
                          Text(
                              if (editTaskState.isDeleting)
                                  stringResource(R.string.edit_task_button_deleting)
                              else stringResource(R.string.edit_task_button_delete))
                        }

                    Button(
                        onClick = { editTaskViewModel.editTask(context) },
                        enabled =
                            inputValid && !editTaskState.isSaving && !editTaskState.isDeleting,
                        modifier = Modifier.fillMaxWidth().testTag(CommonTaskTestTags.SAVE_TASK),
                        colors = EurekaStyles.primaryButtonColors()) {
                          Text(
                              if (editTaskState.isSaving)
                                  stringResource(R.string.edit_task_button_saving)
                              else stringResource(R.string.edit_task_button_save))
                        }
                  }
            }
      })

  DeleteConfirmationDialog(
      showDialog = showDeleteDialog,
      onDismiss = { showDeleteDialog = false },
      onConfirm = {
        showDeleteDialog = false
        editTaskViewModel.deleteTask(projectId, taskId)
      })
}

@Composable
private fun DeleteConfirmationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
  if (!showDialog) return

  androidx.compose.material3.AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(stringResource(R.string.edit_task_confirm_deletion_title)) },
      text = { Text(stringResource(R.string.edit_task_confirm_deletion_message)) },
      confirmButton = {
        Button(
            onClick = onConfirm,
            modifier = Modifier.testTag(EditTaskScreenTestTags.CONFIRM_DELETE)) {
              Text(stringResource(R.string.edit_task_confirm_yes))
            }
      },
      dismissButton = {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.testTag(EditTaskScreenTestTags.CANCEL_DELETE)) {
              Text(stringResource(R.string.edit_task_confirm_no))
            }
      })
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
private fun LoadTask(
    taskId: String,
    editTaskState: EditTaskState,
    editTaskViewModel: EditTaskViewModel,
    projectId: String
) {
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
private fun ShowErrorToast(
    errorMsg: String?,
    editTaskViewModel: EditTaskViewModel,
    context: android.content.Context
) {
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
      editTaskViewModel.addAttachment(photoUri.toUri(), true)
    }
  }
}

@Composable
private fun HandleTaskSaved(
    taskSaved: Boolean,
    navigationController: NavHostController,
    editTaskViewModel: EditTaskViewModel
) {
  LaunchedEffect(taskSaved) {
    if (taskSaved) {
      navigationController.popBackStack()
      editTaskViewModel.resetSaveState()
    }
  }
}

@Composable
private fun HandleTaskDeleted(
    taskDeleted: Boolean,
    navigationController: NavHostController,
    editTaskViewModel: EditTaskViewModel
) {
  LaunchedEffect(taskDeleted) {
    if (taskDeleted) {
      navigationController.popBackStack()
      navigationController.popBackStack() // Pop twice to skip viewTaskScreen
      editTaskViewModel.resetDeleteState()
    }
  }
}

@Composable
private fun CleanupAttachmentsOnDispose(
    temporaryPhotoUris: List<Uri>,
    editTaskViewModel: EditTaskViewModel,
    context: android.content.Context,
    navigationController: NavHostController
) {
  val isNavigatingToCamera = remember { mutableStateOf(false) }

  LaunchedEffect(navigationController.currentBackStackEntry) {
    val currentRoute = navigationController.currentBackStackEntry?.destination?.route
    isNavigatingToCamera.value = currentRoute?.contains("Camera") == true
  }

  DisposableEffect(Unit) {
    onDispose {
      if (!isNavigatingToCamera.value) {
        editTaskViewModel.deletePhotosOnDispose(context, temporaryPhotoUris)
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
        cycleError = cycleError)
  }
}
