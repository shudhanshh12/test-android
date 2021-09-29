package `in`.okcredit.frontend.ui.sync

import `in`.okcredit.accounting_core.contract.SyncState
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.usecase.LoginDataSyncerImpl
import `in`.okcredit.frontend.usecase.language_experiment.ShouldShowSelectBusinessFragment
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.utils.getStringStackTrace
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncViewModel @Inject constructor(
    initialState: SyncContract.State,
    private val syncAuthScope: Lazy<LoginDataSyncerImpl>,
    private val tracker: Lazy<Tracker>,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val shouldShowSelectBusinessFragment: Lazy<ShouldShowSelectBusinessFragment>,
) : BaseViewModel<SyncContract.State, SyncContract.PartialState, SyncContract.ViewEvent>(initialState) {

    private val reload: PublishSubject<Unit> = PublishSubject.create()
    private val syncPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    private val showAlertPublishSubject: PublishSubject<String> = PublishSubject.create()
    private val syncUpdatePublishSubject: BehaviorSubject<SyncState> = BehaviorSubject.createDefault(SyncState.WAITING)
    private var isFile = false

    override fun handle(): Observable<UiState.Partial<SyncContract.State>> {
        return mergeArray(

            // hide network error when network becomes available
            intent<SyncContract.Intent.Load>()
                .switchMap { checkNetworkHealth.get().execute(Unit) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        reload.onNext(Unit)
                        SyncContract.PartialState.ClearNetworkError
                    } else {
                        SyncContract.PartialState.NoChange
                    }
                },

            // Start sync on load
            intent<SyncContract.Intent.Load>()
                .take(1)
                .switchMap { getActiveBusinessId.get().execute().toObservable() }
                .map {
                    tracker.get().trackSyncStarted()
                    syncPublishSubject.onNext(Unit)
                    SyncContract.PartialState.NoChange
                },

            // Retry
            intent<SyncContract.Intent.Retry>()
                .map {
                    SyncContract.PartialState.SetErrorState(false)
                }.doAfterNext {
                    syncUpdatePublishSubject.onNext(SyncState.WAITING)
                    syncPublishSubject.onNext(Unit)
                },

            syncPublishSubject
                .switchMap { syncAuthScope.get().execute(syncUpdatePublishSubject) }
                .map {
                    when (it) {
                        is Result.Progress -> SyncContract.PartialState.NoChange
                        is Result.Success -> {
                            SyncContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    tracker.get().trackSyncError(
                                        isFile,
                                        "Presenter",
                                        "Auth Failure",
                                        it.error.getStringStackTrace()
                                    )
                                    emitViewEvent(SyncContract.ViewEvent.GotoLogin)
                                    SyncContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    tracker.get().trackSyncError(
                                        isFile,
                                        "Presenter",
                                        "No Internet",
                                        it.error.getStringStackTrace()
                                    )
                                    SyncContract.PartialState.ShowNoInternet(true)
                                }
                                else -> {
                                    tracker.get().trackSyncError(
                                        isFile,
                                        "Usecase",
                                        it.error.message,
                                        it.error.getStringStackTrace()
                                    )
                                    SyncContract.PartialState.SetErrorState(true)
                                }
                            }
                        }
                    }
                },

            syncUpdatePublishSubject.map {
                Timber.d("<<<<FileDownload SyncState $it")
                if (it == SyncState.DOWNLOADING) {
                    isFile = true
                }
                if (it == SyncState.COMPLETED) {
                    tracker.get().trackSyncCompleted(isFile)
                    pushIntent(SyncContract.Intent.GoToSelectBusinessOrHome)
                }
                SyncContract.PartialState.SetDataProgress(it)
            },

            // handle `show alert`
            showAlertPublishSubject
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<SyncContract.PartialState> { SyncContract.PartialState.HideAlert }
                        .startWith(SyncContract.PartialState.ShowAlert(it))
                },

            canShowSelectBusiness(),
        )
    }

    private fun canShowSelectBusiness() = intent<SyncContract.Intent.GoToSelectBusinessOrHome>()
        .switchMap {
            wrap { shouldShowSelectBusinessFragment.get().execute() }
        }
        .map {
            if (it is Result.Success) {
                if (it.value) {
                    emitViewEvent(SyncContract.ViewEvent.GoToSelectBusinessOrHome)
                } else {
                    emitViewEvent(SyncContract.ViewEvent.GoHome)
                }
                return@map SyncContract.PartialState.NoChange
            }
            SyncContract.PartialState.NoChange
        }

    override fun reduce(currentState: SyncContract.State, partialState: SyncContract.PartialState): SyncContract.State {
        return when (partialState) {
            is SyncContract.PartialState.ShowNoInternet -> currentState.copy(
                isSyncRetryVisible = true,
                syncState = SyncState.NETWORK_ERROR
            )
            is SyncContract.PartialState.SetErrorState -> currentState.copy(error = partialState.error)
            is SyncContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is SyncContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is SyncContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError
            )
            is SyncContract.PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is SyncContract.PartialState.SetVisibilityOfRetry -> currentState.copy(isSyncRetryVisible = partialState.status)
            is SyncContract.PartialState.SetDataProgress -> currentState.copy(syncState = partialState.syncState)
            is SyncContract.PartialState.NoChange -> currentState
        }
    }
}
