/*
 * Co-Authored-By: Claude Sonnet 4.5, Claude Opus 4.5, and Grok
 */
package ch.eureka.eurekapp.screens.subscreens.tasks.creation

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.tasks.Attachment
import ch.eureka.eurekapp.model.tasks.CreateTaskState
import ch.eureka.eurekapp.model.tasks.CreateTaskViewModel
import ch.eureka.eurekapp.navigation.Route
import ch.eureka.eurekapp.screens.subscreens.tasks.AttachmentsList
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.FieldInteractionMode
import ch.eureka.eurekapp.screens.subscreens.tasks.ProjectSelectionField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskDependenciesSelectionField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskDescriptionField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskDueDateField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskReminderField
import ch.eureka.eurekapp.screens.subscreens.tasks.TaskTitleField
import ch.eureka.eurekapp.screens.subscreens.tasks.TemplateFieldsSection
import ch.eureka.eurekapp.screens.subscreens.tasks.TemplateSelectionField
import ch.eureka.eurekapp.screens.subscreens.tasks.UserAssignmentField
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.components.help.HelpContext
import ch.eureka.eurekapp.ui.components.help.InteractiveHelpEntryPoint
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

const val CREATE_SCREEN_PHOTO_BUTTON_SIZE = 0.3f

object CreateTaskScreenTestTags {
  const val ADD_FILE = "add_file"
  const val HELP_BUTTON = "createTaskHelpButton"
}

@Composable
fun CreateTaskScreen(
    navigationController: NavHostController = rememberNavController(),
    createTaskViewModel: CreateTaskViewModel = viewModel(),
) {
  val createTaskState by createTaskViewModel.uiState.collectAsState()
  val inputValid by createTaskViewModel.inputValid.collectAsState()
  val errorMsg = createTaskState.errorMsg
  val projectId = createTaskState.projectId
  val availableProjects = createTaskState.availableProjects
  val availableTasks by createTaskViewModel.availableTasks.collectAsState()
  val cycleError by createTaskViewModel.cycleError.collectAsState()
  var hasTouchedTitle by remember { mutableStateOf(false) }
  var hasTouchedDescription by remember { mutableStateOf(false) }
  var hasTouchedDate by remember { mutableStateOf(false) }
  val savedStateHandle = navigationController.currentBackStackEntry?.savedStateHandle
  val photoUri by
      savedStateHandle?.getStateFlow("photoUri", "")?.collectAsState()
          ?: remember { mutableStateOf("") }
  val createdTemplateId by
      savedStateHandle?.getStateFlow("createdTemplateId", "")?.collectAsState()
          ?: remember { mutableStateOf("") }
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  val isConnected by createTaskViewModel.isConnected.collectAsState()

  // Navigate back if connection is lost
  LaunchedEffect(isConnected) {
    if (!isConnected) {
      Toast.makeText(context, "Connection lost. Returning to previous screen.", Toast.LENGTH_SHORT)
          .show()
      navigationController.popBackStack()
    }
  }

  // File picker launcher for any file type
  val filePickerLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let { createTaskViewModel.addAttachment(it) }
      }

  HandleErrorToast(errorMsg, createTaskViewModel, context)
  HandlePhotoUri(photoUri, createTaskViewModel)
  HandleProjectId(projectId, createTaskViewModel)
  HandleCreatedTemplateId(createdTemplateId, createTaskViewModel, savedStateHandle)
  HandleTaskSaved(createTaskState.taskSaved, navigationController, createTaskViewModel)
  CleanupAttachmentsOnDispose(
      createTaskState.temporaryPhotoUris, createTaskViewModel, context, navigationController)

  Scaffold(
      topBar = {
        EurekaTopBar(
            title = "Create Task",
            navigationIcon = {
              BackButton(
                  onClick = { navigationController.popBackStack() },
                  modifier = Modifier.testTag(CommonTaskTestTags.BACK_BUTTON))
            },
            actions = {
              InteractiveHelpEntryPoint(
                  helpContext = HelpContext.CREATE_TASK,
                  modifier = Modifier.testTag(CreateTaskScreenTestTags.HELP_BUTTON))
            })
      },
      content = { paddingValues ->
        CreateTaskContent(
            config =
                CreateTaskContentConfig(
                    paddingValues = paddingValues,
                    scrollState = scrollState,
                    createTaskState = createTaskState,
                    createTaskViewModel = createTaskViewModel,
                    hasTouchedTitle = hasTouchedTitle,
                    onTitleFocusChanged = { _ -> hasTouchedTitle = true },
                    hasTouchedDescription = hasTouchedDescription,
                    onDescriptionFocusChanged = { _ -> hasTouchedDescription = true },
                    hasTouchedDate = hasTouchedDate,
                    onDateFocusChanged = { _ -> hasTouchedDate = true },
                    availableProjects = availableProjects,
                    projectId = projectId,
                    availableTasks = availableTasks,
                    cycleError = cycleError,
                    navigationController = navigationController,
                    context = context,
                    inputValid = inputValid,
                    filePickerLauncher = filePickerLauncher,
                    temporaryPhotoUris = createTaskState.temporaryPhotoUris,
                    isConnected = isConnected))
      })
}

