package com.jorkoh.transportezaragozakt.destinations.line_details

import androidx.lifecycle.*
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.repositories.RuralRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository
import com.jorkoh.transportezaragozakt.repositories.util.Status
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LineDetailsViewModel(
    val lineId: String,
    val lineType: LineType,
    stopsRepository: StopsRepository,
    trackingsRepository: RuralRepository
) : ViewModel() {

    private var activeStopsCollector : Job? = null

    val line: LiveData<Line> = stopsRepository.loadLine(lineType, lineId).onEach { line ->
        activeStopsCollector?.cancel()
        activeStopsCollector = viewModelScope.launch {
            stopsRepository.loadStops(line.type.toStopType(), line.stopIdsFirstDestination + line.stopIdsSecondDestination)
                .collect { stops ->
                    _stops.postValue(stops)
                }
        }
    }.asLiveData()

    val stops: LiveData<List<Stop>>
        get() = _stops
    private val _stops = MutableLiveData<List<Stop>>()

    val lineLocations = stopsRepository.loadLineLocations(lineType, lineId).asLiveData()

    val selectedItemId = MutableLiveData<String>()

    val ruralTrackings: LiveData<List<RuralTracking>>? =
        if (lineType == LineType.RURAL) {
            liveData {
                while (true) {
                    trackingsRepository.loadTrackings().collect { trackings ->
                        if (trackings.status == Status.SUCCESS) {
                            trackings.data?.filter { it.lineId == lineId }?.let { lineTrackings ->
                                emit(lineTrackings)
                            }
                        }
                    }
                    delay(30_000)
                }
            }
        } else {
            null
        }
}