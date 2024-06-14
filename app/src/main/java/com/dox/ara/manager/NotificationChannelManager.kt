package com.dox.ara.manager

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dox.ara.R
import com.dox.ara.activity.MainActivity
import com.dox.ara.ui.data.NavItem
import com.dox.ara.ui.data.RouteItem
import com.dox.ara.utility.Constants
import com.dox.ara.utility.Constants.ALERT_NOTIFICATION_CHANNEL_ID
import com.dox.ara.utility.Constants.ALERT_NOTIFICATION_CHANNEL_NAME
import com.dox.ara.utility.Constants.MAIN_NOTIFICATION_CHANNEL_ID
import com.dox.ara.utility.Constants.MAIN_NOTIFICATION_CHANNEL_NAME
import com.dox.ara.utility.Constants.MUSIC_NOTIFICATION_CHANNEL_ID
import com.dox.ara.utility.Constants.MUSIC_NOTIFICATION_CHANNEL_NAME
import com.dox.ara.utility.Constants.SILENT_NOTIFICATION_CHANNEL_ID
import com.dox.ara.utility.Constants.SILENT_NOTIFICATION_CHANNEL_NAME
import com.dox.ara.utility.Constants.SPEECH_NOTIFICATION_CHANNEL_ID
import com.dox.ara.utility.Constants.SPEECH_NOTIFICATION_CHANNEL_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject


class NotificationChannelManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) {

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val silentChannel = NotificationChannel(
            SILENT_NOTIFICATION_CHANNEL_ID,
            SILENT_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_MIN
        )
        val mainChannel = NotificationChannel(
            MAIN_NOTIFICATION_CHANNEL_ID,
            MAIN_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        val alertChannel = NotificationChannel(
            ALERT_NOTIFICATION_CHANNEL_ID,
            ALERT_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )

        val musicChannel = NotificationChannel(
            MUSIC_NOTIFICATION_CHANNEL_ID,
            MUSIC_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_MIN
        )

        val speechChannel = NotificationChannel(
            SPEECH_NOTIFICATION_CHANNEL_ID,
            SPEECH_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_MIN
        )

        silentChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        alertChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        mainChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        musicChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        speechChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        val manager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(silentChannel)
        manager.createNotificationChannel(mainChannel)
        manager.createNotificationChannel(alertChannel)
        manager.createNotificationChannel(musicChannel)
        manager.createNotificationChannel(speechChannel)
    }


    @SuppressLint("MissingPermission")
    fun showAssistantResponseNotification(chatId: Long, title: String, text: String) {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(Constants.START_ROUTE_EXTRA, RouteItem.Home.route)
        intent.putExtra(Constants.START_PAGE_EXTRA, NavItem.Chats.page)
        intent.putExtra(Constants.NAVIGATE_TO_EXTRA, "${RouteItem.Chat.route}/$chatId")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val pendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(chatId.toInt(), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val builder = NotificationCompat.Builder(context, MAIN_NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(permissionManager.checkPostNotifications()) {
                with(NotificationManagerCompat.from(context)) {
                    notify(chatId.toInt(), builder.build())
                }
            } else {
                Timber.w("[${::showAssistantResponseNotification.name}] No notification permission")
            }
        } else {
            with(NotificationManagerCompat.from(context)) {
                notify(chatId.toInt(), builder.build())
            }
        }
    }
}