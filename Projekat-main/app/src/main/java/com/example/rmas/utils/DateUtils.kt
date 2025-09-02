package com.example.rmas.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun formatRange(start: Long?, end: Long?): String {
    if (start == null || end == null) return "Izaberite opseg"

    val startDate = Instant.ofEpochMilli(start).asShortString()
    val endDate = Instant.ofEpochMilli(end).asShortString()

    return "Od $startDate do $endDate"
}

@RequiresApi(Build.VERSION_CODES.O)
fun Instant.asShortString(): String {
    return format("dd/MM/yyyy")
}

@RequiresApi(Build.VERSION_CODES.O)
fun Instant.format(pattern: String): String {
    return DateTimeFormatter
        .ofPattern(pattern)
        .withZone(ZoneOffset.UTC)
        .format(this)
}