@Composable
private fun HandleErrorToast(
    errorMsg: String?,
    createTaskViewModel: CreateTaskViewModel,
    context: android.content.Context
) {
  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      createTaskViewModel.clearErrorMsg()
    }
  }
}

@Composable
private fun HandlePhotoUri(photoUri: String, createTaskViewModel: CreateTaskViewModel) {
  LaunchedEffect(photoUri) {
    if (photoUri.isNotEmpty()) {
      createTaskViewModel.addAttachment(photoUri.toUri(), true)
    }
  }
}

@Composable
private fun HandleProjectId(projectId: String, createTaskViewModel: CreateTaskViewModel) {
  LaunchedEffect(projectId) {
    if (projectId.isNotEmpty()) {
      createTaskViewModel.loadAvailableTasks(projectId)
      createTaskViewModel.loadProjectMembers(projectId)
      createTaskViewModel.loadTemplatesForProject(projectId)
    }
  }
}

@Composable
private fun HandleCreatedTemplateId(
    createdTemplateId: String,
    createTaskViewModel: CreateTaskViewModel,
    savedStateHandle: androidx.lifecycle.SavedStateHandle?
) {
  LaunchedEffect(createdTemplateId) {
    if (createdTemplateId.isNotEmpty()) {
      createTaskViewModel.selectTemplate(createdTemplateId)
      savedStateHandle?.remove<String>("createdTemplateId")
    }
  }
}

@Composable
private fun HandleTaskSaved(
    taskSaved: Boolean,
    navigationController: NavHostController,
    createTaskViewModel: CreateTaskViewModel
) {
  LaunchedEffect(taskSaved) {
    if (taskSaved) {
      navigationController.popBackStack()
      createTaskViewModel.resetSaveState()
    }
  }
}

@Composable
private fun CleanupAttachmentsOnDispose(
    temporaryPhotoUris: List<Uri>,
    createTaskViewModel: CreateTaskViewModel,
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
        createTaskViewModel.deletePhotosOnDispose(context, temporaryPhotoUris)
      }
    }
  }
}

private data class CreateTaskContentConfig(
    val paddingValues: PaddingValues,
    val scrollState: ScrollState,
    val createTaskState: CreateTaskState,
    val createTaskViewModel: CreateTaskViewModel,
    val hasTouchedTitle: Boolean,
    val onTitleFocusChanged: (Boolean) -> Unit,
    val hasTouchedDescription: Boolean,
    val onDescriptionFocusChanged: (Boolean) -> Unit,
    val hasTouchedDate: Boolean,
    val onDateFocusChanged: (Boolean) -> Unit,
    val availableProjects: List<Project>,
    val projectId: String,
    val availableTasks: List<Task>,
    val cycleError: String?,
    val navigationController: NavHostController,
    val context: android.content.Context,
    val inputValid: Boolean,
    val filePickerLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Uri?>,
    val temporaryPhotoUris: List<Uri>,
    val isConnected: Boolean
)

