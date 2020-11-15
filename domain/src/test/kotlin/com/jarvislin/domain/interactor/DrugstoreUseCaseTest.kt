package com.jarvislin.domain.interactor

import com.jarvislin.domain.entity.*
import com.jarvislin.domain.repository.DrugstoreRepository
import io.mockk.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException

class DrugstoreUseCaseTest {

    private lateinit var useCase: DrugstoreUseCase
    private val drugstoreRepository = mockk<DrugstoreRepository>(relaxed = true)

    private val lat = 123.0
    private val lng = 456.0
    private val id = "12345"
    private val maskStatus = Status.Warning

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
        every { drugstoreRepository.transformToDrugstoreInfo(downloadResult.file) } returns
                Single.just(info)
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
            .assertValueAt(1, UpdateFailed)
    }

    @Test
    fun findNearDrugstoreInfo() {
        useCase.findNearDrugstoreInfo(lat, lng).test()

        verify { drugstoreRepository.findNearDrugstoreInfo(lat, lng) }
    }

    @Test
    fun saveLastLocation() {
        useCase.saveLastLocation(lat, lng)

        verify { drugstoreRepository.saveLastLocation(lat, lng) }
    }

    @Test
    fun getLastLocation() {
        val location = Pair(lat, lng)
        every { drugstoreRepository.getLastLocation() } returns location

        Assert.assertEquals(location, useCase.getLastLocation())
    }

    @Test
    fun searchAddress() {
        val keyword = "word"

        useCase.searchAddress(keyword)
        verify { drugstoreRepository.searchAddress(keyword) }
    }

    @Test
    fun reportMaskStatusSuccessfully() {
        every { drugstoreRepository.isValidReportTime() } returns true
        every { drugstoreRepository.reportMaskStatus(id, maskStatus) } returns
                Completable.complete()

        useCase.reportMaskStatus(id, maskStatus)
            .test()
            .assertComplete()

        verify {
            drugstoreRepository.reportMaskStatus(id, maskStatus)
            drugstoreRepository.saveReportTime()
        }
    }

    @Test
    fun reportMaskStatusFailed() {
        every { drugstoreRepository.isValidReportTime() } returns false

        useCase.reportMaskStatus(id, maskStatus)
            .test()
            .assertError(InvalidReportTimeException::class.java)

        verify(exactly = 0, verifyBlock = {
            drugstoreRepository.reportMaskStatus(id, maskStatus)
            drugstoreRepository.saveReportTime()
        })
    }

    @Test
    fun fetchMaskStatus() {
        useCase.fetchMaskStatus(id).test()
        verify { drugstoreRepository.fetchMaskStatus(id) }
    }

    @Test
    fun reportNumberTicket() {
        useCase.reportNumberTicket(id, true)
        verify { drugstoreRepository.reportNumberTicket(id, true) }
    }

    @Test
    fun fetchUsesNumberTicket() {
        useCase.fetchUsesNumberTicket(id)
        verify { drugstoreRepository.fetchUsesNumberTicket(id) }
    }

    @Test
    fun fetchRecords() {
        useCase.fetchRecords(id)
        verify { drugstoreRepository.fetchRecords(id) }
    }

    @Test
    fun isFirstLaunch() {
        every { drugstoreRepository.isFirstLaunch() } returns true
        Assert.assertTrue(useCase.isFirstLaunch())
    }

    @Test
    fun updateFirstLaunch() {
        useCase.updateFirstLaunch()
        verify { drugstoreRepository.updateFirstLaunch() }
    }

    @Test
    fun updateRatingCount() {
        useCase.updateRatingCount()
        verify { drugstoreRepository.updateRatingCount() }
    }
}