package com.ritik.telemetrylab.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritik.telemetrylab.repository.TelemetryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TelemetryViewModel @Inject constructor() : ViewModel() {
    private val latencyWindow = ArrayDeque<Long>()
    private val windowSize = 30

    private val _uiState = MutableStateFlow(TelemetryUiState())
    val uiState: StateFlow<TelemetryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            TelemetryRepository.computeUpdates.collect { (latency, powerSave) ->
                updateLatency(latency, powerSave)
            }
        }
    }

    fun start() {
        _uiState.update { it.copy(isRunning = true) }
    }

    fun stop() {
        _uiState.update { it.copy(isRunning = false) }
    }

    fun updateLoad(load: Int) {
        _uiState.update { it.copy(computeLoad = load) }
        TelemetryRepository.updateLoad(load)
    }

    fun updateLatency(latency: Long, powerSave: Boolean) {
        latencyWindow.addLast(latency)
        if (latencyWindow.size > windowSize) {
            latencyWindow.removeFirst()
        }
        val avg = latencyWindow.average().toLong()
        _uiState.update {
            it.copy(
                frameLatencyMs = latency,
                avgLatencyMs = avg,
                counterList = (it.counterList + (it.counterList.size + 1)),
                isPowerSave = powerSave
            )
        }
    }

    fun updateJankPercent(jankPercent: Float, jankFrames: Int) {
        _uiState.update {
            it.copy(
                jankFrames = jankFrames,
                jankPercent = jankPercent
            )
        }
    }
}

data class TelemetryUiState(
    val isRunning: Boolean = false,
    val computeLoad: Int = 1,
    val frameLatencyMs: Long = 0,
    val avgLatencyMs: Long = 0,
    val jankPercent: Float = 0f,
    val jankFrames: Int = 0,
    val counterList: List<Int> = emptyList(),
    val isPowerSave: Boolean = false
)
