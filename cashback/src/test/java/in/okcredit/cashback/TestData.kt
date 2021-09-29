package `in`.okcredit.cashback

import `in`.okcredit.cashback.contract.model.CashbackMessageDetails
import `in`.okcredit.cashback.datasource.remote.apiClient.CashbackMessageDetailsDto
import `in`.okcredit.merchant.rewards.server.internal.ApiMessages
import org.joda.time.DateTime

object TestData {
    val cashbackMessageDetails = CashbackMessageDetails(true, 100, 100)
    val cashbackMessageDetailsDto = CashbackMessageDetailsDto(true, 100, 100)

    val rewardFromApi = ApiMessages.RewardFromApi(
        id = "reward-WYOWMUZK",
        create_time = DateTime(System.currentTimeMillis()),
        update_time = DateTime(System.currentTimeMillis()),
        status = "unclaimed/fake",
        amount = 0,
        reward_type = "better_luck_next_time",
        feature_details = null,
        labels = HashMap(),
        created_by = "",
    )
}
