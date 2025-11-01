package com.example.lab_week_08

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SecondNotificationService : Service() {

    private lateinit var builder: NotificationCompat.Builder
    private lateinit var handler: Handler

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        builder = startForegroundService()
        val t = HandlerThread("SecondNotifThread").apply { start() }
        handler = Handler(t.looper)
    }

    private fun startForegroundService(): NotificationCompat.Builder {
        val pending = getPendingIntent()
        val channelId = createChannel()
        val b = getBuilder(pending, channelId)
        startForeground(NOTIF_ID, b.build())
        return b
    }

    private fun getPendingIntent(): PendingIntent {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), flag)
    }

    private fun createChannel(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val id = "002"
            val name = "002 Channel"
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            val mgr = requireNotNull(
                ContextCompat.getSystemService(this, NotificationManager::class.java)
            )
            mgr.createNotificationChannel(channel)
            id
        } else ""

    private fun getBuilder(pending: PendingIntent, channelId: String) =
        NotificationCompat.Builder(this, channelId)
            .setContentTitle("Third worker process is done")
            .setContentText("Wrapping up…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pending)
            .setTicker("Third worker process is done, wrapping up…")
            .setOngoing(true)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val rv = super.onStartCommand(intent, flags, startId)
        val id = intent?.getStringExtra(EXTRA_ID)
            ?: throw IllegalStateException("Channel ID must be provided")

        handler.post {
            // boleh ganti durasi countdown supaya tidak tabrakan toast
            for (i in 5 downTo 0) {
                Thread.sleep(1000L)
                builder.setContentText("$i seconds until final message").setSilent(true)
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                    .notify(NOTIF_ID, builder.build())
            }
            Handler(Looper.getMainLooper()).post { mutableID.value = id }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return rv
    }

    companion object {
        const val NOTIF_ID = 0xCA8
        const val EXTRA_ID = "Id"

        private val mutableID = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = mutableID
    }
}
