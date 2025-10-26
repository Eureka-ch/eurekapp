package ch.eureka.eurekapp.screens.subscreens.meetings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.BorderGrayColor
import ch.eureka.eurekapp.ui.theme.DarkColorScheme
import ch.eureka.eurekapp.ui.theme.Typography

@Composable
fun MeetingAudioRecordingScreen(
    context: Context = LocalContext.current,
    projectId: String,
    meetingId: String,

    ){

    var microphonePermissionIsGranted by remember{mutableStateOf(
        ContextCompat.checkSelfPermission(context,
        Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_DENIED)}

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        microphonePermissionIsGranted = isGranted
    }


    LaunchedEffect(Unit) {
        if(!microphonePermissionIsGranted){
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text("Audio Recording", style = Typography.titleMedium,
                color = DarkColorScheme.background)
        }

        Surface(
            modifier =
                Modifier
                    .border(
                    border = BorderStroke(width = 1.dp, color = BorderGrayColor),
                    shape = RoundedCornerShape(16.dp))
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f),
            shadowElevation = 3.dp,
            color = Color.White,
            shape = RoundedCornerShape(16.dp)) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text("00:45", style = Typography.titleMedium,
                        color = DarkColorScheme.background)
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
                ){
                    IconButton(
                        shape = CircleShape,
                        onClick = {}
                    ) {
                        Icon(Icons.Outlined.Stop, null)
                    }
                    /**Start-Pause button**/
                    IconButton(
                        shape = CircleShape,
                        onClick = {}
                    ) {
                        Icon(Icons.Outlined.PlayArrow, null)
                    }
                }

            }

        }
    }
}