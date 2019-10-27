package com.jorkoh.transportezaragozakt.destinations.line_details

import android.os.Handler
import androidx.lifecycle.*
import com.jorkoh.transportezaragozakt.db.Line
import com.jorkoh.transportezaragozakt.db.LineType
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.db.toStopType
import com.jorkoh.transportezaragozakt.repositories.RuralRepository
import com.jorkoh.transportezaragozakt.repositories.StopsRepository
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.repositories.util.Status

class LineDetailsViewModel(
    val lineId: String,
    val lineType: LineType,
    private val stopsRepository: StopsRepository,
    val trackingsRepository: RuralRepository
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

    val ruralTrackings = MediatorLiveData<Resource<List<RuralTracking>>>()
    private lateinit var tempRuralTrackings: LiveData<Resource<List<RuralTracking>>>

    private val handler = Handler()
    private val refreshTrackers = object : Runnable {
        override fun run() {
            if (::tempRuralTrackings.isInitialized) {
                ruralTrackings.removeSource(tempRuralTrackings)
            }
            tempRuralTrackings = trackingsRepository.loadTrackingsFromLine(lineId)
            ruralTrackings.addSource(tempRuralTrackings) { value ->
                if (value.status == Status.SUCCESS && value.data != ruralTrackings.value) {
                    ruralTrackings.postValue(value)
                }
            }
            handler.postDelayed(this, 30000)
        }
    }

    init {
        if (lineType == LineType.RURAL) {
            handler.post(refreshTrackers)
        }
    }
}