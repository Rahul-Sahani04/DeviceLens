package com.devicelens.app.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devicelens.app.domain.model.CapabilityResult
import com.devicelens.app.domain.model.CapabilityState
import com.devicelens.app.domain.model.DeviceReport
import com.devicelens.app.ui.theme.Danger
import com.devicelens.app.ui.theme.PrimaryGreen
import com.devicelens.app.ui.theme.Surface
import com.devicelens.app.ui.theme.TextSecondary
import com.devicelens.app.ui.theme.Warning
import com.devicelens.app.util.FormatUtils

private fun stateMark(state: CapabilityState): String = when (state) {
    CapabilityState.AVAILABLE, CapabilityState.ENABLED -> "✓"
    CapabilityState.RESTRICTED -> "!"
    CapabilityState.UNAVAILABLE -> "×"
    CapabilityState.NOT_EXPOSED -> "?"
}

private fun stateColor(state: CapabilityState) = when (state) {
    CapabilityState.AVAILABLE, CapabilityState.ENABLED -> PrimaryGreen
    CapabilityState.RESTRICTED -> Warning
    CapabilityState.UNAVAILABLE -> Danger
    CapabilityState.NOT_EXPOSED -> TextSecondary
}

/** Screen 02 — Dashboard (PRD §5). */
@Composable
fun DashboardScreen(
    report: DeviceReport,
    onCapabilityClick: (CapabilityResult) -> Unit,
    onGenerateReport: () -> Unit,
    onViewRaw: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("DEVICELENS", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            "${report.device.manufacturer} ${report.device.model}",
            fontSize = 20.sp, fontWeight = FontWeight.SemiBold
        )
        Text(
            "Android ${report.device.androidVersion} · API ${report.device.apiLevel}",
            color = TextSecondary, fontSize = 13.sp
        )
        Text(report.device.oemSkin, color = TextSecondary, fontSize = 13.sp)

        Spacer(Modifier.height(16.dp))
        Text("DEVICE HEALTH", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Surface)) {
            Column(Modifier.padding(16.dp)) {
                HealthRow("Battery", FormatUtils.percentLabel(report.health.batteryPercent))
                HealthRow("Storage", "${FormatUtils.bytesToGb(report.health.storageFreeBytes)} free")
                HealthRow("RAM", report.health.ramTotalGb?.let { "%.1f GB".format(it) } ?: "—")
                HealthRow("Temperature", FormatUtils.tempLabel(report.health.batteryTempCelsius))
                HealthRow("Thermal", report.health.thermalStatus)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("CAPABILITIES", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(320.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(report.capabilities.take(12)) { cap ->
                Card(
                    modifier = Modifier.clickable { onCapabilityClick(cap) },
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Row(Modifier.padding(12.dp)) {
                        Text(
                            stateMark(cap.state),
                            color = stateColor(cap.state),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(cap.name, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("APP ENVIRONMENT", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Surface)) {
            val e = report.environment
            Column(Modifier.padding(16.dp)) {
                HealthRow("Battery optimization", if (e.batteryOptimizationExempt == true) "EXEMPT" else "ON")
                HealthRow("Notifications", if (e.notificationsEnabled) "ON" else "OFF")
                HealthRow("Microphone", e.microphonePermission.uppercase())
                HealthRow("Camera", e.cameraPermission.uppercase())
                HealthRow("Location", if (e.locationEnabled) "ON" else "OFF")
                HealthRow("Bluetooth", when (e.bluetoothEnabled) { true -> "ON"; false -> "OFF"; null -> "—" })
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(onClick = onGenerateReport, modifier = Modifier.fillMaxWidth()) {
            Text("GENERATE DEVICE REPORT")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onViewRaw, modifier = Modifier.fillMaxWidth()) {
            Text("VIEW RAW DATA")
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun HealthRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
    }
}
