package `in`.okcredit.merchant.customer_ui.ui.customer

import `in`.okcredit.analytics.*
import `in`.okcredit.backend._offline.model.DueInfo
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.backend.utils.StringUtils
import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection.contract.rxPreference.CollectionRxPreference.IS_ONLINE_COLLECTION_EDUCATION_DENIED
import `in`.okcredit.collection.contract.rxPreference.CollectionRxPreference.IS_ONLINE_COLLECTION_EDUCATION_SHOWN
import `in`.okcredit.communication_inappnotification.contract.LocalInAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.ui.local.TapTargetLocal
import `in`.okcredit.communication_inappnotification.contract.ui.local.TooltipLocal
import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.home.HomeNavigator
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.*
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker.Companion.RELATION_CUSTOMER
import `in`.okcredit.merchant.customer_ui.databinding.CustomerFragmentBinding
import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog.AddNumberDialogScreen
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTransactionFragment.Companion.KEY_TRANSACTION_ID
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity
import `in`.okcredit.merchant.customer_ui.ui.customer.bottom_sheet.googlePay.CollectWithGooglePayBottomSheet
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.MenuOptionsBottomSheet
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.MenuOptionsBottomSheet.Companion.MenuOptions
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.analytics.MenuOptionEventTracker
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.model.MenuSheet
import `in`.okcredit.merchant.customer_ui.ui.customer.views.DeleteTransactionViewModel_
import `in`.okcredit.merchant.customer_ui.ui.customer.views.TransactionViewModel_
import `in`.okcredit.merchant.customer_ui.ui.dialogs.AutoDueDateDialog
import `in`.okcredit.merchant.customer_ui.ui.dialogs.BlockRelationShipDialogFragment
import `in`.okcredit.merchant.customer_ui.ui.dialogs.BlockedDialogFragment
import `in`.okcredit.merchant.customer_ui.ui.dialogs.PaymentPendingDialog
import `in`.okcredit.merchant.customer_ui.ui.payment.AddCustomerPaymentActivity
import `in`.okcredit.merchant.customer_ui.ui.subscription.SubscriptionActivity
import `in`.okcredit.merchant.customer_ui.ui.subscription.SubscriptionEventTracker
import `in`.okcredit.merchant.customer_ui.usecase.GetCollectionNudgeOnDueDateCrossed
import `in`.okcredit.merchant.customer_ui.usecase.GetCustomerScreenSortSelection
import `in`.okcredit.merchant.customer_ui.usecase.GetCustomerScreenSortSelection.CustomerScreenSortSelection.BILL_DATE
import `in`.okcredit.merchant.customer_ui.utils.calender.MonthView
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import `in`.okcredit.payment.contract.AddPaymentDestinationListener
import `in`.okcredit.payment.contract.BlindPayListener
import `in`.okcredit.payment.contract.EditDestinationListener
import `in`.okcredit.payment.contract.PaymentNavigator
import `in`.okcredit.payment.contract.PaymentType
import `in`.okcredit.referral.contract.RewardsOnSignupTracker
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.ScreenName
import `in`.okcredit.shared.utils.addTo
import `in`.okcredit.web.WebExperiment
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.send_or_receive_payment_layout_ab.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.withContext
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.analytics.AccountingEventTracker.Companion.SOURCE_LEDGER
import merchant.okcredit.accounting.contract.model.LedgerType
import merchant.okcredit.accounting.model.Transaction
import merchant.okcredit.accounting.ui.customer_support_exit_dialog.CustomerSupportExitDialog
import merchant.okcredit.accounting.ui.customer_support_option_dialog.CustomerSupportOptionDialog
import merchant.okcredit.accounting.ui.transaction_sort_options_dialog.TransactionsSortCriteriaSelectionBottomSheet
import merchant.okcredit.accounting.ui.transaction_sort_options_dialog.TransactionsSortCriteriaSelectionBottomSheet.TransactionsSortCriteriaSelectionListener
import merchant.okcredit.accounting.utils.AccountingSharedUtils
import merchant.okcredit.supplier.contract.SupplierNavigator
import merchant.okcredit.supplier.contract.SupplierPaymentListener
import org.joda.time.DateTime
import tech.okcredit.account_chat_contract.ChatNavigator
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.CalenderUtils
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.keyboardUtils.Unregistrar
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.bills.BillsNavigator
import tech.okcredit.help.HelpActivity
import tech.okcredit.userSupport.ContextualHelp
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.lang.ref.WeakReference
import java.net.InetAddress
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.absoluteValue

