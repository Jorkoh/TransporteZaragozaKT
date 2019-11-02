package com.jorkoh.transportezaragozakt.destinations.line_details

import androidx.lifecycle.*
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.repositories.RuralRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository
import com.jorkoh.transportezaragozakt.repositories.util.Status
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

class LineDetailsViewModel(
    val lineId: String,
    val lineType: LineType,
    stopsRepository: StopsRepository,
    trackingsRepository: RuralRepository
) : ViewModel() {

    val line: LiveData<Line> = stopsRepository.loadLine(lineType, lineId).asLiveData()

    val stops: LiveData<List<Stop>> = Transformations.switchMap(line) { line ->
        stopsRepository.loadStops(line.type.toStopType(), line.stopIdsFirstDestination + line.stopIdsSecondDestination).asLiveData()
    }

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