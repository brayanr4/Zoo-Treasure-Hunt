package com.example.zootreasurehunt.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.zootreasurehunt.R

class CongratulationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val animalName = inputData.getString("ANIMAL_NAME") ?: "Animal"
        createNotificationChannel()
        showNotification(animalName)
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "zoo_channel",
                "Zoo Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for Zoo Treasure Hunt"
            }

            val notificationManager =
                applicationContext.getSystemService<NotificationManager>()

            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun showNotification(animalName: String) {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(applicationContext, "zoo_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Great Job!")
            .setContentText("You found the $animalName!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(1, notification)
    }
}