package tech.okcredit.android.ab.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.device.DeviceRepository
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.ab.server.AbRemoteSource
import tech.okcredit.android.ab.store.AbLocalSource
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.base.crashlytics.RecordException
import javax.inject.Inject

class SyncAbProfile @Inject constructor(
    private val authService: Lazy<AuthService>,
    private val deviceRepository: Lazy<DeviceRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val localSource: Lazy<AbLocalSource>,
    private val remoteSource: Lazy<AbRemoteSource>,
) {

    /**
     * Fetches data from /GetProfile if user is logged in, else calls /GetDeviceProfile endpoint
     */
    fun execute(businessId: String?, sourceType: String): Completable {
        val deviceId = deviceRepository.get().deviceDeprecated.id
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapCompletable { _businessId ->
            if (authService.get().isAuthenticated()) {
                remoteSource.get().getProfile(deviceId, _businessId, sourceType)
            } else {
                remoteSource.get().getDeviceProfile(deviceId, sourceType)
            }.flatMapCompletable {
                localSource.get().setProfile(it, _businessId)
            }.doOnError {
                RecordException.recordException(it)
            }
        }
    }
}
