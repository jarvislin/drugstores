package com.jarvislin.drugstores.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ApiRapidTestLocation(
    @SerializedName("type")
    val type: String,
    @SerializedName("features")
    val features: List<ApiRapidTestFeature>
) : Serializable

class ApiRapidTestFeature(
    @SerializedName("properties")
    val property: ApiRapidTestFeatureProperty,
) : Serializable

class ApiRapidTestFeatureProperty(
    @SerializedName("縣市區域")
    val city: String,
    @SerializedName("站名")
    val name: String,
    @SerializedName("已停止營運")
    val suspended: String,
    @SerializedName("不確定是否有快篩服務")
    val unchecked: String,
    @SerializedName("官方資料更新日期")
    val updatedTime: String,
    @SerializedName("資料來源")
    val source: String,
    @SerializedName("服務醫院")
    val hospitalName: String,
    @SerializedName("服務對象或區域")
    val limit: String,
    @SerializedName("每日人數")
    val quotaOfPeople: String,
    @SerializedName("服務時間")
    val openingHours: String,
    @SerializedName("排隊或預約方法")
    val method: String,
    @SerializedName("掛號網址")
    val reservationWebsite: String,
    @SerializedName("戶外地點描述")
    val areaDescription: String,
    @SerializedName("地址")
    val address: String,
    @SerializedName("聯絡電話")
    val phone: String,
    @SerializedName("備註")
    val note: String,
    @SerializedName("latitude")
    val latitude: String,
    @SerializedName("longitude")
    val longitude: String,
) : Serializable