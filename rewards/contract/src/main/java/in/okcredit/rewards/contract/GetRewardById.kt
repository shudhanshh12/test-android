package `in`.okcredit.rewards.contract

import io.reactivex.Observable

interface GetRewardById {
    fun execute(id: String): Observable<RewardModel>
}
