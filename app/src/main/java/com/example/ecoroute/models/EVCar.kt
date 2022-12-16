package com.example.ecoroute.models

data class EVCar(
    val carId: Int,
    val carName: String,
    var carAge: String,
    val carType: CarType,

    ) {
    data class CarType(
        var plugType: String,
        val carEffectFactor: Int,
        var carImageUrl: String,
        var chargingSpeed: Double      //Time in hours needed for charging full to empty
    )

}
