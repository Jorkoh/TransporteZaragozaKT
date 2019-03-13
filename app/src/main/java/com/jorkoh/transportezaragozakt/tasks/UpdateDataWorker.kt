package com.jorkoh.transportezaragozakt.tasks

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.Stop
import com.jorkoh.transportezaragozakt.db.StopType
import com.jorkoh.transportezaragozakt.db.StopsDao
import com.parse.ParseObject
import com.parse.ParseQuery
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.concurrent.TimeUnit

class UpdateDataWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams),
    KoinComponent {

    companion object {
        @JvmStatic
        fun enqueueWorker() {
            val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val updateDataRequest = PeriodicWorkRequestBuilder<UpdateDataWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance().enqueue(updateDataRequest)
        }
    }

    private val stopsDao: StopsDao by inject()
    private val executors: AppExecutors by inject()

    override fun doWork(): Result {
        Log.d("TestingStuff", "DO WORK FIRED!!")

        // Do the work here
        checkBusStopsUpdate()
//        checkTramStopUpdate()

        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }

    //TODO: Replace this with the actual work
    private fun checkBusStopsUpdate() {
        with(ParseQuery.getQuery<ParseObject>(applicationContext.getString(R.string.parse_bus_stops_collection))) {
            orderByDescending("version")
            limit = 1
            getFirstInBackground { busStopDocument, e ->
                if (e != null) {
                    Log.d("Testing stuff", "Retrieving new stops failed", e)
                } else {
                    checkBusStopsUpdate(busStopDocument)
                }
            }
        }
    }

    private fun checkBusStopsUpdate(busStopDocument: ParseObject) {
        if (!isNewBusVersion(busStopDocument)) {
            Log.d("Testing stuff", "Bus stops data is up to date")
        } else {
            updateBusStops(busStopDocument)

            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val builder = NotificationCompat.Builder(applicationContext, "TestingStuff")
                .setSmallIcon(R.drawable.ic_bus)
                .setContentTitle("Updated bus stops")
                .setContentText("New version: ${busStopDocument.getInt("version")}")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            notificationManager.notify(1, builder.build())
        }
    }

    private fun isNewBusVersion(busStopDocument: ParseObject): Boolean {
        val sharedPreferences = applicationContext.getSharedPreferences(
            applicationContext.getString(R.string.preferences_version_number_key),
            Context.MODE_PRIVATE
        )

        return busStopDocument.getInt("version") > sharedPreferences.getInt(
            applicationContext.getString(R.string.saved_bus_version_number_key),
            1
        )
    }

    private fun updateBusStops(busStopDocument: ParseObject) {
        val busStopEntities = mutableListOf<Stop>()
        busStopDocument.getJSONArray("stops")?.let { stopsJson ->
            for (i in 0 until stopsJson.length()) {
                val stopJson = stopsJson.getJSONObject(i)
                val location = stopJson.getJSONArray("location")?.let { locationJsonArray ->
                    LatLng(locationJsonArray[1] as Double, locationJsonArray[0] as Double)
                }
                val stopEntity = Stop(
                    StopType.BUS,
                    checkNotNull(stopJson.getString("id")),
                    checkNotNull(stopJson.getString("title")),
                    checkNotNull(location),
                    false
                )

                busStopEntities.add(stopEntity)
            }
        }

        executors.diskIO().execute {
            stopsDao.updateStops(busStopEntities)
        }

        val sharedPreferences = applicationContext.getSharedPreferences(
            applicationContext.getString(R.string.preferences_version_number_key),
            Context.MODE_PRIVATE
        )
        with(sharedPreferences.edit()) {
            putInt(
                applicationContext.getString(R.string.saved_bus_version_number_key),
                busStopDocument.getInt("version")
            )
            apply()
        }
    }
}