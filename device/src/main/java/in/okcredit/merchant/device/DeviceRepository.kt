package `in`.okcredit.merchant.device

import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.Interceptor

interface DeviceRepository {
    @Deprecated(message = "Use getDevice() method instead")
    val deviceDeprecated: Device
    val interceptor: Interceptor

    fun isDeviceReady(): Observable<Boolean>

    fun addReferrer(referrer: Referrer)

    fun setFcmToken(fcmToken: String)

    fun getReferrals(): Single<List<Referrer>>

    fun getDevice(): Observable<Device>

    suspend fun getIpRegion(): String
}
