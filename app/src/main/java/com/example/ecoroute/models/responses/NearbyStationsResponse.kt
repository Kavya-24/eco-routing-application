package com.example.ecoroute.models.responses


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
class NearbyStationsResponse : ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>() {
    data class NearbyStationsResponseItem
        (
        @field:Json(name = "address")
        val address: Address,
        @field:Json(name = "chargingPark")
        val chargingPark: ChargingPark,
        @field:Json(name = "entryPoints")
        val entryPoints: List<EntryPoint>,
        @field:Json(name = "id")
        val id: String,
        @field:Json(name = "info")
        val info: String,
        @field:Json(name = "poi")
        val poi: Poi,
        @field:Json(name = "position")
        val position: Position,
        @field:Json(name = "score")
        val score: Double,
        @field:Json(name = "type")
        val type: String, // POI
        @field:Json(name = "viewport")
        val viewport: Viewport
    ) {
        data class Address(
            @field:Json(name = "country")
            val country: String, // India
            @field:Json(name = "countryCode")
            val countryCode: String, // IN
            @field:Json(name = "countryCodeISO3")
            val countryCodeISO3: String, // IND
            @field:Json(name = "countrySecondarySubdivision")
            val countrySecondarySubdivision: String, // Darbhanga
            @field:Json(name = "countrySubdivision")
            val countrySubdivision: String, // Bihar
            @field:Json(name = "freeformAddress")
            val freeformAddress: String, // Singhiya Darbhanga Road, Near K M Tank Laheria Sarai, Mirzapur, Darbhanga 846001, Bihar
            @field:Json(name = "localName")
            val localName: String, // Darbhanga
            @field:Json(name = "municipality")
            val municipality: String, // Darbhanga
            @field:Json(name = "municipalitySubdivision")
            val municipalitySubdivision: String, // Mirzapur
            @field:Json(name = "postalCode")
            val postalCode: String, // 846001
            @field:Json(name = "streetName")
            val streetName: String // Singhiya Darbhanga Road, Near K M Tank Laheria Sarai
        )

        data class ChargingPark(
            @field:Json(name = "connectors")
            val connectors: List<Connector>
        ) {
            data class Connector(
                @field:Json(name = "connectorType")
                val connectorType: String // IEC60309AC1PhaseBlue
            )
        }

        data class EntryPoint(
            @field:Json(name = "position")
            val position: Position,
            @field:Json(name = "type")
            val type: String // main
        ) {
            data class Position(
                @field:Json(name = "lat")
                val lat: Double, // 26.11509
                @field:Json(name = "lon")
                val lon: Double // 85.89764
            )
        }

        data class Poi(
            @field:Json(name = "categories")
            val categories: List<String>,
            @field:Json(name = "categorySet")
            val categorySet: List<CategorySet>,
            @field:Json(name = "classifications")
            val classifications: List<Classification>,
            @field:Json(name = "name")
            val name: String // Electrify Bihar EV Charging Station
        ) {
            data class CategorySet(
                @field:Json(name = "id")
                val id: Int // 7309
            )

            data class Classification(
                @field:Json(name = "code")
                val code: String, // ELECTRIC_VEHICLE_STATION
                @field:Json(name = "names")
                val names: List<Name>
            ) {
                data class Name(
                    @field:Json(name = "name")
                    val name: String, // electric vehicle station
                    @field:Json(name = "nameLocale")
                    val nameLocale: String // en-US
                )
            }
        }

        data class Position(
            @field:Json(name = "lat")
            val lat: Double, // 26.11505
            @field:Json(name = "lon")
            val lon: Double // 85.89765
        )

        data class Viewport(
            @field:Json(name = "btmRightPoint")
            val btmRightPoint: BtmRightPoint,
            @field:Json(name = "topLeftPoint")
            val topLeftPoint: TopLeftPoint
        ) {
            data class BtmRightPoint(
                @field:Json(name = "lat")
                val lat: Double, // 26.11415
                @field:Json(name = "lon")
                val lon: Double // 85.89865
            )

            data class TopLeftPoint(
                @field:Json(name = "lat")
                val lat: Double, // 26.11595
                @field:Json(name = "lon")
                val lon: Double // 85.89665
            )
        }
    }
}