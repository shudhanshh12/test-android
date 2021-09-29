package `in`.okcredit.merchant.customer_ui.ui.transaction_details

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.core.model.TransactionAmountHistory
import `in`.okcredit.merchant.customer_ui.data.server.model.response.Subscription
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import merchant.okcredit.accounting.contract.model.SupportType
import merchant.okcredit.accounting.model.Transaction
import merchant.okcredit.accounting.model.TransactionImage
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.camera_contract.CapturedImage

interface TransactionContract {

    data class State(
        val isLoading: Boolean = true,
        val transaction: Transaction? = null,
        val customer: Customer? = null,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val isSmsSent: Boolean = false,
        val collection: Collection? = null,
        val canOpenNoteEditor: Boolean = false,
        val deleteStatus: DeleteLayoutStatus = DeleteLayoutStatus.Unkown,
        val business: Business? = null,
        val referralId: String? = null,
        val transactionAmountHistory: TransactionAmountHistory? = null,
        val isEditTxnAmountEnabled: Boolean = false,
        val isTxnViewExpanded: Boolean = false,
        val isEditAmountEducationShown: Boolean? = null,
        val showViewHistoryLoader: Boolean = false,
        val isPasswordSet: Boolean = false,
        val isFourDigitPin: Boolean = false,
        val isMerchantSync: Boolean = false,
        val isSingleListEnabled: Boolean = false,
        val showSubscription: Boolean = false,
        val subscriptionName: String? = null,
        val contextualHelpIds: List<String> = emptyList(),
        val supportType: SupportType = SupportType.NONE,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object ShowLoading : TransactionContract.PartialState()

        data class SetTransactionDetails(val transaction: Transaction, val deleteStatus: DeleteLayoutStatus) :
            TransactionContract.PartialState()

        data class SetCustomerDetails(val customer: Customer) : TransactionContract.PartialState()

        object ErrorState : TransactionContract.PartialState()

        data class ShowAlert(val message: String) : TransactionContract.PartialState()

        object HideAlert : TransactionContract.PartialState()

        object NoChange : TransactionContract.PartialState()

        data class SetBusiness(val business: Business) : TransactionContract.PartialState()

        object SmsSent : TransactionContract.PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class SetCollection(val collection: Collection) : PartialState()

        data class NoteEditorState(val canOpenNoteEditor: Boolean) : PartialState()

        data class SetDeleteStatus(val deleteStatus: DeleteLayoutStatus) : PartialState()

        data class SetReferralId(val referralId: String) : PartialState()

        data class SetTransactionAmountHistory(val transactionAmountHistory: TransactionAmountHistory) : PartialState()

        data class IsEditTransactionAmountEnabled(val isEditTxnAmountEnabled: Boolean) : PartialState()

        data class IsTxnViewExpanded(val isTxnViewExpanded: Boolean) : PartialState()

        data class IsEditAmountEducationShown(val isEditAmountEducationShown: Boolean) : PartialState()

        data class ShowViewHistoryLoader(val showViewHistoryLoader: Boolean) : PartialState()

        data class SetIsPasswordSet(val isPasswordSet: Boolean) : PartialState()

        data class SetIsFourDigitPin(val isFourDigitPin: Boolean) : PartialState()

        data class SetIsMerchantSync(val isMerchantSync: Boolean) : PartialState()

        data class IsSingleListEnabled(val isSingleListEnabled: Boolean) : PartialState()

        data class SubscriptionName(val name: String) : PartialState()

        data class SetContextualHelpIds(val helpIds: List<String>) : PartialState()

        data class SetSupportType(val type: SupportType) : PartialState()
    }

    sealed class DeleteLayoutStatus {
        object Active : DeleteLayoutStatus()
        object InActive : DeleteLayoutStatus()
        object Unkown : DeleteLayoutStatus()
    }

    class ImagesInfo {
        var existingImages: ArrayList<TransactionImage> = ArrayList()
        var deletedImages: ArrayList<TransactionImage> = ArrayList()
        var newAddedImages: ArrayList<CapturedImage> = ArrayList()
        var tempImages: ArrayList<TransactionImage> = ArrayList()
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

        data class ImagesChanged(val pair: Triple<ArrayList<TransactionImage>, ArrayList<TransactionImage>, String>) :
            Intent()

        data class NewImagesAdded(val list: Triple<ArrayList<CapturedImage>, List<TransactionImage>, String>) : Intent()

        data class OnImagesChanged(val imagesInfo: ImagesInfo, val isDirtyTransaction: Boolean) : Intent()

        data class OnKnowMoreClicked(val id: String) : Intent()

        data class WhatsApp(val contactPermissionAvailable: Boolean) : Intent()

        data class IsTxnViewExpanded(val isTxnViewExpanded: Boolean) : Intent()

        data class RxPreferenceBoolean(val key: String, val value: Boolean, val scope: Scope) : Intent()

        data class SubscriptionDetail(val subscriptionId: String) : Intent()

        object ShowDeleteTxnEducation : Intent()

        object EditPayment : Intent()

        object SyncMerchantPref : Intent()

        object SetNewPin : Intent()

        object UpdatePin : Intent()

        object Resume : Intent()

        object CheckIsFourdigitPinSet : Intent()
        object SubscriptionClicked : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class SubscriptionDetail(val subscription: Subscription) : ViewEvent()
    }

    interface Navigator {
        fun gotoLogin()
        fun goToSmsApp(mobile: String, smsContent: String)
        fun goToDeletePage(transactionId: String)
        fun goToWhatsappShare(customer: Customer, business: Business, transaction: Transaction)
        fun goBack()
        fun goToKnowMoreScreen(it: String, accountType: String)
        fun goToWhatsAppOptIn()
        fun openWhatsApp(okCreditNumber: String)
        fun showDeleteTxnConfirmationDialog()
        fun goToEnterPinScreen()
        fun goToSetPinScreen()
        fun handleFourDigitPin(isFourDigitPinSet: Boolean)
        fun syncDone()
        fun showUpdatePinScreen()
    }
}
