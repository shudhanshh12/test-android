package tech.okcredit.applock

import `in`.okcredit.individual.contract.IndividualRepository
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.applock.usecase.CheckFingerPrintLockAvailability
import tech.okcredit.applock.usecase.GetMerchantFingerprintPreference
import tech.okcredit.contract.MerchantPrefSyncStatus
import javax.inject.Inject

class MerchantPrefSyncImpl @Inject constructor(
    private val businessApi: Lazy<BusinessRepository>,
    private val individualRepository: Lazy<IndividualRepository>,
    private val checkFingerPrintLockAvailability: Lazy<CheckFingerPrintLockAvailability>,
    private val checkIsFingerprintEnabled: Lazy<GetMerchantFingerprintPreference>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : MerchantPrefSyncStatus {

    override fun checkMerchantPrefSync(): Single<Boolean> {
        return rxSingle { individualRepository.get().isPreferenceAvailable(PreferenceKey.FOUR_DIGIT_PIN.key) }
    }

    override fun execute(): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            businessApi.get().executeSyncBusiness(businessId)
        }
    }

    override fun checkFingerPrintAvailability(): Observable<Boolean> {
        return checkFingerPrintLockAvailability.get().execute()
    }

    override fun checkFingerPrintEnable(): Observable<Boolean> {
        return checkIsFingerprintEnabled.get().execute()
    }
}
