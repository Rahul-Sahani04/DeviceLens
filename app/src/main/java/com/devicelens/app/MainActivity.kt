package com.devicelens.app

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devicelens.app.data.report.JsonReportGenerator
import com.devicelens.app.data.report.PdfReportGenerator
import com.devicelens.app.domain.model.CapabilityResult
import com.devicelens.app.domain.usecase.RunDeviceScanUseCase
import com.devicelens.app.ui.DeviceLensViewModel
import com.devicelens.app.ui.ScanUiState
import com.devicelens.app.ui.dashboard.DashboardScreen
import com.devicelens.app.ui.detail.CapabilityDetailSheet
import com.devicelens.app.ui.report.ReportScreen
import com.devicelens.app.ui.scan.ScanScreen
import com.devicelens.app.ui.theme.DeviceLensTheme
import com.devicelens.app.util.ShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scanUseCase = RunDeviceScanUseCase(applicationContext)
        setContent {
            DeviceLensTheme {
                val factory = remember { DeviceLensViewModelFactory(scanUseCase) }
                val vm: DeviceLensViewModel = viewModel(factory = factory)
                LaunchedEffect(Unit) { vm.startScan() }
                AppNav(vm)
            }
        }
    }

    private inner class DeviceLensViewModelFactory(
        private val useCase: RunDeviceScanUseCase
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return DeviceLensViewModel(useCase) as T
        }
    }
}

@Composable
private fun AppNav(vm: DeviceLensViewModel) {
    val nav = rememberNavController()
    val state by vm.state.collectAsState()
    var selected: CapabilityResult? by remember { mutableStateOf(null) }
    var exporting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // context for export actions
    val ctx = androidx.compose.ui.platform.LocalContext.current

    Scaffold { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is ScanUiState.Scanning -> ScanScreen(
                    step = s.step, progress = s.progress,
                    model = Build.MODEL ?: "Android device"
                )
                is ScanUiState.Error -> Text("Scan failed: ${s.message}")
                is ScanUiState.Ready -> {
                    val report = s.report
                    NavHost(navController = nav, startDestination = "dashboard") {
                        composable("dashboard") {
                            DashboardScreen(
                                report = report,
                                onCapabilityClick = { selected = it },
                                onGenerateReport = { nav.navigate("report") },
                                onViewRaw = { nav.navigate("raw") }
                            )
                        }
                        composable("report") {
                            ReportScreen(
                                report = report,
                                exporting = exporting,
                                onExportPdf = {
                                    scope.launch {
                                        exporting = true
                                        try {
                                            val file = withContext(Dispatchers.IO) {
                                                val dir = ShareHelper.reportsDir(ctx)
                                                val out = File(dir, "DeviceLens-report.pdf")
                                                PdfReportGenerator.generate(report, out)
                                                out
                                            }
                                            ShareHelper.shareFile(ctx, file, "application/pdf")
                                        } catch (e: Exception) {
                                            Toast.makeText(ctx, "PDF failed: ${e.message}", Toast.LENGTH_LONG).show()
                                        } finally { exporting = false }
                                    }
                                },
                                onExportJson = {
                                    scope.launch {
                                        exporting = true
                                        try {
                                            val file = withContext(Dispatchers.IO) {
                                                val dir = ShareHelper.reportsDir(ctx)
                                                val out = File(dir, "DeviceLens-report.json")
                                                out.writeText(JsonReportGenerator.generate(report))
                                                out
                                            }
                                            ShareHelper.shareFile(ctx, file, "application/json")
                                        } catch (e: Exception) {
                                            Toast.makeText(ctx, "JSON failed: ${e.message}", Toast.LENGTH_LONG).show()
                                        } finally { exporting = false }
                                    }
                                },
                                onBack = { nav.popBackStack() }
                            )
                        }
                        composable("raw") {
                            RawDataScreen(
                                report = s.report,
                                onBack = { nav.popBackStack() }
                            )
                        }
                    }
                    selected?.let { cap ->
                        CapabilityDetailSheet(cap = cap, onDismiss = { selected = null })
                    }
                }
            }
        }
    }
}

@Composable
private fun RawDataScreen(report: com.devicelens.app.domain.model.DeviceReport, onBack: () -> Unit) {
    val json = remember(report) { JsonReportGenerator.generate(report) }
    androidx.compose.foundation.layout.Column(
        Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("RAW DATA")
        androidx.compose.foundation.layout.Spacer(Modifier.padding(4.dp))
        androidx.compose.foundation.lazy.LazyColumn(Modifier.weight(1f)) {
            item { Text(json, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) }
        }
        TextButton(onClick = onBack) { Text("BACK") }
    }
}
