package com.jarvislin.drugstores.page.map

import androidx.collection.LruCache
import com.google.android.gms.maps.model.Marker
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.entity.EntireInfo

class MarkerCacheManager {

    companion object {
        const val MAX_MARKER_AMOUNT = 200
        private const val SIZE = 1
    }

    private val storeSet = mutableSetOf<EntireInfo>()
    private val cache = object : LruCache<EntireInfo, Marker>(MAX_MARKER_AMOUNT) {

        override fun sizeOf(key: EntireInfo, value: Marker): Int = SIZE

        override fun entryRemoved(
            evicted: Boolean,
            key: EntireInfo,
            oldValue: Marker,
            newValue: Marker?
        ) {
            storeSet.remove(key)
            oldValue.remove()
            super.entryRemoved(evicted, key, oldValue, newValue)
        }
    }

    fun isCached(info: EntireInfo): Boolean {
        return cache[info] != null
    }

    fun add(drugstore: EntireInfo, marker: Marker) {
        cache.put(drugstore, marker)
        storeSet.add(drugstore)
    }

    fun getEntireInfo(marker: Marker): EntireInfo {
        return storeSet.first { it.getId() == marker.snippet }
    }
}