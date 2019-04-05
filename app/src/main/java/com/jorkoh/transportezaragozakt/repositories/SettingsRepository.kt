package com.jorkoh.transportezaragozakt.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.repositories.util.SharedPreferencesLiveData
import com.jorkoh.transportezaragozakt.repositories.util.intLiveData

interface SettingsRepository {
    fun loadMapType(): LiveData<Int>
    fun setMapType(mapType: Int)
}

class SettingsRepositoryImplementation(
    private val sharedPreferences: SharedPreferences,
    private val context: Context
) : SettingsRepository {

    override fun loadMapType(): LiveData<Int> {
        return sharedPreferences.intLiveData(
            context.getString(com.jorkoh.transportezaragozakt.R.string.map_type_key),
            1
        )
    }

    override fun setMapType(mapType: Int) {
        with(sharedPreferences.edit()) {
            putInt(
                context.getString(com.jorkoh.transportezaragozakt.R.string.map_type_key),
                mapType
            )
            apply()
        }
    }
}