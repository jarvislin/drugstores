package com.jarvislin.drugstores.widget

import android.graphics.drawable.Drawable
import android.location.Location
import androidx.core.content.ContextCompat
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.entity.MaskStatus
import com.jarvislin.domain.entity.Status
import com.jarvislin.drugstores.R
import com.jarvislin.drugstores.base.App
import com.jarvislin.drugstores.page.map.toLatLng
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round


class ModelConverter {
    fun from(info: DrugstoreInfo) = InfoConverter(info)
    fun from(status: MaskStatus) = MaskStatusConverter(status)
}

class InfoConverter(private val info: DrugstoreInfo) {
    fun toAdultMaskAmountWording() = "成人 " + info.adultMaskAmount.toString()
    fun toChildMaskAmountWording() = "兒童 " + info.childMaskAmount.toString()
    fun toAdultMaskBackground() = toMaskBackground(info.adultMaskAmount)
    fun toChildMaskBackground() = toMaskBackground(info.childMaskAmount)

    private fun toMaskBackground(amount: Int): Drawable? {
        return ContextCompat.getDrawable(
            App.instance(), when (amount) {
                0 -> R.drawable.background_empty
                in 1..20 -> R.drawable.background_warning
                else -> R.drawable.background_sufficient
            }
        )
    }

    fun toUpdateWording(): String {
        val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        return try {
            val current = System.currentTimeMillis()
            val date = format.parse(info.updateAt)
            val diffHour = (current - date.time) / 60 / 60 / 1000
            val diffMinute = (current - date.time) / 60 / 1000
            val diffSecond = (current - date.time) / 1000
            when {
                diffHour > 0 -> "資料更新於 $diffHour 小時前"
                diffMinute > 0 -> "資料更新於 $diffMinute 分鐘前"
                else -> "資料更新於 $diffSecond 秒前"
            }
        } catch (ex: Exception) {
            if (info.updateAt.isEmpty()) {
                "無更新時間"
            } else {
                "更新於：$this"
            }
        }
    }

    fun toOpenTime(): List<Triple<Boolean, Boolean, Boolean>> {
        val mornings = CharArray(7)
        val afternoons = CharArray(7)
        val nights = CharArray(7)
        info.available.toCharArray(mornings, 0, 0, 7)
        info.available.toCharArray(afternoons, 0, 7, 14)
        info.available.toCharArray(nights, 0, 14, 21)
        return mornings.mapIndexed { index: Int, morning: Char ->
            Triple(
                morning == 'N',
                afternoons[index] == 'N',
                nights[index] == 'N'
            )
        }
    }

    fun toShareContentText(): String {
        val wording = if (info.note.isEmpty()) {
            ""
        } else {
            info.note + "，"
        }
        return "${info.name}位於${info.address}，" +
                wording +
                "成人口罩數量為：${info.adultMaskAmount}個，" +
                "兒童口罩數量為：${info.childMaskAmount}個，" +
                "口罩數量更新時間為：${info.updateAt}，" +
                "更多資訊請參考口罩資訊地圖：https://play.google.com/store/apps/details?id=com.jarvislin.drugstores"
    }

    fun toDistance(location: Location?): String {
        location?.toLatLng()?.let {
            val result = FloatArray(1)
            Location.distanceBetween(
                it.latitude, it.longitude,
                info.lat, info.lng,
                result
            )
            return when (val meter = result.first()) {
                in 0f..50f -> "約 50 公尺內"
                in 51f..999f -> "約 ${meter.toInt()} 公尺"
                in 1000f..30000f -> "約 ${(meter/1000).round(1)} 公里"
                else -> "超過 30 公里"
            }
        }
        return ""
    }

    companion object {

        fun toDayOfWeek(index: Int): String {
            return when (index) {
                0 -> "週一"
                1 -> "週二"
                2 -> "週三"
                3 -> "週四"
                4 -> "週五"
                5 -> "週六"
                else -> "週日"
            }
        }
    }
}

class MaskStatusConverter(private val maskStatus: MaskStatus) {
    fun toReportWording(): String {
        val current = System.currentTimeMillis()
        val diffHour = (current - maskStatus.date.time) / 60 / 60 / 1000
        val diffMinute = (current - maskStatus.date.time) / 60 / 1000
        val diffSecond = (current - maskStatus.date.time) / 1000

        return when {
            diffHour > 0 -> "回報於 $diffHour 小時前"
            diffMinute > 0 -> "回報於 $diffMinute 分鐘前"
            else -> "回報於 $diffSecond 秒前"
        }
    }

    fun toAmountWording(): String {
        val text = when (maskStatus.status) {
            Status.Empty -> App.instance().getString(R.string.option_empty)
            Status.Warning -> App.instance().getString(R.string.option_warning)
            Status.Sufficient -> App.instance().getString(R.string.option_sufficient)
        }

        return "成人口罩$text。"
    }
}

fun Float.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}