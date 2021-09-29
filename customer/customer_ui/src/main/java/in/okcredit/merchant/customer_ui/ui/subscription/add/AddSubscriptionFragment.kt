package `in`.okcredit.merchant.customer_ui.ui.subscription.add

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.data.server.model.request.DayOfWeek
import `in`.okcredit.merchant.customer_ui.data.server.model.request.toFormattedString
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency.*
import `in`.okcredit.merchant.customer_ui.databinding.AddSubscriptionScreenBinding
import `in`.okcredit.merchant.customer_ui.ui.subscription.SubscriptionEventTracker
import `in`.okcredit.merchant.customer_ui.ui.subscription.add.AddSubscriptionContract.*
import `in`.okcredit.merchant.customer_ui.ui.subscription.add.frequency.AddSubscriptionFrequencyBottomSheet
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.calculator.CalculatorContract
import `in`.okcredit.shared.dialogs.bottomsheetloader.BottomSheetLoaderScreen
import `in`.okcredit.shared.utils.addTo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.transition.TransitionManager
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import com.bumptech.glide.util.Preconditions.checkArgument
import com.google.common.base.Strings
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddSubscriptionFragment :
    BaseFragment<State, ViewEvent, Intent>(
        label = "AddSubscriptionScreen",
        contentLayoutId = R.layout.add_subscription_screen
    ),
    CalculatorContract.Callback,
    AddSubscriptionNameBottomSheet.NameSubmitListener,
    AddSubscriptionFrequencyBottomSheet.FrequencySubmitListener {

    private val binding: AddSubscriptionScreenBinding by viewLifecycleScoped(AddSubscriptionScreenBinding::bind)
    private lateinit var savedStateHandle: SavedStateHandle

    private var calculatorHeight = 0

    @Inject
    lateinit var subscriptionEventTracker: Lazy<SubscriptionEventTracker>

    private var bottomSheetLoaderScreen: BottomSheetLoaderScreen? = null

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun onResume() {
        super.onResume()
        hideSoftKeyboard(binding.etAmount)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedStateHandle = findNavController(this@AddSubscriptionFragment).previousBackStackEntry!!.savedStateHandle
        savedStateHandle.set(AddSubscriptionContract.SUBSCRIPTION_ADDED, false)

        binding.apply {
            calculatorLayout.afterMeasured {
                calculatorHeight = this.measuredHeight
            }
            toolbar.setNavigationOnClickListener { findNavController(this@AddSubscriptionFragment).popBackStack() }
            addNameGroup.setGroupOnClickListener { pushIntent(Intent.AddNameClicked) }
            frequencyGroup.setGroupOnClickListener { pushIntent(Intent.AddFrequencyClicked) }

            buttonSave.setOnClickListener { button ->
                pushIntent(Intent.SubmitClicked)
                button.disable()
                initAndShowLoaderScreen()
            }

            addNameGroup.invisible()
            frequencyGroup.invisible()
            addDateGroup.invisible()
        }
        initAmountFormatter()
    }

    private fun initAndShowLoaderScreen() {
        if (bottomSheetLoaderScreen == null) bottomSheetLoaderScreen =
            BottomSheetLoaderScreen.getInstance(getString(R.string.saving_subscription))
        bottomSheetLoaderScreen?.show(childFragmentManager, BottomSheetLoaderScreen::class.java.simpleName)
    }

    private fun initAmountFormatter() {
        binding.etAmount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (binding.calculatorLayout.translationY > 0) {
                    binding.viewButtonDivider.animate().translationY(0f)
                    binding.buttonSave.animate().translationY(0f)
                    binding.calculatorLayout.animate().translationY(0f)
                }
                binding.viewAmountBorder.setBackgroundColor(getColorCompat(R.color.tx_credit))
            } else {
                if (binding.calculatorLayout.translationY == 0f) {
                    binding.viewButtonDivider.animate().translationY(calculatorHeight.toFloat())
                    binding.buttonSave.animate().translationY(calculatorHeight.toFloat())
                    binding.calculatorLayout.animate().translationY(calculatorHeight.toFloat())
                }
                binding.viewAmountBorder.setBackgroundColor(getColorCompat(R.color.grey300))
            }
        }

        binding.etAmount.showSoftInputOnFocus = false
        binding.calculatorLayout.setData(this, 0, "")

        binding.etAmount.requestFocus()
    }

    override fun onBackPressed(): Boolean {
        if (binding.etAmount.hasFocus()) {
            binding.etAmount.clearFocus()
            return true
        }
        return super.onBackPressed()
    }

    private fun hideErrorAfterDelay() {
        Completable
            .timer(1500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.errorAmount.gone() }
            .addTo(autoDisposable)
    }

    override fun render(state: State) {
        checkForCustomerData(state)
        checkForAmountAdded(state)
        checkForNameAdded(state)
        checkForFrequencyAdded(state)
        checkForButtonState(state)
    }

    private fun checkForCustomerData(state: State) {
        if (state.customer == null) return

        binding.profileName.text = state.customer.description

        CurrencyUtil.renderV2(state.customer.balanceV2, binding.due, 0)

        val defaultPic = TextDrawable
            .builder()
            .buildRound(
                state.customer.description.substring(0, 1).toUpperCase(Locale.getDefault()),
                ColorGenerator.MATERIAL.getColor(state.customer.description)
            )

        if (Strings.isNullOrEmpty(state.customer.profileImage)) {
            binding.profileImage.setImageDrawable(defaultPic)
        } else {
            Glide.with(binding.profileImage)
                .load(state.customer.profileImage)
                .placeholder(defaultPic)
                .into(binding.profileImage)
        }
    }

    private fun checkForButtonState(state: State) {
        if (state.shouldShowSaveButton()) {
            binding.viewButtonDivider.visible()
            binding.buttonSave.visible()
        } else {
            binding.viewButtonDivider.gone()
            binding.buttonSave.gone()
        }
    }

    private fun checkForAmountAdded(state: State) {
        if (state.amountAdded()) {
            binding.addNameGroup.visible()
            binding.frequencyGroup.visible()
        } else {
            binding.addNameGroup.invisible()
            binding.frequencyGroup.invisible()
        }

        state.amount?.let {
            val amount = state.amountCalculation?.replace("*", "x") ?: ""
            binding.etAmount.setText(amount)
            binding.etAmount.setSelection(amount.length)
            CurrencyUtil.renderAsSubtitle(binding.textAmount, it)
        }

        state.amountCalculation?.let {
            if (state.amountCalculation.contains("*") ||
                state.amountCalculation.contains("x") ||
                state.amountCalculation.contains("+") ||
                state.amountCalculation.contains("-") ||
                state.amountCalculation.contains("/")
            ) {
                binding.textAmount.visibility = View.VISIBLE
            } else {
                binding.textAmount.visibility = View.GONE
            }
        }
    }

    private fun checkForFrequencyAdded(state: State) {
        if (state.selectedFrequency == null) return

        when (state.selectedFrequency) {
            DAILY -> {
                binding.tvFrequencyLabel.text = getString(R.string.repeat)
                binding.tvFrequency.text = getString(R.string.daily)
            }
            WEEKLY -> {
                binding.tvFrequencyLabel.text = getString(R.string.repeat_weekly)
                binding.tvFrequency.text = (state.daysInWeek)?.toFormattedString() ?: ""
            }
            MONTHLY -> {
                binding.tvFrequencyLabel.text = getString(R.string.repeat_monthly)
                if (state.startDate != null) {
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = state.startDate
                    }
                    binding.tvFrequency.text =
                        getString(
                            R.string.on_day_of_month,
                            calendar.get(Calendar.DAY_OF_MONTH),
                            getDayOfMonthSuffix(calendar.get(Calendar.DAY_OF_MONTH))
                        )
                }
            }
        }
    }

    private fun getDayOfMonthSuffix(dayOfMonth: Int): String? {
        checkArgument(dayOfMonth in 1..31, "illegal day of month: $dayOfMonth")
        return if (dayOfMonth in 11..13) {
            "th"
        } else when (dayOfMonth % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }

    private fun checkForNameAdded(state: State) {
        if (state.nameAdded()) {
            if (state.amountAdded()) {
                binding.tvAddNameLabel.visible()
            } else {
                binding.tvAddNameLabel.invisible()
            }
            binding.tvAddName.text = state.name
        } else {
            binding.tvAddNameLabel.gone()
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoToAddName -> getToAddName(event.name)
            is ViewEvent.GoToAddFrequency -> goToAddFrequency(
                event.selectedFrequency,
                event.daysInWeek,
                event.startDate
            )
            is ViewEvent.ShowError -> {
                bottomSheetLoaderScreen?.dismissAllowingStateLoss()
                bottomSheetLoaderScreen = null
                longToast(event.error)
            }
            is ViewEvent.Success -> goToSubscriptionSuccess()
        }
    }

    private fun goToSubscriptionSuccess() {
        trackSuccessEvent()
        bottomSheetLoaderScreen?.dismissAllowingStateLoss()
        bottomSheetLoaderScreen = null
        val navController = findNavController(this)
        val currentBackStackEntry = navController.currentBackStackEntry
        currentBackStackEntry?.let { backStackEntry ->
            val currentHandle = backStackEntry.savedStateHandle
            currentHandle.getLiveData<Boolean>(SubscriptionSuccessScreen.ANIMATION_FINISHED)
                .observe(
                    backStackEntry,
                    { finished: Boolean? ->
                        if (finished == true) {
                            savedStateHandle.set(AddSubscriptionContract.SUBSCRIPTION_ADDED, true)
                            findNavController(this).popBackStack()
                        }
                    }
                )
        }
        val action = AddSubscriptionFragmentDirections.actionSubscriptionSuccess()
        navController.navigate(action)
    }

    private fun trackSuccessEvent() {
        val monthValue = if (getCurrentState().startDate != null) {
            Calendar.getInstance().apply {
                timeInMillis = getCurrentState().startDate!!
            }.get(Calendar.DAY_OF_MONTH).toString()
        } else {
            ""
        }
        subscriptionEventTracker.get().trackSubscriptionConfirm(
            name = getCurrentState().name ?: "",
            cycle = getCurrentState().selectedFrequency?.analyticsName ?: "",
            weekValue = getCurrentState().daysInWeek?.toFormattedString() ?: "",
            monthValue = monthValue,
            accountId = getCurrentState().customer?.id ?: "",
            mobile = getCurrentState().customer?.mobile
        )
    }

    private fun goToAddFrequency(
        selectedFrequency: SubscriptionFrequency?,
        daysInWeek: List<DayOfWeek>?,
        startDate: Long?
    ) {
        binding.etAmount.clearFocus()
        subscriptionEventTracker.get().trackSubscriptionIntervalStarted(
            getCurrentState().customer?.id ?: "",
            getCurrentState().customer?.mobile
        )
        AddSubscriptionFrequencyBottomSheet.getInstance(selectedFrequency, daysInWeek, startDate).apply {
            setListener(this@AddSubscriptionFragment)
        }.show(childFragmentManager, AddSubscriptionFrequencyBottomSheet::class.java.simpleName)
    }

    private fun getToAddName(name: String?) {
        binding.etAmount.clearFocus()
        subscriptionEventTracker.get().trackSubscriptionNameStarted(
            getCurrentState().customer?.id ?: "",
            getCurrentState().customer?.mobile
        )
        AddSubscriptionNameBottomSheet.getInstance(name).apply {
            setListener(this@AddSubscriptionFragment)
        }.show(childFragmentManager, AddSubscriptionNameBottomSheet::class.java.simpleName)
    }

    override fun callbackData(amountCalculation: String?, amount: Long, calculatorOperatorsUsed: String?) {
        subscriptionEventTracker.get().trackSubscriptionAmountEntered(
            getCurrentState().customer?.id ?: "",
            getCurrentState().customer?.mobile
        )
        pushIntent(Intent.CalculatorData(amountCalculation, amount))
    }

    override fun isInvalidAmount() {
        if (binding.errorAmount.visibility == View.VISIBLE) return
        binding.errorAmount.visible()
        TransitionManager.beginDelayedTransition(binding.textInputAmount)
        AnimationUtils.shake(binding.textInputAmount)
        hideErrorAfterDelay()
    }

    override fun onTextSubmitted(name: String) {
        subscriptionEventTracker.get().trackSubscriptionNameCompleted(
            name = name,
            accountId = getCurrentState().customer?.id ?: "",
            mobile = getCurrentState().customer?.mobile
        )
        pushIntentWithDelay(Intent.NameAdded(name))
    }

    override fun frequencySelected(
        selectedFrequency: SubscriptionFrequency,
        daysInWeek: List<DayOfWeek>?,
        startDate: Long?
    ) {
        val monthValue = if (startDate != null) {
            Calendar.getInstance().apply {
                timeInMillis = startDate
            }.get(Calendar.DAY_OF_MONTH).toString()
        } else {
            ""
        }
        subscriptionEventTracker.get().trackSubscriptionIntervalCompleted(
            cycle = selectedFrequency.analyticsName,
            weekValue = daysInWeek?.toFormattedString() ?: "",
            monthValue = monthValue,
            accountId = getCurrentState().customer?.id ?: "",
            mobile = getCurrentState().customer?.mobile
        )
        pushIntentWithDelay(Intent.FrequencyAdded(selectedFrequency, daysInWeek, startDate))
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }
}
