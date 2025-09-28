package com.ritik.telemetrylab.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ritik.telemetrylab.R
import com.ritik.telemetrylab.data.Convolution
import com.ritik.telemetrylab.repository.TelemetryRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

@AndroidEntryPoint
class TelemetryService : Service() {

    @Inject lateinit var convolution: Convolution

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var computeJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Telemetry Running")
            .setContentText("Edge compute in progress…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startCompute()
        return START_STICKY
    }

    private fun startCompute() {
        computeJob?.cancel()
        computeJob = serviceScope.launch {
            val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager

            while (isActive) {
                val currentLoad = TelemetryRepository.loadFlow.value
                println(currentLoad)
                val isPowerSave = powerManager.isPowerSaveMode
                val frameInterval = if (isPowerSave) 100L else 50L
                val adjustedLoad = if (isPowerSave) max(1, currentLoad - 1) else currentLoad

                val start = System.nanoTime()
                convolution.runCompute(adjustedLoad)
                val latency = (System.nanoTime() - start) / 1_000_000


                TelemetryRepository.postUpdate(latency, isPowerSave)

                delay(frameInterval)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        computeJob?.cancel()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Telemetry Service",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "telemetry"
        private const val NOTIFICATION_ID = 1
    }
}
