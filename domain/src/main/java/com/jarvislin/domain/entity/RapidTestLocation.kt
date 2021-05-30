package com.jarvislin.domain.entity


class RapidTestLocation(
//    @SerializedName("縣市區域")
    val city: String,
//    @SerializedName("站名")
    val name: String?,
//    @SerializedName("已停止營運")
    val suspended: Boolean,
//    @SerializedName("不確定是否有快篩服務")
    val unchecked: Boolean,
//    @SerializedName("官方資料更新日期")
    val updatedTime: String?,
//    @SerializedName("資料來源")
    val dataSourceUrl: String?,
//    @SerializedName("服務醫院")
    val hospitalName: String?,
//    @SerializedName("服務對象或區域")
    val limit: String?,
//    @SerializedName("每日人數")
    val quotaOfPeople: String?,
//    @SerializedName("服務時間")
    val openingHours: String?,
//    @SerializedName("排隊或預約方法")
    val method: String?,
//    @SerializedName("掛號網址")
    val reservationUrl: String?,
//    @SerializedName("戶外地點描述")
    val locationDescription: String?,
//    @SerializedName("地址")
    val address: String?,
//    @SerializedName("聯絡電話")
    val phone: String?,
//    @SerializedName("備註")
    val note: String?,
//    @SerializedName("latitude")
    val latitude: Double?,
//    @SerializedName("longitude")
    val longitude: Double?,
) {
    fun hasLocation() = latitude != null && longitude != null
    fun getLocation() = Pair(latitude!!, longitude!!)
    fun getShareText(): String {
        return getShareText("站名", name) +
                getShareText("資格", limit) +
                getShareText("每日人數", quotaOfPeople) +
                getShareText("服務時間", openingHours) +
                getShareText("排隊或預約方法", method) +
                getShareText("地址", address) +
                getShareText("地點", locationDescription) +
                getShareText("服務醫院", hospitalName) +
                getShareText("預約網址", reservationUrl) +
                getShareText("電話", phone) +
                getShareText("備註", note) +
                "更多內容請參考防疫資訊 App：https://play.google.com/store/apps/details?id=com.jarvislin.drugstores"
    }

    private fun getShareText(title: String, data: String?): String {
        return if (data == null) ""
        else "$title：$data\n"
    }
}