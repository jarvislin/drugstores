package com.jarvislin.drugstores.repository

import com.jarvislin.domain.repository.ScanRepository
import com.jarvislin.drugstores.data.LocalData

class ScanRepositoryImpl(private val localData: LocalData) : ScanRepository {
    override fun saveWebDialogSetting(enabled: Boolean) {
        localData.isEnabledWebDialog = enabled
    }

    override fun saveSmsDialogSetting(enabled: Boolean) {
        localData.isEnabledSmsDialog = enabled
    }

    override fun isEnabledWebDialog(): Boolean {
        return localData.isEnabledWebDialog
    }

    override fun isEnabledSmsDialog(): Boolean {
        return localData.isEnabledSmsDialog
    }
}