package `in`.okcredit.supplier.supplier_profile_bottom_sheet

import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface SupplierProfileContract {

    data class State(
        val business: Business? = null,
        val supplier: Supplier? = null,
        val supplierCollectionCustomerProfile: CollectionCustomerProfile? = null,
        val unreadMessageCount: String = "",
        val firstUnseenMessageId: String? = null,
        val isChatEnabled: Boolean = false,
        val isPaymentEnabled: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetSupplier(val supplier: Supplier) : PartialState()
        data class SetUnreadMessageCount(val unreadMessageCount: String, val firstUnseenMessageId: String?) :
            PartialState()

        data class SetChatEnabled(val isEnabled: Boolean) : PartialState()
        data class SetPaymentEnabled(val isEnabled: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object LoadFirst : Intent()
        object SendWhatsAppReminder : Intent()
        object ActionOnCall : Intent()
        object RedirectToChatScreen : Intent()
        object GoToSupplierPaymentScreen : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        object GoToLogin : ViewEvents()
        data class ShowToast(val msg: String) : ViewEvents()
        data class AddSupplierMobile(val supplierId: String) : ViewEvents()
        data class CallToSupplier(val mobile: String) : ViewEvents()
        data class ShareWhatsappReminder(val mobile: String?, val name: String) : ViewEvents()
        object RedirectToChatScreen : ViewEvents()
        data class GoToSupplierPaymentScreen(val supplierId: String) : ViewEvents()
    }
}
