package com.ritik.telemetrylab.repository


import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object TelemetryRepository {
    private val _computeUpdates = MutableSharedFlow<Pair<Long, Boolean>>(replay = 1)
    val computeUpdates: SharedFlow<Pair<Long, Boolean>> = _computeUpdates.asSharedFlow()

    suspend fun postUpdate(latency: Long, isPowerSave: Boolean) {
        _computeUpdates.emit(latency to isPowerSave)
    }
    private val _loadFlow = MutableStateFlow(1) // default load
    val loadFlow: StateFlow<Int> = _loadFlow.asStateFlow()

    fun updateLoad(load: Int) {
        _loadFlow.value = load
    }
}
