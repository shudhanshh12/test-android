package tech.okcredit.home.ui.business_health_dashboard

import `in`.okcredit.business_health_dashboard.contract.model.usecases.GetBusinessHealthDashboardData
import `in`.okcredit.business_health_dashboard.contract.model.usecases.RefreshBusinessHealthDashboardData
import `in`.okcredit.business_health_dashboard.contract.model.usecases.SetUserPreferredTimeCadence
import `in`.okcredit.business_health_dashboard.contract.model.usecases.SubmitFeedbackForTrend
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.network.ApiError
import tech.okcredit.base.network.utils.NetworkHelper
import tech.okcredit.home.ui.business_health_dashboard.BusinessHealthDashboardAnalyticsTracker.BusinessHealthDashboardValue.API_ERROR_TYPE
import tech.okcredit.home.ui.business_health_dashboard.BusinessHealthDashboardAnalyticsTracker.BusinessHealthDashboardValue.DASHBOARD_API_SOURCE
import tech.okcredit.home.ui.business_health_dashboard.BusinessHealthDashboardAnalyticsTracker.BusinessHealthDashboardValue.FEEDBACK_API_SOURCE
import tech.okcredit.home.ui.business_health_dashboard.BusinessHealthDashboardAnalyticsTracker.BusinessHealthDashboardValue.NO_INTERNET_ERROR_TYPE
import tech.okcredit.home.ui.business_health_dashboard.BusinessHealthDashboardContract.*
import javax.inject.Inject

class BusinessHealthDashboardViewModel @Inject constructor(
    initialState: Lazy<State>,
    private val getBusinessHealthDashboardData: Lazy<GetBusinessHealthDashboardData>,
    private val refreshBusinessHealthDashboardData: Lazy<RefreshBusinessHealthDashboardData>,
    private val setUserPreferredTimeCadence: Lazy<SetUserPreferredTimeCadence>,
    private val submitFeedbackForTrend: Lazy<SubmitFeedbackForTrend>,
    private val businessHealthDashboardEventTracker: Lazy<BusinessHealthDashboardAnalyticsTracker>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState.get()) {
    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            load(),
            submitFeedbackForTrend(),
            refreshDashboardData(),
            setUserPreferredTimeCadence(),
        )
    }

    private fun load(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap {
                pushIntent(Intent.RefreshDashboardData)
                wrap(getBusinessHealthDashboardData.get().execute())
            }
            .map {
                when (it) {
                    is Result.Failure -> PartialState.NoChange
                    is Result.Success -> PartialState.SetBusinessHealthDashboardData(it.value)
                    is Result.Progress -> PartialState.SetLoadingState
                }
            }
    }

    private fun refreshDashboardData(): Observable<PartialState> {
        return intent<Intent.RefreshDashboardData>()
            .switchMap {
                wrap(refreshBusinessHealthDashboardData.get().execute())
            }
            .map {
                when (it) {
                    is Result.Progress -> {
                        PartialState.NoChange
                    }
                    is Result.Success -> {
                        PartialState.NoChange
                    }
                    is Result.Failure -> {
                        if (NetworkHelper.isNetworkError(it.error)) {
                            businessHealthDashboardEventTracker.get().trackNetworkError(
                                source = DASHBOARD_API_SOURCE,
                                type = NO_INTERNET_ERROR_TYPE,
                                errorMessage = it.error.message ?: ""
                            )
                            PartialState.SetNetworkErrorStatus(NetworkErrorType.InternetError)
                        } else if (it.error is ApiError) {
                            businessHealthDashboardEventTracker.get().trackNetworkError(
                                source = DASHBOARD_API_SOURCE,
                                type = API_ERROR_TYPE,
                                errorMessage = it.error.message ?: ""
                            )
                            PartialState.SetNetworkErrorStatus(NetworkErrorType.ApiError)
                        } else {
                            PartialState.NoChange
                        }
                    }
                }
            }
    }

    private fun submitFeedbackForTrend(): Observable<PartialState> {
        return intent<Intent.SubmitFeedbackForTrend>()
            .switchMap {
                val response = if (it.feedbackType == FeedbackClickListener.FeedbackType.PositiveFeedback) {
                    submitFeedbackForTrend.get().POSITIVE_FEEDBACK_RESPONSE_STRING
                } else {
                    submitFeedbackForTrend.get().NEGATIVE_FEEDBACK_RESPONSE_STRING
                }
                wrap(submitFeedbackForTrend.get().execute(it.trendId, response))
            }
            .map {
                when (it) {
                    is Result.Progress -> {
                        PartialState.NoChange
                    }
                    is Result.Success -> {
                        PartialState.NoChange
                    }
                    is Result.Failure -> {
                        if (NetworkHelper.isNetworkError(it.error)) {
                            businessHealthDashboardEventTracker.get().trackNetworkError(
                                source = FEEDBACK_API_SOURCE,
                                type = NO_INTERNET_ERROR_TYPE,
                                errorMessage = it.error.message ?: ""
                            )
                            PartialState.SetNetworkErrorStatus(NetworkErrorType.InternetError)
                        } else if (it.error is ApiError) {
                            businessHealthDashboardEventTracker.get().trackNetworkError(
                                source = FEEDBACK_API_SOURCE,
                                type = API_ERROR_TYPE,
                                errorMessage = it.error.message ?: ""
                            )
                            PartialState.SetNetworkErrorStatus(NetworkErrorType.ApiError)
                        } else {
                            PartialState.NoChange
                        }
                    }
                }
            }
    }

    private fun setUserPreferredTimeCadence(): Observable<PartialState> {
        return intent<Intent.SetUserPreferredTimeCadence>()
            .switchMap {
                setUserPreferredTimeCadence.get().execute(it.timeCadenceTitle)
                    .andThen(Observable.just(PartialState.NoChange))
            }
    }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.SetLoadingState -> currentState.copy(
                dashboardData = DashboardData.Loading,
                networkErrorType = NetworkErrorType.NoNetworkError
            )
            is PartialState.SetBusinessHealthDashboardData -> currentState.copy(
                dashboardData = DashboardData.Available(
                    businessHealthDashboardModel = partialState.businessHealthDashboardModel
                )
            )
            is PartialState.SetNetworkErrorStatus -> {
                when (partialState.networkErrorType) {
                    NetworkErrorType.InternetError -> emitViewEvent(ViewEvent.ShowInternetErrorSnackbar)
                    NetworkErrorType.ApiError -> emitViewEvent(ViewEvent.ShowApiErrorSnackbar)
                }
                currentState.copy(networkErrorType = partialState.networkErrorType)
            }
        }
    }
}
