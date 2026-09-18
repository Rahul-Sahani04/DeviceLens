package com.devicelens.app.data.report

import com.devicelens.app.domain.model.CapabilityState
import com.devicelens.app.domain.model.DeviceReport
import com.devicelens.app.domain.model.countByState
import org.json.JSONArray
import org.json.JSONObject
import java.time.format.DateTimeFormatter

/** Machine-readable JSON export, schemaVersion 1.0 (PRD §11). */
object JsonReportGenerator {
    fun generate(report: DeviceReport): String {
        val root = JSONObject()
        root.put("schemaVersion", "1.0")
        root.put("generatedAt", DateTimeFormatter.ISO_INSTANT.format(report.generatedAt))

        val device = JSONObject()
            .put("manufacturer", report.device.manufacturer)
            .put("brand", report.device.brand)
            .put("model", report.device.model)
            .put("deviceCodename", report.device.deviceCodename)
            .put("androidVersion", report.device.androidVersion)
            .put("apiLevel", report.device.apiLevel)
            .put("buildLabel", report.device.buildLabel)
            .put("oemSkin", report.device.oemSkin)
        root.put("device", device)

        val health = JSONObject()
            .put("batteryPercent", report.health.batteryPercent)
            .put("chargingState", report.health.chargingState)
            .put("batteryHealth", report.health.batteryHealth)
            .put("batteryTempCelsius", report.health.batteryTempCelsius)
            .put("ramTotalGb", report.health.ramTotalGb)
            .put("ramAvailableGb", report.health.ramAvailableGb)
            .put("storageTotalBytes", report.health.storageTotalBytes)
            .put("storageFreeBytes", report.health.storageFreeBytes)
            .put("storageUsedBytes", report.health.storageUsedBytes)
            .put("thermalStatus", report.health.thermalStatus)
            .put("powerRestricted", report.health.powerRestricted)
        root.put("health", health)

        val caps = JSONArray()
        report.capabilities.forEach { c ->
            caps.put(
                JSONObject()
                    .put("id", c.id)
                    .put("name", c.name)
                    .put("state", c.state.name)
                    .put("value", c.value)
                    .put("source", c.source)
                    .put("confidence", c.confidence)
                    .put("explanation", c.explanation)
                    .put("vendor", c.vendor)
            )
        }
        root.put("capabilities", caps)

        val env = JSONObject()
            .put("cameraPermission", report.environment.cameraPermission)
            .put("microphonePermission", report.environment.microphonePermission)
            .put("locationPermission", report.environment.locationPermission)
            .put("nearbyDevicesPermission", report.environment.nearbyDevicesPermission)
            .put("notificationsEnabled", report.environment.notificationsEnabled)
            .put("batteryOptimizationExempt", report.environment.batteryOptimizationExempt)
            .put("bluetoothEnabled", report.environment.bluetoothEnabled)
            .put("locationEnabled", report.environment.locationEnabled)
        root.put("environment", env)

        // Summary block mirrors Screen 04 counts
        root.put(
            "summary", JSONObject()
                .put("available", report.capabilities.countByState(CapabilityState.AVAILABLE) + report.capabilities.countByState(CapabilityState.ENABLED))
                .put("restricted", report.capabilities.countByState(CapabilityState.RESTRICTED))
                .put("unavailable", report.capabilities.countByState(CapabilityState.UNAVAILABLE))
                .put("notExposed", report.capabilities.countByState(CapabilityState.NOT_EXPOSED))
        )

        return root.toString(2)
    }
}
