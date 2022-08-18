package com.example.ecoroute.models

data class EVCar(
    val carId: Int,
    val name: String,
    val imageUrl: String?,
    val carType: CarType,
    val plugType: String,
    val chargingSpeed: Double      //Time in hours needed for charging full to empty

) {
    data class CarType(
        val carTypeIdentifier: Int,
        val carTypeName: String
    )


}
