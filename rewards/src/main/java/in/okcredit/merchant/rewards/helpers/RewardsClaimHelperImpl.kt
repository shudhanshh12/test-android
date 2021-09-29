package `in`.okcredit.merchant.rewards.helpers

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.rewards.temp.SyncableRewardsRepository
import `in`.okcredit.rewards.contract.RewardStatus
import `in`.okcredit.rewards.contract.RewardStatus.*
import `in`.okcredit.rewards.contract.RewardsClaimHelper
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.language.LocaleManager
import javax.inject.Inject

/*
 * This file documents different types of statuses and additional information used to parse rewards
 *
 * // FAILURE
 * "bank/unavailable"
 * ".../retryable" // Postfix
 *
 * // ON_HOLD
 * "bank/user_details/duplicate"
 * "bank/user_details/inactive"
 * "bank/payout/daily_limit_reached"
 *
 * // PROCESSING
 * "bank/payout/delayed"
 * "bank/payout/started"
 * "okcredit/budget/exhausted"
 *
 * Ref: https://okcredit.atlassian.net/wiki/spaces/AC/pages/1224179757/Rewards+Revamp+design
 */

class RewardsClaimHelperImpl @Inject constructor(
    private val rewardsRepository: Lazy<SyncableRewardsRepository>,
    private val localeManager: Lazy<LocaleManager>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : RewardsClaimHelper {

    override fun claim(rewardId: String): Single<RewardStatus> {
        return Single.fromCallable { localeManager.get().getLanguage() }
            .flatMap {
                getActiveBusinessId.get().execute().flatMap { businessId ->
                    rewardsRepository.get().claimReward(rewardId, it, businessId)
                }
            }
            .map {
                when (val prefix = it.status.split("/").firstOrNull()) {
                    "unclaimed" -> UNCLAIMED(it.status, it.custom_message ?: "")
                    "claimed" -> CLAIMED(it.status, it.custom_message ?: "")
                    "failed" -> FAILED(it.status, it.custom_message ?: "")
                    "on_hold" -> ON_HOLD(it.status, it.custom_message ?: "")
                    "processing" -> PROCESSING(it.status, it.custom_message ?: "")
                    else -> {
                        val t = IllegalStateException("Unknown ClaimReward status prefix: $prefix")
                        RecordException.recordException(t)
                        throw t
                    }
                }
            }
    }

    // On_Hold statuses
    override fun isBankDetailsDuplication(onHold: ON_HOLD) = onHold.status.contains("bank/user_details/duplicate")
    override fun isUpiInactive(onHold: ON_HOLD) = onHold.status.contains("bank/user_details/inactive")
    override fun isDailyPayoutLimitReached(onHold: ON_HOLD) = onHold.status.contains("bank/payout/daily_limit_reached")

    // Processing statuses
    override fun isPayoutDelayed(processing: PROCESSING) = processing.status.contains("bank/payout/delayed")
    override fun isPayoutStarted(processing: PROCESSING) = processing.status.contains("bank/payout/started")
    override fun isBudgetExhausted(processing: PROCESSING) = processing.status.contains("okcredit/budget/exhausted")

    // Failed statuses
    override fun isRetryable(failed: FAILED) = failed.status.split("/").contains("retryable")
    override fun isFailedBankUnavailable(failed: FAILED) = failed.status.contains("bank/unavailable")

    // Custom statuses
    override fun isCustom(state: RewardStatus) = state.status.endsWith("custom")
}
