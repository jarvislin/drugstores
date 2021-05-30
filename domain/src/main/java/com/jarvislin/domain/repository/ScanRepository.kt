package com.jarvislin.domain.repository


interface ScanRepository {
    fun saveWebDialogSetting(enabled: Boolean)
    fun saveSmsDialogSetting(enabled: Boolean)
    fun isEnabledWebDialog(): Boolean
    fun isEnabledSmsDialog(): Boolean
}