# 口罩資訊地圖

透過地圖呈現週遭的特約藥局，並顯示即時的口罩數量，也提供關鍵字搜尋，方便民眾查詢相關資訊。

採用健康保險開放資料服務：
健保特約機構口罩剩餘數量明細清單之資料集。

## Download
https://play.google.com/store/apps/details?id=com.jarvislin.drugstores

## Changelog
* 1.2.1: fix GPS issue
* 1.2.0: feature of note, DB migration
* 1.1.4: using defensice programming to handle remote data resource
* 1.1.3: hotfix wrong proguard config
* 1.1.2: handle root causes of CSV ParseError
* 1.1.1: hotfix CSV ParseError
* 1.1.0: feature of Search
* 1.0.0: feature of Mask map

## Install
* `git clone git@github.com:jarvislin/drugstores.git`
* Replace Google Map API key in `values/google_maps_api.xml`

## Attribute
UI from [Penny Yang](https://challenge.thef2e.com/user/3405?schedule=4432#works-4432).

Icons made by [Freepik](https://www.flaticon.com/authors/freepik) from https://www.flaticon.com/

## Reference
https://github.com/kiang/pharmacies
