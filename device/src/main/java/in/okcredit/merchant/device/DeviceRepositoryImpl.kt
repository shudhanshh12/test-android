package `in`.okcredit.merchant.device

import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.SuperProperties
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.merchant.device.temp.DeviceSyncer
import android.annotation.SuppressLint
import android.content.Context
import com.scottyab.rootbeer.RootBeer
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.rx2.await
import okhttp3.Interceptor
import org.joda.time.DateTime
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.json.json
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.DateTimeUtils
import timber.log.Timber
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val deviceLocalSource: Lazy<DeviceLocalSource>,
    private val deviceSyncer: Lazy<DeviceSyncer>,
    private val deviceRemoteSource: Lazy<DeviceRemoteSource>,
    private val tracker: Lazy<Tracker>,
    private val context: Lazy<Context>,
    private val rootBeer: Lazy<RootBeer>,
    schedulerProvider: SchedulerProvider
) : DeviceRepository {

    companion object {
        const val TAG = "<<<<DeviceSDK"
        private var deviceCache: Device? = null
    }

    private val isReady = BehaviorSubject.createDefault(false)

    private fun isReady(): Observable<Boolean> = isReady.filter { it }.hide().distinctUntilChanged()

    // TODO:Trace
    init {
        // 1. create device if not exists
        // 2. update device with new field values, if they are changed (NEVER update referrer from a source once a value is received)
        // 3. schedule sync if update_time is greater than sync_time
        // 4. observe device from local db and cache it in memory

        Timber.d("$TAG init")

        syncDeviceData().subscribeOn(schedulerProvider.io()).doOnError { RecordException.recordException(it) }
            .subscribe()
    }

    private fun createNewDevice(id: String, aaid: String): Device {
        return Device(
            id,
            DeviceUtils.getVersionCode(),
            DeviceUtils.getApiLevel(),
            aaid,
            null,
            arrayListOf(),
            DateTimeUtils.currentDateTime(),
            DateTimeUtils.currentDateTime(),
            DateTime(0),
            rootBeer.get().isRooted
        )
    }

    override val deviceDeprecated: Device
        get() =
            if (deviceCache != null) {
                deviceCache!!
            } else {
                getDevice().blockingFirst()
            }

    override val interceptor: Interceptor
        get() = Interceptor { chain: Interceptor.Chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("X-DeviceId", deviceDeprecated.id)
                    .header("OKC_APP_VERSION", BuildConfig.VERSION_CODE.toString())
                    .build()
            )
        }

    override fun isDeviceReady(): Observable<Boolean> {
        return isReady
    }

    @SuppressLint("CheckResult")
    override fun addReferrer(referrer: Referrer) {
        isReady().map {
            Timber.w("$TAG addReferrer ${referrer.source}")
            if (deviceCache != null) {
                if (deviceCache?.referrers?.any { it.source == referrer.source } != true) {
                    deviceCache?.referrers?.add(referrer)
                    deviceLocalSource.get().putDevice(deviceCache?.copy(updateTime = DateTimeUtils.currentDateTime())!!)
                        .doOnComplete { Timber.d("$TAG Added referrer") }
                        .andThen(deviceSyncer.get().executeSyncDevice())
                        .blockingGet()
                }
            }
        }.subscribe()
    }

    @SuppressLint("CheckResult")
    override fun setFcmToken(fcmToken: String) {
        Timber.w("$TAG setFcmToken: $fcmToken")
        isReady().map {
            if (deviceCache != null) {
                deviceLocalSource.get()
                    .putDevice(deviceCache?.copy(fcmToken = fcmToken, updateTime = DateTimeUtils.currentDateTime())!!)
                    .doOnComplete { Timber.d("$TAG setFcmToken completed") }
                    .andThen(deviceSyncer.get().executeSyncDevice())
                    .doOnEvent {
                        tracker.get().trackEvents(
                            eventName = "NotificationData: New Token Processed",
                            propertiesMap = PropertiesMap.create().add("fcm_token", fcmToken)
                        )
                    }
                    .blockingGet()
            }
        }.subscribe()
    }

    @SuppressLint("CheckResult")
    override fun getReferrals(): Single<List<Referrer>> {
        return deviceLocalSource.get().getDevice().firstOrError().map { it.referrers }
    }

    private fun syncDeviceData(): Single<Device> {
        return deviceLocalSource.get().isDevicePresent().flatMap { isPresent ->
            Timber.d("$TAG isPresent=$isPresent")
            return@flatMap if (isPresent) {
                Completable.complete()
            } else {
                // Checking older device for migration of device ID
                deviceLocalSource.get().checkDeprecatedDevicePresent().flatMapCompletable { isOlderDevicePresent ->
                    DeviceUtils.fetchAdId(context.get()).flatMapCompletable { aaid ->
                        if (isOlderDevicePresent) {
                            deviceLocalSource.get().getDeprecatedDeviceId().flatMapCompletable {
                                deviceLocalSource.get().putDevice(createNewDevice(it, aaid))
                            }.doOnComplete { Timber.d("$TAG Created New Device with existing ID") }
                        } else {
                            deviceLocalSource.get().putDevice(createNewDevice(DeviceUtils.createDeviceId(), aaid))
                                .doOnComplete { Timber.d("$TAG Created New Device with new ID") }
                        }
                    }
                }
            }.andThen(
                deviceLocalSource.get().getDevice()
                    .firstOrError()
                    .flatMap {
                        Timber.d("$TAG getDevicegetDevice")
                        deviceCache = it
                        isReady.onNext(true)
                        Timber.i("$TAG EMITTED=${deviceCache.json()}")
                        DeviceUtils.fetchAdId(context.get()).flatMap { aaid ->
                            if (
                                deviceDeprecated.versionCode != DeviceUtils.getVersionCode() ||
                                deviceDeprecated.apiLevel != DeviceUtils.getApiLevel() ||
                                deviceDeprecated.aaid != aaid ||
                                deviceDeprecated.isRooted != rootBeer.get().isRooted
                            ) {

                                val updatedDevice = Device(
                                    deviceDeprecated.id,
                                    DeviceUtils.getVersionCode(),
                                    DeviceUtils.getApiLevel(),
                                    aaid,
                                    deviceDeprecated.fcmToken,
                                    deviceDeprecated.referrers,
                                    deviceDeprecated.createTime,
                                    DateTimeUtils.currentDateTime(),
                                    deviceDeprecated.syncTime,
                                    rootBeer.get().isRooted
                                )

                                return@flatMap deviceLocalSource.get().putDevice(updatedDevice)
                                    .andThen(Single.just(updatedDevice))
                            } else {
                                return@flatMap Single.just(deviceDeprecated)
                            }
                        }
                    }.flatMap {
                        Timber.e(
                            "$TAG updateTime=${it.updateTime.millis} syncTime=${
                            it.syncTime?.millis
                                ?: 0
                            } AVAILABLE=${it.updateTime.millis > it.syncTime?.millis ?: 0}"
                        )

                        if (it.aaid.isNullOrEmpty().not()) {
                            tracker.get().setSuperProperties(SuperProperties.AAID, it.aaid ?: "")
                        }

                        if (it.updateTime.millis > it.syncTime?.millis ?: 0) {
                            deviceSyncer.get().executeSyncDevice()
                        } else {
                            Completable.complete()
                        }.andThen(Single.just(it))
                    }
            )
        }
    }

    override fun getDevice() = deviceLocalSource.get().getDevice()

    override suspend fun getIpRegion(): String {
        return deviceLocalSource.get().getIsIpRegionSynced()
            .flatMap { isIpRegionSynced ->
                if (isIpRegionSynced) {
                    deviceLocalSource.get().getIpRegion()
                } else {
                    deviceRemoteSource.get().getIpAddressData()
                        .map { it.region_code ?: "" }
                        .flatMap {
                            Timber.i("syncIpData success, region: $it")
                            deviceLocalSource.get().setIpRegion(it).toSingleDefault(it)
                        }
                }
            }
            .onErrorReturnItem("")
            .await()
    }
}
