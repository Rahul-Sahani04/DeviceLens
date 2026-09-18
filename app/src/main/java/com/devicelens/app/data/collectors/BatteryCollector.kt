package com.devicelens.app.data.collectors

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.devicelens.app.domain.model.HealthSnapshot

class BatteryCollector(private val context: Context) {
    fun collect(): HealthSnapshot {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val battery: Intent? = context.registerReceiver(null, filter)
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.takeIf { it in 0..100 }

        val status = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val chargingState = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            else -> "Unknown"
        }

        val healthInt = battery?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val batteryHealth = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            else -> "Unknown"
        }

        val tempTenths = battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val tempC = if (tempTenths != null && tempTenths != Int.MIN_VALUE) tempTenths / 10f else null

        val thermalStatus = readThermalStatus()
        val powerRestricted = readPowerRestricted()

        return HealthSnapshot(
            batteryPercent = percent,
            chargingState = chargingState,
            batteryHealth = batteryHealth,
            batteryTempCelsius = tempC,
            ramTotalGb = null, // filled by StorageMemoryCollector merge
            ramAvailableGb = null,
            storageTotalBytes = null,
            storageFreeBytes = null,
            storageUsedBytes = null,
            thermalStatus = thermalStatus,
            powerRestricted = powerRestricted
        )
    }

    private fun readThermalStatus(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                when (pm?.currentThermalStatus) {
                    PowerManager.THERMAL_STATUS_NONE -> "None"
                    PowerManager.THERMAL_STATUS_LIGHT -> "Light"
                    PowerManager.THERMAL_STATUS_MODERATE -> "Moderate"
                    PowerManager.THERMAL_STATUS_SEVERE -> "Severe"
                    PowerManager.THERMAL_STATUS_CRITICAL -> "Critical"
                    PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency"
                    PowerManager.THERMAL_STATUS_SHUTDOWN -> "Shutdown"
                    else -> "Normal"
                }
            } else "Normal"
        } catch (_: Exception) {
            "Unknown"
        }
    }

    private fun readPowerRestricted(): Boolean? {
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm?.let { it.isPowerSaveMode } // true = restricted
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
