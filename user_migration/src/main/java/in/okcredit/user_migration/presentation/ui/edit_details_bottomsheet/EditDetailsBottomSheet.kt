package `in`.okcredit.user_migration.presentation.ui.edit_details_bottomsheet

import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.user_migration.R
import `in`.okcredit.user_migration.databinding.EditDetailsBottomsheetBinding
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.models.CustomerUiTemplate
import `in`.okcredit.user_migration.presentation.ui.edit_details_bottomsheet.EditDetailContract.*
import `in`.okcredit.user_migration.presentation.utils.UserMigrationUtils.ErrorType
import `in`.okcredit.user_migration.presentation.utils.UserMigrationUtils.ErrorType.*
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.core.widget.doBeforeTextChanged
import androidx.lifecycle.lifecycleScope
import com.instacart.library.truetime.TrueTime
import io.reactivex.Observable
import org.joda.time.DateTime
import tech.okcredit.android.base.edit_text.DecimalDigitsInputFilter
import tech.okcredit.android.base.extensions.setNavigationResult
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import java.util.*

class EditDetailsBottomSheet :
    BaseBottomSheetWithViewEvents<State, ViewEvent, Intent>(
        "EditDetailsBottomSheet"
    ),
    DatePickerDialog.OnDateSetListener {

    private val binding: EditDetailsBottomsheetBinding by viewLifecycleScoped(EditDetailsBottomsheetBinding::bind)

    companion object {
        const val TAG = "EditDetailsBottomSheet"
        const val MAX_DIGITS_COUNT = 7
        const val MAX_DECIMAL_COUNT = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return EditDetailsBottomsheetBinding.inflate(inflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListener()
    }

    private fun initClickListener() {
        binding.apply {
            etDate.setOnClickListener {
                pushIntent(
                    Intent.SelectBillDateAndUpdateEntries(
                        amount = etAmount.text.toString(),
                        mobile = etMobile.text.toString(),
                        name = etCustomerName.text.toString()
                    )
                )
            }

            etAmount.filters = arrayOf<InputFilter>(
                DecimalDigitsInputFilter(
                    MAX_DIGITS_COUNT,
                    MAX_DECIMAL_COUNT
                )
            )

            etAmount.doBeforeTextChanged { text, _, _, after ->
                if (text.isNullOrBlank() && after > 0) {
                    // todo tracker Edit event
                }
            }

            etAmount.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    amountInputLayout.apply {
                        error = null
                        isErrorEnabled = false
                    }
                }
            }

            etCustomerName.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    customerNameInputLayout.apply {
                        error = null
                        isErrorEnabled = false
                    }
                }
            }

            etMobile.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    phoneNumberInputLayout.apply {
                        error = null
                        isErrorEnabled = false
                    }
                }
            }
        }

        binding.save.setOnClickListener {
            pushIntent(
                Intent.CheckValidation(
                    customerName = binding.etCustomerName.text.toString(),
                    amount = binding.etAmount.text.toString(),
                    mobile = binding.etMobile.text.toString()
                )
            )
        }
    }

    private fun getUpdatedEntries(customerName: String, amount: Long, mobile: String): CustomerUiTemplate {
        val currentState = getCurrentState()
        return CustomerUiTemplate(
            index = currentState.customer?.index!!, // wanted the crash the app.
            customerId = currentState.customer.customerId,
            name = customerName,
            phone = mobile,
            type = currentState.customer.type,
            amount = amount,
            dueDate = DateTime(currentState.selectedDate.timeInMillis),
            error = false
        )
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun render(state: State) {
        binding.apply {

            etAmount.setText(getAmount(state))
            etMobile.setText(state.phoneNumber)
            etDate.setText(state.billDate)
            etCustomerName.setText(state.customerName)
        }
    }

    private fun getAmount(state: State) = if (state.visibleAmount.endsWith(".0")) {
        state.visibleAmount.replace(".0", "")
    } else state.visibleAmount

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val selectedCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            pushIntent(Intent.BillDateSelected(selectedCalendar))
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.ShowCalendar -> showCalendar(event.selectedDate)
            is ViewEvent.ValidationErrors -> showValidationError(event.anyError)
            is ViewEvent.SetUpdatedEntries -> {
                setNavigationResult(getUpdatedEntries(event.customerName, event.amount, event.mobile))
                dismiss()
            }
        }
    }

    private fun showValidationError(anyError: List<ErrorType>) = binding.apply {
        if (anyError.contains(PhoneError)) {
            phoneNumberInputLayout.apply {
                error = getString(R.string.phone_error)
                isErrorEnabled = true
            }
        }

        if (anyError.contains(NameError)) {
            customerNameInputLayout.apply {
                error = getString(R.string.error_please_add_customer_name)
                isErrorEnabled = true
            }
        }

        if (anyError.contains(AmountError)) {
            amountInputLayout.apply {
                error = getString(R.string.amount_greater_than_0)
                isErrorEnabled = true
            }
        }
    }

    private fun showCalendar(selectedDate: Calendar?) {
        val currentCalendar = Calendar.getInstance().apply { time = TrueTime.now() }
        DatePickerDialog(
            requireContext(),
            this,
            selectedDate?.get(Calendar.YEAR) ?: currentCalendar.get(Calendar.YEAR),
            selectedDate?.get(Calendar.MONTH) ?: currentCalendar.get(Calendar.MONTH),
            selectedDate?.get(Calendar.DAY_OF_MONTH)
                ?: currentCalendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = currentCalendar.timeInMillis
            setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.ok), this)
            setButton(DatePickerDialog.BUTTON_NEGATIVE, getString(R.string.cancel), this)
            show()
        }
    }
}
