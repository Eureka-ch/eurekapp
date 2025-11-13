package ch.eureka.eurekapp.screens.subscreens.tasks.creation

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
import ch.eureka.eurekapp.model.tasks.CreateTaskViewModel
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

const val CREATE_SCREEN_PHOTO_BUTTON_SIZE = 0.3f

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
    val photoUri by savedStateHandle?.getStateFlow("photoUri", "")?.collectAsState()
        ?: remember { mutableStateOf("") }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var isNavigatingToCamera by remember { mutableStateOf(false) }

    HandleErrorToast(errorMsg, createTaskViewModel, context)
    HandlePhotoUri(photoUri, createTaskViewModel)
    HandleProjectId(projectId, createTaskViewModel)
    HandleTaskSaved(createTaskState.taskSaved, navigationController, createTaskViewModel)

    HandlePhotoCleanupDisposableEffect(isNavigatingToCamera,
        createTaskState.attachmentUris,
        createTaskViewModel, context)

    Scaffold(content = { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TaskTitleField(
                value = createTaskState.title,
                onValueChange = { createTaskViewModel.setTitle(it) },
                hasTouched = hasTouchedTitle,
                onFocusChanged = { hasTouchedTitle = true })

            TaskDescriptionField(
                value = createTaskState.description,
                onValueChange = { createTaskViewModel.setDescription(it) },
                hasTouched = hasTouchedDescription,
                onFocusChanged = { hasTouchedDescription = true })

            TaskDueDateField(
                value = createTaskState.dueDate,
                onValueChange = { createTaskViewModel.setDueDate(it) },
                hasTouched = hasTouchedDate,
                onFocusChanged = { hasTouchedDate = true },
                dateRegex = createTaskViewModel.dateRegex)

            TaskReminderField(
                value = createTaskState.reminderTime,
                onValueChange = { createTaskViewModel.setReminderTime(it) })

            ProjectSelectionField(
                projects = availableProjects,
                selectedProjectId = projectId,
                onProjectSelected = { projectId -> createTaskViewModel.setProjectId(projectId) })

            UserAssignmentField(
                availableUsers = createTaskState.availableUsers,
                selectedUserIds = createTaskState.selectedAssignedUserIds,
                onUserToggled = { userId -> createTaskViewModel.toggleUserAssignment(userId) },
                enabled = projectId.isNotEmpty()
            )

            if (projectId.isNotEmpty()) {
                TaskDependenciesSelectionField(
                    availableTasks = availableTasks,
                    selectedDependencyIds = createTaskState.dependingOnTasks,
                    onDependencyAdded = { taskId -> createTaskViewModel.addDependency(taskId) },
                    onDependencyRemoved = { taskId -> createTaskViewModel.removeDependency(taskId) },
                    currentTaskId = "",
                    cycleError = cycleError
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                OutlinedButton(
                    onClick = {
                        isNavigatingToCamera = true
                        navigationController.navigate(Route.Camera)
                    },
                    colors = EurekaStyles.outlinedButtonColors(),
                    modifier = Modifier
                        .fillMaxWidth(CREATE_SCREEN_PHOTO_BUTTON_SIZE)
                        .testTag(CommonTaskTestTags.ADD_PHOTO)
                ) {
                    Text("Add Photo")
                }

                Button(
                    onClick = { createTaskViewModel.addTask(context) },
                    enabled = inputValid && !createTaskState.isSaving,
                    modifier = Modifier.fillMaxWidth().testTag(CommonTaskTestTags.SAVE_TASK),
                    colors = EurekaStyles.primaryButtonColors()
                ) {
                    Text(if (createTaskState.isSaving) "Saving..." else "Save")
                }
            }

            AttachmentsList(
                attachments = createTaskState.attachmentUris,
                onDelete = { index ->
                    val uri = createTaskState.attachmentUris[index]
                    if (createTaskViewModel.deletePhoto(context, uri)) {
                        createTaskViewModel.removeAttachment(index)
                    }
                })
        }
    })
}

@Composable
private fun HandleErrorToast(errorMsg: String?, createTaskViewModel: CreateTaskViewModel,
                             context: android.content.Context) {
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
            createTaskViewModel.addAttachment(photoUri.toUri())
        }
    }
}

@Composable
private fun HandleProjectId(projectId: String, createTaskViewModel: CreateTaskViewModel) {
    LaunchedEffect(projectId) {
        if (projectId.isNotEmpty()) {
            createTaskViewModel.loadAvailableTasks(projectId)
            createTaskViewModel.loadProjectMembers(projectId)
        }
    }
}

@Composable
private fun HandleTaskSaved(taskSaved: Boolean, navigationController: NavHostController,
                            createTaskViewModel: CreateTaskViewModel) {
    LaunchedEffect(taskSaved) {
        if (taskSaved) {
            navigationController.popBackStack()
            createTaskViewModel.resetSaveState()
        }
    }
}

@Composable
private fun HandlePhotoCleanupDisposableEffect(
    isNavigatingToCamera: Boolean,
    attachmentUris: List<Uri>,
    createTaskViewModel: CreateTaskViewModel,
    context: android.content.Context
) {
    DisposableEffect(Unit) {
        onDispose {
            if (!isNavigatingToCamera) {
                attachmentUris.forEach { uri ->
                    createTaskViewModel.deletePhoto(context, uri)
                }
            }
        }
    }
}
