package com.devicelens.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicelens.app.domain.model.DeviceReport
import com.devicelens.app.domain.usecase.RunDeviceScanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ScanUiState {
    data class Scanning(val step: String, val progress: Float) : ScanUiState
    data class Ready(val report: DeviceReport) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class DeviceLensViewModel(
    private val scanUseCase: RunDeviceScanUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ScanUiState>(ScanUiState.Scanning("Preparing scan", 0f))
    val state: StateFlow<ScanUiState> = _state

    private val steps = listOf(
        "Reading device identity",
        "Checking hardware",
        "Reading system state",
        "Checking permissions",
        "Preparing report"
    )

    fun startScan() {
        viewModelScope.launch {
            try {
                val report = scanUseCase.execute { step ->
                    val idx = steps.indexOf(step).takeIf { it >= 0 } ?: 0
                    val progress = (idx + 1) / (steps.size + 1f)
                    _state.value = ScanUiState.Scanning(step, progress)
                }
                _state.value = ScanUiState.Ready(report)
            } catch (e: Exception) {
                _state.value = ScanUiState.Error(e.message ?: "Scan failed")
            }
        }
    }
}
