package com.devicelens.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ShareHelper {
    fun shareFile(context: Context, file: File, mime: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share DeviceLens report"))
    }

    fun reportsDir(context: Context): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}

object FormatUtils {
    fun bytesToGb(bytes: Long?): String =
        if (bytes == null) "—" else "%.1f GB".format(bytes / 1_073_741_824.0)

    fun tempLabel(celsius: Float?): String =
        celsius?.let { "%.1f°C".format(it) } ?: "Not exposed"

    fun percentLabel(p: Int?): String = p?.let { "$it%" } ?: "—"
}
