package `in`.okcredit.collection_ui.ui.inventory.create_bill

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.InventoryItem
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.PropertyValue.NEW_BILL
import `in`.okcredit.collection_ui.databinding.FragmentInventoryItemListBinding
import `in`.okcredit.collection_ui.ui.inventory.add_item_dialog.AddInventoryItemBottomSheetDialog
import `in`.okcredit.collection_ui.ui.inventory.add_item_dialog.AddInventoryItemBottomSheetDialog.Companion.TAG
import `in`.okcredit.collection_ui.ui.inventory.create_bill.view.InventoryItemView
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.web.WebExperiment
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject

class InventoryItemListFragment :
    BaseFragment<InventoryItemListContract.State, InventoryItemListContract.ViewEvents, InventoryItemListContract.Intent>(
        "InventoryItemListFragment",
        contentLayoutId = R.layout.fragment_inventory_item_list
    ),
    AddInventoryItemBottomSheetDialog.AddBillDialogListener,
    InventoryItemView.Listener {

    companion object {
        const val KEY_BILL_ID = "billId"
        const val KEY_BUSINESS_ID = "businessId"
        const val KEY_TYPE = "type"
        const val MERCHANT = "merchant"
    }

    private val binding: FragmentInventoryItemListBinding by viewLifecycleScoped(FragmentInventoryItemListBinding::bind)

    private val controller by lazy { InventoryItemListController(this) }

    private var inventoryNavigationListener: `in`.okcredit.collection_ui.ui.inventory.InventoryNavigationListener? = null

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        setClickListener()
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if (context is `in`.okcredit.collection_ui.ui.inventory.InventoryNavigationListener) {
            inventoryNavigationListener = context
        }
    }

    private fun setClickListener() {
        binding.clNewItemLayout.setOnClickListener {
            pushIntent(InventoryItemListContract.Intent.OpenInventoryBottomSheetDialog)
        }

        binding.toolbar.setNavigationOnClickListener {
            val popped = requireActivity().supportFragmentManager.popBackStackImmediate()
            if (!popped) {
                requireActivity().finish()
            }
        }

        binding.buttonSaveBill.setOnClickListener {
            val finalList = mutableListOf<InventoryItem>()
            getCurrentState().listToShow.forEach {
                if (it.value.quantity > 0) {
                    finalList.add(it.value)
                }
            }

            if (finalList.size > 0) {
                pushIntent(InventoryItemListContract.Intent.CreateBill(finalList))
            } else {
                shortToast(R.string.inventory_no_item_added)
            }
        }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun loadIntent(): UserIntent {
        return InventoryItemListContract.Intent.Load
    }

    private fun initList() {
        binding.rvBillItems.adapter = controller.adapter
    }

    override fun render(state: InventoryItemListContract.State) {
        controller.setData(state.inventoryItemList)
        setEmptyView(state)
        setTotalBillView(state)
    }

    private fun setEmptyView(state: InventoryItemListContract.State) {
        if (state.listToShow.values.isEmpty()) {
            binding.textEmpty.visible()
        } else {
            binding.textEmpty.gone()
        }
    }

    private fun setTotalBillView(state: InventoryItemListContract.State) {
        if (state.totalBill > 0L) {
            binding.clBillTotalLayout.visible()
            binding.textBillTotal.text = getString(R.string.bill_total, CurrencyUtil.formatV2(state.totalBill))
            binding.textItemQty.text =
                getString(R.string.items_and_qty, state.totalItem.toString(), state.totalQuantity.toString())
        } else {
            binding.clBillTotalLayout.gone()
        }
    }

    override fun handleViewEvent(event: InventoryItemListContract.ViewEvents) {
        when (event) {
            is InventoryItemListContract.ViewEvents.OpenBillWebView -> {
                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                    val queryParams = mapOf(
                        KEY_BILL_ID to event.billId,
                        KEY_BUSINESS_ID to event.businessId,
                        KEY_TYPE to MERCHANT,
                    )
                    legacyNavigator.get()
                        .goWebExperimentScreen(requireActivity(), WebExperiment.Experiment.BILLING.type, queryParams)
                    inventoryNavigationListener?.loadBills()
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
            is InventoryItemListContract.ViewEvents.ShowError -> shortToast(event.err)
            InventoryItemListContract.ViewEvents.OpenInventoryBottomSheetDialog -> {
                val instance = AddInventoryItemBottomSheetDialog.newInstance(screen = NEW_BILL)
                instance.setListener(this)
                instance.show(childFragmentManager, TAG)
            }
        }
    }

    override fun onAdd(billItem: InventoryItem) {
        pushIntent(InventoryItemListContract.Intent.AddItem(billItem))
    }

    override fun onDelete(billItem: InventoryItem) {
        pushIntent(InventoryItemListContract.Intent.DeleteItem(billItem))
    }

    override fun onSubmitAddBillItem(billItem: InventoryItem) {
        pushIntent(InventoryItemListContract.Intent.AddItem(billItem))
    }
}
