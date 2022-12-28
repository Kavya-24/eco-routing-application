package com.example.ecoroute.utils

import com.example.ecoroute.models.EVCar

object InitOptions {

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


    //Get the EV Car List with some default values
    fun getEVCarsList(): MutableList<EVCar> {

        val cars = mutableListOf<EVCar>()


        cars.add(
            EVCar(
                0, "Tesla", "1", EVCar.CarType(plugType = "A", 1, "tesla", chargingSpeed = 100.0)
            )
        )

        cars.add(
            EVCar(
                1, "Ola", "1", EVCar.CarType(plugType = "A", 2, "ola", chargingSpeed = 95.0)
            )
        )


        cars.add(
            EVCar(
                2, "Altroz", "1", EVCar.CarType(plugType = "B", 4, "altroz", chargingSpeed = 100.0)
            )
        )


        cars.add(
            EVCar(
                3, "Honda", "1", EVCar.CarType(plugType = "C", 3, "honda", chargingSpeed = 100.0)
            )
        )



        return cars
    }


}