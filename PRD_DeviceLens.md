# DeviceLens
## Product Requirements Document

**Version:** 1.0 MVP
**Platform:** Android
**Primary goal:** Create a polished device-diagnostics app that scans the phone and turns platform-level information into a readable, exportable health + capability report.
**Target demo:** 60–90 seconds from launch to a visually impressive report.

---

## 1. Product Definition

DeviceLens is a privacy-first Android diagnostic scanner. It performs a fast local scan of the device, groups the results into understandable sections, highlights unsupported or restricted capabilities, and lets the user export the result as PDF or JSON.

The product should feel closer to a premium system utility than a generic settings screen.

### Core value proposition

> “Know exactly what your Android device can do, what state it is in, and what your apps are allowed to access.”

### MVP success criteria

- Scan completes in under 2 seconds on a normal modern device.
- The first screen immediately shows device identity and a health summary.
- Capabilities are clearly separated from permissions and runtime state.
- Every result has one of four states: **Available**, **Enabled**, **Restricted**, or **Unavailable / Not exposed**.
- A report can be exported without a backend.
- The app never needs an account or cloud service.
- The UI remains useful when a permission is denied.

---

# 2. Scope

## In scope

### Device identity
- Manufacturer
- Brand
- Model
- Device codename where useful
- Android version
- API level
- Build / release label
- OEM skin / UI label when reliably exposed

### Hardware and system health
- Battery percentage
- Charging state
- Battery health when available
- Battery temperature when exposed by the platform
- RAM total and approximate available RAM
- Internal storage total / used / free
- Thermal status
- Whether the device is idle / power restricted where exposed

### Hardware capabilities
- Camera presence
- Front / rear camera count when accessible
- Microphone presence
- Bluetooth support
- Bluetooth LE support
- NFC support
- GPS / location provider availability
- Accelerometer
- Gyroscope
- Magnetometer
- Proximity sensor
- Light sensor
- Fingerprint / biometric hardware availability
- Telephony capability

### App environment
- Camera permission
- Microphone permission
- Location permission
- Nearby-device permissions
- Notifications enabled
- Battery-optimization exemption state
- Bluetooth enabled / disabled
- Location services enabled / disabled

### Reporting
- In-app report preview
- Export to PDF
- Export to JSON
- Native Android share sheet

## Explicitly out of MVP

- Root-only diagnostics
- CPU benchmark
- GPU benchmark
- NAND benchmark
- Battery cycle estimation when the device does not expose a trustworthy value
- Carrier-specific diagnostics
- Hidden OEM APIs
- Live network speed tests
- Automatic “repair” actions
- Continuous background monitoring

---

# 3. Important Data-Accuracy Rules

DeviceLens must not present an inference as a hardware fact.

For example:

- **“Gyroscope: Available”** means Android exposes a gyroscope sensor to the app.
- **“Microphone permission: Granted”** means the app has the relevant runtime permission. It does not guarantee that the microphone is physically usable at every moment.
- **“GPS”** should be represented as **Location / GNSS availability**, distinguishing hardware/provider capability from whether location services are currently switched on.
- **Bluetooth audio** should not be shown as a universal “supported / unsupported” hardware flag. Prefer profile/service availability where observable, otherwise show **Bluetooth LE / Bluetooth adapter available**.

This distinction is a key part of the product’s credibility.

---

# 4. Primary User Flow

```text
Launch
  ↓
Fast device scan
  ↓
Device overview
  ↓
Health + capabilities cards
  ↓
Tap section for details
  ↓
Generate Device Report
  ↓
Preview
  ↓
Export PDF / JSON / Share
```

### Scan behavior

The scan should feel active, but not fake.

Sequence:

1. Read static device/build information.
2. Read battery and thermal state.
3. Read storage and memory.
4. Enumerate supported system features.
5. Enumerate sensors.
6. Read permission and environment state.
7. Build a normalized `DeviceReport` object.
8. Render the dashboard.

The progress animation is visual feedback only. Each section should complete as its real data becomes available.

---

# 5. Screen Requirements

## Screen 01 — Launch / Scan

### Layout

Top:
- DeviceLens logo
- Small status label: `LOCAL DEVICE SCAN`

Center:
- Large circular scan indicator
- Device model beneath it
- Status text changing through:
  - Reading device identity
  - Checking hardware
  - Reading system state
  - Checking permissions
  - Preparing report

Bottom:
- `SCANNING DEVICE...`

### Motion

- Circular progress ring with subtle pulse.
- Section labels fade in as each subsystem completes.
- No flashy particle effects. The app should feel precise.

---

## Screen 02 — Dashboard

### Header

```text
DEVICELENS

Galaxy F06 5G
Android 16 · API 36
One UI 8
```

Use a compact device avatar / phone silhouette.

### Hero card — Device Health

