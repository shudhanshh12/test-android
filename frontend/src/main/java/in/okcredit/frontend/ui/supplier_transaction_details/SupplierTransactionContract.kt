package `in`.okcredit.frontend.ui.supplier_transaction_details

import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import merchant.okcredit.accounting.contract.model.SupportType

interface SupplierTransactionContract {

    data class State(
        val isLoading: Boolean = true,
        val transaction: Transaction? = null,
        val supplier: Supplier? = null,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val isSmsSent: Boolean = false,
        val syncing: Boolean = false,
        val deleteStatus: DeleteLayoutStatus = DeleteLayoutStatus.Unknown,
        val business: Business? = null,
        val referralId: String? = null,
        val collection: Collection? = null,
        val supportType: SupportType = SupportType.NONE,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object ShowLoading : SupplierTransactionContract.PartialState()

        data class SetTransaction(val transaction: Transaction) : SupplierTransactionContract.PartialState()

        data class SetSupplier(val supplier: Supplier) : SupplierTransactionContract.PartialState()

        object ErrorState : SupplierTransactionContract.PartialState()

        data class ShowAlert(val message: String) : SupplierTransactionContract.PartialState()

        object HideAlert : SupplierTransactionContract.PartialState()

        object NoChange : SupplierTransactionContract.PartialState()

        data class SetBusiness(val business: Business) : SupplierTransactionContract.PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class SyncTransaction(val syncing: Boolean) : PartialState()

        data class SetDeleteStatus(val deleteStatus: DeleteLayoutStatus) : PartialState()

        data class SetReferralId(val referralId: String) : PartialState()

        data class SetCollection(val collection: Collection) : PartialState()

        data class SetSupportType(val type: SupportType) : PartialState()
    }

    sealed class DeleteLayoutStatus {
        object Active : DeleteLayoutStatus()

        object InActive : DeleteLayoutStatus()

        object Unknown : DeleteLayoutStatus()
    }

    sealed class Intent : UserIntent {
        // load
        object Load : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        // delete tx
        object Delete : Intent()

        // sync tx
        object SyncTransaction : Intent()

        object ShareOnWhatsApp : Intent()

        // click know more
        data class OnKnowMoreClicked(val id: String) : Intent()

        data class WhatsApp(val contactPermissionAvailable: Boolean) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        data class GoToDeletePage(val transactionId: String) : ViewEvent()

        data class GoToWhatsAppShare(val supplier: Supplier, val business: Business, val transaction: Transaction) :
            ViewEvent()

        data class GoToKnowMoreScreen(val it: String, val accountType: String) : ViewEvent()

        data class GoToWhatsApp(val okCreditNumber: String) : ViewEvent()

        object WhatsAppOptIn : ViewEvent()

        object GoToHome : ViewEvent()
    }
}
