package `in`.okcredit.frontend.ui.accountstatement

import `in`.okcredit.backend._offline.usecase.GetAccountStatement
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReportWorkerStatusProvider
import `in`.okcredit.frontend.ui.account_statement.AccountStatementContract
import `in`.okcredit.frontend.ui.account_statement.AccountStatementViewModel
import com.google.common.truth.Truth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class AccountStatementViewModelTest {
    companion object {
        val startDate = DateTime("2020-09-12T04:27:00.000+01:00")
        val endDate = DateTime("2020-10-12T04:27:00.000+01:00")
    }

    lateinit var accountStatementViewModel: AccountStatementViewModel
    private val getAccountStatement: GetAccountStatement = mock()
    private val downloadReport: DownloadReport = mock()
    private val downloadReportWorkerStatusProvider: DownloadReportWorkerStatusProvider = mock()
    private lateinit var testScheduler: TestScheduler

    fun createViewModel(initialState: AccountStatementContract.State) {
        accountStatementViewModel = AccountStatementViewModel(
            initialState = initialState,
            getAccountStatement = getAccountStatement,
            source = "Test_screen",
            duration = "test_duration",
            filter = "online",
            downloadReport = { downloadReport },
            downloadReportWorkerStatusProvider = { downloadReportWorkerStatusProvider }
        )
    }

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        testScheduler = TestScheduler()
        every { Schedulers.computation() } returns testScheduler

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `when ChangeDate intent if filter is online setSourceScreen event`() {
        // given
        val initialState = AccountStatementContract.State()
        createViewModel(initialState)

        // when
        val result = accountStatementViewModel.state().test()
        accountStatementViewModel.attachIntents(
            Observable.just(AccountStatementContract.Intent.ChangeDateRange(startDate, endDate))
        )
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        // then
        Truth.assertThat(result.values().last().startDate == startDate && result.values().last().endDate == endDate).isTrue()
        result.dispose()
    }

    @Test
    fun `when load intent if filter is online setSourcescreen event`() {
        // given
        val initialState = AccountStatementContract.State()
        createViewModel(initialState)

        // when
        accountStatementViewModel.attachIntents(Observable.just(AccountStatementContract.Intent.Load))
        val result = accountStatementViewModel.state().test()

        // then
        Truth.assertThat(result.values().last().isOnlineTransactionSelected)
        result.dispose()
    }

    @Test
    fun `when load OldTransactions Nochange event`() {
        // given
        val initialState = AccountStatementContract.State()
        createViewModel(initialState)

        // when
        accountStatementViewModel.attachIntents(Observable.just(AccountStatementContract.Intent.LoadOldTxns))
        val result = accountStatementViewModel.state().test()

        // then
        Truth.assertThat(result.values().last() == initialState)
        result.dispose()
    }

    @Test
    fun `when load ShowAlert then ShowAlert event`() {
        // given
        val initialState = AccountStatementContract.State()
        createViewModel(initialState)

        // when
        accountStatementViewModel.attachIntents(Observable.just(AccountStatementContract.Intent.ShowAlert("test_msg")))
        val result = accountStatementViewModel.state().test()

        // then
        Truth.assertThat(result.values().last().alertMessage == "test_msg")
        result.dispose()
    }

    @Test
    fun `when load HideDownloadAlert then HideAlert event`() {
        // given
        val initialState = AccountStatementContract.State()
        createViewModel(initialState)

        // when
        accountStatementViewModel.attachIntents(Observable.just(AccountStatementContract.Intent.HideDownloadAlert))
        val result = accountStatementViewModel.state().test()

        // then
        Truth.assertThat(result.values().last().alertMessage == " ")
        result.dispose()
    }
}