```text
DEVICE HEALTH

Battery         87%
Storage         118 GB free
RAM              6 GB
Temperature     34.2°C
Thermal         Normal
```

Each metric gets a tiny status indicator.

### Capability grid

Two-column cards:

```text
✓ Camera
✓ Microphone
✓ Bluetooth
✓ NFC
✓ Location
✓ Gyroscope
✓ Accelerometer
✓ Telephony
```

Tap opens a detail sheet.

### Environment card

```text
APP ENVIRONMENT

Battery optimization    ON
Notifications           ON
Microphone              ON
Camera                   ON
Location                 OFF
Bluetooth                ON
```

### Primary CTA

`GENERATE DEVICE REPORT`

Secondary action:

`VIEW RAW DATA`

---

## Screen 03 — Capability Detail

Example:

```text
GYROSCOPE

AVAILABLE

Type       Rotation Vector / Gyroscope
Vendor     <vendor when available>
Power      <value when exposed>
Resolution <value when exposed>

Sensor is exposed by Android and available to applications.
```

For unavailable items:

```text
NFC

NOT AVAILABLE

No NFC system feature was reported by Android on this device.
```

---

## Screen 04 — Report Preview

A polished document-like view:

```text
DEVICE REPORT

Galaxy F06 5G
Generated 18 Sep 2026

HEALTH
███████████████░░░ 87

CAPABILITIES
12 available · 2 restricted · 1 unavailable

ENVIRONMENT
6 enabled · 1 disabled

SYSTEM
Android 16 · API 36

[ EXPORT PDF ]   [ JSON ]
```

---

# 6. Visual Design System

## Theme: “Instrument Panel”

The visual language should feel like a mix of:
- premium Android system diagnostics
- aviation / engineering instrumentation
- modern developer tooling

Avoid generic Material cards everywhere. Use a small number of strong surfaces.

### Color tokens

| Token | Hex | Use |
|---|---|---|
| Background | `#090B0F` | App background |
| Surface | `#11151B` | Main cards |
| Surface Raised | `#171C23` | Detail / modal surfaces |
| Primary | `#7CFFB2` | Success / available |
| Accent | `#57C7FF` | Interactive / scan |
| Warning | `#FFCB6B` | Restricted / attention |
| Danger | `#FF6B7A` | Critical |
| Text Primary | `#F4F7FA` | Main text |
| Text Secondary | `#8C97A6` | Metadata |
| Divider | `#242A33` | Borders |

### Typography

Preferred:
- Display: `Space Grotesk` or `Manrope`
- Body: `Inter` or Android system sans
- Numeric metrics: `JetBrains Mono`

Numbers should use a monospaced face to create the instrument-panel feel.

### Components

- 16–20dp corner radius
- 1dp subtle borders
- Minimal shadows
- Dense but breathable spacing
- Small uppercase section labels
- Icon + status + metric pattern

### Status iconography

Use a consistent state vocabulary:

- `✓` Available / enabled
- `!` Restricted / attention
- `×` Unavailable
- `?` Not exposed

Do not use green/red as the only indication; always pair color with text/icon.

---

# 7. Motion Design

### Scan

- 1.2–1.6s scan loop
- Animated ring
- Section completion tick

### Dashboard

- Cards stagger in over 250–400ms
- Numbers count up once
- No perpetual animation

### Detail sheet

- Bottom-sheet slide + 180ms fade

### Export

- CTA morphs into a progress state:

`GENERATE REPORT` → `GENERATING...` → `REPORT READY`

---

# 8. Technical Architecture

## Suggested stack

- Kotlin
- Jetpack Compose
- Material 3 as a base, with custom tokens/components
- Coroutines
- ViewModel
- AndroidX Activity / Lifecycle
- `PdfDocument` for native PDF generation

### Suggested modules

```text
app/
 ├── ui/
 │    ├── scan/
 │    ├── dashboard/
 │    ├── detail/
 │    └── report/
 ├── domain/
 │    ├── model/
 │    └── usecase/
 ├── data/
 │    ├── collectors/
 │    │    ├── DeviceInfoCollector
 │    │    ├── BatteryCollector
 │    │    ├── StorageCollector
 │    │    ├── SensorCollector
 │    │    ├── CapabilityCollector
 │    │    └── PermissionCollector
 │    └── report/
 └── util/
```

### Normalized model

```kotlin
data class DeviceReport(
    val generatedAt: Instant,
    val device: DeviceIdentity,
    val health: HealthSnapshot,
    val capabilities: List<CapabilityResult>,
    val environment: EnvironmentSnapshot
)
```

Each `CapabilityResult` should carry:

```text
id
name
state
value
source
confidence
explanation
```

The UI must render from the normalized model instead of querying Android APIs directly.

---

# 9. Android API Mapping

