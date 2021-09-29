package `in`.okcredit.merchant.customer_ui.ui.customer.bottom_sheet.googlePay

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import androidx.annotation.StringRes

interface CollectWithGooglePayContract {

    data class State(
        val dueBalance: Long = 0L,
        val currentAmountSelected: Long? = null,
        val merchantId: String = "",
        val accountId: String = "",
        val customerMobile: String = "",
        val paymentAddress: String = "",
        val destinationType: String = "",
        val name: String = "",
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetCustomer(val customer: Customer) : PartialState()
        data class SetMerchantProfile(val merchantProfile: CollectionMerchantProfile) : PartialState()
        data class SetAmountEntered(val amount: Long) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class SetAmountEntered(val amount: Long) : Intent()
        data class CollectWithGooglePay(val amount: Long, val customerMobile: String) : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        object CollectWithGPayRequestSent : ViewEvents()
        object DismissBottomSheet : ViewEvents()
        data class ShowError(@StringRes val error: Int) : ViewEvents()
    }

    companion object {
        const val ARG_CUSTOMER_ID = "customer_id"
    }
}
