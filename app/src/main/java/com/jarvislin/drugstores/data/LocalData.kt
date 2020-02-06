package com.jarvislin.drugstores.data

import android.content.Context
import com.jarvislin.drugstores.data.Preferences.Companion.PREFERENCE_NAME
import java.util.*

class LocalData(private val context: Context) {
    companion object {
        private const val KEY_FAVORITE_ANIMALS = "key_favorite_animals"
    }

    var favoriteAnimals: String by preferences(KEY_FAVORITE_ANIMALS, "[]")

//    fun getAnimals(): Vector<Animal> {
//        return favoriteAnimals.jsonToList()
//    }

    private fun <T : Any> preferences(name: String, defaultValue: T) =
        DelegatesExt.preferences(name, defaultValue, context)

    fun clear() {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        preferences.edit().clear().apply()
    }
}