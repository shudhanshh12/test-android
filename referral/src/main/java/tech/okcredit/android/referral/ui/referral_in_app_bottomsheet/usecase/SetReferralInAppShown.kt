package tech.okcredit.android.referral.ui.referral_in_app_bottomsheet.usecase

import `in`.okcredit.referral.contract.ReferralRepository
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class SetReferralInAppShown @Inject constructor(
    private val referralRepository: Lazy<ReferralRepository>
) {
    fun execute(): Completable {
        return referralRepository.get().setReferralInAppPreference(true)
    }
}
