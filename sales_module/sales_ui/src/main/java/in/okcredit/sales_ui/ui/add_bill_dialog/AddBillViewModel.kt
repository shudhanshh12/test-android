package `in`.okcredit.sales_ui.ui.add_bill_dialog

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.usecase.AddBillItem
import `in`.okcredit.sales_ui.usecase.UpdateBillItem
import `in`.okcredit.sales_ui.utils.SalesUtil
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class AddBillViewModel @Inject constructor(
    val initialState: AddBillContract.State,
    private val navigator: AddBillContract.Navigator,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val addBillItem: AddBillItem,
    private val updateBillItem: UpdateBillItem,
    private val context: Context
) : BasePresenter<AddBillContract.State, AddBillContract.PartialState>(initialState) {

    private var merchantId = ""
    override fun handle(): Observable<out UiState.Partial<AddBillContract.State>> {
        return Observable.mergeArray(
            intent<AddBillContract.Intent.Load>()
                .switchMap { UseCase.wrapSingle(getActiveBusinessId.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> AddBillContract.PartialState.NoChange
                        is Result.Success -> {
                            merchantId = it.value
                            AddBillContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> navigator.goToLogin()
                                isInternetIssue(it.error) -> navigator.showError(context.getString(R.string.no_internet_msg))
                                else -> navigator.showError(it.error.localizedMessage)
                            }
                            AddBillContract.PartialState.NoChange
                        }
                    }
                },
            intent<AddBillContract.Intent.AddBillItemIntent>()
                .switchMap { addBillItem.execute(AddBillItem.Request(merchantId, it.addBillItemRequest)) }
                .map {
                    when (it) {
                        is Result.Progress -> AddBillContract.PartialState.NoChange
                        is Result.Success -> {
                            navigator.onBillItemAdded(it.value.item)
                            AddBillContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> navigator.goToLogin()
                                isInternetIssue(it.error) -> navigator.showError(context.getString(R.string.no_internet_msg))
                                else -> navigator.onAddFailed(it.error.message ?: "")
                            }
                            AddBillContract.PartialState.NoChange
                        }
                    }
                },
            intent<AddBillContract.Intent.UpdateBillItemIntent>()
                .switchMap { updateBillItem.execute(UpdateBillItem.Request(it.billId, it.updateBillItemIntent)) }
                .map {
                    when (it) {
                        is Result.Progress -> AddBillContract.PartialState.NoChange
                        is Result.Success -> {
                            navigator.onBillItemUpdated(it.value.item)
                            AddBillContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> navigator.goToLogin()
                                isInternetIssue(it.error) -> navigator.showError(context.getString(R.string.no_internet_msg))
                                else -> navigator.onUpdateFailed(it.error.message ?: "")
                            }
                            AddBillContract.PartialState.NoChange
                        }
                    }
                },
            intent<AddBillContract.Intent.ShowErrorIntent>()
                .map {
                    navigator.showError(it.msg)
                    AddBillContract.PartialState.NoChange
                },
            intent<AddBillContract.Intent.SetNameIntent>()
                .map {
                    AddBillContract.PartialState.SetName(it.name)
                },
            intent<AddBillContract.Intent.SetRateIntent>()
                .map {
                    if (it.rate.isNotEmpty() && it.rate != ".") {
                        AddBillContract.PartialState.SetRate(it.rate.toDouble())
                    } else {
                        AddBillContract.PartialState.SetRate(0.0)
                    }
                },
            intent<AddBillContract.Intent.SetQuantityIntent>()
                .map {
                    if (it.quantity.isNotEmpty()) {
                        AddBillContract.PartialState.SetQuantity(it.quantity.toDouble())
                    } else {
                        AddBillContract.PartialState.SetQuantity(1.0)
                    }
                },
            intent<AddBillContract.Intent.ShowRateAndQuantityIntent>()
                .map {
                    AddBillContract.PartialState.ShowRateAndQuantity
                },
            intent<AddBillContract.Intent.SetSaveEnableIntent>()
                .map {
                    AddBillContract.PartialState.SetSaveEnable(it.isEnable)
                },
            intent<AddBillContract.Intent.UpdateQuantityIntent>()
                .map {
                    navigator.onBillItemUpdated(it.billItem)
                    AddBillContract.PartialState.NoChange
                },
            intent<AddBillContract.Intent.SetDataIntent>()
                .map {
                    AddBillContract.PartialState.SetData(
                        it.billItem?.name ?: "",
                        it.billItem?.rate ?: 0.0,
                        it.billItem?.quantity ?: 1.0
                    )
                },
            intent<AddBillContract.Intent.PlusIntent>()
                .map {
                    var text = it.qty
                    if (text.isEmpty() || text == ".") {
                        text = "0"
                    }
                    var qty = text.toDouble()
                    qty += 1
                    if (qty > 999.99) {
                        qty = qty.toInt().toDouble()
                    }
                    navigator.updateQuantity(SalesUtil.displayDecimalNumber(qty))
                    AddBillContract.PartialState.NoChange
                },
            intent<AddBillContract.Intent.MinusIntent>()
                .map {
                    var text = it.qty
                    if (text.isEmpty() || text == ".") {
                        text = "0"
                    }
                    var qty = text.toDouble()
                    qty -= 1
                    if (qty >= 0.0) {
                        navigator.updateQuantity(SalesUtil.displayDecimalNumber(qty))
                    }
                    AddBillContract.PartialState.NoChange
                }
        )
    }

    override fun reduce(
        currentState: AddBillContract.State,
        partialState: AddBillContract.PartialState
    ): AddBillContract.State {
        return when (partialState) {
            AddBillContract.PartialState.NoChange -> currentState
            is AddBillContract.PartialState.SetName -> currentState.copy(name = partialState.name)
            is AddBillContract.PartialState.SetRate -> currentState.copy(rate = partialState.rate)
            is AddBillContract.PartialState.SetQuantity -> currentState.copy(quantity = partialState.quantity)
            AddBillContract.PartialState.ShowRateAndQuantity -> currentState.copy(canShowRateAndQuantity = true)
            is AddBillContract.PartialState.SetSaveEnable -> currentState.copy(enableSave = partialState.isEnable)
            is AddBillContract.PartialState.SetData -> currentState.copy(
                name = partialState.name,
                rate = partialState.rate,
                quantity = partialState.quantity
            )
        }
    }
}
