package tech.okcredit.android.ab.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.device.DeviceRepository
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.ab.server.AbRemoteSource
import tech.okcredit.android.auth.AuthService
import javax.inject.Inject

class AcknowledgeExperiment @Inject constructor(
    private val authService: Lazy<AuthService>,
    private val deviceRepository: Lazy<DeviceRepository>,
    private val remoteSource: Lazy<AbRemoteSource>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    /**
     * Acknowledges experiment on /Ack if user is logged in, else calls /DeviceAck endpoint
     */
    fun execute(
        experimentName: String,
        experimentVariant: String,
        experimentStatus: Int,
        acknowledgeTime: Long,
        businessId: String?,
    ): Completable {
        val deviceId = deviceRepository.get().deviceDeprecated.id
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapCompletable { _businessId ->
            if (authService.get().isAuthenticated()) {
                remoteSource.get().acknowledgeExperiment(
                    deviceId, experimentName, experimentVariant, experimentStatus, acknowledgeTime, _businessId,
                )
            } else {
                remoteSource.get().deviceAcknowledgeExperiment(
                    deviceId, experimentName, experimentVariant, experimentStatus, acknowledgeTime
                )
            }
        }
    }
}
