import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardType
import com.nhaarman.mockitokotlin2.mock

val fakeRewardModel = RewardModel(
    id = "12323",
    create_time = mock(),
    update_time = mock(),
    status = "claimed/fake",
    reward_type = RewardType.ACTIVATION_FEATURE_REWARDS.type,
    amount = 2500L,
    featureName = "Ok_Dance",
    featureTitle = "OKDance",
    description = "Let's Dance and Stay Fit",
    deepLink = "",
    icon = "",
    labels = HashMap(),
    createdBy = "",
)

val fakeListOfRewards = listOf(
    fakeRewardModel,
    fakeRewardModel
)
