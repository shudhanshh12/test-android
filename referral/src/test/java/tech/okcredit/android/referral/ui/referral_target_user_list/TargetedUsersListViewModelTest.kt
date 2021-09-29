package tech.okcredit.android.referral.ui.referral_target_user_list

import `in`.okcredit.referral.contract.models.TargetedUser
import `in`.okcredit.shared.usecase.Result
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.referral.ui.referral_target_user_list.usecase.GetShareToWhatsAppStatusVisibility
import tech.okcredit.android.referral.ui.referral_target_user_list.usecase.GetTargetUsers
import tech.okcredit.android.referral.usecase.GetReferralIntent
import java.util.concurrent.TimeUnit

class TargetedUsersListViewModelTest {

    private val initialState = ReferralTargetedUsersListContract.State()
    private val getTargetUsers: GetTargetUsers = mock()
    private val getShareToWhatsAppStatusVisibility: GetShareToWhatsAppStatusVisibility = mock()
    private val getShareIntent: GetReferralIntent = mock()
    private val targetUsers: List<TargetedUser> = listOf(mock(), mock(), mock())

    private lateinit var viewModel: ReferralTargetedUsersListViewModel
    private lateinit var testScheduler: TestScheduler
    private lateinit var stateObserver: TestObserver<ReferralTargetedUsersListContract.State>
    private lateinit var viewEventObserver: TestObserver<ReferralTargetedUsersListContract.ViewEvents>

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        testScheduler = TestScheduler()
        every { Schedulers.computation() } returns testScheduler

        viewModel = ReferralTargetedUsersListViewModel(
            initialState,
            { getTargetUsers },
            { getShareToWhatsAppStatusVisibility },
            { getShareIntent }
        )
        stateObserver = viewModel.state().test()
        viewEventObserver = viewModel.viewEvent().test()
    }

    @Test
    fun `get targetedUsers`() {
        whenever(getTargetUsers.execute()).thenReturn(Single.just(targetUsers))
        whenever(getShareToWhatsAppStatusVisibility.execute()).thenReturn(
            Observable.just(
                Result.Failure(
                    IllegalAccessException()
                )
            )
        )

        viewModel.attachIntents(Observable.just(ReferralTargetedUsersListContract.Intent.Load))
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        Truth.assertThat(stateObserver.values().first()).isEqualTo(initialState)
        Truth.assertThat(
            stateObserver.values().last()
        ).isEqualTo(
            initialState.copy(
                targetedUsers = targetUsers,
                showTargetedUserList = true
            )
        )

        stateObserver.dispose()
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
