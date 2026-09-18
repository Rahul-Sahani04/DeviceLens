package com.devicelens.app.data.collectors

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.hardware.fingerprint.FingerprintManager
import android.location.LocationManager
import android.nfc.NfcAdapter
import android.os.Build
import com.devicelens.app.domain.model.CapabilityResult
import com.devicelens.app.domain.model.CapabilityState

/**
 * Hardware capabilities via PackageManager.hasSystemFeature + adapter checks.
 * Never infers; unavailable features are UNAVAILABLE, ambiguous ones NOT_EXPOSED (PRD §3).
 */
class CapabilityCollector(private val context: Context) {

    fun collect(): List<CapabilityResult> {
        val pm = context.packageManager
        val out = mutableListOf<CapabilityResult>()

        // Camera
        val hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        out += CapabilityResult(
            id = "camera", name = "Camera",
            state = if (hasCamera) CapabilityState.AVAILABLE else CapabilityState.UNAVAILABLE,
            value = if (hasCamera) "Present" else "Absent",
            source = "PackageManager.hasSystemFeature(FEATURE_CAMERA_ANY)",
            confidence = "high",
            explanation = if (hasCamera) "Android exposes a camera to applications."
            else "No camera system feature was reported by Android on this device."
        )

        // Microphone: no direct FEATURE_MICROPHONE on all APIs; use boolean feature + audio record capability
        val hasMic = pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
        out += CapabilityResult(
            id = "microphone", name = "Microphone",
            state = if (hasMic) CapabilityState.AVAILABLE else CapabilityState.NOT_EXPOSED,
            value = if (hasMic) "Present" else "Unknown",
            source = "PackageManager.hasSystemFeature(FEATURE_MICROPHONE)",
            confidence = if (hasMic) "high" else "medium",
            explanation = if (hasMic) "Microphone input is exposed by Android."
            else "Android did not report a microphone feature; hardware state is not directly exposed."
        )

        // Bluetooth — getName()/isEnabled need BLUETOOTH_CONNECT on API 31+;
        // never let a missing runtime permission crash the scan (PRD §14).
        val btAvailable = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        val btAdapter = runCatching { BluetoothAdapter.getDefaultAdapter() }.getOrNull()
        val btName = runCatching { btAdapter?.name }.getOrNull()
        val btEnabled = runCatching { btAdapter?.isEnabled }.getOrNull()
        out += CapabilityResult(
            id = "bluetooth", name = "Bluetooth",
            state = if (btAvailable && btAdapter != null) CapabilityState.AVAILABLE
            else if (btAvailable) CapabilityState.RESTRICTED
            else CapabilityState.UNAVAILABLE,
            value = btName ?: btEnabled?.let { if (it) "Adapter available (on)" else "Adapter available (off)" },
            source = "PackageManager.FEATURE_BLUETOOTH + BluetoothAdapter",
            confidence = "high",
            explanation = when {
                btAvailable && btAdapter != null -> "Bluetooth adapter is available."
                btAvailable -> "Bluetooth feature reported but no default adapter was returned."
                else -> "No Bluetooth system feature was reported by Android."
            }
        )

        // BLE
        val hasBle = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        out += CapabilityResult(
            id = "ble", name = "Bluetooth LE",
            state = if (hasBle) CapabilityState.AVAILABLE else CapabilityState.UNAVAILABLE,
            value = if (hasBle) "Supported" else "Unsupported",
            source = "PackageManager.FEATURE_BLUETOOTH_LE",
            confidence = "high",
            explanation = if (hasBle) "Bluetooth LE is reported as available."
            else "Bluetooth LE was not reported by Android."
        )

        // NFC
        val hasNfc = pm.hasSystemFeature(PackageManager.FEATURE_NFC)
        val nfcAdapter = runCatching { NfcAdapter.getDefaultAdapter(context) }.getOrNull()
        out += CapabilityResult(
            id = "nfc", name = "NFC",
            state = if (hasNfc && nfcAdapter != null) CapabilityState.AVAILABLE
            else if (hasNfc) CapabilityState.RESTRICTED
            else CapabilityState.UNAVAILABLE,
            value = if (hasNfc) "Present" else "Absent",
            source = "PackageManager.FEATURE_NFC + NfcAdapter",
            confidence = "high",
            explanation = if (hasNfc) "NFC hardware is reported by Android."
            else "No NFC system feature was reported by Android on this device."
        )

        // Location / GNSS — distinguish capability vs enabled state (PRD §3)
        val hasGps = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
        val hasLocation = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION)
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val providers = runCatching { lm?.getProviders(true)?.joinToString() }.getOrNull()
        val locState = when {
            hasGps -> CapabilityState.AVAILABLE
            hasLocation -> CapabilityState.RESTRICTED
            else -> CapabilityState.UNAVAILABLE
        }
        out += CapabilityResult(
            id = "location_gnss", name = "Location / GNSS",
            state = locState,
            value = providers,
            source = "PackageManager.FEATURE_LOCATION_GPS + LocationManager",
            confidence = "high",
            explanation = "Hardware/provider capability. Whether location services are switched on is shown under App Environment."
        )

