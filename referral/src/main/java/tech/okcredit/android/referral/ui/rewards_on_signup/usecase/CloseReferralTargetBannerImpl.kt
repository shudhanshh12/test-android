package tech.okcredit.android.referral.ui.rewards_on_signup.usecase

import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.referral.contract.usecase.CloseReferralTargetBanner
import dagger.Lazy
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

class CloseReferralTargetBannerImpl @Inject constructor(
    private val referralRepository: Lazy<ReferralRepository>,
) : CloseReferralTargetBanner {
    override fun execute(): Completable {
        return rxSingle {
            referralRepository.get().setTargetBannerCloseTime(System.currentTimeMillis())
        }.ignoreElement()
    }
}
