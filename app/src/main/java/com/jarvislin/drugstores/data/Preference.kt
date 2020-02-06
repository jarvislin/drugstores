package com.jarvislin.drugstores.data


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class Preferences<T>(
    private val name: String,
    private val defaultValue: T,
    private val context: Context
) : ReadWriteProperty<Any?, T> {

    companion object {
        const val PREFERENCE_NAME = "preference"
    }

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getPreferences(name, defaultValue)
    }


    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putPreferences(name, value)
    }

    private fun <T> getPreferences(name: String, defaultValue: T): T = with(preferences) {

        val res: Any = when (defaultValue) {
            is Long -> getLong(name, defaultValue)
            is String -> getString(name, defaultValue)!! // because it has default value
            is Int -> getInt(name, defaultValue)
            is Boolean -> getBoolean(name, defaultValue)
            is Float -> getFloat(name, defaultValue)
            else -> throw IllegalArgumentException("Preferences not support this type")
        }

        return res as T
    }

    @SuppressLint("CommitPrefEdits")
    private fun <U> putPreferences(name: String, value: U) = with(preferences.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("Preferences not support this type")
        }.apply()
    }
}

object DelegatesExt {
    fun <T : Any> preferences(
        name: String,
        defaultValue: T,
        context: Context
    ) = Preferences(name, defaultValue, context)
}