| Data | Suggested source |
|---|---|
| Device identity | `Build`, `Build.VERSION` |
| RAM | `ActivityManager.MemoryInfo` |
| Storage | `StatFs` / storage APIs |
| Battery | `BatteryManager`, `ACTION_BATTERY_CHANGED` |
| Battery health | `BatteryManager.EXTRA_HEALTH` |
| Battery temperature | `BatteryManager.EXTRA_TEMPERATURE` when exposed |
| Thermal state | `PowerManager` thermal status |
| Hardware features | `PackageManager.hasSystemFeature()` |
| Sensors | `SensorManager` |
| Bluetooth state | `BluetoothAdapter` |
| NFC | `PackageManager.FEATURE_NFC` / `NfcAdapter` |
| Location/GNSS | `LocationManager` + package/system feature checks |
| Telephony | `TelephonyManager` + `PackageManager.FEATURE_TELEPHONY` |
| Runtime permissions | `checkSelfPermission()` |
| Notifications | `NotificationManager` / notification-enabled state |
| Battery optimization | `PowerManager.isIgnoringBatteryOptimizations()` where applicable |
| PDF | `android.graphics.pdf.PdfDocument` |

Android's BatteryManager exposes level, charging state, health, cycle count on supported API levels, voltage, and temperature. Android's PowerManager exposes standardized thermal states. citeturn623999search2turn623999search5

Android exposes system features through PackageManager and sensor availability through SensorManager. citeturn601676search2turn623999search6

Native PDF creation is supported by Android's `PdfDocument`. citeturn623999search3

---

# 10. Permissions Strategy

Ask only when a feature genuinely requires a runtime permission.

Prefer:

```text
Device scan
  ↓
Read-only checks first
  ↓
Only request permission for data the user explicitly asks to inspect
```

For the demo, the app can show permission state without requiring every permission to be granted.

Example:

```text
MICROPHONE
Permission: GRANTED
Hardware: AVAILABLE

CAMERA
Permission: DENIED
Hardware: AVAILABLE
```

This is more useful than forcing a permission wall on launch.

---

# 11. Report Format

## PDF

Sections:

1. Device identity
2. Health summary
3. Hardware capabilities
4. Sensor inventory
5. Connectivity
6. Telephony
7. App permissions
8. System environment
9. Limitations / unavailable fields

Footer:

`Generated locally by DeviceLens · No cloud data sent`

## JSON

Machine-readable structure:

```json
{
  "schemaVersion": "1.0",
  "generatedAt": "2026-09-18T06:40:00Z",
  "device": {},
  "health": {},
  "capabilities": [],
  "environment": {}
}
```

---

# 12. Demo Script

### 45–60 second client demo

1. Open DeviceLens.
2. Let the real scan complete.
3. Show device model + Android version.
4. Point to live battery / RAM / storage / temperature.
5. Open the sensor grid.
6. Show one available capability and one restricted capability.
7. Tap **Generate Device Report**.
8. Open the PDF preview.
9. Share the report using Android's share sheet.

The story is simple: **launch → inspect → explain → export.**

---

# 13. MVP Delivery Plan

### 4–6 hour implementation target

**Hour 1:** Project setup, Compose theme, navigation, data model.

**Hour 2:** Device identity + health collectors.

**Hour 3:** Capabilities + sensors + environment state.

**Hour 4:** Dashboard polish + detail sheets.

**Hour 5:** PDF / JSON export + share sheet.

**Hour 6:** Device testing, edge cases, animations, screenshots.

---

# 14. Non-Functional Requirements

- Offline-first
- No account
- No backend
- No analytics in MVP
- No personal data collection
- Scan failures must degrade gracefully
- API-specific fields must include `unsupported` rather than defaulting to fake values
- Works in dark mode as the primary visual mode
- Handles small and large phone screens

---

# 15. Portfolio Positioning

DeviceLens demonstrates:

- Native Android development
- Kotlin + Jetpack Compose
- Android framework APIs
- Runtime permissions
- Hardware abstraction
- Sensor enumeration
- System-state inspection
- PDF generation
- Structured data modeling
- UX for technical information

The strongest portfolio angle is not “I made a phone info app.” It is:

> “I built a native Android diagnostics system that turns fragmented platform APIs into a structured device report.”

---

# 16. Technical References

- Android 16 APIs: https://developer.android.com/about/versions/16/features
- BatteryManager: https://developer.android.com/reference/kotlin/android/os/BatteryManager
- PowerManager: https://developer.android.com/reference/kotlin/android/os/PowerManager
- SensorManager: https://developer.android.com/reference/android/hardware/SensorManager
- PackageManager: https://developer.android.com/reference/android/content/pm/PackageManager
- PDF APIs: https://developer.android.com/reference/android/graphics/pdf/package-summary
- Shared document storage: https://developer.android.com/training/data-storage/shared/documents-files
