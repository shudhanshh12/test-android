package `in`.okcredit.installedpackges.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface InstalledPackagesApiClient {
    @GET("merchant_report/{merchant_id}/package_list")
    suspend fun getPackageListForTracking(
        @Path("merchant_id") merchantId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): PackageInfoResponse

    @PUT("merchant_report/{merchant_id}")
    suspend fun updatePackagesStatus(
        @Path("merchant_id") merchantId: String,
        @Body updatedPackagesRequestBody: UpdatedPackagesRequestBody,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): PackageUpdateResponse
}

@JsonClass(generateAdapter = true)
data class PackageInfo(
    @Json(name = "id")
    val packageId: String = "",
    @Json(name = "package_name")
    val packageName: String = "",
)

@JsonClass(generateAdapter = true)
data class UpdatedPackagesRequestBody(
    @Json(name = "report")
    val report: List<PackageReport>,
)

@JsonClass(generateAdapter = true)
data class PackageReport(
    @Json(name = "package_id")
    val packageId: String = "",
    @Json(name = "package_name")
    val packageName: String = "",
    @Json(name = "is_installed")
    val isInstalled: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class PackageUpdateResponse(
    @Json(name = "status")
    val status: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class PackageInfoResponse(
    @Json(name = "last_tracked")
    val lastTracked: String? = null,
    @Json(name = "packages")
    val packages: List<PackageInfo> = arrayListOf(),
)