        // Telephony
        val hasTel = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        out += CapabilityResult(
            id = "telephony", name = "Telephony",
            state = if (hasTel) CapabilityState.AVAILABLE else CapabilityState.UNAVAILABLE,
            value = if (hasTel) "Supported" else "Unsupported",
            source = "PackageManager.FEATURE_TELEPHONY + TelephonyManager",
            confidence = "high",
            explanation = if (hasTel) "Telephony capability is reported by Android."
            else "No telephony feature was reported (e.g. Wi-Fi-only tablet)."
        )

        // Biometrics
        out += biometricResult(pm)

        // Call recording: deliberately NOT_EXPOSED per PRD §3
        out += CapabilityResult(
            id = "call_recording", name = "Call recording",
            state = CapabilityState.NOT_EXPOSED,
            value = "Not directly exposed",
            source = "No universal API",
            confidence = "low",
            explanation = "Android does not provide a universal API that lets DeviceLens reliably declare recording support across OEMs and regions."
        )

        // Bluetooth audio: show adapter availability, not a universal supported flag (PRD §3)
        out += CapabilityResult(
            id = "bluetooth_audio", name = "Bluetooth audio",
            state = if (btAvailable) CapabilityState.AVAILABLE else CapabilityState.UNAVAILABLE,
            value = if (btAvailable) "Adapter available" else "Unavailable",
            source = "BluetoothAdapter",
            confidence = "medium",
            explanation = "Profile/service support varies; DeviceLens reports adapter availability rather than a universal audio flag."
        )

        return out
    }

    private fun biometricResult(pm: PackageManager): CapabilityResult {
        val hasFingerprint = pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        val hasFace = try {
            pm.hasSystemFeature("android.hardware.biometrics.face")
        } catch (_: Exception) { false }
        val hasIris = try {
            pm.hasSystemFeature("android.hardware.biometrics.iris")
        } catch (_: Exception) { false }
        val anyHardware = hasFingerprint || hasFace || hasIris
        // Runtime enroll state requires BiometricManager.canAuthenticate — surface as value, not as hardware fact.
        val enrolled = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val bmm = context.getSystemService(BiometricManager::class.java)
                bmm?.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            } else {
                val fm = context.getSystemService(Context.FINGERPRINT_SERVICE) as? FingerprintManager
                @Suppress("DEPRECATION")
                if (fm?.isHardwareDetected == true) 0 else -1
            }
        }.getOrNull()
        val enrolledLabel = when (enrolled) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Enrolled"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometrics enrolled"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No hardware"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Hardware unavailable"
            else -> null
        }
        return CapabilityResult(
            id = "biometric", name = "Biometric",
            state = if (anyHardware) CapabilityState.AVAILABLE else CapabilityState.UNAVAILABLE,
            value = enrolledLabel,
            source = "PackageManager biometric features + BiometricManager",
            confidence = "high",
            explanation = if (anyHardware) "Biometric hardware is reported by Android."
            else "No biometric hardware feature was reported by Android."
        )
    }
}
