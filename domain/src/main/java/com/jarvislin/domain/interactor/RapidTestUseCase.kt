package com.jarvislin.domain.interactor

import com.jarvislin.domain.entity.RapidTestLocation
import com.jarvislin.domain.repository.RapidTestRepository
import io.reactivex.Single

class RapidTestUseCase(private val repository: RapidTestRepository) {
    fun fetchRapidTestLocations(): Single<List<RapidTestLocation>> {
        return repository.downloadData()
            .flatMap { repository.convert(it) }
            .map { it.filter { it.suspended.not() } }
    }
}