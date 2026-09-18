package com.devicelens.app.data.collectors

import android.content.Context
import android.os.Build
import com.devicelens.app.domain.model.DeviceIdentity

class DeviceInfoCollector(private val context: Context) {
    fun collect(): DeviceIdentity {
        val oemSkin = detectOemSkin()
        return DeviceIdentity(
            manufacturer = Build.MANUFACTURER ?: "Unknown",
            brand = Build.BRAND ?: "Unknown",
            model = Build.MODEL ?: "Unknown",
            deviceCodename = Build.DEVICE ?: "Unknown",
            androidVersion = Build.VERSION.RELEASE ?: "Unknown",
            apiLevel = Build.VERSION.SDK_INT,
            buildLabel = Build.DISPLAY ?: "Unknown",
            oemSkin = oemSkin
        )
    }

    /** Best-effort OEM skin label; returns "Stock / Unknown" when not reliably exposed. */
    private fun detectOemSkin(): String {
        // Samsung One UI exposes via ro.build.version.oneui; read via system property reflection.
        val oneUi = getSystemProperty("ro.build.version.oneui").cleanProp()
        if (!oneUi.isNullOrBlank()) return "One UI $oneUi"
        val miui = getSystemProperty("ro.miui.ui.version.name").cleanProp()
        if (!miui.isNullOrBlank()) return "MIUI $miui"
        val colorOs = getSystemProperty("ro.build.version.opporom").cleanProp()
        if (!colorOs.isNullOrBlank()) return "ColorOS $colorOs"
        val emui = getSystemProperty("ro.build.version.emui").cleanProp()
        if (!emui.isNullOrBlank()) return "EMUI $emui"
        return "Stock / Unknown"
    }

    private fun String?.cleanProp(): String? =
        this?.takeIf { it.isNotBlank() && !it.equals("unknown", ignoreCase = true) }

    private fun getSystemProperty(key: String): String? {
        return try {
            val c = Class.forName("android.os.SystemProperties")
            val get = c.getMethod("get", String::class.java, String::class.java)
            get.invoke(null, key, "unknown") as? String
        } catch (_: Exception) {
            null
        }
    }
}
