package com.jorkoh.transportezaragozakt.tasks

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.maps.model.LatLng
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.db.*
import com.jorkoh.transportezaragozakt.db.daos.StopsDao
import com.jorkoh.transportezaragozakt.destinations.utils.createChangelogDeepLink
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject


class UpdateDataWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val stopsDao: StopsDao by inject()
    private val sharedPreferences: SharedPreferences by inject()

    private val ctx: Context
        get() = applicationContext

    var newVersion = 0
    var oldVersion = 0

    // May fire multiple times? https://issuetracker.google.com/issues/119886476
    override suspend fun doWork() = coroutineScope {
        launch {
            val versions = ParseQuery.getQuery<ParseObject>(ctx.getString(R.string.parse_versions_collection)).first

            // Bus stops
            newVersion = versions.getInt(ctx.getString(R.string.parse_bus_stops_collection))
            oldVersion = sharedPreferences.getInt(ctx.getString(R.string.saved_bus_stops_version_number_key), 0)
            if (newVersion != oldVersion) {
                val doc = ParseQuery.getQuery<ParseObject>(ctx.getString(R.string.parse_bus_stops_collection))
                    .whereEqualTo("version", newVersion)
                    .first
                updateStops(doc, StopType.BUS)
                updateVersion(ctx.getString(R.string.saved_bus_stops_version_number_key), newVersion)
            }

            // Tram stops
            newVersion = versions.getInt(ctx.getString(R.string.parse_tram_stops_collection))
            oldVersion = sharedPreferences.getInt(ctx.getString(R.string.saved_tram_stops_version_number_key), 0)
            if (newVersion != oldVersion) {
                val doc = ParseQuery.getQuery<ParseObject>(ctx.getString(R.string.parse_tram_stops_collection))
                    .whereEqualTo("version", newVersion)
                    .first
                updateStops(doc, StopType.TRAM)
                updateVersion(ctx.getString(R.string.saved_tram_stops_version_number_key), newVersion)
            }

            // Rural stops
            newVersion = versions.getInt(ctx.getString(R.string.parse_rural_stops_collection))
            oldVersion = sharedPreferences.getInt(ctx.getString(R.string.saved_rural_stops_version_number_key), 0)
            if (newVersion != oldVersion) {
                val doc = ParseQuery.getQuery<ParseObject>(ctx.getString(R.string.parse_rural_stops_collection))
                    .whereEqualTo("version", newVersion)
                    .first
                updateStops(doc, StopType.RURAL)
                updateVersion(ctx.getString(R.string.saved_rural_stops_version_number_key), newVersion)
            }

            // Bus lines
            newVersion = versions.getInt(ctx.getString(R.string.parse_bus_lines_collection))
            oldVersion = sharedPreferences.getInt(ctx.getString(R.string.saved_bus_lines_version_number_key), 0)
            if (newVersion != oldVersion) {
                val doc = ParseQuery.getQuery<ParseObject>(ctx.getString(R.string.parse_bus_lines_collection))
                    .whereEqualTo("version", newVersion)
                    .first
                updateLines(doc, LineType.BUS)
                updateVersion(ctx.getString(R.string.saved_bus_lines_version_number_key), newVersion)
            }

            // Tram lines
            newVersion = versions.getInt(ctx.getString(R.string.parse_tram_lines_collection))
            oldVersion = sharedPreferences.getInt(ctx.getString(R.string.saved_tram_lines_version_number_key), 0)
            if (newVersion != oldVersion) {
                val doc = ParseQuery.getQuery<ParseObject>(ctx.getString(R.string.parse_tram_lines_collection))
                    .whereEqualTo("version", newVersion)
                    .first
                updateLines(doc, LineType.TRAM)
                updateVersion(ctx.getString(R.string.saved_tram_lines_version_number_key), newVersion)
            }

            // Rural lines
            newVersion = versions.getInt(ctx.getString(R.string.parse_rural_lines_collection))
            oldVersion = sharedPreferences.getInt(ctx.getString(R.string.saved_rural_lines_version_number_key), 0)
            if (newVersion != oldVersion) {
                val doc = ParseQuery.getQuery<ParseObject>(ctx.getString(R.string.parse_rural_lines_collection))
                    .whereEqualTo("version", newVersion)
                    .first
                updateLines(doc, LineType.RURAL)
                updateVersion(ctx.getString(R.string.saved_rural_lines_version_number_key), newVersion)
            }

            // Bus lines locations
            newVersion = versions.getInt(ctx.getString(R.string.parse_bus_lines_locations_collection))
            oldVersion = sharedPreferences.getInt(ctx.getString(R.string.saved_bus_lines_locations_version_number_key), 0)
            if (newVersion != oldVersion) {
                val doc = ParseQuery.getQuery<ParseObject>(ctx.getString(R.string.parse_bus_lines_locations_collection))
                    .whereEqualTo("version", newVersion)
                    .first
                updateLinesLocations(doc, LineType.BUS)
                updateVersion(ctx.getString(R.string.saved_bus_lines_locations_version_number_key), newVersion)
            }

            // Tram lines locations
            newVersion = versions.getInt(ctx.getString(R.string.parse_tram_lines_locations_collection))
            oldVersion = sharedPreferences.getInt(ctx.getString(R.string.saved_tram_lines_locations_version_number_key), 0)
            if (newVersion != oldVersion) {
                val doc = ParseQuery.getQuery<ParseObject>(ctx.getString(R.string.parse_tram_lines_locations_collection))
                    .whereEqualTo("version", newVersion)
                    .first
                updateLinesLocations(doc, LineType.TRAM)
                updateVersion(ctx.getString(R.string.saved_tram_lines_locations_version_number_key), newVersion)
            }

            // Rural lines locations
            newVersion = versions.getInt(ctx.getString(R.string.parse_rural_lines_locations_collection))
            oldVersion = sharedPreferences.getInt(ctx.getString(R.string.saved_rural_lines_locations_version_number_key), 0)
            if (newVersion != oldVersion) {
                val doc = ParseQuery.getQuery<ParseObject>(ctx.getString(R.string.parse_rural_lines_locations_collection))
                    .whereEqualTo("version", newVersion)
                    .first
                updateLinesLocations(doc, LineType.RURAL)
                updateVersion(ctx.getString(R.string.saved_rural_lines_locations_version_number_key), newVersion)
            }

            // Changelog
            newVersion = versions.getInt(ctx.getString(R.string.parse_changelog_collection))
            oldVersion = sharedPreferences.getInt(ctx.getString(R.string.saved_changelog_version_number_key), 0)
            if (newVersion != oldVersion) {
                val doc = ParseQuery.getQuery<ParseObject>(ctx.getString(R.string.parse_changelog_collection))
                    .whereEqualTo("version", newVersion)
                    .first
                updateChangelog(doc)
                updateVersion(ctx.getString(R.string.saved_rural_lines_locations_version_number_key), newVersion)
                showUpdateNotification()
            }
        }
        Result.success()
    }

    private fun updateVersion(key: String, newVersion: Int) {
        with(sharedPreferences.edit()) {
            putInt(key, newVersion)
            apply()
        }
    }

    private fun updateStops(stopsDocument: ParseObject, stopType: StopType) {
        val stopEntities = mutableListOf<Stop>()
        val stopsJson = checkNotNull(stopsDocument.getJSONArray("stops"))
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
                stopType,
                stopJson.getString("id"),
                stopJson.getString("number"),
                stopJson.getString("title"),
                checkNotNull(location),
                lines,
                false
            )
            stopEntities.add(stopEntity)
        }
        runBlocking(Dispatchers.IO) {
            stopsDao.updateStops(stopEntities, stopType)
        }
    }

    private fun updateLines(linesDocument: ParseObject, lineType: LineType) {
        val linesEntities = mutableListOf<Line>()
        val linesJson = checkNotNull(linesDocument.getJSONArray("lines"))
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
                    if (getString("parentId").isEmpty()) null else getString("parentId"),
                    lineType,
                    getString("nameES"),
                    getString("nameEN"),
                    destinations,
                    stopsFirstDestination,
                    stopsSecondDestination
                )
                linesEntities.add(lineEntity)
            }
        }
        runBlocking(Dispatchers.IO) {
            stopsDao.updateLines(linesEntities, lineType)
        }
    }

    private suspend fun updateLinesLocations(linesLocationsDocument: ParseObject, lineType: LineType) {
        val linesLocationsEntities = mutableListOf<LineLocation>()
        val linesLocationsJson = checkNotNull(linesLocationsDocument.getJSONArray("lines"))
        for (i in 0 until linesLocationsJson.length()) {
            linesLocationsJson.getJSONObject(i).let { line ->
                val locationsJson = line.getJSONArray("coordinates")
                for (j in 0 until locationsJson.length()) {
                    locationsJson.getJSONArray(j).let { location ->
                        val lineLocationEntity = LineLocation(
                            line.getString("id"),
                            lineType,
                            j + 1,
                            LatLng(location[1] as Double, location[0] as Double)
                        )
                        linesLocationsEntities.add(lineLocationEntity)
                    }
                }
            }
        }
        stopsDao.updateLinesLocations(linesLocationsEntities, lineType)
    }

    private fun updateChangelog(changelogDocument: ParseObject) {
        with(PreferenceManager.getDefaultSharedPreferences(ctx).edit()) {
            putString(ctx.getString(R.string.saved_changelog_en_key), changelogDocument.getString("textEN"))
            putString(ctx.getString(R.string.saved_changelog_es_key), changelogDocument.getString("textES"))
            apply()
        }
    }

    private fun showUpdateNotification() {
        //NavDeepLinkBuilder doesn't work with bottom navigation view navigation so let's create the deep link normally
        val pendingIntent = PendingIntent.getActivity(
            ctx,
            -50,
            createChangelogDeepLink(),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val updateNotification = NotificationCompat.Builder(
            ctx,
            ctx.getString(R.string.notification_channel_id_updates)
        ).apply {
            setSmallIcon(R.drawable.ic_notification_icon)
            setContentTitle(ctx.getString(R.string.updated_stops_lines_title))
            setContentText(ctx.getString(R.string.updated_stops_lines_message))
            setContentIntent(pendingIntent)
            // If the notification is clicked the app opens into the stop details so notification is deleted
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_LOW
            setOnlyAlertOnce(true)
        }

        (ctx.getSystemService(LifecycleService.NOTIFICATION_SERVICE) as NotificationManager).notify(
            -50,
            updateNotification.build()
        )
    }
}