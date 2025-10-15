package ch.eureka.eurekapp.screens.subscreens.project_selection_subscreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.model.data.project.CreateProjectViewModel
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.navigation.MainScreens
import ch.eureka.eurekapp.navigation.navigationFunction
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.BlackTextColor
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.BorderGrayColor
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.GrayTextColor2
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.LightingBlue
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.WhiteTextColor
import ch.eureka.eurekapp.ui.theme.LightColorScheme
import ch.eureka.eurekapp.ui.theme.Typography
import ch.eureka.eurekapp.utils.Utils
import ch.eureka.eurekapp.utils.Utils.convertMillisToDate
import kotlinx.coroutines.launch

object CreateProjectScreenTestTags {
  const val PROJECT_NAME_TEST_TAG_TEXT_INPUT = "Project name text input"
  const val DESCRIPTION_NAME_TEST_TAG_TEXT_INPUT = "Description text input"
  const val START_DATE_TEST_TAG = "start date text input test tag"
  const val END_DATE_TEST_TAG = "end date test tag"
  const val PROJECT_STATUS_DROPDOWN_TEST_TAG = "project status dropdown test tag"
  const val CHECKBOX_ENABLE_GOOGLE_DRIVE_FOLDER_TEST_TAG = "checkbox enable google drive folder"
  const val CHECKBOX_LINK_GITHUB_REPOSITORY = "checkbox link github repository"
  const val GITHUB_URL_TEST_TAG = "text input github url"

  const val CALENDAR_ICON_BUTTON_START = "calendar icon start"
  const val CALENDAR_ICON_BUTTON_END = "calendar icon end"

  fun createProjectStatusTestTag(status: ProjectStatus): String {
    return "status: ${status.name}"
  }
}

/**
 * The screen to create projects
 *
 * @param createProjectViewModel the view model that communicates with the repositories
 * @param navigationController the navigation controller of the app *
 */
