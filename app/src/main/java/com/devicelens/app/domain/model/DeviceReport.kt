package com.devicelens.app.domain.model

import java.time.Instant

/**
 * Normalized device report. UI must render from this model,
 * never query Android APIs directly (per PRD §8).
 */
data class DeviceReport(
    val generatedAt: Instant,
    val device: DeviceIdentity,
    val health: HealthSnapshot,
    val capabilities: List<CapabilityResult>,
    val environment: EnvironmentSnapshot
)

data class DeviceIdentity(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val deviceCodename: String,
    val androidVersion: String,
    val apiLevel: Int,
    val buildLabel: String,
    val oemSkin: String
)

data class HealthSnapshot(
    val batteryPercent: Int?,
    val chargingState: String,
    val batteryHealth: String,
    val batteryTempCelsius: Float?,
    val ramTotalGb: Double?,
    val ramAvailableGb: Double?,
    val storageTotalBytes: Long?,
    val storageFreeBytes: Long?,
    val storageUsedBytes: Long?,
    val thermalStatus: String,
    val powerRestricted: Boolean?
)

enum class CapabilityState {
    AVAILABLE,
    ENABLED,
    RESTRICTED,
    UNAVAILABLE,
    NOT_EXPOSED
}

data class CapabilityResult(
    val id: String,
    val name: String,
    val state: CapabilityState,
    val value: String?,
    val source: String,
    val confidence: String,
    val explanation: String,
    val vendor: String? = null,
    val extra: Map<String, String> = emptyMap()
)

data class EnvironmentSnapshot(
    val cameraPermission: String,
    val microphonePermission: String,
    val locationPermission: String,
    val nearbyDevicesPermission: String,
    val notificationsEnabled: Boolean,
    val batteryOptimizationExempt: Boolean?,
    val bluetoothEnabled: Boolean?,
    val locationEnabled: Boolean
)

/** Helpers for report summary counts (PRD Screen 04). */
fun List<CapabilityResult>.countByState(state: CapabilityState): Int = count { it.state == state }
