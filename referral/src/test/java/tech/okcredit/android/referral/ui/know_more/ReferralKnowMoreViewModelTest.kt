package tech.okcredit.android.referral.ui.know_more

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.referral.contract.models.ReferralInfo
import `in`.okcredit.referral.contract.utils.ReferralVersion
import `in`.okcredit.shared.usecase.UseCase
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.referral.usecase.GetTotalRewardAmountFromReferral
import tech.okcredit.android.referral.utils.GetReferralVersionImpl

class ReferralKnowMoreViewModelTest {
    private val referralRepository: ReferralRepository = mock()
    private val getReferralVersionImpl: GetReferralVersionImpl = mock()
    private val collectionRepository: CollectionRepository = mock()
    private val getTotalRewardAmountFromReferral: GetTotalRewardAmountFromReferral = mock()
    private val referralInfo: ReferralInfo = mock()

    lateinit var viewModel: ReferralKnowMoreViewModel

    private fun createViewModel(initialState: ReferralKnowMoreContract.State): ReferralKnowMoreViewModel {
        return ReferralKnowMoreViewModel(
            initialState,
            Lazy { referralRepository },
            Lazy { getReferralVersionImpl },
            Lazy { collectionRepository },
            Lazy { getTotalRewardAmountFromReferral }
        )
    }

    @Before
    fun setup() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `get Referral Version is equal to TARGETED_REFERRAL_WITH_SHARE_OPTION`() {
        val initialState = ReferralKnowMoreContract.State()
        viewModel = createViewModel(initialState)

        // when
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION))

        val stateObserver = TestObserver<ReferralKnowMoreContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                version = ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION
            )
        )

        stateObserver.dispose()
    }

    @Test
    fun `get Referral Version is equal to TARGETED_REFERRAL`() {
        val initialState = ReferralKnowMoreContract.State()
        viewModel = createViewModel(initialState)

        // when
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.TARGETED_REFERRAL))

        val stateObserver = TestObserver<ReferralKnowMoreContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                version = ReferralVersion.TARGETED_REFERRAL
            )
        )

        stateObserver.dispose()
    }

    @Test
    fun `get Referral Version is equal to NO_REWARD`() {
        val initialState = ReferralKnowMoreContract.State()
        viewModel = createViewModel(initialState)

        // when
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.NO_REWARD))

        val stateObserver = TestObserver<ReferralKnowMoreContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                version = ReferralVersion.NO_REWARD
            )
        )

        stateObserver.dispose()
    }

    @Test
    fun `get Referral Version is equal to UNKNOWN`() {
        val initialState = ReferralKnowMoreContract.State()
        viewModel = createViewModel(initialState)

        // when
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.UNKNOWN))

        val stateObserver = TestObserver<ReferralKnowMoreContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                version = ReferralVersion.UNKNOWN
            )
        )

        stateObserver.dispose()
    }

    @Test
    fun `get Referral Version is equal to REWARDS_ON_ACTIVATION`() {
        val initialState = ReferralKnowMoreContract.State()
        viewModel = createViewModel(initialState)

        // when
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.REWARDS_ON_ACTIVATION))

        val stateObserver = TestObserver<ReferralKnowMoreContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                version = ReferralVersion.REWARDS_ON_ACTIVATION
            )
        )

        stateObserver.dispose()
    }

    @Test
    fun `get ReferralInfo`() {
        val initialState = ReferralKnowMoreContract.State()
        viewModel = createViewModel(initialState)

        // when
        whenever(referralRepository.getReferralInfo()).thenReturn(Observable.just(referralInfo))

        val stateObserver = TestObserver<ReferralKnowMoreContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                referralInfo = referralInfo
            )
        )

        stateObserver.dispose()
    }

    @Test
    fun `get TotalRewards`() {
        val initialState = ReferralKnowMoreContract.State()
        viewModel = createViewModel(initialState)

        // when
        whenever(getTotalRewardAmountFromReferral.execute(Unit)).thenReturn(
            UseCase.wrapObservable(Observable.just(responseTest))
        )

        val stateObserver = TestObserver<ReferralKnowMoreContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                totalClaimedReferralRewards = responseTest.totalClaimedReferralReward,
                totalUnclaimedReferralRewards = responseTest.totalUnClaimedReferralReward
            )
        )

        stateObserver.dispose()
    }

    val responseTest: GetTotalRewardAmountFromReferral.Response = mock()
}
