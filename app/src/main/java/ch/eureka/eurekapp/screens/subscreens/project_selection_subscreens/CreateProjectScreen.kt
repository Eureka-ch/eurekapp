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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ch.eureka.eurekapp.model.project.CreateProjectViewModel
import ch.eureka.eurekapp.model.project.ProjectStatus
import ch.eureka.eurekapp.ui.theme.BackgroundGray
import ch.eureka.eurekapp.ui.theme.Black
import ch.eureka.eurekapp.ui.theme.BorderGrayColor
import ch.eureka.eurekapp.ui.theme.DarkerGray
import ch.eureka.eurekapp.ui.theme.LightingBlue
import ch.eureka.eurekapp.ui.theme.NormalTextGray
import ch.eureka.eurekapp.ui.theme.Typography
import ch.eureka.eurekapp.ui.theme.White
import ch.eureka.eurekapp.utils.Utils.convertMillisToDate

/**
 * The screen to create projects
 * @param createProjectViewModel the view model that communicates with the repositories
 * @param navigationController the navigation controller of the app
 * **/
@Composable
fun CreateProjectScreen(
    navigationController: NavController = rememberNavController(),
    createProjectViewModel: CreateProjectViewModel = viewModel()
) {
    val projectName = remember { mutableStateOf<String>("") }
    val projectDescription = remember { mutableStateOf<String>("") }
    val startDate = remember { mutableStateOf<String>("") }
    val endDate = remember { mutableStateOf<String>("") }
    val projectStatus = remember { mutableStateOf<ProjectStatus>(ProjectStatus.OPEN) }

    val enableGoogleDriveFolderChecked = remember { mutableStateOf<Boolean>(false) }
    val linkGithubRepository = remember { mutableStateOf<Boolean>(false) }

    val githubUrl = remember { mutableStateOf<String>("") }

    Column(modifier = Modifier.fillMaxSize().background(color = BackgroundGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 10.dp)
        ){
            Text(text = "Create New Project", style = Typography.titleLarge,
                fontWeight = FontWeight(600))
            Text(text = "Define the core details. You can link Google Workspace and Github now or later.",
                style = Typography.titleMedium, color = NormalTextGray
            )
        }
        //project creation
        Surface(
            modifier = Modifier
                .border(border = BorderStroke(width = 1.dp, color = BorderGrayColor),
                    shape = RoundedCornerShape(16.dp))
                .fillMaxWidth(0.95f).fillMaxHeight(0.9f),
            shadowElevation = 3.dp,
            color= Color.White,
            shape = RoundedCornerShape(16.dp)

        ){
            Column(
                modifier = Modifier.padding(vertical = 5.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ){
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    CreateProjectTextField(
                        title = "Project name",
                        placeHolderText = "Ex: SwEnt - Sprint Manager",
                        textValue = projectName
                    )
                }
                Row(
                    modifier = Modifier.weight(1.5f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    CreateProjectTextField(
                        title = "Description",
                        placeHolderText = "Short context and objectives...",
                        textValue = projectDescription
                    )
                }
                Row(modifier = Modifier.weight(0.9f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically){
                    Row(modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically){
                        CreateProjectTextField(
                            title = "Start",
                            placeHolderText = "dd/MM/yyyy",
                            isDatePicker = true,
                            textValue = startDate
                        )
                    }
                    Spacer(modifier = Modifier.padding(1.dp))
                    Row(modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically){
                        CreateProjectTextField(
                            title = "End (optional)",
                            placeHolderText = "dd/MM/yyyy",
                            isDatePicker = true,
                            textValue = endDate
                        )
                    }
                }

                Row(modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically){
                    ProjectStateSelectionMenu(
                        projectStatus = projectStatus
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 15.dp),
                    thickness = 1.dp, color=BorderGrayColor)

                Column(modifier = Modifier.weight(1.5f).fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 5.dp),
                    horizontalAlignment = Alignment.Start){
                    Text("Integrations", style = Typography.titleMedium,
                        fontWeight = FontWeight(600))
                    CheckboxOptionComponent("Enable Google Drive Folder",
                        enableGoogleDriveFolderChecked)
                    CheckboxOptionComponent("Link Github Repository",
                        linkGithubRepository)

                    if(linkGithubRepository.value){
                        CreateProjectTextField(
                            title = "Github URL (optional)",
                            placeHolderText = "https://github.com/org/repo",
                            textValue = githubUrl
                        )
                    }
                }

                Row(modifier = Modifier.weight(1f)
                    .fillMaxWidth().padding(horizontal = 10.dp), horizontalArrangement =
                    Arrangement.Start, verticalAlignment = Alignment.CenterVertically){
                    FilledTonalButton(
                        modifier = Modifier.width(140.dp).height(40.dp),
                        onClick = {},
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = LightingBlue
                        )
                    ) {
                        Text(text = "Create Project", color = White,
                            fontWeight = FontWeight(500), style = Typography.titleSmall)
                    }
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
private val defaultPopupProperties = PopupProperties(
    focusable = true,
    dismissOnClickOutside = true,
    dismissOnBackPress = true,
    usePlatformDefaultWidth = false
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectTextField(title: String,
                           textValue: MutableState<String>,
                           placeHolderText: String, isDatePicker: Boolean = false){
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis?.let {
        val converted = convertMillisToDate(it)
        textValue.value = converted
        converted
    } ?: ""
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalContainerPadding,
            vertical = verticalContainerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        var text by remember { mutableStateOf("") }
        Text(modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
            text = title, style = titleTypography, color = NormalTextGray,
            textAlign = TextAlign.Start)
        Spacer(modifier = Modifier.padding(2.dp))
        OutlinedTextField(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 0.dp)
                .background(color = White).fillMaxWidth().fillMaxHeight(),
            shape = textFieldShape,
            value = if(!isDatePicker) textValue.value else selectedDate,
            onValueChange = {
                if(!isDatePicker){
                    textValue.value = it
                }
            },
            textStyle = textTypography,
            placeholder = {
                Text(placeHolderText, style = textTypography, color = NormalTextGray)
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = DarkerGray,
                unfocusedBorderColor = BorderGrayColor
            ),
            trailingIcon = {
                if(isDatePicker){
                    IconButton(onClick = { showDatePicker = !showDatePicker }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select date"
                        )
                    }
                }
            }

        )
    }

    if(showDatePicker){
        Popup(
            onDismissRequest = {showDatePicker = false},
            alignment = Alignment.Center,
            properties = defaultPopupProperties
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .shadow(elevation = 4.dp).background(White)
            ){
                DatePicker(
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = LightingBlue,
                        todayDateBorderColor = LightingBlue
                    ),
                    state = datePickerState,
                    showModeToggle = false
                )
            }
        }
    }
}

