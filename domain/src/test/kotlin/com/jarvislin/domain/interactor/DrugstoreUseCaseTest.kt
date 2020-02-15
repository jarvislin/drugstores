package com.jarvislin.domain.interactor

import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.repository.DrugstoreRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import java.io.File

class DrugstoreUseCaseTest {

    private lateinit var useCase: DrugstoreUseCase
    private val drugstoreRepository = mockk<DrugstoreRepository>(relaxed = true)

    @Before
    fun setUp() {
        useCase = DrugstoreUseCase(drugstoreRepository)
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun handleLatestData() {
        val file = File("")
        val result = emptyList<DrugstoreInfo>()

        every { drugstoreRepository.deleteDrugstoreInfo() } returns Single.just(-1)
        every { drugstoreRepository.transformToDrugstoreInfo(file) } returns Single.just(result)
        every { drugstoreRepository.saveDrugstoreInfo(result) } returns Completable.complete()

        useCase.handleLatestData(file)
            .test()
            .assertComplete()
    }
}