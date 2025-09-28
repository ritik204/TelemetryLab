package com.ritik.telemetrylab

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.metrics.performance.FrameData
import androidx.metrics.performance.JankStats
import com.ritik.telemetrylab.screen.TelemetryScreen
import com.ritik.telemetrylab.screen.TelemetryViewModel
import com.ritik.telemetrylab.service.TelemetryService
import com.ritik.telemetrylab.ui.theme.TelemetryLabTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var jankStats: JankStats
    private lateinit var viewModel: TelemetryViewModel

    private var totalFrames = 0
    private var jankFrames = 0
    private var startTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TelemetryLabTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    viewModel = hiltViewModel()

                    // Track Jank
                    jankStats = JankStats.createAndTrack(
                        window,
                        object : JankStats.OnFrameListener {
                            override fun onFrame(volatileFrameData: FrameData) {
                                totalFrames++
                                if (volatileFrameData.isJank) jankFrames++
                                if (startTime == 0L) startTime = System.currentTimeMillis()

                                val now = System.currentTimeMillis()
                                if (now - startTime >= 30_000) {
                                    val jankPercent =
                                        if (totalFrames > 0) (jankFrames * 100f / totalFrames)
                                            .roundToInt()
                                            .toFloat()
                                        else 0f
                                    println("total frames $totalFrames")
                                    viewModel.updateJankPercent(jankPercent, jankFrames)

                                    startTime = now
                                    totalFrames = 0
                                    jankFrames = 0
                                }
                            }
                        }
                    )

                    TelemetryScreen(
                        onStart = { startTelemetryService() },
                        onStop = { stopTelemetryService() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun startTelemetryService() {
        val intent = Intent(this, TelemetryService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopTelemetryService() {
        stopService(Intent(this, TelemetryService::class.java))
    }

    override fun onDestroy() {
        jankStats.isTrackingEnabled = false
        super.onDestroy()
    }
}
