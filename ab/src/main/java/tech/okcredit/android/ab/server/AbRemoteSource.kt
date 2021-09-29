package tech.okcredit.android.ab.server

import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.ab.Profile

interface AbRemoteSource {

    fun getProfile(deviceId: String, businessId: String, sourceType: String): Single<Profile>

    fun getDeviceProfile(deviceId: String, sourceType: String): Single<Profile>

    fun acknowledgeExperiment(
        deviceId: String,
        experimentName: String,
        experimentVariant: String,
        experimentStatus: Int,
        acknowledgeTime: Long,
        businessId: String,
    ): Completable

    fun deviceAcknowledgeExperiment(
        deviceId: String,
        experimentName: String,
        experimentVariant: String,
        experimentStatus: Int,
        acknowledgeTime: Long,
    ): Completable
}
