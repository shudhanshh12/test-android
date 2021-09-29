package `in`.okcredit.merchant.rewards.ui.rewards_dialog

import `in`.okcredit.collection.contract.GetCollectionActivationStatus
import `in`.okcredit.merchant.rewards.TestViewModel
import `in`.okcredit.merchant.rewards.analytics.RewardsEventTracker
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.ClaimRewardsContract.*
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase.ExecuteFeatureRewardsDeeplink
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase.GetRewardShareIntent
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase.SetClaimErrorPreference
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardStatus
import `in`.okcredit.rewards.contract.RewardType
import `in`.okcredit.rewards.contract.RewardsClaimHelper
import `in`.okcredit.shared.usecase.GetConnectionStatus
import `in`.okcredit.shared.usecase.Result
import android.content.Intent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Assert
import org.junit.Test

class ClaimRewardsViewModelTest : TestViewModel<State, PartialState, ViewEvents>() {

    private val mockRewardsClaimHelper: RewardsClaimHelper = mock()
    private val mockGetConnectionStatus: GetConnectionStatus = mock()
    private val mockGetCollectionActivationStatus: GetCollectionActivationStatus = mock()
    private val mockInternalDeeplinkNavigator: ExecuteFeatureRewardsDeeplink = mock()
    private val mockSetErrorPreference: SetClaimErrorPreference = mock()
    private val mockRewardsEventTracker: RewardsEventTracker = mock()
    private val mockGetRewardShareIntent: GetRewardShareIntent = mock()

    @Test
    fun `getConnectionStatus should update the state when usecase return success with true value`() {
        whenever(mockGetConnectionStatus.execute())
            .thenReturn(Observable.just(Result.Success(true)))
        whenever(mockGetCollectionActivationStatus.execute())
            .thenReturn(Observable.just(false))

        // provide intent
        pushIntent(ClaimRewardsContract.Intent.Load)

        assertLastValue {
            !it.claimApiProcessingState
        }
        assertLastValue {
            !it.networkError
        }
    }

    @Test
    fun `getConnectionStatus should update the state with false when usecase return Failure`() {

        whenever(mockGetConnectionStatus.execute())
            .thenReturn(Observable.just(Result.Failure(Exception())))
        whenever(mockGetCollectionActivationStatus.execute())
            .thenReturn(Observable.just(false))

        // provide intent
        pushIntent(ClaimRewardsContract.Intent.Load)

        // expectations
        assertLastValue {
            !it.claimApiProcessingState
        }
        assertLastValue {
            it.networkError
        }
    }

    @Test
    fun `getRewardShareIntent should emit a viewevent when usecase return Success with Intent`() {
        val fakeIntent = Intent()
        val currentState = lastState()
        whenever(mockGetRewardShareIntent.execute(2500L))
            .thenReturn(Observable.just(Result.Success(fakeIntent)))
        whenever(mockGetConnectionStatus.execute())
            .thenReturn(Observable.just(Result.Success(true)))
        whenever(mockGetCollectionActivationStatus.execute())
            .thenReturn(Observable.just(false))

        // provide intent
        pushIntent(ClaimRewardsContract.Intent.ShareReward)

        // expectations
        Assert.assertTrue(lastViewEvent() == ViewEvents.ShareReward(fakeIntent))
        assertLastState(currentState)
    }

    @Test
    fun `SetBankDetails Intent Should emit a viewevent showMerchantDestinationDailog`() {
        val currentState = lastState()
        whenever(mockGetConnectionStatus.execute())
            .thenReturn(Observable.just(Result.Success(true)))
        whenever(mockGetCollectionActivationStatus.execute())
            .thenReturn(Observable.just(false))

        // provide intent
        pushIntent(ClaimRewardsContract.Intent.SetBankDetails)

        // expectations
        Assert.assertTrue(
            lastViewEvent() == ViewEvents.ShowAddMerchantDestinationDialog()
        )
        assertLastState(currentState)
    }

    @Test
    fun `SetError Intent should call setErrorPreference usecase`() {
        val currentState = lastState()
        whenever(mockGetConnectionStatus.execute())
            .thenReturn(Observable.just(Result.Success(true)))

        // provide intent
        pushIntent(ClaimRewardsContract.Intent.SetError)

        // expectations
        verify(mockSetErrorPreference).execute(true)
        assertLastState(currentState)
    }

    @Test
    fun `CheckCollectionStatus Intent should call usecase and if usecase return false then Update the state`() {

        val currentState = lastState()
        whenever(mockGetConnectionStatus.execute())
            .thenReturn(Observable.just(Result.Success(true)))
        whenever(
            mockGetCollectionActivationStatus
                .execute()
        ).thenReturn(Observable.just(false))

        // provide intent
        pushIntent(ClaimRewardsContract.Intent.CheckCollectionStatus)

        // expectations
        verify(mockGetCollectionActivationStatus).execute()

        assertLastState(currentState.copy(showEnterBankDetailsButton = true))
    }

    @Test
    fun `ClaimReward Intent should call usecase and if usecase return Suceess`() {
        whenever(mockGetConnectionStatus.execute())
            .thenReturn(Observable.just(Result.Success(true)))
        whenever(mockRewardsClaimHelper.claim(fakeRewardModel.id))
            .thenReturn(Single.just(fakeRewardStatus))

        // provide intent
        pushIntent(ClaimRewardsContract.Intent.ClaimReward)

        // expectations

        assertLastValue { it.claimSuccess }
        assertLastValue { it.claimed }
        assertLastValue { !it.claimApiProcessingState }
        assertLastValue { !it.isBankDetailsDuplication }
//        assertLastValue { !it.claimFailure } // Todo Harshit
        assertLastValue { !it.isBudgetExhausted }
    }

    override fun createViewModel() = ClaimRewardsViewModel(
        initialState = fakeInitialState,
        rewards = fakeRewardModel,
        source = fakeSource,
        referenceId = fakeReferenceId,
        rewardsClaimHelper = { mockRewardsClaimHelper },
        getConnectionStatus = { mockGetConnectionStatus },
        getCollectionActivationStatus = { mockGetCollectionActivationStatus },
        internalDeeplinkNavigator = { mockInternalDeeplinkNavigator },
        setErrorPreference = { mockSetErrorPreference },
        rewardsEventTracker = { mockRewardsEventTracker },
        getRewardShareIntent = { mockGetRewardShareIntent },
    )

    companion object {
        private val fakeRewardStatus = RewardStatus.CLAIMED(
            status = "",
            customMessage = ""
        )

        private val fakeRewardModel = RewardModel(
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

        private val fakeInitialState = State.fromReward(fakeRewardModel)
        private const val fakeSource: String = "fake screen"
        private const val fakeReferenceId = "fake reference id"
    }
}
