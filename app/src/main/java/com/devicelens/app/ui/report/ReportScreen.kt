package com.devicelens.app.ui.report

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devicelens.app.domain.model.CapabilityState
import com.devicelens.app.domain.model.DeviceReport
import com.devicelens.app.domain.model.countByState
import com.devicelens.app.ui.theme.Surface
import com.devicelens.app.ui.theme.TextSecondary
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Screen 04 — Report preview (PRD §5). */
@Composable
fun ReportScreen(
    report: DeviceReport,
    exporting: Boolean,
    onExportPdf: () -> Unit,
    onExportJson: () -> Unit,
    onBack: () -> Unit
) {
    val date = report.generatedAt.atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    val available = report.capabilities.countByState(CapabilityState.AVAILABLE) +
        report.capabilities.countByState(CapabilityState.ENABLED)
    val restricted = report.capabilities.countByState(CapabilityState.RESTRICTED)
    val unavailable = report.capabilities.countByState(CapabilityState.UNAVAILABLE)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("DEVICE REPORT", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("${report.device.manufacturer} ${report.device.model}", fontSize = 16.sp)
        Text("Generated $date", color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("HEALTH", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("${report.health.batteryPercent ?: "?"} / 100 battery", fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text("CAPABILITIES", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("$available available · $restricted restricted · $unavailable unavailable", fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text("SYSTEM", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Android ${report.device.androidVersion} · API ${report.device.apiLevel}", fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onExportPdf,
                enabled = !exporting,
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) { Text(if (exporting) "GENERATING…" else "EXPORT PDF") }
            OutlinedButton(
                onClick = onExportJson,
                enabled = !exporting,
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            ) { Text("JSON") }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("BACK") }
        Spacer(Modifier.height(8.dp))
        Text(
            "Generated locally by DeviceLens · No cloud data sent",
            color = TextSecondary, fontSize = 11.sp
        )
    }
}
