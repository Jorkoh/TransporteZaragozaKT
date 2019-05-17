package com.jorkoh.transportezaragozakt.destinations.line_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.repositories.SettingsRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository

class LineDetailsViewModel(
    private val stopsRepository: StopsRepository
) :
    ViewModel() {

    lateinit var lineId: String
    lateinit var lineType: LineType

    lateinit var line: LiveData<Line>
    lateinit var lineLocations: LiveData<List<LineLocation>>
    lateinit var stops: LiveData<List<Stop>>

    val selectedStopId = MutableLiveData<String>()

    fun init(lineId: String, lineType: LineType) {
        this.lineId = lineId
        this.lineType = lineType

        lineLocations = stopsRepository.loadLineLocations(lineType, lineId)
        line = stopsRepository.loadLine(lineType, lineId)
    }

    fun loadStops(stopIds: List<String>) {
        stops = stopsRepository.loadStops(lineType.toStopType(), stopIds)
    }
}