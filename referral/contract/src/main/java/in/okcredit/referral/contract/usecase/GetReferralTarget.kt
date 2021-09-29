package `in`.okcredit.referral.contract.usecase

import `in`.okcredit.shared.referral_views.model.Place
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import io.reactivex.Single
import tech.okcredit.android.base.utils.Optional

interface GetReferralTarget {
    fun execute(place: Place): Single<Optional<ReferralTargetBanner>>
}
