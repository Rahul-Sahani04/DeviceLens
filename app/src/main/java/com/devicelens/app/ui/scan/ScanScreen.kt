package com.devicelens.app.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devicelens.app.ui.theme.AccentBlue
import com.devicelens.app.ui.theme.TextSecondary

/** Screen 01 — Launch / Scan (PRD §5). */
@Composable
fun ScanScreen(step: String, progress: Float, model: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("DEVICELENS", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text("LOCAL DEVICE SCAN", color = TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.height(28.dp))
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(120.dp),
            color = AccentBlue,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        Text(model, color = TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text(step, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        Text("SCANNING DEVICE…", color = TextSecondary, fontSize = 11.sp)
    }
}