private val dropDownMenuButtonShape = RoundedCornerShape(12.dp)
private val dropDownMenuButtonBorderStroke = BorderStroke(width = 1.dp, color = BorderGrayColor)
@Composable
fun ProjectStateSelectionMenu(
    projectStatus: MutableState<ProjectStatus>
){
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalContainerPadding,
            vertical = verticalContainerPadding)
    ) {
        var expanded by remember { mutableStateOf(false) }
        Text(modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
            text = "Project Status", style = titleTypography, color = NormalTextGray,
            textAlign = TextAlign.Start)
        OutlinedButton(
            modifier = Modifier.background(color = White),
            onClick = {expanded = true},
            shape = dropDownMenuButtonShape,
            border = dropDownMenuButtonBorderStroke
        ) {
            Text(modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
                text = projectStatus.value.name, style = titleTypography, color = NormalTextGray,
                textAlign = TextAlign.Start)
        }
        DropdownMenu(
            modifier = Modifier.background(color = White, shape = dropDownMenuButtonShape),
            expanded = expanded,
            onDismissRequest = {expanded = false},
            properties = defaultPopupProperties
        ) {
            ProjectStatus.values().forEachIndexed { index, status ->
                DropdownMenuItem(
                    text = {
                        Text(text = status.name, style = Typography.bodyMedium)
                    },
                    onClick = {
                        projectStatus.value = status
                        expanded = false
                    }
                )
            }
        }
    }
}

private val checkBoxVerticalPadding = 5.dp
private val checkBoxHorizontalpadding = 6.dp
private val checkBoxShape = RoundedCornerShape(3.dp)
@Composable
fun CheckboxOptionComponent(title: String, value: MutableState<Boolean>){
    Row(modifier = Modifier.padding(vertical = checkBoxVerticalPadding, horizontal = checkBoxHorizontalpadding),
        horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically){
        Checkbox(modifier = Modifier.size(5.dp).padding(0.dp), checked =value.value,
            onCheckedChange = {value.value = !value.value},
            colors = CheckboxDefaults.colors(
                checkedColor = LightingBlue
            )
        )
        Spacer(modifier = Modifier.padding(horizontal = 10.dp))
        Text(text= title, style = Typography.bodySmall, color = Black)
    }
}