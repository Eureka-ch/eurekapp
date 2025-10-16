package ch.eureka.eurekapp.screens.subscreens.overview_project_subscreens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.BorderGrayColor
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.GrayTextColor2
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.LightingBlue
import ch.eureka.eurekapp.ui.theme.LightColorScheme
import ch.eureka.eurekapp.ui.theme.Typography
import ch.eureka.eurekapp.utils.Utils.convertMillisToDate

@Composable
fun CreateInvitationSubscreens(onInvitationCreate: () -> Unit) {
  val scrollState = rememberScrollState()
  Column(
      modifier = Modifier.fillMaxSize().background(color = LightColorScheme.background),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceEvenly) {
        Column(modifier = Modifier.padding(vertical = 2.dp, horizontal = 10.dp)) {
          Text(
              text = "Create Invitation Token",
              style = Typography.titleLarge,
              fontWeight = FontWeight(600))
          Text(
              text = "Invite collaborators with a role, permissions and expiration rules.",
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
                    Row() {}
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
