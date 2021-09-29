package `in`.okcredit.merchant.customer_ui.ui.add_discount

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.CommonUtils
import androidx.annotation.StringRes
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import tech.okcredit.camera_contract.CapturedImage

interface AddDiscountContract {

    companion object {
        const val INPUT_MODE_AMOUNT = 0
        const val INPUT_MODE_NOTE = 1
        const val INPUT_MODE_MEDIA = 2
        const val INPUT_MODE_PERMISSION = 3
        const val INPUT_MODE_PASSWORD = 4
        const val INPUT_MODE_DEFAULT = 5
        const val ORIGIN_DELETE_SCREEN = 1
        const val ORIGIN_CUSTOMER_SCREEN = 2
        const val ARG_TX_AMOUNT = "transaction_amount"
    }

    data class State(
        val activeInputMode: Int = INPUT_MODE_AMOUNT,
        val customer: Customer? = null,
        val txType: Int = Transaction.CREDIT,
        val isDateDialogueVisible: Boolean = false,
        val enablePasswordDialogVisible: Boolean = false,
        val amountCalculation: String? = null,
        val isPassWordEnable: Boolean = false,
        val isPasswordSet: Boolean = false,
        val amount: Long = 0L,
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
        val showNoteTutorial: Boolean = false,
        val imageList: ArrayList<CapturedImage> = ArrayList(),
        val isUserSpeaking: Boolean = false,
        val inputType: String = "",
        val voiceId: String = "",
        val canShowDiscountAddLoading: Boolean = false,
        val isSubmitLoading: Boolean = false,
        val isSubmitSuccess: Boolean = false,
        val isSubmitFailureMessage: String? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        data class ShowData(val customer: Customer, val txType: Int) : PartialState()

        data class SetAmountDetails(val amount: Long, val amountCalculation: String) : PartialState()

        data class ChangeInputMode(val value: Int) : PartialState()

        data class SetIsPasswordStatus(val status: Boolean) : PartialState()

        data class ChangeDate(val value: DateTime) : PartialState()

        data class ChangeImage(val listPhotos: ArrayList<CapturedImage>) : PartialState()

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

        data class SetNoteTutorialVisibility(val isVisible: Boolean) : PartialState()

        data class OnUserSpeaking(val isUserSpeaking: Boolean) : PartialState()

        data class SetVoiceInputData(
            val voiceAmount: Long,
            val voiceAmountCalculation: String,
            val voiceId: String,
            val inputType: String,
            val inputMode: Int
        ) : PartialState()

        data class ShowDiscountLoading(val discountLoading: Boolean) : PartialState()

        object ShowSubmitLoader : PartialState()

        data class HideSubmitLoader(val result: Boolean, @StringRes val errorMessage: Int? = null) : PartialState()
    }

    sealed class Intent : UserIntent {

        // dummy intent
        object NoChangeIntent : Intent()

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
        data class OnChangeImage(val listPhotos: ArrayList<CapturedImage>) : Intent()

        object OnDeleteImage : Intent()

        // submitting password
        data class SubmitPassword(
            val amount: Long,
            val password: String?,
            val txType: Int,
            val phtoList: List<CapturedImage>?,
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

        object GoToCustomerProfile : Intent()

        object TryDiscountAddAgain : Intent()

        data class SetSpeechAnimationVisibility(val status: Boolean) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class GoToForgotPasswordScreen(val mobile: String) : ViewEvent()

        object GoToTxSuccessScreen : ViewEvent()

        data class GoToCustomerProfile(val customerId: String?) : ViewEvent()

        object ShowPasswordError : ViewEvent()

        object GoToInternetPopup : ViewEvent()

        object DismissBottomLoader : ViewEvent()
    }
}
