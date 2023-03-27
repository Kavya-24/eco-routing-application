package com.example.ecoroute.models

data class EVCar(

    var carName: String,

    //config
    var carAge: Int,                 //in days
    var carMileage: Int,                //in miles on ful charge
    var carBatterCapacity: Int,          //in kwh
    //charger
    var carConnector: String,           //usable string literal
    var carChargerType: String        //enum {fast, normal, slow}

) {}
