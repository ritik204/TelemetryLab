package com.ritik.telemetrylab.screen

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun TelemetryScreen(
    onStart: () -> Unit,
    onStop: () -> Unit,
    viewModel: TelemetryViewModel = hiltViewModel(),
    modifier: Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {

        if (uiState.isPowerSave) {
            viewModel.updateLoad(1)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("⚡ Power Save Mode: Reduced to 10Hz, Load=${uiState.computeLoad}",
                    color = Color.White)
            }
            Spacer(Modifier.height(8.dp))
        }

        Button(onClick = {
            if (uiState.isRunning) {
                onStop()
                viewModel.stop()
            } else {
                onStart()
                viewModel.start()
            }
        }) {
            Text(if (uiState.isRunning) "Stop" else "Start")
        }

        Spacer(Modifier.height(16.dp))

        Text("Compute Load: ${uiState.computeLoad}")
        Slider(
            value = uiState.computeLoad.toFloat(),
            onValueChange = { viewModel.updateLoad(it.toInt()) },
            valueRange = 1f..5f,
            steps = 3
        )

        Spacer(Modifier.height(16.dp))
        Text("Latency: ${uiState.frameLatencyMs} ms")
        Text("avg: ${uiState.avgLatencyMs}")
        Text("Jank %: ${uiState.jankPercent}")
        Text("Jank Frames: ${uiState.jankFrames}")

        Spacer(Modifier.height(16.dp))
//
//        LazyColumn(modifier = Modifier.fillMaxWidth()) {
//            items(100) { index ->
//                Text("Item $index")
//            }
//
//        }
        AnimatedCounter()
    }
}

@Composable
fun AnimatedCounter() {
    var count by remember { mutableIntStateOf(0) }
    val animatedCount by animateIntAsState(targetValue = count)

    LaunchedEffect(Unit) {
        while (true) {
            delay(100) // update every 100ms
            count++
        }
    }

    Text(
        text = "Counter: $animatedCount",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(16.dp)
    )
}
