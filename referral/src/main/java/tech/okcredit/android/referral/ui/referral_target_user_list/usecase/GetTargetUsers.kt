package tech.okcredit.android.referral.ui.referral_target_user_list.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.referral.contract.models.TargetedUser
import dagger.Lazy
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.android.base.coroutines.DispatcherProvider
import javax.inject.Inject

class GetTargetUsers @Inject constructor(
    private val referralRepository: Lazy<ReferralRepository>,
    private val dispatcherProvider: Lazy<DispatcherProvider>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(): Single<List<TargetedUser>> = getActiveBusinessId.get().execute().flatMap { businessId ->
        rxSingle(dispatcherProvider.get().io()) {
            referralRepository.get().getTargetedUsers(businessId)
        }
    }
}
