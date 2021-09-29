package tech.okcredit.android.referral.ui.referral_rewards_v1.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.referral.data.GetReferredMerchantsRequest
import tech.okcredit.android.referral.data.GetReferredMerchantsResponse
import tech.okcredit.android.referral.data.ReferralApiService
import javax.inject.Inject

class GetReferredMerchants @Inject constructor(
    private val referralApi: Lazy<ReferralApiService>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(): Observable<Result<GetReferredMerchantsResponse>> {
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute().flatMap {
                referralApi.get().getReferredMerchants(GetReferredMerchantsRequest(it), businessId = it)
            }
        )
    }
}
