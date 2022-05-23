package com.example.mute

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log


class MuteService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int) : Int {
        Log.w("mute", "service.onStartCommand")

        if (intent.getStringExtra(ACTION) == CANCEL) {
            Log.w("mute", "will stop service")
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        Log.w("mute", "will start service")
        val notificationChannel = NotificationChannel(CHANNEL_ID, "mute", NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(notificationChannel)
        val notification: Notification = Notification.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setContentTitle(getText(R.string.notification_title))
            .setSmallIcon(R.drawable.ic_tile)
            .build()

        startForeground(1, notification)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.w("mute", "service.onDestroy")
        super.onDestroy()
    }

    companion object {
        val CHANNEL_ID = "com.example.mute.n"
        val ACTION = "action"
        val CANCEL = "cancel"
    }
}