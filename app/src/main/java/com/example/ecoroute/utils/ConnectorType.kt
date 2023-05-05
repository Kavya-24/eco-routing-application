package com.example.ecoroute.utils

import androidx.annotation.Keep

@Keep
object ConnectorType {

    fun extarct_suitable_connector(): MutableList<String> {
        return mutableListOf<String>(
            "GBT20234Part2",
            "IEC62196Type3",
            "IEC62196Type2CableAttached",
            "IEC62196Type2CCS",
            "IEC60309DCWhite",
            "IEC62196Type1",
            "IEC62196Type2Outlet",
            "StandardHouseholdCountrySpecific",
            "Chademo",
            "GBT20234Part3",
            "IEC60309AC1PhaseBlue",
            "IEC62196Type1CCS"
        )
    }
}