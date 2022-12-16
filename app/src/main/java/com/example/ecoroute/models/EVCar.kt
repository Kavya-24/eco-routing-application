package com.example.ecoroute.models

data class EVCar(
    val carId: Int,
    val carName: String,
    val carImageUrl: String?,
    val carAge: String,
    val carType: CarType,
    val chargingSpeed: Double      //Time in hours needed for charging full to empty

) {
    data class CarType(
        val carTypeIdentifier: Int,
        val carTypeName: String,
        val plugType: String
    )


}
