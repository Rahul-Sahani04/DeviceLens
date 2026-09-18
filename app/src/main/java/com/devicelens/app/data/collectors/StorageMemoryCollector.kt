package com.devicelens.app.data.collectors

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.devicelens.app.domain.model.HealthSnapshot

class StorageMemoryCollector(private val context: Context) {

    data class StorageMemory(
        val ramTotalGb: Double?,
        val ramAvailableGb: Double?,
        val storageTotalBytes: Long?,
        val storageFreeBytes: Long?,
        val storageUsedBytes: Long?
    )

    fun collect(): StorageMemory {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        var ramTotal: Double? = null
        var ramAvail: Double? = null
        try {
            am?.getMemoryInfo(memInfo)
            ramAvail = memInfo.availMem / 1_073_741_824.0
            // totalMem is 0 on some low-RAM reporting paths; guard it
            ramTotal = if (memInfo.totalMem > 0) memInfo.totalMem / 1_073_741_824.0 else null
        } catch (_: Exception) { /* leave null -> unsupported */ }

        var total: Long? = null
        var free: Long? = null
        var used: Long? = null
        try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            total = stat.blockCountLong * blockSize
            free = stat.availableBytes
            used = if (total != null && free != null) total - free else null
        } catch (_: Exception) { /* unsupported */ }

        return StorageMemory(ramTotal, ramAvail, total, free, used)
    }
}

/** Merge helper used by RunDeviceScanUseCase. */
fun com.devicelens.app.data.collectors.StorageMemoryCollector.StorageMemory.toHealthPatch(
    base: HealthSnapshot
): HealthSnapshot = base.copy(
    ramTotalGb = ramTotalGb,
    ramAvailableGb = ramAvailableGb,
    storageTotalBytes = storageTotalBytes,
    storageFreeBytes = storageFreeBytes,
    storageUsedBytes = storageUsedBytes
)
