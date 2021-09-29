package `in`.okcredit.merchant.rewards.ui.rewards_screen

import `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase.GetClaimErrorPreference
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase.SetClaimErrorPreference
import `in`.okcredit.merchant.rewards.ui.rewards_screen.usecase.GetAllRewards
import `in`.okcredit.merchant.rewards.ui.rewards_screen.usecase.GetHomeMerchantData
import `in`.okcredit.rewards.contract.RewardsSyncer
import `in`.okcredit.shared.usecase.GetConnectionStatus
import `in`.okcredit.shared.usecase.Result
import com.google.common.truth.Truth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import fakeListOfRewards
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.feature_help.contract.GetContextualHelpIds

class RewardsViewModelTest {
    private val initialState = RewardsContract.State()
    lateinit var rewardsViewModel: RewardsViewModel
    private val getConnectionStatus: GetConnectionStatus = mock()
    private val getAllRewards: GetAllRewards = mock()
    private val rewardsSync: RewardsSyncer = mock()
    private val getHomeMerchantData: GetHomeMerchantData = mock()
    private val getClaimErrorPreference: GetClaimErrorPreference = mock()
    private val setClaimErrorPreference: SetClaimErrorPreference = mock()
    private val getContextualHelpIds: GetContextualHelpIds = mock()

    fun createViewModel(): RewardsViewModel =
        RewardsViewModel(
            initialState = initialState,
            getConnectionStatus = { getConnectionStatus },
            getAllRewards = { getAllRewards },
            rewardsSync = { rewardsSync },
            getHomeMerchantData = { getHomeMerchantData },
            getClaimErrorPreference = { getClaimErrorPreference },
            setClaimErrorPreference = { setClaimErrorPreference },
            getContextualHelpIds = { getContextualHelpIds }
        )

    @Before
    fun setup() {
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics

        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `if getConnectionStatus return exception then viewModel should return networkError = true`() {
        val initialState = RewardsContract.State()
        rewardsViewModel = createViewModel()

        // when
        rewardsViewModel.attachIntents(Observable.just(RewardsContract.Intent.Load))
        whenever(getConnectionStatus.execute()).thenReturn(Observable.just(Result.Failure(RuntimeException())))

        val stateObserver = TestObserver<RewardsContract.State>()
        rewardsViewModel.state().subscribe(stateObserver)

        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                networkError = true
            )
        )

        stateObserver.dispose()
    }

    @Test
    fun `when getClaimErrorPreference return true then state should be updated`() {
        val initialState = RewardsContract.State()
        rewardsViewModel = createViewModel()

        // when
        rewardsViewModel.attachIntents(Observable.just(RewardsContract.Intent.Load))
        whenever(getClaimErrorPreference.execute()).thenReturn(Observable.just(Result.Success(true)))

        val stateObserver = TestObserver<RewardsContract.State>()
        rewardsViewModel.state().subscribe(stateObserver)

        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                isAlertVisible = true
            )
        )

        stateObserver.dispose()
    }

    @Test
    fun `when allRewards return rewardStatement then state should be updated`() {
        val initialState = RewardsContract.State()
        val fakeRewardStatement = GetAllRewards.RewardsStatement(
            rewards = fakeListOfRewards,
            sumOfClaimedRewards = 2544000L,
            unclaimedRewards = 0L
        )
        rewardsViewModel = createViewModel()

        // when
        rewardsViewModel.attachIntents(Observable.just(RewardsContract.Intent.Load))
        whenever(getAllRewards.execute()).thenReturn(Observable.just(fakeRewardStatement))

        val stateObserver = TestObserver<RewardsContract.State>()
        rewardsViewModel.state().subscribe(stateObserver)

        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                loader = false,
                rewards = fakeRewardStatement.rewards.convertToRewardsControllerModels(),
                sumOfClaimedRewards = fakeRewardStatement.sumOfClaimedRewards,
                unclaimedRewards = fakeRewardStatement.unclaimedRewards,
                error = false
            )
        )

        stateObserver.dispose()
    }
}
