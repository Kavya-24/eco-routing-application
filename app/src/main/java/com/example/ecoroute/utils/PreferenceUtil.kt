package com.example.ecoroute.utils


import android.preference.PreferenceManager
import com.example.ecoroute.models.EVCar

/**
 * PreferenceUtil to store the Car of the User Locally
 */

object PreferenceUtil {


    private val pm = PreferenceManager.getDefaultSharedPreferences(ApplicationUtils.instance)

    private val CARS = mutableListOf<EVCar>()
    private val LAST_USED_CAR_IDX = ""


    var _lastUsedCarIndex: String?
        get() = pm.getString(LAST_USED_CAR_IDX, "")
        set(value) {
            pm.edit().putString(LAST_USED_CAR_IDX, value).apply()
        }

    fun getCarsFromPreference(): MutableList<EVCar> {
        val pref = PreferenceUtil
        return pref.CARS
    }

    fun setCarsInPreference(_car: EVCar) {
        val pref = PreferenceUtil
        pref.CARS.add(_car)
    }


    fun clearPrefData() {
        pm.edit().clear().apply()
    }


}