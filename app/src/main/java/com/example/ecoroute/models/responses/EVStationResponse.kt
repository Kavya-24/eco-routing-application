package com.example.ecoroute.models.responses


import androidx.annotation.Keep
import com.squareup.moshi.Json
@Keep
data class EVStationResponse(
    @Json(name = "results")
    val results: List<Result>,
    @Json(name = "summary")
    val summary: Summary
) {
    data class Result(
        @Json(name = "address")
        val address: Address,
        @Json(name = "chargingPark")
        val chargingPark: ChargingPark,
        @Json(name = "dist")
        val dist: Double, // 3106.491590289694
        @Json(name = "entryPoints")
        val entryPoints: List<EntryPoint>,
        @Json(name = "id")
        val id: String, // 356009046348259
        @Json(name = "info")
        val info: String, // search:ev:356009046348259
        @Json(name = "poi")
        val poi: Poi,
        @Json(name = "position")
        val position: Position,
        @Json(name = "score")
        val score: Double, // 5.6571307182
        @Json(name = "type")
        val type: String, // POI
        @Json(name = "viewport")
        val viewport: Viewport
    ) {
        data class Address(
            @Json(name = "country")
            val country: String, // India
            @Json(name = "countryCode")
            val countryCode: String, // IN
            @Json(name = "countryCodeISO3")
            val countryCodeISO3: String, // IND
            @Json(name = "countrySecondarySubdivision")
            val countrySecondarySubdivision: String, // Gautam Buddha Nagar
            @Json(name = "countrySubdivision")
            val countrySubdivision: String, // Uttar Pradesh
            @Json(name = "freeformAddress")
            val freeformAddress: String, // Industrial Area, Surajpur Site 4, Industrial Area Block G, Greater Noida 201310, Uttar Pradesh
            @Json(name = "localName")
            val localName: String, // Greater Noida
            @Json(name = "municipality")
            val municipality: String, // Sadar
            @Json(name = "municipalitySubdivision")
            val municipalitySubdivision: String, // Industrial Area Block G
            @Json(name = "postalCode")
            val postalCode: String, // 201310
            @Json(name = "streetName")
            val streetName: String, // Industrial Area, Surajpur Site 4
            @Json(name = "streetNumber")
            val streetNumber: String // A54
        )

        data class ChargingPark(
            @Json(name = "connectors")
            val connectors: List<Connector>
        ) {
            data class Connector(
                @Json(name = "connectorType")
                val connectorType: String, // IEC62196Type2Outlet
                @Json(name = "currentA")
                val currentA: Int, // 32
                @Json(name = "currentType")
                val currentType: String, // AC3
                @Json(name = "ratedPowerKW")
                val ratedPowerKW: Double, // 7.4
                @Json(name = "voltageV")
                val voltageV: Int // 400
            )
        }

        data class EntryPoint(
            @Json(name = "position")
            val position: Position,
            @Json(name = "type")
            val type: String // main
        ) {
            data class Position(
                @Json(name = "lat")
                val lat: Double, // 28.459
                @Json(name = "lon")
                val lon: Double // 77.52451
            )
        }

        data class Poi(
            @Json(name = "categories")
            val categories: List<String>,
            @Json(name = "categorySet")
            val categorySet: List<CategorySet>,
            @Json(name = "classifications")
            val classifications: List<Classification>,
            @Json(name = "name")
            val name: String // Sagar Motors Electric Vehicle Charging Station
        ) {
            data class CategorySet(
                @Json(name = "id")
                val id: Int // 7309
            )

            data class Classification(
                @Json(name = "code")
                val code: String, // ELECTRIC_VEHICLE_STATION
                @Json(name = "names")
                val names: List<Name>
            ) {
                data class Name(
                    @Json(name = "name")
                    val name: String, // electric vehicle station
                    @Json(name = "nameLocale")
                    val nameLocale: String // en-US
                )
            }
        }

        data class Position(
            @Json(name = "lat")
            val lat: Double, // 28.45891
            @Json(name = "lon")
            val lon: Double // 77.52459
        )

        data class Viewport(
            @Json(name = "btmRightPoint")
            val btmRightPoint: BtmRightPoint,
            @Json(name = "topLeftPoint")
            val topLeftPoint: TopLeftPoint
        ) {
            data class BtmRightPoint(
                @Json(name = "lat")
                val lat: Double, // 28.45801
                @Json(name = "lon")
                val lon: Double // 77.52561
            )

            data class TopLeftPoint(
                @Json(name = "lat")
                val lat: Double, // 28.45981
                @Json(name = "lon")
                val lon: Double // 77.52357
            )
        }
    }

    data class Summary(
        @Json(name = "fuzzyLevel")
        val fuzzyLevel: Int, // 1
        @Json(name = "geoBias")
        val geoBias: GeoBias,
        @Json(name = "numResults")
        val numResults: Int, // 50
        @Json(name = "offset")
        val offset: Int, // 0
        @Json(name = "query")
        val query: String, // charging station
        @Json(name = "queryTime")
        val queryTime: Int, // 125
        @Json(name = "queryType")
        val queryType: String, // NON_NEAR
        @Json(name = "totalResults")
        val totalResults: Int // 934
    ) {
        data class GeoBias(
            @Json(name = "lat")
            val lat: Double, // 28.4815688
            @Json(name = "lon")
            val lon: Double // 77.5059994
        )
    }
}