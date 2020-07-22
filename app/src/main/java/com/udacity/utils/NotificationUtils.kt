package com.udacity.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.udacity.DetailActivity
import com.udacity.MainActivity
import com.udacity.R

object NotificationUtils {

    private const val CHANNEL_ID = "channel_id"
    private const val CHANNEL_NAME = "channel_name"
    private const val REQUEST_CODE = 1001
    
    fun downloadsChannel(context: Context): ChannelUtil {
        return ChannelUtil(
            CHANNEL_ID,
            CHANNEL_NAME,
            context.getString(R.string.download_files),
            NotificationManager.IMPORTANCE_HIGH,
            NotificationCompat.PRIORITY_HIGH,
            NotificationCompat.VISIBILITY_PUBLIC
        )
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(context: Context, ChannelUtil: ChannelUtil) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationChannel(
            ChannelUtil.id,
            ChannelUtil.name,
            ChannelUtil.importance
        ).apply {
            enableLights(true)
            enableVibration(true)
            description
            setShowBadge(true)
            description = ChannelUtil.description
            lockscreenVisibility = ChannelUtil.visibility
            notificationManager.createNotificationChannel(this)
        }
    }
    
    fun downloadNotification(
        context: Context,
        downloadId: Int,
        downloadStatus: MainActivity.DownloadStatus,
        fileName: String
    ) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notifyIntent = Intent(context, DetailActivity::class.java)
        notifyIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        notifyIntent.putExtras(DetailActivity.withExtras(downloadId, downloadStatus, fileName))

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val downloadsChannel = downloadsChannel(context)

        val notification = NotificationCompat.Builder(context, downloadsChannel.id)
            .setContentTitle(fileName)
            .setContentText(
                if (downloadStatus == MainActivity.DownloadStatus.SUCCESS) {
                    context.getString(R.string.download_completed)
                } else {
                    context.getString(R.string.download_failed)
                }
            )
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setVisibility(downloadsChannel.visibility)
            .setPriority(downloadsChannel.priority)
            .addAction(
                NotificationCompat.Action(
                    0,
                    context.getString(R.string.download_details),
                    pendingIntent
                )
            )
            .build()

        manager.notify(downloadId, notification)
    }

    fun clearNotification(context: Context, notificationId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}
