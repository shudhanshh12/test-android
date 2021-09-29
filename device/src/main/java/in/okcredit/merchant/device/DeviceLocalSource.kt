package `in`.okcredit.merchant.device

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DeviceLocalSource {

    fun isDevicePresent(): Single<Boolean>

    fun getDevice(): Observable<Device>

    fun putDevice(device: Device): Completable

    // Get older device(V1) id.
    fun getDeprecatedDeviceId(): Single<String>

    // Check older device present.
    fun checkDeprecatedDevicePresent(): Single<Boolean>

    fun getIsIpRegionSynced(): Single<Boolean>

    fun getIpRegion(): Single<String>

    fun setIpRegion(value: String): Completable
}
