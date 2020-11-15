package com.jarvislin.domain.entity

sealed class UpdateProgress
object StartDownloading : UpdateProgress()
object LatestDataDownloaded : UpdateProgress()
object OldDataDeleted : UpdateProgress()
object LatestDataTransformed : UpdateProgress()
object LatestDataSaved : UpdateProgress()
object UpdateFailed : UpdateProgress()