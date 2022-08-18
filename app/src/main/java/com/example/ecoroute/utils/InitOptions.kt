package com.example.ecoroute.utils

import com.example.ecoroute.models.EVCar

object InitOptions {


    fun getEVCarsList(): MutableList<EVCar> {
        val lst: MutableList<EVCar> = mutableListOf()

        lst.add(
            EVCar(
                carId = 0,
                name = "Honda",
                imageUrl = null,
                carType = EVCar.CarType(0, "EV"),
                plugType = "Type 2",
                chargingSpeed = 8.0
            )
        )


        lst.add(
            EVCar(
                carId = 1,
                name = "Tesla",
                imageUrl = null,
                carType = EVCar.CarType(0, "EV"),
                plugType = "Type 1",
                chargingSpeed = 4.0
            )
        )

        return lst
    }


    fun getRandomNodeWeightsForMapping(): MutableMap.MutableEntry<String, Pair<Double, Double>> {

        val nameWeightMap = getNodeWeightMappings()
        val s = nameWeightMap.size
        val rnd = (0 until s).random()
        return nameWeightMap.entries.elementAt(rnd)
    }


    private fun getNodeWeightMappings(): MutableMap<String, Pair<Double, Double>> {
        val nameWeight: MutableMap<String, Pair<Double, Double>> = mutableMapOf()

        nameWeight.put("SCHOOL", Pair(getRandomWeightMappings(), getRandomTimeMappings()))
        nameWeight.put("HOSPITAL", Pair(getRandomWeightMappings(), getRandomTimeMappings()))
        nameWeight.put("AIRPORT", Pair(getRandomWeightMappings(), getRandomTimeMappings()))
        nameWeight.put("RAILWAY_STATION", Pair(getRandomWeightMappings(), getRandomTimeMappings()))
        nameWeight.put("OFFICE_BUILDING", Pair(getRandomWeightMappings(), getRandomTimeMappings()))
        return nameWeight
    }

    //Weight for nodes can be anywhere between 1-10
    private fun getRandomWeightMappings(): Double {
        return (0..5).random().toDouble()
    }

    //Time for nodes can be anywhere between 1-10 minutes = 60 to 600 s
    private fun getRandomTimeMappings(): Double {
        return (60..120).random().toDouble()
    }


}