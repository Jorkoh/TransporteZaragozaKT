package com.jorkoh.transportezaragozakt.tasks

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.*
import com.parse.ParseObject
import com.parse.ParseQuery
import org.koin.core.KoinComponent
import org.koin.core.inject


class UpdateDataWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams),
    KoinComponent {

    private val stopsDao: StopsDao by inject()
    private val executors: AppExecutors by inject()
    private val sharedPreferences: SharedPreferences by inject()

    // May fire multiple times? https://issuetracker.google.com/issues/119886476
    override fun doWork(): Result {

        // Bus stops
        checkUpdate(
            applicationContext.getString(R.string.parse_bus_stops_collection),
            applicationContext.getString(R.string.saved_bus_stops_version_number_key),
            2,
            updateBusStops
        )

        // Tram stops
        checkUpdate(
            applicationContext.getString(R.string.parse_tram_stops_collection),
            applicationContext.getString(R.string.saved_tram_stops_version_number_key),
            2,
            updateTramStops
        )

        // Bus lines
        checkUpdate(
            applicationContext.getString(R.string.parse_bus_lines_collection),
            applicationContext.getString(R.string.saved_bus_lines_version_number_key),
            1,
            updateBusLines
        )

        // Tram lines
        checkUpdate(
            applicationContext.getString(R.string.parse_tram_lines_collection),
            applicationContext.getString(R.string.saved_tram_lines_version_number_key),
            1,
            updateTramLines
        )

        // Bus lines locations
        checkUpdate(
            applicationContext.getString(R.string.parse_bus_lines_locations_collection),
            applicationContext.getString(R.string.saved_bus_lines_locations_version_number_key),
            1,
            updateBusLinesLocations
        )

        // Tram lines locations
        checkUpdate(
            applicationContext.getString(R.string.parse_tram_lines_locations_collection),
            applicationContext.getString(R.string.saved_tram_lines_locations_version_number_key),
            1,
            updateTramLinesLocations
        )

        // TODO Changelog

        return Result.success()
    }

    private fun checkUpdate(collectionName: String, versionKey: String, defaultVersion: Int, update: (document: ParseObject) -> Unit) {
        with(ParseQuery.getQuery<ParseObject>(collectionName)) {
            orderByDescending("version")
            limit = 1
            getFirstInBackground { document, e ->
                if (e == null && isNewVersion(document, versionKey, defaultVersion)) {
                    update(document)
                    showUpdateNotification()
                }
            }
        }
    }

    private fun isNewVersion(document: ParseObject, versionKey: String, defaultVersion: Int) =
        document.getInt("version") > sharedPreferences.getInt(versionKey, defaultVersion)

    private val updateBusStops = { busStopDocument: ParseObject ->
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
                lines.add(linesJson.getString(j))
            }

            val stopEntity = Stop(
                StopType.BUS,
                stopJson.getString("id"),
                stopJson.getString("number"),
                stopJson.getString("title"),
                checkNotNull(location),
                lines,
                false
            )
            busStopEntities.add(stopEntity)
        }
        executors.diskIO().execute {
            stopsDao.updateStops(busStopEntities, StopType.BUS)
        }

        with(sharedPreferences.edit()) {
            putInt(
                applicationContext.getString(R.string.saved_bus_stops_version_number_key),
                busStopDocument.getInt("version")
            )
            apply()
        }
    }

    private val updateTramStops = { tramStopDocument: ParseObject ->
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
                stopJson.getString("title"),
                checkNotNull(location),
                lines,
                false
            )
            tramStopEntities.add(stopEntity)
        }
        executors.diskIO().execute {
            stopsDao.updateStops(tramStopEntities, StopType.TRAM)
        }

        with(sharedPreferences.edit()) {
            putInt(
                applicationContext.getString(R.string.saved_tram_stops_version_number_key),
                tramStopDocument.getInt("version")
            )
            apply()
        }
    }

    private val updateBusLines = { busLinesDocument: ParseObject ->
        val busLinesEntities = mutableListOf<Line>()
        val linesJson = checkNotNull(busLinesDocument.getJSONArray("lines"))
        for (i in 0 until linesJson.length()) {
            linesJson.getJSONObject(i).run {
                val destinationsJson = getJSONArray("destinations")
                val destinations = mutableListOf<String>()
                for (j in 0 until destinationsJson.length()) {
                    destinations.add(destinationsJson.getString(j))
                }
                val stopsFirstDestinationJson = getJSONArray("stopsFirstDestination")
                val stopsFirstDestination = mutableListOf<String>()
                for (j in 0 until stopsFirstDestinationJson.length()) {
                    stopsFirstDestination.add(stopsFirstDestinationJson.getString(j))
                }
                val stopsSecondDestinationJson = getJSONArray("stopsSecondDestination")
                val stopsSecondDestination = mutableListOf<String>()
                for (j in 0 until stopsSecondDestinationJson.length()) {
                    stopsSecondDestination.add(stopsSecondDestinationJson.getString(j))
                }

                val lineEntity = Line(
                    getString("id"),
                    LineType.BUS,
                    getString("name"),
                    destinations,
                    stopsFirstDestination,
                    stopsSecondDestination
                )
                busLinesEntities.add(lineEntity)
            }
        }
        executors.diskIO().execute {
            stopsDao.updateLines(busLinesEntities, LineType.BUS)
        }

        with(sharedPreferences.edit()) {
            putInt(
                applicationContext.getString(R.string.saved_bus_lines_version_number_key),
                busLinesDocument.getInt("version")
            )
            apply()
        }
    }

    private val updateTramLines = { tramLinesDocument: ParseObject ->
        val tramLinesEntities = mutableListOf<Line>()
        val linesJson = checkNotNull(tramLinesDocument.getJSONArray("lines"))
        for (i in 0 until linesJson.length()) {
            linesJson.getJSONObject(i).run {
                val destinationsJson = getJSONArray("destinations")
                val destinations = mutableListOf<String>()
                for (j in 0 until destinationsJson.length()) {
                    destinations.add(destinationsJson.getString(j))
                }
                val stopsFirstDestinationJson = getJSONArray("stopsFirstDestination")
                val stopsFirstDestination = mutableListOf<String>()
                for (j in 0 until stopsFirstDestinationJson.length()) {
                    stopsFirstDestination.add(stopsFirstDestinationJson.getString(j))
                }
                val stopsSecondDestinationJson = getJSONArray("stopsSecondDestination")
                val stopsSecondDestination = mutableListOf<String>()
                for (j in 0 until stopsSecondDestinationJson.length()) {
                    stopsSecondDestination.add(stopsSecondDestinationJson.getString(j))
                }

                val lineEntity = Line(
                    getString("id"),
                    LineType.TRAM,
                    getString("name"),
                    destinations,
                    stopsFirstDestination,
                    stopsSecondDestination
                )
                tramLinesEntities.add(lineEntity)
            }
        }
        executors.diskIO().execute {
            stopsDao.updateLines(tramLinesEntities, LineType.TRAM)
        }

        with(sharedPreferences.edit()) {
            putInt(
                applicationContext.getString(R.string.saved_tram_lines_version_number_key),
                tramLinesDocument.getInt("version")
            )
            apply()
        }
    }

    private val updateBusLinesLocations = { busLinesLocationsDocument: ParseObject ->
        val busLinesLocationsEntities = mutableListOf<LineLocation>()
        val linesLocationsJson = checkNotNull(busLinesLocationsDocument.getJSONArray("lines"))
        for (i in 0 until linesLocationsJson.length()) {
            linesLocationsJson.getJSONObject(i).let { line ->
                val locationsJson = line.getJSONArray("coordinates")
                for (j in 0 until locationsJson.length()) {
                    locationsJson.getJSONArray(j).let { location ->
                        val lineLocationEntity = LineLocation(
                            line.getString("id"),
                            LineType.BUS,
                            j + 1,
                            LatLng(location[1] as Double, location[0] as Double)
                        )
                        busLinesLocationsEntities.add(lineLocationEntity)
                    }
                }
            }
        }
        executors.diskIO().execute {
            stopsDao.updateLinesLocations(busLinesLocationsEntities, LineType.BUS)
        }

        with(sharedPreferences.edit()) {
            putInt(
                applicationContext.getString(R.string.saved_bus_lines_locations_version_number_key),
                busLinesLocationsDocument.getInt("version")
            )
            apply()
        }
    }

    private val updateTramLinesLocations = { tramLinesLocationsDocument: ParseObject ->
        val tramLinesLocationsEntities = mutableListOf<LineLocation>()
        val linesLocationsJson = checkNotNull(tramLinesLocationsDocument.getJSONArray("lines"))
        for (i in 0 until linesLocationsJson.length()) {
            linesLocationsJson.getJSONObject(i).let { line ->
                val locationsJson = line.getJSONArray("coordinates")
                for (j in 0 until locationsJson.length()) {
                    locationsJson.getJSONArray(j).let { location ->
                        val lineLocationEntity = LineLocation(
                            line.getString("id"),
                            LineType.TRAM,
                            j + 1,
                            LatLng(location[1] as Double, location[0] as Double)
                        )
                        tramLinesLocationsEntities.add(lineLocationEntity)
                    }
                }
            }
        }
        executors.diskIO().execute {
            stopsDao.updateLinesLocations(tramLinesLocationsEntities, LineType.TRAM)
        }

        with(sharedPreferences.edit()) {
            putInt(
                applicationContext.getString(R.string.saved_tram_lines_locations_version_number_key),
                tramLinesLocationsDocument.getInt("version")
            )
            apply()
        }
    }

    private fun showUpdateNotification() {
        val updateNotification =
            NotificationCompat.Builder(
                applicationContext,
                applicationContext.getString(R.string.notification_channel_id_updates)
            )
                .apply {
                    setSmallIcon(R.drawable.ic_notification_icon)
                    setContentTitle(applicationContext.getString(R.string.updated_stops_lines_title))
                    setContentText(applicationContext.getString(R.string.updated_stops_lines_message))
                    priority = NotificationCompat.PRIORITY_LOW
                    setOnlyAlertOnce(true)
                }

        (applicationContext.getSystemService(LifecycleService.NOTIFICATION_SERVICE) as NotificationManager).notify(
            -50,
            updateNotification.build()
        )
    }
}