package com.jarvislin.drugstores.page.proclamation

import com.jarvislin.domain.interactor.ProclamationUseCase
import com.jarvislin.drugstores.base.BaseViewModel
import org.koin.core.inject

class ProclamationViewModel : BaseViewModel() {
    private val useCase by inject<ProclamationUseCase>()

    fun saveProclamations(json: String) {
        useCase.saveProclamations(json)
    }

}