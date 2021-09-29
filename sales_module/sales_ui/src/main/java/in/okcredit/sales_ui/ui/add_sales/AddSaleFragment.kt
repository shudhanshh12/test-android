package `in`.okcredit.sales_ui.ui.add_sales

import BillSharedViewModel
import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.analytics.SalesAnalytics
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.Event.CASH_SALE_ITEM_ADD_STARTED
import `in`.okcredit.sales_ui.analytics.SalesAnalytics.Event.CASH_SALE_NEW_ITEM_ADD_STARTED
import `in`.okcredit.sales_ui.databinding.AddSaleFragmentBinding
import `in`.okcredit.sales_ui.ui.add_bill_dialog.AddBillBottomSheetDialog
import `in`.okcredit.sales_ui.ui.add_sales.views.BillController
import `in`.okcredit.sales_ui.ui.add_sales.views.CalculatorView
import `in`.okcredit.sales_ui.ui.billing_name.BillingNameBottomSheetDialog
import `in`.okcredit.sales_ui.utils.SalesUtil
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.CommonUtils
import `in`.okcredit.shared.utils.addTo
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.setBooleanVisibility
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddSaleFragment :
    BaseScreen<AddSaleContract.State>("AddSaleScreen"),
    AddSaleContract.Navigator,
    CalculatorView.CalcListener,
    DatePickerDialog.OnDateSetListener,
    BillingNameBottomSheetDialog.Listener,
    AddBillBottomSheetDialog.AddBillDialogListener {

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var tracker: Tracker

    @Inject
    internal lateinit var salesAnalytics: SalesAnalytics

    lateinit var binding: AddSaleFragmentBinding

    private val onDigitClicked: PublishSubject<Int> = PublishSubject.create()
    private val onOperatorClicked: PublishSubject<String> = PublishSubject.create()
    private val onEqualsClicked: PublishSubject<Unit> = PublishSubject.create()
    private val onDotClicked: PublishSubject<Unit> = PublishSubject.create()
    private val onLongBackPress: PublishSubject<Unit> = PublishSubject.create()
    private val onBackPressClicked: PublishSubject<Unit> = PublishSubject.create()
    private val onChangeInputMode: PublishSubject<Int> = PublishSubject.create()
    private val submitSale: PublishSubject<AddSaleViewModel.Sale> = PublishSubject.create()
    private val showDatePicker: PublishSubject<Unit> = PublishSubject.create()
    private val onChangeDate: PublishSubject<DateTime> = PublishSubject.create()
    private val setBillingDataSubject: PublishSubject<Pair<String, String?>> = PublishSubject.create()
    private val showBillingNameSubject: PublishSubject<Unit> = PublishSubject.create()
    private val addBillItem: PublishSubject<BillModel.BillItem> = PublishSubject.create()
    private val setBilledItems: PublishSubject<List<BillModel.BillItem>> = PublishSubject.create()
    private val canShowHandEducationSubject: PublishSubject<Boolean> = PublishSubject.create()
    private val canShowDotHighlightSubject: PublishSubject<Boolean> = PublishSubject.create()

    private var datePickerDialog: DatePickerDialog? = null
    private var billingNameDialog: BillingNameBottomSheetDialog? = null
    private var saleAmountEntered = false
    private var alert: Snackbar? = null

    private val controller = BillController()

    private var billedItems: List<BillModel.BillItem>? = null

    private var canShowHandEducation = false

    private var canShowDotHighlight = false

    private var isAddBillItemStarted = false

    private var isSinglePressed = false

    companion object {
        const val ADDED_SALE = "added_sale"
    }

    private var handEducationTimer = object : CountDownTimer(2000, 1000) {
        override fun onFinish() {
            if (isStateInitialized() && getCurrentState().isNewUser) {
                canShowHandEducation = true
                canShowHandEducationSubject.onNext(true)
            }
        }

        override fun onTick(millisUntilFinished: Long) {
        }
    }.start()

    private var dotHighlightTimer = object : CountDownTimer(2000, 1000) {
        override fun onFinish() {
            if (isStateInitialized() && getCurrentState().isNewUser.not()) {
                canShowDotHighlight = true
                canShowDotHighlightSubject.onNext(true)
            }
        }

        override fun onTick(millisUntilFinished: Long) {
        }
    }.start()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = AddSaleFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onBackPressed(): Boolean {
        if (isSinglePressed.not() && getCurrentState().amountCalculation.isNullOrEmpty().not()) {
            context?.shortToast(getString(R.string.touch_again_exit))
            isSinglePressed = true
            Completable.timer(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    isSinglePressed = false
                }.addTo(autoDisposable)
            return true
        } else {
            hideSoftKeyboard()
            arguments?.let {
                if (it.getBoolean("deeplink", false)) {
                    activity?.finish()
                    return true
                }
            }
            findNavController(this).popBackStack()
            return true
        }
    }

    private fun resetTimer() {
        canShowHandEducation = false
        canShowDotHighlight = false
        canShowHandEducationSubject.onNext(false)
        canShowDotHighlightSubject.onNext(false)
        handEducationTimer.cancel()
        dotHighlightTimer.cancel()
        handEducationTimer.start()
        dotHighlightTimer.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = activity?.let { ViewModelProvider(it).get(BillSharedViewModel::class.java) }
        viewModel?.billItems?.observe(
            viewLifecycleOwner,
            {
                if (billedItems != it) {
                    billedItems = it
                }
            }
        )
        binding.rvBillItems.adapter = controller.adapter
        binding.rvBillItems.layoutManager = LinearLayoutManager(requireContext())
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.calculatorView.setListener(this)

        binding.amountCalculation.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                v.requestFocus()
                onChangeInputMode.onNext(AddSaleContract.INPUT_MODE_AMOUNT)
            }
            return@setOnTouchListener true
        }
        binding.note.setOnFocusChangeListener { v, hasFocus ->
            if (isStateInitialized() && getCurrentState().inputMode == AddSaleContract.INPUT_MODE_AMOUNT) {
                if (hasFocus) {
                    v.setBackgroundResource(R.drawable.background_white_corner_radius_4dp_stroke_green)
                    binding.calculatorView.gone()
                    onChangeInputMode.onNext(AddSaleContract.INPUT_MODE_NOTE)
                } else {
                    v.setBackgroundResource(R.drawable.background_white_corner_radius_4dp)
                }
            }
        }

        binding.addItems.setOnClickListener {
            if (isAddBillItemStarted.not()) {
                isAddBillItemStarted = true
                salesAnalytics.trackEvents(
                    eventName = CASH_SALE_ITEM_ADD_STARTED,
                    screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
                    propertiesMap = PropertiesMap.create()
                        .add(SalesAnalytics.PropertyKey.FIRST_FLOW, getCurrentState().isNewUser)
                        .add(SalesAnalytics.PropertyKey.AMOUNT, getCurrentState().amount / 100)
                )
            }
            if (getCurrentState().isNewUser) {
                salesAnalytics.trackEvents(
                    eventName = CASH_SALE_NEW_ITEM_ADD_STARTED,
                    screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
                    propertiesMap = PropertiesMap.create()
                        .add(SalesAnalytics.PropertyKey.FIRST_FLOW, getCurrentState().isNewUser)
                )
                val dialog = AddBillBottomSheetDialog()
                dialog.setListener(this)
                dialog.show(childFragmentManager, AddBillBottomSheetDialog.TAG)
            } else {
                goToAddBillItemScreen()
            }
        }

        binding.submitSale.setOnClickListener {
            if (isStateInitialized() && getCurrentState().isLoading.not()) {
                var amount = (getCurrentState().amount.toDouble() / 100)
                getCurrentState().billedItems?.let {
                    amount = if (it.total.isNotEmpty()) it.total.toDouble() else 0.0
                }
                val note = binding.note.text.toString()
                if (binding.amountCalculation.text.isNotEmpty()) {
                    tracker.trackEvents(
                        eventName = Event.ADD_CASH_SALE_COMPLETED,
                        screen = PropertyValue.CASH_SALE_TX,
                        propertiesMap = PropertiesMap.create()
                            .add("Amount", amount)
                            .add("items", getCurrentState().billItems.size ?: 0)
                            .add("quantity", getCurrentState().billItems.sumByDouble { it.quantity })
                    )
                    tracker.trackEvents(
                        eventName = Event.ADD_CASH_SALE_ITEM_COMPLETED,
                        screen = PropertyValue.CASH_SALE_TX,
                        propertiesMap = PropertiesMap.create()
                            .add("Method", "Fab")
                            .add("Value", "")
                    )
                    submitSale.onNext(
                        AddSaleViewModel.Sale(
                            amount,
                            note,
                            getCurrentState().saleDate,
                            getCurrentState().billingName,
                            getCurrentState().billingMobile,
                            getCurrentState().billedItems
                        )
                    )
                }
            }
        }

        binding.date.setOnClickListener {
            tracker.trackEvents(eventName = Event.SELECT_CASH_SALE_DATE, screen = PropertyValue.CASH_SALE_TX)
            showDatePicker.onNext(Unit)
            showDatePicker()
        }

        binding.billingName.setOnClickListener {
            showBillingNameSubject.onNext(Unit)
        }

        KeyboardVisibilityEvent.setEventListener(activity) { isOpen ->
            if (isOpen) {
                binding.calculatorView.setBooleanVisibility(false)
            } else {
                binding.calculatorView.postDelayed(
                    {
                        binding.calculatorView.setBooleanVisibility(true)
                    },
                    200
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isStateInitialized() && billedItems.isNullOrEmpty().not()) {
            setBilledItems.onNext(billedItems!!)
        }
    }

    private fun activateCursor() {
        val animation = AlphaAnimation(0.5f, 0f)
        animation.duration = 500
        animation.interpolator = LinearInterpolator()
        animation.repeatCount = Animation.INFINITE
        animation.repeatMode = Animation.REVERSE
        binding.cursor.startAnimation(animation)
    }

    override fun loadIntent(): UserIntent {
        return AddSaleContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            onDigitClicked
                .map {
                    resetTimer()
                    AddSaleContract.Intent.OnDigitClicked(it)
                },
            onOperatorClicked
                .map {
                    resetTimer()
                    AddSaleContract.Intent.OnOperatorClicked(it)
                },

            onEqualsClicked
                .map {
                    resetTimer()
                    AddSaleContract.Intent.OnEqualClicked
                },

            onDotClicked
                .map {
                    resetTimer()
                    AddSaleContract.Intent.OnDotClicked
                },

            onLongBackPress
                .map {
                    resetTimer()
                    AddSaleContract.Intent.OnLongPressBackSpace
                },

            onBackPressClicked
                .map {
                    resetTimer()
                    AddSaleContract.Intent.OnBackSpaceClicked
                },
            onChangeInputMode
                .map {
                    resetTimer()
                    AddSaleContract.Intent.OnChangeInputMode(it)
                },
            submitSale.throttleFirst(600, TimeUnit.MILLISECONDS)
                .map {
                    handEducationTimer.cancel()
                    AddSaleContract.Intent.AddSale(it)
                },
            showDatePicker.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    AddSaleContract.Intent.ShowDatePickerDialog
                },
            onChangeDate
                .map {
                    resetTimer()
                    AddSaleContract.Intent.OnChangeDate(it)
                },
            setBillingDataSubject
                .map {
                    resetTimer()
                    AddSaleContract.Intent.SetBillingDataIntent(it.first, it.second)
                },
            showBillingNameSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    AddSaleContract.Intent.ShowBillingNameDialogIntent
                },
            addBillItem
                .map {
                    AddSaleContract.Intent.AddBillItemIntent(it)
                },
            setBilledItems
                .map {
                    resetTimer()
                    AddSaleContract.Intent.SetBilledItemsIntent(it)
                },
            canShowHandEducationSubject
                .map {
                    AddSaleContract.Intent.ShowHandEducationIntent(it)
                },
            canShowDotHighlightSubject
                .map {
                    AddSaleContract.Intent.ShowDotHighlightIntent(it)
                }

        )
    }

    override fun render(state: AddSaleContract.State) {
        if (state.isBillingAbEnabled) {
            renderForCashSalesWithBill(state)
        } else {
            renderForCashSales(state)
        }
    }

    private fun renderForCashSales(state: AddSaleContract.State) {
        binding.rvBillItems.setBooleanVisibility(false)
        binding.billHeader.setBooleanVisibility(false)
        binding.addBillItemGroup.setBooleanVisibility(false)
        binding.note.setBooleanVisibility(false)
        val amountCalculation = state.amountCalculation?.replace("*", "x")
        if (amountCalculation.isNullOrEmpty() || state.amount == 0L) {
            binding.amount.text = getString(R.string.add_total_amount)
            binding.note.visibility = View.GONE
            binding.submitSale.visibility = View.GONE
            binding.date.visibility = View.INVISIBLE
            binding.billingName.visibility = View.GONE
        } else {
            binding.amount.text = SalesUtil.formatV2(state.amount)
            binding.note.visibility = View.VISIBLE
            binding.submitSale.visibility = View.VISIBLE
            binding.date.visibility = View.VISIBLE
            if (state.canShowBillingName) {
                binding.billingName.visibility = View.VISIBLE
                if (state.billingName.isNullOrEmpty().not()) {
                    binding.billingName.text = state.billingName
                }
            }
            if (amountCalculation.contains("x") || amountCalculation.contains("-") || amountCalculation.contains("+")) {
                binding.amount.visibility = View.VISIBLE
            } else {
                binding.amount.visibility = View.GONE
            }
        }
        binding.amountCalculation.text = amountCalculation
        binding.date.text = DateTimeUtils.getFormat2(context, state.saleDate)

        if (state.inputMode == AddSaleContract.INPUT_MODE_NOTE) {
            noteMode()
        } else {
            calculatorMode()
        }
        if (state.canShowAlert) {
            alert = when {
                state.alert.isNotEmpty() -> view?.snackbar(state.alert, Snackbar.LENGTH_INDEFINITE)
                else -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_INDEFINITE)
            }
            if (alert?.isShown?.not()!!) {
                alert?.show()
            }
        } else {
            alert?.dismiss()
        }
    }

    private fun renderForCashSalesWithBill(state: AddSaleContract.State) {
        controller.setState(state)
        binding.addBillItemGroup.setBooleanVisibility(true)
        binding.note.setBooleanVisibility(false)
        val amountCalculation = state.amountCalculation?.replace("*", "x")
        if (amountCalculation.isNullOrEmpty()) {
            if (state.addBillTotalAb) {
                binding.amount.text = getString(R.string.add_bill_total)
            } else {
                binding.amount.text = getString(R.string.add_sale_amount)
            }
            binding.amount.visibility = View.VISIBLE
            binding.date.visibility = View.INVISIBLE
            binding.billingName.visibility = View.GONE
            binding.rvBillItems.visibility = View.GONE
            binding.billHeader.visibility = View.GONE
            binding.submitSale.isEnabled = false
            binding.submitSale.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.grey400)
            binding.submitSale.visibility = View.VISIBLE
            binding.addItems.visibility = View.VISIBLE
        } else {
            binding.amount.text = SalesUtil.formatV2(state.amount)
            binding.submitSale.isEnabled = true
            binding.submitSale.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.green_primary)
            binding.submitSale.visibility = View.VISIBLE
            binding.addItems.visibility = View.VISIBLE
            binding.date.visibility = View.VISIBLE
            binding.billingName.visibility = View.GONE
            if (amountCalculation.contains("x") || amountCalculation.contains("-") || amountCalculation.contains("+")) {
                binding.amount.visibility = View.VISIBLE
            } else {
                binding.amount.visibility = View.GONE
            }
        }
        binding.amountCalculation.text = amountCalculation
        binding.date.text = DateTimeUtils.getFormat2(context, state.saleDate)

        if (state.canShowAlert) {
            alert = when {
                state.alert.isNotEmpty() -> view?.snackbar(state.alert, Snackbar.LENGTH_INDEFINITE)
                else -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_INDEFINITE)
            }
            if (alert?.isShown?.not()!!) {
                alert?.show()
            }
        } else {
            alert?.dismiss()
        }

        if (state.billItems.isNullOrEmpty() && billedItems.isNullOrEmpty()) {
            binding.cursor.visibility = View.VISIBLE
            activateCursor()
            clearFocusFromNote()
            binding.calculatorView.visibility = View.VISIBLE
            binding.rvBillItems.visibility = View.GONE
            binding.billHeader.visibility = View.GONE
        } else {
            binding.amount.text = requireContext().getString(R.string.bill_total_string)
            binding.amount.visibility = View.VISIBLE
            binding.rvBillItems.visibility = View.VISIBLE
            binding.billHeader.visibility = View.VISIBLE
            binding.calculatorView.visibility = View.GONE
            binding.cursor.animation?.cancel()
            binding.cursor.visibility = View.GONE
        }
        if (state.isNewUser && canShowHandEducation && binding.submitSale.isEnabled) {
            binding.handIcon.visibility = View.VISIBLE
            AnimationUtils.upDownMotion(binding.handIcon)
            binding.lottieCollectionHighlighter.setBooleanVisibility(false)
        } else if (state.billItems.isNotEmpty() && state.isNewUser.not() && canShowDotHighlight && binding.submitSale.isEnabled) {
            binding.lottieCollectionHighlighter.enableMergePathsForKitKatAndAbove(true)
            binding.lottieCollectionHighlighter.setBooleanVisibility(state.billItems.isNotEmpty())
        } else {
            binding.handIcon.visibility = View.GONE
            binding.lottieCollectionHighlighter.setBooleanVisibility(false)
        }
        if (state.isNewUser) {
            AnimationUtils.pendulumMotion(binding.addImg)
        }
    }

    private fun noteMode() {
        binding.cursor.animation?.cancel()
        binding.cursor.visibility = View.GONE
        binding.amountCalculation.clearFocus()
        binding.note.requestFocus()
    }

    private fun calculatorMode() {
        hideSoftKeyboard()
        activateCursor()
        binding.cursor.visibility = View.VISIBLE
        clearFocusFromNote()
    }

    override fun onPause() {
        binding.handIcon.clearAnimation()
        binding.addImg.clearAnimation()
        super.onPause()
    }

    private fun clearFocusFromNote() {
        binding.note.setBackgroundResource(R.drawable.background_white_corner_radius_4dp)
        binding.note.clearFocus()
        hideSoftKeyboard()
    }

    override fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    override fun goBack() {
        activity?.runOnUiThread {
            hideSoftKeyboard()
            findNavController(this).popBackStack()
        }
    }

    override fun showDatePicker() {
        activity?.runOnUiThread {
            if (datePickerDialog == null) {
                val today = CommonUtils.currentDateTime()

                datePickerDialog = DatePickerDialog(
                    requireContext(),
                    this,
                    today.year,
                    today.monthOfYear.minus(1),
                    today.dayOfMonth
                )
                if (datePickerDialog?.datePicker != null) {
                    datePickerDialog?.datePicker?.maxDate = CommonUtils.currentDateTime().millis
                }

                datePickerDialog?.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.ok), datePickerDialog)
                datePickerDialog?.setButton(
                    DatePickerDialog.BUTTON_NEGATIVE,
                    getString(R.string.cancel),
                    datePickerDialog
                )
                datePickerDialog?.setOnCancelListener {
                    it.dismiss()
                }
            }
            datePickerDialog!!.show()
        }
    }

    override fun showBillingNameDialog() {
        activity?.runOnUiThread {
            if (billingNameDialog == null) {
                billingNameDialog = BillingNameBottomSheetDialog()
                billingNameDialog?.setListener(this)
            }
            billingNameDialog?.show(childFragmentManager, BillingNameBottomSheetDialog.TAG)
        }
    }

    override fun goToAddBillItemScreen() {
        activity?.runOnUiThread {
            if (findNavController(this).currentDestination?.id == R.id.addSaleScreen) {
                findNavController(this).navigate(
                    R.id.action_addSaleScreen_to_addBillItemsScreen,
                    bundleOf("bill_items" to BillModel.BillItems(getCurrentState().billItems))
                )
            }
        }
    }

    override fun onAddSaleSuccessful(saleId: String) {
        activity?.runOnUiThread {
            findNavController(this).previousBackStackEntry?.savedStateHandle?.set(ADDED_SALE, true)
            if (getCurrentState().isBillingAbEnabled) {
                if (findNavController(this).currentDestination?.id == R.id.addSaleScreen) {
                    findNavController(this).navigate(
                        R.id.action_addSaleScreen_to_billSummaryScreen,
                        bundleOf("sale_id" to saleId)
                    )
                }
            } else {
                goBack()
            }
        }
    }

    override fun onDigitClicked(d: Int) {
        if (!saleAmountEntered) {
            saleAmountEntered = true
            tracker.trackEvents(eventName = Event.CASH_SALE_AMOUNT_ENTERED, screen = PropertyValue.CASH_SALE_TX)
        }
        onDigitClicked.onNext(d)
    }

    override fun onOperatorClicked(d: String) {
        onOperatorClicked.onNext(d)
    }

    override fun onDotClicked() {
        onDotClicked.onNext(Unit)
    }

    override fun onEqualsClicked() {
        onEqualsClicked.onNext(Unit)
    }

    override fun onBackspaceClicked() {
        onBackPressClicked.onNext(Unit)
    }

    override fun onBackspaceLongPress() {
        onLongBackPress.onNext(Unit)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        val newDate = DateTime(calendar.timeInMillis)
        val defaultDate = DateTimeUtils.currentDateTime()
        if (defaultDate.dayOfMonth == newDate.dayOfMonth && defaultDate.monthOfYear == newDate.monthOfYear && defaultDate.year == newDate.year) {
            tracker.trackEvents(
                eventName = Event.UPDATE_CASH_SALE_DATE,
                screen = PropertyValue.CASH_SALE_TX,
                propertiesMap = PropertiesMap.create()
                    .add("Value", DateTimeUtils.formatDateOnly(newDate))
                    .add("Default", true)
            )
        } else {
            tracker.trackEvents(
                eventName = Event.UPDATE_CASH_SALE_DATE,
                screen = PropertyValue.CASH_SALE_TX,
                propertiesMap = PropertiesMap.create()
                    .add("Value", DateTimeUtils.formatDateOnly(newDate))
                    .add("Default", false)
            )
        }

        onChangeDate.onNext(newDate)
        datePickerDialog?.dismiss()
    }

    override fun onSubmit(name: String, mobile: String) {
        setBillingDataSubject.onNext(Pair(name, mobile))
    }

    override fun onSubmitAddBillItem(billItem: BillModel.BillItem) {
        addBillItem.onNext(billItem)
    }

    override fun onSubmitUpdateBillItem(billItem: BillModel.BillItem) {
        addBillItem.onNext(billItem)
    }

    override fun onDestroy() {
        handEducationTimer.cancel()
        activity?.let { ViewModelProvider(it).get(BillSharedViewModel::class.java) }?.setBilledItems(listOf())
        super.onDestroy()
    }
}
