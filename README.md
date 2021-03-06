[![Build Status](https://travis-ci.org/jarvislin/drugstores.svg?branch=master)](https://travis-ci.org/jarvislin/drugstores)
[![Coverage Status](https://coveralls.io/repos/github/jarvislin/drugstores/badge.svg?branch=master)](https://coveralls.io/github/jarvislin/drugstores?branch=master)
![Made with love in Taiwan](https://madewithlove.now.sh/tw?heart=true&colorA=%234c4c4c&colorB=%232ea7bd)

# 防疫資訊站

提供疫情期間的相關資訊及工具
1. 確診訊息：透過疾病管制署的開放資料每日更新。
2. 實聯制掃描：提供掃碼工具，快速完成實聯登記。
3. 官方新聞稿：提供疾病管制署的新聞稿內容，了解近期疫情資訊。
4. 口罩地圖：利用地圖瀏覽附近的藥局。

- 採用健康保險開放資料：健保特約機構口罩剩餘數量明細清單
- 採用衛生福利部疾病管制署開放資料：COVID-19台灣最新病例、檢驗統計

# COVID-19 INFO APP

Provides information and tools about the COVID-19 in Taiwan.

<img src="https://github.com/jarvislin/drugstores/blob/master/art/Screenshot_20210530-150515.jpeg" width="25%" />


## Download
https://play.google.com/store/apps/details?id=com.jarvislin.drugstores

## Changelog
* 2.1.1 fix: fix the bug of scanning official QR Code
* 2.1.0 feature: translate the page of Menu into English
* 2.0.2 feature: translate the feature of Scanning QR code into English
* 2.0.1 refacotr: adjust UX of the scanner
* 2.0.0 feature: Confirmed case info, QR code scanner, rapid test loactions
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
The rapid tests information is collected by the g0v community (CC BY 4.0 g0v contributors at http://bit.ly/TaiwanRapidTests) 

UI from [Penny Yang](https://challenge.thef2e.com/user/3405?schedule=4432#works-4432).

Some icons made by [Freepik](https://www.flaticon.com/authors/freepik) from https://www.flaticon.com/

## Reference
https://github.com/kiang/pharmacies
