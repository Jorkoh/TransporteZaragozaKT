package com.jorkoh.transportezaragozakt.repositories

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.db.AppDatabase
import com.jorkoh.transportezaragozakt.db.RuralTracking
import com.jorkoh.transportezaragozakt.db.TrackingsDao
import com.jorkoh.transportezaragozakt.repositories.util.NetworkBoundResource
import com.jorkoh.transportezaragozakt.repositories.util.Resource
import com.jorkoh.transportezaragozakt.services.common.util.ApiResponse
import com.jorkoh.transportezaragozakt.services.common.util.ApiSuccessResponse
import com.jorkoh.transportezaragozakt.services.ctaz_api.CtazAPIService
import com.jorkoh.transportezaragozakt.services.ctaz_api.responses.rural.RuralTrackingsCtazAPIResponse
import java.util.*

interface RuralRepository {
    fun loadTrackings(): LiveData<Resource<List<RuralTracking>>>
}

class RuralRepositoryImplementation(
    private val appExecutors: AppExecutors,
    private val ctazAPIService: CtazAPIService,
    private val trackingsDao: TrackingsDao,
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
}

// The list is fresh if its oldest member is fresh
fun List<RuralTracking>.isFresh(timeoutInSeconds: Int) =
    (this.minBy { it.updatedAt }?.isFresh(timeoutInSeconds) ?: false)

// The destination is fresh if the time elapsed since the last update is less than the timeout
fun RuralTracking.isFresh(timeoutInSeconds: Int): Boolean = ((Date().time - updatedAt.time) / 1000) < timeoutInSeconds