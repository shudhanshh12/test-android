package tech.okcredit.home.usecase

import `in`.okcredit.merchant.rewards.temp.SyncableRewardsRepository
import `in`.okcredit.rewards.contract.RewardModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Test

class GetUnClaimedRewardsTest {
    private val rewardsRepository: SyncableRewardsRepository = mock()
    private val rewards: List<RewardModel> = listOf(mock(), mock(), mock())

    private val getUnClaimedRewards = GetUnClaimedRewards { rewardsRepository }

    @Test
    fun `UseCase should return unclaimed rewards`() {
        whenever(rewardsRepository.listRewards()).thenReturn(Observable.just(rewards))

        // when
        val testObserver =
            getUnClaimedRewards.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValue(
            rewards.filter { it.isUnclaimed() }
        )

        testObserver.dispose()
    }
}
