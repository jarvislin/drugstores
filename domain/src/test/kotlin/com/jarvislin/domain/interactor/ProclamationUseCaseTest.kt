package com.jarvislin.domain.interactor

import com.jarvislin.domain.entity.Proclamation
import com.jarvislin.domain.repository.ProclamationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Maybe
import org.junit.Before
import org.junit.Test

class ProclamationUseCaseTest {

    private lateinit var useCase: ProclamationUseCase
    private val repository = mockk<ProclamationRepository>(relaxed = true)

    @Before
    fun setUp() {
        useCase = ProclamationUseCase(repository)
    }

    @Test
    fun saveProclamations() {
        val json = "json"

        useCase.saveProclamations(json)
        verify { repository.saveProclamations(json) }
    }

    @Test
    fun fetchProclamations() {
        val proclamations : List<Proclamation> = emptyList()
        val hasNew = true
        
        every { repository.fetchProclamation() } returns Maybe.just(proclamations)
        every { repository.hasNewProclamation(proclamations) } returns hasNew

        useCase.fetchProclamations()
            .test()
            .assertValue(Pair(proclamations, hasNew))
    }
}