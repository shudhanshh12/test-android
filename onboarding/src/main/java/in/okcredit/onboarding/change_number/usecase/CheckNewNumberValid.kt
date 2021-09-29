package `in`.okcredit.onboarding.change_number.usecase

import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.contract.NumberCheckResponse
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class CheckNewNumberValid @Inject constructor(
    private val businessApi: Lazy<BusinessRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(req: Request): Single<NumberCheckResponse> {
        return getActiveBusinessId.get().execute()
            .flatMap { businessId -> businessApi.get().checkNewNumberExists(req.mobile, businessId) }
    }

    data class Request(val mobile: String)
}
