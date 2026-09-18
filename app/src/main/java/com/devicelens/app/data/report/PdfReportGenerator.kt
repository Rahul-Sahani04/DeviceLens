package com.devicelens.app.data.report

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.devicelens.app.domain.model.DeviceReport
import java.io.File
import java.io.FileOutputStream
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Native PDF export via android.graphics.pdf.PdfDocument (PRD §11).
 * Sections: identity, health, capabilities, sensors, connectivity,
 * telephony, permissions, environment, limitations.
 */
object PdfReportGenerator {

    fun generate(report: DeviceReport, outFile: File) {
        val doc = PdfDocument()
        val pageWidth = 595  // A4 @72dpi
        val pageHeight = 842
        val margin = 40f
        var pageNumber = 1

        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = margin + 10f

        val titlePaint = Paint().apply { textSize = 22f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = 0xFF111111.toInt() }
        val h1 = Paint().apply { textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = 0xFF222222.toInt() }
        val body = Paint().apply { textSize = 10f; color = 0xFF333333.toInt() }
        val muted = Paint().apply { textSize = 9f; color = 0xFF777777.toInt() }

        fun newPageIfNeeded(need: Float = 60f) {
            if (y + need > pageHeight - margin) {
                doc.finishPage(page)
                pageNumber++
                page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = page.canvas
                y = margin + 10f
            }
        }

        fun drawLine(text: String, paint: Paint, indent: Float = 0f, gap: Float = 15f) {
            newPageIfNeeded()
            // naive truncation to page width
            var t = text
            while (paint.measureText(t) > pageWidth - 2 * margin - indent && t.length > 10) {
                t = t.dropLast(4) + "…"
                if (t.length < 12) break
            }
            canvas.drawText(t, margin + indent, y, paint)
            y += gap
        }

        val date = report.generatedAt.atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))

        drawLine("DEVICE REPORT — DeviceLens", titlePaint, gap = 26f)
        drawLine("${report.device.manufacturer} ${report.device.model} · Generated $date", muted, gap = 20f)
        drawLine("Generated locally by DeviceLens · No cloud data sent", muted, gap = 24f)

        drawLine("1. Device identity", h1, gap = 20f)
        drawLine("Manufacturer: ${report.device.manufacturer}", body)
        drawLine("Brand: ${report.device.brand} · Model: ${report.device.model}", body)
        drawLine("Codename: ${report.device.deviceCodename}", body)
        drawLine("Android ${report.device.androidVersion} · API ${report.device.apiLevel}", body)
        drawLine("Build: ${report.device.buildLabel}", body)
        drawLine("OEM skin: ${report.device.oemSkin}", body, gap = 22f)

        drawLine("2. Health summary", h1, gap = 20f)
        drawLine("Battery: ${report.health.batteryPercent?.let { "$it%" } ?: "Unknown"} · ${report.health.chargingState} · Health: ${report.health.batteryHealth}", body)
        drawLine("Battery temp: ${report.health.batteryTempCelsius?.let { "$it°C" } ?: "Not exposed"}", body)
        drawLine("RAM: total ${report.health.ramTotalGb?.let { "%.1f GB".format(it) } ?: "?"} · avail ${report.health.ramAvailableGb?.let { "%.1f GB".format(it) } ?: "?"}", body)
        report.health.storageFreeBytes?.let {
            val gb = it / 1_073_741_824.0
            drawLine("Storage free: %.1f GB".format(gb), body)
        } ?: drawLine("Storage: not exposed", body)
        drawLine("Thermal: ${report.health.thermalStatus}", body)
        drawLine("Power-save restricted: ${report.health.powerRestricted?.toString() ?: "Unknown"}", body, gap = 22f)

        drawLine("3. Hardware capabilities", h1, gap = 20f)
        report.capabilities.forEach { c ->
            drawLine("• ${c.name}: ${c.state}${c.value?.let { " ($it)" } ?: ""}", body, gap = 14f)
        }
        y += 8f

        drawLine("4. App permissions & environment", h1, gap = 20f)
        val e = report.environment
        drawLine("Camera permission: ${e.cameraPermission}", body)
        drawLine("Microphone permission: ${e.microphonePermission}", body)
        drawLine("Location permission: ${e.locationPermission}", body)
        drawLine("Nearby devices: ${e.nearbyDevicesPermission}", body)
        drawLine("Notifications enabled: ${e.notificationsEnabled}", body)
        drawLine("Battery-opt exempt: ${e.batteryOptimizationExempt?.toString() ?: "Unknown"}", body)
        drawLine("Bluetooth enabled: ${e.bluetoothEnabled?.toString() ?: "Unknown"}", body)
        drawLine("Location services on: ${e.locationEnabled}", body, gap = 22f)

        drawLine("5. Limitations / unavailable fields", h1, gap = 20f)
        report.capabilities.filter { it.state.name == "NOT_EXPOSED" || it.state.name == "UNAVAILABLE" }.forEach { c ->
            drawLine("• ${c.name}: ${c.explanation.take(110)}", muted, gap = 14f)
        }

        y += 16f
        drawLine("Generated locally by DeviceLens · No cloud data sent", muted)

        doc.finishPage(page)
        FileOutputStream(outFile).use { doc.writeTo(it) }
        doc.close()
    }
}
