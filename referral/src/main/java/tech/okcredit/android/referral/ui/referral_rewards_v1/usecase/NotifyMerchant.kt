package tech.okcredit.android.referral.ui.referral_rewards_v1.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.referral.data.NotifyMerchantRequest
import tech.okcredit.android.referral.data.NotifyMerchantResponse
import tech.okcredit.android.referral.data.ReferralApiService
import javax.inject.Inject

class NotifyMerchant @Inject constructor(
    private val referralApi: Lazy<ReferralApiService>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<String, NotifyMerchantResponse> {

    override fun execute(req: String): Observable<Result<NotifyMerchantResponse>> {
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute().flatMap {
                referralApi.get().notifyUser(NotifyMerchantRequest(it, req), businessId = it)
            }
        )
    }
}
