package `in`.okcredit.merchant.customer_ui.onboarding.usecase

import `in`.okcredit.backend.contract.GetTotalTxnCount
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class GetEligibilityOnboardingNudge @Inject constructor(
    private val getTotalTxnCount: Lazy<GetTotalTxnCount>,
) {
    fun execute(): Single<Boolean> {
        return getTotalTxnCount.get().execute().map { count -> count < 1 }
    }
}
