[![Build Status](https://travis-ci.org/jarvislin/drugstores.svg?branch=master)](https://travis-ci.org/jarvislin/drugstores)
[![Coverage Status](https://coveralls.io/repos/github/jarvislin/drugstores/badge.svg?branch=master)](https://coveralls.io/github/jarvislin/drugstores?branch=master)

# 口罩資訊地圖

透過地圖呈現週遭的特約藥局，並顯示即時的口罩數量，也提供關鍵字搜尋。

採用健康保險開放資料服務：
健保特約機構口罩剩餘數量明細清單之資料集。

![screenshot](https://github.com/jarvislin/drugstores/blob/master/art/2.png)


## Download
https://play.google.com/store/apps/details?id=com.jarvislin.drugstores

## Changelog
* 1.7.2 refactor: adjust UX of showing progress / markers
* 1.7.1 refactor: remove chart of mask ammount history
* 1.7.0 feature: News published by Centers for Disease Control
* 1.6.2 feature: integrate FCM, let users report number tickect multiple times
* 1.6.1 feature: Q & A
* 1.6.0 feature: Proclamations announced by government
* 1.5.3 feature: showing distance bwtween user and pharmacies
* 1.5.2 fix: GPS permission
* 1.5.1 refactor: adjust UI / UX
* 1.5.0 feature: Mask amoumt's records
* 1.4.0 feature: Opening hours of drugstores
* 1.3.0 feature: Report
* 1.2.1 fix: GPS issue
* 1.2.0 feature of note, DB migration
* 1.1.4 fix: using defensive programming to handle remote data resource
* 1.1.3 fix: wrong proguard config
* 1.1.2 fix: handle root causes of CSV ParseError
* 1.1.1 fix: CSV ParseError
* 1.1.0 feature: Search
* 1.0.0 feature: Mask map

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
