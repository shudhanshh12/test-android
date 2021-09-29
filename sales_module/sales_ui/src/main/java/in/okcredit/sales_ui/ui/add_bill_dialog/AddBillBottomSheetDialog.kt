package `in`.okcredit.sales_ui.ui.add_bill_dialog

import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.analytics.SalesAnalytics
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.Event.CASH_SALE_ITEM_NAME_ADD
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.Event.CASH_SALE_ITEM_PRICE_ADD
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.Event.CASH_SALE_ITEM_QUANTITY_EDIT
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.Event.CASH_SALE_ITEM_VIEW
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.Event.CASH_SALE_NEW_ITEM_ADD_FAILED
import `in`.okcredit.sales_ui.databinding.DialogAddBillItemBinding
import `in`.okcredit.sales_ui.utils.DecimalDigitsInputFilter
import `in`.okcredit.sales_ui.utils.SalesUtil
import `in`.okcredit.shared.base.BaseBottomDialogScreen
import `in`.okcredit.shared.base.UserIntent
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.setBooleanVisibility
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.utils.KeyboardUtil.showKeyboardImplicit
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject

class AddBillBottomSheetDialog :
    BaseBottomDialogScreen<AddBillContract.State>("AddBillBottomSheetDialog"),
    AddBillContract.Navigator {
    private var listener: AddBillDialogListener? = null

    interface AddBillDialogListener {
        fun onSubmitAddBillItem(billItem: BillModel.BillItem)
        fun onSubmitUpdateBillItem(billItem: BillModel.BillItem)
    }

    @Inject
    lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var salesAnalytics: SalesAnalytics

    lateinit var binding: DialogAddBillItemBinding

    private var billItem: BillModel.BillItem? = null
    private val setNameSubject: PublishSubject<String> = PublishSubject.create()
    private val setRateSubject: PublishSubject<String> = PublishSubject.create()
    private val setQuantitySubject: PublishSubject<String> = PublishSubject.create()
    private val setDataSubject: PublishSubject<BillModel.BillItem> = PublishSubject.create()
    private val showRateAndQuantitySubject: PublishSubject<Boolean> = PublishSubject.create()
    private val setSaveEnableSubject: PublishSubject<Boolean> = PublishSubject.create()

    private var addNameEventStarted: Boolean = false
    private var addPriceEventStarted: Boolean = false

    private var flowEvent = "new"
    private var isFirstTimeFlow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogAddBillItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun setData(billItem: BillModel.BillItem) {
        this.billItem = billItem
        setDataSubject.onNext(billItem)
    }

    fun setListener(listener: AddBillDialogListener) {
        this.listener = listener
    }

    override fun onDetach() {
        listener = null
        billItem = null
        super.onDetach()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            isFirstTimeFlow = it.getBoolean("isFirstTime")
        }
        initDialog()
        initView()
        setListeners()
    }

    private fun initDialog() {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog?.setOnShowListener {
            val dialog = dialog as BottomSheetDialog?
            val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<View>(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun initView() {
        binding.apply {
            nameEditText.requestFocus()
            showKeyboardImplicit(requireContext(), binding.nameEditText)
            title.text = getString(R.string.add_item)
            billItem?.let {
                nameEditText.setText(it.name)
                nameEditText.setSelection(it.name.length)
                quantityEditText.setText(SalesUtil.displayDecimalNumber(it.quantity))
                if (it.id.isNotEmpty()) {
                    title.text = getString(R.string.update_item)
                    rateEditText.setText(SalesUtil.displayDecimalNumber(it.rate))
                    flowEvent = "update"
                    rateEditText.requestFocus()
                } else {
                    rateEditText.requestFocus()
                }
            }
        }
        salesAnalytics.trackEvents(
            CASH_SALE_ITEM_VIEW,
            screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
            propertiesMap = PropertiesMap.create()
                .add("search", billItem?.id?.isEmpty() ?: false)
                .add(SalesAnalytics.PropertyKey.FLOW, flowEvent)
        )
    }

    private fun setSaveEnable(isEnabled: Boolean) {
        if (isEnabled.not()) {
            binding.submitBillItem.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.grey400)
            binding.submitBillItem.isEnabled = false
        } else {
            binding.submitBillItem.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.green_primary)
            binding.submitBillItem.isEnabled = true
        }
    }

    private fun setListeners() {
        binding.nameEditText.doOnTextChanged { text, start, count, after ->
            if (addNameEventStarted.not()) {
                addNameEventStarted = true
                salesAnalytics.trackEvents(
                    CASH_SALE_ITEM_NAME_ADD,
                    screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
                    propertiesMap = PropertiesMap.create()
                        .add(SalesAnalytics.PropertyKey.FLOW, flowEvent)
                        .add(SalesAnalytics.PropertyKey.FIRST_FLOW, isFirstTimeFlow)
                )
            }
            if (text != null && text.length < 60) {
                binding.nameLayout.isErrorEnabled = false
                setNameSubject.onNext(text.toString().trim())
            } else {
                binding.nameLayout.isErrorEnabled = true
                binding.nameLayout.error = requireContext().getString(R.string.item_name_max_count)
            }
        }
        binding.rateEditText.doOnTextChanged { text, start, count, after ->
            if (addPriceEventStarted.not()) {
                addPriceEventStarted = true
                salesAnalytics.trackEvents(
                    CASH_SALE_ITEM_PRICE_ADD,
                    screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
                    propertiesMap = PropertiesMap.create()
                        .add(SalesAnalytics.PropertyKey.FLOW, flowEvent)
                        .add(SalesAnalytics.PropertyKey.FIRST_FLOW, isFirstTimeFlow)
                )
            }
            setRateSubject.onNext(text.toString().trim())
        }
        binding.rateEditText.filters = arrayOf(DecimalDigitsInputFilter())
        binding.quantityEditText.filters = arrayOf(DecimalDigitsInputFilter())
        binding.quantityEditText.doOnTextChanged { text, start, count, after ->
            if (binding.quantityEditText.hasFocus()) {
                salesAnalytics.trackEvents(
                    CASH_SALE_ITEM_QUANTITY_EDIT,
                    screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
                    propertiesMap = PropertiesMap.create()
                        .add("Method", "Typing")
                        .add(
                            SalesAnalytics.PropertyKey.TYPE,
                            "add"
                        )
                        .add(SalesAnalytics.PropertyKey.FLOW, flowEvent)
                        .add(SalesAnalytics.PropertyKey.FIRST_FLOW, isFirstTimeFlow)
                )
            }
            val qty = if (text.toString().trim() == ".") "0" else text.toString().trim()
            setQuantitySubject.onNext(qty)
        }
        binding.nameEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.rateEditText.requestFocus()
            }
            return@setOnEditorActionListener true
        }

        binding.quantityEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus.not() && binding.quantityEditText.text.isNullOrEmpty()) {
                binding.quantityEditText.setText("1")
            }
        }
    }

    override fun loadIntent(): UserIntent {
        return AddBillContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            Observable.just(AddBillContract.Intent.SetDataIntent(billItem)),
            setNameSubject.map {
                AddBillContract.Intent.SetNameIntent(it)
            },
            setRateSubject.map {
                AddBillContract.Intent.SetRateIntent(it)
            },
            setQuantitySubject.map {
                AddBillContract.Intent.SetQuantityIntent(it)
            },
            setDataSubject.map {
                AddBillContract.Intent.SetDataIntent(it)
            },
            showRateAndQuantitySubject.map {
                AddBillContract.Intent.ShowRateAndQuantityIntent
            },
            setSaveEnableSubject.map {
                AddBillContract.Intent.SetSaveEnableIntent(it)
            },
            binding.add.clicks().map {
                salesAnalytics.trackEvents(
                    CASH_SALE_ITEM_QUANTITY_EDIT,
                    screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
                    propertiesMap = PropertiesMap.create()
                        .add("Method", "Button")
                        .add(SalesAnalytics.PropertyKey.TYPE, "add")
                        .add(SalesAnalytics.PropertyKey.FLOW, flowEvent)
                        .add(SalesAnalytics.PropertyKey.FIRST_FLOW, isFirstTimeFlow)
                )
                AddBillContract.Intent.PlusIntent("0")
            },
            binding.plus.clicks().map {
                salesAnalytics.trackEvents(
                    CASH_SALE_ITEM_QUANTITY_EDIT,
                    screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
                    propertiesMap = PropertiesMap.create()
                        .add("Method", "Button")
                        .add(SalesAnalytics.PropertyKey.TYPE, "add")
                        .add(SalesAnalytics.PropertyKey.FLOW, flowEvent)
                        .add(SalesAnalytics.PropertyKey.FIRST_FLOW, isFirstTimeFlow)
                )
                AddBillContract.Intent.PlusIntent(binding.quantityEditText.text.toString())
            },
            binding.minus.clicks().map {
                salesAnalytics.trackEvents(
                    CASH_SALE_ITEM_QUANTITY_EDIT,
                    screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
                    propertiesMap = PropertiesMap.create()
                        .add("Method", "Button")
                        .add(SalesAnalytics.PropertyKey.TYPE, "sub")
                        .add(SalesAnalytics.PropertyKey.FLOW, flowEvent)
                        .add(SalesAnalytics.PropertyKey.FIRST_FLOW, isFirstTimeFlow)
                )
                AddBillContract.Intent.MinusIntent(binding.quantityEditText.text.toString())
            },
            binding.submitBillItem.clicks()
                .map {
                    val rate = getCurrentState().rate
                    val qty = getCurrentState().quantity
                    if (binding.quantityError.isVisible) {
                        return@map AddBillContract.Intent.ShowErrorIntent("Invalid Quantity")
                    }
                    if (binding.rateLayout.isErrorEnabled) {
                        return@map AddBillContract.Intent.ShowErrorIntent("Invalid Rate")
                    }
                    if (billItem != null && billItem!!.id.isNotEmpty()) {
                        updateBillItem(qty, rate)
                    } else {
                        AddBillContract.Intent.AddBillItemIntent(
                            BillModel.AddBillItemRequest(
                                BillModel.AddBillItem(
                                    getCurrentState().name,
                                    rate
                                )
                            )
                        )
                    }
                }
        )
    }

    private fun updateBillItem(qty: Double, rate: Double): AddBillContract.Intent {
        val list = mutableListOf<String>()
        if (getCurrentState().name != billItem!!.name) {
            list.add("name")
        }
        if (getCurrentState().rate != billItem!!.rate) {
            list.add("rate")
        }
        if (list.isEmpty()) {
            return AddBillContract.Intent.UpdateQuantityIntent(
                BillModel.BillItem(
                    billItem!!.id,
                    billItem!!.name,
                    billItem!!.rate,
                    qty
                )
            )
        }
        return AddBillContract.Intent.UpdateBillItemIntent(
            billItem!!.id,
            BillModel.UpdateBillItemRequest(
                BillModel.AddBillItem(
                    getCurrentState().name,
                    rate
                ),
                list
            )
        )
    }

    override fun render(state: AddBillContract.State) {
        setSaveEnable(state.name.isNotEmpty())
        if (state.quantity > 1) {
            binding.minus.setImageResource(R.drawable.ic_minus)
            binding.minus.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.green_primary)
        } else {
            binding.minus.setImageResource(R.drawable.ic_delete_bill)
            binding.minus.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.grey400)
        }
        if (state.rate > 99999.99) {
            binding.rateLayout.error = context?.getString(R.string.invalid_rate)
            binding.rateLayout.isErrorEnabled = true
        } else {
            binding.rateLayout.isErrorEnabled = false
        }
        if ((SalesUtil.isDecimal(state.quantity) && state.quantity > 999.99) || (SalesUtil.isDecimal(state.quantity).not() && state.quantity > 9999.99)) {
            binding.quantityError.visibility = View.VISIBLE
        } else {
            binding.quantityError.visibility = View.GONE
        }
    }

    override fun goToLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreen(requireActivity())
        }
    }

    @UiThread
    override fun showError(msg: String?) {
        activity?.runOnUiThread {
            if (context == null) return@runOnUiThread
            msg?.let {
                if (it.contains("403")) {
                    binding.nameLayout.error = requireContext().getString(R.string.item_name_already_exist)
                    binding.nameLayout.isErrorEnabled = true
                    return@runOnUiThread
                }
            }
            shortToast(msg ?: getString(R.string.err_default))
        }
    }

    override fun onBillItemAdded(billItem: BillModel.BillItem) {
        val text = binding.quantityEditText.text.toString()
        billItem.quantity = if (text.isNotEmpty()) text.toDouble() else 0.0
        listener?.onSubmitAddBillItem(billItem)
        dismiss()
    }

    override fun onBillItemUpdated(billItem: BillModel.BillItem) {
        val text = binding.quantityEditText.text.toString()
        billItem.quantity = if (text.isNotEmpty()) text.toDouble() else 0.0
        listener?.onSubmitUpdateBillItem(billItem)
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        hideSoftKeyboard()
        super.onDismiss(dialog)
    }

    override fun onAddFailed(msg: String) {
        salesAnalytics.trackEvents(
            CASH_SALE_NEW_ITEM_ADD_FAILED,
            screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
            propertiesMap = PropertiesMap.create()
                .add(SalesAnalytics.PropertyKey.PRICE, getCurrentState().rate)
                .add(SalesAnalytics.PropertyKey.ITEM_NAME, getCurrentState().name)
                .add(SalesAnalytics.PropertyKey.FLOW, "new")
                .add(SalesAnalytics.PropertyKey.FIRST_FLOW, isFirstTimeFlow)
                .add("reason", msg)
        )
        showError(msg)
    }

    override fun onUpdateFailed(msg: String) {
        salesAnalytics.trackEvents(
            CASH_SALE_NEW_ITEM_ADD_FAILED,
            screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
            propertiesMap = PropertiesMap.create()
                .add(SalesAnalytics.PropertyKey.PRICE, getCurrentState().rate)
                .add(SalesAnalytics.PropertyKey.ITEM_NAME, getCurrentState().name)
                .add(SalesAnalytics.PropertyKey.FLOW, "update")
                .add(SalesAnalytics.PropertyKey.FIRST_FLOW, isFirstTimeFlow)
                .add("reason", msg)
        )
        showError(msg)
    }

    override fun updateQuantity(qty: String) {
        activity?.runOnUiThread {
            binding.quantityEditText.clearFocus()
            binding.quantityEditText.setText(qty)
            val showAdd = qty.toDouble() == 0.0
            if (showAdd) {
                binding.quantityLayout.background =
                    resources.getDrawable(R.drawable.circle_background_grey100_outline)
            } else {
                binding.quantityLayout.background = resources.getDrawable(R.drawable.circle_background_green_primary_outline)
            }
            binding.add.setBooleanVisibility(showAdd)
            binding.minus.setBooleanVisibility(showAdd.not())
            binding.quantityEditText.setBooleanVisibility(showAdd.not())
        }
    }

    companion object {
        private const val ID = "id"
        const val TAG = "AddBillBottomSheetDialog"
        fun newInstance(isFirstTime: Boolean): AddBillBottomSheetDialog {
            val frag = AddBillBottomSheetDialog()
            val bundle = Bundle()
            bundle.putBoolean("isFirstTimeFlow", isFirstTime)
            frag.arguments = bundle
            return frag
        }
    }
}
