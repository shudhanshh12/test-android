package `in`.okcredit.frontend.ui.account_statement

import `in`.okcredit.backend._offline.usecase.GetAccountStatement
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport.Companion.getWorkName
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport.ReportType
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReportWorkerStatusProvider
import `in`.okcredit.backend._offline.usecase.reports_v2.WorkerStatus.*
import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.account_statement.AccountStatementContract.*
import `in`.okcredit.frontend.ui.account_statement.AccountStatementContract.Intent
import `in`.okcredit.frontend.ui.account_statement.AccountStatementContract.Intent.*
import `in`.okcredit.frontend.ui.account_statement.AccountStatementContract.PartialState
import `in`.okcredit.frontend.ui.account_statement.AccountStatementContract.PartialState.*
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.shared.utils.CommonUtils
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.BehaviorSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AccountStatementViewModel @Inject constructor(
    initialState: State,
    private val getAccountStatement: GetAccountStatement,
    @ViewModelParam(MainActivity.ARG_SOURCE) val source: String,
    @ViewModelParam(ARG_DURATION) val duration: String,
    @ViewModelParam(ARG_FILTER) val filter: String,
    private val downloadReportWorkerStatusProvider: Lazy<DownloadReportWorkerStatusProvider>,
    private val downloadReport: Lazy<DownloadReport>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    companion object {
        const val ARG_DURATION = "duration"
        const val ARG_FILTER = "filter"
    }

    private val timeRange: BehaviorSubject<GetAccountStatement.Request> = BehaviorSubject.createDefault(
        GetAccountStatement.Request(
            initialState.startDate,
            initialState.endDate,
            initialState.isShowOld
        )
    )
    private var isInitialLoad = true
    private var shouldShowDownloadAlert = false

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(

            // load page
            intent<Load>()
                .switchMap { timeRange }
                .switchMap {
                    UseCase.wrapObservable(
                        getAccountStatement.execute(
                            GetAccountStatement.Request(
                                it.startTime,
                                it.endTime,
                                it.isShowAll
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> ShowLoading
                        is Result.Success -> {
                            // Default is today. if there is no txns today, we are setting default to 1 month
                            val durationInDays = duration.toIntOrNull()
                            if (durationInDays != null && isInitialLoad) {
                                isInitialLoad = false
                                val startDate =
                                    CommonUtils.currentDateTime().withTimeAtStartOfDay().minusDays(durationInDays)
                                val endDate =
                                    CommonUtils.currentDateTime().plusDays(1).withTimeAtStartOfDay().minusMillis(1)
                                timeRange.onNext(GetAccountStatement.Request(startDate, endDate, false))
                                PartialState.ChangeDateRange(startDate, endDate)
                            } else if (it.value.transactionWrappers.isEmpty() && isInitialLoad) {
                                isInitialLoad = false
                                val startDate = CommonUtils.currentDateTime().withTimeAtStartOfDay().minusMonths(1)
                                val endDate =
                                    CommonUtils.currentDateTime().plusDays(1).withTimeAtStartOfDay().minusMillis(1)
                                timeRange.onNext(GetAccountStatement.Request(startDate, endDate, false))
                                PartialState.ChangeDateRange(startDate, endDate)
                            } else {
                                isInitialLoad = false
                                Timber.d("${GetAccountStatement.TAG} ShowData of ${it.value.transactionWrappers.size}")
                                ShowData(
                                    it.value.transactionWrappers,
                                    it.value.totalCreditAmount,
                                    it.value.totalCreditCount,
                                    it.value.totalPaymentAmount,
                                    it.value.totalPaymentCount,
                                    it.value.isShowLoadMore,
                                    it.value.totalDiscountAmount,
                                    it.value.totalDiscountCount
                                )
                            }
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> {
                                    SetNetworkError(true)
                                }
                                else -> {
                                    ErrorState
                                }
                            }
                        }
                    }
                },

            intent<Load>()
                .take(1)
                .map {
                    val showOnlineTxnSelected = source == "collection_screen" || filter == "online"
                    SetSourceScreen(source, showOnlineTxnSelected)
                },

            // change date range
            intent<Intent.ChangeDateRange>()
                .doOnNext {
                    timeRange.onNext(
                        GetAccountStatement.Request(
                            it.startDate,
                            it.endDate,
                            timeRange.value?.isShowAll ?: false
                        )
                    )
                }
                .map { PartialState.ChangeDateRange(it.startDate, it.endDate) },

            // load all items
            intent<Intent.LoadOldTxns>()
                .doOnNext {
                    timeRange.onNext(
                        GetAccountStatement.Request(
                            timeRange.value?.startTime!!,
                            timeRange.value?.endTime!!,
                            true
                        )
                    )
                }
                .map { NoChange },

            // handle `show alert` intent
            intent<Intent.ShowAlert>()
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<PartialState> { HideAlert }
                        .startWith(PartialState.ShowAlert(it.message))
                },

            // download report
            intent<DownloadStatement>()
                .switchMap {
                    shouldShowDownloadAlert = true
                    UseCase.wrapCompletable(
                        downloadReport.get().schedule(
                            DownloadReport.Request(
                                reportType = ReportType.CUSTOMER_ACCOUNT,
                                accountId = null,
                                startTimeSec = it.startDate,
                                endTimeSec = it.endDate,
                                workName = getWorkName(ReportType.CUSTOMER_ACCOUNT),
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> SetDownloadLoadingStatus(true)
                        else -> NoChange
                    }
                },

            intent<Intent.HideDownloadAlert>()
                .map {
                    PartialState.HideDownloadAlert
                },

            intent<SelectOnlineTransactions>()
                .map {
                    SetOnlineTxnSelected(it.isOnlineTransationSelected)
                },

            intent<ObserveWorkerStatus>()
                .take(1)
                .switchMap {
                    downloadReportWorkerStatusProvider.get().execute(
                        weakLifecycleOwner = it.weakLifecycleOwner,
                        workerName = getWorkName(ReportType.CUSTOMER_ACCOUNT),
                    )
                }.map {
                    when {
                        it is Running -> {
                            shouldShowDownloadAlert = true
                            SetDownloadLoadingStatus(true)
                        }
                        it is Error && shouldShowDownloadAlert -> {
                            if (it.isInternetIssue) SetNetworkError(true)
                            else ErrorState
                        }
                        it is Completed && shouldShowDownloadAlert -> ShowDownloadAlert(
                            it.uriString
                        )
                        else -> NoChange
                    }
                }
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is ShowLoading -> currentState.copy(isLoading = true)
            is ShowData -> currentState.copy(
                isLoading = false,
                transactions = partialState.transaction,
                totalCreditAmount = partialState.totalCreditAmount,
                totalCreditCount = partialState.totalCreditCount,
                totalPaymentAmount = partialState.totalPaymentAmount,
                totalPaymentCount = partialState.totalPaymentCount,
                isShowOld = partialState.isShowOld,
                totalDiscountAmount = partialState.totalDiscountAmount,
                totalDiscountCount = partialState.totalDiscountCount
            )
            is ErrorState -> currentState.copy(
                isLoading = false,
                error = true,
                isLoadingDownload = false
            )
            is PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message,
                isShowDownloadAlert = false
            )
            is PartialState.ChangeDateRange -> currentState.copy(
                startDate = partialState.startDate,
                endDate = partialState.endDate
            )
            is HideAlert -> currentState.copy(isAlertVisible = false)
            is ChangeShowOldStatus -> currentState.copy(isShowOld = partialState.status)
            is SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false,
                isLoadingDownload = false
            )
            is ShowDownloadAlert -> currentState.copy(
                isShowDownloadAlert = true,
                isLoadingDownload = false,
                downloadedFileUriString = partialState.downloadedFilePath
            )
            is PartialState.HideDownloadAlert -> currentState.copy(isShowDownloadAlert = false)
            is SetDownloadLoadingStatus -> currentState.copy(
                isLoadingDownload = partialState.status,
                isShowDownloadAlert = false
            )
            is SetOnlineTxnSelected -> currentState.copy(
                isOnlineTransactionSelected = partialState.isOnlineTransactionSelected
            )
            is SetSourceScreen -> currentState.copy(
                sourceScreen = partialState.sourceScreen,
                isOnlineTransactionSelected = partialState.isOnlineTransactionSelected
            )
            is NoChange -> currentState
        }
    }
}
