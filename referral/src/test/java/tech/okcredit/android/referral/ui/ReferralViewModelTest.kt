package tech.okcredit.android.referral.ui

import `in`.okcredit.referral.contract.utils.ReferralVersion
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.referral.analytics.ReferralEventTracker
import tech.okcredit.android.referral.ui.referral_screen.ReferralContract
import tech.okcredit.android.referral.ui.referral_screen.ReferralViewModel
import tech.okcredit.android.referral.usecase.SyncReferral
import tech.okcredit.android.referral.utils.GetReferralVersionImpl
import tech.okcredit.feature_help.contract.GetContextualHelpIds

class ReferralViewModelTest {
    private lateinit var referralViewModel: ReferralViewModel
    private val initialState: ReferralContract.State = ReferralContract.State()
    private val syncReferral: SyncReferral = mock()
    private val getReferralVersionImpl: GetReferralVersionImpl = mock()
    private val referralEventTracker: ReferralEventTracker = mock()
    private val getContextualHelpIds: GetContextualHelpIds = mock()

    private fun createViewModel(): ReferralViewModel {
        return ReferralViewModel(
            initialState = initialState,
            syncReferral = { syncReferral },
            getReferralVersion = { getReferralVersionImpl },
            referralEventTracker = { referralEventTracker },
            getContextualHelpIds = { getContextualHelpIds },
        )
    }

    @Before
    fun setup() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `get Referral version is TARGETED_REFERRAL`() {
        val initialState = ReferralContract.State()
        referralViewModel = createViewModel()

        // when
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.TARGETED_REFERRAL))

        val stateObserver = TestObserver<ReferralContract.State>()
        referralViewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values()).contains(initialState)
    }

    @Test
    fun `get Referral version is TARGETED_REFERRAL_WITH_SHARE_OPTION`() {
        val initialState = ReferralContract.State()
        referralViewModel = createViewModel()

        // when
        whenever(getReferralVersionImpl.execute()).thenReturn(Observable.just(ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION))

        val stateObserver = TestObserver<ReferralContract.State>()
        referralViewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values()).contains(initialState)
    }
}
