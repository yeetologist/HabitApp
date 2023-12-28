package com.dicoding.habitapp.ui.countdown

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat.getParcelableExtra
import androidx.lifecycle.ViewModelProvider
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.notification.NotificationWorker
import com.dicoding.habitapp.utils.HABIT
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import java.util.UUID
import java.util.concurrent.TimeUnit

class CountDownActivity : AppCompatActivity() {

    private lateinit var workerUUID: UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_down)
        supportActionBar?.title = "Count Down"

        val habit = getParcelableExtra(intent, HABIT, Habit::class.java)

        if (habit != null) {
            findViewById<TextView>(R.id.tv_count_down_title).text = habit.title

            val viewModel = ViewModelProvider(this).get(CountDownViewModel::class.java)

            viewModel.setInitialTime(habit.minutesFocus)
            //TODO 10 : Set initial time and observe current time. Update button state when countdown is finished

            viewModel.currentTimeString.observe(this) {
                findViewById<TextView>(R.id.tv_count_down).text = it
            }

            findViewById<Button>(R.id.btn_start).setOnClickListener {
                viewModel.startTimer()
                updateButtonState(true)
                workerUUID = scheduleDailyReminder(habit.id, habit.title, habit.minutesFocus)
            }

            findViewById<Button>(R.id.btn_stop).setOnClickListener {
                viewModel.resetTimer()
                updateButtonState(false)
                cancelScheduledReminder(workerUUID)
            }

            //TODO 13 : Start and cancel One Time Request WorkManager to notify when time is up.
            viewModel.eventCountDownFinish.observe(this) {
                updateButtonState(!it)
            }
        }

    }

    private fun scheduleDailyReminder(habitId: Int, habitTitle: String, minutes: Long): UUID {
        val workManager = WorkManager.getInstance(applicationContext)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val inputData = Data.Builder()
            .putInt(HABIT_ID, habitId)
            .putString(HABIT_TITLE, habitTitle)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setConstraints(constraints)
            .setInitialDelay(minutes, TimeUnit.MINUTES)
            .setInputData(inputData)
            .build()

        workManager.enqueue(oneTimeWorkRequest)
        return oneTimeWorkRequest.id
    }

    private fun cancelScheduledReminder(workRequestId: UUID) {
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.cancelWorkById(workRequestId)
    }


    private fun updateButtonState(isRunning: Boolean) {
        findViewById<Button>(R.id.btn_start).isEnabled = !isRunning
        findViewById<Button>(R.id.btn_stop).isEnabled = isRunning
    }
}