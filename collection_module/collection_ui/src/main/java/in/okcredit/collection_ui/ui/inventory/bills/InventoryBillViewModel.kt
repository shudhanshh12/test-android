package `in`.okcredit.collection_ui.ui.inventory.bills

import `in`.okcredit.collection.contract.InventoryEpoxyModel
import `in`.okcredit.collection.contract.InventoryItem
import `in`.okcredit.collection.contract.InventorySource
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker
import `in`.okcredit.collection_ui.ui.inventory.helper.InventoryHelper
import `in`.okcredit.collection_ui.ui.inventory.view.InventoryTabListItem
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.network.NetworkError
import javax.inject.Inject

class InventoryBillViewModel @Inject constructor(
    initialState: InventoryBillContract.State,
    private val inventoryHelper: Lazy<InventoryHelper>,
    private val inventoryEventTracker: Lazy<InventoryEventTracker>,
) : BaseViewModel<InventoryBillContract.State, InventoryBillContract.PartialState, InventoryBillContract.ViewEvents>(
    initialState
) {
    override fun handle(): Observable<out UiState.Partial<InventoryBillContract.State>> {
        return Observable.mergeArray(
            loadObservable(),
            openBillWebView(),
        )
    }

    private fun loadObservable(): Observable<InventoryBillContract.PartialState>? {
        return intent<InventoryBillContract.Intent.Load>()
            .switchMap {
                wrap {
                    inventoryHelper.get().getBillsList()
                }
            }
            .map {
                when (it) {
                    is Result.Failure -> {
                        if (it.error is NetworkError) {
                            emitViewEvent(InventoryBillContract.ViewEvents.ShowError(R.string.err_network))
                        } else {
                            emitViewEvent(InventoryBillContract.ViewEvents.ShowError(R.string.err_default))
                        }
                        InventoryBillContract.PartialState.NoChange
                    }
                    is Result.Progress -> InventoryBillContract.PartialState.NoChange
                    is Result.Success -> InventoryBillContract.PartialState.SetItemList(it.value.itemInventories)
                }
            }
    }

    private fun openBillWebView(): Observable<InventoryBillContract.PartialState>? {
        return intent<InventoryBillContract.Intent.OpenBillWebView>()
            .map {
                inventoryEventTracker.get().trackBillCLicked(
                    billId = it.billId,
                )
                emitViewEvent(
                    InventoryBillContract.ViewEvents.OpenBillWebView(
                        billId = it.billId,
                        merchantId = it.merchantId,
                    )
                )
                InventoryBillContract.PartialState.NoChange
            }
    }

    override fun reduce(
        currentState: InventoryBillContract.State,
        partialState: InventoryBillContract.PartialState,
    ): InventoryBillContract.State {
        val tempState = when (partialState) {
            InventoryBillContract.PartialState.NoChange -> currentState
            is InventoryBillContract.PartialState.SetItemList -> currentState.copy(
                inventoryBillList = partialState.itemListInventory
            )
        }

        return tempState.copy(
            inventoryTabListItem = getInventoryTabListItem(tempState),
        )
    }

    private fun getInventoryTabListItem(state: InventoryBillContract.State): List<InventoryTabListItem> {
        val inventoryTabListItemList: MutableList<InventoryTabListItem> = mutableListOf()

        val finalBillItem = mutableListOf<InventoryItem>()
        var index = 0
        state.inventoryBillList.forEach {
            if (it.quantity > 0) {
                finalBillItem.add(
                    InventoryItem(
                        link = it.link,
                        quantity = it.quantity,
                        price = it.price,
                        createTime = it.createTime,
                        item = "Bill ${++index}",
                        billId = it.billId,
                        merchantId = it.businessId,
                    )
                )
            }
        }

        finalBillItem.sortByDescending {
            it.createTime.millis
        }

        finalBillItem.map { item ->
            inventoryTabListItemList.add(
                InventoryTabListItem.InventoryTabItemToShow(
                    inventoryEpoxyModel = InventoryEpoxyModel(
                        inventoryItem = item,
                        source = InventorySource.BILL,
                    )
                )
            )
        }
        return inventoryTabListItemList
    }
}
