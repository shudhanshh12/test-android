package `in`.okcredit.merchant.device.store

import `in`.okcredit.merchant.device.Device
import `in`.okcredit.merchant.device.DeviceLocalSource
import `in`.okcredit.merchant.device.DeviceRepositoryImpl.Companion.TAG
import `in`.okcredit.merchant.device.DeviceUtils
import `in`.okcredit.shared.service.keyval.KeyValService
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.json.JSONObject
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_DEPRECATED_DEVICE
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_DEVICE
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_IP_REGION
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.GsonUtil
import tech.okcredit.android.base.utils.ThreadUtils
import timber.log.Timber
import javax.inject.Inject

class DeviceLocalSourceImpl @Inject constructor(
    private val keyValService: Lazy<KeyValService>
) : DeviceLocalSource {

    override fun isDevicePresent(): Single<Boolean> {
        return keyValService.get().contains(PREF_INDIVIDUAL_DEVICE, Scope.Individual)
            .subscribeOn(ThreadUtils.database())
    }

    override fun getDevice(): Observable<Device> {
        return keyValService.get()[PREF_INDIVIDUAL_DEVICE, Scope.Individual]
            .filter { it.isNotBlank() }
            .map {
                GsonUtil.getGson().fromJson(it, Device::class.java)
            }.subscribeOn(ThreadUtils.database())
    }

    override fun putDevice(device: Device): Completable {
        Timber.d("$TAG Store putDevice Device=$device")
        val deviceString = GsonUtil.getGson().toJson(device)
        return keyValService.get()
            .put(PREF_INDIVIDUAL_DEVICE, deviceString, Scope.Individual)
            .subscribeOn(ThreadUtils.database())
    }

    override fun getDeprecatedDeviceId(): Single<String> {
        Timber.d("$TAG Store getDeprecatedDeviceId Device")
        return keyValService.get().get(PREF_INDIVIDUAL_DEPRECATED_DEVICE, Scope.Individual).firstOrError().flatMap {
            // Safety Check
            try {
                val deviceString = JSONObject(it).getString("id")
                return@flatMap Single.just(deviceString)
            } catch (e: Exception) {
                return@flatMap Single.just(DeviceUtils.createDeviceId())
            }
        }.subscribeOn(ThreadUtils.database())
    }

    override fun checkDeprecatedDevicePresent(): Single<Boolean> {
        return keyValService.get().contains(PREF_INDIVIDUAL_DEPRECATED_DEVICE, Scope.Individual).doOnSuccess {
            Timber.e("$TAG Existing Device Available=$it")
        }.subscribeOn(ThreadUtils.database())
    }

    override fun getIsIpRegionSynced(): Single<Boolean> {
        return keyValService.get().contains(PREF_INDIVIDUAL_IP_REGION, Scope.Individual).doOnSuccess {
            Timber.e("$TAG IS IP REGION SYNCED=$it")
        }.subscribeOn(ThreadUtils.database())
    }

    override fun getIpRegion(): Single<String> {
        return keyValService.get().get(PREF_INDIVIDUAL_IP_REGION, Scope.Individual).first("").doOnSuccess {
            Timber.e("$TAG IP REGION=$it")
        }.subscribeOn(ThreadUtils.database())
    }

    override fun setIpRegion(value: String): Completable {
        return keyValService.get().put(PREF_INDIVIDUAL_IP_REGION, value, Scope.Individual).doOnComplete {
            Timber.e("$TAG Set IP REGION=$value")
        }.subscribeOn(ThreadUtils.database())
    }
}
