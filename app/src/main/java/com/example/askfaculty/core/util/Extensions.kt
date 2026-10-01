package com.example.askfaculty.core.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Timestamp?.toRelativeTime(): String {
    if (this == null) return ""
    val now = System.currentTimeMillis()
    val diff = now - this.toDate().time
    return when {
        diff < TimeUnit.MINUTES.toMillis(1)  -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1)    -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} min ago"
        diff < TimeUnit.HOURS.toMillis(24)   -> "${TimeUnit.MILLISECONDS.toHours(diff)} hr ago"
        diff < TimeUnit.DAYS.toMillis(7)     -> "${TimeUnit.MILLISECONDS.toDays(diff)} days ago"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(this.toDate())
    }
}

fun Timestamp.toDateString(): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(this.toDate())

fun Timestamp.toTimeString(): String =
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(this.toDate())

fun Timestamp.toDateTimeString(): String =
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(this.toDate())

fun Timestamp.isToday(): Boolean {
    val cal1 = java.util.Calendar.getInstance().apply { time = this@isToday.toDate() }
    val cal2 = java.util.Calendar.getInstance()
    return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
            cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
}

fun Timestamp.isYesterday(): Boolean {
    val cal1 = java.util.Calendar.getInstance().apply { time = this@isYesterday.toDate() }
    val cal2 = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
    return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
            cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
}

fun greetingForTime(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else      -> "Good evening"
    }
}
