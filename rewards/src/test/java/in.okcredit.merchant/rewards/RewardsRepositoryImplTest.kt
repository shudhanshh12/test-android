package `in`.okcredit.merchant.rewards

import `in`.okcredit.merchant.rewards.server.RewardsServer
import `in`.okcredit.merchant.rewards.server.internal.ApiMessages
import `in`.okcredit.merchant.rewards.store.RewardsStore
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardType
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils

class RewardsRepositoryImplTest {

    private val mockStore: RewardsStore = mock()
    private val mockServer: RewardsServer = mock()
    private val mockSyncer: RewardsSyncerImpl = mock()

    private val repositoryImpl = RewardsRepositoryImpl(
        mockStore,
        mockServer,
        mockSyncer
    )

    private val fakeLocale = "fake_locale"

    @Before
    fun setup() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `claimReward() should return claimResponse`() = runBlocking {
        val fakeRewardClaimResponse = ApiMessages.ClaimRewardResponse("fake_status", "fake_message")
        val businessId = "business-id"
        whenever(mockServer.claimReward("reward_id", fakeLocale, businessId)).thenReturn(fakeRewardClaimResponse)
        whenever(mockSyncer.syncRewards()).thenReturn(mock())

        val testObserver = repositoryImpl.claimReward("reward_id", fakeLocale, businessId).test()

        assert(testObserver.awaitTerminalEvent()) // Used as we are using rxSingle (Saket)
        verify(mockServer).claimReward("reward_id", fakeLocale, businessId)
        verify(mockSyncer).syncRewards()
        testObserver.assertValue(
            fakeRewardClaimResponse
        )

        testObserver.dispose()
    }

    @Test
    fun `listRewards() should return all rewards`() {
        val rewards: List<RewardModel> = listOf(mock(), mock(), mock())
        whenever(mockStore.listRewards(RewardType.IPL_REWARDS.map { it.type })).thenReturn(Observable.just(rewards))

        val testObserver = repositoryImpl.listRewards().test()

        verify(mockStore).listRewards(RewardType.IPL_REWARDS.map { it.type })
        testObserver.assertValue(rewards)

        testObserver.dispose()
    }

    @Test
    fun `listRewardsFromServer() should throw exception if API fails`() = runBlocking {
        val rewards: List<RewardModel> = listOf(mock(), mock(), mock())
        val businessId = " business-id"
        whenever(mockServer.getRewards(businessId)).thenReturn(rewards)

        val testObserver = repositoryImpl.listRewardsFromServer(businessId).test()

        assert(testObserver.awaitTerminalEvent()) // Used as we are using rxSingle (Saket)
        verify(mockServer).getRewards(businessId)
        testObserver.assertValue(rewards)

        testObserver.dispose()
    }

    @Test
    fun `clearRewards() should clear rewards tables`() {
        whenever(mockStore.clearRewards()).thenReturn(Completable.complete())

        val testObserver = repositoryImpl.clearLocalData().test()

        verify(mockStore).clearRewards()
        testObserver.assertComplete()

        testObserver.dispose()
    }
}
