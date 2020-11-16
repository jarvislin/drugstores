package com.jarvislin.drugstores.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jarvislin.domain.entity.DownloadResult
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.repository.DrugstoreRepository
import com.jarvislin.drugstores.RxImmediateSchedulerRule
import com.jarvislin.drugstores.data.LocalData
import com.jarvislin.drugstores.data.db.DrugstoreDao
import com.jarvislin.drugstores.data.model.ApiDrugstoreInfo
import com.jarvislin.drugstores.data.remote.Downloader
import com.jarvislin.drugstores.extension.toJson
import com.jarvislin.drugstores.page.map.MarkerCacheManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.*
import java.io.File


class DrugstoreRepositoryImplTest {

    @get:Rule
    val rxRule = RxImmediateSchedulerRule()
    private lateinit var repository: DrugstoreRepository
    private val drugstoreDao = mockk<DrugstoreDao>(relaxed = true)
    private val info = mockk<DrugstoreInfo>(relaxed = true)
    private val localData = mockk<LocalData>(relaxed = true)
    private val downloader = mockk<Downloader>(relaxed = true)
    private val firestore = mockk<FirebaseFirestore>(relaxed = true)

    private val lat = 123.0
    private val lng = 456.0
    private val file = File("file")

    @Before
    fun setUp() {
        repository = DrugstoreRepositoryImpl(drugstoreDao, localData, downloader, firestore)
    }

    @After
    fun tearDown() {
        unmockkAll()
        file.delete()
    }

    @Test
    fun saveDrugstoreInfo() {
        val list = listOf(info)
        every { drugstoreDao.addDrugstoreInfo(list) } returns Completable.complete()

        repository.saveDrugstoreInfo(list)
            .test()
            .assertComplete()
    }

    @Test
    fun deleteDrugstoreInfo() {
        every { drugstoreDao.removeDrugstoreInfo() } returns Single.just(-1)

        repository.deleteDrugstoreInfo()
            .test()
            .assertValue(-1)
    }

    @Test
    fun findNearDrugstoreInfo() {
        val result = listOf(info)

        every {
            drugstoreDao.getNearDrugstoreInfo(
                lat, lng,
                MarkerCacheManager.MAX_MARKER_AMOUNT
            )
        } returns Single.just(result)

        repository.findNearDrugstoreInfo(lat, lng)
            .test()
            .assertValue(result)
    }

    @Test
    fun saveLastLocation() {
        repository.saveLastLocation(lat, lng)
        verify { localData.lastLocation = "$lat,$lng" }
    }

    @Test
    fun getLastLocation() {
        val raw = "$lat,$lng"
        val location = Pair(lat, lng)

        every { localData.lastLocation } returns raw

        Assert.assertEquals(location, repository.getLastLocation())
    }

    @Test
    fun downloadData() {
        val result = DownloadResult(File("file"))

        every { downloader.download(DrugstoreRepositoryImpl.DATA_URL) } returns Single.just(result)

        repository.downloadData()
            .test()
            .assertValue(result)
    }

    @Test
    fun searchAddress() {
        val word = "word"
        val result = listOf(info)

        every { repository.searchAddress(word) } returns Single.just(result)

        repository.searchAddress(word)
            .test()
            .assertValue(result)
    }

    @Test
    fun transformToDrugstoreInfo() {
        ApiDrugstoreInfo("", emptyList()).toJson().let { file.writeText(it) }

        repository.transformToDrugstoreInfo(file)
            .test()
            .assertValue { it.isEmpty() }
    }
}