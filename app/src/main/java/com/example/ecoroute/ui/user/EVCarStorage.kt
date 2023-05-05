package com.example.ecoroute.ui.user

import com.squareup.moshi.Moshi
import android.content.Context
import androidx.annotation.Keep
import com.example.ecoroute.models.EVCar
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types

@Keep
object EVCarStorage {
    private const val SHARED_PREFS_NAME = "ev_cars_prefs"
    private const val EV_CARS_KEY = "ev_cars_key"


    private val moshi = Moshi.Builder().build()
    private val evCarListType = Types.newParameterizedType(List::class.java, EVCar::class.java)
    private val evCarListAdapter: JsonAdapter<List<EVCar>> = moshi.adapter(evCarListType)

    fun saveCar(context: Context, car: EVCar) {
        val sharedPreferences =
            context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val savedCarsJson = sharedPreferences.getString(EV_CARS_KEY, "[]")!!
        val savedCars = evCarListAdapter.fromJson(savedCarsJson) ?: emptyList()

        val newSavedCars = savedCars.toMutableList().apply { add(car) }
        val newSavedCarsJson = evCarListAdapter.toJson(newSavedCars)

        sharedPreferences.edit().putString(EV_CARS_KEY, newSavedCarsJson).apply()
    }

    fun getCars(context: Context): List<EVCar> {
        val sharedPreferences =
            context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val savedCarsJson = sharedPreferences.getString(EV_CARS_KEY, "[]")!!
        return evCarListAdapter.fromJson(savedCarsJson) ?: emptyList()
    }

    fun editCars(context: Context, carList: List<EVCar>) {
        val sharedPreferences =
            context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter<List<EVCar>>(
            Types.newParameterizedType(
                List::class.java,
                EVCar::class.java
            )
        )
        val carListJson = adapter.toJson(carList)
        editor.putString(EV_CARS_KEY, carListJson)
        editor.apply()

    }

    fun selectCar(position: Int, context: Context) {
        val sharedPreferences =
            context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putInt("current_position", position) // Replace 5 with your desired integer
        editor?.apply()
    }

    fun getCurrentCarPosition(context: Context): Int {
        val sharedPreferences =
            context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getInt("current_position", 0) // Replace 0 with your default value
    }
}