@Composable
fun CreateProjectScreen(
    navigationController: NavController = rememberNavController(),
    createProjectViewModel: CreateProjectViewModel = viewModel()
) {
  val projectName = remember { mutableStateOf<String>("") }
  val projectNameError = remember { mutableStateOf<Boolean>(false) }

  val projectDescription = remember { mutableStateOf<String>("") }
  val projectDescriptionError = remember { mutableStateOf<Boolean>(false) }

  val startDate = remember { mutableStateOf<String>("") }
  val startDateError = remember { mutableStateOf<Boolean>(false) }

  val endDate = remember { mutableStateOf<String>("") }
  val endDateError = remember { mutableStateOf<Boolean>(false) }

  val projectStatus = remember { mutableStateOf<ProjectStatus>(ProjectStatus.OPEN) }

  val enableGoogleDriveFolderChecked = remember { mutableStateOf<Boolean>(false) }
  val linkGithubRepository = remember { mutableStateOf<Boolean>(false) }

  val githubUrl = remember { mutableStateOf<String>("") }

  val scrollState = rememberScrollState()

  var failedToCreateProjectText by remember { mutableStateOf<String>("") }

  Column(
      modifier = Modifier.fillMaxSize().background(color = LightColorScheme.background),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceEvenly) {
        Column(modifier = Modifier.padding(vertical = 2.dp, horizontal = 10.dp)) {
          Text(
              text = "Create New Project",
              style = Typography.titleLarge,
              fontWeight = FontWeight(600))
          Text(
              text =
                  "Define the core details. You can link Google Workspace and Github now or later.",
              style = Typography.titleMedium,
              color = GrayTextColor2)
        }
        // project creation
        Surface(
            modifier =
                Modifier.border(
                        border = BorderStroke(width = 1.dp, color = BorderGrayColor),
                        shape = RoundedCornerShape(16.dp))
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f),
            shadowElevation = 3.dp,
            color = Color.White,
            shape = RoundedCornerShape(16.dp)) {
              Column(
                  modifier =
                      Modifier.padding(vertical = 5.dp).fillMaxWidth().verticalScroll(scrollState),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Top) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                          CreateProjectTextField(
                              title = "Project name",
                              placeHolderText = "Ex: SwEnt - Sprint Manager",
                              textValue = projectName,
                              inputIsError = { input -> Utils.stringIsEmptyOrBlank(input) },
                              errorText = "Project name cannot be empty!",
                              isErrorState = projectNameError,
                              testTag =
                                  CreateProjectScreenTestTags.PROJECT_NAME_TEST_TAG_TEXT_INPUT)
                        }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                          CreateProjectTextField(
                              title = "Description",
                              placeHolderText = "Short context and objectives...",
                              textValue = projectDescription,
                              inputIsError = { input -> Utils.stringIsEmptyOrBlank(input) },
                              errorText = "Description cannot be empty!",
                              minLine = 4,
                              isErrorState = projectDescriptionError,
                              testTag =
                                  CreateProjectScreenTestTags.DESCRIPTION_NAME_TEST_TAG_TEXT_INPUT)
                        }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                          Row(
                              modifier = Modifier.weight(1f),
                              horizontalArrangement = Arrangement.Center,
                              verticalAlignment = Alignment.CenterVertically) {
                                CreateProjectTextField(
                                    title = "Start",
                                    placeHolderText = "dd/MM/yyyy",
                                    isDatePicker = true,
                                    textValue = startDate,
                                    inputIsError = { input ->
                                      !Utils.isDateParseableToStandardAppPattern(input)
                                    },
                                    errorText = "Date should be of the format dd/MM/yyyy",
                                    isErrorState = startDateError,
                                    testTag = CreateProjectScreenTestTags.START_DATE_TEST_TAG,
                                    datePickerButtonTag =
                                        CreateProjectScreenTestTags.CALENDAR_ICON_BUTTON_START)
                              }
                          Row(
                              modifier = Modifier.weight(1f),
                              horizontalArrangement = Arrangement.Center,
                              verticalAlignment = Alignment.CenterVertically) {
                                CreateProjectTextField(
                                    title = "End (optional)",
                                    placeHolderText = "dd/MM/yyyy",
                                    isDatePicker = true,
                                    textValue = endDate,
                                    inputIsError = { input ->
                                      !Utils.isDateParseableToStandardAppPattern(input)
                                    },
                                    errorText = "Date should be of the format dd/MM/yyyy",
                                    isErrorState = endDateError,
                                    testTag = CreateProjectScreenTestTags.END_DATE_TEST_TAG,
                                    datePickerButtonTag =
                                        CreateProjectScreenTestTags.CALENDAR_ICON_BUTTON_END)
                              }
                        }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                          ProjectStateSelectionMenu(projectStatus = projectStatus)
                        }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 15.dp),
                        thickness = 1.dp,
                        color = BorderGrayColor)

                    Column(
                        modifier =
                            Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 5.dp),
                        horizontalAlignment = Alignment.Start) {
                          Text(
                              "Integrations",
                              style = Typography.titleMedium,
                              fontWeight = FontWeight(600))
                          CheckboxOptionComponent(
                              "Enable Google Drive Folder",
                              enableGoogleDriveFolderChecked,
                              CreateProjectScreenTestTags
                                  .CHECKBOX_ENABLE_GOOGLE_DRIVE_FOLDER_TEST_TAG)
                          CheckboxOptionComponent(
                              "Link Github Repository",
                              linkGithubRepository,
                              CreateProjectScreenTestTags.CHECKBOX_LINK_GITHUB_REPOSITORY)

                          if (linkGithubRepository.value) {
                            Row(modifier = Modifier) {
                              CreateProjectTextField(
                                  title = "Github URL (optional)",
                                  placeHolderText = "https://github.com/org/repo",
                                  textValue = githubUrl,
                                  inputIsError = { input -> false },
                                  errorText = "",
                                  testTag = CreateProjectScreenTestTags.GITHUB_URL_TEST_TAG)
                            }
                          }
                        }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier =
                            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically) {
                          FilledTonalButton(
                              modifier = Modifier.width(140.dp).height(40.dp),
                              onClick = {
                                /** The method that handles the creation of a project */
                                projectNameError.value =
                                    Utils.stringIsEmptyOrBlank(projectName.value)
                                projectDescriptionError.value =
                                    Utils.stringIsEmptyOrBlank(projectDescription.value)
                                startDateError.value =
                                    !Utils.isDateParseableToStandardAppPattern(startDate.value)
                                endDateError.value =
                                    !Utils.isDateParseableToStandardAppPattern(endDate.value)

                                createProjectViewModel.viewModelScope.launch {
                                  val newId = createProjectViewModel.getNewProjectId()
                                  val currentUserId = createProjectViewModel.getCurrentUser()
                                  if (!projectNameError.value &&
                                      !projectDescriptionError.value &&
                                      !startDateError.value &&
                                      !endDateError.value) {
                                    if (newId != null && currentUserId != null) {
                                      val projectToAdd =
                                          Project(
                                              projectId = newId,
                                              createdBy = currentUserId,
                                              memberIds = listOf(currentUserId),
                                              name = projectName.value,
                                              description = projectDescription.value,
                                              status = projectStatus.value)
                                      createProjectViewModel.createProject(
                                          projectToCreate = projectToAdd,
                                          onSuccessCallback = {
                                            navigationFunction(
                                                navigationController,
                                                goBack = false,
                                                destination = MainScreens.ProjectSelectionScreen)
                                          },
                                          onFailureCallback = {
                                            failedToCreateProjectText =
                                                "Failed to create the project..."
                                          })
                                    }
                                  }
                                }
                              },
                              colors =
                                  ButtonDefaults.filledTonalButtonColors(
                                      containerColor = LightingBlue)) {
                                Text(
                                    text = "Create Project",
                                    color = WhiteTextColor,
                                    fontWeight = FontWeight(500),
                                    style = Typography.titleSmall)
                              }

                          Text(failedToCreateProjectText, color = LightColorScheme.error)
                        }
                  }
            }
      }
}

