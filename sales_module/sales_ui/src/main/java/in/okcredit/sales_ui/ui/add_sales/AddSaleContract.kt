package `in`.okcredit.sales_ui.ui.add_sales

import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import org.joda.time.DateTime

interface AddSaleContract {

    companion object {
        const val INPUT_MODE_AMOUNT = 0
        const val INPUT_MODE_NOTE = 1
    }

    data class State(
        val isLoading: Boolean = false,
        val isNewUser: Boolean = false,
        val canShowHandEducation: Boolean = false,
        val canShowDotHighlight: Boolean = false,
        val amountCalculation: String? = null,
        val amount: Long = 0L,
        val saleDate: DateTime = DateTime.now(),
        val inputMode: Int = INPUT_MODE_AMOUNT,
        val canShowBillingName: Boolean = false,
        val canShowAlert: Boolean = false,
        val alert: String = "",
        val billingName: String? = null,
        val billingMobile: String? = null,
        val billItems: List<BillModel.BillItem> = arrayListOf(),
        val billedItems: BillModel.BilledItems? = null,
        val addBillTotalAb: Boolean = false,
        val isBillingAbEnabled: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class ShowLoading(val isLoading: Boolean) : PartialState()

        data class SetAmountDetails(val amount: Long, val amountCalculation: String) : PartialState()

        data class SetInputMode(val inputMode: Int) : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class SetDate(val date: DateTime) : PartialState()

        data class ShowBillingName(val canShow: Boolean) : PartialState()

        data class ShowAlert(val msg: String) : PartialState()

        object HideAlert : PartialState()

        data class SetBillingData(val name: String, val mobile: String? = null) : PartialState()

        data class SetBillItems(val billItems: List<BillModel.BillItem>) : PartialState()

        data class SetBilledItem(val billedItems: BillModel.BilledItems, val billItems: List<BillModel.BillItem>) : PartialState()

        data class SetNewUser(val isNewUser: Boolean) : PartialState()

        data class ShowHandEducation(val canShow: Boolean) : PartialState()

        data class ShowDotHighlight(val canShow: Boolean) : PartialState()

        data class SetAddBillTotalAb(val isEnabled: Boolean) : PartialState()

        data class SetBillingAb(val isBillingAbEnabled: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class ShowLoading(val isLoading: Boolean) : Intent()

        data class OnDigitClicked(val digit: Int) : Intent()

        data class OnOperatorClicked(val amountCalculation: String) : Intent()

        object OnEqualClicked : Intent()

        object OnDotClicked : Intent()

        object OnLongPressBackSpace : Intent()

        object OnBackSpaceClicked : Intent()

        data class OnChangeInputMode(val inputMode: Int) : Intent()

        data class AddSale(val sale: AddSaleViewModel.Sale) : Intent()

        object ShowDatePickerDialog : Intent()

        data class OnChangeDate(val date: DateTime) : Intent()

        data class SetBillingDataIntent(val name: String, val mobile: String? = null) : Intent()

        object ShowBillingNameDialogIntent : Intent()

        data class AddBillItemIntent(val billItem: BillModel.BillItem) : Intent()

        data class SetBilledItemsIntent(val billItems: List<BillModel.BillItem>) : Intent()

        data class ShowHandEducationIntent(val canShow: Boolean) : Intent()

        data class ShowDotHighlightIntent(val canShow: Boolean) : Intent()
    }

    interface Navigator {
        fun gotoLogin()
        fun goBack()
        fun showDatePicker()
        fun showBillingNameDialog()
        fun goToAddBillItemScreen()
        fun onAddSaleSuccessful(saleId: String)
    }
}
