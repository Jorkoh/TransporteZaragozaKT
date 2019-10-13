package com.jorkoh.transportezaragozakt.destinations.line_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.repositories.StopsRepository

class LineDetailsViewModel(
    private val stopsRepository: StopsRepository
) :
    ViewModel() {

    lateinit var lineId: String
    lateinit var lineType: LineType

    val line: LiveData<Line>
        get() = _line
    private val _line = MutableLiveData<Line>()

    lateinit var stops: LiveData<List<Stop>>
    lateinit var lineLocations: LiveData<List<LineLocation>>

    val selectedStopId = MutableLiveData<String>()

    fun init(lineId: String, lineType: LineType) {
        this.lineId = lineId
        this.lineType = lineType

        lineLocations = stopsRepository.loadLineLocations(lineType, lineId)

        stops = Transformations.switchMap(stopsRepository.loadLine(lineType, lineId)) { line ->
            _line.value = line
            line?.let {
                // Load the stops forming the line
                stopsRepository.loadStops(line.type.toStopType(), line.stopIdsFirstDestination + line.stopIdsSecondDestination)
            }
        }
    }
}