package com.example.ecoroute.models.responses

data class ElevationAPIResponse(
    val dataSet: DataSet,
    val geoPoints: List<GeoPoint>,
    val message: String,
    val metrics: Metrics,
    val resultCount: Int,
    val statusCode: Int
) {
    data class DataSet(
        val attribution: Attribution,
        val dataSource: DataSource,
        val description: String,
        val extentInfo: String,
        val fileFormat: FileFormat,
        val isListed: Boolean,
        val name: String,
        val noDataValue: Int,
        val pointsPerDegree: Int,
        val publicUrl: String,
        val resolutionArcSeconds: Double,
        val resolutionMeters: Double,
        val srid: Int
    ) {
        data class Attribution(
            val acknowledgement: String,
            val subject: String,
            val text: String,
            val url: String
        )

        data class DataSource(
            val dataSourceType: Int,
            val indexFilePath: String
        )

        data class FileFormat(
            val fileExtension: String,
            val name: String,
            val registration: Int,
            val type: Int
        )
    }

    data class GeoPoint(
        val distanceFromOriginMeters: Double,
        val elevation: Double,
        val latitude: Double,
        val longitude: Double
    )

    data class Metrics(
        val climb: Double,
        val descent: Double,
        val distance: Double,
        val hasVoids: Boolean,
        val intervisible: Boolean,
        val maxElevation: Double,
        val minElevation: Double,
        val numPoints: Int,
        val numVoidPoints: Int,
        val obstacles: List<Any>
    )
}