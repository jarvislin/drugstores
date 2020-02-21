package com.jarvislin.drugstores.data

import android.content.Context
import com.jarvislin.drugstores.data.Preferences.Companion.PREFERENCE_NAME
import java.util.*

class LocalData(private val context: Context) {
    companion object {
        private const val KEY_LAST_LOCATION = "key_last_location"
        private const val KEY_LAST_REPORT_TIMESTAMP = "key_last_report_timestamp"
    }

    var lastLocation: String by preferences(KEY_LAST_LOCATION, "25.0393868,121.5087163")
    var lastReportTimestamp: Long by preferences(KEY_LAST_REPORT_TIMESTAMP, 0L)


    private fun <T : Any> preferences(name: String, defaultValue: T) =
        DelegatesExt.preferences(name, defaultValue, context)

    fun clear() {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        preferences.edit().clear().apply()
    }
}