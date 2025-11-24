package ch.eureka.eurekapp.services.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.text.toUpperCase
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ch.eureka.eurekapp.MainActivity
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.notifications.NotificationType
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class LocalFirestoreMessagingService: FirebaseMessagingService(){
    private val usersRepository = FirestoreRepositoriesProvider.userRepository
    private val meetingsRepository = FirestoreRepositoriesProvider.meetingRepository
    private val chatRepository = FirestoreRepositoriesProvider.chatRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        //Extract data from FCM Message
        val type = message.data["type"] ?: "general"
        val title = message.data["title"] ?: "notification"
        val body = message.data["body"] ?: ""
        val payloadId = message.data["id"]

        showNotification(title, body, type, payloadId)
    }

    private fun showNotification(title: String, body: String, type: String, id: String?){
        val channelId = when(type){
            "meeting" -> NotificationType.MEETING_NOTIFICATION.displayString
            "message" -> NotificationType.MESSAGE_NOTIFICATION.displayString
            else -> NotificationType.GENERAL_NOTIFICATION.displayString
        }

        createNotificationChannel(channelId)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
            putExtra("notification_id", id)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.outline_circle_notifications_24)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = channelId.uppercase()
            val description = "$channelId notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                this.description = description
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}