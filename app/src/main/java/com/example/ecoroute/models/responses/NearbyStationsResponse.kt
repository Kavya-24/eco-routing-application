package com.example.ecoroute.models.responses


import com.squareup.moshi.Json

class NearbyStationsResponse : ArrayList<NearbyStationsResponse.NearbyStationsResponseItem>(){
    data class NearbyStationsResponseItem
        (
        @Json(name = "address")
        val address: Address,
        @Json(name = "chargingPark")
        val chargingPark: ChargingPark,
        @Json(name = "entryPoints")
        val entryPoints: List<EntryPoint>,
        @Json(name = "id")
        val id: String,
        @Json(name = "info")
        val info: String,
        @Json(name = "poi")
        val poi: Poi,
        @Json(name = "position")
        val position: Position,
        @Json(name = "score")
        val score: Double,
        @Json(name = "type")
        val type: String, // POI
        @Json(name = "viewport")
        val viewport: Viewport
    )

    {
        data class Address(
            @Json(name = "country")
            val country: String, // India
            @Json(name = "countryCode")
            val countryCode: String, // IN
            @Json(name = "countryCodeISO3")
            val countryCodeISO3: String, // IND
            @Json(name = "countrySecondarySubdivision")
            val countrySecondarySubdivision: String, // Darbhanga
            @Json(name = "countrySubdivision")
            val countrySubdivision: String, // Bihar
            @Json(name = "freeformAddress")
            val freeformAddress: String, // Singhiya Darbhanga Road, Near K M Tank Laheria Sarai, Mirzapur, Darbhanga 846001, Bihar
            @Json(name = "localName")
            val localName: String, // Darbhanga
            @Json(name = "municipality")
            val municipality: String, // Darbhanga
            @Json(name = "municipalitySubdivision")
            val municipalitySubdivision: String, // Mirzapur
            @Json(name = "postalCode")
            val postalCode: String, // 846001
            @Json(name = "streetName")
            val streetName: String // Singhiya Darbhanga Road, Near K M Tank Laheria Sarai
        )
    
        data class ChargingPark(
            @Json(name = "connectors")
            val connectors: List<Connector>
        ) {
            data class Connector(
                @Json(name = "connectorType")
                val connectorType: String // IEC60309AC1PhaseBlue
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
                val lat: Double, // 26.11509
                @Json(name = "lon")
                val lon: Double // 85.89764
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
            val name: String // Electrify Bihar EV Charging Station
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
            val lat: Double, // 26.11505
            @Json(name = "lon")
            val lon: Double // 85.89765
        )
    
        data class Viewport(
            @Json(name = "btmRightPoint")
            val btmRightPoint: BtmRightPoint,
            @Json(name = "topLeftPoint")
            val topLeftPoint: TopLeftPoint
        ) {
            data class BtmRightPoint(
                @Json(name = "lat")
                val lat: Double, // 26.11415
                @Json(name = "lon")
                val lon: Double // 85.89865
            )
    
            data class TopLeftPoint(
                @Json(name = "lat")
                val lat: Double, // 26.11595
                @Json(name = "lon")
                val lon: Double // 85.89665
            )
        }
    }
}