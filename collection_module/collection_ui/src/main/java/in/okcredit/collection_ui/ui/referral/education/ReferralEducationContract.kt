package `in`.okcredit.collection_ui.ui.referral.education

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface ReferralEducationContract {

    data class State(
        val error: String = "",
        val customerIdFrmLedger: String? = null,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object InviteClicked : Intent()
        object HelpClicked : Intent()
        object InviteOnWhatsApp : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        object GotoReferralListScreen : ViewEvents()
        data class HelpClicked(val ids: List<String>) : ViewEvents()
        data class InviteOnWhatsApp(val intent: android.content.Intent) : ViewEvents()
        object ShowWhatsappError : ViewEvents()
        object ShowError : ViewEvents()
    }
}
