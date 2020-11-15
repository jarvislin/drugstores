package com.jarvislin.domain.interactor

import com.jarvislin.domain.entity.*
import com.jarvislin.domain.repository.DrugstoreRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException

class DrugstoreUseCaseTest {

    private lateinit var useCase: DrugstoreUseCase
    private val drugstoreRepository = mockk<DrugstoreRepository>(relaxed = true)

    @Before
    fun setUp() {
        useCase = DrugstoreUseCase(drugstoreRepository)
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun fetchDataSuccessfully() {
        val file = File("")
        val downloadResult = DownloadResult(file)
        val info = emptyList<DrugstoreInfo>()
        val subject: PublishSubject<UpdateProgress> = PublishSubject.create()

        every { drugstoreRepository.downloadData() } returns Single.just(downloadResult)
        every { drugstoreRepository.deleteDrugstoreInfo() } returns Single.just(-1)
        every { drugstoreRepository.transformToDrugstoreInfo(downloadResult.file) } returns Single.just(info)
        every { drugstoreRepository.saveDrugstoreInfo(info) } returns Completable.complete()


        val testObserver = subject.test()

        useCase.fetchData(subject)
            .test()
            .assertComplete()

        testObserver
            .assertValueAt(0, StartDownloading)
            .assertValueAt(1, LatestDataDownloaded)
            .assertValueAt(2, OldDataDeleted)
            .assertValueAt(3, LatestDataTransformed)
            .assertValueAt(4, LatestDataSaved)
    }

    @Test
    fun fetchDataFailed() {
        val subject: PublishSubject<UpdateProgress> = PublishSubject.create()

        every { drugstoreRepository.downloadData() } returns Single.error(IOException("Download failed"))

        val testObserver = subject.test()

        useCase.fetchData(subject)
            .test()
            .assertError(IOException::class.java)

        testObserver
            .assertValueAt(0, StartDownloading)
    }
}