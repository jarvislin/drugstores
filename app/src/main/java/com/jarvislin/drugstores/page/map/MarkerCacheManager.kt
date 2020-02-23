package com.jarvislin.drugstores.page.map

import androidx.collection.LruCache
import com.google.android.gms.maps.model.Marker
import com.jarvislin.domain.entity.DrugstoreInfo

class MarkerCacheManager {

    companion object {
        const val MAX_MARKER_AMOUNT = 200
        private const val SIZE = 1
    }

    private val storeSet = mutableSetOf<DrugstoreInfo>()
    private val cache = object : LruCache<DrugstoreInfo, Marker>(MAX_MARKER_AMOUNT) {

        override fun sizeOf(key: DrugstoreInfo, value: Marker): Int = SIZE

        override fun entryRemoved(
            evicted: Boolean,
            key: DrugstoreInfo,
            oldValue: Marker,
            newValue: Marker?
        ) {
            storeSet.remove(key)
            oldValue.remove()
            super.entryRemoved(evicted, key, oldValue, newValue)
        }
    }

    fun isCached(info: DrugstoreInfo): Boolean {
        return cache[info] != null
    }

    fun add(drugstore: DrugstoreInfo, marker: Marker) {
        cache.put(drugstore, marker)
        storeSet.add(drugstore)
    }

    fun getDrugstoreInfo(marker: Marker): DrugstoreInfo {
        return storeSet.first { it.id == marker.snippet }
    }
}