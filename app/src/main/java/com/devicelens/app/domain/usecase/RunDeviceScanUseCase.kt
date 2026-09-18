package com.devicelens.app.domain.usecase

import android.content.Context
import com.devicelens.app.data.collectors.BatteryCollector
import com.devicelens.app.data.collectors.CapabilityCollector
import com.devicelens.app.data.collectors.DeviceInfoCollector
import com.devicelens.app.data.collectors.EnvironmentCollector
import com.devicelens.app.data.collectors.SensorCollector
import com.devicelens.app.data.collectors.StorageMemoryCollector
import com.devicelens.app.domain.model.CapabilityResult
import com.devicelens.app.domain.model.CapabilityState
import com.devicelens.app.domain.model.DeviceReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.time.Instant

/** Orchestrates the 8-step scan sequence from PRD §4. */
class RunDeviceScanUseCase(private val context: Context) {

    private fun restrictedEntry(id: String, name: String, detail: String) = CapabilityResult(
        id = id, name = name,
        state = CapabilityState.RESTRICTED,
        value = null,
        source = "DeviceLens scan guard",
        confidence = "low",
        explanation = "Section failed gracefully: ${detail.take(160)}"
    )

    suspend fun execute(onStep: (String) -> Unit = {}): DeviceReport = withContext(Dispatchers.Default) {
        val appCtx = context.applicationContext
        onStep("Reading device identity")
        val deviceDeferred = async {
            runCatching { DeviceInfoCollector(appCtx).collect() }.getOrElse {
                com.devicelens.app.domain.model.DeviceIdentity(
                    "Unknown", "Unknown", android.os.Build.MODEL ?: "Unknown",
                    android.os.Build.DEVICE ?: "Unknown",
                    android.os.Build.VERSION.RELEASE ?: "Unknown",
                    android.os.Build.VERSION.SDK_INT,
                    android.os.Build.DISPLAY ?: "Unknown", "Stock / Unknown"
                )
            }
        }

        onStep("Reading system state")
        val batteryDeferred = async {
            runCatching { BatteryCollector(appCtx).collect() }.getOrElse {
                com.devicelens.app.domain.model.HealthSnapshot(
                    null, "Unknown", "Unknown", null,
                    null, null, null, null, null, "Unknown", null
                )
            }
        }
        val storageDeferred = async {
            runCatching { StorageMemoryCollector(appCtx).collect() }.getOrNull()
        }

        onStep("Checking hardware")
        val capsDeferred = async {
            runCatching { CapabilityCollector(appCtx).collect() }
                .getOrElse { e -> listOf(restrictedEntry("capabilities", "Capabilities", e.message ?: "unknown")) }
        }
        val sensorsDeferred = async {
            runCatching { SensorCollector(appCtx).collect() }.getOrDefault(emptyList())
        }

        onStep("Checking permissions")
        val envDeferred = async {
            runCatching { EnvironmentCollector(appCtx).collect() }.getOrElse {
                com.devicelens.app.domain.model.EnvironmentSnapshot(
                    "Unknown", "Unknown", "Unknown", "Unknown",
                    true, null, null, false
                )
            }
        }

        onStep("Preparing report")
        val battery = batteryDeferred.await()
        val storage = storageDeferred.await()

        DeviceReport(
            generatedAt = Instant.now(),
            device = deviceDeferred.await(),
            health = battery.copy(
                ramTotalGb = storage?.ramTotalGb,
                ramAvailableGb = storage?.ramAvailableGb,
                storageTotalBytes = storage?.storageTotalBytes,
                storageFreeBytes = storage?.storageFreeBytes,
                storageUsedBytes = storage?.storageUsedBytes
            ),
            capabilities = capsDeferred.await() + sensorsDeferred.await(),
            environment = envDeferred.await()
        )
    }
}
