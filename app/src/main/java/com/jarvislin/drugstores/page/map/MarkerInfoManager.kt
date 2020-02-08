package com.jarvislin.drugstores.page.map

import com.jarvislin.drugstores.R

object MarkerInfoManager {
    fun getMarkerInfo(maskAmount: Int): MarkerInfo {
        return when (maskAmount) {
            in 0..0 -> MarkerInfo.Empty
            in 1..20 -> MarkerInfo.Warning
            else -> MarkerInfo.Sufficient
        }
    }
}

sealed class MarkerInfo(val drawableId: Int, val zIndex: Float) {
    object Empty : MarkerInfo(R.drawable.ic_location_empty, 1f)
    object Warning : MarkerInfo(R.drawable.ic_location_warning, 200f)
    object Sufficient : MarkerInfo(R.drawable.ic_location_sufficient, 3000f)
    object Favorite : MarkerInfo(R.drawable.ic_location_favorite, 40000f)
}