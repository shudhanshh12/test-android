package `in`.okcredit.merchant.rewards

import `in`.okcredit.rewards.contract.RewardModel
import io.reactivex.Completable
import io.reactivex.Single

interface RewardsSyncRepository {

    fun listRewardsFromServer(businessId: String): Single<List<RewardModel>>

    // clear all data from reward DB. using when user logout
    fun clearLocalData(): Completable
}
