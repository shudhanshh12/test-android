package `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet

import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.usecase.GetInviteOptionVisibility
import `in`.okcredit.referral.contract.models.TargetedUser
import `in`.okcredit.referral.contract.usecase.GetReferralVersion
import `in`.okcredit.referral.contract.utils.ReferralVersion
import com.google.common.truth.Truth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.referral.usecase.GetReferralIntent

class MenuOptionBottomSheetViewModelTest {
    lateinit var viewModel: MenuOptionBottomSheetViewModel
    private val getInviteOptionVisibility: GetInviteOptionVisibility = mock()
    private val getShareContent: GetReferralIntent = mock()
    private val getReferralVersion: GetReferralVersion = mock()
    private val intent: android.content.Intent = mock()

    private fun createViewModel(): MenuOptionBottomSheetViewModel {
        return MenuOptionBottomSheetViewModel(
            initialState = MenuOptionBottomSheetContract.State(),
            getInviteOptionVisibility = { getInviteOptionVisibility },
            getShareContent = { getShareContent },
            getReferralVersion = { getReferralVersion }
        )
    }

    @Before
    fun setup() {
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `get ReferralVesrion is TARGETED_REFERRAL`() {
        val initialState = MenuOptionBottomSheetContract.State()
        viewModel = createViewModel()
        viewModel.attachIntents(
            Observable.just(MenuOptionBottomSheetContract.Intent.Load)
        )

        // when
        whenever(getReferralVersion.execute()).thenReturn(Observable.just(ReferralVersion.TARGETED_REFERRAL))

        val stateObserver = TestObserver<MenuOptionBottomSheetContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                referralVersion = ReferralVersion.TARGETED_REFERRAL
            )
        )
    }

    @Test
    fun `get ReferralVesrion is TARGETED_REFERRAL_WITH_SHARE_OPTION`() {
        val initialState = MenuOptionBottomSheetContract.State()
        viewModel = createViewModel()
        viewModel.attachIntents(
            Observable.just(MenuOptionBottomSheetContract.Intent.Load)
        )

        // when
        whenever(getReferralVersion.execute()).thenReturn(Observable.just(ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION))

        val stateObserver = TestObserver<MenuOptionBottomSheetContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                referralVersion = ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION
            )
        )
    }

    @Test
    fun `when ReferralVersion is UNKNOW then state should not be changed`() {
        val initialState = MenuOptionBottomSheetContract.State()
        viewModel = createViewModel()
        viewModel.attachIntents(
            Observable.just(MenuOptionBottomSheetContract.Intent.Load)
        )

        // when
        whenever(getReferralVersion.execute()).thenReturn(Observable.just(ReferralVersion.UNKNOWN))

        val stateObserver = TestObserver<MenuOptionBottomSheetContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState
        )
    }

    @Test
    fun `when ReferralVersion is NO_REWARDS then state should not be changed`() {
        val initialState = MenuOptionBottomSheetContract.State()
        viewModel = createViewModel()
        viewModel.attachIntents(
            Observable.just(MenuOptionBottomSheetContract.Intent.Load)
        )

        // when
        whenever(getReferralVersion.execute()).thenReturn(Observable.just(ReferralVersion.NO_REWARD))

        val stateObserver = TestObserver<MenuOptionBottomSheetContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState
        )
    }

    @Test
    fun `when ReferralVersion is REWARDS_ON_ACTIVATION then state should not be changed`() {
        val initialState = MenuOptionBottomSheetContract.State()
        viewModel = createViewModel()
        viewModel.attachIntents(
            Observable.just(MenuOptionBottomSheetContract.Intent.Load)
        )

        // when
        whenever(getReferralVersion.execute()).thenReturn(Observable.just(ReferralVersion.REWARDS_ON_ACTIVATION))

        val stateObserver = TestObserver<MenuOptionBottomSheetContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState
        )
    }

    @Test
    fun `get InviteOptionVisibility event`() {
        val initialState = MenuOptionBottomSheetContract.State()
        viewModel = createViewModel()
        viewModel.attachIntents(
            Observable.just(MenuOptionBottomSheetContract.Intent.CustomerModel(TestData.CUSTOMER))
        )

        // when
        whenever(getInviteOptionVisibility.execute(TestData.CUSTOMER)).thenReturn(
            `in`.okcredit.shared.usecase.UseCase.wrapObservable(
                Observable.just(true)
            )
        )

        val stateObserver = TestObserver<MenuOptionBottomSheetContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                canShowInviteOption = true
            )
        )
    }

    @Test
    fun `get ShareContent event`() {
        viewModel = createViewModel()
        viewModel.attachIntents(
            Observable.just(MenuOptionBottomSheetContract.Intent.SendInviteToWhatsApp(targetUser = targetedUser))
        )

        // when
        whenever(getShareContent.getWhatsAppIntent()).thenReturn(
            `in`.okcredit.shared.usecase.UseCase.wrapObservable(
                Observable.just(intent)
            )
        )

        val viewEventObserver = TestObserver<MenuOptionBottomSheetContract.ViewEvent>()
        viewModel.viewEvent().subscribe(viewEventObserver)

        Truth.assertThat(
            viewEventObserver.values().contains(
                MenuOptionBottomSheetContract.ViewEvent.SendInviteToTargetedUser(
                    intent
                )
            )
        )
    }

    val targetedUser = TargetedUser(
        id = "1234",
        name = "Babu rao ganpat rao apte",
        phoneNumber = "9722688188",
        imageUrl = "",
        converted = false,
        amount = 5000L,
        source = ""
    )
}
