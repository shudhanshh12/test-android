package `in`.okcredit.payment.ui.add_payment_dialog

import `in`.okcredit.collection.contract.CollectionDestinationType
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import merchant.okcredit.accounting.contract.model.LedgerType
import merchant.okcredit.accounting.contract.model.SupportType

interface AddPaymentDestinationContract {

    companion object {
        const val INVALID_ACCOUNT_NUMBER = 101
        const val INVALID_IFSC_CODE = 102
        const val INVALID_ACCOUNT_NUMBER_AND_IFSC_CODE = 104
    }

    data class State(
        val isLoading: Boolean = false,
        val isNetworkError: Boolean = false,
        val invalidBankAccountError: Boolean = false,
        val invalidBankAccountCode: Int = -1,
        val adoptionMode: CollectionDestinationType = CollectionDestinationType.UPI,
        val upiLoaderStatus: Boolean = false,
        val upiErrorServer: Boolean = false,
        val isPayOnlineEducationShown: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val errorMessage: String = "",
        val accountId: String = "",
        val mobile: String = "",
        val name: String? = "",
        val profileImage: String = "",
        val dueBalance: Long = 0L,
        val accountType: String = "",
        val supportType: SupportType = SupportType.NONE,
        val supportNumber: String = "",
        val support24x7String: String = "",
        val supportMsg: String = "",
    ) : UiState {
        fun getRelationFrmAccountType(): String {
            return if (accountType == LedgerType.SUPPLIER.value) PaymentAnalyticsEvents.PaymentPropertyValue.SUPPLIER
            else PaymentAnalyticsEvents.PaymentPropertyValue.CUSTOMER
        }
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        object ShowLoading : PartialState()

        data class SetNetworkError(val isNetworkError: Boolean) : PartialState()

        data class SetAdoptionMode(val adoptionMode: CollectionDestinationType) : PartialState()

        data class UpdateMerchantLoaderStatus(val upiLoaderStatus: Boolean) : PartialState()

        data class ShowInvalidUpiServerError(val upiErrorServer: Boolean) : PartialState()

        data class ShowInValidErrorStatus(val invalidBankAccountError: Boolean, val invalidBankAccountCode: Int) :
            PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        data class SetErrorMessage(val errorMessage: String) : PartialState()

        data class SetSupportData(
            val supportType: SupportType,
            val supportNumber: String,
            val support24x7String: String,
        ) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object LoadFirst : Intent()

        data class SetPaymentVpa(val vpa: String, val paymentType: String) : Intent()

        data class SetAdoptionMode(val adoptionMode: CollectionDestinationType) : Intent()

        object ClearUpiError : Intent()

        data class ShareRequestToWhatsApp(val sharingText: String) : Intent()

        data class SupportClicked(val msg: String, val number: String) : Intent()

        data class SendWhatsAppMessage(val msg: String, val number: String) : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {

        data class OnAccountAddedSuccessfully(
            val accountId: String,
            val paymentAddressType: String,
        ) : ViewEvents()

        data class ShareRequestToWhatsapp(val sharingText: String) : ViewEvents()

        data class ShowErrorMessage(val errorMessage: String) : ViewEvents()

        object CallCustomerCare : ViewEvents()

        object ShowWhatsAppError : ViewEvents()

        object ShowDefaultError : ViewEvents()

        data class SendWhatsAppMessage(val intent: android.content.Intent) : ViewEvents()
    }
}
