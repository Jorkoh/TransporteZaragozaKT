package com.jorkoh.transportezaragozakt.tasks

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
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



class UpdateDataWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams),
    KoinComponent {

    private val stopsDao: StopsDao by inject()
    private val executors: AppExecutors by inject()
    private val sharedPreferences : SharedPreferences by inject()

    override fun doWork(): Result {
        Log.d("TestingStuff", "DO WORK FIRED!!")

        // Do the work here
        checkBusStopsUpdate()
        checkTramStopsUpdate()

        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }

    private fun checkBusStopsUpdate() {
        with(ParseQuery.getQuery<ParseObject>(applicationContext.getString(R.string.parse_bus_stops_collection))) {
            orderByDescending("version")
            limit = 1
            getFirstInBackground { busStopDocument, e ->
                if (e != null) {
                    Log.d("Testing stuff", "Retrieving new bus stops failed", e)
                } else {
                    checkBusStopsUpdate(busStopDocument)
                }
            }
        }
    }

    private fun checkTramStopsUpdate() {
        with(ParseQuery.getQuery<ParseObject>(applicationContext.getString(R.string.parse_tram_stops_collection))) {
            orderByDescending("version")
            limit = 1
            getFirstInBackground { tramStopDocument, e ->
                if (e != null) {
                    Log.d("Testing stuff", "Retrieving new tram stops failed", e)
                } else {
                    checkTramStopsUpdate(tramStopDocument)
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

    private fun checkTramStopsUpdate(tramStopDocument: ParseObject) {
        if (!isNewTramVersion(tramStopDocument)) {
            Log.d("Testing stuff", "Tram stops data is up to date")
        } else {
            updateTramStops(tramStopDocument)

            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val builder = NotificationCompat.Builder(applicationContext, "TestingStuff")
                .setSmallIcon(R.drawable.ic_bus)
                .setContentTitle("Updated tram stops")
                .setContentText("New version: ${tramStopDocument.getInt("version")}")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            notificationManager.notify(1, builder.build())
        }
    }

    private fun isNewBusVersion(busStopDocument: ParseObject): Boolean {
        return busStopDocument.getInt("version") > sharedPreferences.getInt(
            applicationContext.getString(R.string.saved_bus_version_number_key),
            1
        )
    }

    private fun isNewTramVersion(tramStopDocument: ParseObject): Boolean {
        return tramStopDocument.getInt("version") > sharedPreferences.getInt(
            applicationContext.getString(R.string.saved_tram_version_number_key),
            1
        )
    }

    private fun updateBusStops(busStopDocument: ParseObject) {
        val busStopEntities = mutableListOf<Stop>()
        val stopsJson = checkNotNull(busStopDocument.getJSONArray("stops"))
        for (i in 0 until stopsJson.length()) {
            val stopJson = stopsJson.getJSONObject(i)
            val location = stopJson.getJSONArray("location")?.let { locationJsonArray ->
                LatLng(locationJsonArray[1] as Double, locationJsonArray[0] as Double)
            }
            val linesJson = stopJson.getJSONArray("lines")
            val lines = mutableListOf<String>()
            for (j in 0 until linesJson.length()) {
                lines.add(linesJson.getString(i))
            }

            val stopEntity = Stop(
                StopType.BUS,
                stopJson.getString("id"),
                stopJson.getString("number"),
                stopJson.getString("stopTitle"),
                checkNotNull(location),
                lines,
                false
            )
            busStopEntities.add(stopEntity)
        }
        executors.diskIO().execute {
            stopsDao.updateStops(busStopEntities)
        }

        with(sharedPreferences.edit()) {
            putInt(
                applicationContext.getString(R.string.saved_bus_version_number_key),
                busStopDocument.getInt("version")
            )
            apply()
        }
    }

    private fun updateTramStops(tramStopDocument: ParseObject) {
        val tramStopEntities = mutableListOf<Stop>()
        val stopsJson = checkNotNull(tramStopDocument.getJSONArray("stops"))
        for (i in 0 until stopsJson.length()) {
            val stopJson = stopsJson.getJSONObject(i)
            val location = stopJson.getJSONArray("location")?.let { locationJsonArray ->
                LatLng(locationJsonArray[1] as Double, locationJsonArray[0] as Double)
            }
            val linesJson = stopJson.getJSONArray("lines")
            val lines = mutableListOf<String>()
            for (j in 0 until linesJson.length()) {
                lines.add(linesJson.getString(j))
            }

            val stopEntity = Stop(
                StopType.TRAM,
                stopJson.getString("id"),
                stopJson.getString("number"),
                stopJson.getString("stopTitle"),
                checkNotNull(location),
                lines,
                false
            )
            tramStopEntities.add(stopEntity)
        }
        executors.diskIO().execute {
            stopsDao.updateStops(tramStopEntities)
        }

        with(sharedPreferences.edit()) {
            putInt(
                applicationContext.getString(R.string.saved_tram_version_number_key),
                tramStopDocument.getInt("version")
            )
            apply()
        }
    }
}