package `in`.okcredit.collection_ui.ui.inventory.items

import `in`.okcredit.collection.contract.InventoryEpoxyModel
import `in`.okcredit.collection.contract.InventorySource
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.ui.inventory.helper.InventoryHelper
import `in`.okcredit.collection_ui.ui.inventory.view.InventoryTabListItem
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.network.NetworkError
import javax.inject.Inject

class InventoryItemViewModel @Inject constructor(
    initialState: InventoryItemContract.State,
    private val inventoryHelper: Lazy<InventoryHelper>,
) : BaseViewModel<InventoryItemContract.State, InventoryItemContract.PartialState, InventoryItemContract.ViewEvents>(
    initialState
) {
    override fun handle(): Observable<out UiState.Partial<InventoryItemContract.State>> {
        return Observable.mergeArray(
            loadObservable(),
            createItem(),
        )
    }

    private fun loadObservable(): Observable<InventoryItemContract.PartialState>? {
        return intent<InventoryItemContract.Intent.Load>()
            .switchMap {
                wrap { inventoryHelper.get().getItemsList() }
            }
            .map {
                when (it) {
                    is Result.Failure -> {
                        if (it.error is NetworkError) {
                            emitViewEvent(InventoryItemContract.ViewEvents.ShowError(R.string.err_network))
                        } else {
                            emitViewEvent(InventoryItemContract.ViewEvents.ShowError(R.string.err_default))
                        }
                        InventoryItemContract.PartialState.NoChange
                    }
                    is Result.Progress -> InventoryItemContract.PartialState.NoChange
                    is Result.Success -> InventoryItemContract.PartialState.SetItemList(it.value.items)
                }
            }
    }

    private fun createItem(): Observable<InventoryItemContract.PartialState>? {
        return intent<InventoryItemContract.Intent.CreateItem>()
            .switchMap {
                wrap { inventoryHelper.get().createItem(it.inventoryItem) }
            }
            .map {
                when (it) {
                    is Result.Success -> {
                        pushIntent(InventoryItemContract.Intent.Load)
                    }
                    is Result.Failure -> {
                        if (it.error is NetworkError) {
                            emitViewEvent(InventoryItemContract.ViewEvents.ShowError(R.string.err_network))
                        } else {
                            emitViewEvent(InventoryItemContract.ViewEvents.ShowError(R.string.err_default))
                        }
                    }
                    is Result.Progress -> {
                        // No change
                    }
                }
                InventoryItemContract.PartialState.NoChange
            }
    }

    override fun reduce(
        currentState: InventoryItemContract.State,
        partialState: InventoryItemContract.PartialState,
    ): InventoryItemContract.State {
        val tempState = when (partialState) {
            InventoryItemContract.PartialState.NoChange -> currentState
            is InventoryItemContract.PartialState.SetItemList -> currentState.copy(
                inventoryList = partialState.itemList
            )
        }

        return tempState.copy(
            inventoryTabListItem = getInventoryTabListItem(tempState),
        )
    }

    private fun getInventoryTabListItem(state: InventoryItemContract.State): List<InventoryTabListItem> {
        val inventoryTabListItemList: MutableList<InventoryTabListItem> = mutableListOf()
        val finalItems = state.inventoryList.toMutableList()

        finalItems.sortByDescending {
            it.createTime.millis
        }

        finalItems.map { item ->
            inventoryTabListItemList.add(
                InventoryTabListItem.InventoryTabItemToShow(
                    InventoryEpoxyModel(
                        inventoryItem = item,
                        source = InventorySource.ITEM,
                    )
                )
            )
        }
        return inventoryTabListItemList
    }
}
