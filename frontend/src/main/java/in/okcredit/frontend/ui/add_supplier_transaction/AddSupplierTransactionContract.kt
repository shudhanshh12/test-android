package `in`.okcredit.frontend.ui.add_supplier_transaction

import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.CommonUtils
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import java.io.File

interface AddSupplierTransactionContract {

    companion object {
        const val INPUT_MODE_AMOUNT = 0
        const val INPUT_MODE_NOTE = 1
        const val INPUT_MODE_MEDIA = 2
        const val INPUT_MODE_PERMISSION = 3
        const val INPUT_MODE_PASSWORD = 4

        const val ORIGIN_DELETE_SCREEN = 1
        const val ORIGIN_CUSTOMER_SCREEN = 2
    }

    data class State(
        val activeInputMode: Int = INPUT_MODE_AMOUNT,
        val supplier: Supplier? = null,
        val txType: Int = Transaction.CREDIT,
        val isDateDialogueVisible: Boolean = false,
        val enablePasswordDialogVisible: Boolean = false,
        val amountCalculation: String? = null,
        val isPassWordEnable: Boolean = false,
        val isPasswordSet: Boolean = false,
        val amount: Long = 0L,
        val imageLocal: String? = null,
        val imageGlobal: String? = null,
        val note: String? = null,
        val isIncorrectPassword: Boolean = false,
        val password: String = "",
        val date: DateTime = CommonUtils.currentDateTime(),
        val isLoading: Boolean = true,
        val error: Boolean = false,
        val amountError: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val originScreen: Int = ORIGIN_DELETE_SCREEN,
        val canShowMidCamera: Boolean = false,
        val isSubmitLoading: Boolean = false,
        val isSubmitSuccess: Boolean = false,
        val isFourDigitPin: Boolean = false,
        val isMerchantPrefSynced: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        data class ShowData(val supplier: Supplier, val txType: Int) : PartialState()

        data class SetAmountDetails(val amount: Long, val amountCalculation: String) : PartialState()

        data class ChangeInputMode(val value: Int) : PartialState()

        data class SetIsPasswordStatus(val status: Boolean) : PartialState()

        data class ChangeDate(val value: DateTime) : PartialState()

        data class ChangeImage(val localUrl: String?) : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        object ShowAmountError : PartialState()

        object HideAmountError : PartialState()

        data class ChangeNote(val note: String) : PartialState()

        data class SetPasswordEnableStatus(val status: Boolean) : PartialState()

        object NoChange : PartialState()

        data class SetPasswordErrorStatus(val status: Boolean) : PartialState()

        data class SetPassword(val password: String) : PartialState()

        data class SetOriginInfo(val value: Int) : PartialState()

        data class CanShowMidCamera(val canShowMidCamera: Boolean) : PartialState()

        object ShowSubmitLoader : PartialState()

        data class HideSubmitLoader(val result: Boolean) : PartialState()

        data class SetIsFourDigitPin(val isFourDigitPin: Boolean) : PartialState()

        data class SetIsMerchantPrefSync(val isMerchantPrefSynced: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        // show alert
        object Load : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        // digit clicked from calculator
        data class OnDigitClicked(val digit: Int) : Intent()

        // operators clicked from calculator
        data class OnOperatorClicked(val amountCalculation: String) : Intent()

        // equal clicked from calculator
        object OnEqualClicked : Intent()

        // dot clicked from calculator
        object OnDotClicked : Intent()

        // long backpress from calculator
        object OnLongPressBackSpace : Intent()

        // backspace clicked from calculator
        object OnBackSpaceClicked : Intent()

        // change input type
        data class OnChangeInputMode(val d: Int) : Intent()

        // change bill_date
        data class OnChangeDate(val date: DateTime) : Intent()

        // change photo
        data class OnChangeImage(val image: File) : Intent()

        object OnDeleteImage : Intent()

        // submitting password
        data class SubmitPassword(
            val amount: Long,
            val password: String?,
            val payment: Boolean,
            val image: String?,
            val billDate: DateTime,
            val note: String?,
            val isPasswordVerifyRequired: Boolean
        ) : Intent()

        // adding transaction
        data class AddTransaction(val note: String) : Intent()

        // forgot password clicked
        object OnForgotPasswordClicked : Intent()

        // set password
        data class OnChangePassword(val password: String) : Intent()

        // go to supplier profile
        object GoToCustomerProfile : Intent()

        object SyncMerchantPref : Intent()

        object CheckIsFourDigitPinSet : Intent()
    }

    interface Navigator {

        fun gotoForgotPasswordScreen(mobile: String)

        fun gotoTxSuccessScreen()

        fun gotoCustomerProfile(customerId: String?)

        fun showNetworkError()

        fun goToHomeClearStack()

        fun handleFourDigitPin(isFourDigitPinSet: Boolean)

        fun onMerchantPrefSynced()

        fun showUpdatePinScreen()

        fun goToAuthScreen()

        fun showFailedMsg()

        fun setNewTransactionIdAsNavResult(transactionCreateTime: Long)
    }
}
