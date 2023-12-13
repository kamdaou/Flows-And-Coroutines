package com.example.coroutines_flows_jc.ui.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutines_flows_jc.data.data_source.remote.dto.LocationX
import com.example.coroutines_flows_jc.data.data_source.remote.dto.SwapiResult
import com.example.coroutines_flows_jc.data.data_source.util.UiText
import com.example.coroutines_flows_jc.data.repository.MyRepository
import com.example.coroutines_flows_jc.domain.model.TypesOrganisation
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.measureTimeMillis

@ExperimentalCoroutinesApi
class MyViewModel(
    private val repository: MyRepository
) : ViewModel() {
    private val _locationsSharedPlusFlow: MutableSharedFlow<List<LocationX>> = MutableSharedFlow()
    val locationsSharedPlusFlow: SharedFlow<List<LocationX>> = _locationsSharedPlusFlow.shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        1
    )
    private val _peopleChannelFlow: MutableStateFlow<List<SwapiResult>> =
        MutableStateFlow(emptyList())
    val peopleChannelFlow: StateFlow<List<SwapiResult>> = _peopleChannelFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    private val _peopleFlow: MutableSharedFlow<List<SwapiResult>> = MutableSharedFlow()
    val peopleFlow: SharedFlow<List<SwapiResult>> = _peopleFlow.shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        1
    )
    private val _typesSharedPlusList: MutableSharedFlow<List<TypesOrganisation>> =
        MutableSharedFlow()
    val typesSharedPlusList: SharedFlow<List<TypesOrganisation>> = _typesSharedPlusList.shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        1
    )
    private val _error: MutableSharedFlow<UiText> = MutableSharedFlow()
    val error: SharedFlow<UiText> = _error.shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )
    private val _loadingLocation: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loadingLocation: StateFlow<Boolean> = _loadingLocation

    private val _loadingType: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loadingType: StateFlow<Boolean> = _loadingType

    private val _loadingSwapiFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loadingSwapiFlow: StateFlow<Boolean> = _loadingSwapiFlow

    private val _loadingSwapiChannel: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loadingSwapiChannel: StateFlow<Boolean> = _loadingSwapiChannel

    init {
        fetchLocationsAndTypes()
        cpuIntensiveAndSharedState()
    }

    /**
     * In the following, we are running long cpu-intensive tasks,
     * Dispatchers.Default is recommended for them and we can limit
     * to one thread in order to avoid losing time for changing threads
     * For the last task, we are using mutex to control concurrency
     * and make sure we don't lose any data
     */
    private fun cpuIntensiveAndSharedState() {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                val list = mutableListOf<Int>()
                val time = measureTimeMillis {
                    (1..100_000).map {
                        launch { calculateFactorial(500_000) }
                    }
                }
                Log.d(
                    "FlowSwapi",
                    "factorial calculated in ${time}s with IO Dispatcher and list size is: ${list.size}"
                )
            }
            launch(Dispatchers.Default.limitedParallelism(1)) {
                val list = mutableListOf<Int>()
                val time = measureTimeMillis {
                    (1..100_000).map {
                        launch { calculateFactorial(500_000) }
                    }
                }
                Log.d(
                    "FlowSwapi",
                    "factorial calculated in ${time}s with Default Dispatcher and list size is: ${list.size}"
                )
            }
            launch(Dispatchers.IO.limitedParallelism(100)) {
                val list = mutableListOf<Int>()
                val time = measureTimeMillis {
                    (1..100_000).map {
                        launch { calculateFactorial(500_000) }
                    }
                }
                Log.d(
                    "FlowSwapi",
                    "factorial calculated in ${time}s with 100 Threads and list size is: ${list.size}"
                )
            }
    //            launch(Dispatchers.Main) { // Will block the main thread and the app may crash, don't uncomment unless you want to test
    //                val list = mutableListOf<Int>()
    //                val time = measureTimeMillis {
    //                    (1..100_000).map {
    //                        launch { calculateFactorial(50_000) }
    //                    }
    //                }
    //                Log.d("FlowSwapi", "factorial calculated in ${time}s with Main Dispatcher and list size is: ${list.size}")
    //            }

            launch(Dispatchers.IO) {
                var values: List<Deferred<Int>>
                val list = mutableListOf<Int>()
                val mutex = Mutex()
                val time = measureTimeMillis {
                    values = (1..100_000).map {
                        async { calculateFactorial(500_000) }
                    }
                    values.awaitAll().forEach {
                        mutex.withLock {
                            list.add(it)
                        }
                    }
                }
                Log.d(
                    "FlowSwapi",
                    "factorial calculated in ${time}s with 100 Threads and list size is: ${list.size}"
                )
            }
        }
    }

    internal fun fetchLocationsAndTypes() {
        _loadingType.update { true }
        _loadingLocation.update { true }
        _loadingSwapiChannel.update { true }
        _loadingSwapiFlow.update { true }
        viewModelScope.launch {
            val locationResult = async { repository.fetchLocationsWithFlow() }
            val typesResult = async { repository.fetchEnterpriseTypesWithList() }
            val swapiChannel = async { repository.emitChannelFlow() }
            val swapiFlow = async { repository.emitFlow() }

            locationResult.await()
                .onStart {
                    Log.d("FlowSwapi", "location started")
                    _loadingLocation.update {
                        true
                    }
                }
                .onCompletion {
                    _loadingLocation.update {
                        false
                    }
                }
                .catch {
                    it.localizedMessage?.let { it1 -> UiText.DynamicString(it1) }
                        ?.let { it2 -> _error.emit(it2) }
                }
                .onEach { list ->
                    _locationsSharedPlusFlow.emit(list)
                }
                .collect()
            _loadingLocation.update { false }
            try {
//                _loadingType.update { true }
                typesResult.await().let {
                    _loadingType.update { false }
                    Log.d("FlowSwapi", "types started")
                    if (it.data != null) {
                        _typesSharedPlusList.emit(it.data)
                    } else {
                        it.error?.let { it1 -> _error.emit(value = it1) }
                    }
                }
            } catch (e: Exception) {
                e.localizedMessage?.let { UiText.DynamicString(it) }?.let { _error.emit(it) }
            }


            try {
                val time = measureTimeMillis {
                    swapiChannel.await()
                        .onStart {
                            Log.d("FlowSwapi", "channel started")
                            _loadingSwapiChannel.update {
                                true
                            }
                        }
                        .onEach { emitted ->
                            Log.d("FlowSwapi", "channel fetched: $emitted")
                            _peopleChannelFlow.update {
                                it + emitted
                            }
                        }
                        .onCompletion {
                            _loadingSwapiChannel.update {
                                false
                            }
                        }
                        .catch { _error.emit(UiText.DynamicString(it.message.toString())) }
//                        .first { element ->
//                            element.any {
//                                it.url.contains("50")
//                            }
//                        }
                        .collect()
                    _loadingSwapiChannel.update { false }
                }
                Log.d("FlowSwapi", "Channel Time: $time")
            } catch (e: Exception) {
                _error.emit(UiText.DynamicString(e.message.toString()))
            }

            _loadingSwapiChannel.update { false }

            swapiFlow.await().let { flow ->
                try {
                    val time = measureTimeMillis {
                        flow
                            .onStart {
                                Log.d("FlowSwapi", "flow started")
                            _loadingSwapiFlow.update {
                                true
                            }
                            }
                            .onCompletion {
                                _loadingSwapiFlow.update {
                                    false
                                }
                            }
                            .catch { _error.emit(UiText.DynamicString(it.message.toString())) }
                            .onEach {
                                _peopleFlow.emit(_peopleChannelFlow.value + it)
                            }
                            .collect()
//                            .first { list ->
//                                list.any { it.url.contains("50") }
//                            }
                        _loadingSwapiFlow.update { false }
                    }
                    Log.d("FlowSwapi", "Flow time: $time")
                } catch (e: Exception) {
                    _error.emit(UiText.DynamicString(e.message.toString()))
                }
            }
        }
    }

    fun calculateFactorial(n: Int): Int {
        require(n > 0) { "$n should be greater than 0" }
        if (n == 0 || n == 1) {
            return 1
        }
        return (1..n).fold(1L, Long::times).toInt()
    }
}

