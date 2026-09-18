package com.devicelens.app.data.collectors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.devicelens.app.domain.model.CapabilityResult
import com.devicelens.app.domain.model.CapabilityState

/** Sensor inventory via SensorManager — "Available" means exposed to the app (PRD §3). */
class SensorCollector(private val context: Context) {

    fun collect(): List<CapabilityResult> {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            ?: return listOf(
                CapabilityResult(
                    id = "sensors", name = "Sensors",
                    state = CapabilityState.NOT_EXPOSED, value = null,
                    source = "SensorManager", confidence = "low",
                    explanation = "SensorManager was not accessible on this device."
                )
            )
        val out = mutableListOf<CapabilityResult>()
        out += sensorResult(sm, Sensor.TYPE_ACCELEROMETER, "accelerometer", "Accelerometer")
        out += sensorResult(sm, Sensor.TYPE_GYROSCOPE, "gyroscope", "Gyroscope")
        out += sensorResult(sm, Sensor.TYPE_MAGNETIC_FIELD, "magnetometer", "Magnetometer")
        out += sensorResult(sm, Sensor.TYPE_PROXIMITY, "proximity", "Proximity")
        out += sensorResult(sm, Sensor.TYPE_LIGHT, "light", "Light sensor")
        // Camera count when accessible
        out += cameraCountResult()
        return out
    }

    private fun sensorResult(
        sm: SensorManager, type: Int, id: String, name: String
    ): CapabilityResult {
        val sensor = runCatching { sm.getDefaultSensor(type) }.getOrNull()
        return if (sensor != null) {
            CapabilityResult(
                id = id, name = name,
                state = CapabilityState.AVAILABLE,
                value = sensor.name,
                source = "SensorManager.getDefaultSensor($type)",
                confidence = "high",
                explanation = "Sensor is exposed by Android and available to applications.",
                vendor = sensor.vendor,
                extra = mapOf(
                    "power_mA" to sensor.power.toString(),
                    "resolution" to sensor.resolution.toString(),
                    "maxRange" to sensor.maximumRange.toString()
                )
            )
        } else {
            CapabilityResult(
                id = id, name = name,
                state = CapabilityState.UNAVAILABLE,
                value = "Absent",
                source = "SensorManager.getDefaultSensor($type)",
                confidence = "high",
                explanation = "Android did not expose a $name sensor to applications."
            )
        }
    }

    private fun cameraCountResult(): CapabilityResult {
        return try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as? android.hardware.camera2.CameraManager
            val ids = cm?.cameraIdList
            if (ids != null) {
                CapabilityResult(
                    id = "camera_count", name = "Camera count",
                    state = CapabilityState.AVAILABLE,
                    value = "${ids.size} camera(s)",
                    source = "CameraManager.cameraIdList",
                    confidence = "high",
                    explanation = "Cameras enumerated via Camera2 API."
                )
            } else {
                CapabilityResult(
                    id = "camera_count", name = "Camera count",
                    state = CapabilityState.NOT_EXPOSED, value = null,
                    source = "CameraManager",
                    confidence = "medium",
                    explanation = "Camera count was not accessible without additional permission/state."
                )
            }
        } catch (e: Exception) {
            CapabilityResult(
                id = "camera_count", name = "Camera count",
                state = CapabilityState.RESTRICTED, value = null,
                source = "CameraManager",
                confidence = "medium",
                explanation = "Camera enumeration was restricted: ${e.message?.take(120)}"
            )
        }
    }
}
