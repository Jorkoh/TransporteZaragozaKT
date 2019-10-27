package com.jorkoh.transportezaragozakt.destinations.line_details

import androidx.lifecycle.*
import com.jorkoh.transportezaragozakt.db.Line
import com.jorkoh.transportezaragozakt.db.LineType
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.db.toStopType
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
) :
    ViewModel() {

    val line: LiveData<Line>
        get() = _line
    private val _line = MutableLiveData<Line>()

    val stops = Transformations.switchMap(stopsRepository.loadLine(lineType, lineId)) { line ->
        _line.value = line
        line?.let {
            // Load the stops forming the line
            stopsRepository.loadStops(line.type.toStopType(), line.stopIdsFirstDestination + line.stopIdsSecondDestination)
        }
    }
    val lineLocations = stopsRepository.loadLineLocations(lineType, lineId)

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
                    delay(30000)
                }
            }
        } else {
            null
        }
}