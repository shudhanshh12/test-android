package tech.okcredit.android.referral.ui.share

import `in`.okcredit.shared.usecase.UseCase
import android.content.Intent
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.referral.usecase.GetReferralIntent
import tech.okcredit.android.referral.usecase.ShareReferralUseCase
import java.util.concurrent.TimeUnit

class ShareReferralViewModelTest {

    private lateinit var testObserver: TestObserver<ShareReferralContract.State>
    private lateinit var viewModel: ShareReferralViewModel

    private val mockSharedReferralUseCase: ShareReferralUseCase = mock()
    private val mockGetReferralIntent: GetReferralIntent = mock()
    private val mockShareIntent: Intent = mock()
    private lateinit var testScheduler: TestScheduler

    private val initialState = ShareReferralContract.State()

    @Before
    fun setup() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()

        mockkStatic(Schedulers::class)
        testScheduler = TestScheduler()
        every { Schedulers.io() } returns Schedulers.trampoline()
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        every { Schedulers.computation() } returns testScheduler

        viewModel = ShareReferralViewModel(
            initialState,
            { mockGetReferralIntent },
            { mockSharedReferralUseCase }
        )

        testObserver = viewModel.state().test()
    }

    @Test
    fun `when Load Intent is fired view event stream contains showShareNudge status`() {
        whenever(mockSharedReferralUseCase.shouldShowShareNudge())
            .thenReturn(Single.just(true))

        viewModel.attachIntents(Observable.just(ShareReferralContract.Intent.Load))
        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        Truth.assertThat(testObserver.values().first())
            .isEqualTo(initialState)
        Truth.assertThat(testObserver.values().last())
            .isEqualTo(initialState.copy(showProgress = false, showNudge = true))
    }

    @Test
    fun `when WhatsAppShare intent is fired then emits view event for it and stops progress`() {
        val eventObserver = TestObserver<ShareReferralContract.ViewEvent>()

        whenever(mockSharedReferralUseCase.setShareNudge(false))
            .thenReturn(Completable.complete())
        whenever(mockGetReferralIntent.getWhatsAppIntent())
            .thenReturn(UseCase.wrapObservable(Observable.just(mockShareIntent)))
        viewModel.viewEvent().subscribe(eventObserver)

        viewModel.attachIntents(Observable.just(ShareReferralContract.Intent.WhatsAppShare))
        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        Truth.assertThat(testObserver.values().first())
            .isEqualTo(initialState)
        Truth.assertThat(eventObserver.values().last())
            .isEqualTo(ShareReferralContract.ViewEvent.ShareToWhatsApp(mockShareIntent))
        Truth.assertThat(testObserver.values().last())
            .isEqualTo(initialState.copy(showProgress = false))
    }
}
