package tech.okcredit.home.ui.activity

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import tech.okcredit.home.ui.activity.viewpager.BottomMenuItem

interface HomeActivityContract {

    companion object {
        const val FEATURE_HOME_DASHBOARD = "home_dashboard"
        const val FEATURE_PAYMENTS = "nav_payments"
        const val FEATURE_HELP_SUPPORT = "nav_help_disable"
        const val FEATURE_BUSINESS_HEALTH_DASHBOARD = "business_health_dashboard"
    }

    object State : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class SetupExperiments(val bottomNavItemsList: List<BottomMenuItem>) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object OnResume : Intent()

        object DashboardEducationShown : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class GoToWebScreen(val webUrl: String) : ViewEvent()

        object GoToLogin : ViewEvent()

        object ShowDashboardEducation : ViewEvent()

        data class SetupViewPager(val bottomNavItemsList: List<BottomMenuItem>) : ViewEvent()
    }
}
