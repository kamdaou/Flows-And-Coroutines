package com.example.coroutines_flows_jc.ui.signup

import com.example.coroutines_flows_jc.data.data_source.util.ApiResult
import com.example.coroutines_flows_jc.data.repository.MyRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class MyViewModelTest {

    private lateinit var viewModel: MyViewModel
    private lateinit var repository: MyRepository
    private val infiniteFlow = flow<Nothing> {
        while (true) {
            delay(100)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = mockk()
        coEvery { repository.fetchEnterpriseTypesWithList() } returns ApiResult()
        coEvery { repository.fetchLocationsWithFlow() } returns flowOf()
        coEvery { repository.emitFlow() } returns flowOf()
        coEvery { repository.emitChannelFlow() } returns flowOf()
        viewModel = MyViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test time dependency`() = runTest {
        // GIVEN - APIs with the most heavy call lasting 3 seconds
        coEvery { repository.fetchEnterpriseTypesWithList() } coAnswers {
            delay(999)
            ApiResult()
        }
        coEvery { repository.fetchLocationsWithFlow() } coAnswers {
            delay(2000)
            flowOf()
        }
        coEvery { repository.emitFlow() } coAnswers {
            delay(3000)
            flowOf()
        }
        coEvery { repository.emitChannelFlow() } coAnswers {
            delay(2000)
            flowOf()
        }
        // WHEN - they are called asynchronously
        viewModel.fetchLocationsAndTypes()

        // THEN -
        assertEquals(true, viewModel.loadingSwapiFlow.value)
        assertEquals(true, viewModel.loadingSwapiChannel.value)
        assertEquals(true, viewModel.loadingLocation.value)
        advanceTimeBy(1000)
        runCurrent()
        assertEquals(true, viewModel.loadingSwapiFlow.value)
        assertEquals(true, viewModel.loadingSwapiChannel.value)
        assertEquals(true, viewModel.loadingLocation.value)
        advanceTimeBy(500)
        runCurrent()
        assertEquals(true, viewModel.loadingSwapiFlow.value)
        assertEquals(true, viewModel.loadingSwapiChannel.value)
        assertEquals(true, viewModel.loadingLocation.value)

        advanceTimeBy(1500)
        runCurrent()
        assertEquals(false, viewModel.loadingSwapiChannel.value)
        assertEquals(false, viewModel.loadingSwapiFlow.value)
        assertEquals(false, viewModel.loadingLocation.value)
        assertEquals(false, viewModel.loadingType.value)
//        advanceUntilIdle()
        assertEquals(3000L, currentTime)
    }

    @Test
    fun `should start at most one connection`() = runTest {
        // given
        var connectionsCounter = 0
        val source = infiniteFlow
            .onStart { connectionsCounter++ }
            .onCompletion { connectionsCounter-- }
        val service = MessagesService(
            messagesSource = source,
            scope = backgroundScope,
        )
        // when
        service.observeMessages("0")
            .launchIn(backgroundScope)
        service.observeMessages("1")
            .launchIn(backgroundScope)
        service.observeMessages("0")
            .launchIn(backgroundScope)
        service.observeMessages("2")
            .launchIn(backgroundScope)
        delay(1000)
        // then
        assertEquals(1, connectionsCounter)
    }

    @Test
    fun `can start more than one connection`() = runTest {
        // given
        var connectionsCounter = 0
        val source = infiniteFlow
            .onStart { connectionsCounter++ }
            .onCompletion { connectionsCounter-- }
        val service = MessagesServices(
            messagesSource = source,
        )
        // when
        service.observeMessages("0")
            .launchIn(backgroundScope)
        service.observeMessages("1")
            .launchIn(backgroundScope)
        service.observeMessages("0")
            .launchIn(backgroundScope)
        service.observeMessages("2")
            .launchIn(backgroundScope)
        delay(1000)
        // then
        assertNotEquals(1, connectionsCounter)
    }
}