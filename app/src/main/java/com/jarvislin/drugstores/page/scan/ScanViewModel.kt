package com.jarvislin.drugstores.page.scan

import com.jarvislin.domain.interactor.ScanUseCase
import com.jarvislin.drugstores.base.BaseViewModel
import org.koin.core.inject

class ScanViewModel : BaseViewModel() {
    private val useCase by inject<ScanUseCase>()
    fun saveWebDialogSetting(checked: Boolean) {
        if (checked) useCase.saveWebDialogSetting(checked.not())
    }

    fun saveSmsDialogSetting(checked: Boolean) {
        if (checked) useCase.saveSmsDialogSetting(checked.not())
    }

    fun isEnabledWebDialog(): Boolean {
        return useCase.isEnabledWebDialog()
    }

    fun isEnabledSmsDialog(): Boolean {
        return useCase.isEnabledSmsDialog()
    }
}
