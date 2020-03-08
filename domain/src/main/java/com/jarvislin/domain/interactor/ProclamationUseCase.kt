package com.jarvislin.domain.interactor

import com.jarvislin.domain.entity.Proclamation
import com.jarvislin.domain.repository.ProclamationRepository
import io.reactivex.Maybe

class ProclamationUseCase(private val repository: ProclamationRepository) {
    fun saveProclamations(json: String) {
        repository.saveProclamations(json)
    }

    fun fetchProclamations(): Maybe<Pair<List<Proclamation>, Boolean>> {
        return repository.fetchProclamation()
            .map { Pair(it, repository.hasNewProclamation(it)) }
    }
}