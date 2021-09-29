package tech.okcredit.android.ab.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AbApiClient {

    // Pre-login
    @GET("v2/GetDeviceProfile")
    fun getDeviceProfile(
        @Query("device_id") deviceId: String,
        @Header("X-App-Source") source: String,
        @Header("X-App-Source-Type") sourceType: String
    ): Single<Response<GetProfileResponse>>

    // Pre-login
    @POST("v2/DeviceAck")
    fun deviceAcknowledge(
        @Body req: AcknowledgementRequest,
        @Header("X-App-Source") source: String,
        @Header("X-App-Source-Type") sourceType: String
    ): Completable

    // Post-login
    @GET("v2/GetProfile")
    fun getProfile(
        @Query("device_id") deviceId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
        @Header("X-App-Source") source: String,
        @Header("X-App-Source-Type") sourceType: String
    ): Single<Response<GetProfileResponse>>

    // Post-login
    @POST("v2/Ack")
    fun acknowledge(
        @Body req: AcknowledgementRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
        @Header("X-App-Source") source: String,
        @Header("X-App-Source-Type") sourceType: String
    ): Completable
}