@Composable
private fun CreateTaskContent(config: CreateTaskContentConfig) {
  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(config.paddingValues)
              .padding(16.dp)
              .verticalScroll(config.scrollState),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TaskTitleField(
            value = config.createTaskState.title,
            onValueChange = { config.createTaskViewModel.setTitle(it) },
            hasTouched = config.hasTouchedTitle,
            onFocusChanged = config.onTitleFocusChanged)

        TaskDescriptionField(
            value = config.createTaskState.description,
            onValueChange = { config.createTaskViewModel.setDescription(it) },
            hasTouched = config.hasTouchedDescription,
            onFocusChanged = config.onDescriptionFocusChanged)

        TaskDueDateField(
            value = config.createTaskState.dueDate,
            onValueChange = { config.createTaskViewModel.setDueDate(it) },
            hasTouched = config.hasTouchedDate,
            onFocusChanged = config.onDateFocusChanged,
            dateRegex = config.createTaskViewModel.dateRegex)

        TaskReminderField(
            value = config.createTaskState.reminderTime,
            onValueChange = { config.createTaskViewModel.setReminderTime(it) })

        ProjectSelectionField(
            projects = config.availableProjects,
            selectedProjectId = config.projectId,
            onProjectSelected = { projectId -> config.createTaskViewModel.setProjectId(projectId) })

        if (config.projectId.isNotEmpty()) {
          TemplateSelectionField(
              templates = config.createTaskState.availableTemplates,
              selectedTemplateId = config.createTaskState.templateId,
              onTemplateSelected = { templateId ->
                config.createTaskViewModel.selectTemplate(templateId)
              },
              onCreateTemplate = {
                config.navigationController.navigate(
                    Route.TasksSection.CreateTemplate(projectId = config.projectId))
              })

          if (config.createTaskState.selectedTemplate != null) {
            TemplateFieldsSection(
                template = config.createTaskState.selectedTemplate,
                customData = config.createTaskState.customData,
                onFieldValueChange = { fieldId, value ->
                  config.createTaskViewModel.updateCustomFieldValue(fieldId, value)
                },
                mode = FieldInteractionMode.EditOnly)
          }
        }

        UserAssignmentField(
            availableUsers = config.createTaskState.availableUsers,
            selectedUserIds = config.createTaskState.selectedAssignedUserIds,
            onUserToggled = { userId -> config.createTaskViewModel.toggleUserAssignment(userId) },
            enabled = config.projectId.isNotEmpty())

        if (config.projectId.isNotEmpty()) {
          TaskDependenciesSelectionField(
              availableTasks = config.availableTasks,
              selectedDependencyIds = config.createTaskState.dependingOnTasks,
              onDependencyAdded = { taskId -> config.createTaskViewModel.addDependency(taskId) },
              onDependencyRemoved = { taskId ->
                config.createTaskViewModel.removeDependency(taskId)
              },
              currentTaskId = "",
              cycleError = config.cycleError)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedButton(
                  onClick = { config.navigationController.navigate(Route.Camera) },
                  colors = EurekaStyles.outlinedButtonColors(),
                  modifier = Modifier.weight(1f).testTag(CommonTaskTestTags.ADD_PHOTO)) {
                    Text("Add Photo")
                  }

              OutlinedButton(
                  onClick = { config.filePickerLauncher.launch("*/*") },
                  colors = EurekaStyles.outlinedButtonColors(),
                  modifier = Modifier.weight(1f).testTag(CreateTaskScreenTestTags.ADD_FILE)) {
                    Text("Add File")
                  }
            }

        AttachmentsList(
            attachments = config.createTaskState.attachmentUris.map { Attachment.Local(it) },
            onDelete = { index ->
              config.createTaskViewModel.removeAttachmentAndDelete(config.context, index)
            },
            isReadOnly = false,
            isConnected = config.isConnected)

        Button(
            onClick = { config.createTaskViewModel.addTask(config.context) },
            enabled = config.inputValid && !config.createTaskState.isSaving,
            modifier = Modifier.fillMaxWidth().testTag(CommonTaskTestTags.SAVE_TASK),
            colors = EurekaStyles.primaryButtonColors()) {
              Text(if (config.createTaskState.isSaving) "Saving..." else "Save")
            }
      }
}
