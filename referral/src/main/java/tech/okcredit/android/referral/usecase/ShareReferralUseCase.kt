package tech.okcredit.android.referral.usecase

import `in`.okcredit.referral.contract.ReferralRepository
import dagger.Lazy
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

class ShareReferralUseCase @Inject constructor(private val referralRepository: Lazy<ReferralRepository>) {

    fun shouldShowShareNudge() = rxSingle {
        referralRepository.get().shouldShowShareNudge()
    }

    fun setShareNudge(shouldShowNudge: Boolean) = rxCompletable {
        referralRepository.get().setShareNudge(shouldShowNudge)
    }
}
