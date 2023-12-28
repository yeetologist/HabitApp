package com.dicoding.habitapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.data.HabitRepository
import com.dicoding.habitapp.ui.detail.DetailHabitActivity
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import com.dicoding.habitapp.utils.NOTIFICATION_CHANNEL_ID

class NotificationWorker(private val ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val habitId = inputData.getInt(HABIT_ID, 0)
    private val habitTitle = inputData.getString(HABIT_TITLE)

    private fun getPendingIntent(habit: Habit): PendingIntent? {
        val intent = Intent(applicationContext, DetailHabitActivity::class.java).apply {
            putExtra(HABIT_ID, habit.id)
        }
        return TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(intent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }
    }

    override fun doWork(): Result {
        val prefManager =
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val shouldNotify =
            prefManager.getBoolean(applicationContext.getString(R.string.pref_key_notify), false)

        val habitRepository = HabitRepository.getInstance(ctx)
        val habit = habitRepository.getHabitByUId(habitId)

        //TODO 12 : If notification preference on, show notification with pending intent

        return run {
            if (shouldNotify) {
                showNotification(habit)
            }
            Result.success()
        }
    }

    private fun showNotification(habit: Habit) {
        val notificationManager =
            ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(habit.title)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentText(ctx.getString(R.string.notify_content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getPendingIntent(habit))
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            builder.setChannelId(NOTIFICATION_CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = builder.build()
        notificationManager.notify(habit.id, notification)

    }

    companion object {
        const val CHANNEL_NAME = "habit_channel"
        const val WORKER_NAME = "notification_worker"
    }
}
