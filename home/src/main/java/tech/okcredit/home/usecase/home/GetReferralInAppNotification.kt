package tech.okcredit.home.usecase.home

import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.referral.contract.usecase.GetReferralVersion
import `in`.okcredit.referral.contract.utils.ReferralVersion
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class GetReferralInAppNotification @Inject constructor(
    private val getReferralVersion: Lazy<GetReferralVersion>,
    private val referralRepository: Lazy<ReferralRepository>,
) : UseCase<Unit, Boolean> {

    override fun execute(req: Unit): Observable<Result<Boolean>> {
        return UseCase.wrapSingle(
            referralRepository.get().isReferralInAppDisplayed().flatMap { displayed ->
                return@flatMap if (displayed) {
                    isReferralAbEnabled()
                } else {
                    Single.just(false)
                }
            }
        )
    }

    private fun isReferralAbEnabled() = getReferralVersion.get().execute().firstOrError()
        .map { referralVersion -> referralVersion != ReferralVersion.NO_REWARD }
}
