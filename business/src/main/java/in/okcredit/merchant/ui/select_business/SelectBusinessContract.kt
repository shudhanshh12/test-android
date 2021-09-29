package `in`.okcredit.merchant.ui.select_business

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import android.app.Activity
import java.lang.ref.WeakReference

interface SelectBusinessContract {

    data class State(
        val businessList: List<BusinessData> = listOf(),
    ) : UiState

    data class BusinessData(
        val business: Business,
        val balanceAmount: Long? = null,
    )

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class SetActiveBusiness(
            val activeBusinessId: String,
            val businessName: String,
            val weakActivity: WeakReference<Activity>? = null,
        ) : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetBusinessData(val businessDataList: List<BusinessData>) : PartialState()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class ShowError(val msg: String) : ViewEvent()
    }
}
