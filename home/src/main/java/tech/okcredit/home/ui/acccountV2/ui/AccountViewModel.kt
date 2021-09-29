package tech.okcredit.home.ui.acccountV2.ui

import `in`.okcredit.backend._offline.IsWebLibraryEnabled
import `in`.okcredit.backend._offline.usecase.GetAccountSummary
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport.Companion.getWorkName
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport.ReportType.BACKUP_ALL
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReportWorkerStatusProvider
import `in`.okcredit.backend._offline.usecase.reports_v2.WorkerStatus
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.supplier.statement.usecase.GetSupplierBalanceAndCount
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.home.ui.acccountV2.ui.AccountContract.*
import tech.okcredit.home.ui.acccountV2.ui.AccountContract.Intent.*
import tech.okcredit.home.ui.acccountV2.ui.AccountContract.PartialState.*
import tech.okcredit.home.ui.acccountV2.ui.AccountContract.ViewEvent.*
import tech.okcredit.home.usecase.GetBusinessHealthDashboardEnabled
import javax.inject.Inject

class AccountViewModel @Inject constructor(
    initialState: State,
    private val getAccountSummary: Lazy<GetAccountSummary>,
    private var getActiveBusiness: Lazy<GetActiveBusiness>,
    private val downloadReportWorkerStatusProvider: Lazy<DownloadReportWorkerStatusProvider>,
    private val downloadReport: Lazy<DownloadReport>,
    private val isWebLibraryEnabled: Lazy<IsWebLibraryEnabled>,
    private val getSupplierBalanceAndCount: Lazy<GetSupplierBalanceAndCount>,
    private val getBusinessHealthDashboardEnabled: Lazy<GetBusinessHealthDashboardEnabled>,
    @ViewModelParam(AccountActivity.ARG_NOTIFICATION_URL) private val notificationUrl: String,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    private var shouldShowDownloadAlert = false

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Load>()
                .switchMap { UseCase.wrapObservable(getAccountSummary.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> NoChange
                        is Result.Success -> SetAccountSummary(it.value)
                        is Result.Failure -> NoChange
                    }
                },
            intent<Load>()
                .switchMap {
                    UseCase.wrapObservable(getActiveBusiness.get().execute())
                }.map {
                    when (it) {
                        is Result.Progress -> NoChange
                        is Result.Success -> SetMerchantName(it.value.name)
                        is Result.Failure -> NoChange
                    }
                },
            intent<Load>()
                .take(1)
                .filter { notificationUrl.isEmpty().not() && notificationUrl.contains("backup") }
                .map {
                    emitViewEvent(DeepLinkStartDownload)
                    NoChange
                },

            intent<Load>()
                .take(1)
                .switchMap {
                    UseCase.wrapObservable(isWebLibraryEnabled.get().execute())
                }.map {
                    when (it) {
                        is Result.Progress -> NoChange
                        is Result.Success -> SetWebTesting(it.value)
                        is Result.Failure -> NoChange
                    }
                },
            intent<WebLibraryClick>()
                .map {
                    emitViewEvent(GoToWebWebViewScreen)
                    NoChange
                },
            customerKhataClick(),
            supplierKhataClick(),
            getSupplierAccountSummary(),
            observeLastUpdatedAtVisibilityStatus(),
            intent<Intent.DownloadReport>()
                .map {
                    SetLoading(true)
                },
            intent<CheckSupplierCreditFeatureAndObserveWorkerStatus>()
                .take(1)
                .map {
                    pushIntent(ObserveDownloadReportWorkerStatus(it.weakLifecycleOwner, BACKUP_ALL))
                    NoChange
                },
            intent<ObserveDownloadReportWorkerStatus>()
                .take(1)
                .switchMap {
                    downloadReportWorkerStatusProvider.get().execute(
                        weakLifecycleOwner = it.weakLifecycleOwner,
                        workerName = getWorkName(it.reportType),
                    )
                }
                .map {
                    when {
                        it is WorkerStatus.Running -> {
                            shouldShowDownloadAlert = true
                            SetLoading(true)
                        }
                        it is WorkerStatus.Error && shouldShowDownloadAlert -> {
                            emitViewEvent(ReportGenerationFailed(it.isInternetIssue))
                            SetLoading(false)
                        }
                        it is WorkerStatus.Completed && shouldShowDownloadAlert ->
                            SetDownloadStatus(it.uriString)
                        else -> NoChange
                    }
                },
            intent<Intent.DownloadReport>()
                .switchMap {
                    shouldShowDownloadAlert = true
                    UseCase.wrapCompletable(
                        downloadReport.get().schedule(
                            DownloadReport.Request(
                                BACKUP_ALL,
                                workName = getWorkName(BACKUP_ALL)
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> SetLoading(true)
                        else -> NoChange
                    }
                }
        )
    }

    private fun observeLastUpdatedAtVisibilityStatus(): Observable<PartialState> {
        return intent<Load>()
            .switchMap { getBusinessHealthDashboardEnabled.get().execute() }
            .map {
                SetShouldShowLastUpdatedAtText(it)
            }
    }

    private fun getSupplierAccountSummary(): Observable<PartialState> {
        return intent<Load>()
            .switchMap { wrap(getSupplierBalanceAndCount.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> NoChange
                    is Result.Success -> SetSupplierBalanceAndCount(it.value)
                    is Result.Failure -> NoChange
                }
            }
    }

    private fun customerKhataClick(): Observable<PartialState> {
        return intent<CustomerKhataClick>()
            .map {
                emitViewEvent(GoToAccountStatementScreen)
                NoChange
            }
    }

    private fun supplierKhataClick(): Observable<PartialState> {
        return intent<SupplierKhataClick>()
            .map {
                emitViewEvent(GoToSupplierStatementScreen)
                NoChange
            }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is NoChange -> currentState
            is SetAccountSummary -> currentState.copy(accountSummary = partialState.accountSummary)
            is SetMerchantName -> currentState.copy(merchantName = partialState.merchantName)
            is SetLoading -> currentState.copy(
                isLoading = partialState.isLoading,
                isDownloaded = false
            )
            is SetWebTesting -> currentState.copy(isWebTestingActivated = partialState.isWebTestingActivated)
            is SetDownloadStatus -> currentState.copy(
                downloadedFileUriString = partialState.downloadedFilePath,
                isLoading = false,
                isDownloaded = true
            )
            is SetSupplierBalanceAndCount -> currentState.copy(supplierSummary = partialState.supplierSummary)
            is SetShouldShowLastUpdatedAtText -> currentState.copy(shouldShowLastUpdatedAtText = partialState.shouldShowLastUpdatedAtText)
        }
    }
}
