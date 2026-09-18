package com.devicelens.app.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devicelens.app.domain.model.CapabilityResult
import com.devicelens.app.ui.theme.TextSecondary

/** Screen 03 — Capability detail sheet (PRD §5). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapabilityDetailSheet(cap: CapabilityResult, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text(cap.name.uppercase(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(cap.state.name.replace('_', ' '), color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            cap.value?.let { Text("Value  $it", fontSize = 13.sp) }
            cap.vendor?.let { Text("Vendor  $it", fontSize = 13.sp, color = TextSecondary) }
            cap.extra.forEach { (k, v) -> Text("$k  $v", fontSize = 12.sp, color = TextSecondary) }
            Spacer(Modifier.height(12.dp))
            Text(cap.explanation, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Text("Source: ${cap.source} · Confidence: ${cap.confidence}", fontSize = 11.sp, color = TextSecondary)
            Spacer(Modifier.height(24.dp))
        }
    }
}
