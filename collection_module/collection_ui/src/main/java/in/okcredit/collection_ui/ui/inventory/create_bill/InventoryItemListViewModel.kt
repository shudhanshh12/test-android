package `in`.okcredit.collection_ui.ui.inventory.create_bill

import `in`.okcredit.collection.contract.InventoryItem
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker
import `in`.okcredit.collection_ui.ui.inventory.create_bill.view.InventoryListItem
import `in`.okcredit.collection_ui.ui.inventory.helper.InventoryHelper
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.base.network.NetworkError
import javax.inject.Inject

class InventoryItemListViewModel @Inject constructor(
    initialState: InventoryItemListContract.State,
    private val inventoryHelper: Lazy<InventoryHelper>,
    private val inventoryEventTracker: Lazy<InventoryEventTracker>,
) : BaseViewModel<InventoryItemListContract.State, InventoryItemListContract.PartialState, InventoryItemListContract.ViewEvents>(
    initialState
) {

    override fun handle(): Observable<out UiState.Partial<InventoryItemListContract.State>> {
        return Observable.mergeArray(
            loadObservable(),
            addItemObservable(),
            deleteItemObservable(),
            createBill(),
            openInventoryBottomSheetDialog(),
        )
    }

    private fun loadObservable(): Observable<InventoryItemListContract.PartialState>? {
        return intent<InventoryItemListContract.Intent.Load>()
            .switchMap {
                wrap(
                    rxSingle { inventoryHelper.get().getItemsList() }
                )
            }
            .map {
                when (it) {
                    is Result.Failure -> {
                        if (it.error is NetworkError) {
                            emitViewEvent(InventoryItemListContract.ViewEvents.ShowError(R.string.err_network))
                        } else {
                            emitViewEvent(InventoryItemListContract.ViewEvents.ShowError(R.string.err_default))
                        }
                        InventoryItemListContract.PartialState.NoChange
                    }
                    is Result.Progress -> InventoryItemListContract.PartialState.NoChange
                    is Result.Success -> {
                        val finalList = mutableListOf<InventoryItem>()
                        it.value.items.forEach {
                            finalList.add(
                                it.apply {
                                    quantity = 0
                                }
                            )
                        }
                        InventoryItemListContract.PartialState.SetDefaultItemList(finalList)
                    }
                }
            }
    }

    private fun addItemObservable(): Observable<InventoryItemListContract.PartialState>? {
        return intent<InventoryItemListContract.Intent.AddItem>()
            .map {
                inventoryEventTracker.get().trackBillingBillItemAdded(it.item.item)
                InventoryItemListContract.PartialState.AddNewItem(it.item)
            }
    }

    private fun deleteItemObservable(): Observable<InventoryItemListContract.PartialState>? {
        return intent<InventoryItemListContract.Intent.DeleteItem>()
            .map {
                inventoryEventTracker.get().trackBillingBillItemRemoved(it.item.item)
                InventoryItemListContract.PartialState.DeleteItem(it.item)
            }
    }

    private fun openInventoryBottomSheetDialog(): Observable<InventoryItemListContract.PartialState>? {
        return intent<InventoryItemListContract.Intent.OpenInventoryBottomSheetDialog>()
            .map {
                inventoryEventTracker.get().trackBillingNewItemClicked()
                emitViewEvent(InventoryItemListContract.ViewEvents.OpenInventoryBottomSheetDialog)
                InventoryItemListContract.PartialState.NoChange
            }
    }

    private fun createBill(): Observable<InventoryItemListContract.PartialState>? {
        return intent<InventoryItemListContract.Intent.CreateBill>()
            .switchMap {
                getCurrentState().let {
                    inventoryEventTracker.get().trackBillingSaveBillClicked(
                        billTotal = it.totalBill.toString(),
                        itemsCount = it.totalItem.toString(),
                        totalQuantity = it.totalQuantity.toString(),
                    )
                }
                wrap(
                    rxSingle { inventoryHelper.get().createBill(it.listBillItem) }
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> InventoryItemListContract.PartialState.NoChange
                    is Result.Success -> {
                        emitViewEvent(
                            InventoryItemListContract.ViewEvents.OpenBillWebView(
                                it.value.billUrl,
                                it.value.billId,
                                it.value.businessId,
                            )
                        )
                        InventoryItemListContract.PartialState.NoChange
                    }
                    is Result.Failure -> {
                        if (it.error is NetworkError) {
                            emitViewEvent(InventoryItemListContract.ViewEvents.ShowError(R.string.err_network))
                        } else {
                            emitViewEvent(InventoryItemListContract.ViewEvents.ShowError(R.string.err_default))
                        }
                        inventoryEventTracker.get().trackBillError(errorReason = it.error.message ?: "")
                        InventoryItemListContract.PartialState.NoChange
                    }
                }
            }
    }

    override fun reduce(
        currentState: InventoryItemListContract.State,
        partialState: InventoryItemListContract.PartialState,
    ): InventoryItemListContract.State {
        val tempState = when (partialState) {
            is InventoryItemListContract.PartialState.AddNewItem -> {

                val mutableList: MutableMap<String, InventoryItem> = currentState.listToShow.toMutableMap()
                if (mutableList.containsKey(partialState.item.item)) {
                    val billItem = mutableList[partialState.item.item]
                    mutableList.remove(partialState.item.item)
                    mutableList[partialState.item.item] =
                        billItem?.copy(quantity = billItem.quantity + partialState.item.quantity) ?: InventoryItem()
                } else {
                    mutableList[partialState.item.item] =
                        partialState.item
                }

                currentState.copy(listToShow = mutableList)
            }
            is InventoryItemListContract.PartialState.DeleteItem -> {
                val mutableList: MutableMap<String, InventoryItem> = currentState.listToShow.toMutableMap()
                if (mutableList.containsKey(partialState.item.item)) {
                    val billItem = mutableList[partialState.item.item]
                    mutableList.remove(partialState.item.item)
                    mutableList[partialState.item.item] =
                        billItem?.copy(quantity = if (billItem.quantity <= 0) 0 else billItem.quantity - 1)
                        ?: InventoryItem()
                }
                currentState.copy(listToShow = mutableList)
            }
            InventoryItemListContract.PartialState.NoChange -> currentState
            is InventoryItemListContract.PartialState.SetDefaultItemList -> {
                val mutableList: MutableMap<String, InventoryItem> = currentState.listToShow.toMutableMap()
                partialState.itemList.forEach {
                    mutableList.put(it.item, it)
                }
                currentState.copy(
                    defaultList = partialState.itemList,
                    listToShow = mutableList
                )
            }
        }
        return returnStateAfterCalculation(tempState)
    }

    private fun returnStateAfterCalculation(state: InventoryItemListContract.State): InventoryItemListContract.State {
        var totalBill = 0L
        var totalQuantity = 0
        var totalItem = 0
        state.listToShow.forEach {
            if (it.value.quantity > 0) {
                totalBill += it.value.price.times(it.value.quantity)
                totalQuantity += it.value.quantity
                totalItem += 1
            }
        }
        return state.copy(
            totalBill = totalBill,
            totalQuantity = totalQuantity,
            totalItem = totalItem,
            inventoryItemList = getInventoryItemList(state),
        )
    }

    private fun getInventoryItemList(state: InventoryItemListContract.State): List<InventoryListItem> {
        val inventoryListItemList: MutableList<InventoryListItem> = mutableListOf()
        state.listToShow.toSortedMap().map {
            inventoryListItemList.add(InventoryListItem.InventoryItemViewToShow(it.value))
        }
        return inventoryListItemList
    }
}
