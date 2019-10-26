package com.jorkoh.transportezaragozakt.repositories

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.db.daos.StopsDao
import com.jorkoh.transportezaragozakt.db.daos.TrackingsDao
import com.jorkoh.transportezaragozakt.repositories.util.NetworkBoundResource
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.services.common.util.ApiResponse
import com.jorkoh.transportezaragozakt.services.common.util.ApiSuccessResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.CtazAPIService
import com.jorkoh.transportezaragozakt.services.ctaz_api.officialAPIToCtazAPIId
import com.jorkoh.transportezaragozakt.services.ctaz_api.responses.rural.RuralStopCtazAPIResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.responses.rural.RuralTrackingsCtazAPIResponse
import java.util.*

interface RuralRepository {
    fun loadTrackings(): LiveData<Resource<List<RuralTracking>>>
    fun loadTrackingsFromLine(lineId: String): LiveData<Resource<List<RuralTracking>>>
    fun loadStopDestinations(ruralStopId: String): LiveData<Resource<List<StopDestination>>>
    fun loadStop(busStopId: String): LiveData<Stop>
    fun loadStops(): LiveData<List<Stop>>
    fun loadMainLines(): LiveData<List<Line>>
    fun loadLineLocations(lineId: String): LiveData<List<LineLocation>>
    fun loadLine(lineId: String): LiveData<Line>
    fun loadAlternativeLineIds(lineId: String): LiveData<List<String>>
    fun loadStops(stopIds: List<String>): LiveData<List<Stop>>
}

class RuralRepositoryImplementation(
    private val appExecutors: AppExecutors,
    private val ctazAPIService: CtazAPIService,
    private val trackingsDao: TrackingsDao,
    private val stopsDao: StopsDao,
    private val db: AppDatabase
) : RuralRepository {

    companion object {
        //Just to limit users a bit from spamming requests
        const val FRESH_TIMEOUT = 10
    }

    override fun loadTrackings(): LiveData<Resource<List<RuralTracking>>> {
        return object :
            NetworkBoundResource<List<RuralTracking>, RuralTrackingsCtazAPIResponse>(
                appExecutors
            ) {
            override fun processResponse(response: ApiSuccessResponse<RuralTrackingsCtazAPIResponse>): List<RuralTracking> {
                return response.body.toRuralTrackings()
            }

            override fun saveCallResult(result: List<RuralTracking>) {
                db.runInTransaction {
                    trackingsDao.deleteTrackings()
                    trackingsDao.insertTrackings(result)
                }
            }

            override fun shouldFetch(data: List<RuralTracking>?): Boolean {
                return (data == null || data.isEmpty() || !data.isFresh(FRESH_TIMEOUT))
            }

            override fun loadFromDb(): LiveData<List<RuralTracking>> = trackingsDao.getTrackings()

            override fun createCall(): LiveData<ApiResponse<RuralTrackingsCtazAPIResponse>> = ctazAPIService.getRuralTrackings()
        }.asLiveData()
    }

    override fun loadTrackingsFromLine(lineId: String): LiveData<Resource<List<RuralTracking>>> {
        return object :
            NetworkBoundResource<List<RuralTracking>, RuralTrackingsCtazAPIResponse>(
                appExecutors
            ) {
            override fun processResponse(response: ApiSuccessResponse<RuralTrackingsCtazAPIResponse>): List<RuralTracking> {
                return response.body.toRuralTrackings().filter { it.lineId == lineId }
            }

            override fun saveCallResult(result: List<RuralTracking>) {
                db.runInTransaction {
                    trackingsDao.deleteTrackings()
                    trackingsDao.insertTrackings(result)
                }
            }

            override fun shouldFetch(data: List<RuralTracking>?): Boolean {
                return (data == null || data.isEmpty() || !data.isFresh(FRESH_TIMEOUT))
            }

            override fun loadFromDb(): LiveData<List<RuralTracking>> = trackingsDao.getTrackings()

            override fun createCall(): LiveData<ApiResponse<RuralTrackingsCtazAPIResponse>> = ctazAPIService.getRuralTrackings()
        }.asLiveData()
    }

    override fun loadStopDestinations(ruralStopId: String): LiveData<Resource<List<StopDestination>>> {
        return object :
            NetworkBoundResource<List<StopDestination>, RuralStopCtazAPIResponse>(
                appExecutors
            ) {
            override fun processResponse(response: ApiSuccessResponse<RuralStopCtazAPIResponse>): List<StopDestination> {
                return response.body.toStopDestinations(ruralStopId)
            }

            override fun saveCallResult(result: List<StopDestination>) {
                db.runInTransaction {
                    stopsDao.deleteStopDestinations(ruralStopId)
                    stopsDao.insertStopDestinations(result)
                }
            }

            override fun shouldFetch(data: List<StopDestination>?): Boolean {
                return (data == null || data.isEmpty() || !data.isFresh(FRESH_TIMEOUT))
            }

            override fun loadFromDb(): LiveData<List<StopDestination>> = stopsDao.getStopDestinations(ruralStopId)

            override fun createCall() = ctazAPIService.getRuralStopCtazAPI(ruralStopId.officialAPIToCtazAPIId())

        }.asLiveData()
    }

    override fun loadStop(busStopId: String): LiveData<Stop> {
        return stopsDao.getStop(busStopId)
    }

    override fun loadStops(): LiveData<List<Stop>> {
        return stopsDao.getStopsByType(StopType.RURAL)
    }

    override fun loadStops(stopIds: List<String>): LiveData<List<Stop>> {
        return stopsDao.getStops(stopIds)
    }

    override fun loadMainLines(): LiveData<List<Line>> {
        return stopsDao.getMainLinesByType(LineType.RURAL)
    }

    override fun loadLineLocations(lineId: String): LiveData<List<LineLocation>> {
        return stopsDao.getLineLocations(lineId)
    }

    override fun loadLine(lineId: String): LiveData<Line> {
        return stopsDao.getLine(lineId)
    }

    override fun loadAlternativeLineIds(lineId: String): LiveData<List<String>> {
        return stopsDao.getAlternativeLineIds(lineId)
    }
}

// The list is fresh if its oldest member is fresh
fun List<RuralTracking>.isFresh(timeoutInSeconds: Int) = (this.minBy { it.updatedAt }?.isFresh(timeoutInSeconds) ?: false)

// The destination is fresh if the time elapsed since the last update is less than the timeout
fun RuralTracking.isFresh(timeoutInSeconds: Int): Boolean = ((Date().time - updatedAt.time) / 1000) < timeoutInSeconds