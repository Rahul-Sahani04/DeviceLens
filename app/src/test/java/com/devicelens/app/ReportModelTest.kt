package com.devicelens.app

import com.devicelens.app.domain.model.CapabilityResult
import com.devicelens.app.domain.model.CapabilityState
import com.devicelens.app.domain.model.DeviceIdentity
import com.devicelens.app.domain.model.DeviceReport
import com.devicelens.app.domain.model.EnvironmentSnapshot
import com.devicelens.app.domain.model.HealthSnapshot
import com.devicelens.app.domain.model.countByState
import com.devicelens.app.data.report.JsonReportGenerator
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ReportModelTest {

    private fun sampleReport() = DeviceReport(
        generatedAt = Instant.parse("2026-09-18T06:40:00Z"),
        device = DeviceIdentity("Google", "google", "Pixel 8", "shiba", "16", 36, "BP1A", "Stock / Unknown"),
        health = HealthSnapshot(87, "Discharging", "Good", 34.2f, 6.0, 3.1, 128L * 1_073_741_824, 118L * 1_073_741_824, 10L * 1_073_741_824, "Normal", false),
        capabilities = listOf(
            CapabilityResult("camera", "Camera", CapabilityState.AVAILABLE, "Present", "pm", "high", "ok"),
            CapabilityResult("nfc", "NFC", CapabilityState.UNAVAILABLE, "Absent", "pm", "high", "no"),
            CapabilityResult("call_recording", "Call recording", CapabilityState.NOT_EXPOSED, null, "none", "low", "n/a")
        ),
        environment = EnvironmentSnapshot("Granted", "Denied", "Denied", "Denied", true, false, true, false)
    )

    @Test
    fun counts_by_state_are_correct() {
        val caps = sampleReport().capabilities
        assertEquals(1, caps.countByState(CapabilityState.AVAILABLE))
        assertEquals(1, caps.countByState(CapabilityState.UNAVAILABLE))
        assertEquals(1, caps.countByState(CapabilityState.NOT_EXPOSED))
        assertEquals(0, caps.countByState(CapabilityState.RESTRICTED))
    }

    @Test
    fun json_export_has_schema_and_sections() {
        val json = JSONObject(JsonReportGenerator.generate(sampleReport()))
        assertEquals("1.0", json.getString("schemaVersion"))
        assertTrue(json.has("device"))
        assertTrue(json.has("health"))
        assertTrue(json.has("capabilities"))
        assertTrue(json.has("environment"))
        assertTrue(json.has("summary"))
        assertEquals(3, json.getJSONArray("capabilities").length())
        assertEquals("Pixel 8", json.getJSONObject("device").getString("model"))
    }

    @Test
    fun json_never_fakes_call_recording() {
        val json = JSONObject(JsonReportGenerator.generate(sampleReport()))
        val arr = json.getJSONArray("capabilities")
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("id") == "call_recording") {
                assertEquals("NOT_EXPOSED", o.getString("state"))
                return
            }
        }
        throw AssertionError("call_recording entry missing")
    }
}
