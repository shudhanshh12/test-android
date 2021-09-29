package `in`.okcredit.collection_ui.ui.passbook.detail

import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import android.graphics.Bitmap

interface PaymentDetailContract {
    data class State(
        val source: String = "",
        val error: Boolean = false,
        val collectionOnlinePayment: CollectionOnlinePayment? = null,
        val merchantPaymentAddress: String = "",
        val txnId: String = ""
    ) : UiState

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class SendWhatsApp(val image: Bitmap) : Intent()
        object OpenWhatsAppForHelp : Intent()
        object ShowAddMerchantDestinationDialog : Intent()
        data class TriggerMerchantPayout(val payoutType: String) : Intent()
        object ShowInvalidAddressToolTip : Intent()
        object ShowRefundConsentBottomSheet : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetCollectionOnlinePayment(val collectionOnlinePayment: CollectionOnlinePayment) : PartialState()
        data class SetMerchantPaymentAddress(val address: String) : PartialState()
    }

    sealed class ViewEvents : BaseViewEvent {
        data class ShowError(val errorMsg: String) : ViewEvents()
        data class SendWhatsApp(val intent: android.content.Intent) : ViewEvents()
        data class OpenWhatsAppForHelp(val intent: android.content.Intent) : ViewEvents()
        object ShowAddMerchantDestinationDialog : ViewEvents()
        object ShowInvalidAddressToolTip : ViewEvents()
        object ShowRefundConsentBottomSheet : ViewEvents()
    }
}
