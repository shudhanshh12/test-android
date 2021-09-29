package `in`.okcredit.merchant.device.server

import `in`.okcredit.merchant.device.IpAddressData
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Url

interface DeviceApiClient {

    @PUT("devices")
    fun createOrUpdateDeviceSingle(@Body req: ApiMessages.CreateOrUpdateDeviceRequest): Single<Response<ApiMessages.Device>>

    @GET
    fun getIpAddressData(@Url url: String): Single<Response<IpAddressData>>
}
