package `in`.okcredit.rewards.contract

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface RewardsRepository {

    fun getRewardById(id: String): Observable<RewardModel>

    fun listRewards(): Observable<List<RewardModel>>

    fun listRewardsFromServer(businessId: String): Single<List<RewardModel>>

    fun clearLocalData(): Completable

    fun getRewards(rewardTypes: List<RewardType>): Observable<List<RewardModel>>

    fun setClaimErrorPreference(status: Boolean)

    fun getClaimErrorPreference(): Observable<Boolean>
}
