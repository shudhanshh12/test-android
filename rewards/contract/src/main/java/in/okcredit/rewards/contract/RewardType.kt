package `in`.okcredit.rewards.contract

enum class RewardType constructor(val type: String) {

    IPL_DAILY("ipl_reward_daily"),
    IPL_WEEKLY("ipl_reward_weekly"),
    IPL_TSHIRT("ipl_reward_tshirt"),
    IPL_BAT("ipl_reward_bat"),
    Collection_ADOPTION_REWARDS("collection_adoption_reward"),
    DIGITAL_SERVICE_SURVEY_REWARD("digital_service_survey_reward"),
    REFERRAL_REWARDS("internal_referral_reward"),
    ACTIVATION_MONEY_REWARDS("activation_money_reward"),
    ACTIVATION_FEATURE_REWARDS("feature_as_reward"),
    BETTER_LUCK_NEXT_TIME("better_luck_next_time"),
    PAY_ONLINE_CASHBACK_REWARDS("okp_cashback"),
    COLLECTION_TARGETED_REFERRAL("collection_targeted_referral"),
    REWARD_TYPE_UNKNOWN("unknown");

    companion object {
        private val map = values().associateBy(RewardType::type)

        fun fromString(type: String) = map[type]

        val IPL_REWARDS = listOf(IPL_DAILY, IPL_WEEKLY, IPL_TSHIRT, IPL_BAT)

        val IPL_ALL_WEEKLY = listOf(IPL_WEEKLY, IPL_TSHIRT, IPL_BAT)

        val NON_CASH_REWARDS = listOf(IPL_BAT, IPL_TSHIRT)
    }
}
