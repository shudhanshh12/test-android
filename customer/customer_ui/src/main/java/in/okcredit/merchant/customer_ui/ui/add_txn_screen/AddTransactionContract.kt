package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity.Source
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.controller.models.AddBillModel
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.models.RoboflowState
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import android.net.Uri
import merchant.okcredit.accounting.model.Transaction
import tech.okcredit.camera_contract.CapturedImage
import java.util.*

interface AddTransactionContract {

    data class State(
        val customer: Customer? = null,
        val txType: Int = Transaction.CREDIT,
        val tx: Transaction? = null,
        val isLoading: Boolean = true,
        val error: Boolean = false,
        val note: String = "",
        val billDate: String = "",
        val amount: Long? = null,
        val amountCalculation: String? = null,
        val addBillModels: List<AddBillModel> = emptyList(),
        val source: Source = Source.CUSTOMER_SCREEN,
        val merchantPrefSync: Boolean = false,
        val isFourDigitPin: Boolean = false,

        // roboflow
        val isRoboflowEnabled: Boolean = false,
        val roboflowState: RoboflowState? = null,
        val isInternetAvailable: Boolean = true,
        val predictedAmount: Long? = null,
        val canPredictedAmount: Boolean = true,

        // activation
        val canShowAddReceiptExperiment: Boolean = false,
        val isImageAdded: Boolean = false,

        // Core Experience
        val canCollectVoiceSamplesFromNotes: Boolean = false,
        val voiceSamplesFromNotesInputUri: Uri? = null,
        val transcribedTextFromNotes: String? = null,
        val isSupplierCreditEnabledCustomer: Boolean = false,
    ) : UiState {
        fun getPaymentType(): String {
            var type = "Credit"
            if (txType == Transaction.PAYMENT) {
                type = "Payment"
            } else if (txType == Transaction.CREDIT) {
                type = "Credit"
            }
            return type
        }

        fun amountAdded() = amount != null && amount > 0
    }

    sealed class Intent : UserIntent {

        object Load : Intent()

        object CalculatorEducationDismissed : Intent()

        object AddBillImageClicked : Intent()

        object SelectBillDateClicked : Intent()

        object CustomerProfileClicked : Intent()

        object UpdatePin : Intent()

        object SyncMerchantPref : Intent()

        data class CheckFourPinPasswordSet(
            val merchantPrefSync: Boolean,
            val isFourDigitPin: Boolean = false,
        ) : Intent()

        object EditAmount : Intent()

        data class UploadAudioSample(
            val uri: Uri,
            val transcribedText: String,
            val noteText: String,
            val transactionId: String,
        ) : Intent()

        object OptOutFromVoiceSamplesCollection : Intent()

        object StartSpeechRecognition : Intent()

        data class SetAudioSample(val uri: Uri, val transcribedText: String) : Intent()

        object EnterAmountManuallyCancelUploadReceipt : Intent()

        object AddedAmountFirst : Intent()

        object ShowCreditPostTransactionEducation : Intent()

        data class BillDateSelected(val calendar: Calendar) : Intent()

        data class BillImagesAdded(val imageList: List<CapturedImage>, val isFinalList: Boolean = false) : Intent()

        data class AddTransaction(
            val amount: Long?,
            val shouldVerifyPassword: Boolean,
            val password: String? = null,
            val note: String? = null,
            val txnType: Int,
        ) : Intent()

        data class CalculatorData(val amountCalculation: String?, val amount: Long) :
            Intent()

        data class SubmitClicked(val note: String, val amount: Long?) : Intent()

        data class AddTransactionThroughShortCut(val note: String, val txnType: Int) : Intent()

        data class RoboflowUploadReceipt(val image: CapturedImage) : Intent()

        data class RoboflowSetAmountAmended(
            val customerId: String,
            val newAmount: Long,
            val transaction: Transaction,
        ) : Intent()

        object CheckMerchantPrefSync : Intent()

        object CheckCalculatorEducation : Intent()

        object CheckRoboflowAddBillTooltip : Intent()

        object CheckForAddReceiptVisibility : Intent()

        object CheckForPasswordEnable : Intent()

        object CheckForFourDigitPin : Intent()

        object CheckForNoteTutorial : Intent()

        object CheckForCollectVoiceSamplesFromNotes : Intent()

        object CheckForPasswordSet : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        object CheckForPassword : PartialState()

        data class AddTransactionSuccess(val transaction: Transaction) : PartialState()

        data class SetConnectionStatus(val status: Boolean) : PartialState()

        data class LoadInitialData(val customer: Customer) : PartialState()

        data class BillDateAdded(val calendar: Calendar) : PartialState()

        data class BillImagesAdded(val imageList: List<CapturedImage>) : PartialState()

        data class SetIsMerchantPrefSync(val synced: Boolean) : PartialState()

        data class SetIsFourDigitPin(val isFourDigitPin: Boolean) : PartialState()

        data class CalculatorData(val amount: Long, val amountCalculation: String?) : PartialState()

        data class SetRoboflowState(val roboflowState: RoboflowState) : PartialState()

        data class SetCanCollectVoiceSamplesFromNotes(val collect: Boolean) : PartialState()

        data class SetAudioSample(val uri: Uri, val transcribedText: String) : PartialState()

        data class SetSupplierCreditEnabledStatus(val isSupplierCreditEnabledCustomer: Boolean) : PartialState()
    }

    sealed class AddTxnViewEvent : BaseViewEvent {

        object GoToEnterPassword : AddTxnViewEvent()

        object SelectBillImage : AddTxnViewEvent()

        object ShowUpdatePinDialog : AddTxnViewEvent()

        object AddTransactionSuccess : AddTxnViewEvent()

        object ShowBottomSheetLoader : AddTxnViewEvent()

        object DismissBottomSheetLoader : AddTxnViewEvent()

        object ShowNoteTutorial : AddTxnViewEvent()

        object ShowNewSuccess : AddTxnViewEvent()

        object HideKeyboardWhenRoboflowEnabled : AddTxnViewEvent()

        object TrackAddNoteCompleted : AddTxnViewEvent()

        object GoToCustomerProfile : AddTxnViewEvent()

        data class StartSpeechRecognition(val intent: android.content.Intent) : AddTxnViewEvent()

        object ShowCalculatorEducation : AddTxnViewEvent()

        data class TrackRoboflowState(val currentState: RoboflowState) : AddTxnViewEvent()

        data class TrackAddedImagesCount(val count: Int) : AddTxnViewEvent()

        data class ShowError(val error: Int, val errorCode: Int? = null) : AddTxnViewEvent()

        data class ShowCalendar(val selectedDate: Calendar?) : AddTxnViewEvent()

        data class InvalidAmountError(val message: Int) : AddTxnViewEvent()
    }
}
