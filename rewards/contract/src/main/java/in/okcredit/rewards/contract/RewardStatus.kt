package `in`.okcredit.rewards.contract

/*
 * This file documents different types of statuses used to parse rewards
 *
 * Ref: https://okcredit.atlassian.net/wiki/spaces/AC/pages/1224179757/Rewards+Revamp+design
 */

@Suppress("ClassName")
sealed class RewardStatus(
    val status: String,
    val customMessage: String,
) {
    class UNCLAIMED(status: String, customMessage: String) : RewardStatus(status, customMessage)
    class CLAIMED(status: String, customMessage: String) : RewardStatus(status, customMessage)
    class FAILED(status: String, customMessage: String) : RewardStatus(status, customMessage)
    class ON_HOLD(status: String, customMessage: String) : RewardStatus(status, customMessage)
    class PROCESSING(status: String, customMessage: String) : RewardStatus(status, customMessage)
}
