package com.jarvislin.domain.repository

import com.jarvislin.domain.entity.Proclamation
import io.reactivex.Maybe

interface ProclamationRepository {
    fun saveProclamations(json: String)
    fun fetchProclamation(): Maybe<List<Proclamation>>
    fun hasNewProclamation(proclamations: List<Proclamation>): Boolean

}