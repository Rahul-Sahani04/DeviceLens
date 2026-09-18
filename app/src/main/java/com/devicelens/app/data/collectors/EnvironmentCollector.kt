package com.devicelens.app.data.collectors

import android.Manifest
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.devicelens.app.domain.model.EnvironmentSnapshot

/** App environment: permission + runtime state. Never forces a permission wall (PRD §10). */
class EnvironmentCollector(private val context: Context) {

    fun collect(): EnvironmentSnapshot {
        return EnvironmentSnapshot(
            cameraPermission = permLabel(Manifest.permission.CAMERA),
            microphonePermission = permLabel(Manifest.permission.RECORD_AUDIO),
            locationPermission = locationPermLabel(),
            nearbyDevicesPermission = nearbyPermLabel(),
            notificationsEnabled = notificationsEnabled(),
            batteryOptimizationExempt = batteryExempt(),
            bluetoothEnabled = bluetoothEnabled(),
            locationEnabled = locationEnabled()
        )
    }

    private fun permLabel(perm: String): String {
        return try {
            when (ContextCompat.checkSelfPermission(context, perm)) {
                PackageManager.PERMISSION_GRANTED -> "Granted"
                else -> "Denied"
            }
        } catch (_: Exception) { "Unknown" }
    }

    private fun locationPermLabel(): String {
        val fine = runCatching {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        }.getOrDefault(PackageManager.PERMISSION_DENIED)
        val coarse = runCatching {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        }.getOrDefault(PackageManager.PERMISSION_DENIED)
        return if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) "Granted" else "Denied"
    }

    private fun nearbyPermLabel(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return "Not required (API < 31)"
        val scan = runCatching {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
        }.getOrDefault(PackageManager.PERMISSION_DENIED)
        val connect = runCatching {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
        }.getOrDefault(PackageManager.PERMISSION_DENIED)
        return if (scan == PackageManager.PERMISSION_GRANTED || connect == PackageManager.PERMISSION_GRANTED) "Granted" else "Denied"
    }

    private fun notificationsEnabled(): Boolean {
        return try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.areNotificationsEnabled() ?: true
        } catch (_: Exception) { true }
    }

    private fun batteryExempt(): Boolean? {
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(context.packageName)
        } catch (_: Exception) { null }
    }

    private fun bluetoothEnabled(): Boolean? {
        return try {
            val adapter = BluetoothAdapter.getDefaultAdapter() ?: return null
            adapter.isEnabled
        } catch (_: Exception) { null }
    }

    private fun locationEnabled(): Boolean {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return false
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) { false }
    }
}
