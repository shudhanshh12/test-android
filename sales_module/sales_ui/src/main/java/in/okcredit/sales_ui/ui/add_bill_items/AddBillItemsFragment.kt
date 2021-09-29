package `in`.okcredit.sales_ui.ui.add_bill_items

import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.analytics.SalesAnalytics
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.Event.CASH_SALE_ITEM_ADD_COMPLETED
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.Event.CASH_SALE_ITEM_QUANTITY_EDIT
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.Event.CASH_SALE_ITEM_SEARCH
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.Event.CASH_SALE_ITEM_SELECT
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.Event.CASH_SALE_NEW_ITEM_ADD_COMPLETED
import `in`.okcredit.sales_ui.databinding.AddBillItemsScreenBinding
import `in`.okcredit.sales_ui.dialogs.InputBillAmountDialog
import `in`.okcredit.sales_ui.ui.add_bill_dialog.AddBillBottomSheetDialog
import `in`.okcredit.sales_ui.ui.add_bill_items.AddBillItemsContract.*
import `in`.okcredit.sales_ui.ui.add_bill_items.views.AddBillView
import `in`.okcredit.sales_ui.ui.add_bill_items.views.BillItemController
import `in`.okcredit.sales_ui.utils.SalesUtil
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.clear
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.setBooleanVisibility
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddBillItemsFragment :
    BaseFragment<State, ViewEvent, Intent>("AddBillItemsScreen"),
    AddBillBottomSheetDialog.AddBillDialogListener,
    AddBillView.Listener,
    InputBillAmountDialog.Listener {

    @Inject
    internal lateinit var salesAnalytics: SalesAnalytics

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    lateinit var binding: AddBillItemsScreenBinding

    private val controller = BillItemController(this)

    private var billItemsDisposable: Disposable? = null

    private val addBillDialogSubject: PublishSubject<Unit> = PublishSubject.create()
    private val updateBillDialogSubject: PublishSubject<BillModel.BillItem> = PublishSubject.create()
    private val addBillItemSubject: PublishSubject<BillModel.BillItem> = PublishSubject.create()
    private val removeBillItemSubject: PublishSubject<BillModel.BillItem> = PublishSubject.create()

    private var addBillDialog: AddBillBottomSheetDialog? = null

    private var billTotalDialog: InputBillAmountDialog? = null

    lateinit var linearLayoutManager: LinearLayoutManager

    private var isSearched = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AddBillItemsScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val viewModel = activity?.let { viewModelProvider(it).get(BillSharedViewModel::class.java) }
        initListView()
        binding.newItemLayout.setOnClickListener {
            salesAnalytics.trackEvents(
                eventName = SalesAnalytics.Event.CASH_SALE_NEW_ITEM_ADD_STARTED,
                screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
                propertiesMap = PropertiesMap.create()
                    .add(SalesAnalytics.PropertyKey.FIRST_FLOW, false)
            )
            addBillDialogSubject.onNext(Unit)
        }
        binding.addBtn.setOnClickListener {
            activity?.runOnUiThread {
                val listTotal: Double = getCurrentState().billedItems?.total?.toDouble() ?: 0.0
                val total: Double = if (getCurrentState().totalQuantity > 0) listTotal else getCurrentState().total
                salesAnalytics.trackEvents(
                    CASH_SALE_ITEM_ADD_COMPLETED,
                    screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
                    propertiesMap = PropertiesMap.create()
                        .add("Items", getCurrentState().addedItems.size)
                        .add(SalesAnalytics.PropertyKey.QUANTITY, getCurrentState().totalQuantity)
                        .add(SalesAnalytics.PropertyKey.AMOUNT, total)
                        .add(SalesAnalytics.PropertyKey.SEARCH, isSearched)
                )
                pushIntent(
                    Intent.AddSale(
                        total,
                        if (getCurrentState().totalQuantity > 0) getCurrentState().billedItems else null
                    )
                )
            }
        }
        binding.toolbar.setNavigationOnClickListener {
            if (binding.search.hasFocus()) {
                hideSoftKeyboard()
                it.postDelayed(
                    {
                        activity?.onBackPressed()
                    },
                    200
                )
            } else {
                activity?.onBackPressed()
            }
        }
        binding.search.doOnTextChanged { text, _, _, _ ->
            text?.let {
                pushIntent(Intent.SearchBillItemIntent(it.toString().trim()))
            }
        }
        binding.search.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                salesAnalytics.trackEvents(
                    CASH_SALE_ITEM_SEARCH,
                    screen = SalesAnalytics.PropertyValue.CASH_SALE_TX
                )
            }
        }
        binding.btnClose.setOnClickListener {
            binding.search.setText("")
        }
        binding.addBillAmount.setOnClickListener {
            if (billTotalDialog == null) {
                billTotalDialog = InputBillAmountDialog()
            }
            billTotalDialog?.show(childFragmentManager, InputBillAmountDialog.TAG)
        }
        KeyboardVisibilityEvent.registerEventListener(requireActivity()) {
            if (it.not()) {
                binding.search.clearFocus()
            }
        }
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        if (childFragment is InputBillAmountDialog) {
            childFragment.setListener(this)
        }
    }

    private fun initListView() {
        linearLayoutManager = LinearLayoutManager(context)
        binding.rvBillItems.adapter = controller.adapter
        binding.rvBillItems
        binding.rvBillItems.layoutManager = linearLayoutManager
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            addBillDialogSubject.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Intent.ShowAddBillDialogIntent
                },
            updateBillDialogSubject.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Intent.ShowUpdateBillDialogIntent(it)
                },
            addBillItemSubject.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Intent.AddBillItemIntent(it)
                },
            removeBillItemSubject.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Intent.RemoveBillItemIntent(it)
                },
        )
    }

    override fun onResume() {
        super.onResume()
        AnimationUtils.pendulumMotion(binding.img)
    }

    override fun onPause() {
        binding.img.clearAnimation()
        super.onPause()
    }

    override fun render(state: State) {
        controller.setState(state)
        binding.introductoryText.setBooleanVisibility(state.inventoryItems != null && state.inventoryItems!!.isEmpty())
        if (state.inventoryItems != null && state.inventoryItems!!.size > 5 || state.addedItems.size > 5) {
            binding.search.visibility = View.VISIBLE
            binding.toolbarTitle.visibility = View.GONE
        } else {
            binding.search.visibility = View.GONE
            binding.toolbarTitle.visibility = View.VISIBLE
        }
        if (state.searchQuery.isNotEmpty()) {
            binding.selectItems.visibility = View.GONE
            binding.btnClose.visibility = View.VISIBLE
            linearLayoutManager.scrollToPosition(0)
            val lp = binding.rvBillItems.layoutParams as ConstraintLayout.LayoutParams
            lp.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
            binding.rvBillItems.layoutParams = lp
            val noResult = (
                state.inventoryItems?.filter {
                    it.name.toLowerCase().startsWith(state.searchQuery.toLowerCase())
                }?.size == 0
                )
            binding.noResultLayout.setBooleanVisibility(noResult)
        } else {
            binding.selectItems.visibility = View.VISIBLE
            binding.btnClose.visibility = View.INVISIBLE
            val lp = binding.rvBillItems.layoutParams as ConstraintLayout.LayoutParams
            lp.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            binding.rvBillItems.layoutParams = lp
        }
        val listTotal: Double = getCurrentState().billedItems?.total?.toDouble() ?: 0.0
        val total: Double = if (getCurrentState().totalQuantity > 0) listTotal else getCurrentState().total
        binding.billTotal.text = requireContext().getString(
            R.string.bill_total,
            SalesUtil.currencyDisplayFormat(total)
        )
        binding.itemsQty.text = requireContext().getString(
            R.string.items_and_qty,
            state.billedItems?.items?.size ?: "0",
            SalesUtil.displayDecimalNumber(state.totalQuantity)
        )
        if (state.totalQuantity > 0) {
            binding.addBillAmount.gone()
            binding.itemsQty.visible()
        } else {
            binding.addBillAmount.visible()
            binding.itemsQty.gone()
        }
    }

    override fun handleViewEvent(effect: ViewEvent) {
        when (effect) {
            ViewEvent.GoToLoginScreen -> {
                legacyNavigator.goToLoginScreenForAuthFailure(requireContext())
            }
            is ViewEvent.ShowError -> {
                context?.shortToast(effect.msg)
            }
            ViewEvent.ShowAddBillDialog -> {
                addBillDialog = AddBillBottomSheetDialog()
                addBillDialog?.setListener(this)
                addBillDialog?.show(childFragmentManager, AddBillBottomSheetDialog.TAG)
            }
            is ViewEvent.ShowUpdateBillDialog -> {
                addBillDialog = AddBillBottomSheetDialog()
                addBillDialog?.setListener(this)
                addBillDialog?.show(childFragmentManager, AddBillBottomSheetDialog.TAG)
                addBillDialog?.setData(effect.billItem)
            }
            ViewEvent.UpdateList -> {
                binding.rvBillItems.adapter?.notifyDataSetChanged()
            }
            ViewEvent.ScrollToTop -> {
                binding.rvBillItems.postDelayed(
                    {
                        linearLayoutManager.scrollToPosition(0)
                    },
                    200
                )
            }
            ViewEvent.ClearSearch -> {
                binding.search.clear()
                binding.search.clearFocus()
                hideSoftKeyboard()
                linearLayoutManager.scrollToPosition(0)
            }
            is ViewEvent.onAddSaleSuccessfull -> {
                activity?.runOnUiThread {
                    if (findNavController(this).currentDestination?.id == R.id.addBillItemsScreen) {
                        findNavController(this).navigate(
                            R.id.action_addBillItemsScreen_to_billSummaryScreen,
                            bundleOf("sale_id" to effect.saleId)
                        )
                    }
                }
            }
        }
    }

    override fun onSubmitAddBillItem(billItem: BillModel.BillItem) {
        salesAnalytics.trackEvents(
            CASH_SALE_NEW_ITEM_ADD_COMPLETED,
            screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
            propertiesMap = PropertiesMap.create()
                .add("Items", getCurrentState().addedItems?.size ?: 0)
                .add(SalesAnalytics.PropertyKey.QUANTITY, getCurrentState().totalQuantity)
                .add(SalesAnalytics.PropertyKey.AMOUNT, getCurrentState().billedItems?.total ?: 0.0)
                .add(SalesAnalytics.PropertyKey.SEARCH, isSearched)
                .add(SalesAnalytics.PropertyKey.FLOW, "new")
        )
        pushIntent(Intent.NewBillItemIntent(billItem))
    }

    override fun onSubmitUpdateBillItem(billItem: BillModel.BillItem) {
        salesAnalytics.trackEvents(
            CASH_SALE_NEW_ITEM_ADD_COMPLETED,
            screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
            propertiesMap = PropertiesMap.create()
                .add("Items", getCurrentState().addedItems?.size ?: 0)
                .add(SalesAnalytics.PropertyKey.QUANTITY, getCurrentState().totalQuantity)
                .add(SalesAnalytics.PropertyKey.AMOUNT, getCurrentState().billedItems?.total ?: 0.0)
                .add(SalesAnalytics.PropertyKey.SEARCH, isSearched)
                .add(SalesAnalytics.PropertyKey.FLOW, "update")
        )
        pushIntent(Intent.UpdateBillItemIntent(billItem))
    }

    override fun onAdd(billItem: BillModel.BillItem) {
        salesAnalytics.trackEvents(
            CASH_SALE_ITEM_SELECT,
            screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
            propertiesMap = PropertiesMap.create()
                .add("search", isSearched)
                .add("word entered", getCurrentState().searchQuery)
        )
        val qty = 1.0
        addBillItemSubject.onNext(BillModel.BillItem(billItem.id, billItem.name, billItem.rate, qty))
    }

    override fun onAddQuantity(billItem: BillModel.BillItem) {
        salesAnalytics.trackEvents(
            CASH_SALE_ITEM_QUANTITY_EDIT,
            screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
            propertiesMap = PropertiesMap.create()
                .add("Method", "Button")
                .add(SalesAnalytics.PropertyKey.TYPE, "add")
                .add(SalesAnalytics.PropertyKey.FLOW, "list")
                .add(SalesAnalytics.PropertyKey.FIRST_FLOW, false)
        )
        var qty = billItem.quantity + 1
        if (qty > 999.99) {
            qty = qty.toInt().toDouble()
        }
        if (qty < 10000) {
            pushIntent(Intent.UpdateBillItemIntent(BillModel.BillItem(billItem.id, billItem.name, billItem.rate, qty)))
        } else {
            shortToast("Invalid Quantity")
        }
    }

    override fun onSubtractQuantity(billItem: BillModel.BillItem) {
        if (billItem.quantity <= 1) {
            return
        }
        salesAnalytics.trackEvents(
            CASH_SALE_ITEM_QUANTITY_EDIT,
            screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
            propertiesMap = PropertiesMap.create()
                .add("Method", "Button")
                .add(SalesAnalytics.PropertyKey.TYPE, "sub")
                .add(SalesAnalytics.PropertyKey.FLOW, "list")
                .add(SalesAnalytics.PropertyKey.FIRST_FLOW, false)
        )
        val qty = billItem.quantity - 1
        pushIntent(Intent.UpdateBillItemIntent(BillModel.BillItem(billItem.id, billItem.name, billItem.rate, qty)))
    }

    override fun onDelete(billItem: BillModel.BillItem) {
        salesAnalytics.trackEvents(
            CASH_SALE_ITEM_QUANTITY_EDIT,
            screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
            propertiesMap = PropertiesMap.create()
                .add("Method", "Button")
                .add(SalesAnalytics.PropertyKey.TYPE, "delete")
                .add(SalesAnalytics.PropertyKey.FLOW, "list")
                .add(SalesAnalytics.PropertyKey.FIRST_FLOW, false)
        )
        removeBillItemSubject.onNext(billItem)
    }

    override fun onTap(billItem: BillModel.BillItem, isSearchItem: Boolean) {
        salesAnalytics.trackEvents(
            CASH_SALE_ITEM_SELECT,
            screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
            propertiesMap = PropertiesMap.create()
                .add("search", isSearchItem)
                .add("word entered", getCurrentState().searchQuery)
        )
        if (billItem.quantity == 0.0) {
            updateBillDialogSubject.onNext(BillModel.BillItem(billItem.id, billItem.name, billItem.rate, 1.0))
        } else {
            updateBillDialogSubject.onNext(billItem)
        }
    }

    override fun onDestroyView() {
        hideSoftKeyboard()
        billItemsDisposable?.dispose()
        super.onDestroyView()
    }

    override fun onAddTotal(total: Double) {
        if (total > 0) {
            binding.addBillAmount.text = getString(R.string.tap_to_edit)
        } else {
            binding.addBillAmount.text = getString(R.string.tap_to_add)
        }
        pushIntent(Intent.SetBillTotal(total))
    }
}