private val textTypography = Typography.bodyMedium
private val titleTypography = Typography.titleSmall
private val verticalContainerPadding = 5.dp
private val horizontalContainerPadding = 15.dp
private val textFieldShape = RoundedCornerShape(12.dp)
private val defaultPopupProperties =
    PopupProperties(
        focusable = true,
        dismissOnClickOutside = true,
        dismissOnBackPress = true,
        usePlatformDefaultWidth = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectTextField(
    title: String,
    textValue: MutableState<String>,
    placeHolderText: String,
    isErrorState: MutableState<Boolean> = remember { mutableStateOf<Boolean>(false) },
    inputIsError: (String) -> Boolean,
    errorText: String,
    minLine: Int = 1,
    testTag: String,
    datePickerButtonTag: String = "",
    isDatePicker: Boolean = false
) {
  var showDatePicker by remember { mutableStateOf(false) }
  val datePickerState = rememberDatePickerState()

  val selectedDate =
      datePickerState.selectedDateMillis?.let {
        val converted = convertMillisToDate(it)
        textValue.value = converted
        isErrorState.value = inputIsError(textValue.value)
        converted
      } ?: ""

  Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal = horizontalContainerPadding,
                        vertical = verticalContainerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
              Text(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
                  text = title,
                  style = titleTypography,
                  color = GrayTextColor2,
                  textAlign = TextAlign.Start)
              Spacer(modifier = Modifier.padding(2.dp))
              OutlinedTextField(
                  minLines = minLine,
                  singleLine = isDatePicker,
                  readOnly = isDatePicker,
                  modifier =
                      Modifier.padding(horizontal = 5.dp, vertical = 0.dp)
                          .background(color = LightColorScheme.surface)
                          .fillMaxWidth()
                          .testTag(testTag),
                  shape = textFieldShape,
                  value = if (!isDatePicker) textValue.value else selectedDate,
                  onValueChange = {
                    if (!isDatePicker) {
                      textValue.value = it
                      isErrorState.value = inputIsError(it)
                    }
                  },
                  textStyle = textTypography,
                  placeholder = {
                    Text(placeHolderText, style = textTypography, color = GrayTextColor2)
                  },
                  colors =
                      TextFieldDefaults.outlinedTextFieldColors(
                          focusedBorderColor = GrayTextColor2,
                          unfocusedBorderColor = BorderGrayColor),
                  trailingIcon = {
                    if (isDatePicker) {
                      IconButton(
                          modifier = Modifier.testTag(datePickerButtonTag),
                          onClick = { showDatePicker = !showDatePicker }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select date")
                          }
                    }
                  },
                  isError = isErrorState.value)
            }
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
              Spacer(modifier = Modifier.padding(2.dp))
              if (isErrorState.value) {
                Text(errorText, style = TextStyle(fontSize = 10.sp), color = LightColorScheme.error)
              }
            }
      }

  if (showDatePicker) {
    Popup(
        onDismissRequest = { showDatePicker = false },
        alignment = Alignment.Center,
        properties = defaultPopupProperties) {
          Box(
              contentAlignment = Alignment.Center,
              modifier =
                  Modifier.fillMaxWidth(0.85f)
                      .shadow(elevation = 4.dp)
                      .background(LightColorScheme.surface)) {
                DatePicker(
                    colors =
                        DatePickerDefaults.colors(
                            selectedDayContainerColor = LightingBlue,
                            todayDateBorderColor = LightingBlue),
                    state = datePickerState,
                    showModeToggle = false)
              }
        }
  }
}

