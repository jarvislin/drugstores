package com.jarvislin.domain.interactor

import com.jarvislin.domain.repository.ScanRepository

class ScanUseCase(private val repository: ScanRepository) {
    fun saveWebDialogSetting(enabled: Boolean) {
        repository.saveWebDialogSetting(enabled)
    }

    fun saveSmsDialogSetting(enabled: Boolean) {
        repository.saveSmsDialogSetting(enabled)
    }

    fun isEnabledWebDialog() = repository.isEnabledWebDialog()
    fun isEnabledSmsDialog() = repository.isEnabledSmsDialog()
}