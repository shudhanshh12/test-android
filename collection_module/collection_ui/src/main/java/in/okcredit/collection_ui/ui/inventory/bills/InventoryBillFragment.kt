package `in`.okcredit.collection_ui.ui.inventory.bills

import `in`.okcredit.collection.contract.InventoryItem
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.FragmentInventoryBillBinding
import `in`.okcredit.collection_ui.ui.inventory.view.InventoryTabItemView
import `in`.okcredit.collection_ui.utils.scrollToTopOnItemInsertItem
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.web.WebExperiment
import android.os.Bundle
import android.view.View
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject

class InventoryBillFragment :
    BaseFragment<InventoryBillContract.State, InventoryBillContract.ViewEvents, InventoryBillContract.Intent>(
        "InventoryBillFragment",
        contentLayoutId = R.layout.fragment_inventory_bill
    ),
    InventoryTabItemView.Listener {

    companion object {
        const val KEY_BILL_ID = "billId"
        const val KEY_BUSINESS_ID = "businessId"
        const val KEY_TYPE = "type"
        const val MERCHANT = "merchant"
    }

    private val binding: FragmentInventoryBillBinding by viewLifecycleScoped(FragmentInventoryBillBinding::bind)

    private val controller by lazy { InventoryBillController(this) }

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun loadIntent(): UserIntent {
        return InventoryBillContract.Intent.Load
    }

    private fun initList() {
        binding.rvList.adapter = controller.adapter
        controller.adapter.scrollToTopOnItemInsertItem(binding.rvList)
    }

    override fun render(state: InventoryBillContract.State) {
        controller.setData(state.inventoryTabListItem)
        binding.tvSubHeading.text = getString(R.string.inventory_bill_created, state.inventoryBillList.size.toString())

        if (state.inventoryBillList.isEmpty()) {
            binding.textEmpty.visible()
            binding.textEmpty.text =
                getString(R.string.inventory_empty_bill_msg)
        } else {
            binding.textEmpty.gone()
        }
    }

    override fun handleViewEvent(event: InventoryBillContract.ViewEvents) {
        when (event) {
            is InventoryBillContract.ViewEvents.ShowError -> shortToast(event.err)
            is InventoryBillContract.ViewEvents.OpenBillWebView -> {
                val queryParams = mapOf(
                    KEY_BILL_ID to event.billId,
                    KEY_BUSINESS_ID to event.merchantId,
                    KEY_TYPE to MERCHANT,
                )
                legacyNavigator.get()
                    .goWebExperimentScreen(requireActivity(), WebExperiment.Experiment.BILLING.type, queryParams)
            }
        }
    }

    override fun clickedBill(inventoryItem: InventoryItem) {
        pushIntent(
            InventoryBillContract.Intent.OpenBillWebView(
                billId = inventoryItem.billId ?: "",
                merchantId = inventoryItem.merchantId ?: "",
            )
        )
    }

    fun loadBills() {
        pushIntent(InventoryBillContract.Intent.Load)
    }
}
