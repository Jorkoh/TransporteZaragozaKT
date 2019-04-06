package com.jorkoh.transportezaragozakt.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.repositories.util.SharedPreferencesLiveData
import com.jorkoh.transportezaragozakt.repositories.util.booleanLiveData
import com.jorkoh.transportezaragozakt.repositories.util.intLiveData

interface SettingsRepository {
    fun loadMapType(): LiveData<Int>
    fun setMapType(mapType: Int)
    fun loadBusFilterEnabled() : LiveData<Boolean>
    fun setBusFilterEnabled(enabled: Boolean)
    fun loadTramFilterEnabled() : LiveData<Boolean>
    fun setTramFilterEnabled(enabled: Boolean)
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

    override fun loadBusFilterEnabled() : LiveData<Boolean>{
        return sharedPreferences.booleanLiveData(
            context.getString(com.jorkoh.transportezaragozakt.R.string.bus_filter_key),
            true
        )
    }


    override fun setBusFilterEnabled(enabled: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(
                context.getString(com.jorkoh.transportezaragozakt.R.string.bus_filter_key),
                enabled
            )
            apply()
        }
    }

    override fun loadTramFilterEnabled() : LiveData<Boolean>{
        return sharedPreferences.booleanLiveData(
            context.getString(com.jorkoh.transportezaragozakt.R.string.tram_filter_key),
            true
        )
    }

    override fun setTramFilterEnabled(enabled: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(
                context.getString(com.jorkoh.transportezaragozakt.R.string.tram_filter_key),
                enabled
            )
            apply()
        }
    }
}