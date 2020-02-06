package com.jarvislin.drugstores.extension

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

fun Any.toJson(): String {
    return Gson().toJson(this)
}

fun <T> String.toObject(clazz: Class<T>): T {
    return Gson().fromJson(this, clazz)
}

inline fun <reified T> String.toList(): T {
    val gson = GsonBuilder()
        .create()
    val typeToken = object : TypeToken<T>() {}.type
    return gson.fromJson<T>(this, typeToken)
}

inline fun <reified T : Collection<Any>> T.listToJson(): String {
    val gson = GsonBuilder()
        .create()
    val typeToken = object : TypeToken<T>() {}.type
    return gson.toJson(this, typeToken)
}