package `in`.okcredit.referral.contract.utils

enum class ReferralVersion(val type: String) {
    NO_REWARD("No Rewards"), // Only share
    REWARDS_ON_ACTIVATION("general"),
    TARGETED_REFERRAL("list"),
    TARGETED_REFERRAL_WITH_SHARE_OPTION("list & general"),
    UNKNOWN("Unknown");
}
