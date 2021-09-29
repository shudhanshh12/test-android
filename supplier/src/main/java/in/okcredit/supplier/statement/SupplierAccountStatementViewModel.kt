package `in`.okcredit.supplier.statement

import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport.Companion.getWorkName
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReportWorkerStatusProvider
import `in`.okcredit.backend._offline.usecase.reports_v2.WorkerStatus
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.shared.utils.CommonUtils
import `in`.okcredit.supplier.statement.AccountStatementModel.StatementSummary
import `in`.okcredit.supplier.statement.SupplierAccountStatementContract.Intent
import `in`.okcredit.supplier.statement.SupplierAccountStatementContract.PartialState
import `in`.okcredit.supplier.statement.SupplierAccountStatementContract.State
import `in`.okcredit.supplier.statement.SupplierAccountStatementContract.ViewEvent
import `in`.okcredit.supplier.statement.usecase.GetSupplierAccountSummary
import `in`.okcredit.supplier.statement.usecase.SupplierRequest
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class SupplierAccountStatementViewModel @Inject constructor(
    initialState: State,
    private val getSupplierSummary: Lazy<GetSupplierAccountSummary>,
    @ViewModelParam(ARG_SOURCE) val source: String,
    @ViewModelParam(ARG_DURATION) val duration: String,
    private val downloadReportWorkerStatusProvider: Lazy<DownloadReportWorkerStatusProvider>,
    private val downloadReport: Lazy<DownloadReport>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    companion object {
        const val ARG_DURATION = "duration"
        const val ARG_SOURCE = "source"
    }

    private val timeRange: BehaviorSubject<SupplierRequest> = BehaviorSubject.createDefault(
        SupplierRequest(
            initialState.startDate,
            initialState.endDate,
            initialState.showLoadMore
        )
    )
    private var isInitialLoad = true
    private var shouldShowDownloadAlert = false

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(

            // load page
            intent<Intent.Load>()
                .switchMap { timeRange }
                .switchMap {
                    UseCase.wrapObservable(
                        getSupplierSummary.get().execute(
                            SupplierRequest(
                                it.startTime,
                                it.endTime,
                                it.isShowAll
                            )
                        )
                    )
                }
                .map { result ->
                    when (result) {
                        is Result.Progress -> PartialState.ShowLoading
                        is Result.Success -> {
                            // Default is today. if there is no txn today, we are setting default to 1 month
                            val durationInDays = duration.toIntOrNull()
                            if (durationInDays != null && isInitialLoad) {
                                isInitialLoad = false
                                val startDate =
                                    CommonUtils.currentDateTime().withTimeAtStartOfDay().minusDays(durationInDays)
                                val endDate =
                                    CommonUtils.currentDateTime().plusDays(1).withTimeAtStartOfDay().minusMillis(1)
                                timeRange.onNext(SupplierRequest(startDate, endDate, false))
                                PartialState.ChangeDateRange(startDate, endDate)
                            } else if (result.value.supplierTransactionWrappers.isEmpty() && isInitialLoad) {
                                isInitialLoad = false
                                val startDate = CommonUtils.currentDateTime().withTimeAtStartOfDay().minusMonths(1)
                                val endDate =
                                    CommonUtils.currentDateTime().plusDays(1).withTimeAtStartOfDay().minusMillis(1)
                                timeRange.onNext(SupplierRequest(startDate, endDate, false))
                                PartialState.ChangeDateRange(startDate, endDate)
                            } else {
                                isInitialLoad = false
                                val summaryModel = StatementSummary(
                                    total = result.value.totalCreditAmount - result.value.totalPaymentAmount,
                                    paymentAmount = result.value.totalPaymentAmount,
                                    paymentCount = result.value.totalPaymentCount,
                                    creditCount = result.value.totalCreditCount,
                                    creditAmount = result.value.totalCreditAmount,
                                )
                                val transactionModels =
                                    result.value.supplierTransactionWrappers.map { it.toTransactionStatementModel() }
                                PartialState.AccountStatementModels(
                                    summaryModel,
                                    transactionModels,
                                    result.value.showLoadMore
                                )
                            }
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(result.error) -> PartialState.NetworkError
                                else -> PartialState.ErrorState
                            }
                        }
                    }
                },

            // change date range
            intent<Intent.ChangeDateRange>()
                .doOnNext {
                    timeRange.onNext(SupplierRequest(it.startDate, it.endDate, timeRange.value?.isShowAll ?: false))
                }
                .map { PartialState.ChangeDateRange(it.startDate, it.endDate) },

            // load all items
            intent<Intent.LoadOldTransactions>()
                .doOnNext {
                    timeRange.onNext(SupplierRequest(timeRange.value?.startTime!!, timeRange.value?.endTime!!, true))
                }.map { PartialState.NoChange },

            intent<Intent.SelectOnlineTransactions>()
                .map { PartialState.ShowOnlyOnlineTransactions(it.isOnlineTransationSelected) },

            intent<Intent.ObserveWorkerStatus>()
                .take(1)
                .switchMap {
                    downloadReportWorkerStatusProvider.get()
                        .execute(
                            weakLifecycleOwner = it.weakLifecycleOwner,
                            workerName = getWorkName(DownloadReport.ReportType.SUPPLIER_ACCOUNT),
                        )
                }.map {
                    when {
                        it is WorkerStatus.Running -> {
                            shouldShowDownloadAlert = true
                            PartialState.SetDownloadingStatus(true)
                        }
                        it is WorkerStatus.Error && shouldShowDownloadAlert -> {
                            if (it.isInternetIssue) PartialState.NetworkError
                            else PartialState.ErrorState
                        }
                        it is WorkerStatus.Completed && shouldShowDownloadAlert -> {
                            PartialState.ShowDownloadAlert(it.uriString)
                        }
                        else -> PartialState.NoChange
                    }
                },

            intent<Intent.DownloadStatement>()
                .switchMap {
                    shouldShowDownloadAlert = true
                    UseCase.wrapCompletable(
                        downloadReport.get().schedule(
                            DownloadReport.Request(
                                reportType = DownloadReport.ReportType.SUPPLIER_ACCOUNT,
                                accountId = null,
                                startTimeSec = it.startDate,
                                endTimeSec = it.endDate,
                                workName = getWorkName(DownloadReport.ReportType.SUPPLIER_ACCOUNT)
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.SetDownloadingStatus(true)
                        else -> PartialState.NoChange
                    }
                },
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.ShowLoading -> currentState.copy(
                statementModels = listOf(AccountStatementModel.Loading),
                showDownloadAlert = false,
            )
            is PartialState.ErrorState -> currentState.copy(
                error = true,
                isDownloading = false,
                showDownloadAlert = false,
            )
            is PartialState.ChangeDateRange -> currentState.copy(
                startDate = partialState.startDate,
                endDate = partialState.endDate
            )
            is PartialState.NetworkError -> currentState.copy(
                statementModels = listOf(AccountStatementModel.NetworkError),
                isDownloading = false,
                error = false,
                showDownloadAlert = false
            )
            is PartialState.ShowOnlyOnlineTransactions -> currentState.copy(
                showOnlyOnlineTransactions = partialState.showOnlyOnlineTransactions,
                statementModels = getModels(
                    partialState.showOnlyOnlineTransactions,
                    currentState.summaryModel,
                    currentState.transactionModels,
                    currentState.showLoadMore
                )
            )
            is PartialState.AccountStatementModels -> currentState.copy(
                error = false,
                summaryModel = partialState.summaryModel,
                transactionModels = partialState.transactionModels,
                showLoadMore = partialState.showLoadMore,
                statementModels = getModels(
                    currentState.showOnlyOnlineTransactions,
                    partialState.summaryModel,
                    partialState.transactionModels,
                    partialState.showLoadMore
                )
            )
            is PartialState.SetDownloadingStatus -> currentState.copy(
                isDownloading = partialState.status,
                error = false,
                showDownloadAlert = false
            )
            is PartialState.ShowDownloadAlert -> currentState.copy(
                isDownloading = false,
                showDownloadAlert = true,
                downloadedFileUriString = partialState.downloadedFilePath
            )
        }
    }

    private fun getModels(
        showOnlyOnlineTransactions: Boolean,
        summaryModel: StatementSummary?,
        transactionModels: List<AccountStatementModel.Transaction>?,
        showLoadMore: Boolean,
    ): List<AccountStatementModel> {
        return if (showOnlyOnlineTransactions) {
            getOnlineTransactionModels(summaryModel, transactionModels, showLoadMore)
        } else {
            getAllTransactionModels(summaryModel, transactionModels, showLoadMore)
        }
    }

    private fun getOnlineTransactionModels(
        summaryModel: StatementSummary?,
        transactionModels: List<AccountStatementModel.Transaction>?,
        showLoadMore: Boolean,
    ): List<AccountStatementModel> {
        val list = mutableListOf<AccountStatementModel>()
        val onlineTransactions = transactionModels?.filter { m -> m.wrapper.transaction.isOnlineTransaction() }
        onlineTransactions?.let { transactions ->
            summaryModel?.let { list.add(it) }
            list.addAll(transactions)
            if (showLoadMore) {
                list.add(AccountStatementModel.LoadMore)
            }
        } ?: list.add(AccountStatementModel.Empty)
        return list
    }

    private fun getAllTransactionModels(
        summaryModel: StatementSummary?,
        transactionModels: List<AccountStatementModel.Transaction>?,
        showLoadMore: Boolean,
    ): List<AccountStatementModel> {
        val list = mutableListOf<AccountStatementModel>()
        transactionModels?.let { transactions ->
            summaryModel?.let { list.add(it) }
            list.addAll(transactions)
            if (showLoadMore) {
                list.add(AccountStatementModel.LoadMore)
            }
        } ?: list.add(AccountStatementModel.Empty)
        return list
    }
}
