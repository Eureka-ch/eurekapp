package ch.eureka.eurekapp.services.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ch.eureka.eurekapp.MainActivity
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.notifications.NotificationType
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

object LocalFirestoreMessagingServiceCompanion {
  const val intentNotificationTypeString = "notification_type"
  const val intentNotificationIdString = "notification_id"
  const val intentNotificationProjectId = "notification_project_id"
}

class LocalFirestoreMessagingService : FirebaseMessagingService() {

  override fun onNewToken(token: String) {
    super.onNewToken(token)
  }

  override fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)

    // Extract data from FCM Message
    val type = message.data["type"] ?: "general"
    val title = message.data["title"] ?: "notification"
    val body = message.data["body"] ?: ""
    val payloadId = message.data["id"]
    val projectId = message.data["projectId"]

    showNotification(title, body, type, payloadId, projectId)
  }

  private fun showNotification(
      title: String,
      body: String,
      type: String,
      id: String?,
      projectId: String?
  ) {
    val channelId =
        when (type) {
          NotificationType.MEETING_NOTIFICATION.backendTypeString ->
              NotificationType.MEETING_NOTIFICATION.displayString
          NotificationType.MESSAGE_NOTIFICATION.backendTypeString ->
              NotificationType.MESSAGE_NOTIFICATION.displayString
          else -> NotificationType.GENERAL_NOTIFICATION.displayString
        }

    createNotificationChannel(channelId)

    val intent =
        Intent(this, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
          putExtra(LocalFirestoreMessagingServiceCompanion.intentNotificationTypeString, type)
          putExtra(LocalFirestoreMessagingServiceCompanion.intentNotificationIdString, id)
          putExtra(LocalFirestoreMessagingServiceCompanion.intentNotificationProjectId, projectId)
        }

    val pendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE
                else 0)

    val builder =
        NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.outline_circle_notifications_24)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED) {
      NotificationManagerCompat.from(this)
          .notify(System.currentTimeMillis().toInt(), builder.build())
    }
  }

  private fun createNotificationChannel(channelId: String) {
    val name = channelId.uppercase()
    val description = "$channelId notifications"
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel =
        NotificationChannel(channelId, name, importance).apply { this.description = description }
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)
  }
}
