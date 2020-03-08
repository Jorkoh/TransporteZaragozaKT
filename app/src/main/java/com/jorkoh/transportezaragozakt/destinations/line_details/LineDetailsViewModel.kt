package com.jorkoh.transportezaragozakt.destinations.line_details

import androidx.lifecycle.*
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.repositories.RuralRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository
import com.jorkoh.transportezaragozakt.repositories.util.Status
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

class LineDetailsViewModel(
    private val stopsRepository: StopsRepository,
    private val trackingsRepository: RuralRepository
) : ViewModel() {

    lateinit var lineId: String
    lateinit var lineType: LineType

    lateinit var line: LiveData<Line>

    lateinit var stops: LiveData<List<Stop>>

    lateinit var lineLocations: LiveData<List<LineLocation>>

    val preservedItemId = MutableLiveData<String>()

    val selectedItemId = Channel<String>()

    var ruralTrackings: LiveData<List<RuralTracking>>? = null

    fun init(lineId: String, lineType: LineType) {
        this.lineId = lineId
        this.lineType = lineType

        line = stopsRepository.loadLine(lineType, lineId).asLiveData()

        stops = Transformations.switchMap(line) { line ->
            stopsRepository.loadStops(line.type.toStopType(), line.stopIdsFirstDestination + line.stopIdsSecondDestination).asLiveData()
        }

        lineLocations = stopsRepository.loadLineLocations(lineType, lineId).asLiveData()

        ruralTrackings =
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
}