package `in`.okcredit.frontend.ui.live_sales

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.frontend.usecase.GetLiveSalesStatement
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import org.joda.time.DateTime

interface LiveSalesContract {

    companion object {
        const val INVALID_ACCOUNT_NUMBER = 101
        const val INVALID_IFSC_CODE = 102
        const val INVALID_NAME = 103
        const val INVALID_ACCOUNT_NUMBER_AND_IFSC_CODE = 104
    }

    data class State(
        val isLoading: Boolean = true,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val customer: Customer? = null,
        val transactions: List<GetLiveSalesStatement.LiveSaleItemView> = arrayListOf(),
        val business: Business? = null,
        val error: Boolean = false,
        val networkError: Boolean = false,
        val isTxnExpanded: Boolean = false,
        val scrollTopTransactionDate: DateTime = DateTime(0),
        val merchantPaymentAddress: String? = null,
        val isCollectionActivated: Boolean = false,
        val upiErrorServer: Boolean = false,
        val upiLoaderStatus: Boolean = false,
        val invalidBankAccountError: Boolean = false,
        val invalidBankAccountCode: Int = -1,
        val adoptionMode: String = "",
        val isCollectionPopupOpen: Boolean? = null,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        data class ShowCustomer(val customer: Customer) : PartialState()

        data class ShowData(val transactions: MutableList<GetLiveSalesStatement.LiveSaleItemView>) : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        data class SetBusiness(val business: Business) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        object ExpandTransactions : PartialState()

        data class SetScrollTopTransaction(val date: DateTime) : PartialState()

        data class SetMerchantPaymentAddress(val merchantPaymentAddress: String?) :
            PartialState()

        data class SetCollectionActivated(val isCollectionActivated: Boolean) : PartialState()

        data class ShowInvalidUpiServerError(val upiErrorServer: Boolean) : PartialState()

        data class UpdateMerchantLoaderStatus(val upiLoaderStatus: Boolean) : PartialState()

        data class ShowInValidErrorStatus(val invalidBankAccountError: Boolean, val invalidBankAccountCode: Int) :
            PartialState()

        object ShowError : PartialState()

        object HideError : PartialState()

        data class SetAdoptionMode(val adoptionMode: String) : PartialState()

        data class SetCollectionPopupOpen(val isCollectionPopupOpen: Boolean?) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        // view txn
        data class ViewTransactionDetails(val txnId: String, val currentDue: Long) : Intent()

        // expand txs
        object ExpandTransactions : Intent()

        // set scroll top txn
        data class SetScrollTopTransaction(val date: DateTime) : Intent()

        // go to privacy screen
        object GoToPrivacyScreen : Intent()

        object UpdateLastViewTime : Intent()

        object ShowQrCodeDialog : Intent()

        object HideQrCodeDialog : Intent()

        data class SendWhatsAppReminder(val customerId: String) : Intent()

        data class SetUpiVpa(val upiVpa: String, val source: String) : Intent()

        data class ConfirmBankAccount(val paymentAddress: String) : Intent()

        data class SetAdoptionMode(val adoptionMode: String) : Intent()

        data class SetCollectionPopupOpen(val isCollectionPopupOpen: Boolean?) : Intent()
    }

    interface Navigator {
        fun gotoLogin()

        fun gotoTransactionScreen(transactionId: String, currentDue: Long)

        fun showQrCodePopup(
            customer: Customer,
            collectionCustomerProfile: CollectionCustomerProfile,
            business: Business?,
            merchantPaymentAddress: String?
        )

        fun gotoCustomerPrivacyScreen()

        fun shareReminder(intent: android.content.Intent)
    }
}
