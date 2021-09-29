package tech.okcredit.android.referral.ui.rewards_on_signup.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.referral.contract.usecase.GetReferralTarget
import `in`.okcredit.shared.referral_views.model.Place
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import dagger.Lazy
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.android.base.utils.Optional
import tech.okcredit.android.base.utils.ofNullable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetReferralTargetImpl @Inject constructor(
    private val referralRepository: Lazy<ReferralRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>
) : GetReferralTarget {

    override fun execute(place: Place): Single<Optional<ReferralTargetBanner>> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            referralRepository.get().getQualifiedForJourney(businessId).flatMap { qualified ->
                rxSingle {
                    val closeTime = referralRepository.get().getTargetBannerCloseTime()
                    val transactionInitiationTime = referralRepository.get().getTransactionInitiatedTime()
                    if (qualified && closedMoreThanADayAgo(closeTime) &&
                        isTransactionInitiatedGreaterThan2Min(transactionInitiationTime)
                    ) {
                        referralRepository.get().getReferralTargets(businessId)
                            .firstOrNull { it.bannerPlace.contains(place.value) }
                            .ofNullable()
                    } else {
                        Optional.Absent
                    }
                }
            }
        }
    }

    private fun closedMoreThanADayAgo(closedAt: Long) =
        (System.currentTimeMillis() - closedAt) > TimeUnit.DAYS.toMillis(1)

    private fun isTransactionInitiatedGreaterThan2Min(time: Long) =
        (System.currentTimeMillis() - time) > TimeUnit.MINUTES.toMillis(2)
}
