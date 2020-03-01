# 口罩資訊地圖

透過地圖呈現週遭的特約藥局，並顯示即時的口罩數量，也提供關鍵字搜尋。

採用健康保險開放資料服務：
健保特約機構口罩剩餘數量明細清單之資料集。

![screenshot](https://github.com/jarvislin/drugstores/blob/master/art/2.png)


## Download
https://play.google.com/store/apps/details?id=com.jarvislin.drugstores

## Changelog
* 1.3.0: feature of Report
* 1.2.1: fix GPS issue
* 1.2.0: feature of note, DB migration
* 1.1.4: using defensive programming to handle remote data resource
* 1.1.3: hotfix wrong proguard config
* 1.1.2: handle root causes of CSV ParseError
* 1.1.1: hotfix CSV ParseError
* 1.1.0: feature of Search
* 1.0.0: feature of Mask map

## Install
* `git clone git@github.com:jarvislin/drugstores.git`
* Replace Google Map API key in `values/google_maps_api.xml`
* Enable Anonymous login and Firestore in Firebase console, replace thie config `google-services.json` with yours.

## License
Apache License 2.0 - UI design and icons are not included.

## Attribute
UI from [Penny Yang](https://challenge.thef2e.com/user/3405?schedule=4432#works-4432).

Some icons made by [Freepik](https://www.flaticon.com/authors/freepik) from https://www.flaticon.com/

## Reference
https://github.com/kiang/pharmacies
