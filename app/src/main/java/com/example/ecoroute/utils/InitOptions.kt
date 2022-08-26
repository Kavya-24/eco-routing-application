package com.example.ecoroute.utils

import com.example.ecoroute.models.EVCar
import com.example.ecoroute.models.StreetStyle

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


    fun getMapStyleSource(): StreetStyle {


        val layerDefinitions = mutableListOf<StreetStyle.Layer>()


        layerDefinitions.add(
            StreetStyle.Layer(
                id = "admin",
                source = "mapbox-streets",
                sourceLayer = "admin",
                type = "line",
                paint = StreetStyle.Layer.Paint(
                    lineColor = "#ffffff",
                    circleColor = null,
                    circleRadius = null,
                    circleStrokeColor = null,
                    circleStrokeWidth = null,
                    fillColor = null,
                    fillOutlineColor = null
                )
            )
        )


        layerDefinitions.add(
            StreetStyle.Layer(
                id = "aeroway",
                source = "mapbox-streets",
                sourceLayer = "aeroway",
                type = "line",
                paint = StreetStyle.Layer.Paint(
                    lineColor = "#ffffff",
                    circleColor = null,
                    circleRadius = null,
                    circleStrokeColor = null,
                    circleStrokeWidth = null,
                    fillColor = null,
                    fillOutlineColor = null
                )
            )
        )

        layerDefinitions.add(
            StreetStyle.Layer(
                id = "airport_label",
                source = "mapbox-streets",
                sourceLayer = "airport_label",
                type = "circle",
                paint = StreetStyle.Layer.Paint(
                    lineColor = null,
                    circleColor = "rgba(238,78,139, 0.4)",
                    circleStrokeColor = "rgba(238,78,139, 1)",
                    circleRadius = null,
                    circleStrokeWidth = null,
                    fillColor = null,
                    fillOutlineColor = null
                )
            )
        )

        layerDefinitions.add(
            StreetStyle.Layer(
                id = "housenum_label",
                source = "mapbox-streets",
                sourceLayer = "housenum_label",
                type = "circle",
                paint = StreetStyle.Layer.Paint(
                    lineColor = null,
                    circleRadius = 3,
                    circleColor = "rgba(238,78,139, 0.4)",
                    circleStrokeColor = "rgba(238,78,139, 1)",
                    circleStrokeWidth = 1,
                    fillColor = null,
                    fillOutlineColor = null
                )
            )
        )


        layerDefinitions.add(
            StreetStyle.Layer(
                id = "landuse_overlay",
                source = "mapbox-streets",
                sourceLayer = "landuse_overlay",
                type = "circle",
                paint = StreetStyle.Layer.Paint(
                    lineColor = null,
                    circleRadius = null,
                    circleColor = null,
                    circleStrokeColor = null,
                    circleStrokeWidth = null,
                    fillColor = "rgba(66,100,251, 0.3)",
                    fillOutlineColor = "rgba(66,100,251, 1)"
                )
            )
        )


        layerDefinitions.add(
            StreetStyle.Layer(
                id = "building",
                source = "mapbox-streets",
                sourceLayer = "building",
                type = "fill",
                paint = StreetStyle.Layer.Paint(
                    lineColor = null,
                    circleColor = null,
                    circleRadius = null,
                    circleStrokeColor = null,
                    circleStrokeWidth = null,
                    fillColor = "rgba(66,100,251, 0.3)",
                    fillOutlineColor = "rgba(66,100,251, 1)"
                )
            )
        )

        return StreetStyle(
            version = 8, name = "Mapbox Streets tileset v8", sources = StreetStyle.Sources(
                StreetStyle.Sources.MapboxStreets(
                    type = "vector",
                    url = "mapbox=//mapbox.mapbox-streets-v8"
                )
            ), layers = layerDefinitions
        )
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