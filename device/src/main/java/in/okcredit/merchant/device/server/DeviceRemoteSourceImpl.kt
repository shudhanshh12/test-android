package `in`.okcredit.merchant.device.server

import `in`.okcredit.merchant.device.Device
import `in`.okcredit.merchant.device.DeviceRemoteSource
import `in`.okcredit.merchant.device.IpAddressData
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import javax.inject.Inject

class DeviceRemoteSourceImpl @Inject constructor(private val deviceApiClient: Lazy<DeviceApiClient>) : DeviceRemoteSource {

    override fun createOrUpdateDeviceSingle(device: Device): Completable {
        return deviceApiClient.get().createOrUpdateDeviceSingle(ApiMessages.CreateOrUpdateDeviceRequest(device.toApiModel()))
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .flatMapCompletable {
                if (it.isSuccessful) {
                    return@flatMapCompletable Completable.complete()
                } else {
                    val error = it.asError()
                    throw error
                }
            }
    }

    override fun getIpAddressData(): Single<IpAddressData> {
        return deviceApiClient.get().getIpAddressData(IP_LOOKUP_URL)
            .map {
                if (it.isSuccessful) {
                    return@map it.body() as IpAddressData
                } else {
                    throw it.asError()
                }
            }
            .subscribeOn(Schedulers.io())
    }

    companion object {
        // Contact Saket for changes to this
        const val IP_LOOKUP_URL =
            "https://api.ipdata.co/?api-key=58bab55ef703bc8a92776eb15e6f6d78324d1752d2a316b6f87792d7"
    }
}
