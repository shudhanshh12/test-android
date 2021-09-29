package `in`.okcredit.frontend.ui.supplier_reports

import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport.Companion.getWorkName
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReportFileNameProvider
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReportWorkerStatusProvider
import `in`.okcredit.backend._offline.usecase.reports_v2.GetReportV2UrlWithTimeout
import `in`.okcredit.backend._offline.usecase.reports_v2.ReportsV2Tracker
import `in`.okcredit.backend._offline.usecase.reports_v2.ReportsV2Tracker.PropertyValue.NETWORK_ERROR
import `in`.okcredit.backend._offline.usecase.reports_v2.ReportsV2Tracker.PropertyValue.SUCCESSS
import `in`.okcredit.backend._offline.usecase.reports_v2.ReportsV2Tracker.PropertyValue.SUPPLIER_REPORT
import `in`.okcredit.backend._offline.usecase.reports_v2.ReportsV2Tracker.PropertyValue.UNKNOWN_ERROR
import `in`.okcredit.backend._offline.usecase.reports_v2.WorkerStatus
import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.frontend.ui.supplier_reports.SupplierReportsContract.*
import `in`.okcredit.frontend.ui.supplier_reports.SupplierReportsContract.PartialState.*
import `in`.okcredit.frontend.usecase.GetAllTransactionsForSupplier
import `in`.okcredit.frontend.usecase.GetSupplierMiniStatementReport
import `in`.okcredit.frontend.usecase.GetSupplierStatementForDateRange
import `in`.okcredit.merchant.customer_ui.usecase.GetSharableReportIntent
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.supplier.usecase.GetSupplier
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SupplierReportsViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam("supplier_id") private val supplierId: String,
    private val getSupplierStatementForDateRange: Lazy<GetSupplierStatementForDateRange>,
    private val getSupplier: Lazy<GetSupplier>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getAllTransactionsForCustomer: Lazy<GetAllTransactionsForSupplier>,
    private val rxSharedPreference: Lazy<DefaultPreferences>,
    private val getMiniStatementReport: Lazy<GetSupplierMiniStatementReport>,
    private val downloadReportWorkerStatusProvider: Lazy<DownloadReportWorkerStatusProvider>,
    private val getReportUrlV2WithTimeout: Lazy<GetReportV2UrlWithTimeout>,
    private val downloadReportFileNameProvider: Lazy<DownloadReportFileNameProvider>,
    private val downloadReport: Lazy<DownloadReport>,
    private val getSharableReportIntent: Lazy<GetSharableReportIntent>,
    private val reportsV2Tracker: Lazy<ReportsV2Tracker>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    private val timeRange: BehaviorSubject<GetSupplierStatementForDateRange.Request> = BehaviorSubject.create()
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

            updateSupplierMobileAndShareReport(),

            intent<Intent.RxPreferenceBoolean>()
                .switchMap { wrap(rxCompletable { rxSharedPreference.get().set(it.key, it.value, it.scope) }) }
                .map { NoChange },

            intent<Intent.GetMiniStatementDateRange>()
                .switchMap { UseCase.wrapSingle(getMiniStatementReport.get().execute(supplierId)) }
                .switchMap {
                    when (it) {
                        is Result.Progress -> {
                            Observable.just(NoChange)
                        }
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.TrackPreviewEvent(it.value.supplierStatementResponse.transactions.isEmpty()))
                            Observable.just(
                                ShowAllTransactionsData(
                                    transactions = it.value.supplierStatementResponse.transactions,
                                    paymentTransactionCount = it.value.supplierStatementResponse.paymentTransactionCount,
                                    creditTransactionCount = it.value.supplierStatementResponse.creditTransactionCount,
                                    balanceForSelectedDuration = it.value.supplierStatementResponse.balanceForSelectedDateRange,
                                    totalPayment = it.value.supplierStatementResponse.totalPayment,
                                    totalCredit = it.value.supplierStatementResponse.totalCredit,
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

            observeDownloadReportWorkerStatus(),
            shareIntentToWhatsapp(),
        )
    }

    private fun getTransactionForSelectedPeriodObservable(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .take(1)
            .switchMap { timeRange }
            .switchMap {
                UseCase.wrapSingle(
                    getSupplierStatementForDateRange.get().execute(
                        GetSupplierStatementForDateRange.Request(
                            it.supplierId,
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

    private fun getAllTransactionsObservable(): Observable<PartialState>? {
        return intent<Intent.GetAllTransactions>()
            .switchMap {
                UseCase.wrapSingle(
                    getAllTransactionsForCustomer.get().execute(supplierId, it.selectedMode)
                )
            }
            .switchMap {
                when (it) {
                    is Result.Progress -> {
                        Observable.just(NoChange)
                    }
                    is Result.Success -> {
                        emitViewEvent(ViewEvent.TrackPreviewEvent(it.value.supplierStatementResponse.transactions.isEmpty()))
                        Observable.just(
                            ShowAllTransactionsData(
                                transactions = it.value.supplierStatementResponse.transactions,
                                paymentTransactionCount = it.value.supplierStatementResponse.paymentTransactionCount,
                                creditTransactionCount = it.value.supplierStatementResponse.creditTransactionCount,
                                balanceForSelectedDuration = it.value.supplierStatementResponse.balanceForSelectedDateRange,
                                totalPayment = it.value.supplierStatementResponse.totalPayment,
                                totalCredit = it.value.supplierStatementResponse.totalCredit,
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
            }
    }

    private fun getCustomerObservable(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap { getSupplier.get().execute(supplierId) }
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

    private fun updateSupplierMobileAndShareReport() = intent<Intent.UpdateMobileAndShareReport>()
        .switchMap { getSupplier.get().execute(supplierId) }
        .map {
            when (it) {
                is Result.Progress -> NoChange
                is Result.Success -> {
                    emitViewEvent(ViewEvent.StartShareReport)
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
                            reportType = DownloadReport.ReportType.SUPPLIER_REPORT,
                            accountId = supplierId,
                            startTimeSec = it.startDate,
                            endTimeSec = it.endDate,
                            workName = getWorkName(
                                DownloadReport.ReportType.SUPPLIER_REPORT,
                                supplierId
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

    private fun changeDateObservable(): Observable<ChangeDateRange>? {
        return intent<Intent.ChangeDateRange>()
            .map {
                timeRange.onNext(
                    GetSupplierStatementForDateRange.Request(
                        supplierId,
                        it.startDate,
                        it.endDate
                    )
                )
                ChangeDateRange(supplierId, it.startDate, it.endDate, it.selectedMode)
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
                            DownloadReport.ReportType.SUPPLIER_REPORT,
                            supplierId
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

    private fun shareReportObservable(): Observable<PartialState>? {
        return intent<Intent.ShareReportClicked>()
            .switchMap {
                getCurrentState().let {
                    wrap(
                        getReportUrlV2WithTimeout.get().execute(
                            accountId = supplierId,
                            it.startDate.millis,
                            it.endDate.millis,
                            DownloadReport.ReportType.SUPPLIER_REPORT.typeKeywordAtServer,
                        )
                    )
                }
            }
            .map { result ->
                when (result) {
                    is Result.Progress -> ShowDownloadLoader(true)
                    is Result.Success -> {
                        reportsV2Tracker.get().trackReportCreated(
                            reportTypeName = SUPPLIER_REPORT,
                            success = true,
                            accountId = supplierId,
                            response = SUCCESSS,
                        )
                        pushIntent(Intent.ShareReportToWhatsapp(result.value))
                        ShowDownloadLoader(false)
                    }
                    is Result.Failure -> {
                        var response = ""
                        when {
                            isInternetIssue(result.error) -> {
                                response = NETWORK_ERROR
                                emitViewEvent(ViewEvent.ReportGenerationFailed(isInternetIssue = true))
                            }

                            result.error is DownloadReport.ReportUrlGenerationApiError -> {
                                response = (result.error as DownloadReport.ReportUrlGenerationApiError)
                                    .errorMessage ?: ""
                                emitViewEvent(ViewEvent.ReportGenerationFailed(isInternetIssue = false))
                            }

                            else -> {
                                response = UNKNOWN_ERROR
                                emitViewEvent(ViewEvent.ReportGenerationFailed(isInternetIssue = false))
                            }
                        }

                        reportsV2Tracker.get().trackReportCreated(
                            reportTypeName = SUPPLIER_REPORT,
                            success = false,
                            accountId = supplierId,
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
                        getCurrentState().supplier?.mobile ?: "",
                        it.url,
                        downloadReportFileNameProvider.get().execute(
                            DownloadReport.Request(
                                reportType = DownloadReport.ReportType.SUPPLIER_REPORT,
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
                supplierId = partialState.supplierId,
                startDate = partialState.startDate,
                endDate = partialState.endDate,
                selectedMode = partialState.selectedMode
            )
            is SetCollectionActivated -> currentState.copy(isCollectionAdopted = partialState.isCollectionAdopted)
            is IsDateFilterEducationShown -> currentState.copy(
                isDateFilterEducationShown = partialState.isDateFilterEducationShown
            )
            is SetCustomer -> currentState.copy(supplier = partialState.customer)
            is IsReportShareEducationShown -> currentState.copy(
                isReportShareEducationShown = partialState.isReportShareEducationShown
            )
            is SetReportDownloading -> currentState.copy(isReportDownloading = partialState.isDownloading)
        }
    }
}