class CustomerFragment :
    BaseFragment<CustomerContract.State, CustomerContract.ViewEvent, CustomerContract.Intent>(
        label = "CustomerScreen",
        contentLayoutId = R.layout.customer_fragment
    ),
    ReminderBottomSheetDialog.ReminderMode,
    VoiceInputBottomSheetFragment.VoiceInputListener,
    BlockedDialogFragment.BlockedListener,
    CustomerControllerV2.CustomerControllerListener,
    MenuOptionsBottomSheet.MenuListener,
    AddPaymentDestinationListener,
    EditDestinationListener,
    BlindPayListener,
    TransactionsSortCriteriaSelectionListener {

    companion object {
        const val REQUEST_CODE_WHATSAPP = 3
        const val REQUEST_CODE_ADD_TRANSACTION = 1719
        private const val CALENDER_READ_AND_WRITE_PERMISSION = 1
        const val ARG_SCREEN_REDIRECT_TO_PAYMENT = "redirect_to_payment"
    }

    private var qrSource: String? = null
    private var voiceBottomSheet: VoiceInputBottomSheetFragment? = null

    private var isPayOnlineEducationShownOnce: Boolean = false
    internal var dueDatePicker: DueDatePickerDialog? = null
    private val compositeDisposable = CompositeDisposable()

    private var alert: Snackbar? = null
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var isPlayedSound: Boolean = false
    private var scrolledToDeletedTransaction = false
    private var shouldScrollToAddedTransaction = false
    private var keyboardVisibilityEventRegister: Unregistrar? = null
    internal var checkForAutoDueDate = true
    private var isRedirectedToPayment = false
    private val validPromptStates =
        arrayListOf(MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED, MaterialTapTargetPrompt.STATE_FOCAL_PRESSED)

    @Inject
    lateinit var collectionNavigator: Lazy<CollectionNavigator>

    @Inject
    lateinit var homeNavigator: Lazy<HomeNavigator>

    @Inject
    lateinit var referralRewardsController: Lazy<ReferralRewardsController>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var imageLoader: Lazy<IImageLoader>

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    @Inject
    lateinit var eventTracker: Lazy<CustomerEventTracker>

    @Inject
    lateinit var rxSharedPreference: Lazy<DefaultPreferences>

    @Inject
    lateinit var menuOptionEventTracker: Lazy<MenuOptionEventTracker>

    @Inject
    lateinit var onboardingPreferences: Lazy<OnboardingPreferences>

    @Inject
    lateinit var subscriptionEventTracker: Lazy<SubscriptionEventTracker>

    @Inject
    lateinit var localeManager: Lazy<LocaleManager>

    @Inject
    lateinit var chatNavigator: Lazy<ChatNavigator>

    @Inject
    lateinit var paymentNavigator: Lazy<PaymentNavigator>

    @Inject
    lateinit var supplierAnalyticsEvents: Lazy<CustomerEventTracker>

    @Inject
    lateinit var supplierNavigator: Lazy<SupplierNavigator>

    @Inject
    lateinit var billsNavigator: Lazy<BillsNavigator>

    @Inject
    lateinit var accountingEventTracker: Lazy<AccountingEventTracker>

    @Inject
    lateinit var localInAppNotificationHandler: Lazy<LocalInAppNotificationHandler>

    private var alertDialog: AlertDialog? = null

    internal var mobile: String? = null

    internal var customerId: String? = null

    private var progressDialog: ProgressDialog? = null

    var totalAmount: Long = 0L

    var dueDate: DateTime? = null

    internal var canShowDiscountEducation = false

    internal var canShowMenuEducation = false

    internal var dueDateType = "Manual"

    internal var autoDueDateDialog: AutoDueDateDialog? = null

    private lateinit var customerController: CustomerControllerV2

    private val giveDiscountTimer = object : CountDownTimer(2000, 1000) {
        override fun onFinish() {
            canShowDiscountEducation = true
        }

        override fun onTick(millisUntilFinished: Long) {
        }
    }.start()

    private val menuTimer = object : CountDownTimer(2000, 1000) {
        override fun onFinish() {
            canShowMenuEducation = true
        }

        override fun onTick(millisUntilFinished: Long) {
        }
    }.start()

    private val dataObserver by lazy {
        object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (shouldScrollToAddedTransaction.not()) {
                    binding.recyclerView.scrollToPosition(customerController.adapter.itemCount - 1)
                }
            }
        }
    }

    @Inject
    lateinit var referralSignupTracker: Lazy<RewardsOnSignupTracker>

    internal val binding: CustomerFragmentBinding by viewLifecycleScoped(CustomerFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        initRecyclerView()
        initClickListener()
        initUi()
        binding.rootView.setTracker(performanceTracker)
        AnimationUtils.bounce(binding.ivHand)
        setClickListener()
    }

    private fun setClickListener() {
        binding.materialTextCashback.setRightDrawableTouchListener {
            pushIntent(CustomerContract.Intent.CashbackBannerClosed)
        }
    }

    private fun loadData() {
        loadIntents(
            CustomerContract.Intent.LoadKycDetails,
            CustomerContract.Intent.LoadReportFromBalanceWidgetExpt
        )
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            customerController = CustomerControllerV2(tracker, performanceTracker, accountingEventTracker)
            customerController.addListener(this@CustomerFragment)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = customerController.adapter
            setHasFixedSize(true)
            val epoxyVisibilityTracker = EpoxyVisibilityTracker()
            epoxyVisibilityTracker.attach(this)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (isAdded) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (dy > 0) {
                            val currentState = getCurrentState()
                            if (currentState.transactions.isNotEmpty()) {
                                binding.totalAmountContainer.visible()
                                handleSortTransactionsByUi(currentState)
                            }
                        } else if (dy < 0) {
                            binding.totalAmountContainer.gone()
                            setSortTransactionsByVisibility(false)
                        }

                        if (isScrolledToBottom(recyclerView)) {
                            binding.btnScrollToBottom.gone()
                        }
                    }
                }
            })
        }
    }

    internal fun isScrolledToBottom(recyclerView: RecyclerView): Boolean {
        return !recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN)
    }

    private fun initClickListener() {
        binding.collectGpay.debounceClickListener {
            val collectWithGPayEnabled = if (isStateInitialized()) getCurrentState().collectWithGPayEnabled else true
            val collectionActive = if (isStateInitialized()) getCurrentState().isCollectionActivated else false
            if (collectWithGPayEnabled) {
                if (collectionActive) {
                    CollectWithGooglePayBottomSheet.newInstance(customerId ?: "")
                        .show(childFragmentManager, CollectWithGooglePayBottomSheet::class.java.simpleName)
                } else {
                    showAddMerchantDestinationDialog("collect_with_gpay")
                }
            } else {
                showRequestAlreadySentToolTip()
            }

            eventTracker.get().trackCollectWithGpayClicked(
                accountId = getCurrentState().customer?.id ?: "",
                screen = CustomerEventTracker.CUSTOMER_SCREEN,
                relation = CustomerEventTracker.RELATION_CUSTOMER,
                amount = getCurrentState().customer?.balanceV2?.absoluteValue ?: 0L,
                type = if (collectWithGPayEnabled) "not_sent" else "sent",
                source = "button"
            )
        }
        binding.collectGpay.iconTint = null
    }

    private fun showRequestAlreadySentToolTip() {
        lifecycleScope.launch {
            localInAppNotificationHandler.get()
                .generateTooltip(
                    weakScreen = WeakReference(requireActivity()),
                    tooltip = TooltipLocal(
                        targetView = WeakReference(binding.collectGpay),
                        title = getString(R.string.collect_with_gpay_request_already_sent),
                        autoDismissTime = 5000L,
                        textSize = 13f,
                        backgroundColor = R.color.grey700,
                        screenName = label
                    )
                )
        }
    }

    override fun onDestroyView() {
        customerController.removeListener()
        customerController.adapter.unregisterAdapterDataObserver(dataObserver)
        super.onDestroyView()
    }

    private fun initUi() {
        referralRewardsController.get().onReferralTransactionInitiated = this::onReferralTransactionInitiated
        referralRewardsController.get().onReferralCloseClicked = this::closeReferralTargetBanner

        binding.referralTransactionRewardEpoxy.apply {
            adapter = referralRewardsController.get().adapter
        }

        customerController.adapter.registerAdapterDataObserver(dataObserver)

        binding.sendOrReceivePaymentAb.voiceFabAb.debounceClickListener {
            compositeDisposable.add(
                isInternetAvailable().subscribeOn(schedulerProvider.get().io()).subscribe {
                    if (it) {
                        openVoiceBottonSheet()
                    } else {
                        Toast.makeText(
                            context, getString(R.string.interent_error), Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

        binding.sendOrReceivePaymentAb.addCreditBtnAb.debounceClickListener {
            resetEducationPopUps()
            tracker.get().trackAddTransactionFlowsStarted(
                type = PropertyKey.CREDIT,
                relation = PropertyValue.CUSTOMER,
                accountId = customerId,
                source = Screen.CUSTOMER_SCREEN
            )
            checkForAutoDueDate = true
            pushIntent(CustomerContract.Intent.GoToAddTxn(Transaction.CREDIT))
        }

        binding.apply {
            dueInfoContainer.gone()
            dueDateContainer.gone()
        }

        binding.discountButton.debounceClickListener {
            onGiveDiscountClicked(PropertyValue.RELATIONSHIP_SCREEN)
        }

        binding.dueActionText.debounceClickListener {
            dueDateType = "Manual"
            openDueCalenderPopUp("Button", "New")
        }

        binding.reminderAction.debounceClickListener {
            val state = getCurrentState()
            state.let {
                state.customer?.let {
                    if (state.isKycLimitReached && state.canShowKycDialogOnRemind) {
                        showKycDialog(state)
                    } else if (it.mobile.isNullOrBlank()) {
                        openAddNumberPopup(it.id, getString(R.string.to_send_reminder), true)
                    } else {
                        pushIntent(CustomerContract.Intent.SendCollectionReminderClicked)
                    }
                }
            }
        }

        binding.warningCustomerImmutable.deleteImmutableAccount.debounceClickListener {
            val customerSyncStatus = when (
                if (isStateInitialized()) getCurrentState().customer?.customerSyncStatus
                else null
            ) {
                IMMUTABLE.code -> "Immutable"
                DIRTY.code -> "Dirty"
                CLEAN.code -> "Clean"
                else -> "Unknown"
            }
            tracker.get().trackDeleteRelationship(
                type = PropertyValue.CUSTOMER,
                accountId = customerId,
                customerSyncStatus = customerSyncStatus
            )
            pushIntent(CustomerContract.Intent.DeleteImmutableAccount)
        }

        binding.customerScreenToolbar.profileImage.debounceClickListener {
            tracker.get().trackViewProfile(
                PropertyValue.RELATION_PAGE,
                PropertyValue.CUSTOMER,
                PropertyValue.CUSTOMER,
                customerId
            )
            pushIntent(CustomerContract.Intent.GoToCustomerProfile)
        }
        binding.customerScreenToolbar.profileName.debounceClickListener {
            tracker.get().trackViewProfile(
                PropertyValue.RELATION_PAGE,
                PropertyValue.CUSTOMER,
                PropertyValue.CUSTOMER,
                customerId
            )
            pushIntent(CustomerContract.Intent.GoToCustomerProfile)
        }

        binding.customerScreenToolbar.tapDetails.debounceClickListener {
            tracker.get().trackViewProfile(
                PropertyValue.RELATION_PAGE,
                PropertyValue.CUSTOMER,
                PropertyValue.CUSTOMER,
                customerId
            )
            pushIntent(CustomerContract.Intent.GoToCustomerProfile)
        }

        binding.customerScreenToolbar.contextualHelp.initDependencies(
            ScreenName.CustomerScreen.value, tracker.get(), legacyNavigator.get()
        )

        binding.reminderAction.setOnLongClickListener {
            val state = getCurrentState()
            getCurrentState().customer?.let {
                if (state.isKycLimitReached && state.canShowKycDialogOnRemind) {
                    showKycDialog(state, true)
                } else {
                    return@setOnLongClickListener remindOnLongClick()
                }
            }
            return@setOnLongClickListener true
        }

        binding.sendOrReceivePaymentAb.addPaymentBtnAb.debounceClickListener {
            resetEducationPopUps()
            tracker.get().trackAddTransactionFlowsStarted(
                PropertyKey.PAYMENT,
                PropertyValue.CUSTOMER,
                customerId,
                source = Screen.CUSTOMER_SCREEN
            )
            pushIntent(CustomerContract.Intent.GoToAddTxn(Transaction.PAYMENT))
        }

        binding.customerScreenToolbar.menu.debounceClickListener {
            tracker.get().trackViewMore(
                PropertyValue.RELATIONSHIP_SCREEN,
                PropertyValue.CUSTOMER,
                customerId,
            )
            openMenuBottomSheet()
        }

        binding.tvSortTransactionsBy.setOnClickListener {
            // Disable item animation *momentarily* as it causes IndexOutOfBoundsException when sort selection is changed
            // https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/recyclerview/recyclerview/src/main/java/androidx/recyclerview/widget/RecyclerView.java#6304
            binding.recyclerView.itemAnimator = null
            openSortTransactionsByOptionsBottomSheet()
        }

        binding.btnScrollToBottom.setOnClickListener {
            scrollRecyclerViewToBottom()
        }
    }

    private fun scrollRecyclerViewToBottom() {
        binding.btnScrollToBottom.gone()
        binding.recyclerView.scrollToPosition(customerController.adapter.itemCount - 1)
        binding.totalAmountContainer.visible()
        handleSortTransactionsByUi(getCurrentState())
        accountingEventTracker.get().trackScrollToBottomClicked(RELATION_CUSTOMER, Screen.CUSTOMER_SCREEN)
    }

    override fun onSortOptionSelected(sortSelection: String) {
        accountingEventTracker.get().trackSortByUpdated(sortSelection, RELATION_CUSTOMER)
        pushIntent(CustomerContract.Intent.SortTransactionsByOptionSelected(sortSelection))
        if (customerController.adapter.itemCount > 0) {
            binding.recyclerView.scrollToPosition(customerController.adapter.itemCount - 1)
        }
    }

    private fun openSortTransactionsByOptionsBottomSheet() {
        accountingEventTracker.get().trackSortByClicked(RELATION_CUSTOMER)
        val sortSelection = getCurrentState().sortTransactionsBy?.value
        val bottomSheet = TransactionsSortCriteriaSelectionBottomSheet.newInstance(sortSelection, this)
        bottomSheet.show(requireActivity().supportFragmentManager, TransactionsSortCriteriaSelectionBottomSheet.TAG)
    }

    fun isInternetAvailable(): Observable<Boolean> {
        return Observable.fromCallable {
            try {
                val ipAddr: InetAddress = InetAddress.getByName("google.com")
                !ipAddr.equals("")
            } catch (e: Exception) {
                false
            }
        }.subscribeOn(ThreadUtils.api()).observeOn(AndroidSchedulers.mainThread())
    }

    private fun enableReportFromBalanceWidgetExp() {
        binding.arrowIcon.visible()
        binding.totalAmountContainer.debounceClickListener {
            pushIntent(CustomerContract.Intent.ShowCustomerReport("balance_widget"))
        }
    }

    internal fun remindOnLongClick(): Boolean {
        getCurrentState().customer?.let {
            if (it.mobile.isNullOrBlank()) {
                handleAddNumber(it.id, getString(R.string.to_send_reminder), true)
            } else {
                onReminderLongClicked(it, "Due Relationship Screen")
            }
        }
        return true
    }

    internal fun gotoKycScreen() {
        tracker.get()
            .trackStartKycClicked(
                type = getCurrentState().kycRiskCategory.value,
                screen = PropertyValue.RELATION_PAGE
            )
        legacyNavigator.get().goWebExperimentScreen(requireContext(), "kyc")
    }

    private fun showKycDialog(state: CustomerContract.State, isLongClicked: Boolean = false) {
        val listener = object : KycDialogListener {
            override fun onDisplayed(eventName: String, campaign: String) {
                trackKycEventDisplayed(eventName, campaign)
            }

            override fun onConfirmKyc(dontAskAgain: Boolean, eventName: String) {
                dontAskAgain(dontAskAgain)
                if (eventName.isEmpty()) return
                gotoKycScreen()
                trackKycEvents(eventName, dontAskAgain)
            }

            override fun onCancelKyc(dontAskAgain: Boolean, eventName: String) {
                if (isLongClicked) {
                    remindOnLongClick()
                } else {
                    pushIntent(CustomerContract.Intent.ForceRemind)
                }
                dontAskAgain(dontAskAgain)
                trackKycEvents(eventName, dontAskAgain)
            }

            override fun onDismissKyc(eventName: String) {
                trackKycEvents(eventName)
            }

            private fun dontAskAgain(dontAskAgain: Boolean) {
                if (dontAskAgain) {
                    pushIntent(CustomerContract.Intent.DontShowKycDialogOnRemind)
                }
            }
        }
        collectionNavigator.get().showKycDialog(
            fragmentManager = childFragmentManager,
            listener = listener,
            kycDialogMode = KycDialogMode.Remind,
            kycStatus = state.kycStatus,
            kycRiskCategory = state.kycRiskCategory
        )
    }

    internal fun trackKycEventDisplayed(eventName: String, campaign: String) {
        val state = getCurrentState()
        tracker.get()
            .trackEvents(
                eventName = eventName,
                screen = PropertyValue.HOME_PAGE,
                propertiesMap = PropertiesMap.create()
                    .add("merchant_id", state.business?.id ?: "")
                    .add("kyc_status", state.kycStatus.value.lowercase(Locale.getDefault()))
                    .add("risk_type", state.kycRiskCategory.value.lowercase(Locale.getDefault()))
                    .add("_campaign_id", campaign)
            )
    }

    internal fun trackKycBannerShown(bannerType: String) {
        val state = getCurrentState()
        tracker.get()
            .trackKycBannerShown(
                type = state.kycRiskCategory.value,
                screen = PropertyValue.RELATIONSHIP_SCREEN,
                bannerType = bannerType
            )
    }

    internal fun trackKycEvents(eventName: String, dontAskAgain: Boolean = false) {
        val state = getCurrentState()
        tracker.get()
            .trackEvents(
                eventName = eventName,
                screen = PropertyValue.RELATIONSHIP_SCREEN,
                propertiesMap = PropertiesMap.create()
                    .add("merchant_id", state.business?.id ?: "")
                    .add("account_id", state.customer?.id ?: "")
                    .add("kyc_status", state.kycStatus.value.lowercase(Locale.getDefault()))
                    .add("risk_type", state.kycRiskCategory.value.lowercase(Locale.getDefault()))
                    .add("dont_ask_again", dontAskAgain.toString())
            )
    }

    internal fun resetEducationPopUps() {
        canShowDiscountEducation = false
        canShowMenuEducation = false
        giveDiscountTimer.cancel()
        giveDiscountTimer.start()
        menuTimer.cancel()
        menuTimer.start()
    }

    private fun handleNotificationEventAndIntent(
        notificationInteractionState: String,
        type: String,
        screen: String,
        state: Int = MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED,
        focalAreaTapped: Boolean? = null,
        intentKey: String? = null,
        intentValue: Boolean? = null,
        scope: Scope? = null,
    ) {
        if (state !in validPromptStates)
            return

        val eventProperties = EventProperties.create().apply {
            with(PropertyKey.TYPE, type)
            with(PropertyKey.SCREEN, screen)
            if (focalAreaTapped != null)
                with("focal_area", focalAreaTapped)
        }
        Analytics.track(notificationInteractionState, eventProperties)

        if (!intentKey.isNullOrEmpty() && intentValue != null && scope != null)
            pushIntent(CustomerContract.Intent.RxPreferenceBoolean(intentKey, intentValue, scope))
    }

    private fun showGiveDiscountEducation() {
        handleNotificationEventAndIntent(
            AnalyticsEvents.IN_APP_NOTI_DISPLAYED,
            "give_discount",
            "customer"
        )
        activity?.runOnUiThread {
            if (context == null) return@runOnUiThread

            lifecycleScope.launch {
                val prompt = localInAppNotificationHandler.get()
                    .generateTapTarget(
                        weakScreen = WeakReference(requireActivity()),
                        tapTarget = TapTargetLocal(
                            screenName = label,
                            targetView = WeakReference(binding.customerScreenToolbar.menu),
                            title = getString(R.string.give_discount_education_primary_text),
                            titleTypeFaceStyle = R.style.OKC_TextAppearance_Headline6,
                            subtitle = getString(R.string.give_discount_education_secondary_text),
                            listener = { _, state ->
                                handleNotificationEventAndIntent(
                                    CustomerEventTracker.IN_APP_NOTI_CLICKED,
                                    "give_discount", "customer", state,
                                    state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED,
                                    RxSharedPrefValues.GIVE_DISCOUNT_EDUCATION, false,
                                    Scope.Individual
                                )

                                if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED)
                                    onGiveDiscountClicked(PropertyValue.RELATIONSHIP_SCREEN)
                            }
                        )
                    )
            }
        }
    }

    private fun showMenuEducation() {
        handleNotificationEventAndIntent(
            AnalyticsEvents.IN_APP_NOTI_DISPLAYED,
            "menu",
            "customer"
        )

        activity?.runOnUiThread {

            lifecycleScope.launch {
                val prompt = localInAppNotificationHandler.get()
                    .generateTapTarget(
                        weakScreen = WeakReference(requireActivity()),
                        tapTarget = TapTargetLocal(
                            screenName = label,
                            targetView = WeakReference(binding.customerScreenToolbar.menu),
                            title = getString(R.string.customer_menu_education_primary_text),
                            titleTypeFaceStyle = R.style.OKC_TextAppearance_Headline6,
                            subtitle = getString(R.string.customer_menu_education_secondary_text),
                            listener = { _, state ->

                                handleNotificationEventAndIntent(
                                    CustomerEventTracker.IN_APP_NOTI_CLICKED,
                                    "menu", "customer", state,
                                    state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED,
                                    RxSharedPrefValues.CUSTOMER_MENU_EDUCATION, false,
                                    Scope.Individual
                                )

                                if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED)
                                    onGiveDiscountClicked(PropertyValue.RELATIONSHIP_SCREEN)
                            }
                        )
                    )

                Observable.timer(5, TimeUnit.SECONDS).subscribe {
                    requireActivity().runOnUiThread {
                        prompt?.dismiss()
                        openMenuBottomSheet()
                    }
                }.addTo(autoDisposable)
            }
        }
    }

    private fun openVoiceBottonSheet() {
        voiceBottomSheet = VoiceInputBottomSheetFragment.getVoiceInstance()
        childFragmentManager.executePendingTransactions()
        if (voiceBottomSheet?.isAdded!!.not()) {
            voiceBottomSheet?.show(childFragmentManager, VoiceInputBottomSheetFragment.TAG)
        }
    }

    internal fun getCalenderPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_CALENDAR
            )
            != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_CALENDAR
                )
            != PackageManager.PERMISSION_GRANTED
        ) {
            tracker.get().trackViewCalenderPermission(PropertyValue.RELATIONSHIP_SCREEN)
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR),
                CALENDER_READ_AND_WRITE_PERMISSION
            )
        } else {
            onCalenderPermissionGranted()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CALENDER_READ_AND_WRITE_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    tracker.get().trackGrantPermission(PropertyValue.RELATIONSHIP_SCREEN, "Calender")
                    onCalenderPermissionGranted()
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CALENDAR)) {
                        tracker.get().trackDenyPermission(PropertyValue.RELATIONSHIP_SCREEN, "Calender", false)
                    } else {
                        tracker.get().trackDenyPermission(PropertyValue.RELATIONSHIP_SCREEN, "Calender", true)
                    }
                }
            }
        }
    }

    private fun onCalenderPermissionGranted() {
        val calID: Long? = CalenderUtils.getCalendarId(WeakReference(this.context))
        CalenderUtils.setEvent(
            calID,
            WeakReference(this.context),
            getCurrentState().customer?.description,
            DueDatePickerDialog.selectedDueDate?.okcDate?.timeInMillis,
            StringUtils.generateDeepLinkForCustomer(customerId)
        )
    }

    override fun loadIntent(): UserIntent {
        return CustomerContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        resetEducationPopUps()
        return Observable.mergeArray(
            Observable.just(CustomerContract.Intent.OnResume),
            binding.payOnline.clicks()
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    payOnlineIntent()
                }
        )
    }

    private fun payOnlineIntent(): CustomerContract.Intent.PayOnline {
        getCurrentState().let {
            supplierAnalyticsEvents.get().trackSupplierOnlinePaymentClick(
                accountId = it.customer?.id ?: "",
                dueAmount = it.customer?.balanceV2.toString(),
                screen = CustomerEventTracker.CUSTOMER_SCREEN,
                relation = CustomerEventTracker.RELATION_CUSTOMER,
                riskType = "",
                isCashbackMessageVisible = getCurrentState().cashbackMessage.isNotNullOrBlank()
            )
        }

        return CustomerContract.Intent.PayOnline
    }

    private fun setMenuOptions(state: CustomerContract.State) {
        if (state.menuOptionsResponse.menuOptions.isNullOrEmpty()) return
        setProfileImageVisibility(state)
        state.menuOptionsResponse.toolbarOptions.forEachIndexed { index, menuOptions ->
            var unreadCount = ""
            var canShowNewFlag = false
            when (menuOptions) {
                is MenuOptions.AccountChat -> {
                    unreadCount = state.unreadMessageCount
                    canShowNewFlag = state.canShowChatNewSticker
                }
                MenuOptions.Bill -> {
                    unreadCount = state.unreadBillCount.toString()
                    canShowNewFlag = state.canShowBillNewSticker
                }
                else -> {
                } // do nothing
            }
            when (index) {
                0 -> setToolbarMenu0(menuOptions)
                1 -> setToolbarMenu1(menuOptions, unreadCount, canShowNewFlag)
                2 -> setToolbarMenu2(menuOptions, unreadCount, canShowNewFlag)
                else -> {
                } // do nothing
            }
        }
        if (state.menuOptionsResponse.canShowContextualHelp) {
            binding.customerScreenToolbar.menu.gone()
            binding.customerScreenToolbar.contextualHelp.isVisible = !state.contextualHelpIds.isNullOrEmpty()
        } else {
            binding.customerScreenToolbar.menu.visible()
            binding.customerScreenToolbar.contextualHelp.gone()
        }
    }

    private fun setProfileImageVisibility(state: CustomerContract.State) {
        if (state.menuOptionsResponse.toolbarOptions.size >= 3) {
            binding.customerScreenToolbar.profileImage.gone()
        } else {
            binding.customerScreenToolbar.profileImage.visible()
        }
    }

    private fun setToolbarMenu0(menu: MenuOptions) {
        binding.customerScreenToolbar.toolbarMenu0.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                menu.icon
            )
        )
        binding.customerScreenToolbar.toolbarMenu0.debounceClickListener {
            onMenuClicked(menu, true)
        }

        binding.customerScreenToolbar.toolbarMenu0.visible()
    }

    private fun setToolbarMenu1(menu: MenuOptions, unreadCount: String = "", canShowNewFlag: Boolean = false) {
        binding.customerScreenToolbar.toolbarMenu1.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                menu.icon
            )
        )
        binding.customerScreenToolbar.toolbarMenu1Container.debounceClickListener {
            onMenuClicked(menu, true)
        }
        binding.customerScreenToolbar.toolbarMenu1.visible()
        binding.customerScreenToolbar.toolbarMenu1Container.visible()
        if (menu is MenuOptions.AccountChat || menu == MenuOptions.Bill) {
            setDetailMenu1(unreadCount, canShowNewFlag)
        }
    }

    private fun setToolbarMenu2(menu: MenuOptions, unreadCount: String = "", canShowNewFlag: Boolean = false) {
        binding.customerScreenToolbar.toolbarMenu2.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                menu.icon
            )
        )
        binding.customerScreenToolbar.toolbarMenu2Container.debounceClickListener {
            onMenuClicked(menu, true)
        }
        binding.customerScreenToolbar.toolbarMenu2.visible()
        binding.customerScreenToolbar.toolbarMenu2Container.visible()
        if (menu is MenuOptions.AccountChat || menu is MenuOptions.Bill) {
            setDetailMenu2(unreadCount, canShowNewFlag)
        }
    }

    private fun setDetailMenu1(unreadCount: String, canShowNewFlag: Boolean) {
        val view = binding.customerScreenToolbar
        when {
            unreadCount.isNotEmpty() && unreadCount.toInt() != 0 -> {
                view.menu1UnreadContianer.visible()
                view.menu1NewContianer.gone()
                view.menu1UnreadCount.visible()
                view.menu1UnreadCount.text = unreadCount
            }
            canShowNewFlag -> {
                view.menu1UnreadContianer.gone()
                view.menu1NewContianer.visible()
            }
            else -> {
                view.menu1UnreadContianer.gone()
                view.menu1NewContianer.gone()
            }
        }
    }

    private fun setDetailMenu2(unreadCount: String, canShowNewFlag: Boolean) {
        val view = binding.customerScreenToolbar
        when {
            unreadCount.isNotEmpty() && unreadCount.toInt() != 0 -> {
                view.menu2UnreadContianer.visible()
                view.menu2NewContianer.gone()
                view.menu2UnreadCount.visible()
                view.menu2UnreadCount.text = unreadCount
            }
            canShowNewFlag -> {
                view.menu2UnreadContianer.gone()
                view.menu2NewContianer.visible()
            }
            else -> {
                view.menu2UnreadContianer.gone()
                view.menu2NewContianer.gone()
            }
        }
    }

    private fun onMenuClicked(menu: MenuOptions, fromToolbar: Boolean = false) {
        when (menu) {
            is MenuOptions.AccountChat -> redirectToChatActivity(if (fromToolbar) "Relationship" else "More Options")
            MenuOptions.Bill -> redirectToBillActivity()
            MenuOptions.Call -> makeCall(if (fromToolbar) PropertyValue.RELATION_PAGE else "Relationship Drawer")
            MenuOptions.CollectWithGooglePay -> onGooglePayClicked()
            MenuOptions.CustomerStatements -> pushIntent(CustomerContract.Intent.ShowCustomerReport(if (fromToolbar) "toolbar" else "view_more"))
            MenuOptions.GiveDiscounts -> onGiveDiscountClicked(if (fromToolbar) PropertyValue.RELATIONSHIP_SCREEN else "Relationship  Drawer")
            MenuOptions.Help -> onHelpClicked(fromToolbar)
            MenuOptions.QrCode -> {
                qrSource = if (fromToolbar) "Relationship Screen" else "Relationship Drawer"
                pushIntent(CustomerContract.Intent.ShowQrCodeDialog)
            }
            MenuOptions.Subscriptions -> onSubscriptionClicked(fromToolbar)
            MenuOptions.DeleteRelationship -> getCurrentState().customer?.let { gotoDeleteCustomerScreen(it.id) }
            else -> {
            } // do nothing
        }
    }

    private fun getToolbarMenuView(menu: MenuOptions): View {
        return when (getCurrentState().menuOptionsResponse.toolbarOptions.indexOf(menu)) {
            0 -> binding.customerScreenToolbar.toolbarMenu0
            1 -> binding.customerScreenToolbar.toolbarMenu1
            else -> binding.customerScreenToolbar.toolbarMenu2
        }
    }

    private fun onSubscriptionClicked(fromToolbar: Boolean) {
        val source = if (fromToolbar) "relationship_screen" else "view_more"
        subscriptionEventTracker.get().trackSubscriptionClick(
            source = source,
            accountId = getCurrentState().customer?.id ?: "",
            mobile = getCurrentState().customer?.mobile
        )
        getCurrentState().customer?.id?.let { startActivity(SubscriptionActivity.getIntent(requireContext(), it)) }
    }

    @SuppressLint("CheckResult")
    @AddTrace(name = Traces.RENDER_CUSTOMER)
    override fun render(state: CustomerContract.State) {
        mobile = state.customer?.mobile
        customerId = state.customer?.id
        customerController.setData(state.customerScreenList)

        setMenuOptions(state)
        binding.customerScreenToolbar.contextualHelp.setContextualHelpIds(state.contextualHelpIds)
        // TODO: clean this up later
        binding.cashbackMessageTextView.gone() // this should be on top of handleTotalAmount invocation
        handleTotalAmount(state) // this should be on top

        mayBeShowVoiceTransactionButton(state.isVoiceTransactionEnabled)
        handleCustomerStatement(state)
        handleSpacingForRecyclerView(state)
        if (state.transactions.isNotEmpty()) {
            binding.totalAmountContainer.visible()
        } else {
            binding.totalAmountContainer.gone()
        }

        if (state.isSupplierCreditEnabledForCustomer) {
            handleSupplierCreditEnabled(state)
        } else if (!state.isCollectionActivated) {
            setCollectionEnabledText()
        } else {
            binding.commonLedgerDivider.gone()
            binding.registeredCustomerContainer.gone()
        }

        handleErrorAlert(state)
        handleDiscountButton(state)
        handlePlaySound(state)
        handleVoiceBottomSheetState(state)
        handleTargetBanner(state)
        handleMerchantBlocked(state)
        handleCreditPaymentLayout(state)
        handleMenuEducation(state)
        setToolbarInfo(state)
        handleGiveDiscount(state)
        handlePayOnlineEducation(state)
        setToolbarIcons(state)
        handleCollectWithGpay(state)
        handleWarningBanner(state)
        handleOnboardingNudges(state)
        handleCashBackUi(state)
        handleSortTransactionsByUi(state)
        checkForCustomerImmutable(state) // this function will be last ladder please add code to above this
    }

    internal fun handleSortTransactionsByUi(state: CustomerContract.State) {
        setSortTransactionsByVisibility(state.showSortTransactionsBy)
        if (state.showSortTransactionsBy) {
            val sortBy = when (state.sortTransactionsBy) {
                GetCustomerScreenSortSelection.CustomerScreenSortSelection.CREATE_DATE -> getString(R.string.t_001_filter_sort_by_create_date)
                else -> getString(R.string.t_001_filter_sort_by_bill_date)
            }
            binding.tvSortTransactionsBy.text = sortBy

            // Set recyclerview padding to avoid overlap
            binding.recyclerView.updatePadding(top = dpToPixel(56f).toInt())
            binding.recyclerView.clipToPadding = false
        }
    }

    internal fun setSortTransactionsByVisibility(visible: Boolean) {
        binding.tvSortTransactionsBy.isVisible = visible
        binding.tvSortTransactionsByLabel.isVisible = visible
    }

    private fun handleCashBackUi(state: CustomerContract.State) {
        binding.materialTextCashback.isVisible = state.shouldShowCashbackBanner
    }

    private fun handleWarningBanner(state: CustomerContract.State) {
        if (state.showPreNetworkWarningBanner) {
            binding.preNetworkBanner.visible()
        } else {
            binding.preNetworkBanner.gone()
        }
    }

    private fun handleOnboardingNudges(state: CustomerContract.State) {
        binding.apply {
            ivHand.isVisible = state.showOnboardingNudges
            if (state.showOnboardingNudges) {
                customerScreenToolbar.toolbarMenu1Container.gone()
                customerScreenToolbar.toolbarMenu2Container.gone()
                customerScreenToolbar.toolbarMenu0.gone()
                customerScreenToolbar.menu.gone()
                customerScreenToolbar.contextualHelp.gone()
            }
        }
    }

    private fun checkForCustomerImmutable(state: CustomerContract.State) {
        if (state.customer?.customerSyncStatus == IMMUTABLE.code) {
            binding.apply {
                warningCustomerImmutable.root.visible()
                sendOrReceivePaymentAb.root.gone()
                totalAmountContainer.gone()
                customerScreenToolbar.tapDetails.gone()
                customerScreenToolbar.profileImage.isClickable = false
                customerScreenToolbar.profileName.isClickable = false
                customerScreenToolbar.root.isClickable = false
                customerScreenToolbar.toolbarMenu1Container.gone()
                customerScreenToolbar.toolbarMenu2Container.gone()
                customerScreenToolbar.toolbarMenu0.gone()
                customerScreenToolbar.menu.gone()
                customerScreenToolbar.contextualHelp.gone()
                ivHand.gone()
                warningCustomerImmutable.descUnableAddCustomer.text = getCleanCompanionDescription(state)
            }
        }
    }

    private fun getCleanCompanionDescription(state: CustomerContract.State): String {
        return if (state.cleanCompanionDescription.isNotNullOrBlank()) {
            getString(
                R.string.ce_relation_scrn_err_subtitle_already_registered_as_customer_supplier,
                state.customer?.mobile.toString(),
                state.cleanCompanionDescription
            )
        } else {
            getString(
                R.string.already_registered_without_number,
                state.customer?.mobile.toString()
            )
        }
    }

    private fun mayBeShowVoiceTransactionButton(isVoiceTransactionEnabled: Boolean) {
        binding.sendOrReceivePaymentAb.voiceFabAb.isVisible = isVoiceTransactionEnabled
    }

    private fun handleCollectWithGpay(state: CustomerContract.State) {
        val dueDateActive = (state.dueInfo != null && state.dueInfo.isDueActive)
        val reminderDateVisible = !dueDateActive && state.canShowCollectionDate
        val balance = state.customer?.balanceV2 ?: 0
        val discountVisible = dueDate != null && DateTimeUtils.isDatePassed(dueDate) && state.isDiscountEnabled
        if (!reminderDateVisible &&
            !discountVisible &&
            balance < 0 &&
            state.showCollectionWithGpay &&
            state.customer?.mobile.isNotNullOrBlank()
        ) {
            binding.dueInfoContainer.setVisibility(binding.collectGpay, View.VISIBLE)
        } else {
            binding.dueInfoContainer.setVisibility(binding.collectGpay, View.GONE)
        }

        if (state.collectWithGPayEnabled) {
            binding.collectGpay.strokeColor = ColorStateList.valueOf(getColorCompat(R.color.grey400))
            binding.collectGpay.backgroundTintList = ColorStateList.valueOf(getColorCompat(R.color.white))
            binding.collectGpay.setTextColor(ColorStateList.valueOf(getColorCompat(R.color.grey900)))
        } else {
            binding.collectGpay.strokeColor = ColorStateList.valueOf(getColorCompat(R.color.grey200))
            binding.collectGpay.backgroundTintList = ColorStateList.valueOf(getColorCompat(R.color.grey200))
            binding.collectGpay.setTextColor(ColorStateList.valueOf(getColorCompat(R.color.grey500)))
        }
    }

    private fun onGooglePayClicked() {
        val collectWithGPayEnabled = if (isStateInitialized()) getCurrentState().collectWithGPayEnabled else true
        val collectionActive = if (isStateInitialized()) getCurrentState().isCollectionActivated else false
        if (collectWithGPayEnabled) {
            if (collectionActive) {
                CollectWithGooglePayBottomSheet.newInstance(customerId ?: "")
                    .show(childFragmentManager, CollectWithGooglePayBottomSheet::class.java.simpleName)
            } else {
                showAddMerchantDestinationDialog("collect_with_gpay")
            }
        } else {
            longToast(R.string.collect_with_gpay_request_already_sent)
        }

        eventTracker.get().trackCollectWithGpayClicked(
            accountId = getCurrentState().customer?.id ?: "",
            screen = CustomerEventTracker.CUSTOMER_SCREEN,
            relation = CustomerEventTracker.RELATION_CUSTOMER,
            amount = getCurrentState().customer?.balanceV2?.absoluteValue ?: 0L,
            type = if (collectWithGPayEnabled) "not_sent" else "sent",
            source = "menu"
        )
    }

    private fun handleCashbackMessage(state: CustomerContract.State) {
        binding.cashbackMessageTextView.apply {
            if (state.cashbackMessage.isNotNullOrBlank()) {
                text = state.cashbackMessage
                visible()
            } else {
                gone()
            }
        }
    }

    private fun setPayOnlineUi(state: CustomerContract.State) {
        binding.apply {
            // TODO: clean this up later, not an ideal solution
            if (!payOnline.isVisible) {
                tracker.get().trackPayOnlinePageView(
                    accountId = customerId,
                    screen = PropertyValue.RELATIONSHIP,
                    type = PropertyValue.ONLINE_PAYMENT,
                    relation = PropertyValue.CUSTOMER,
                )
            }
            payOnline.visible()
            handleCashbackMessage(state)
            if (state.showPayOnlineButtonLoader) {
                payOnline.text = ""
                ivPayOnlineLoading.visible()
                ivPayOnlineLoading.startAnimation(
                    android.view.animation.AnimationUtils.loadAnimation(
                        context,
                        R.anim.payment_rotate
                    )
                )
            } else {
                // When coming via online_payment deeplink
                if (getCurrentState().redirectToPayment && !isRedirectedToPayment) {
                    isRedirectedToPayment = true
                    pushIntent(payOnlineIntent())
                }
                ivPayOnlineLoading.clearAnimation()
                ivPayOnlineLoading.gone()
                payOnline.text = getString(R.string.pay_online)
            }
        }
    }

    private fun handleTotalAmount(state: CustomerContract.State) {
        totalAmount = if (state.customer?.balanceV2 == null) 0L else state.customer.balanceV2
        CurrencyUtil.renderV2(totalAmount, binding.total, 0)
        when {
            totalAmount == 0L -> {
                binding.dueDateContainer.visibility = View.GONE
            }
            totalAmount > 0 -> {
                handleAdvanceBalance(state)
            }
            else -> {
                handleDueDateInfo(state)
            }
        }
    }

    private fun handleSpacingForRecyclerView(state: CustomerContract.State) {
        if (state.transactions.isNullOrEmpty()) return

        if (state.customer?.dueActive != null) {
            if (state.transactions.isNotEmpty() && state.transactions.last().receiptUrl.isNullOrEmpty().not()) {
                binding.recyclerView.setPadding(0, 0, 0, dpToPixel(300.0f).toInt())
                handleSpacingDueToCashbackUi(state, 348.0f)
            } else {
                binding.recyclerView.setPadding(0, 0, 0, dpToPixel(240.0f).toInt())
                handleSpacingDueToCashbackUi(state, 288.0f)
            }
        } else {
            if (totalAmount < 0) {
                binding.recyclerView.setPadding(0, 0, 0, dpToPixel(100.0f).toInt())
                handleSpacingDueToCashbackUi(state, 148.0f)
            } else {
                binding.recyclerView.setPadding(0, 0, 0, dpToPixel(50.0f).toInt())
                handleSpacingDueToCashbackUi(state, 98.0f)
            }
        }
    }

    private fun handleSpacingDueToCashbackUi(state: CustomerContract.State, space: Float) {
        state.customerCollectionProfile?.let {
            if (it.cashbackEligible)
                binding.recyclerView.setPadding(0, 0, 0, dpToPixel(space).toInt())
        }
    }

    private fun handleErrorAlert(state: CustomerContract.State) {
        if (state.error) {
            alert = view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_LONG)
            alert?.show()
        } else {
            alert?.dismiss()
        }
    }

    private fun handleAdvanceBalance(state: CustomerContract.State) {
        if (state.transactions.isNullOrEmpty()) return

        binding.balanceText.text = requireContext().getString(R.string.customer_balance_advance)
        binding.dueDateInformal.text = ""
        binding.dueInfoContainer.visibility = View.GONE
        if (state.isCustomerPayOnlinePaymentEnable && state.customer?.customerSyncStatus == CLEAN.code) {
            setPayOnlineUi(state)
            binding.dueDateContainer.visible()
        } else {
            binding.dueDateContainer.visibility = View.GONE
        }
    }

    private fun handleDiscountButton(state: CustomerContract.State) {
        if (state.transactions.isNullOrEmpty()) return

        if (!binding.discountButton.isVisible() &&
            !binding.dueActionText.isVisible() &&
            !binding.collectGpay.isVisible() &&
            binding.reminderAction.isVisible()
        ) {
            binding.space.visibility = View.GONE
        } else {
            if (!binding.discountButton.isVisible() &&
                !binding.dueActionText.isVisible() &&
                !binding.discountButton.isVisible() &&
                !binding.collectGpay.isVisible() &&
                !binding.reminderAction.isVisible() &&
                binding.payOnline.isVisible()
            ) {
                binding.space.visibility = View.GONE
            } else {
                binding.space.visible()
            }
        }
    }

    private fun handleSupplierCreditEnabled(state: CustomerContract.State) {
        if (state.customer != null &&
            (state.customer.isAddTransactionPermissionDenied() || state.canShowCreditPaymentLayout.not())
        ) {
            if (state.canShowCreditPaymentLayout.not()) {
                binding.commentAccountText.text =
                    getString(R.string.single_list_transaction_disabled, state.customer.description)
            } else {
                binding.commentAccountText.text = getString(R.string.transaction_disabled)
            }
            binding.ivCommentAccount.setImageResource(R.drawable.ic_add_transaction_disabled_notify)
            binding.commentAccountText.isClickable = false
        } else {
            if (state.isSingleListEnabled) {
                binding.commentAccountText.text =
                    Html.fromHtml(getString(R.string.single_list_common_ledger_text_contact_know_more))
            } else {
                binding.commentAccountText.text =
                    Html.fromHtml(getString(R.string.common_ledger_text_customer_know_more))
            }
            binding.ivCommentAccount.setImageResource(R.drawable.ic_common_ledger)
            binding.commentAccountText.isClickable = true
            binding.commentAccountText.debounceClickListener {
                HelpActivity.start(requireContext(), ScreenName.CustomerScreen.value, ContextualHelp.COMMON_LEDGER)
            }
        }
        binding.commonLedgerDivider.visible()
        binding.registeredCustomerContainer.visible()
    }

    private fun handleDueDateInfo(state: CustomerContract.State) {
        if (state.dueInfo == null || state.transactions.isNullOrEmpty()) return
        binding.payOnline.gone()

        binding.balanceText.text = requireContext().getString(R.string.customer_balance_due)
        binding.dueDateInformal.text = ""

        if (state.customer?.customerSyncStatus != CLEAN.code) {
            binding.dueInfoContainer.gone()
            return
        }
        binding.dueInfoContainer.visible()
        binding.dueInfoContainer.debounceClickListener {
            if (state.canShowCollectionDate) {
                openDueCalenderPopUp("Text", "Update")
            }
        }
        binding.editDueDate.debounceClickListener {
            if (state.canShowCollectionDate) {
                openDueCalenderPopUp("Edit Icon", "Update")
            }
        }
        binding.dueMonth.debounceClickListener {
            if (state.canShowCollectionDate) {
                openDueCalenderPopUp("Date", "Update")
            }
        }
        binding.dueDateTextView.debounceClickListener {
            if (state.canShowCollectionDate) {
                openDueCalenderPopUp("Date", "Update")
            }
        }
        binding.dueDateContainer.visible()
        showDefaultDueActionContainer()
        showRemindIcon(state.customer.reminderMode)
        if (state.dueInfo.isDueActive) {
            dueDate = state.dueInfo.activeDate
            binding.dueDateTextView.text = DateTimeUtils.getDateFromMillis(dueDate).toString()
            binding.dueMonth.text = DateTimeUtils.getMonth(dueDate)
        }
        if (dueDate != null && state.dueInfo.isDueActive) {
            binding.dueInfoContainer.setVisibility(binding.dueActionText, View.GONE)
            when {
                DateTimeUtils.isPresentDate(dueDate) -> {
                    binding.dueDateInformal.text = getString(R.string.today)
                    binding.dueDateInformal.setTextColor(getColorCompat(R.color.green_primary))
                    binding.dueInfoContainer.setVisibility(binding.dueDateInformal, View.VISIBLE)
                    binding.dueDateInformal.setChipBackgroundColorResource(R.color.green_lite)
                    binding.dueDateInformal.setChipStrokeColorResource(R.color.green_lite_1)
                    binding.dueInfoSubtitle.text = getString(R.string.due_info_sms_sent)
                    binding.dueInfoTitle.text = getString(R.string.collect)
                    binding.dueMonth.setTextColor(getColorCompat(R.color.green_primary))
                    binding.dueDateTextView.setTextColor(getColorCompat(R.color.green_primary))
                }
                DateTimeUtils.isDatePassed(dueDate) -> {
                    binding.dueDateInformal.text = getString(R.string.pending)
                    binding.dueDateInformal.setTextColor(getColorCompat(R.color.red_primary))
                    binding.dueInfoSubtitle.text = getString(R.string.due_info_sms_sent)
                    binding.dueInfoTitle.text = getString(R.string.collection)
                    binding.dueInfoContainer.setVisibility(binding.dueDateInformal, View.VISIBLE)
                    binding.dueDateInformal.setChipBackgroundColorResource(R.color.red_lite)
                    binding.dueDateInformal.setChipStrokeColorResource(R.color.red_lite_1)
                    binding.dueMonth.setTextColor(getColorCompat(R.color.red_primary))
                    binding.dueDateTextView.setTextColor(getColorCompat(R.color.red_primary))
                }
                DateTimeUtils.isFutureDate(dueDate) -> {
                    binding.dueInfoSubtitle.text = getString(R.string.automatic_sms)
                    binding.dueInfoTitle.text = getString(R.string.upcoming_due)
                    binding.dueInfoContainer.setVisibility(binding.dueDateInformal, View.INVISIBLE)
                    binding.dueMonth.setTextColor(getColorCompat(R.color.green_primary))
                    binding.dueDateTextView.setTextColor(getColorCompat(R.color.green_primary))
                }
            }
            binding.editDueDate.visible()
            binding.dueInfoContainer.visible()
            binding.dueActionText.text = getString(R.string.change_reminder_date)
        } else {
            binding.dueInfoContainer.visibility = View.GONE
            binding.dueInfoContainer.setVisibility(binding.dueDateInformal, View.GONE)
            if (state.canShowCollectionDate) {
                binding.dueActionText.visible()
            } else {
                binding.dueActionText.gone()
            }
        }
    }

    private fun showRemindIcon(reminderMode: String?) {
        if (reminderMode == "sms") {
            binding.reminderAction.icon = getDrawableCompact(R.drawable.ic_sms)
        } else {
            binding.reminderAction.icon = getDrawableCompact(R.drawable.ic_whatsapp)
        }
    }

    private fun setCollectionEnabledText() {
        binding.commentAccountText.text = getString(R.string.setup_online_payments)
        binding.commentAccountText.isClickable = true
        binding.ivCommentAccount.setImageResource(R.drawable.ic_collection_orange)
        binding.commonLedgerDivider.visible()
        binding.registeredCustomerContainer.visible()
        binding.commentAccountText.debounceClickListener {
            startActivity(collectionNavigator.get().collectionBenefitsActivity(requireContext(), "customer_top_banner"))
        }
    }

    private fun handlePlaySound(state: CustomerContract.State) {
        if (state.playSound) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        mediaPlayer = MediaPlayer.create(context, R.raw.tx_add_sound)
                    }
                    if (!isPlayedSound) {
                        isPlayedSound = true
                        mediaPlayer.start()
                    }
                    mediaPlayer.setOnCompletionListener {
                        it.release()
                        pushIntent(CustomerContract.Intent.StopMediaPlayer)
                        isPlayedSound = false
                    }
                } catch (exception: Exception) {
                }
            }
        }
    }

    private fun handleDueDatePicker() {
        if (dueDatePicker == null) {
            initDueDatePicker()
        }
        dueDatePicker?.let { dueDatePicker ->
            if (!dueDatePicker.isVisible) {
                if (dueDatePicker.isAdded.not()) {
                    dueDatePicker.show(childFragmentManager, DueDatePickerDialog.TAG)
                }
                dueDatePicker.setUI(getCurrentState().dueInfo)
            }
        }
    }

    private fun handleVoiceBottomSheetState(state: CustomerContract.State) {
        if (state.isAlertVisible) {
            if (state.alertMessage == requireContext().getString(R.string.no_internet_msg)) {
                voiceBottomSheet?.let {
                    if (it.isVisible) {
                        it.showInternetError()
                    }
                }
            }
            alert = view?.snackbar(state.alertMessage, Snackbar.LENGTH_INDEFINITE)
            alert?.show()
        } else {
            alert?.dismiss()
        }
        if (state.canShowVoiceError) {
            voiceBottomSheet?.canDisplayError(true)
        } else {
            voiceBottomSheet?.canDisplayError(false)
        }
    }

    private fun handleTargetBanner(state: CustomerContract.State) {
        if (state.referralTargetBanner != null) {
            binding.referralTransactionRewardEpoxy.visible()
            referralRewardsController.get()
                .setReferralTargets(state.referralTargetBanner)
        } else {
            binding.referralTransactionRewardEpoxy.gone()
        }
    }

    private fun handleCustomerStatement(state: CustomerContract.State) {
        if (state.showCustomerStatementLoader) {
            if (progressDialog == null) {
                progressDialog = ProgressDialog.show(
                    context, "", getString(R.string.account_report_loading)
                )
            } else {
                progressDialog?.show()
            }
        } else {
            progressDialog?.dismiss()
        }
    }

    private fun handleMerchantBlocked(state: CustomerContract.State) {
        if (state.isBlocked) {
            showBlockedDialog()
        } else {
            binding.blockContainer.gone()
        }
    }

    private fun handleCreditPaymentLayout(state: CustomerContract.State) {
        if (state.canShowCreditPaymentLayout) {
            binding.sendOrReceivePaymentAb.root.visible()
        } else {
            binding.sendOrReceivePaymentAb.root.gone()
            binding.commentAccountText.text = getString(
                R.string.single_list_transaction_disabled,
                state.customer!!.description
            )
            binding.ivCommentAccount.setImageResource(R.drawable.ic_add_transaction_disabled_notify)
            binding.commentAccountText.isClickable = false
        }
    }

    private fun handleGiveDiscount(state: CustomerContract.State) {
        if (state.dueInfo != null && state.dueInfo.isDueActive && DateTimeUtils.isDatePassed(state.dueInfo.activeDate) && state.isDiscountEnabled) {
            binding.dueInfoContainer.setVisibility(binding.discountButton, View.VISIBLE)
        } else {
            binding.dueInfoContainer.setVisibility(binding.discountButton, View.GONE)
        }
        if (state.showGiveDiscountEducation && canShowDiscountEducation) {
            showGiveDiscountEducation()
        }
    }

    private fun handleMenuEducation(state: CustomerContract.State) {
        if (state.showCustomerMenuEducation && canShowMenuEducation) {
            showMenuEducation()
        }
    }

    private fun handlePayOnlineEducation(state: CustomerContract.State) {
        if (canShowPayOnline(state, totalAmount) && isPayOnlineEducationShownOnce.not()) {
            isPayOnlineEducationShownOnce = true
            pushIntent(CustomerContract.Intent.ShowPayOnlineEducation)
        }
    }

    private fun initDueDatePicker() {
        dueDatePicker = DueDatePickerDialog().addInteractionListnener(object :
                DueDatePickerDialog.InteractionListener {
                override fun onOkClicked(
                    capturedDate: MonthView.CapturedDate,
                    suggestedDaysSpan: String,
                ) {
                    if (capturedDate.dateStatus == MonthView.CapturedDate.DateStatus.ADDED) {
                        var flow = "New"
                        if (dueDate != null) {
                            flow = "Update"
                        }
                        if (dueDate != null && dueDate!!.millis != capturedDate.okcDate.timeInMillis) {
                            dueDateType = "Manual"
                        }
                        tracker.get().trackDueDateConfirmed(
                            "Due Relationship Screen",
                            capturedDate.okcDate.timeInMillis,
                            dueDate?.millis,
                            customerId,
                            PropertyValue.CUSTOMER,
                            flow,
                            suggestedDaysSpan,
                            dueDateType
                        )
                    }
                    resetEducationPopUps()
                    dismiss()
                    pushIntentWithDelay(CustomerContract.Intent.OnDueDateChange(capturedDate to getCurrentState().customer!!))
                    if (capturedDate.dateStatus == MonthView.CapturedDate.DateStatus.ADDED && getCurrentState().canGetCalendarPermission) {
                        getCalenderPermission()
                    }
                }

                private fun dismiss() {
                    dueDatePicker?.dismiss()
                }

                override fun onCancelClicked() {
                    resetEducationPopUps()
                    dismiss()
                }

                override fun onOutsideClicked() {
                    var flow = "New"
                    if (dueDate != null) {
                        flow = "Update"
                    }
                    tracker.get().trackCancelDueDate(
                        screen = "Due Relationship Screen",
                        accountId = customerId,
                        relation = PropertyValue.CUSTOMER,
                        flow = flow,
                        method = "Outer Screen"
                    )
                }

                override fun onClearClicked() {
                    var flow = "New"
                    getCurrentState().dueInfo?.isDueActive?.let {
                        if (it)
                            flow = "Update"
                    }
                    tracker.get()
                        .trackClearDueDate(
                            screen = "Due Relationship Screen",
                            accountID = customerId,
                            relation = PropertyValue.CUSTOMER,
                            flow = flow,
                            dateType = dueDateType
                        )
                }
            })
    }

    private fun setToolbarIcons(state: CustomerContract.State) {
        binding.customerScreenToolbar.apply {
            if (state.isLoading) {
                shimmerViewContainer.visible()
                profileName.gone()
            } else {
                shimmerViewContainer.gone()
                profileName.visible()
            }
        }
    }

    private fun showDefaultDueActionContainer() {
        binding.dueInfoContainer.apply {
            setVisibility(binding.dueActionText, View.VISIBLE)
            setVisibility(binding.reminderAction, View.VISIBLE)
            setVisibility(binding.discountButton, View.GONE)
        }
    }

    private fun trackDiscountClicked(source: String) {
        tracker.get().trackOnGiveDiscountClicked(
            source,
            PropertyValue.CUSTOMER,
            getCurrentState().customer?.id,
            "Discount",
            getCurrentState().dueInfo?.activeDate?.millis,
            DateTimeUtils.getFormat3(getCurrentState().dueInfo?.activeDate),
            totalAmount
        )
    }

    private fun showBlockedDialog() {
        var blockDialog = childFragmentManager.findFragmentByTag(BlockedDialogFragment.TAG)
        if (blockDialog != null && blockDialog is BlockedDialogFragment) {
            blockDialog.render()
            binding.blockContainer.visible()
            return
        }
        blockDialog = if (getCurrentState().customer != null && getCurrentState().customer!!.isBlockedByCustomer()) {
            BlockedDialogFragment.newInstance(
                BlockedDialogFragment.SCREEN_CUSTOMER,
                BlockedDialogFragment.TYPE_BLOCKED_BY
            )
        } else {
            BlockedDialogFragment.newInstance(
                BlockedDialogFragment.SCREEN_CUSTOMER,
                BlockedDialogFragment.TYPE_BLOCKED
            )
        }

        blockDialog.setListener(this)
        childFragmentManager.beginTransaction().replace(R.id.block_container, blockDialog).commit()
        binding.blockContainer.visible()
        binding.blockContainer.debounceClickListener {
            // blocks access to transaction
        }
    }

    private fun setToolbarInfo(state: CustomerContract.State) {
        binding.customerScreenToolbar.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        if (state.customer != null) {
            binding.customerScreenToolbar.profileName.text = state.customer.description
            binding.customerScreenToolbar.tapDetails.text = getString(R.string.view_profile)
            binding.customerScreenToolbar.tapDetails.setTextColor(getColorCompat(R.color.green_primary))
            binding.customerScreenToolbar.tapDetails.visible()

            val defaultPic = TextDrawable
                .builder()
                .buildRound(
                    state.customer.description.substring(0, 1).uppercase(Locale.getDefault()),
                    ColorGenerator.MATERIAL.getColor(state.customer.description)
                )
            if (state.customer.profileImage != null) {

                imageLoader.get().context(this)
                    .load(state.customer.profileImage)
                    .placeHolder(defaultPic)
                    .scaleType(IImageLoader.CIRCLE_CROP)
                    .into(binding.customerScreenToolbar.profileImage)
                    .build()
            } else {
                binding.customerScreenToolbar.profileImage.setImageDrawable(defaultPic)
//                setProfileImageVisibility(View.VISIBLE)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkForAutoDueDate) {
            checkForAutoDueDate = false
            pushIntent(CustomerContract.Intent.SyncDueInfo)
        }
    }

    private fun onReminderLongClicked(customer: Customer, source: String): Boolean {
        val reminderDialog = ReminderBottomSheetDialog.netInstance(tracker.get())

        if (!reminderDialog.isVisible) {
            val state = getCurrentState()
            val totalAmount = if (state.customer?.balanceV2 == null) 0L else customer.balanceV2
            val reminderMode = state.customer?.reminderMode ?: "whatsapp"
            val mobileNumber = state.customer?.mobile ?: ""
            val accountId = state.customer?.id ?: ""

            val bundle = Bundle()
            bundle.putParcelable(
                ReminderBottomSheetDialog.REMINDER_OBJECT,
                ReminderBottomSheetDialog.ReminderSheet(
                    totalAmount = totalAmount,
                    reminderMode = reminderMode,
                    mobileNumber = mobileNumber,
                    accountId = accountId
                )
            )

            reminderDialog.arguments = bundle

            reminderDialog.show(requireActivity().supportFragmentManager, ReminderBottomSheetDialog.TAG)
            reminderDialog.initialise(this@CustomerFragment)

            tracker.get().trackEvents(
                Event.REMINDER_LONG_PRESS,
                type = PropertyValue.REMINDER_SETTING,
                screen = source,
                relation = PropertyValue.CUSTOMER,
                propertiesMap = PropertiesMap.create()
                    .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
            )
        }

        return true
    }

    private fun openMenuBottomSheet() {
        val menuDialog = MenuOptionsBottomSheet.newInstance()
        val menuOptionsToShow = mutableListOf<MenuOptions>()
        menuOptionsToShow.addAll(getCurrentState().menuOptionsResponse.menuOptions)
        getCurrentState().menuOptionsResponse.toolbarOptions.forEach {
            menuOptionsToShow.remove(it)
        }

        val menuOptionsToHide = mutableListOf<MenuOptions>()
        menuOptionsToHide.addAll(getCurrentState().menuOptionsResponse.toolbarOptions)
        if (menuOptionsToShow.contains(MenuOptions.DeleteRelationship)) {
            menuOptionsToShow.remove(MenuOptions.DeleteRelationship)
            menuOptionsToHide.add(MenuOptions.DeleteRelationship)
        }
        if (!menuDialog.isVisible) {
            val bundle = Bundle()
            bundle.putParcelable(
                MenuOptionsBottomSheet.MENU_PARCEL,
                MenuSheet(menuOptionsToShow, menuOptionsToHide)
            )
            bundle.putString(MenuOptionsBottomSheet.UNREAD_CHAT_ACCOUNT, getCurrentState().unreadMessageCount)
            bundle.putParcelable(MenuOptionsBottomSheet.CUSTOMER_NAME, getCurrentState().customer)
            menuDialog.arguments = bundle
            menuOptionEventTracker.get().trackMenuOptionViewed()
            menuDialog.show(requireActivity().supportFragmentManager, MenuOptionsBottomSheet.TAG)
            menuDialog.initialise(this@CustomerFragment)
        }
    }

    private fun makeCall(source: String) {
        if (!isStateInitialized()) {
            return
        }
        var isCallIconEnable = true
        val state = getCurrentState()
        if (state.customer?.mobile.isNullOrEmpty()) {
            isCallIconEnable = false
            openAddNumberPopup(state.customer?.id ?: "", getString(R.string.to_call_add_customer_number))
        } else {
            pushIntent(CustomerContract.Intent.GoToPhoneDialer)
        }

        val accountId = state.customer?.id ?: ""
        tracker.get().trackEvents(
            Event.CALL_RELATIONSHIP, screen = source, relation = PropertyValue.CUSTOMER,
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.ACCOUNT_ID, accountId)
                .add(PropertyKey.ENABLED, isCallIconEnable)
        )
    }

    override fun onPause() {
        super.onPause()

        keyboardVisibilityEventRegister?.unregister()
        mediaPlayer.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_WHATSAPP -> {
                    lifecycleScope.launchWhenResumed {
                        pushIntent(CustomerContract.Intent.MarkCustomerShared)
                    }
                }
                REQUEST_CODE_ADD_TRANSACTION -> {
                    val isSortedByBillDate = getCurrentState().sortTransactionsBy == BILL_DATE
                    if (isSortedByBillDate.not()) return // Only scroll to txn if sortBy selection is BILL_DATE

                    lifecycleScope.launchWhenResumed {
                        shouldScrollToAddedTransaction = true
                        delay(500)
                        scrollToAddedTransaction(data?.getStringExtra(KEY_TRANSACTION_ID))
                    }
                }
            }
        }
    }

    private fun scrollToAddedTransaction(newTransactionId: String?) {
        if (newTransactionId.isNullOrBlank()) return

        val models = customerController.adapter.copyOfModels
        models.forEachIndexed { index, model ->
            if (model !is TransactionViewModel_) return@forEachIndexed // Early return if model is not TransactionView

            val transactionId = model.data().id
            if (transactionId == newTransactionId) {
                shouldScrollToAddedTransaction = false
                (binding.recyclerView.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(index, 300)

                if (index == models.lastIndex) return // Do not animate if transaction is the latest one

                binding.recyclerView.postDelayed({ animateTransactionViewBackground(index) }, 200)
                binding.btnScrollToBottom.visible()
            }
        }
    }

    private fun animateTransactionViewBackground(index: Int) {
        binding.recyclerView.findViewHolderForAdapterPosition(index)?.itemView?.let { transactionView ->
            val container = transactionView.findViewById<MaterialCardView>(R.id.cvContainer)
            val highlightColor = ColorStateList.valueOf(getColorCompat(R.color.indigo_lite))
            val transparent = ColorStateList.valueOf(getColorCompat(R.color.transparent))
            container?.backgroundTintList = highlightColor
            container?.postDelayed({ container.backgroundTintList = transparent }, 2000)
            AnimationUtils.shakeV2(transactionView)
        }
    }

    override fun onTransactionClicked(txnId: String, currentDue: Long, isDiscountTxn: Boolean) {
        resetEducationPopUps()
        if (isDiscountTxn) {
            pushIntent(CustomerContract.Intent.ViewDiscount(txnId, currentDue))
        } else {
            pushIntent(CustomerContract.Intent.ViewTransaction(txnId, currentDue))
        }
    }

    override fun chatWithUs(amount: String, paymentTime: String, txnId: String, status: String) {
        CustomerSupportOptionDialog
            .newInstance(
                amount = amount,
                paymentTime = paymentTime,
                txnId = txnId,
                status = status,
                accountId = getCurrentState().customer?.id ?: "",
                ledgerType = LedgerType.CUSTOMER.value.lowercase(),
                source = SOURCE_LEDGER,
            ).show(
                childFragmentManager,
                CustomerSupportOptionDialog.TAG
            )
    }

    override fun onInfoNudgeClicked(type: Int) {
        if (type == 0 || type == 5) {
            val intent = collectionNavigator.get().collectionBenefitsActivity(
                context = requireContext(),
                source = "realtime_contextual",
                sendReminder = true,
                customerId = customerId
            )
            startActivity(intent)
        } else if (type in listOf(1, 2, 3, 4)) {
            pushIntent(CustomerContract.Intent.GotoReferralScreen)
        }
    }

    override fun onLoadMoreClicked() {
        pushIntent(CustomerContract.Intent.ExpandTransactions)
    }

    override fun onTransactionClicked(txnId: String, currentDue: Long) {
        pushIntent(CustomerContract.Intent.ViewTransaction(txnId, currentDue))
    }

    override fun setReminderMode(reminderMode: String) {
        lifecycleScope.launchWhenResumed {
            pushIntent(CustomerContract.Intent.SetReminderMode(reminderMode))
        }
    }

    override fun sharePaymentReminderOnWhatsapp() {
        lifecycleScope.launchWhenResumed {
            val reminderStringsObject = GetPaymentReminderIntent.ReminderStringsObject(
                paymentReminderText = R.string.payment_reminder_text,
                toMobile = R.string.to_mobile,
                dueOn = R.string.due_as_on,
            )
            pushIntent(CustomerContract.Intent.SharePaymentLink("whatsapp", reminderStringsObject))
        }
    }

    private fun onForceRemind() {
        requireActivity().runOnUiThread {
            if (isStateInitialized()) {
                val state = getCurrentState()
                val totalAmount: Long = if (state.customer?.balanceV2 == null) 0L else state.customer.balanceV2
                sharePaymentReminder(totalAmount)
            }
        }
    }

    private fun onReminderClicked() {
        requireActivity().runOnUiThread {
            if (isStateInitialized()) {
                val state = getCurrentState()
                val totalAmount: Long = if (state.customer?.balanceV2 == null) 0L else state.customer.balanceV2
                if (!state.customer?.mobile.isNullOrEmpty()) {
                    sharePaymentReminder(totalAmount)
                } else {
                    state.customer?.let {
                        handleAddNumber(it.id, getString(R.string.to_send_reminder), true)
                    }
                }
            }
        }
    }

    private fun goToCustomerReportScreen(source: String) {
        val customerId = getCurrentState().customer?.id ?: ""
        findNavController(this).navigate(CustomerFragmentDirections.goToCustomerReportsScreen(customerId))

        val state = getCurrentState()

        tracker.get().trackEvents(
            eventName = Event.ACCOUNT_REPORT_CLICK,
            relation = PropertyValue.CUSTOMER,
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
                .add(PropertyKey.COLLECTION_ADOPTED, state.isCollectionActivated)
                .add(PropertyKey.DUE_AMOUNT, state.customer?.balanceV2 ?: "")
                .add(PropertyKey.SOURCE, source)
        )
    }

    fun sharePaymentReminder(totalAmount: Long) {
        when {
            totalAmount >= 0 -> {
                longToast(R.string.balance_in_advance_no_reminder)
            }
            getCurrentState().customer?.reminderMode.equals("sms") -> {
                sharePaymentReminderOnSms()
            }
            else -> {
                sharePaymentReminderOnWhatsapp()
            }
        }
    }

    private fun showOnlineCollectionEducation() {
        val state = getCurrentState()
        val totalAmount: Long = if (state.customer?.balanceV2 == null) 0L else state.customer.balanceV2
        compositeDisposable.add(
            rxCompletable { rxSharedPreference.get().set(IS_ONLINE_COLLECTION_EDUCATION_SHOWN, true, Scope.Individual) }
                .subscribeOn(ThreadUtils.database())
                .subscribe()
        )

        tracker.get().trackEvents(
            Event.COLLECTION_INAPP_DISPLAYED,
            screen = Screen.RELATIONSHIP_REMINDER,
            relation = PropertyValue.CUSTOMER,
            propertiesMap = PropertiesMap.create().add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
        )
        collectionNavigator.get().showCustomerOnlineCollectionDialog(
            fragmentManager = childFragmentManager,
            listener = object : CustomerOnlineEducationListener {
                override fun skipAndSend(dontAskAgain: Boolean) {
                    dontShowEducationAgain(dontAskAgain)
                    sharePaymentReminder(totalAmount)

                    tracker.get().trackEvents(
                        CustomerEventTracker.COLLECTION_INAPP_CLICKED,
                        screen = Screen.RELATIONSHIP_REMINDER,
                        relation = PropertyValue.CUSTOMER,
                        propertiesMap = PropertiesMap.create()
                            .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
                            .add(PropertyKey.VALUE, PropertyValue.SKIP)
                            .add(PropertyKey.ALWAYS, dontAskAgain)
                    )
                }

                override fun setupNow(dontAskAgain: Boolean) {
                    dontShowEducationAgain(dontAskAgain)
                    showAddMerchantDestinationDialog("online_collection_education")

                    tracker.get().trackEvents(
                        CustomerEventTracker.COLLECTION_INAPP_CLICKED,
                        screen = Screen.RELATIONSHIP_REMINDER,
                        relation = PropertyValue.CUSTOMER,
                        propertiesMap = PropertiesMap.create()
                            .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
                            .add(PropertyKey.VALUE, PropertyValue.SETUP)
                            .add(PropertyKey.ALWAYS, dontAskAgain)
                    )
                }

                override fun onDismiss() {
                    tracker.get().trackEvents(
                        CustomerEventTracker.COLLECTION_INAPP_CLEARED,
                        screen = Screen.RELATIONSHIP_REMINDER,
                        relation = PropertyValue.CUSTOMER,
                        propertiesMap = PropertiesMap.create()
                            .add(PropertyKey.ACCOUNT_ID, state.customer?.id ?: "")
                    )
                }
            }
        )
    }

    internal fun showAddMerchantDestinationDialog(source: String, paymentMethodType: String? = null) {
        collectionNavigator.get().showAddMerchantDestinationDialog(
            fragmentManager = childFragmentManager,
            source = source,
            isUpdateCollection = false,
            paymentMethodType = paymentMethodType
        )
    }

    internal fun dontShowEducationAgain(dontAskAgain: Boolean) {
        if (dontAskAgain) {
            rxCompletable {
                rxSharedPreference.get().set(IS_ONLINE_COLLECTION_EDUCATION_DENIED, true, Scope.Individual)
            }
                .subscribeOn(ThreadUtils.database()).subscribe().addTo(autoDisposable)
        }
    }

    override fun sharePaymentReminderOnSms() {
        lifecycleScope.launchWhenResumed {
            val reminderStringsObject = GetPaymentReminderIntent.ReminderStringsObject(
                paymentReminderText = R.string.payment_reminder_text,
                toMobile = R.string.to_mobile,
                dueOn = R.string.due_as_on,
            )
            pushIntent(CustomerContract.Intent.SharePaymentLink("sms", reminderStringsObject))
        }
    }

    private fun goToHomeScreen() {
        activity?.finishAffinity()
        legacyNavigator.get().goToHome(requireActivity())
    }

    private fun gotoAddTransactionThroughVoice(
        customerId: String,
        txnType: Int,
        amount: Int,
    ) {
        hideSoftKeyboard()
        voiceBottomSheet?.parentFragment?.let {
            voiceBottomSheet?.dismiss()
        }

        tracker.get().trackAddTransactionFlowsStarted(
            type = if (txnType == Transaction.CREDIT) {
                PropertyKey.CREDIT
            } else {
                PropertyKey.PAYMENT
            },
            relation = PropertyValue.CUSTOMER,
            accountId = customerId,
            source = Screen.CUSTOMER_SCREEN_VOICE_TRANSACTION
        )
        startActivity(
            AddTxnContainerActivity.getIntent(
                context = requireContext(),
                customerId = customerId,
                txnType = txnType,
                amount = (amount * 100).toLong(), // convert into paisa
                addTransactionScreenType = AddTxnContainerActivity.ADD_TRANSACTION_SCREEN,
                source = AddTxnContainerActivity.Source.CUSTOMER_SCREEN_VOICE_TRANSACTION
            )
        )
    }

    private fun gotoTransactionDetailFragment(transaction: Transaction) {
        hideSoftKeyboard()
        val flow: String? =
            if (getCurrentState().customer?.isLiveSales == true) PropertyValue.LINK_PAY else null

        val type =
            if (transaction.type == Transaction.PAYMENT || transaction.type == Transaction.RETURN) {
                PropertyValue.PAYMENT
            } else if (transaction.type == Transaction.CREDIT) {
                PropertyValue.CREDIT
            } else {
                "na"
            }

        val status = when {
            transaction.isDeleted -> {
                PropertyValue.DELETED
            }
            transaction.amountUpdated -> {
                PropertyValue.EDITED
            }
            transaction.isOnlinePaymentTransaction -> {
                PropertyValue.ONLINE_PAYMENT
            }
            else -> {
                "na"
            }
        }

        accountingEventTracker.get().trackViewTransaction(
            screen = PropertyValue.RELATION_PAGE,
            relation = PropertyValue.CUSTOMER,
            mobile = mobile,
            accountId = customerId,
            flow = flow,
            type = type,
            status = status,
            blocked = getCurrentState().customer?.isBlockedByCustomer(),
            customerSupportType = getCurrentState().supportType.value,
            customerSupportMessage = getString(R.string.t_002_i_need_help_generic),
            amount = transaction.amountV2.toString(),
            cashbackMessageShown = getCurrentState().collectionsMap[transaction.collectionId]?.cashbackGiven ?: false,
        )

        legacyNavigator.get().goToTransactionDetailFragment(requireContext(), transaction.id)
    }

    private fun gotoCustomerProfileForAddingMobile(customerId: String) {
        hideSoftKeyboard()
        openAddNumberPopup(customerId, getString(R.string.please_add_customer_number))
    }

    private fun gotoCollectionOnboarding() {
        hideSoftKeyboard()
        legacyNavigator.get().goToCollectionTutorialScreen(requireActivity(), PropertyValue.CUSTOMER_SCREEN)
    }

    private fun gotoCustomerPrivacyScreen() {
        hideSoftKeyboard()
        legacyNavigator.get().gotoPrivacyScreen(requireContext())
    }

    private fun gotoCustomerProfile(customerId: String) {
        hideSoftKeyboard()
        legacyNavigator.get().gotoCustomerProfile(requireActivity(), customerId)
    }

    private fun showUnblockDialog() {
        val customer = getCurrentState().customer
        if (customer != null) {
            val unblockDialogFragment = BlockRelationShipDialogFragment.newInstance(
                BlockRelationShipDialogFragment.SCREEN_CUSTOMER,
                BlockRelationShipDialogFragment.TYPE_UNBLOCK,
                customer.description,
                customer.profileImage,
                customer.mobile,
            )
            unblockDialogFragment.show(parentFragmentManager, BlockRelationShipDialogFragment.TAG)
            unblockDialogFragment.setListener(object : BlockRelationShipDialogFragment.Listener {
                override fun onAction(action: String) {
                    if (action == getString(R.string.unblock)) {
                        pushIntentWithDelay(CustomerContract.Intent.Unblock)
                    }
                }
            })
        }
    }

    private fun showCollectionDateEducation() {
        try {
            viewLifecycleOwner
        } catch (e: Exception) {
            return
        }

        if (binding.dueActionText.isVisible().not()) {
            return
        }

        tracker.get().trackInAppDisplayedV1(type = PropertyValue.DUE_DATE, screen = PropertyValue.RELATION_PAGE)
        activity?.runOnUiThread {

            lifecycleScope.launch {
                localInAppNotificationHandler.get()
                    .generateTapTarget(
                        weakScreen = WeakReference(requireActivity()),
                        tapTarget = TapTargetLocal(
                            screenName = label,
                            targetView = WeakReference(binding.dueActionText),
                            title = getString(R.string.reminder_date_education),
                            listener = { _, state ->

                                handleNotificationEventAndIntent(
                                    CustomerEventTracker.IN_APP_NOTI_CLICKED,
                                    PropertyValue.DUE_DATE, PropertyValue.RELATION_PAGE, state,
                                    state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED
                                )
                            }
                        )
                    )
            }
        }
    }

    private fun showRemindEducation() {

        if (binding.reminderAction.isVisible().not()) {
            return
        }
        activity?.runOnUiThread {
            tracker.get()
                .trackInAppDisplayedV1(type = PropertyValue.REMINDER_SETTING, screen = PropertyValue.RELATION_PAGE)

            lifecycleScope.launch {
                localInAppNotificationHandler.get()
                    .generateTapTarget(
                        weakScreen = WeakReference(requireActivity()),
                        tapTarget = TapTargetLocal(
                            screenName = label,
                            targetView = WeakReference(binding.reminderAction),
                            title = getString(R.string.remind_education),
                            titleGravity = Gravity.END,
                            listener = { _, state ->
                                handleNotificationEventAndIntent(
                                    CustomerEventTracker.IN_APP_NOTI_CLICKED,
                                    PropertyValue.REMINDER_SETTING, PropertyValue.RELATION_PAGE, state,
                                    state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED
                                )
                            }
                        )
                    )
            }
        }
    }

    private fun showReportIconEducation() {
        if (isStateInitialized().not() ||
            getCurrentState().customer?.mobile.isNullOrEmpty() ||
            getCurrentState().transactions.isEmpty() ||
            getCurrentState().menuOptionsResponse.toolbarOptions.contains(MenuOptions.CustomerStatements).not()
        ) {
            return
        }
        lifecycleScope.launch {

            tracker.get().trackEvents(
                eventName = CustomerEventTracker.IN_APP_NOTI_DISPLAYED,
                type = PropertyValue.CUSTOMER_REPORT,
                screen = PropertyValue.RELATIONSHIP_SCREEN,
                propertiesMap = PropertiesMap.create()
                    .add(PropertyKey.ACCOUNT_ID, getCurrentState().customer?.id ?: "")
            )
            val view = getToolbarMenuView(MenuOptions.CustomerStatements)

            localInAppNotificationHandler.get()
                .generateTapTarget(
                    weakScreen = WeakReference(requireActivity()),
                    tapTarget = TapTargetLocal(
                        screenName = label,
                        targetView = WeakReference(view),
                        title = getString(R.string.customer_report),
                        titleGravity = Gravity.END,
                        subtitle = getString(R.string.now_you_can_view),
                        subtitleGravity = Gravity.CENTER_HORIZONTAL,
                        listener = { _, state ->
                            when (state) {
                                MaterialTapTargetPrompt.STATE_FOCAL_PRESSED -> {
                                    tracker.get().trackEvents(
                                        eventName = CustomerEventTracker.IN_APP_NOTI_CLICKED,
                                        type = PropertyValue.CUSTOMER_REPORT,
                                        screen = PropertyValue.RELATIONSHIP_SCREEN,
                                        propertiesMap = PropertiesMap.create()
                                            .add(PropertyKey.ACCOUNT_ID, getCurrentState().customer?.id ?: "")
                                            .add(PropertyKey.FOCAL_AREA, true)
                                    )
                                    pushIntent(
                                        CustomerContract.Intent.RxPreferenceBoolean(
                                            RxSharedPrefValues.IS_REPORT_ICON_EDUCATION_SHOWN,
                                            true,
                                            Scope.Individual
                                        )
                                    )
                                }
                                MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED -> {
                                    tracker.get().trackEvents(
                                        eventName = CustomerEventTracker.IN_APP_NOTI_CLICKED,
                                        type = PropertyValue.CUSTOMER_REPORT,
                                        screen = PropertyValue.RELATIONSHIP_SCREEN,
                                        propertiesMap = PropertiesMap.create()
                                            .add(PropertyKey.ACCOUNT_ID, getCurrentState().customer?.id ?: "")
                                            .add(PropertyKey.FOCAL_AREA, false)
                                    )
                                    pushIntent(
                                        CustomerContract.Intent.RxPreferenceBoolean(
                                            RxSharedPrefValues.IS_REPORT_ICON_EDUCATION_SHOWN,
                                            true,
                                            Scope.Individual
                                        )
                                    )
                                }
                                MaterialTapTargetPrompt.STATE_BACK_BUTTON_PRESSED -> {
                                    tracker.get().trackEvents(
                                        eventName = CustomerEventTracker.IN_APP_NOTI_CLEARED,
                                        type = PropertyValue.CUSTOMER_REPORT,
                                        screen = PropertyValue.RELATIONSHIP_SCREEN,
                                        propertiesMap = PropertiesMap.create()
                                            .add(PropertyKey.ACCOUNT_ID, getCurrentState().customer?.id ?: "")
                                            .add(PropertyKey.FOCAL_AREA, false)
                                    )
                                }
                            }
                        }
                    )
                )
        }
    }

    private fun goToDiscountScreen(txnId: String) {
        hideSoftKeyboard()
        tracker.get().trackViewDiscount(PropertyValue.RELATION_PAGE, PropertyValue.CUSTOMER, customerId)
        legacyNavigator.get().gotoDiscountDetailsScreen(requireActivity(), txnId)
    }

    private fun gotoCallCustomer(mobile: String) {
        hideSoftKeyboard()
        tracker.get().trackCallRelationShip(
            screen = PropertyValue.RELATION_PAGE,
            relation = PropertyValue.CUSTOMER,
            accountId = customerId,
            isBlocked = getCurrentState().customer?.state == Customer.State.BLOCKED
        )

        Permission.requestCallPermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                    tracker.get().trackRuntimePermission(PropertyValue.CUSTOMER, Event.CALL, true)
                }

                override fun onPermissionGranted() {
                    activity?.let {
                        val intent = Intent(Intent.ACTION_CALL)
                        intent.data = Uri.parse(getString(R.string.call_template, mobile))
                        activity?.startActivity(intent)
                    }
                }

                override fun onPermissionDenied() {
                    context?.let {
                        tracker.get().trackRuntimePermission(PropertyValue.CUSTOMER, Event.CALL, false)
                        longToast(getString(R.string.call_permission_denied))
                    }
                }
            }
        )
    }

    // when transaction is deleted by customer, visible notification is received
    // when user click on the notification , he is taken to this screen and we auto scroll to the deleted transaction & vibrate phone
    private fun gotoDeletedTransaction(txnId: String) {
        if (!scrolledToDeletedTransaction) {

            Observable.timer(2, TimeUnit.SECONDS)
                .observeOn(ThreadUtils.computation())
                .subscribe {

                    loop@ for ((index, value) in customerController.adapter.copyOfModels.withIndex()) {
                        if (value is DeleteTransactionViewModel_) {

                            val deleteTransaction =
                                (customerController.adapter.getModelAtPosition(index) as DeleteTransactionViewModel_).transaction().id
                            if (deleteTransaction == txnId) {

                                activity?.runOnUiThread {
                                    if (binding.recyclerView.layoutManager is LinearLayoutManager) {
                                        (binding.recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                                            index,
                                            500
                                        )
                                    }

                                    binding.recyclerView.postDelayed(
                                        {
                                            val view = binding.recyclerView.findViewHolderForAdapterPosition(index)
                                            if (view != null) {
                                                val container = view.itemView
                                                AnimationUtils.shakeV1(container)
                                            }
                                        },
                                        200
                                    )
                                }
                                scrolledToDeletedTransaction = true
                                break@loop
                            }
                        }
                    }
                }.addTo(autoDisposable)
        }
    }

    private fun showQrCodePopup() {
        hideSoftKeyboard()
        homeNavigator.get().goToCustomerProfileDialog(childFragmentManager, customerId ?: "", qrSource)
    }

    private fun shareReportIntent(intent: Intent) {
        hideSoftKeyboard()
        activity?.startActivity(intent)
    }

    private fun openPaymentReminderIntent(intent: Intent) {
        hideSoftKeyboard()
        startActivityForResult(intent, REQUEST_CODE_WHATSAPP)
    }

    override fun onBackPressed(): Boolean {
        hideSoftKeyboard()
        pushIntent(CustomerContract.Intent.UpdateLastViewTime)
        return super.onBackPressed()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        menuTimer.cancel()
        giveDiscountTimer.cancel()
        super.onDestroy()
        if (alertDialog != null && alertDialog?.isShowing == true) {
            alertDialog?.dismiss()
        }
    }

    private fun handleAddNumber(
        customerId: String,
        description: String,
        skipAndSend: Boolean = false,
    ) {
        openAddNumberPopup(customerId, description, skipAndSend)
    }

    private fun openAddNumberPopup(customerId: String, description: String, skipAndSend: Boolean = false) {
        val dialog = AddNumberDialogScreen.newInstance(
            customerId = customerId,
            description = description,
            isSkipAndSend = skipAndSend
        )
        dialog.setListener(object : AddNumberDialogScreen.Listener {
            override fun onSkip() {
                pushIntent(CustomerContract.Intent.ForceRemind)
            }

            override fun onDone() {
                pushIntent(CustomerContract.Intent.UpdateMobileAndRemind)
            }
        })
        dialog.show(childFragmentManager, AddNumberDialogScreen.TAG)
    }

    private fun showChatEducation() {
        val chatMenu =
            getCurrentState().menuOptionsResponse.toolbarOptions.firstOrNull { it is MenuOptions.AccountChat }
        if (!isStateInitialized() || chatMenu == null) return
        handleNotificationEventAndIntent(
            AnalyticsEvents.IN_APP_NOTI_DISPLAYED,
            "chat",
            "customer"
        )
        try {
            viewLifecycleOwner
        } catch (e: Exception) {
            return
        }

        val view = getToolbarMenuView(chatMenu)
        activity?.runOnUiThread {
            lifecycleScope.launch {

                localInAppNotificationHandler.get()
                    .generateTapTarget(
                        weakScreen = WeakReference(requireActivity()),
                        tapTarget = TapTargetLocal(
                            screenName = label,
                            targetView = WeakReference(view),
                            title = getString(R.string.chat_education),
                            titleGravity = Gravity.END,
                            listener = { _, state ->

                                handleNotificationEventAndIntent(
                                    CustomerEventTracker.IN_APP_NOTI_CLICKED,
                                    "chat", "customer", state,
                                    state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED
                                )

                                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                    redirectToChatActivity("Relationship")
                                }
                            }
                        )
                    )
            }
        }
    }

    internal fun openDueCalenderPopUp(source: String, flow: String) {
        tracker.get().trackSelectDueDate(
            "Due Relationship Screen",
            customerId,
            PropertyValue.CUSTOMER,
            getCurrentState().dueInfo?.activeDate?.millis,
            DateTimeUtils.getFormat3(getCurrentState().dueInfo?.activeDate),
            flow,
            source
        )
        val totalAmount =
            if (getCurrentState().customer?.balanceV2 == null) 0L else getCurrentState().customer?.balanceV2

        totalAmount?.let {
            if (it < 0) {
                resetEducationPopUps()
                pushIntent(CustomerContract.Intent.ShowDueDatePickerIntent)
            }
        }
    }

    override fun cancel() {
        tracker.get().trackVoiceTransactionClosed(customerId, PropertyValue.CUSTOMER)

        voiceBottomSheet?.dismiss()
    }

    override fun sendVoiceInputText(text: String) {
        pushIntent(CustomerContract.Intent.SubmitVoiceInput(text))
    }

    override fun canShowVoiceError(canShowVoiceError: Boolean) {
        pushIntent(CustomerContract.Intent.VoiceInputState(canShowVoiceError))
    }

    override fun onVoiceTransactionStarted() {
        tracker.get().trackVoiceTransactionStarted(customerId, PropertyValue.CUSTOMER)
    }

    override fun onBlockListenerAction(action: Int) {
        if (action == BlockedDialogFragment.ACTION_CALL) {
            pushIntentWithDelay(CustomerContract.Intent.GoToPhoneDialer)
        } else {
            pushIntentWithDelay(CustomerContract.Intent.ShowUnblockDialog)
            tracker.get().trackUnBlockRelation(
                Event.UNBLOCK_RELATION_CLICKED,
                PropertyValue.CUSTOMER,
                customerId,
                PropertyValue.CUSTOMER_SCREEN
            )
        }
    }

    private fun onGiveDiscountClicked(source: String) {
        trackDiscountClicked(source)
        hideSoftKeyboard()
        activity?.runOnUiThread {
            legacyNavigator.get().gotToAddDiscountScreen(requireActivity(), customerId)
        }
    }

    private fun onHelpClicked(fromToolbar: Boolean) {
        val source = if (fromToolbar) "Relationship Screen" else "Relationship Drawer"
        tracker.get().trackViewHelpItem_v3(
            "contact_us", "Customer Screen", source,
            "Started", "text", customerId, PropertyValue.CUSTOMER
        )
        binding.customerScreenToolbar.contextualHelp.setContextualHelpClick()
    }

    private fun redirectToChatActivity(source: String) {
        tracker.get().trackChatIconClicked(
            customerId,
            PropertyValue.CUSTOMER,
            getCurrentState().unreadMessageCount,
            source

        )

        getCurrentState().customer?.let {
            if (it.mobile.isNullOrEmpty()) {
                openAddNumberPopup(it.id, getString(R.string.to_send_chat))
            } else {
                startActivity(
                    chatNavigator.get().getChatIntent(
                        context = requireContext(),
                        accountId = getCurrentState().customer!!.id,
                        role = "SELLER",
                        unreadMessageCount = getCurrentState().unreadMessageCount,
                        firstUnseenMessageId = getCurrentState().firstUnseenMessageId
                    )
                )
            }
        }
    }

    private fun redirectToBillActivity() {
        tracker.get().trackBillIconClicked(
            customerId,
            PropertyValue.CUSTOMER,
            getCurrentState().unreadBillCount,
            getCurrentState().totalBills,
            "Relationship"
        )

        getCurrentState().customer?.let {
            activity?.let {
                startActivity(
                    billsNavigator.get().getBillActivityIntent(
                        requireActivity(),
                        getCurrentState().customer!!.id,
                        BILL_INTENT_EXTRAS.CUSTOMER,
                        getCurrentState().customer?.description
                    )
                )
            }
        }
    }

    private fun showPayOnlineEducation() {
        try {
            viewLifecycleOwner
        } catch (e: Exception) {
            return
        }

        handleNotificationEventAndIntent(
            AnalyticsEvents.IN_APP_NOTI_DISPLAYED, "PAY ONLINE REMINDER EDUCATION",
            "customer"
        )

        if (binding.payOnline.isVisible()) {

            activity?.runOnUiThread {

                lifecycleScope.launch {
                    localInAppNotificationHandler.get()
                        .generateTapTarget(
                            weakScreen = WeakReference(requireActivity()),
                            tapTarget = TapTargetLocal(
                                screenName = label,
                                targetView = WeakReference(binding.payOnline),
                                title = getString(R.string.single_list_pay_online_education_text),
                                titleTextSize = 16f,
                                titleGravity = Gravity.END,
                                listener = { _, state ->

                                    handleNotificationEventAndIntent(
                                        CustomerEventTracker.IN_APP_NOTI_CLICKED,
                                        "PAY ONLINE REMINDER EDUCATION", "customer", state,
                                        state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED,
                                        RxSharedPrefValues.IS_PAY_ONLINE_EDUCATION_SHOWN, true,
                                        Scope.Individual
                                    )
                                }
                            )
                        )
                }
            }
        }
    }

    private fun canShowPayOnline(state: CustomerContract.State, totalAmount: Long): Boolean {
        return state.isSupplierCollectionEnabled && totalAmount < 0
    }

    private fun isSupplierDestinationPresent(collectionProfile: CollectionCustomerProfile?) =
        !collectionProfile?.message_link.isNullOrBlank() && !collectionProfile?.paymentAddress.isNullOrBlank()

    private fun gotoDeleteCustomerScreen(customerId: String) {
        legacyNavigator.get().goToDeleteCustomerScreen(requireContext(), customerId)
    }

    override fun handleViewEvent(event: CustomerContract.ViewEvent) {
        when (event) {
            is CustomerContract.ViewEvent.GotoCustomerProfile -> gotoCustomerProfile(event.customerId)

            is CustomerContract.ViewEvent.GotoTransactionDetailFragment -> gotoTransactionDetailFragment(
                event.transaction
            )

            is CustomerContract.ViewEvent.GotoAddTransactionThroughVoice -> gotoAddTransactionThroughVoice(
                event.customerId,
                event.txnType,
                event.amount
            )

            is CustomerContract.ViewEvent.GotoLegacyAddTransaction -> {
                startActivityForResult(
                    AddTxnContainerActivity.getIntent(
                        context = requireContext(),
                        customerId = event.customerId,
                        txnType = event.txnType,
                        addTransactionScreenType = if (getCurrentState().isRoboflowFeatureEnabled)
                            AddTxnContainerActivity.ADD_TRANSACTION_ROBOFLOW
                        else
                            AddTxnContainerActivity.ADD_TRANSACTION_SCREEN,
                        source = AddTxnContainerActivity.Source.CUSTOMER_SCREEN
                    ),
                    REQUEST_CODE_ADD_TRANSACTION
                )
            }

            is CustomerContract.ViewEvent.GotoCallCustomer -> gotoCallCustomer(event.mobile)

            is CustomerContract.ViewEvent.ShowQrCodePopup -> showQrCodePopup()

            is CustomerContract.ViewEvent.GotoCustomerProfileForAddingMobile -> gotoCustomerProfileForAddingMobile(
                event.customerId
            )

            is CustomerContract.ViewEvent.GotoCollectionOnboarding -> gotoCollectionOnboarding()

            is CustomerContract.ViewEvent.GotoCustomerPrivacyScreen -> gotoCustomerPrivacyScreen()

            is CustomerContract.ViewEvent.GotoDeletedTransaction -> gotoDeletedTransaction(event.txnId)

            is CustomerContract.ViewEvent.OpenPaymentReminderIntent -> openPaymentReminderIntent(event.intent)

            is CustomerContract.ViewEvent.ShareReportIntent -> shareReportIntent(event.intent)

            is CustomerContract.ViewEvent.GoToHomeScreen -> goToHomeScreen()

            is CustomerContract.ViewEvent.ShowUnblockDialog -> showUnblockDialog()

            is CustomerContract.ViewEvent.GoToDiscountScreen -> goToDiscountScreen(event.txnId)

            is CustomerContract.ViewEvent.ShowCollectionDateEducation -> showCollectionDateEducation()

            is CustomerContract.ViewEvent.ShowRemindEducation -> showRemindEducation()

            is CustomerContract.ViewEvent.OnReminderClicked -> onReminderClicked()

            is CustomerContract.ViewEvent.ShowOnlineCollectionEducation -> showOnlineCollectionEducation()

            is CustomerContract.ViewEvent.ShowChatEducation -> showChatEducation()

            is CustomerContract.ViewEvent.GoToCustomerReport -> goToCustomerReportScreen(event.source)

            is CustomerContract.ViewEvent.ShowReportIconEducation -> showReportIconEducation()

            is CustomerContract.ViewEvent.ShowPayOnlineEducation -> showPayOnlineEducation()

            is CustomerContract.ViewEvent.ShowBuyerTxnAlert -> showBuyerTxnAlert()

            CustomerContract.ViewEvent.ShowSetupCollectionDialog -> showSetupCollectionDialog()

            is CustomerContract.ViewEvent.ShowPaymentPendingDialog -> showPaymentPendingDialog(
                event.customer,
                event.dueInfo,
                event.showVariant
            )
            is CustomerContract.ViewEvent.ForceRemind -> onForceRemind()

            CustomerContract.ViewEvent.ShowBillEducation -> showBillEducation()

            is CustomerContract.ViewEvent.ShowAutoDueDateDialog -> showAutoDueDateDialog(event.activeDate)

            is CustomerContract.ViewEvent.ShowAddPaymentMethodDialog -> {
                showAddPaymentDestinationDialog()
            }
            is CustomerContract.ViewEvent.GotoPaymentEditAmountScreen -> gotoPaymentEditAmountScreen(
                linkId = event.linkId,
                paymentAddress = event.paymentAddress,
                paymentType = event.paymentType,
                paymentName = event.paymentName,
                remainingDailyAmount = event.remainingDailyAmount,
                maxDailyAmount = event.maxDailyAmount,
                riskType = event.riskType,
                kycStatus = event.kycStatus,
                kycRiskCategory = event.kycRiskCategory,
                futureAmountLimit = event.futureAmountLimit,
            )
            is CustomerContract.ViewEvent.GotoCustomerBlindPayEditAmountScreen -> gotoBlindPayEditAmountScreen(
                paymentAddress = event.paymentAddress,
                paymentType = event.paymentType,
                paymentName = event.paymentName,
                remainingDailyAmount = event.remainingDailyAmount,
                maxDailyAmount = event.maxDailyAmount,
                riskType = event.riskType
            )

            is CustomerContract.ViewEvent.ShowWebFlowDestinationDialog -> showWebFlowDestinationDialog(
                messageLink = event.messageLink,
                paymentAddress = event.paymentAddress,
                paymentType = event.paymentType,
                paymentName = event.paymentName
            )
            is CustomerContract.ViewEvent.OpenLimitReachedBottomSheet -> openLimitReachedBottomSheet()
            CustomerContract.ViewEvent.ShowDueDatePickerDialog -> handleDueDatePicker()
            CustomerContract.ViewEvent.EnableReportFromBalanceWidgetExp -> enableReportFromBalanceWidgetExp()
            is CustomerContract.ViewEvent.CollectWithGPayError -> {
                longToast(event.errDefault)
                binding.collectGpay.enable()
            }
            CustomerContract.ViewEvent.CollectWithGPayRequestSent -> {
                longToast(R.string.collect_with_gpay_sent)
                binding.collectGpay.enable()
            }
            is CustomerContract.ViewEvent.OpenWhatsAppForHelp -> startActivity(event.intent)
            is CustomerContract.ViewEvent.ShowError -> shortToast(event.msg)
            is CustomerContract.ViewEvent.AccountDeletedSuccessfully -> {
                shortToast(getString(R.string.ce_relation_scrn_account_deleted_successfully))
                requireActivity().finish()
            }

            is CustomerContract.ViewEvent.ShowBlindPayDialog -> showBlindPayDialog()

            is CustomerContract.ViewEvent.GotoReferralEducationScreen -> collectionNavigator.get()
                .goToReferralEducationScreen(requireContext(), getCurrentState().customer?.id ?: "")
            is CustomerContract.ViewEvent.GotoReferralInviteListScreen -> collectionNavigator.get()
                .goToTargetedReferralInviteScreen(requireContext(), getCurrentState().customer?.id ?: "")
            is CustomerContract.ViewEvent.OpenExitDialog -> {
                CustomerSupportExitDialog
                    .newInstance(
                        ledgerType = LedgerType.CUSTOMER.value,
                        accountId = getCurrentState().customer?.id ?: "",
                        source = event.exitSource
                    )
                    .show(childFragmentManager, CustomerSupportExitDialog.TAG)
            }
            CustomerContract.ViewEvent.CashbackBannerClosed -> binding.materialTextCashback.gone()
            is CustomerContract.ViewEvent.GoToAddPaymentWithQr -> {
                startActivityForResult(
                    AddCustomerPaymentActivity.getIntent(
                        context = requireContext(),
                        customerId = event.customerId,
                        expandedQr = event.expandedQr,
                        source = qrSource ?: AddTxnContainerActivity.Source.CUSTOMER_SCREEN.value
                    ),
                    REQUEST_CODE_ADD_TRANSACTION
                )
            }
        }
    }

    private fun gotoBlindPayEditAmountScreen(
        paymentAddress: String,
        paymentType: String,
        paymentName: String,
        remainingDailyAmount: Long,
        maxDailyAmount: Long,
        riskType: String,
    ) {

        getCurrentState().let {
            val balance = it.customer?.balanceV2 ?: 0L
            val dueBalance = if (balance > 0) -balance else 0L
            paymentNavigator.get().gotoJuspayPaymentEditAmountScreen(
                context = requireActivity(),
                accountId = it.customer?.id ?: "",
                maxDailyLimit = maxDailyAmount,
                remainingDailyAmount = remainingDailyAmount,
                supplierBalance = dueBalance,
                merchantId = it.business?.id ?: "",
                riskType = riskType,
                linkId = it.blindPayLinkId,
                mobile = it.customer?.mobile ?: "",
                paymentAddress = paymentAddress,
                destinationType = paymentType,
                name = paymentName,
                accountType = LedgerType.CUSTOMER.value,
                kycStatus = KycStatus.NOT_SET.value,
                kycRiskCategory = KycRiskCategory.NO_RISK.value,
                futureAmountLimit = 0,
                listener = this,
                isBlindPayFlow = true,
                profileImage = it.customer?.profileImage ?: "",
                profileName = it.customer?.description ?: "",
                destinationUpdateAllowed = it.destinationUpdateAllowed,
                supportType = getCurrentState().supportType.value,
            )
        }
    }

    private fun showWebFlowDestinationDialog(
        messageLink: String,
        paymentAddress: String,
        paymentType: String,
        paymentName: String,
    ) {
        if (!isStateInitialized()) return

        supplierNavigator.get().showSupplierPaymentDialogScreen(
            fragmentManager = childFragmentManager,
            accountId = getCurrentState().customer?.id ?: "",
            mobile = getCurrentState().customer?.mobile ?: "",
            balance = getCurrentState().customer?.balanceV2 ?: 0L,
            destinationType = paymentType,
            messageLink = messageLink,
            paymentAddress = paymentAddress,
            name = paymentName,
            accountType = LedgerType.CUSTOMER.value,
            listener = object : SupplierPaymentListener {
                override fun onChangeDetails(dialog: AppCompatDialogFragment) {
                    dialog.dismiss()
                    showAddPaymentDestinationDialog()
                }

                override fun onConfirm(messageLink: String, dialog: AppCompatDialogFragment) {
                    dialog.dismiss()
                    goToWebActivity(messageLink)
                }
            }
        )
    }

    internal fun goToWebActivity(messageLink: String) {
        activity?.runOnUiThread {
            messageLink.let {
                legacyNavigator.get().goToWebViewScreen(requireActivity(), it)
            }
        }
    }

    internal fun showAddPaymentDestinationDialog() {
        getCurrentState().customer?.let {
            paymentNavigator.get().gotoAddPaymentDestinationDialog(
                it.id, LedgerType.CUSTOMER.value,
                it.mobile ?: "",
                name = it.description,
                dueBalance = it.balanceV2,
                profileImage = it.profileImage ?: "",
                childFragmentManager,
                this,
                getCurrentState().isBlindPayEnabled,
            )
        }
    }

    internal fun showBlindPayDialog() {
        paymentNavigator.get().gotoBlindPayDialog(
            listener = this,
            childFragmentManager = childFragmentManager,
            ledgerType = LedgerType.CUSTOMER.value.lowercase(),
            accountId = getCurrentState().customer?.id.toString(),
        )
    }

    private fun showBillEducation() {
        if (!isStateInitialized() || !getCurrentState().menuOptionsResponse.toolbarOptions.contains(MenuOptions.Bill)) return

        handleNotificationEventAndIntent(
            AnalyticsEvents.IN_APP_NOTI_DISPLAYED, "Bill Management",
            "customer"
        )
        try {
            viewLifecycleOwner
        } catch (e: Exception) {
            return
        }
        val view = getToolbarMenuView(MenuOptions.Bill)

        activity?.runOnUiThread {

            lifecycleScope.launch {
                localInAppNotificationHandler.get()
                    .generateTapTarget(
                        weakScreen = WeakReference(requireActivity()),
                        tapTarget = TapTargetLocal(
                            screenName = label,
                            targetView = WeakReference(view),
                            title = getString(R.string.bills_education),
                            subtitle = getString(R.string.bills_education_details),
                            subtitleGravity = Gravity.END,
                            listener = { _, state ->

                                handleNotificationEventAndIntent(
                                    CustomerEventTracker.IN_APP_NOTI_CLICKED,
                                    "Bill Management", "customer", state,
                                    state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED
                                )
                            }
                        )
                    )
            }
        }
    }

    private fun showPaymentPendingDialog(
        customer: Customer,
        dueInfo: DueInfo,
        showVariant: GetCollectionNudgeOnDueDateCrossed.Show,
    ) {
        val state = getCurrentState()
        val totalAmount: Long = if (state.customer?.balanceV2 == null) 0L else state.customer.balanceV2
        var paymentPendingDialog =
            childFragmentManager.findFragmentByTag(PaymentPendingDialog.TAG) as? PaymentPendingDialog
        if (paymentPendingDialog == null) {
            paymentPendingDialog = PaymentPendingDialog()
            paymentPendingDialog.initListener(object : PaymentPendingDialog.Listener {
                override fun onUpdate() {
                    openDueCalenderPopUp("Text", "Update")
                }

                override fun onRemind() {
                    sharePaymentReminder(totalAmount)
                }

                override fun onSetUpNow() {
                    showAddMerchantDestinationDialog("customer_payment_pending")
                }
            })
            paymentPendingDialog.setEventListener(paymentPendingDialogEventListener)
            paymentPendingDialog.setData(customer, dueInfo, showVariant)
        }
        if (paymentPendingDialog.isVisible.not() && paymentPendingDialog.isAdded.not()) {
            paymentPendingDialog.show(childFragmentManager, PaymentPendingDialog.TAG)
        }
    }

    private val paymentPendingDialogEventListener = object : PaymentPendingDialog.EventListener {
        override fun displayed(variant: String, type: String, screen: String) {
            tracker.get().trackEvents(
                eventName = "InAppNotification Displayed",
                type = type,
                screen = screen,
                relation = PropertyValue.CUSTOMER,
                propertiesMap = PropertiesMap.create()
                    .add("variant", variant)
                    .add("merchant_id", getCurrentState().business?.id ?: "")
                    .add("account_id", getCurrentState().customer?.id ?: "")
            )
        }

        override fun cleared(variant: String, type: String, screen: String) {
            tracker.get().trackEvents(
                eventName = "InAppNotification Cleared",
                type = type,
                screen = screen,
                relation = PropertyValue.CUSTOMER,
                propertiesMap = PropertiesMap.create()
                    .add("variant", variant)
                    .add("merchant_id", getCurrentState().business?.id ?: "")
                    .add("account_id", getCurrentState().customer?.id ?: "")
            )
        }

        override fun clicked(variant: String, focal: Boolean, type: String, screen: String) {
            tracker.get().trackEvents(
                eventName = "InAppNotification clicked",
                type = type,
                screen = screen,
                relation = PropertyValue.CUSTOMER,
                propertiesMap = PropertiesMap.create()
                    .add("variant", variant)
                    .add("focal", focal)
                    .add("merchant_id", getCurrentState().business?.id ?: "")
                    .add("account_id", getCurrentState().customer?.id ?: "")
            )
        }

        override fun trackUpdate(variant: String, type: String, screen: String) {
            tracker.get().trackEvents(
                eventName = "update_due_date_clicked",
                type = type,
                screen = screen,
                relation = PropertyValue.CUSTOMER,
                propertiesMap = PropertiesMap.create()
                    .add("variant", variant)
                    .add("merchant_id", getCurrentState().business?.id ?: "")
                    .add("account_id", getCurrentState().customer?.id ?: "")
            )
        }

        override fun trackRemind(variant: String, type: String, screen: String) {
            tracker.get().trackEvents(
                eventName = "send_reminder",
                type = type,
                screen = screen,
                relation = PropertyValue.CUSTOMER,
                propertiesMap = PropertiesMap.create()
                    .add("variant", variant)
                    .add("merchant_id", getCurrentState().business?.id ?: "")
                    .add("account_id", getCurrentState().customer?.id ?: "")
            )
        }

        override fun trackSetup(variant: String, type: String, screen: String) {
            tracker.get().trackEvents(
                eventName = "set_up_collection_clicked",
                type = type,
                screen = screen,
                relation = PropertyValue.CUSTOMER,
                propertiesMap = PropertiesMap.create()
                    .add("variant", variant)
                    .add("merchant_id", getCurrentState().business?.id ?: "")
                    .add("account_id", getCurrentState().customer?.id ?: "")
            )
        }
    }

    private fun showSetupCollectionDialog() {
        collectionNavigator.get().showCustomerOnlineCollectionDialog(
            fragmentManager = childFragmentManager,
            canSetupLater = false,
            title = getString(R.string.setup_online_payments),
            description = getString(R.string.setup_collection_description),
            listener = object : CustomerOnlineEducationListener {
                override fun skipAndSend(dontAskAgain: Boolean) {
                }

                override fun setupNow(dontAskAgain: Boolean) {
                    showAddMerchantDestinationDialog("customer_set_due_date")
                }

                override fun onDismiss() {
                }
            }
        )
    }

    private fun showBuyerTxnAlert() {
        hideSoftKeyboard()
        val state = getCurrentState()
        findNavController(this).navigate(
            CustomerFragmentDirections.goToCustomerTxnAlertScreen(
                state.customer!!.id,
                state.customer.description,
                state.customer.mobile,
                state.customer.profileImage
            )
        )
    }

    private fun onReferralTransactionInitiated() {
        referralSignupTracker.get().trackTargetBannerInteracted("Banner Clicked")
        pushIntent(CustomerContract.Intent.HideTargetBanner)
    }

    private fun closeReferralTargetBanner() {
        referralSignupTracker.get().trackTargetBannerInteracted("Close Button")
        pushIntent(CustomerContract.Intent.CloseTargetBanner)
    }

    override fun onDestinationAddedSuccessfully() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            pushIntent(CustomerContract.Intent.GetPaymentOutLinkDetail)
        }
    }

    private fun gotoPaymentEditAmountScreen(
        linkId: String,
        paymentAddress: String,
        paymentType: String,
        paymentName: String,
        remainingDailyAmount: Long,
        maxDailyAmount: Long,
        riskType: String,
        kycStatus: KycStatus,
        kycRiskCategory: KycRiskCategory,
        futureAmountLimit: Long,
    ) {

        getCurrentState().let {
            val balance = it.customer?.balanceV2 ?: 0L
            val dueBalance = if (balance > 0) -balance else 0L
            paymentNavigator.get().gotoJuspayPaymentEditAmountScreen(
                requireActivity(),
                it.customer?.id ?: "",
                maxDailyAmount,
                remainingDailyAmount,
                dueBalance,
                it.business?.id ?: "",
                riskType,
                linkId,
                it.customer?.mobile ?: "",
                paymentAddress,
                paymentType,
                paymentName,
                LedgerType.CUSTOMER.value,
                kycStatus.value,
                kycRiskCategory.value,
                futureAmountLimit,
                this,
                supportType = getCurrentState().supportType.value,
                destinationUpdateAllowed = it.destinationUpdateAllowed,
            )
        }
    }

    override fun onEditDestinationClicked(supplierId: String) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            // delay is kept so that when edit amount bottom sheet will dismiss wont
            // clash with AddPaymentMethod bottom sheet
            delay(100L)
            showAddPaymentDestinationDialog()
        }
    }

    override fun onExitFromPaymentFlow(source: String) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            pushIntent(CustomerContract.Intent.OpenExitDialog(source))
        }
    }

    private fun openLimitReachedBottomSheet() {
        supplierNavigator.get().showPaymentLimitWarningBottomSheet(
            childFragmentManager
        )
    }

    private fun showAutoDueDateDialog(dueDate: DateTime) {
        val balance = getCurrentState().customer?.balanceV2 ?: 0
        if (autoDueDateDialog == null && balance < 0) {
            autoDueDateDialog = AutoDueDateDialog.newInstance(DateTimeUtils.getFormat2(requireContext(), dueDate))
            autoDueDateDialog?.setListener(object : AutoDueDateDialog.Listener {
                override fun onEditDueDate(action: String) {
                    dueDateType = "Auto"
                    tracker.get().trackEvents(
                        "Edit Auto Due Date",
                        screen = PropertyValue.RELATIONSHIP_SCREEN,
                        propertiesMap = PropertiesMap.create()
                            .add("merchant_id", getCurrentState().business?.id ?: "")
                            .add("account_id", customerId ?: "")
                            .add(PropertyKey.RELATION, PropertyValue.CUSTOMER)
                            .add("action", action)
                    )
                    pushIntentWithDelay(CustomerContract.Intent.ShowDueDatePickerIntent)
                }

                override fun onOkay() {
                    dueDateType = "Auto"
                    tracker.get().trackDueDateConfirmed(
                        "Due Relationship Screen",
                        dueDate.millis,
                        0,
                        customerId,
                        PropertyValue.CUSTOMER,
                        "New",
                        "",
                        dueDateType
                    )
                }

                override fun onDismissAutoDueDialog() {
                    pushIntent(CustomerContract.Intent.DisableAutoDueDateDialog())
                    autoDueDateDialog = null
                }

                override fun onDisplayed() {
                    tracker.get().trackEvents(
                        eventName = "InAppNotification Displayed",
                        type = "auto_due_date",
                        screen = "Due Relationship Screen",
                        propertiesMap = PropertiesMap.create()
                            .add("merchant_id", getCurrentState().business?.id ?: "")
                            .add("account_id", customerId ?: "")
                    )
                }
            })
            autoDueDateDialog?.show(childFragmentManager, AutoDueDateDialog.TAG)
        }
    }

    override fun onAddBankDetailsClicked() {
        showAddMerchantDestinationDialog("customer_add_bank_details")
    }

    override fun completeKyc() {
        legacyNavigator.get().goWebExperimentScreen(requireContext(), WebExperiment.Experiment.KYC.type)
    }

    fun onMerchantDestinationAdded() {
        pushIntent(CustomerContract.Intent.CollectionDestinationAdded)
    }

    override fun chatWithUs(amount: Long, paymentTime: String, txnId: String, status: String) {
        accountingEventTracker.get().trackCustomerSupportMsgClicked(
            source = SOURCE_LEDGER,
            txnId = txnId,
            amount = amount.toString(),
            relation = LedgerType.CUSTOMER.value.lowercase(),
            status = AccountingEventTracker.STATUS_PENDING,
            supportMsg = AccountingSharedUtils.getWhatsAppMsg(
                requireContext(),
                amount = CurrencyUtil.formatV2(amount),
                paymentTime = paymentTime,
                txnId = txnId,
                status = AccountingEventTracker.STATUS_PENDING,
            ),
            type = getCurrentState().supportType.value,
        )
        CustomerSupportOptionDialog
            .newInstance(
                amount = amount.toString(),
                paymentTime = paymentTime,
                txnId = txnId,
                status = status,
                accountId = getCurrentState().customer?.id ?: "",
                ledgerType = LedgerType.CUSTOMER.value.lowercase(),
                source = SOURCE_LEDGER,
            ).show(
                childFragmentManager,
                CustomerSupportOptionDialog.TAG
            )
    }

    override fun onPaymentTypeSelected(paymentType: PaymentType) {
        when (paymentType) {
            PaymentType.BLIND_PAY -> {
                eventTracker.get()
                    .trackPaymentOption(
                        accountId = getCurrentState().customer?.id ?: "",
                        getCurrentState().business?.id ?: "",
                        true
                    )
                pushIntent((CustomerContract.Intent.IsJuspayFeatureEnabled))
            }
            PaymentType.OTHERS -> {
                eventTracker.get()
                    .trackPaymentOption(
                        accountId = getCurrentState().customer?.id ?: "",
                        getCurrentState().business?.id ?: "",
                        false
                    )
                getCurrentState().customer?.let { showAddPaymentMethodDialog(it.id) }
            }
        }
    }

    internal fun showAddPaymentMethodDialog(customerId: String) {
        getCurrentState().customer?.let {
            paymentNavigator.get().gotoAddPaymentDestinationDialog(
                it.id, LedgerType.CUSTOMER.value,
                it.mobile ?: "",
                name = it.description,
                dueBalance = it.balanceV2,
                profileImage = it.profileImage ?: "",
                childFragmentManager,
                this,
                getCurrentState().isBlindPayEnabled,
            )
        }
    }

    override fun onRequestActionClicked(type: Int) {
        when (type) {
            0 -> {
                showAddMerchantDestinationDialog("customer_payment_intent")
            }
            1, 2 -> {
                val customerId = if (isStateInitialized()) getCurrentState().customer?.id ?: "" else ""
                eventTracker.get().trackContextualTriggerClicked(customerId, "")
                startActivity(
                    collectionNavigator.get().collectionBenefitsActivity(
                        context = requireContext(),
                        source = "collections_contextual_trigger",
                        sendReminder = false,
                        customerId = customerId,
                    )
                )
            }
        }
    }

    override fun onActionAcknowledgeClicked(type: Int) {
        if (type == 0) {
            pushIntent(CustomerContract.Intent.SendCollectionReminderClicked)
        }
    }

    override fun onMenuItemClicked(menu: MenuOptions) {
        onMenuClicked(menu)
    }
}
