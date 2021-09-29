package `in`.okcredit.merchant.ui.switch_business

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import android.app.Activity
import java.lang.ref.WeakReference

interface SwitchBusinessContract {
    data class State(
        val businessModelList: List<BusinessModel> = listOf(),
    ) : UiState

    data class BusinessModel(
        val business: Business,
        val isActive: Boolean = false,
        val notifications: Int = 0,
    )

    sealed class Intent : UserIntent {
        object Load : Intent()
        object CreateNewBusiness : Intent()
        data class SetActiveBusiness(
            val businessId: String,
            val businessName: String,
            val weakActivity: WeakReference<Activity>?,
        ) : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetBusinessList(val businessModelList: List<BusinessModel>) : PartialState()
    }

    sealed class ViewEvent : BaseViewEvent {
        object ShowCreateBusinessDialog : ViewEvent()
        data class ShowError(val msg: String) : ViewEvent()
    }
}
