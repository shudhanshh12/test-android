package `in`.okcredit.rewards.contract

import io.reactivex.Single

interface RewardsClaimHelper {

    fun claim(rewardId: String): Single<RewardStatus>

    fun isBankDetailsDuplication(onHold: RewardStatus.ON_HOLD): Boolean
    fun isUpiInactive(onHold: RewardStatus.ON_HOLD): Boolean
    fun isDailyPayoutLimitReached(onHold: RewardStatus.ON_HOLD): Boolean

    fun isPayoutDelayed(processing: RewardStatus.PROCESSING): Boolean
    fun isPayoutStarted(processing: RewardStatus.PROCESSING): Boolean
    fun isBudgetExhausted(processing: RewardStatus.PROCESSING): Boolean

    fun isRetryable(failed: RewardStatus.FAILED): Boolean
    fun isFailedBankUnavailable(failed: RewardStatus.FAILED): Boolean

    fun isCustom(state: RewardStatus): Boolean
}
