package `in`.okcredit.collection_ui.ui.inventory.items

import `in`.okcredit.collection.contract.InventoryItem
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.PropertyValue.ITEMS
import `in`.okcredit.collection_ui.databinding.FragmentInventoryBillBinding
import `in`.okcredit.collection_ui.ui.inventory.add_item_dialog.AddInventoryItemBottomSheetDialog
import `in`.okcredit.collection_ui.ui.inventory.view.InventoryTabItemView
import `in`.okcredit.collection_ui.utils.scrollToTopOnItemInsertItem
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible

class InventoryItemFragment :
    BaseFragment<InventoryItemContract.State, InventoryItemContract.ViewEvents, InventoryItemContract.Intent>(
        "InventoryItemFragment",
        contentLayoutId = R.layout.fragment_inventory_bill
    ),
    AddInventoryItemBottomSheetDialog.AddBillDialogListener,
    InventoryTabItemView.Listener {

    private val binding: FragmentInventoryBillBinding by viewLifecycleScoped(FragmentInventoryBillBinding::bind)

    private val controller by lazy { InventoryItemController(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun loadIntent(): UserIntent? {
        return InventoryItemContract.Intent.Load
    }

    private fun initList() {
        binding.rvList.adapter = controller.adapter
        controller.adapter.scrollToTopOnItemInsertItem(binding.rvList)
    }

    override fun render(state: InventoryItemContract.State) {
        controller.setData(state.inventoryTabListItem)

        if (state.inventoryList.isEmpty()) {
            binding.textEmpty.visible()
            binding.textEmpty.text =
                getString(R.string.inventory_item_empty_msg)
        } else
            binding.textEmpty.gone()

        var totalQuantity = 0
        state.inventoryList.forEach {
            totalQuantity += it.quantity
        }

        binding.tvSubHeading.text = if (totalQuantity > 0) {
            getString(R.string.inventory_item_in_stock, totalQuantity.toString())
        } else {
            getString(R.string.inventory_no_item_in_stock)
        }
    }

    override fun handleViewEvent(event: InventoryItemContract.ViewEvents) {
        when (event) {
            is InventoryItemContract.ViewEvents.ShowError -> shortToast(event.err)
            is InventoryItemContract.ViewEvents.FillProductDetails -> {
                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                    clickedBill(InventoryItem(item = event.productName))
                }
            }
        }
    }

    override fun onSubmitAddBillItem(billItem: InventoryItem) {
        pushIntent(InventoryItemContract.Intent.CreateItem(billItem))
    }

    override fun clickedBill(billItem: InventoryItem) {
        val instance = AddInventoryItemBottomSheetDialog.newInstance(
            item = billItem.item,
            quantity = billItem.quantity,
            price = billItem.price,
            isNameEditable = false,
            screen = ITEMS,
        )
        instance.setListener(this)
        instance.show(childFragmentManager, AddInventoryItemBottomSheetDialog.TAG)
    }

    fun addItem() {
        val instance = AddInventoryItemBottomSheetDialog.newInstance(screen = ITEMS)
        instance.setListener(this@InventoryItemFragment)
        instance.show(childFragmentManager, AddInventoryItemBottomSheetDialog.TAG)
    }
}
