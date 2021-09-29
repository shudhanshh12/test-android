package `in`.okcredit.sales_ui.ui.add_sales

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.usecase.AddSale
import `in`.okcredit.sales_ui.usecase.Calculator
import `in`.okcredit.sales_ui.usecase.GetBillItems
import `in`.okcredit.sales_ui.utils.Constants
import `in`.okcredit.sales_ui.utils.SalesUtil
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import tech.okcredit.android.ab.AbRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddSaleViewModel @Inject constructor(
    val initialState: AddSaleContract.State,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val addSale: AddSale,
    private val ab: AbRepository,
    private val context: Context,
    private val navigator: AddSaleContract.Navigator,
    private val calculator: Calculator,
    private val getBillItems: GetBillItems
) : BasePresenter<AddSaleContract.State, AddSaleContract.PartialState>(initialState) {

    data class Sale(
        val amount: Double,
        val notes: String,
        val saleDate: DateTime,
        val buyerName: String? = null,
        val buyerMobile: String? = null,
        val billedItems: BillModel.BilledItems? = null
    )

    private var amount: Long = 0
    private var amountCalculation: String = ""
    private var merchantId = ""
    private val showAlertPublishSubject: PublishSubject<String> = PublishSubject.create()
    private var billItems = mutableListOf<BillModel.BillItem>()

    override fun handle(): Observable<out UiState.Partial<AddSaleContract.State>> {
        return Observable.mergeArray(
            intent<AddSaleContract.Intent.Load>()
                .switchMap { UseCase.wrapSingle(getActiveBusinessId.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> AddSaleContract.PartialState.NoChange
                        is Result.Success -> {
                            // network connected
                            merchantId = it.value
                            AddSaleContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    AddSaleContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> AddSaleContract.PartialState.ShowAlert(context.getString(R.string.no_internet_msg))
                                else -> {
                                    showAlertPublishSubject.onNext(context.getString(R.string.err_default))
                                    AddSaleContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                    AddSaleContract.PartialState.NoChange
                },
            intent<AddSaleContract.Intent.Load>()
                .switchMap {
                    getBillItems.execute(GetBillItems.Request())
                }
                .map {
                    when (it) {
                        is Result.Progress -> AddSaleContract.PartialState.NoChange
                        is Result.Success -> {
                            AddSaleContract.PartialState.SetNewUser(it.value.items.isNullOrEmpty())
                        }
                        is Result.Failure -> AddSaleContract.PartialState.NoChange
                    }
                },
            intent<AddSaleContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(ab.isFeatureEnabled(Constants.SALES_SHOW_BILLING_NAME)) }
                .map {
                    when (it) {
                        is Result.Progress -> AddSaleContract.PartialState.NoChange
                        is Result.Success -> {
                            AddSaleContract.PartialState.ShowBillingName(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    AddSaleContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> AddSaleContract.PartialState.ShowAlert(context.getString(R.string.no_internet_msg))
                                else -> AddSaleContract.PartialState.NoChange
                            }
                        }
                    }
                },
            intent<AddSaleContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(ab.isFeatureEnabled(Constants.CASH_SALES_WITH_BILL)) }
                .map {
                    when (it) {
                        is Result.Progress -> AddSaleContract.PartialState.NoChange
                        is Result.Success -> {
                            AddSaleContract.PartialState.SetBillingAb(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    AddSaleContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> AddSaleContract.PartialState.ShowAlert(context.getString(R.string.no_internet_msg))
                                else -> AddSaleContract.PartialState.NoChange
                            }
                        }
                    }
                },
            intent<AddSaleContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(ab.isExperimentEnabled(Constants.ADD_BILL_TOTAL_AB)) }
                .map {
                    when (it) {
                        is Result.Progress -> AddSaleContract.PartialState.NoChange
                        is Result.Success -> {
                            AddSaleContract.PartialState.SetAddBillTotalAb(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    AddSaleContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> AddSaleContract.PartialState.ShowAlert(context.getString(R.string.no_internet_msg))
                                else -> AddSaleContract.PartialState.NoChange
                            }
                        }
                    }
                },
            intent<AddSaleContract.Intent.AddSale>()
                .switchMap {
                    var amount = it.sale.amount
                    if (it.sale.billedItems?.total.isNullOrEmpty().not()) {
                        amount = it.sale.billedItems!!.total.toDouble()
                    }
                    addSale.execute(
                        AddSale.Request(
                            merchantId,
                            amount,
                            it.sale.notes,
                            it.sale.saleDate,
                            it.sale.buyerName,
                            it.sale.buyerMobile,
                            it.sale.billedItems
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> AddSaleContract.PartialState.ShowLoading(true)
                        is Result.Success -> {
                            // network connected
                            navigator.onAddSaleSuccessful(it.value.sale.id)
                            AddSaleContract.PartialState.ShowLoading(false)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    AddSaleContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> AddSaleContract.PartialState.ShowAlert(context.getString(R.string.no_internet_msg))
                                else -> {
                                    showAlertPublishSubject.onNext(context.getString(R.string.err_default))
                                    AddSaleContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                },
            intent<AddSaleContract.Intent.OnDigitClicked>()
                .switchMap {
                    calculator.execute(
                        Calculator.Request(
                            Calculator.Mode.Digit,
                            it.digit.toString(),
                            amountCalculation,
                            amount
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Success -> {
                            amountCalculation = it.value.amountCalculation
                            amount = it.value.amount
                            AddSaleContract.PartialState.SetAmountDetails(amount, amountCalculation)
                        }
                        else -> AddSaleContract.PartialState.NoChange
                    }
                },
            intent<AddSaleContract.Intent.OnOperatorClicked>()
                .switchMap {
                    calculator.execute(
                        Calculator.Request(
                            Calculator.Mode.Operator,
                            it.amountCalculation,
                            amountCalculation,
                            amount
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Success -> {
                            amountCalculation = it.value.amountCalculation
                            amount = it.value.amount
                            AddSaleContract.PartialState.SetAmountDetails(amount, amountCalculation)
                        }
                        else -> AddSaleContract.PartialState.NoChange
                    }
                },

            // set amount and amount calculation path when pressing dot
            intent<AddSaleContract.Intent.OnDotClicked>()
                .switchMap {
                    calculator.execute(
                        Calculator.Request(
                            Calculator.Mode.Dot,
                            "",
                            amountCalculation,
                            amount
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Success -> {
                            amountCalculation = it.value.amountCalculation
                            amount = it.value.amount
                            AddSaleContract.PartialState.SetAmountDetails(amount, amountCalculation)
                        }
                        else -> AddSaleContract.PartialState.NoChange
                    }
                },

            // set amount calculation when pressing equal
            intent<AddSaleContract.Intent.OnEqualClicked>()
                .switchMap {
                    calculator.execute(
                        Calculator.Request(
                            Calculator.Mode.Equals,
                            "",
                            amountCalculation,
                            amount
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Success -> {
                            amountCalculation = it.value.amountCalculation
                            amount = it.value.amount
                            AddSaleContract.PartialState.SetAmountDetails(amount, amountCalculation)
                        }
                        else -> AddSaleContract.PartialState.NoChange
                    }
                },
            intent<AddSaleContract.Intent.OnBackSpaceClicked>()
                .switchMap {
                    calculator.execute(
                        Calculator.Request(
                            Calculator.Mode.Backspace,
                            "",
                            amountCalculation,
                            amount
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Success -> {
                            amountCalculation = it.value.amountCalculation
                            amount = it.value.amount
                            AddSaleContract.PartialState.SetAmountDetails(amount, amountCalculation)
                        }
                        else -> AddSaleContract.PartialState.NoChange
                    }
                },
            intent<AddSaleContract.Intent.OnLongPressBackSpace>()
                .map {
                    amount = 0L
                    amountCalculation = ""
                    AddSaleContract.PartialState.SetAmountDetails(amount, amountCalculation)
                },
            intent<AddSaleContract.Intent.OnChangeInputMode>()
                .map {
                    AddSaleContract.PartialState.SetInputMode(it.inputMode)
                },
            intent<AddSaleContract.Intent.ShowDatePickerDialog>()
                .map {
                    AddSaleContract.PartialState.NoChange
                },
            intent<AddSaleContract.Intent.OnChangeDate>()
                .map {
                    AddSaleContract.PartialState.SetDate(it.date)
                },
            intent<AddSaleContract.Intent.SetBillingDataIntent>()
                .map {
                    AddSaleContract.PartialState.SetBillingData(it.name, it.mobile)
                },
            showAlertPublishSubject
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<AddSaleContract.PartialState> { AddSaleContract.PartialState.HideAlert }
                        .startWith(AddSaleContract.PartialState.ShowAlert(it))
                },
            intent<AddSaleContract.Intent.ShowBillingNameDialogIntent>()
                .map {
                    navigator.showBillingNameDialog()
                    AddSaleContract.PartialState.NoChange
                },
            intent<AddSaleContract.Intent.AddBillItemIntent>()
                .delay(300, TimeUnit.MILLISECONDS)
                .map {
                    billItems.add(it.billItem)
                    val items = addBillItems()
                    AddSaleContract.PartialState.SetBilledItem(
                        BillModel.BilledItems(amountCalculation, items),
                        billItems
                    )
                },
            intent<AddSaleContract.Intent.SetBilledItemsIntent>()
                .map {
                    billItems.clear()
                    billItems.addAll(it.billItems)
                    val items = addBillItems()
                    AddSaleContract.PartialState.SetBilledItem(
                        BillModel.BilledItems(amountCalculation, items),
                        billItems
                    )
                },
            intent<AddSaleContract.Intent.ShowHandEducationIntent>()
                .map {
                    AddSaleContract.PartialState.ShowHandEducation(it.canShow)
                },
            intent<AddSaleContract.Intent.ShowDotHighlightIntent>()
                .map {
                    AddSaleContract.PartialState.ShowDotHighlight(it.canShow)
                }
        )
    }

    private fun addBillItems(): List<BillModel.BilledItem> {
        val total = billItems.sumByDouble { it.rate * it.quantity }
        amount = (total * 100).toLong()
        amountCalculation = SalesUtil.displayDecimalNumber(total)
        val items: MutableList<BillModel.BilledItem> = mutableListOf()
        billItems.forEach {
            items.add(BillModel.BilledItem(it.id, it.quantity))
        }
        return items
    }

    override fun reduce(
        currentState: AddSaleContract.State,
        partialState: AddSaleContract.PartialState
    ): AddSaleContract.State {
        return when (partialState) {
            AddSaleContract.PartialState.NoChange -> currentState
            is AddSaleContract.PartialState.ShowLoading -> currentState.copy(isLoading = partialState.isLoading)
            is AddSaleContract.PartialState.SetAmountDetails -> currentState.copy(
                amountCalculation = partialState.amountCalculation,
                amount = partialState.amount,
                canShowHandEducation = false
            )
            is AddSaleContract.PartialState.SetInputMode -> currentState.copy(inputMode = partialState.inputMode)
            is AddSaleContract.PartialState.SetNetworkError -> currentState
            is AddSaleContract.PartialState.SetDate -> currentState.copy(saleDate = partialState.date)
            is AddSaleContract.PartialState.ShowBillingName -> currentState.copy(canShowBillingName = partialState.canShow)
            is AddSaleContract.PartialState.ShowAlert -> currentState.copy(
                alert = partialState.msg,
                canShowAlert = true
            )
            AddSaleContract.PartialState.HideAlert -> currentState.copy(canShowAlert = false)
            is AddSaleContract.PartialState.SetBillingData -> currentState.copy(
                billingName = partialState.name,
                billingMobile = partialState.mobile
            )
            is AddSaleContract.PartialState.SetBillItems -> currentState.copy(billItems = partialState.billItems)
            is AddSaleContract.PartialState.SetBilledItem -> currentState.copy(
                billedItems = partialState.billedItems,
                billItems = partialState.billItems,
                amountCalculation = partialState.billedItems.total
            )
            is AddSaleContract.PartialState.SetNewUser -> currentState.copy(isNewUser = partialState.isNewUser)
            is AddSaleContract.PartialState.ShowHandEducation -> currentState.copy(canShowHandEducation = partialState.canShow)
            is AddSaleContract.PartialState.SetAddBillTotalAb -> currentState.copy(addBillTotalAb = partialState.isEnabled)
            is AddSaleContract.PartialState.ShowDotHighlight -> currentState.copy(canShowDotHighlight = partialState.canShow)
            is AddSaleContract.PartialState.SetBillingAb -> currentState.copy(isBillingAbEnabled = partialState.isBillingAbEnabled)
        }
    }
}
