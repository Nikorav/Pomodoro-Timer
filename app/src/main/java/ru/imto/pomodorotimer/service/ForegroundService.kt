package ru.imto.pomodorotimer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import ru.imto.pomodorotimer.*
import ru.imto.pomodorotimer.recyclerView.PomodoroTimer
import ru.imto.pomodorotimer.constants.*

class ForegroundService : Service() {

    private var isServiceStarted = false
    private var notificationManager: NotificationManager? = null
    private var job: Job? = null
    private var pomodoroTimer: PomodoroTimer? = null
    private var mediaPlayer: MediaPlayer? = null


    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro timer")
            .setGroup("Timer")
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun processCommand(intent: Intent?) {
        when (intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
            COMMAND_START -> {
                pomodoroTimer =
                    intent?.extras?.getSerializable(STARTED_TIMER_TIME_MS) as PomodoroTimer
                commandStart(pomodoroTimer!!)
            }
            COMMAND_STOP -> commandStop()
            INVALID -> return
        }
    }

    private fun commandStart(pomodoroTimer: PomodoroTimer) {
        if (isServiceStarted) return
        try {
            if (pomodoroTimer.currentMs.toInt() != 0) {
                moveToStartedState()
                startForegroundAndShowNotification()
                continueTimer(pomodoroTimer)
            }
        } finally {
            isServiceStarted = true
        }
    }

    private fun continueTimer(pomodoroTimer: PomodoroTimer) {
        job = CoroutineScope(Dispatchers.Main).launch {
            while (pomodoroTimer.currentMs >= 0) {
                if (pomodoroTimer.currentMs.toInt() <= 0) stopForeground(true)
                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification((pomodoroTimer.currentMs).displayTime())
                )
                pomodoroTimer.currentMs -= INTERVAL
                delay(INTERVAL)
                if (pomodoroTimer.currentMs.toInt() <= 0) {
                    commandStop()
                    mediaPlayer = MediaPlayer.create(applicationContext,R.raw.pip)
                    mediaPlayer?.start()
                }
            }
        }
    }

    private fun commandStop() {
        if (!isServiceStarted) {
            return
        }
        try {
            job?.cancel()
            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }

    }

    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, ForegroundService::class.java))
        }
        else startService(Intent(this, ForegroundService::class.java))
    }

    private fun startForegroundAndShowNotification() {
        createChannel()
        val notification = getNotification("content")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getNotification(content: String) = builder.setContentText(content).build()

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "pomodoroTimer"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, channelName, importance
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT)
    }

    override fun onDestroy() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
        super.onDestroy()
    }

    private companion object {
        private const val CHANNEL_ID = "Channel_ID"
        private const val NOTIFICATION_ID = 666
        private const val INTERVAL = 1000L
    }
}
