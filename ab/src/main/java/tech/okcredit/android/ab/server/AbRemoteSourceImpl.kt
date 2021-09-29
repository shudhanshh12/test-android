package tech.okcredit.android.ab.server

import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import tech.okcredit.android.ab.Profile
import tech.okcredit.base.network.asError
import javax.inject.Inject

@Reusable
class AbRemoteSourceImpl @Inject constructor(
    private val abApiClient: Lazy<AbApiClient>,
) : AbRemoteSource {

    override fun getProfile(deviceId: String, businessId: String, sourceType: String): Single<Profile> {
        return abApiClient.get()
            .getProfile(deviceId, businessId, "sync", sourceType)
            .map {
                if (it.isSuccessful) {
                    return@map it.body()?.profile?.toProfile() ?: Profile(hashMapOf())
                } else {
                    throw it.asError()
                }
            }
            .subscribeOn(Schedulers.io())
    }

    override fun getDeviceProfile(deviceId: String, sourceType: String): Single<Profile> {
        return abApiClient.get()
            .getDeviceProfile(deviceId, "sync", sourceType)
            .map {
                if (it.isSuccessful) {
                    return@map it.body()?.profile?.toProfile() ?: Profile(hashMapOf())
                } else {
                    throw it.asError()
                }
            }
            .subscribeOn(Schedulers.io())
    }

    override fun acknowledgeExperiment(
        deviceId: String,
        experimentName: String,
        experimentVariant: String,
        experimentStatus: Int,
        acknowledgeTime: Long,
        businessId: String,
    ): Completable {
        return abApiClient.get().acknowledge(
            AcknowledgementRequest(
                deviceId, experimentStatus, acknowledgeTime,
                listOf(Experiment(experimentName, 0, experimentVariant, mapOf()))
            ),
            businessId,
            "sync",
            "user_action"
        ).subscribeOn(Schedulers.io())
    }

    override fun deviceAcknowledgeExperiment(
        deviceId: String,
        experimentName: String,
        experimentVariant: String,
        experimentStatus: Int,
        acknowledgeTime: Long,
    ): Completable {
        return abApiClient.get().deviceAcknowledge(
            AcknowledgementRequest(
                deviceId, experimentStatus, acknowledgeTime,
                listOf(Experiment(experimentName, 0, experimentVariant, mapOf()))
            ),
            "sync", "user_action"
        ).subscribeOn(Schedulers.io())
    }
}

internal fun tech.okcredit.android.ab.server.Profile.toProfile(): Profile =
    Profile(features = features, experiments = experiments)
