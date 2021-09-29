package `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import `in`.okcredit.merchant.customer_ui.databinding.AddNumberDialogLayoutBinding
import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog.AddNumberDialogContract.*
import `in`.okcredit.merchant.customer_ui.ui.dialogs.CyclicAccountDialog
import `in`.okcredit.merchant.customer_ui.ui.dialogs.CyclicAccountDialog.Companion.showSupplierConflict
import `in`.okcredit.merchant.customer_ui.utils.CustomerTraces
import `in`.okcredit.merchant.customer_ui.utils.CustomerUtils
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.perf.metrics.AddTrace
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.utils.MobileUtils
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import timber.log.Timber
import javax.inject.Inject

class AddNumberDialogScreen : BaseBottomSheetWithViewEvents<State, ViewEvents, Intent>("AddNumberDialogScreen") {

    private val binding: AddNumberDialogLayoutBinding by viewLifecycleScoped(AddNumberDialogLayoutBinding::bind)

    private val enteredNumberPublishSubject: PublishSubject<String> = PublishSubject.create()
    private val submitNumberPublishSubject: PublishSubject<String> = PublishSubject.create()
    private val editTextFocusChangePublishSubject: PublishSubject<Boolean> = PublishSubject.create()

    private var isSelectProfileEventSent = false
    private var isMobileNumberSetOnce = false
    private val SPACE = " "

    @Inject
    internal lateinit var tracker: Lazy<CustomerEventTracker>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    interface Listener {
        fun onSkip()
        fun onDone()
    }

    private var listener: Listener? = null

    companion object {
        const val TAG = "AddNumberDialogScreen"
        const val EXTRA_CUSTOMER_ID = ""
        const val ADD_MOBILE_RESULT = "add_mobile_result"

        fun newInstance(
            customerId: String,
            description: String,
            mobile: String? = null,
            isSkipAndSend: Boolean = false,
            isSupplier: Boolean = false,
            screen: String = CustomerEventTracker.RELATIONSHIP_REMINDER
        ): AddNumberDialogScreen {

            return AddNumberDialogScreen().apply {
                arguments = Bundle().apply {
                    putString("customer_id", customerId)
                    putString("description", description)
                    putBoolean("is_skip_and_send", isSkipAndSend)
                    putBoolean("is_supplier", isSupplier)
                    putString("screen", screen)
                    mobile?.let {
                        putString("mobile", mobile)
                    }
                }
            }
        }
    }

    @AddTrace(name = CustomerTraces.RENDER_ADD_NUMBER_POPUP)
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogThemeWithKeyboard)
    }

    override fun onStart() {
        super.onStart()

        handleOutsideClick()
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    private fun handleOutsideClick() {
        val outsideView = dialog?.window?.decorView?.findViewById<View>(com.google.android.material.R.id.touch_outside)
        outsideView?.setOnClickListener {
            if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
                KeyboardVisibilityEvent.hideKeyboard(context, view)
            } else {
                dismiss()
            }
        }
    }

    @AddTrace(name = CustomerTraces.RENDER_ADD_NUMBER_POPUP)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AddNumberDialogLayoutBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            validateDetails.setOnClickListener {
                val state = getCurrentState()
                val enteredNumber = state.enteredMobileNumber
                val parseMobile = MobileUtils.parseMobile(enteredNumber)
                if (CustomerUtils.isValidMobileNumber(parseMobile)) {
                    errorMessage.gone()
                    submitNumberPublishSubject.onNext(parseMobile)
                } else {
                    var reason = CustomerEventTracker.INVALID
                    if (enteredNumber.length > 10) {
                        reason = CustomerEventTracker.MORE_DIGITS
                    }
                    trackFailureEvent(reason)
                    errorMessage.text = getString(R.string.invalid_mobile)
                    errorMessage.visible()
                    updateUIForError()
                }
            }

            clearPhoneNumber.setOnClickListener {
                etPhoneNumber.clear()
            }

            etPhoneNumber.afterTextChange { text ->
                if (isSelectProfileEventSent.not() && text.isNotBlank()) {
                    isSelectProfileEventSent = true
                    if (isStateInitialized()) {
                        val state = getCurrentState()
                        tracker.get().trackSelectProfile(
                            screen = state.screen,
                            relation = PropertyValue.CUSTOMER,
                            field = CustomerEventTracker.MOBILE,
                            accountId = state.customerId
                        )
                    }
                }
                enteredNumberPublishSubject.onNext(text)
            }

            etPhoneNumber.setOnFocusChangeListener { _, hasFocus ->
                editTextFocusChangePublishSubject.onNext(hasFocus)
            }

            skipAndSend.setOnClickListener {
                val state = getCurrentState()
                tracker.get().trackSkipSelectProfile(
                    screen = state.screen,
                    relation = PropertyValue.CUSTOMER,
                    field = CustomerEventTracker.MOBILE,
                    accountId = state.customerId
                )
                listener?.onSkip()
                dismiss()
            }
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val behavior: BottomSheetBehavior<*> = showBottomSheetFullyExpanded()

                disableDraggingInBottomSheet(behavior)
            }
        })
    }

    internal fun showBottomSheetFullyExpanded(): BottomSheetBehavior<*> {
        val dialog = dialog as BottomSheetDialog?
        val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.peekHeight = 0
        return behavior
    }

    internal fun disableDraggingInBottomSheet(behavior: BottomSheetBehavior<*>) {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {}

            override fun onStateChanged(view: View, state: Int) {
                if (state == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        })
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            enteredNumberPublishSubject
                .map {
                    Intent.EnteredMobileNumber(it)
                },

            submitNumberPublishSubject
                .map {
                    Intent.SubmitMobileNumber(it)
                },

            editTextFocusChangePublishSubject
                .map {
                    Intent.SetEditTextFocus(it)
                }
        )
    }

    override fun render(state: State) {

        binding.apply {

            if (state.enteredMobileNumber.isNotBlank()) clearPhoneNumber.visible() else clearPhoneNumber.gone()

            if (state.hasFocus) {
                updateUIForFocus()
            } else {
                updateUIForNonFocus()
            }

            if (state.showLoader) {
                progressBar.visible()
                validateDetails.gone()
                etPhoneNumber.disable()
                clearPhoneNumber.isClickable = false
            } else {
                progressBar.gone()
                validateDetails.visible()
                etPhoneNumber.enable()
                clearPhoneNumber.isClickable = true
            }

            if (state.description.isNotBlank()) {
                tvAddMobileDescription.text = state.description
            }

            if (!state.mobile.isNullOrBlank() && isMobileNumberSetOnce.not()) {
                isMobileNumberSetOnce = true
                etPhoneNumber.setText(state.mobile)
                etPhoneNumber.setSelection(state.mobile.length)
            }

            when {
                state.enteredMobileNumber.length == 10 -> {
                    updateUIForFocus()
                    errorMessage.gone()
                }
                state.enteredMobileNumber.length > 10 -> {
                    val builder = StringBuilder(getString(R.string.invalid_phone))
                        .append(SPACE).append(String.format("%d/10", state.enteredMobileNumber.length))
                    errorMessage.text = builder
                    updateUIForError()
                    errorMessage.visible()
                }
                else -> {
                    updateUIForNonFocus()
                    errorMessage.gone()
                }
            }
            showSkipAndSend(state.isSkipAndSend)
            showPopupErrors(state)
            showErrorMessages(state)
        }
    }

    private fun showSkipAndSend(isSkipAndSend: Boolean) {
        binding.skipAndSend.visibility = View.GONE
        if (isSkipAndSend.not()) return
        val content = requireContext().getString(R.string.skip_and_send)
        val spannable = SpannableStringBuilder(content)
        spannable.setSpan(
            UnderlineSpan(),
            0,
            content.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.indigo_primary)),
            0,
            content.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.skipAndSend.text = spannable
        binding.skipAndSend.visibility = View.VISIBLE
    }

    private fun showPopupErrors(state: State) {
        if (state.errorPopup.not()) return

        binding.apply {
            when (state.errorType) {
                AddNumberErrorType.ActiveCyclicAccount -> {
                    if (state.errorSupplier == null) return

                    trackFailureEvent(CustomerEventTracker.SUPPLIER_CONFLICT)

                    showSupplierConflict(
                        requireActivity(), state.errorSupplier,
                        object : CyclicAccountDialog.Listener {
                            override fun onViewClicked() {
                                legacyNavigator.get().startingSupplierIntent(
                                    requireContext(),
                                    state.errorSupplier.id
                                )
                                dismiss()
                            }
                        }
                    )
                }
                AddNumberErrorType.DeletedCyclicAccount -> {
                    if (state.errorSupplier == null) return

                    trackFailureEvent(CustomerEventTracker.SUPPLIER_DELETED)

                    showSupplierConflict(
                        requireActivity(), state.errorSupplier,
                        object : CyclicAccountDialog.Listener {
                            override fun onViewClicked() {
                                legacyNavigator.get().startingSupplierScreenForReactivation(
                                    requireContext(),
                                    state.errorSupplier.id,
                                    null
                                )
                                dismiss()
                            }
                        }
                    )
                }
            }
        }
    }

    private fun showErrorMessages(state: State) {
        if (state.error.not()) return

        binding.apply {
            when (state.errorType) {
                AddNumberErrorType.MobileConflict -> {
                    errorMessage.text = getString(R.string.err_mobile_conflict, state.errorCustomer?.description)
                    errorMessage.visible()
                    updateUIForError()
                    trackFailureEvent(CustomerEventTracker.CUSTOMER_CONFLICT)
                }
                AddNumberErrorType.InternetIssue -> {
                    errorMessage.text = getString(R.string.no_internet_msg)
                    errorMessage.visible()
                    updateUIForError()
                    trackFailureEvent(CustomerEventTracker.NO_INTERNET)
                }
                else -> {
                    errorMessage.text = getString(R.string.err_default)
                    errorMessage.visible()
                    updateUIForError()
                    trackFailureEvent(CustomerEventTracker.SOME_ERROR)
                }
            }
        }
    }

    private fun trackFailureEvent(reason: String) {
        val state = getCurrentState()
        tracker.get().trackUpdateProfileFailed(
            screen = state.screen,
            relation = PropertyValue.CUSTOMER,
            field = CustomerEventTracker.MOBILE,
            accountId = state.customerId,
            reason = reason
        )
    }

    private fun AddNumberDialogLayoutBinding.updateUIForNonFocus() {
        tvPhoneNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey600))
        rlPhoneNumber.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.circular_corners_unselected_background)
        validateDetails.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.circle_grey400_background)
    }

    private fun AddNumberDialogLayoutBinding.updateUIForFocus() {
        tvPhoneNumber.setTextColor(requireContext().getColorFromAttr(R.attr.colorPrimary))
        rlPhoneNumber.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.circular_corners_selected_background)
        validateDetails.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.circle_background_dark_green_no_theme)
    }

    private fun AddNumberDialogLayoutBinding.updateUIForError() {
        tvPhoneNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_primary))
        rlPhoneNumber.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.circular_corners_selected_red_stroke)
        validateDetails.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.circle_grey400_background)
    }

    private fun changeGroupVisibility(group: Group, visibility: Int) {
        group.visibility = visibility
        group.requestLayout()
    }

    private fun goToLogin() {
        activity?.runOnUiThread {
            legacyNavigator.get().goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    private fun remind() {
        try {
            val navBackStackEntry = findNavController(this).previousBackStackEntry
            val isBackStackCustomerScreen =
                navBackStackEntry?.destination?.displayName?.contains("customerScreenv2") ?: false
            if (isBackStackCustomerScreen) {
                navBackStackEntry?.savedStateHandle?.set(ADD_MOBILE_RESULT, true)
            }
        } catch (e: IllegalStateException) {
            Timber.e(e, "NavController does'nt exist for this flow")
        }
    }

    private fun onAccountAddedSuccessfully() {
        activity?.runOnUiThread {
            val state = getCurrentState()
            tracker.get().trackUpdateProfile(
                screen = state.screen,
                relation = PropertyValue.CUSTOMER,
                field = CustomerEventTracker.MOBILE,
                accountId = state.customerId
            )
            hideSoftKeyboard()
            changeGroupVisibility(binding.inputGroup, View.GONE)
            changeGroupVisibility(binding.successGroup, View.VISIBLE)
            binding.progressBar.gone()
            viewLifecycleOwner.lifecycleScope.launch {
                delay(2000)
                if (!isStateSaved) {
                    if (state.isSkipAndSend) {
                        listener?.onDone()
                    }
                    dismiss()
                }
            }
        }
    }

    override fun handleViewEvent(event: ViewEvents) {
        when (event) {
            is ViewEvents.GoToLogin -> goToLogin()
            is ViewEvents.OnAccountAddedSuccessfully -> onAccountAddedSuccessfully()
        }
    }
}
