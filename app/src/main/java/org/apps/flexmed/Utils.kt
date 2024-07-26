package org.apps.flexmed

import java.text.SimpleDateFormat
import java.util.Date

object Utils {
    fun convertTimestampToRealTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        val date = Date(timestamp)
        return sdf.format(date)
    }
}