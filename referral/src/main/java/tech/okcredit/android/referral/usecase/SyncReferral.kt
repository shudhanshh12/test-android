package tech.okcredit.android.referral.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.referral.contract.ReferralRepository
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class SyncReferral @Inject constructor(
    private val referralRepository: Lazy<ReferralRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            referralRepository.get().sync(businessId)
        }
    }
}
