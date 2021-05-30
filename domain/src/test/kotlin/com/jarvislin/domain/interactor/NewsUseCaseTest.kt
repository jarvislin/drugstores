package com.jarvislin.domain.interactor

import com.jarvislin.domain.repository.NewsRepository
import io.mockk.MockKAnnotations
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class NewsUseCaseTest {

    private lateinit var useCase: NewsUseCase
    private val repository = mockk<NewsRepository>(relaxed = true)

    @Before
    fun setUp() {
        useCase = NewsUseCase(repository)
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun downloadNews() {
        useCase.downloadNews()
        verify { repository.downloadNews() }
    }
}