package tech.okcredit.android.referral.share.usecase

import dagger.Lazy
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class GetReferralDescriptionVisibility @Inject constructor(
    private val ab: Lazy<AbRepository>,
) {
    fun execute() = ab.get().isFeatureEnabled(SHOW_REFERRAL_DESCRIPTION)

    companion object {
        const val SHOW_REFERRAL_DESCRIPTION = "show_referral_description"
    }
}
