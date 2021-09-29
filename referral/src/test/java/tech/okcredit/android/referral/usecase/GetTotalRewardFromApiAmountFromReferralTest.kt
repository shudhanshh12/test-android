package tech.okcredit.android.referral.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.rewards.RewardsSyncerImpl
import `in`.okcredit.merchant.rewards.temp.SyncableRewardsRepository
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.junit.Test

class GetTotalRewardFromApiAmountFromReferralTest {
    private val mockSyncableRewardsRepository: SyncableRewardsRepository = mock()
    private val mockRewardsSyncerImpl: RewardsSyncerImpl = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getTotalRewardAmountFromReferral = GetTotalRewardAmountFromReferral(
        { mockSyncableRewardsRepository },
        { mockRewardsSyncerImpl },
        { getActiveBusinessId }
    )

    val listRewards: List<RewardModel> = listOf(
        RewardModel(
            id = "1234",
            create_time = DateTime(2018, 10, 2, 0, 0, 0),
            update_time = DateTime(2018, 10, 2, 0, 0, 0),
            status = "unclaimed/fake",
            reward_type = "internal_referral_reward",
            amount = 5000,
            featureName = "OkDance",
            featureTitle = "FeatureTitle",
            description = "feature description",
            deepLink = "abcdefg",
            icon = "beautiful icon",
            labels = HashMap(),
            createdBy = "",
        ),
        RewardModel(
            id = "12346",
            create_time = DateTime(2018, 10, 2, 0, 0, 0),
            update_time = DateTime(2018, 10, 2, 0, 0, 0),
            status = "unclaimed/fake",
            reward_type = "internal_referral_reward",
            amount = 5000,
            featureName = "OkDance",
            featureTitle = "FeatureTitle",
            description = "feature description",
            deepLink = "abcdefg",
            icon = "beautiful icon",
            labels = HashMap(),
            createdBy = "",
        ),
        RewardModel(
            id = "12346",
            create_time = DateTime(2018, 10, 2, 0, 0, 0),
            update_time = DateTime(2018, 10, 2, 0, 0, 0),
            status = "claimed/fake",
            reward_type = "internal_referral_reward",
            amount = 5000,
            featureName = "OkDance",
            featureTitle = "FeatureTitle",
            description = "feature description",
            deepLink = "abcdefg",
            icon = "beautiful icon",
            labels = HashMap(),
            createdBy = "",
        )
    )
    private val fakeResponse = GetTotalRewardAmountFromReferral.Response(
        5000, 10000
    )
    private val businessId = "business-id"

    data class Response(
        val totalClaimedReferralReward: Long,
        val totalUnClaimedReferralReward: Long,
    )

    @Test
    fun `Usecase should return fake response`() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(mockRewardsSyncerImpl.scheduleEverything(businessId)).thenReturn(Completable.complete())
        whenever(mockSyncableRewardsRepository.listRewards()).thenReturn(Observable.just(listRewards))

        // when
        val testObserver =
            getTotalRewardAmountFromReferral.execute(Unit).subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(fakeResponse)
        )

        testObserver.dispose()
    }
}
