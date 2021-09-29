package `in`.okcredit.merchant.customer_ui.ui.discount_details

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import tech.okcredit.camera_contract.CapturedImage

interface DiscountDetailsContract {

    data class State(
        val isLoading: Boolean = true,
        val transaction: merchant.okcredit.accounting.model.Transaction? = null,
        val customer: Customer? = null,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val isSmsSent: Boolean = false,
        val collection: Collection? = null,
        val canOpenNoteEditor: Boolean = false,
        val deleteStatus: DeleteLayoutStatus = DeleteLayoutStatus.Active,
        val business: Business? = null,
        val referralId: String? = null,
        val contextualHelpIds: List<String> = emptyList(),
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object ShowLoading : DiscountDetailsContract.PartialState()

        data class SetTransactionDetails(
            val transaction: merchant.okcredit.accounting.model.Transaction,
            val customer: Customer,
        ) : DiscountDetailsContract.PartialState()

        object ErrorState : DiscountDetailsContract.PartialState()

        data class ShowAlert(val message: String) : DiscountDetailsContract.PartialState()

        object HideAlert : DiscountDetailsContract.PartialState()

        object NoChange : DiscountDetailsContract.PartialState()

        data class SetMerchant(val business: Business) : DiscountDetailsContract.PartialState()

        object SmsSent : DiscountDetailsContract.PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class SetCollection(val collection: Collection) : PartialState()

        data class NoteEditorState(val canOpenNoteEditor: Boolean) : PartialState()

        data class SetDeleteStatus(val deleteStatus: DeleteLayoutStatus) : PartialState()

        data class SetReferralId(val referralId: String) : PartialState()

        data class SetContextualHelpIds(val helpIds: List<String>) : PartialState()
    }

    sealed class DeleteLayoutStatus {
        object Active : DeleteLayoutStatus()
        object InActive : DeleteLayoutStatus()
    }

    class ImagesInfo {
        var existingImages: ArrayList<merchant.okcredit.accounting.model.TransactionImage> = ArrayList()
        var deletedImages: ArrayList<merchant.okcredit.accounting.model.TransactionImage> = ArrayList()
        var newAddedImages: ArrayList<CapturedImage> = ArrayList()
        var tempImages: ArrayList<merchant.okcredit.accounting.model.TransactionImage> = ArrayList()
        var transactionId: String? = null
    }

    sealed class Intent : UserIntent {
        // load
        object Load : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        // delete tx
        object Delete : Intent()

        // share tx
        object ShareOnWhatsApp : Intent()

        // sync tx
        object SyncTransaction : Intent()

        // open sms app
        object OpenSmsApp : Intent()

        data class Note(val canShowNoteInput: Boolean) : Intent()

        data class NoteSubmitClicked(val note: Pair<String, String>) : Intent()

        data class ImagesChanged(val pair: Triple<ArrayList<merchant.okcredit.accounting.model.TransactionImage>, ArrayList<merchant.okcredit.accounting.model.TransactionImage>, String>) :
            Intent()

        data class NewImagesAdded(val list: Triple<ArrayList<CapturedImage>, List<merchant.okcredit.accounting.model.TransactionImage>, String>) :
            Intent()

        data class OnImagesChanged(val imagesInfo: ImagesInfo, val isDirtyTransaction: Boolean) : Intent()

        data class OnKnowMoreClicked(val id: String) : Intent()

        data class SubmitVoiceInput(val voiceInputText: String) : Intent()

        data class WhatsApp(val contactPermissionAvailable: Boolean) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent

    interface Navigator {
        fun gotoLogin()
        fun goToSmsApp(mobile: String, smsContent: String)
        fun goToDeletePage(transactionId: String)
        fun goToWhatsappShare(
            customer: Customer,
            business: Business,
            transaction: merchant.okcredit.accounting.model.Transaction,
        )

        fun goBack()
        fun goToKnowMoreScreen(it: String, accountType: String)
        fun goToWhatsAppOptIn()
        fun openWhatsApp(okCreditNumber: String)
    }

    companion object {
        const val ARG_TRANSACTION_ID = "transaction_id"
    }
}
