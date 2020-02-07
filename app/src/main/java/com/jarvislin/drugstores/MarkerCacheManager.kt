package com.jarvislin.drugstores

import androidx.collection.LruCache
import com.google.android.gms.maps.model.Marker
import com.jarvislin.domain.entity.Drugstore

class MarkerCacheManager {

    companion object {
        const val MAX_MARKER_AMOUNT = 200
        private const val SIZE = 1
    }

    private val storeSet = mutableSetOf<Drugstore>()
    private val cache = object : LruCache<Drugstore, Marker>(MAX_MARKER_AMOUNT) {

        override fun sizeOf(key: Drugstore, value: Marker): Int = SIZE

        override fun entryRemoved(
            evicted: Boolean,
            key: Drugstore,
            oldValue: Marker,
            newValue: Marker?
        ) {
            storeSet.remove(key)
            oldValue.remove()
            super.entryRemoved(evicted, key, oldValue, newValue)
        }
    }

    fun isCached(drugstore: Drugstore): Boolean {
        return cache[drugstore] != null
    }

    fun add(drugstore: Drugstore, marker: Marker) {
        cache.put(drugstore, marker)
        storeSet.add(drugstore)
    }

    fun getDrugstore(marker: Marker): Drugstore {
        return storeSet.first { it.id == marker.snippet }
    }
}