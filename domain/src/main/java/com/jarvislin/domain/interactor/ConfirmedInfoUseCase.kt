package com.jarvislin.domain.interactor

import com.jarvislin.domain.entity.ConfirmedCase
import com.jarvislin.domain.repository.ConfirmedInfoRepository
import io.reactivex.Single

class ConfirmedInfoUseCase(private val infoRepository: ConfirmedInfoRepository) {
    fun fetchConfirmedCase(): Single<ConfirmedCase> {
        return infoRepository.downloadConfirmedCase()
            .flatMap { infoRepository.convertFile(it.file) }
    }
}