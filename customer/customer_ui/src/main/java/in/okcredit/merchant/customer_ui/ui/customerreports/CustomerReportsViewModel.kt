package `in`.okcredit.merchant.customer_ui.ui.customerreports

import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport.Companion.getWorkName
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReportFileNameProvider
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReportWorkerStatusProvider
import `in`.okcredit.backend._offline.usecase.reports_v2.GetReportV2UrlWithTimeout
import `in`.okcredit.backend._offline.usecase.reports_v2.ReportsV2Tracker
import `in`.okcredit.backend._offline.usecase.reports_v2.WorkerStatus
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.customer_ui.ui.customerreports.CustomerReportsContract.*
import `in`.okcredit.merchant.customer_ui.ui.customerreports.CustomerReportsContract.PartialState.*
import `in`.okcredit.merchant.customer_ui.usecase.GetAllTransactionsForCustomer
import `in`.okcredit.merchant.customer_ui.usecase.GetCustomerStatementForDateRange
import `in`.okcredit.merchant.customer_ui.usecase.GetMiniStatementReport
import `in`.okcredit.merchant.customer_ui.usecase.GetSharableReportIntent
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CustomerReportsViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam("customer_id") private val customerId: String,
    private val getCustomerStatementForDateRange: Lazy<GetCustomerStatementForDateRange>,
    private val getSharableReportIntent: Lazy<GetSharableReportIntent>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getCustomer: Lazy<GetCustomer>,
    private val getAllTransactionsForCustomer: Lazy<GetAllTransactionsForCustomer>,
    private val rxSharedPreference: Lazy<DefaultPreferences>,
    private val getMiniStatementReport: Lazy<GetMiniStatementReport>,
    private val downloadReportWorkerStatusProvider: Lazy<DownloadReportWorkerStatusProvider>,
    private val getReportUrlV2WithTimeout: Lazy<GetReportV2UrlWithTimeout>,
    private val downloadReportFileNameProvider: Lazy<DownloadReportFileNameProvider>,
    private val downloadReport: Lazy<DownloadReport>,
    private val reportsV2Tracker: Lazy<ReportsV2Tracker>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    private val timeRange: BehaviorSubject<GetCustomerStatementForDateRange.Request> = BehaviorSubject.create()

    private val internetErrorSubject: PublishSubject<Unit> = PublishSubject.create()
    private val someErrorSubject: PublishSubject<Unit> = PublishSubject.create()
    private var shouldShowDownloadAlert: Boolean = false

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(

            // load screen
            getTransactionForSelectedPeriodObservable(),

            collectionAdoptedObservable(),

            changeDateObservable(),

            downloadReportObservable(),

            shareReportObservable(),

            dateRangeEducationObservable(),

            reportShareEducationObservable(),

            getCustomerObservable(),

            getAllTransactionsObservable(),

            intent<Intent.RxPreferenceBoolean>()
                .switchMap {
                    UseCase.wrapCompletable(rxCompletable { rxSharedPreference.get().set(it.key, it.value, it.scope) })
                }
                .map { NoChange },

            intent<Intent.GetMiniStatementDateRange>()
                .switchMap { UseCase.wrapSingle(getMiniStatementReport.get().execute(customerId)) }
                .switchMap {
                    when (it) {
                        is Result.Progress -> {
                            Observable.just(NoChange)
                        }
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.TrackPreviewEvent(it.value.customerStatementResponse.transactions.isEmpty()))
                            Observable.just(
                                ShowAllTransactionsData(
                                    transactions = it.value.customerStatementResponse.transactions,
                                    paymentTransactionCount = it.value.customerStatementResponse.paymentTransactionCount,
                                    creditTransactionCount = it.value.customerStatementResponse.creditTransactionCount,
                                    balanceForSelectedDuration = it.value.customerStatementResponse.balanceForSelectedDateRange,
                                    totalPayment = it.value.customerStatementResponse.totalPayment,
                                    totalCredit = it.value.customerStatementResponse.totalCredit,
                                    selectedMode = it.value.selectedDateMode,
                                    startDate = it.value.startDate,
                                    endDate = it.value.endDate
                                )
                            )
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(ViewEvent.GoToLogin)
                                    Observable.just(NoChange)
                                }
                                isInternetIssue(it.error) -> {
                                    networkErrorObservable()
                                }
                                else -> {
                                    someErrorObservable()
                                }
                            }
                        }
                    }
                },
            internetErrorSubject.switchMap {
                Observable.timer(2, TimeUnit.SECONDS)
                    .map { SetNetworkError(false) }
                    .startWith(SetNetworkError(true))
            },
            someErrorSubject.switchMap {
                Observable.timer(2, TimeUnit.SECONDS)
                    .map<PartialState> { ShowError(false) }
                    .startWith(ShowError(true))
            },

            observeDownloadReportWorkerStatus(),
            shareIntentToWhatsapp(),
        )
    }

    private fun getTransactionForSelectedPeriodObservable(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap { timeRange }
            .switchMap {
                UseCase.wrapSingle(
                    getCustomerStatementForDateRange.get().execute(
                        GetCustomerStatementForDateRange.Request(
                            it.customerId,
                            it.startTime,
                            it.endTime
                        )
                    )
                )
            }
            .switchMap {
                when (it) {
                    is Result.Progress -> {
                        Observable.just(NoChange)
                    }
                    is Result.Success -> {
                        emitViewEvent(ViewEvent.TrackPreviewEvent(it.value.transactions.isEmpty()))
                        Observable.just(
                            ShowData(
                                it.value.transactions,
                                it.value.paymentTransactionCount,
                                it.value.creditTransactionCount,
                                it.value.balanceForSelectedDateRange,
                                it.value.totalPayment,
                                it.value.totalCredit
                            )
                        )
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(ViewEvent.GoToLogin)
                                Observable.just(NoChange)
                            }
                            isInternetIssue(it.error) -> {
                                networkErrorObservable()
                            }
                            else -> {
                                someErrorObservable()
                            }
                        }
                    }
                }
            }
    }

    private fun getAllTransactionsObservable(): Observable<UiState.Partial<State>> {
        return intent<Intent.GetAllTransactions>()
            .switchMap {
                UseCase.wrapObservable(
                    getAllTransactionsForCustomer.get().execute(customerId, it.selectedMode)
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> {
                        (NoChange)
                    }
                    is Result.Success -> {
                        emitViewEvent(ViewEvent.TrackPreviewEvent(it.value.customerStatementResponse.transactions.isEmpty()))
                        ShowAllTransactionsData(
                            transactions = it.value.customerStatementResponse.transactions,
                            paymentTransactionCount = it.value.customerStatementResponse.paymentTransactionCount,
                            creditTransactionCount = it.value.customerStatementResponse.creditTransactionCount,
                            balanceForSelectedDuration = it.value.customerStatementResponse.balanceForSelectedDateRange,
                            totalPayment = it.value.customerStatementResponse.totalPayment,
                            totalCredit = it.value.customerStatementResponse.totalCredit,
                            selectedMode = it.value.selectedDateMode,
                            startDate = it.value.startDate,
                            endDate = it.value.endDate
                        )
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(ViewEvent.GoToLogin)
                                NoChange
                            }
                            isInternetIssue(it.error) -> {
                                internetErrorSubject.onNext(Unit)
                                NoChange
                            }
                            else -> {
                                someErrorSubject.onNext(Unit)
                                NoChange
                            }
                        }
                    }
                }
            }
    }

    private fun getCustomerObservable(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap { UseCase.wrapObservable(getCustomer.get().execute(customerId)) }
            .map {
                when (it) {
                    is Result.Progress -> NoChange
                    is Result.Success -> {
                        SetCustomer(it.value)
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(ViewEvent.GoToLogin)
                                NoChange
                            }
                            isInternetIssue(it.error) -> SetNetworkError(true)
                            else -> {
                                NoChange
                            }
                        }
                    }
                }
            }
    }

    private fun dateRangeEducationObservable(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap {
                UseCase.wrapSingle(
                    rxSharedPreference.get()
                        .getBoolean(RxSharedPrefValues.IS_DATE_RANGE_EDUCATION_SHOWN, Scope.Individual)
                        .asObservable().firstOrError()
                )
            }.map {
                when (it) {
                    is Result.Progress -> NoChange
                    is Result.Success -> {
                        IsDateFilterEducationShown(it.value)
                    }
                    is Result.Failure -> NoChange
                }
            }
    }

    private fun reportShareEducationObservable(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap {
                UseCase.wrapSingle(
                    rxSharedPreference.get()
                        .getBoolean(RxSharedPrefValues.IS_REPORT_SHARE_EDUCATION_SHOWN, Scope.Individual)
                        .asObservable().firstOrError()
                )
            }.map {
                when (it) {
                    is Result.Progress -> NoChange
                    is Result.Success -> {
                        IsReportShareEducationShown(it.value)
                    }
                    is Result.Failure -> NoChange
                }
            }
    }

    private fun collectionAdoptedObservable(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap { UseCase.wrapObservable(collectionRepository.get().isCollectionActivated()) }
            .map {
                when (it) {
                    is Result.Progress -> NoChange
                    is Result.Success -> {
                        SetCollectionActivated(it.value)
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(ViewEvent.GoToLogin)
                                NoChange
                            }
                            else -> {
                                NoChange
                            }
                        }
                    }
                }
            }
    }

    private fun downloadReportObservable(): Observable<PartialState>? {
        return intent<Intent.DownloadReport>()
            .switchMap {
                shouldShowDownloadAlert = true
                wrap(
                    downloadReport.get().schedule(
                        DownloadReport.Request(
                            reportType = DownloadReport.ReportType.CUSTOMER_REPORT,
                            accountId = customerId,
                            startTimeSec = it.startDate,
                            endTimeSec = it.endDate,
                            workName = getWorkName(
                                DownloadReport.ReportType.CUSTOMER_REPORT,
                                customerId
                            )
                        )
                    )
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> SetReportDownloading(true)
                    else -> NoChange
                }
            }
    }

    private fun shareReportObservable(): Observable<PartialState>? {
        return intent<Intent.ShareReportClicked>()
            .switchMap {
                getCurrentState().let {
                    wrap(
                        getReportUrlV2WithTimeout.get().execute(
                            accountId = customerId,
                            it.startDate.millis,
                            it.endDate.millis,
                            DownloadReport.ReportType.CUSTOMER_REPORT.typeKeywordAtServer,
                        )
                    )
                }
            }
            .map { result ->
                when (result) {
                    is Result.Progress -> ShowDownloadLoader(true)
                    is Result.Success -> {
                        reportsV2Tracker.get().trackReportCreated(
                            reportTypeName = ReportsV2Tracker.PropertyValue.CUSTOMER_REPORT,
                            success = true,
                            accountId = customerId,
                            response = ReportsV2Tracker.PropertyValue.SUCCESSS,
                        )
                        pushIntent(Intent.ShareReportToWhatsapp(result.value))
                        ShowDownloadLoader(false)
                    }
                    is Result.Failure -> {
                        var response = ""
                        when {
                            isInternetIssue(result.error) -> {
                                response = ReportsV2Tracker.PropertyValue.NETWORK_ERROR
                                emitViewEvent(ViewEvent.ReportGenerationFailed(isInternetIssue = true))
                            }

                            result.error is DownloadReport.ReportUrlGenerationApiError -> {
                                response = (result.error as DownloadReport.ReportUrlGenerationApiError)
                                    .errorMessage ?: ""
                                emitViewEvent(ViewEvent.ReportGenerationFailed(isInternetIssue = false))
                            }

                            else -> {
                                response = ReportsV2Tracker.PropertyValue.UNKNOWN_ERROR
                                emitViewEvent(ViewEvent.ReportGenerationFailed(isInternetIssue = false))
                            }
                        }

                        reportsV2Tracker.get().trackReportCreated(
                            reportTypeName = ReportsV2Tracker.PropertyValue.CUSTOMER_REPORT,
                            success = false,
                            accountId = customerId,
                            response = response,
                        )

                        ShowDownloadLoader(false)
                    }
                }
            }
    }

    private fun shareIntentToWhatsapp(): Observable<PartialState>? {
        return intent<Intent.ShareReportToWhatsapp>()
            .switchMap {
                wrap(
                    getSharableReportIntent.get().execute(
                        getCurrentState().customer?.mobile ?: "",
                        it.url,
                        downloadReportFileNameProvider.get().execute(
                            DownloadReport.Request(
                                reportType = DownloadReport.ReportType.CUSTOMER_REPORT,
                                startTimeSec = getCurrentState().startDate,
                                endTimeSec = getCurrentState().endDate,
                                workName = ""
                            )
                        )
                    )
                )
            }.map {
                if (it is Result.Success)
                    emitViewEvent(ViewEvent.ShareReport(it.value))
                NoChange
            }
    }

    private fun changeDateObservable(): Observable<ChangeDateRange>? {
        return intent<Intent.ChangeDateRange>()
            .map {
                timeRange.onNext(
                    GetCustomerStatementForDateRange.Request(
                        customerId,
                        it.startDate,
                        it.endDate
                    )
                )
                ChangeDateRange(customerId, it.startDate, it.endDate, it.selectedMode)
            }
    }

    private fun networkErrorObservable(): Observable<PartialState>? {
        return Observable.timer(2, TimeUnit.SECONDS)
            .map<PartialState> { SetNetworkError(false) }
            .startWith(SetNetworkError(true))
    }

    private fun someErrorObservable(): Observable<PartialState>? {
        return Observable.timer(2, TimeUnit.SECONDS)
            .map<PartialState> { ShowError(false) }
            .startWith(ShowError(true))
    }

    private fun observeDownloadReportWorkerStatus(): Observable<PartialState>? {
        return intent<Intent.ObserveDownloadReportWorkerStatus>()
            .take(1)
            .switchMap {
                downloadReportWorkerStatusProvider.get()
                    .execute(
                        weakLifecycleOwner = it.weakLifecycleOwner,
                        workerName = getWorkName(
                            DownloadReport.ReportType.CUSTOMER_REPORT,
                            customerId
                        ),
                    )
            }.map {
                when {
                    it is WorkerStatus.Running -> {
                        shouldShowDownloadAlert = true
                        SetReportDownloading(true)
                    }
                    it is WorkerStatus.Error && shouldShowDownloadAlert
                    -> {
                        emitViewEvent(ViewEvent.ReportGenerationFailed(it.isInternetIssue))
                        SetReportDownloading(false)
                    }
                    it is WorkerStatus.Completed && shouldShowDownloadAlert
                    -> {
                        emitViewEvent(ViewEvent.DownloadedAlert)
                        SetReportDownloading(false)
                    }
                    else -> NoChange
                }
            }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is ShowDownloadLoader -> currentState.copy(showDownloadLoader = partialState.showDownloadLoader)
            is ShowError -> currentState.copy(
                showDownloadLoader = false,
                error = partialState.error
            )
            is ShowAlert -> currentState.copy(
                showDownloadLoader = false,
                isAlertVisible = true, messageId = partialState.messageId
            )
            is HideAlert -> currentState.copy(isAlertVisible = false)
            is SetNetworkError -> currentState.copy(
                showDownloadLoader = false,
                networkError = partialState.networkError
            )
            is ClearNetworkError -> currentState.copy(networkError = false)
            is NoChange -> currentState
            is ShowData -> currentState.copy(
                transactions = partialState.transactions,
                paymentTransactionCount = partialState.paymentTransactionCount,
                creditTransactionCount = partialState.creditTransactionCount,
                balanceForSelectedDuration = partialState.balanceForSelectedDuration,
                totalPayment = partialState.totalPayment,
                totalCredit = partialState.totalCredit,
                showDownloadLoader = false
            )
            is ShowAllTransactionsData -> currentState.copy(
                transactions = partialState.transactions,
                paymentTransactionCount = partialState.paymentTransactionCount,
                creditTransactionCount = partialState.creditTransactionCount,
                balanceForSelectedDuration = partialState.balanceForSelectedDuration,
                totalPayment = partialState.totalPayment,
                totalCredit = partialState.totalCredit,
                selectedMode = partialState.selectedMode,
                startDate = partialState.startDate,
                endDate = partialState.endDate,
                showDownloadLoader = false
            )
            is ChangeDateRange -> currentState.copy(
                customerId = partialState.customerId,
                startDate = partialState.startDate,
                endDate = partialState.endDate,
                selectedMode = partialState.selectedMode
            )
            is SetCollectionActivated -> currentState.copy(isCollectionAdopted = partialState.isCollectionAdopted)
            is IsDateFilterEducationShown -> currentState.copy(
                isDateFilterEducationShown = partialState.isDateFilterEducationShown
            )
            is SetCustomer -> currentState.copy(customer = partialState.customer)
            is IsReportShareEducationShown -> currentState.copy(
                isReportShareEducationShown = partialState.isReportShareEducationShown
            )
            is SetReportDownloading -> currentState.copy(isReportDownloading = partialState.isDownloading)
        }
    }
}
