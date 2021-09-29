package tech.okcredit.android.referral.ui.referral_target_user_list.usecase

import `in`.okcredit.referral.contract.utils.ReferralVersion
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.referral.utils.GetReferralVersionImpl
import javax.inject.Inject

class GetShareToWhatsAppStatusVisibility @Inject constructor(
    private val getReferralVersionImpl: Lazy<GetReferralVersionImpl>
) {
    fun execute(): Observable<Result<Boolean>> {
        return UseCase.wrapObservable(
            getReferralVersionImpl.get().execute().map {
                it == ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION
            }
        )
    }
}
