package `in`.okcredit.user_migration.presentation.ui.edit_details_bottomsheet

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState.Partial
import `in`.okcredit.user_migration.presentation.ui.edit_details_bottomsheet.EditDetailContract.*
import `in`.okcredit.user_migration.presentation.ui.edit_details_bottomsheet.usecase.CheckValidation
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.utils.DateTimeUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EditDetailViewModel @Inject constructor(
    initialState: State,
    private val editDetailsBottomSheetArgs: Lazy<EditDetailsBottomSheetArgs>
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    override fun handle(): Observable<Partial<State>> {
        return Observable.mergeArray(
            loadInputFields(),
            updateDate(),
            observeBillDateIntent(),
            checkValidation(),
            observeUpdateEntriesIntent()
        )
    }

    private fun observeUpdateEntriesIntent() = intent<Intent.SetUpdatedEntries>()
        .throttleFirst(300, TimeUnit.MILLISECONDS)
        .map {
            emitViewEvent(
                ViewEvent.SetUpdatedEntries(
                    customerName = it.customerName,
                    amount = it.amount,
                    mobile = it.mobile
                )
            )
            PartialState.NoChange
        }

    private fun checkValidation() = intent<Intent.CheckValidation>()
        .map {
            val isAnyError = CheckValidation.execute(it.customerName, it.amount, it.mobile)
            if (isAnyError.isEmpty()) {
                pushIntent(
                    Intent.SetUpdatedEntries(
                        customerName = it.customerName,
                        amount = covertToLong(it.amount),
                        mobile = it.mobile
                    )
                )
            } else {
                emitViewEvent(ViewEvent.ValidationErrors(isAnyError))
            }
            PartialState.UpdateEntries(
                customerName = it.customerName,
                phoneNumber = it.mobile,
                amount = it.amount
            )
        }

    private fun updateDate() = intent<Intent.BillDateSelected>()
        .map {
            PartialState.BillDateAdded(it.calendar)
        }

    private fun observeBillDateIntent() = intent<Intent.SelectBillDateAndUpdateEntries>()
        .map {
            PartialState.SelectBillDateAndUpdateEntries(
                amount = it.amount,
                mobile = it.mobile,
                name = it.name
            )
        }

    private fun loadInputFields() = intent<Intent.Load>()
        .map {
            val customerModel = editDetailsBottomSheetArgs.get().customer
            val validationError = CheckValidation.execute(
                customerModel.name ?: "",
                customerModel.amount.toString(),
                customerModel.phone ?: ""
            )
            if (validationError.isNotEmpty()) {
                emitViewEvent(ViewEvent.ValidationErrors(validationError))
            }
            PartialState.SetCustomerData(editDetailsBottomSheetArgs.get().customer)
        }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is PartialState.SetCustomerData -> currentState.copy(
                showLoading = false,
                customer = partialState.customer,
                phoneNumber = partialState.customer?.phone ?: "",
                customerName = partialState.customer?.name ?: "",
                visibleAmount = ((partialState.customer?.amount ?: 0).toFloat() / 100).toString(),
                billDate = SimpleDateFormat.getDateInstance(
                    DateFormat.MEDIUM
                ).format(partialState.customer?.dueDate?.millis ?: DateTimeUtils.currentDateTime().millis)
            )
            is PartialState.ShowLoading -> currentState.copy(showLoading = true)
            is PartialState.SelectBillDateAndUpdateEntries -> {
                emitViewEvent(ViewEvent.ShowCalendar(currentState.selectedDate))
                currentState.copy(
                    visibleAmount = partialState.amount,
                    customerName = partialState.name,
                    phoneNumber = partialState.mobile
                )
            }
            is PartialState.UpdateEntries -> currentState.copy(
                visibleAmount = partialState.amount,
                phoneNumber = partialState.phoneNumber,
                customerName = partialState.customerName
            )
            is PartialState.BillDateAdded -> currentState.copy(
                selectedDate = partialState.calendar,
                billDate = SimpleDateFormat.getDateInstance(
                    DateFormat.MEDIUM
                ).format(partialState.calendar.time)
            )
            is PartialState.NoChange -> currentState
        }
    }

    private fun covertToLong(amount: String) = try {
        (amount.toDouble() * 100).toLong()
    } catch (_: Exception) {
        0
    }
}
