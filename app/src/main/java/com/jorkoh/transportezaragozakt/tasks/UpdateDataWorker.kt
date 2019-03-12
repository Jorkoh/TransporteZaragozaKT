package com.jorkoh.transportezaragozakt.tasks

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.jorkoh.transportezaragozakt.R
import com.parse.ParseObject
import com.parse.ParseQuery
import java.util.concurrent.TimeUnit

class UpdateDataWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    companion object {
        @JvmStatic
        fun enqueueWorker() {
            //TODO: Re-add the constraints
            val constraints = Constraints.Builder()
//                .setRequiresCharging(true)
//                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            //TODO: This should be changed to 1, TimeUnit.DAYS
            val updateDataRequest = PeriodicWorkRequestBuilder<UpdateDataWorker>(20, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance().enqueue(updateDataRequest)
        }
    }

    override fun doWork(): Result {
        Log.d("TestingStuff", "DO WORK FIRED!!")

        // Do the work here
        testWork()

        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }

    //TODO: Replace this with the actual work
    private fun testWork() {
        //TODO: Check that version is different!!

        //If it is query it
        //TODO: Make this string a resource? or a constant?
        with(ParseQuery.getQuery<ParseObject>("bus_stops")) {
            orderByDescending("version")
            limit = 1
            getFirstInBackground { parseObject, e ->
                if (e == null) {
                    //TODO: What should we do here?
                    Log.d("Testing stuff", "Retrieving new stops failed")
                }


                val notificationManager: NotificationManager =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val builder = NotificationCompat.Builder(applicationContext, "TestingStuff")
                    .setSmallIcon(R.drawable.ic_bus)
                    .setContentTitle("doWork() launched")
                    .setContentText("Result: ${parseObject.getString("_id")}")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                notificationManager.notify(1, builder.build())
            }
        }
    }
}