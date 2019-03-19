package com.wildan.basajanmessenger

import android.annotation.SuppressLint
import android.app.Application

@SuppressLint("Registered")
@Suppress("NAME_SHADOWING")
class GetTimeAgo : Application(){

    private val SECOND_MILLIS = 1000
    private val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private val DAY_MILLIS = 24 * HOUR_MILLIS


    fun getTimeAgo(time: Long): String? {
        var time = time
        if (time < 1000000000000L) {
            time *= 1000
        }

        val now = System.currentTimeMillis()
        if (time > now || time <= 0) {
            return null
        }

        // TODO: localize
        val diff = now - time
        return when {
            diff < MINUTE_MILLIS -> "baru saja"
            diff < 2 * MINUTE_MILLIS -> "beberapa menit yang lalu"
            diff < 50 * MINUTE_MILLIS -> "${diff / MINUTE_MILLIS}  menit yang lalu"
            diff < 90 * MINUTE_MILLIS -> "satu jam yang lalu"
            diff < 24 * HOUR_MILLIS -> "${diff / HOUR_MILLIS} jam yang lalu"
            diff < 48 * HOUR_MILLIS -> "kemarin"
            else -> "${diff / DAY_MILLIS} hari yang lalu"
        }
    }
}