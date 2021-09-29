package tech.okcredit.home.account

import `in`.okcredit.backend._offline.IsWebLibraryEnabled
import `in`.okcredit.backend._offline.usecase.GetAccountSummary
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReportWorkerStatusProvider
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusiness
import androidx.lifecycle.LifecycleOwner
import com.google.common.truth.Truth.assertThat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.home.ui.acccountV2.ui.AccountContract
import tech.okcredit.home.ui.acccountV2.ui.AccountViewModel
import java.lang.ref.WeakReference

class AccountViewModelTest {

    private lateinit var viewModel: AccountViewModel
    private val getAccountSummary: GetAccountSummary = mock()

    private val getActiveBusiness: GetActiveBusiness = mock()

    private val downloadReportWorkerStatusProvider: DownloadReportWorkerStatusProvider = mock()
    private val downloadReport: DownloadReport = mock()

    private val isWebLibraryEnabled: IsWebLibraryEnabled = mock()
    private val initialState = AccountContract.State()
    lateinit var testObserver: TestObserver<AccountContract.State>
    private val viewEffectObserver = TestObserver<AccountContract.ViewEvent>()

    @Before
    fun setUp() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        createViewModel(initialState)

        // observe state
        testObserver = viewModel.state().test()
        viewModel.viewEvent().subscribe(viewEffectObserver)

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @After
    fun close() {
        testObserver.dispose()
        viewEffectObserver.dispose()
    }

    private fun createViewModel(initialState: AccountContract.State) {
        viewModel = AccountViewModel(
            initialState = initialState,
            getAccountSummary = { getAccountSummary },
            getActiveBusiness = { getActiveBusiness },
            getSupplierBalanceAndCount = mock(),
            downloadReportWorkerStatusProvider = { downloadReportWorkerStatusProvider },
            downloadReport = { downloadReport },
            notificationUrl = "",
            isWebLibraryEnabled = { isWebLibraryEnabled },
            getBusinessHealthDashboardEnabled = mock()
        )
    }

    companion object {
        val accountSummary = GetAccountSummary.AccountSummary(100, 5)
        var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        var dateTime: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")

        val merchant = Business(
            id = "merchantId",
            name = "merchant name",
            mobile = "merchant mobile",
            createdAt = dateTime
        )
    }

    @Test
    fun `getAccountSummary test accountSummary`() {
        // given
        val request = mock<DownloadReport.Request>()
        whenever(getAccountSummary.execute()).thenReturn(Observable.just(accountSummary))
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(merchant))
        whenever(downloadReport.schedule(request)).thenReturn(Completable.complete())

        // when
        viewModel.attachIntents(Observable.just(AccountContract.Intent.Load))

        // then
        assertThat(testObserver.values().first().accountSummary == accountSummary)
    }

    @Test
    fun `getAccountSummary test merchant`() {
        // given
        val request = mock<DownloadReport.Request>()
        whenever(getAccountSummary.execute()).thenReturn(Observable.just(accountSummary))
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(merchant))
        whenever(downloadReport.schedule(request)).thenReturn(Completable.complete())

        // when
        viewModel.attachIntents(Observable.just(AccountContract.Intent.Load))

        // then
        assertThat(testObserver.values().first().merchantName == "merchant name")
    }

    @Test
    fun `test checkCustomerKhataClick`() {
        viewModel.attachIntents(Observable.just(AccountContract.Intent.CustomerKhataClick))

        assertThat(viewEffectObserver.values().last() == AccountContract.ViewEvent.GoToAccountStatementScreen).isTrue()
    }

    @Test
    fun `test DownloadReport`() {
        viewModel.attachIntents(Observable.just(AccountContract.Intent.DownloadReport))

        // then
        assertThat(testObserver.values().first().isLoading)
    }

    @Test
    fun `test DownloadReport test with baseUrl`() {
        // given
        val request = mock<DownloadReport.Request>()
        whenever(downloadReport.schedule(request)).thenReturn(Completable.complete())
        viewModel.attachIntents(Observable.just(AccountContract.Intent.DownloadReport))

        // then
        assertThat(testObserver.values().first().isLoading)
    }

    @Test
    fun `test OnReportUrlGenerated`() {
        viewModel.attachIntents(Observable.just(AccountContract.Intent.DownloadReport))

        // then
        assertThat(!testObserver.values().first().isLoading)
    }

    @Test
    fun `test when test webview clicked`() {
        viewModel.attachIntents(Observable.just(AccountContract.Intent.WebLibraryClick))

        // then
        assertThat(testObserver.values().first().isWebTestingActivated)
    }

    @Test
    fun `observe download report worker status with report type BACKUP_ALL should call status provider with BACKUP_ALL`() {
        val lifecycleOwnerWeak: WeakReference<LifecycleOwner> = mock()
        val reportType = DownloadReport.ReportType.BACKUP_ALL
        val workerName = DownloadReport.getWorkName(DownloadReport.ReportType.BACKUP_ALL)

        viewModel.attachIntents(
            Observable.just(AccountContract.Intent.ObserveDownloadReportWorkerStatus(lifecycleOwnerWeak, reportType))
        )

        verify(downloadReportWorkerStatusProvider).execute(lifecycleOwnerWeak, workerName)
    }

    @Test
    fun `download report with supplier credit enabled should call download report worker with report type BACKUP_ALL`() {
        val workerName = DownloadReport.getWorkName(DownloadReport.ReportType.BACKUP_ALL)
        viewModel.attachIntents(Observable.just(AccountContract.Intent.DownloadReport))

        verify(downloadReport).schedule(
            DownloadReport.Request(
                DownloadReport.ReportType.BACKUP_ALL,
                workName = workerName
            )
        )
    }
}
