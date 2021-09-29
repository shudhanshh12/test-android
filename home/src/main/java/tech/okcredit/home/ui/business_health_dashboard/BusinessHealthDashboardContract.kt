package tech.okcredit.home.ui.business_health_dashboard

import `in`.okcredit.business_health_dashboard.contract.model.BusinessHealthDashboardModel
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface BusinessHealthDashboardContract {
    data class State(
        val dashboardData: DashboardData = DashboardData.Loading,
        val networkErrorType: NetworkErrorType = NetworkErrorType.NoNetworkError
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        object SetLoadingState : PartialState()

        data class SetBusinessHealthDashboardData(
            val businessHealthDashboardModel: BusinessHealthDashboardModel,
        ) : PartialState()

        data class SetNetworkErrorStatus(val networkErrorType: NetworkErrorType) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object RefreshDashboardData : Intent()

        data class SubmitFeedbackForTrend(
            val trendId: String,
            val feedbackType: FeedbackClickListener.FeedbackType,
        ) : Intent()

        data class SetUserPreferredTimeCadence(val timeCadenceTitle: String) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object ShowInternetErrorSnackbar : ViewEvent()
        object ShowApiErrorSnackbar : ViewEvent()
    }
}

sealed class DashboardData {
    object Loading : DashboardData()
    data class Available(val businessHealthDashboardModel: BusinessHealthDashboardModel) : DashboardData()
}

sealed class NetworkErrorType {
    object NoNetworkError : NetworkErrorType()
    object InternetError : NetworkErrorType()
    object ApiError : NetworkErrorType()
}