private val dropDownMenuButtonShape = RoundedCornerShape(12.dp)
private val dropDownMenuButtonBorderStroke = BorderStroke(width = 1.dp, color = BorderGrayColor)

@Composable
fun ProjectStateSelectionMenu(projectStatus: MutableState<ProjectStatus>) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(
                  horizontal = horizontalContainerPadding, vertical = verticalContainerPadding)) {
        var expanded by remember { mutableStateOf(false) }
        Text(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
            text = "Project Status",
            style = titleTypography,
            color = GrayTextColor2,
            textAlign = TextAlign.Start)
        OutlinedButton(
            modifier =
                Modifier.background(color = LightColorScheme.surface)
                    .testTag(CreateProjectScreenTestTags.PROJECT_STATUS_DROPDOWN_TEST_TAG),
            onClick = { expanded = true },
            shape = dropDownMenuButtonShape,
            border = dropDownMenuButtonBorderStroke) {
              Text(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
                  text = projectStatus.value.name,
                  style = titleTypography,
                  color = GrayTextColor2,
                  textAlign = TextAlign.Start)
            }
        DropdownMenu(
            modifier =
                Modifier.background(
                    color = LightColorScheme.surface, shape = dropDownMenuButtonShape),
            expanded = expanded,
            onDismissRequest = { expanded = false },
            properties = defaultPopupProperties) {
              ProjectStatus.values().forEachIndexed { index, status ->
                DropdownMenuItem(
                    modifier =
                        Modifier.testTag(
                            CreateProjectScreenTestTags.createProjectStatusTestTag(status)),
                    text = { Text(text = status.name, style = Typography.bodyMedium) },
                    onClick = {
                      projectStatus.value = status
                      expanded = false
                    })
              }
            }
      }
}

private val checkBoxVerticalPadding = 5.dp
private val checkBoxHorizontalpadding = 6.dp
private val checkBoxShape = RoundedCornerShape(3.dp)

@Composable
fun CheckboxOptionComponent(title: String, value: MutableState<Boolean>, testTag: String) {
  Row(
      modifier =
          Modifier.padding(
              vertical = checkBoxVerticalPadding, horizontal = checkBoxHorizontalpadding),
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            modifier = Modifier.size(5.dp).padding(0.dp).testTag(testTag),
            checked = value.value,
            onCheckedChange = { value.value = !value.value },
            colors = CheckboxDefaults.colors(checkedColor = LightingBlue))
        Spacer(modifier = Modifier.padding(horizontal = 10.dp))
        Text(text = title, style = Typography.bodySmall, color = BlackTextColor)
      }
}
