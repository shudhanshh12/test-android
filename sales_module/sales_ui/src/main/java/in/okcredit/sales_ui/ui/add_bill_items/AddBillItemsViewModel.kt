package `in`.okcredit.sales_ui.ui.add_bill_items

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.usecase.AddSale
import `in`.okcredit.sales_ui.usecase.BillItemOperations
import `in`.okcredit.sales_ui.usecase.GetBillItems
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class AddBillItemsViewModel @Inject constructor(
    val initialState: AddBillItemsContract.State,
    @ViewModelParam("bill_items") val billItems: BillModel.BillItems?,
    private val getBillItems: GetBillItems,
    private val context: Context,
    private val billItemsOperations: BillItemOperations,
    private val addSale: Lazy<AddSale>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : BaseViewModel<AddBillItemsContract.State, AddBillItemsContract.PartialState, AddBillItemsContract.ViewEvent>(
    initialState
) {

    private var merchantId = ""
    private val inventoryItems = mutableListOf<BillModel.BillItem>()
    private var addedItems = mutableListOf<BillModel.BillItem>()
    private var newlyAddedItems = mutableListOf<BillModel.BillItem>()
    private val refreshItems: PublishSubject<Unit> = PublishSubject.create()

    override fun handle(): Observable<out UiState.Partial<AddBillItemsContract.State>> {
        return Observable.mergeArray(
            intent<AddBillItemsContract.Intent.Load>()
                .switchMap {
                    getBillItems.execute(GetBillItems.Request())
                }
                .map {
                    when (it) {
                        is Result.Progress -> AddBillItemsContract.PartialState.NoChange
                        is Result.Success -> {
                            inventoryItems.clear()
                            inventoryItems.addAll(it.value.items)
                            emitViewEvent(AddBillItemsContract.ViewEvent.ScrollToTop)
                            AddBillItemsContract.PartialState.SetInventoryItems(it.value.items)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> emitViewEvent(AddBillItemsContract.ViewEvent.GoToLoginScreen)
                                isInternetIssue(it.error) -> emitViewEvent(
                                    AddBillItemsContract.ViewEvent.ShowError(
                                        context.getString(R.string.no_internet_msg)
                                    )
                                )
                                else -> emitViewEvent(AddBillItemsContract.ViewEvent.ShowError(context.getString(R.string.err_default)))
                            }
                            AddBillItemsContract.PartialState.NoChange
                        }
                    }
                },
            intent<AddBillItemsContract.Intent.Load>()
                .switchMap {
                    UseCase.wrapSingle(getActiveBusinessId.get().execute())
                }
                .map {
                    when (it) {
                        is Result.Progress -> AddBillItemsContract.PartialState.NoChange
                        is Result.Success -> {
                            merchantId = it.value
                            AddBillItemsContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> emitViewEvent(AddBillItemsContract.ViewEvent.GoToLoginScreen)
                                isInternetIssue(it.error) -> emitViewEvent(
                                    AddBillItemsContract.ViewEvent.ShowError(
                                        context.getString(R.string.no_internet_msg)
                                    )
                                )
                                else -> emitViewEvent(AddBillItemsContract.ViewEvent.ShowError(context.getString(R.string.err_default)))
                            }
                            AddBillItemsContract.PartialState.NoChange
                        }
                    }
                },
            refreshItems.switchMap {
                getBillItems.execute(GetBillItems.Request())
            }
                .map {
                    when (it) {
                        is Result.Progress -> AddBillItemsContract.PartialState.NoChange
                        is Result.Success -> {
                            inventoryItems.clear()
                            inventoryItems.addAll(it.value.items)
                            AddBillItemsContract.PartialState.SetInventoryItems(it.value.items)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> emitViewEvent(AddBillItemsContract.ViewEvent.GoToLoginScreen)
                                isInternetIssue(it.error) -> emitViewEvent(
                                    AddBillItemsContract.ViewEvent.ShowError(
                                        context.getString(R.string.no_internet_msg)
                                    )
                                )
                                else -> emitViewEvent(AddBillItemsContract.ViewEvent.ShowError(context.getString(R.string.err_default)))
                            }
                            AddBillItemsContract.PartialState.NoChange
                        }
                    }
                },
            intent<AddBillItemsContract.Intent.SearchBillItemIntent>()
                .map {
                    emitViewEvent(AddBillItemsContract.ViewEvent.ScrollToTop)
                    AddBillItemsContract.PartialState.SetSearchQuery(it.searchQuery)
                },
            intent<AddBillItemsContract.Intent.ShowAddBillDialogIntent>()
                .map {
                    emitViewEvent(AddBillItemsContract.ViewEvent.ShowAddBillDialog)
                    AddBillItemsContract.PartialState.NoChange
                },
            intent<AddBillItemsContract.Intent.ShowUpdateBillDialogIntent>()
                .map {
                    emitViewEvent(AddBillItemsContract.ViewEvent.ShowUpdateBillDialog(it.billItem))
                    AddBillItemsContract.PartialState.NoChange
                },
            intent<AddBillItemsContract.Intent.RemoveBillItemIntent>()
                .switchMap {
                    billItemsOperations.execute(
                        BillItemOperations.Request(
                            AddBillItemsContract.Intent.RemoveBillItemIntent(
                                it.billItem
                            ),
                            inventoryItems, addedItems, newlyAddedItems
                        )
                    )
                }
                .map {
                    setData(it)
                },
            intent<AddBillItemsContract.Intent.AddBillItemIntent>()
                .switchMap {
                    billItemsOperations.execute(
                        BillItemOperations.Request(
                            AddBillItemsContract.Intent.AddBillItemIntent(
                                it.billItem
                            ),
                            inventoryItems, addedItems, newlyAddedItems
                        )
                    )
                }
                .map {
                    setData(it)
                },
            intent<AddBillItemsContract.Intent.NewBillItemIntent>()
                .switchMap {
                    billItemsOperations.execute(
                        BillItemOperations.Request(
                            AddBillItemsContract.Intent.NewBillItemIntent(
                                it.billItem
                            ),
                            inventoryItems, addedItems, newlyAddedItems
                        )
                    )
                }
                .map {
                    emitViewEvent(AddBillItemsContract.ViewEvent.ClearSearch)
                    emitViewEvent(AddBillItemsContract.ViewEvent.ScrollToTop)
                    setData(it)
                },
            intent<AddBillItemsContract.Intent.UpdateBillItemIntent>()
                .switchMap {
                    billItemsOperations.execute(
                        BillItemOperations.Request(
                            AddBillItemsContract.Intent.UpdateBillItemIntent(
                                it.billItem
                            ),
                            inventoryItems, addedItems, newlyAddedItems
                        )
                    )
                }
                .map {
                    setData(it)
                },
            intent<AddBillItemsContract.Intent.AddSale>()
                .switchMap {
                    addSale.get().execute(
                        AddSale.Request(
                            merchantId = merchantId,
                            amount = it.amount,
                            billedItems = it.billedItems
                        )
                    )
                }.map {
                    when (it) {
                        is Result.Progress -> AddBillItemsContract.PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(AddBillItemsContract.ViewEvent.onAddSaleSuccessfull(it.value.sale.id))
                            AddBillItemsContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> emitViewEvent(AddBillItemsContract.ViewEvent.GoToLoginScreen)
                                isInternetIssue(it.error) -> emitViewEvent(
                                    AddBillItemsContract.ViewEvent.ShowError(
                                        context.getString(R.string.no_internet_msg)
                                    )
                                )
                                else -> emitViewEvent(AddBillItemsContract.ViewEvent.ShowError(context.getString(R.string.err_default)))
                            }
                            AddBillItemsContract.PartialState.NoChange
                        }
                    }
                },
            intent<AddBillItemsContract.Intent.SetBillTotal>()
                .map {
                    AddBillItemsContract.PartialState.SetBillTotal(it.total)
                }
        )
    }

    private fun setData(it: Result<BillItemOperations.Response>): AddBillItemsContract.PartialState {
        return when (it) {
            is Result.Success -> {
                addedItems.clear()
                addedItems.addAll(it.value.addedItems)
                newlyAddedItems.clear()
                newlyAddedItems.addAll(it.value.newItems)
                if (it.value.refresh) {
                    refreshItems.onNext(Unit)
                }
                AddBillItemsContract.PartialState.SetDataWithNewItems(
                    it.value.billedItems,
                    it.value.addedItems,
                    it.value.totalQuantity,
                    it.value.newItems
                )
            }
            else -> AddBillItemsContract.PartialState.NoChange
        }
    }

    override fun reduce(
        currentState: AddBillItemsContract.State,
        partialState: AddBillItemsContract.PartialState
    ): AddBillItemsContract.State {
        return when (partialState) {
            AddBillItemsContract.PartialState.NoChange -> currentState
            is AddBillItemsContract.PartialState.SetInventoryItems -> currentState.copy(inventoryItems = partialState.inventoryItems)
            is AddBillItemsContract.PartialState.SetSearchQuery -> currentState.copy(searchQuery = partialState.searchQuery)
            is AddBillItemsContract.PartialState.SetDataWithNewItems -> currentState.copy(
                billedItems = partialState.billedItems,
                addedItems = partialState.addedItems,
                totalQuantity = partialState.totalQuantity,
                newItems = partialState.newlyAddedItems
            )
            is AddBillItemsContract.PartialState.SetBillTotal -> currentState.copy(total = partialState.total)
        }
    }
}
