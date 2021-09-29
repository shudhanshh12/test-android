package `in`.okcredit.merchant.device

import io.reactivex.Completable
import io.reactivex.Single

interface DeviceRemoteSource {

    fun createOrUpdateDeviceSingle(device: Device): Completable

    fun getIpAddressData(): Single<IpAddressData>
}
