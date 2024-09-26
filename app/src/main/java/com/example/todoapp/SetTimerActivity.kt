package com.example.todoapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class SetTimerActivity : AppCompatActivity() {

    private lateinit var dayInput: EditText
    private lateinit var hourInput: EditText
    private lateinit var minuteInput: EditText
    private lateinit var secondInput: EditText
    private lateinit var startTimerButton: Button
    private lateinit var currentTodo: Todo
    private val channelId = "todoTimerChannel"
    private val POST_NOTIFICATION_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_timer)

        // Initializing the views
        dayInput = findViewById(R.id.dayInput)
        hourInput = findViewById(R.id.hourInput)
        minuteInput = findViewById(R.id.minuteInput)
        secondInput = findViewById(R.id.secondInput)
        startTimerButton = findViewById(R.id.startTimerButton)

        // Get the current todo item using the correct method for Parcelable
        currentTodo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("todo", Todo::class.java)
        } else {
            intent.getParcelableExtra("todo")
        } ?: return

        // Check for notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), POST_NOTIFICATION_PERMISSION_REQUEST_CODE)
            }
        }

        createNotificationChannel()

        startTimerButton.setOnClickListener {
            // Get the user input values for days, hours, minutes, seconds
            val days = dayInput.text.toString().toLongOrNull() ?: 0L
            val hours = hourInput.text.toString().toLongOrNull() ?: 0L
            val minutes = minuteInput.text.toString().toLongOrNull() ?: 0L
            val seconds = secondInput.text.toString().toLongOrNull() ?: 0L

            // Convert total time to milliseconds
            val totalMillis = ((days * 24 * 60 * 60) + (hours * 60 * 60) + (minutes * 60) + seconds) * 1000

            // Start the countdown timer
            startTimer(totalMillis)

            // Navigate back to HomeActivity immediately after setting the timer
            val homeIntent = Intent(this@SetTimerActivity, HomeActivity::class.java)
            startActivity(homeIntent)
            finish() // Close the SetTimerActivity
        }
    }

    private fun startTimer(duration: Long) {
        object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update UI if needed
            }

            override fun onFinish() {
                sendNotification(currentTodo.title)

                // Navigate back to HomeActivity after timer ends
                val intent = Intent(this@SetTimerActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.start()
    }

    private fun sendNotification(todoTitle: String) {
        val intent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Timer Finished")
            .setContentText("Timer is over for task: $todoTitle")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(this)) {
            notify(1, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Todo Timer Channel"
            val descriptionText = "Channel for Todo Timer notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == POST_NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, continue with notifications
            } else {
                // Permission denied, handle accordingly
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
