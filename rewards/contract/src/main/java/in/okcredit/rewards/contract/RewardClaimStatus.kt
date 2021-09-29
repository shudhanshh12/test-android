package `in`.okcredit.rewards.contract

class RewardClaimStatus(
    val status: String,
    val customMessage: String = "", // will only be utilized it status ends in `custom`
)
