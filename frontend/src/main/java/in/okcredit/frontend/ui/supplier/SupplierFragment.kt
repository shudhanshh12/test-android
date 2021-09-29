package `in`.okcredit.frontend.ui.supplier

import `in`.okcredit.analytics.*
import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.CollectionConstants
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.communication_inappnotification.contract.LocalInAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.ui.local.TapTargetLocal
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.analytics.SupplierEventTracker
import `in`.okcredit.frontend.databinding.SupplierFragmentBinding
import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.supplier.SupplierContract.*
import `in`.okcredit.frontend.ui.supplier.SupplierContract.Companion.KEY_TRANSACTION_CREATE_TIME
import `in`.okcredit.frontend.ui.supplier.views.DeleteTransactionViewModel_
import `in`.okcredit.frontend.ui.supplier.views.TransactionViewModel_
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.MenuOptionsBottomSheet
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.MenuOptionsBottomSheet.Companion.MenuOptions
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.MenuOptionsBottomSheet.MenuListener
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.model.MenuSheet
import `in`.okcredit.merchant.customer_ui.ui.customer.views.PrivacyView
import `in`.okcredit.merchant.customer_ui.ui.dialogs.BlockRelationShipDialogFragment
import `in`.okcredit.merchant.customer_ui.ui.dialogs.BlockedDialogFragment
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.use_case.GetSupplierScreenSortSelection
import `in`.okcredit.merchant.suppliercredit.use_case.GetSupplierScreenSortSelection.SupplierScreenSortSelection.BILL_DATE
import `in`.okcredit.payment.contract.AddPaymentDestinationListener
import `in`.okcredit.payment.contract.BlindPayListener
import `in`.okcredit.payment.contract.EditDestinationListener
import `in`.okcredit.payment.contract.PaymentNavigator
import `in`.okcredit.payment.contract.PaymentType
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.ScreenName
import `in`.okcredit.shared.utils.TimeUtils.toSeconds
import `in`.okcredit.shared.utils.addTo
import `in`.okcredit.shared.utils.exhaustive
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierPropertyValue.SUPPLIER
import `in`.okcredit.supplier.payment_process.SupplierPaymentDialogScreen
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
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
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.supplier_fragment.*
import kotlinx.android.synthetic.main.supplier_screen_toolbar.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.analytics.AccountingEventTracker.Companion.SOURCE_LEDGER
import merchant.okcredit.accounting.contract.model.LedgerType
import merchant.okcredit.accounting.ui.customer_support_exit_dialog.CustomerSupportExitDialog
import merchant.okcredit.accounting.ui.customer_support_option_dialog.CustomerSupportOptionDialog
import merchant.okcredit.accounting.ui.transaction_sort_options_dialog.TransactionsSortCriteriaSelectionBottomSheet
import merchant.okcredit.accounting.ui.transaction_sort_options_dialog.TransactionsSortCriteriaSelectionBottomSheet.TransactionsSortCriteriaSelectionListener
import merchant.okcredit.supplier.contract.SupplierPaymentListener
import tech.okcredit.account_chat_ui.chat_activity.ChatActivity
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.DimensionUtil
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.bill_management_ui.BillActivity
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.help.HelpActivity
import tech.okcredit.userSupport.ContextualHelp
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SupplierFragment :
    BaseFragment<State, ViewEvent, SupplierContract.Intent>(
        label = "SupplierScreen",
        contentLayoutId = R.layout.supplier_fragment
    ),
    SupplierControllerV2.SupplierControllerListener,
    merchant.okcredit.accounting.views.LoadMoreView.Listener,
    PrivacyView.Listener,
    BlockedDialogFragment.BlockedListener,
    MenuListener,
    EditDestinationListener,
    AddPaymentDestinationListener,
    BlindPayListener,
    TransactionsSortCriteriaSelectionListener {

    // intents
    private val transactionClicks: PublishSubject<Pair<String, Long>> = PublishSubject.create()
    private val callButtonClicks: PublishSubject<Unit> = PublishSubject.create()
    private val addMobileClicks: PublishSubject<Unit> = PublishSubject.create()
    private val shareAppPromotion: PublishSubject<Pair<Bitmap, String>> = PublishSubject.create()
    private val cusProfileClicks: PublishSubject<Unit> = PublishSubject.create()
    private val stopMediaPlayer: PublishSubject<Unit> = PublishSubject.create()
    private val expandTransactions: PublishSubject<Unit> = PublishSubject.create()
    private val privacyClicks: PublishSubject<Unit> = PublishSubject.create()

    private var unblockDialogSubject = PublishSubject.create<Unit>()
    private var unblockSubject = PublishSubject.create<Unit>()
    private var alert: Snackbar? = null
    private var mediaPlayer: MediaPlayer? = MediaPlayer()
    private var isPlayedSound: Boolean = false
    private val newShareReportPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    private var progressDialog: ProgressDialog? = null
    private val showPayOnlineEducationPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    private var supplierLearnMoreSubject = PublishSubject.create<Unit>()
    private var isPayOnlineEducationShownOnce: Boolean = false
    private var isPayOnlineEducationShown: Boolean = false
    private var isRedirectedToPayment = false
    private var shouldScrollToAddedTransaction = false

    private lateinit var supplierController: SupplierControllerV2

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var imageLoader: Lazy<IImageLoader>

    @Inject
    internal lateinit var tracker: Tracker

    @Inject
    internal lateinit var supplierEventTracker: Lazy<SupplierEventTracker>

    @Inject
    lateinit var accountingEventTracker: Lazy<AccountingEventTracker>

    @Inject
    lateinit var paymentNavigator: Lazy<PaymentNavigator>

    @Inject
    lateinit var localInAppNotificationHandler: Lazy<LocalInAppNotificationHandler>

    private var mMobile: String? = null
    internal var supplierId: String? = null

    private var menuOptions = mutableListOf<MenuOptions>()

    companion object {
        const val ARG_SCREEN_REDIRECT_TO_PAYMENT = "redirect_to_payment"
    }

    private var canShowTakeGiveCreditEducation = false

    private val binding: SupplierFragmentBinding by viewLifecycleScoped(SupplierFragmentBinding::bind)

    private var takeGiveCreditTimer = object : CountDownTimer(1000, 1000) {
        override fun onFinish() {
            canShowTakeGiveCreditEducation = true
            pushIntent(SupplierContract.Intent.SetTakeGiveCreditEducation(true))
        }

        override fun onTick(millisUntilFinished: Long) {
        }
    }.start()

    private val dataObserver by lazy {
        object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (shouldScrollToAddedTransaction.not()) {
                    binding.recyclerView.scrollToPosition(supplierController.adapter.itemCount - 1)
                }
            }
        }
    }

    override fun onStop() {
        binding.recyclerView.clearOnScrollListeners()
        super.onStop()
    }

    override fun onDestroyView() {
        supplierController.removeListener()
        supplierController.adapter.unregisterAdapterDataObserver(dataObserver)
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.supplierScreenToolbar.root.contextual_help.initDependencies(
            screenName = ScreenName.SupplierScreen.value,
            tracker = tracker,
            legacyNavigator = legacyNavigator.get()
        )
        initRecyclerView()
        binding.shareReport.setOnClickListener {
            newShareReportPublishSubject.onNext(Unit)
        }

        binding.supplierScreenToolbar.rootChatContainer.setOnClickListener {
            redirectToChatScreen()
        }
        binding.learnMore.setOnClickListener {
            trackLearnMoreClicked("Relationship", "Centre")
            supplierLearnMoreSubject.onNext(Unit)
        }

        binding.supplierScreenToolbar.root.contextual_help.visibilitySubject.distinctUntilChanged().subscribe {
            if (it) {
                menuOptions.addIfNotExists(MenuOptions.Help)
            } else {
                menuOptions.remove(MenuOptions.Help)
            }
            setToolBarIcons()
        }.addTo(autoDisposable)

        binding.supplierScreenToolbar.rootBillContianer.setOnClickListener {
            tracker.trackAddTransactionFlowsStarted(
                PropertyKey.BILL,
                PropertyValue.SUPPLIER,
                supplierId,
                source = Screen.SUPPLIER_SCREEN
            )
            activity?.let {
                val state = getCurrentState()
                tracker.trackBillIconClicked(
                    accountId = supplierId,
                    relation = PropertyValue.SUPPLIER,
                    unreadBillCount = getCurrentState().unreadBillCount,
                    totalBillCount = getCurrentState().totalBills,
                    screen = "Relationship"
                )
                startActivity(
                    BillActivity.getIntent(
                        it,
                        state.supplier!!.id,
                        BILL_INTENT_EXTRAS.SUPPLIER,
                        getCurrentState().supplier?.name
                    )

                )
            }
        }
        binding.rootView.setTracker(performanceTracker)

        binding.supplierScreenToolbar.root.call_toolbar.setOnClickListener {
            callButtonClicks.onNext(Unit)
        }

        binding.supplierScreenToolbar.root.profile_image.setOnClickListener {
            tracker.trackViewProfile(
                PropertyValue.RELATION_PAGE,
                PropertyValue.SUPPLIER,
                PropertyValue.CUSTOMER,
                supplierId
            )
            cusProfileClicks.onNext(Unit)
        }
        binding.supplierScreenToolbar.root.profile_name.setOnClickListener {
            tracker.trackViewProfile(
                PropertyValue.RELATION_PAGE,
                PropertyValue.SUPPLIER,
                PropertyValue.CUSTOMER,
                supplierId
            )
            cusProfileClicks.onNext(Unit)
        }
        binding.supplierScreenToolbar.root.menu.setOnClickListener {
            openMenuBottomSheet()
        }
        binding.tvTransactionsSortSelection.setOnClickListener {
            // Disable item animation *momentarily* as it causes IndexOutOfBoundsException when sort selection is changed
            // https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/recyclerview/recyclerview/src/main/java/androidx/recyclerview/widget/RecyclerView.java#6304
            binding.recyclerView.itemAnimator = null
            openTransactionSortSelectionOptionsBottomSheet()
        }

        binding.btnScrollToBottom.setOnClickListener {
            scrollRecyclerViewToBottom()
        }

        observeNewTransactionId()
    }

    private fun observeNewTransactionId() {
        getNavigationResultLiveData(KEY_TRANSACTION_CREATE_TIME)?.observe(viewLifecycleOwner) { transactionCreateTime ->
            if (
                transactionCreateTime is Long &&
                isStateInitialized() &&
                getCurrentState().transactionSortSelection == BILL_DATE
            ) {
                lifecycleScope.launchWhenResumed {
                    shouldScrollToAddedTransaction = true
                    delay(500)
                    scrollToAddedTransaction(transactionCreateTime)
                }
            }
        }
    }

    private fun scrollToAddedTransaction(newTransactionCreateTime: Long) {
        val models = supplierController.adapter.copyOfModels
        models.forEachIndexed { index, model ->
            if (model !is TransactionViewModel_) return@forEachIndexed // Early return if model is not TransactionView

            val transactionCreateTime = model.data().createTime?.millis?.toSeconds()
            if (transactionCreateTime == newTransactionCreateTime.toSeconds()) {
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

    private fun scrollRecyclerViewToBottom() {
        binding.btnScrollToBottom.gone()
        binding.recyclerView.scrollToPosition(supplierController.adapter.itemCount - 1)
        binding.totalAmountContainer.visible()
        handleTransactionsSortSelectionUi(getCurrentState())
        accountingEventTracker.get().trackScrollToBottomClicked(SUPPLIER, Screen.SUPPLIER_SCREEN)
    }

    private fun openTransactionSortSelectionOptionsBottomSheet() {
        accountingEventTracker.get().trackSortByClicked(SUPPLIER)
        val sortSelection = getCurrentState().transactionSortSelection?.value
        val bottomSheet = TransactionsSortCriteriaSelectionBottomSheet.newInstance(sortSelection, this)
        bottomSheet.show(requireActivity().supportFragmentManager, TransactionsSortCriteriaSelectionBottomSheet.TAG)
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            supplierController = SupplierControllerV2(tracker, performanceTracker, accountingEventTracker)
            supplierController.addListener(this@SupplierFragment)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = supplierController.adapter
            setHasFixedSize(true)
            val epoxyVisibilityTracker = EpoxyVisibilityTracker()
            epoxyVisibilityTracker.attach(this)

            supplierController.adapter.registerAdapterDataObserver(dataObserver)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        val currentState = getCurrentState()
                        if (isStateInitialized() && currentState.transactions.isNotEmpty()) {
                            binding.totalAmountContainer.visible()
                            handleTransactionsSortSelectionUi(currentState)
                        }
                    } else if (dy < 0) {
                        binding.totalAmountContainer.gone()
                        setSortTransactionsByVisibility(false)
                    }

                    if (isScrolledToBottom(recyclerView)) {
                        binding.btnScrollToBottom.gone()
                    }
                }
            })
        }
    }

    internal fun isScrolledToBottom(recyclerView: RecyclerView): Boolean {
        return !recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN)
    }

    private fun redirectToChatScreen() {
        tracker.trackChatIconClicked(
            supplierId,
            PropertyValue.SUPPLIER,
            getCurrentState().unreadMessageCount,
            "Relationship"
        )
        getCurrentState().supplier?.let {
            if (it.mobile.isNullOrEmpty()) {
                longToast(R.string.add_number_of_user_for_chat)
            } else {
                activity?.let {
                    val state = getCurrentState()
                    startActivity(
                        ChatActivity.getIntent(
                            requireActivity(),
                            state.supplier!!.id,
                            "BUYER",
                            getCurrentState().unreadMessageCount,
                            getCurrentState().firstUnseenMessageId
                        )

                    )
                }
            }
        }
    }

    private fun setToolBarIcons() {
        if (menuOptions.size > 3) {
            if (menuOptions.contains(MenuOptions.Call)) {
                binding.supplierScreenToolbar.root.call_toolbar.visible()
            } else binding.supplierScreenToolbar.root.call_toolbar.gone()
            if (menuOptions.contains(MenuOptions.CustomerStatements)) {
                binding.supplierScreenToolbar.root.supplier_statements.visible()
            } else binding.supplierScreenToolbar.root.supplier_statements.gone()
            binding.supplierScreenToolbar.root.menu.visible()
            binding.supplierScreenToolbar.root.qr_code.gone()
            binding.supplierScreenToolbar.root.contextual_help.gone()
        } else {
            binding.supplierScreenToolbar.root.menu.gone()
            if (menuOptions.contains(MenuOptions.Call)) {
                binding.supplierScreenToolbar.root.call_toolbar.visible()
            } else {
                binding.supplierScreenToolbar.root.call_toolbar.gone()
            }
            if (menuOptions.contains(MenuOptions.CustomerStatements)) {
                binding.supplierScreenToolbar.root.supplier_statements.visible()
            } else {
                binding.supplierScreenToolbar.root.supplier_statements.gone()
            }
            if (menuOptions.contains(MenuOptions.QrCode)) {
                binding.supplierScreenToolbar.root.qr_code.visible()
            } else {
                binding.supplierScreenToolbar.root.qr_code.gone()
            }
            if (menuOptions.contains(MenuOptions.Help)) {
                binding.supplierScreenToolbar.root.contextual_help.visible()
            } else {
                binding.supplierScreenToolbar.root.contextual_help.gone()
            }
        }
    }

    private fun isSupplierDestinationPresent(collectionProfile: CollectionCustomerProfile?) =
        !collectionProfile?.message_link.isNullOrBlank() && !collectionProfile?.paymentAddress.isNullOrBlank()

    private fun showSupplierDestinationDialog(supplierId: String) {
        getCurrentState().let {
            val dialog = SupplierPaymentDialogScreen.newInstance(
                supplierId, it.supplier?.mobile ?: "",
                it.supplier?.balance ?: 0L,
                it.collectionCustomerProfile?.type ?: "",
                it.collectionCustomerProfile?.message_link ?: "",
                it.collectionCustomerProfile?.paymentAddress ?: "",
                it.collectionCustomerProfile?.name ?: "",
                LedgerType.SUPPLIER.value

            )
            dialog.setListener(object : SupplierPaymentListener {
                override fun onChangeDetails(dialog: AppCompatDialogFragment) {
                    dialog.dismiss()
                    showAddPaymentMethodDialog(supplierId)
                }

                override fun onConfirm(messageLink: String, dialog: AppCompatDialogFragment) {
                    dialog.dismiss()
                    goToWebActivity()
                }
            })
            dialog.show(childFragmentManager, SupplierPaymentDialogScreen.TAG)
        }
    }

    internal fun showAddPaymentMethodDialog(supplierId: String) {
        getCurrentState().let {
            paymentNavigator.get().gotoAddPaymentDestinationDialog(
                supplierId, LedgerType.SUPPLIER.value,
                it.supplier?.mobile ?: "",
                name = it.supplier?.name ?: "",
                dueBalance = it.supplier?.balance ?: 0L,
                profileImage = it.supplier?.profileImage ?: "",
                childFragmentManager,
                this,
                it.isBlindPayEnabled,
            )
        }
    }

    internal fun showBlindPayDialog() {
        paymentNavigator.get().gotoBlindPayDialog(
            listener = this,
            childFragmentManager = childFragmentManager,
            ledgerType = LedgerType.SUPPLIER.value.lowercase(),
            accountId = getCurrentState().supplier?.id.toString(),
        )
    }

    private fun openMenuBottomSheet() {
        val menuDialog = MenuOptionsBottomSheet.newInstance()
        if (!menuDialog.isVisible) {
            val bundle = Bundle()
            bundle.putParcelable(MenuOptionsBottomSheet.MENU_PARCEL, MenuSheet(menuOptions))
            menuDialog.arguments = bundle
            menuDialog.show(requireActivity().supportFragmentManager, MenuOptionsBottomSheet.TAG)
            menuDialog.initialise(this@SupplierFragment)
        }
    }

    override fun loadIntent(): UserIntent {
        return SupplierContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        resetTimer()
        return Observable.mergeArray(
            Observable.just(SupplierContract.Intent.Reload),

            binding.sendOrReceivePaymentAb.addPaymentBtnAb.clicks()
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    tracker.trackAddTransactionFlowsStarted(
                        PropertyKey.CREDIT,
                        PropertyValue.SUPPLIER,
                        supplierId,
                        source = Screen.SUPPLIER_SCREEN
                    )
                    SupplierContract.Intent.GoToAddTxn(merchant.okcredit.accounting.model.Transaction.CREDIT)
                },
            binding.sendOrReceivePaymentAb.addCreditBtnAb.clicks()
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    tracker.trackAddTransactionFlowsStarted(
                        PropertyKey.PAYMENT,
                        PropertyValue.SUPPLIER,
                        supplierId,
                        source = Screen.SUPPLIER_SCREEN
                    )
                    SupplierContract.Intent.GoToAddTxn(merchant.okcredit.accounting.model.Transaction.PAYMENT)
                },

            // transaction click intent
            transactionClicks
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    SupplierContract.Intent.ViewTransaction(it.first, it.second)
                },

            // supplier profile click intent
            cusProfileClicks
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map { SupplierContract.Intent.GoToSupplierProfile },

            // call supplier click intent
            callButtonClicks
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map { SupplierContract.Intent.GoToPhoneDialer },

            // add supplier mobile click intent
            addMobileClicks
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map { SupplierContract.Intent.AddMobile },

            // stop media player
            stopMediaPlayer
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map { SupplierContract.Intent.StopMediaPlayer },

            // expand txns click intent
            expandTransactions
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { SupplierContract.Intent.ExpandTransactions },

            // expand txns click intent
            privacyClicks
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map { SupplierContract.Intent.GoToPrivacyScreen },

            unblockDialogSubject
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    SupplierContract.Intent.ShowUnblockDialog
                },
            unblockSubject
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    SupplierContract.Intent.Unblock
                },
            shareAppPromotion
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map {
                    SupplierContract.Intent.ShareAppPromotion(it.first, it.second)
                },
            showPayOnlineEducationPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    SupplierContract.Intent.ShowPayOnlineEducation
                },
            supplierLearnMoreSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    SupplierContract.Intent.SupplierLearnMore
                },
            newShareReportPublishSubject.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    SupplierContract.Intent.NewShareReport
                },
            binding.payOnline.clicks()
                .map {
                    SupplierContract.Intent.PayOnline
                }
        )
    }

    private fun resetTimer() {
        canShowTakeGiveCreditEducation = false
        takeGiveCreditTimer.cancel()
        takeGiveCreditTimer.start()
        pushIntent(SupplierContract.Intent.SetTakeGiveCreditEducation(false))
    }

    @SuppressLint("CheckResult")
    @AddTrace(name = Traces.RENDER_SUPPLIER)
    override fun render(state: State) {
        binding.supplierScreenToolbar.root.contextual_help.setContextualHelpIds(state.contextualHelpIds)

        if (state.collectionCustomerProfile != null && state.redirectToPayment && !isRedirectedToPayment) {
            isRedirectedToPayment = true
            pushIntent(SupplierContract.Intent.PayOnline)
        }

        // Set toolbar info
        setToolbarInfo(state)

        mMobile = state.supplier?.mobile
        supplierId = state.supplier?.id

        binding.cashbackMessageTextView.gone()

        supplierController.setData(state.supplierScreenList)

        renderTransactionDetails(state)

        if (state.supplier?.mobile.isNullOrEmpty().not()) {
            menuOptions.addIfNotExists(MenuOptions.Call)
        } else {
            if (menuOptions.contains(MenuOptions.Call))
                menuOptions.remove(MenuOptions.Call)
        }

        handlingNetWork(state)

        if (state.playSound) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        mediaPlayer = MediaPlayer.create(context, R.raw.tx_add_sound)
                    }
                    if (!isPlayedSound) {
                        isPlayedSound = true
                        mediaPlayer?.start()
                    }
                    mediaPlayer?.setOnCompletionListener {
                        it.release()
                        stopMediaPlayer.onNext(Unit)
                        isPlayedSound = false
                    }
                } catch (exception: Exception) {
                }
            }
        }
        if (state.supplier?.name.isNullOrEmpty().not() && state.supplier?.mobile.isNullOrEmpty()) {

            binding.supplierAccountText.text =
                Html.fromHtml(getString(R.string.supplier_add_number, state.supplier!!.name))
            binding.supplierAccountImg.setImageDrawable(
                resources.getDrawable(
                    R.drawable.ic_filled_phone,
                    null
                )
            )
            binding.supplierAccountText.setOnClickListener {
                onAddPhoneClicked()
            }
            binding.supplierAccountImg.setOnClickListener {
                onAddPhoneClicked()
            }
            binding.registeredSupplierContainer.visible()
            binding.commonLedgerDivider.visible()
            binding.registeredSupplierContainer.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.green_lite
                )
            )
        } else {
            when {
                state.supplier?.registered == true -> {
                    if (state.supplier.addTransactionRestricted) {
                        binding.supplierAccountText.text =
                            getString(R.string.transaction_edit_permission, state.supplier.name)
                        binding.supplierAccountImg.setImageDrawable(
                            resources.getDrawable(
                                R.drawable.ic_add_transaction_disabled_notify,
                                null
                            )
                        )
                        binding.supplierAccountText.isClickable = false
                        binding.supplierAccountImg.isClickable = false
                    } else {
                        binding.supplierAccountText.text =
                            Html.fromHtml(getString(R.string.common_ledger_text_supplier_know_more))
                        binding.supplierAccountImg.setImageDrawable(
                            resources.getDrawable(
                                R.drawable.ic_common_ledger,
                                null
                            )
                        )
                        binding.supplierAccountText.setOnClickListener {
                            HelpActivity.start(
                                requireContext(),
                                ScreenName.SupplierScreen.value,
                                ContextualHelp.COMMON_LEDGER

                            )
                        }
                        binding.supplierAccountImg.setOnClickListener {
                            HelpActivity.start(
                                requireContext(),
                                ScreenName.SupplierScreen.value,
                                ContextualHelp.COMMON_LEDGER
                            )
                        }
                    }
                    binding.registeredSupplierContainer.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.orange_lite
                        )
                    )
                    binding.registeredSupplierContainer.visible()
                    binding.commonLedgerDivider.visible()
                }
                state.canShowAppPromotion -> {
                    binding.supplierAccountText.text =
                        Html.fromHtml(getString(R.string.supplier_share_app))
                    binding.supplierAccountImg.setImageDrawable(
                        resources.getDrawable(
                            R.drawable.ic_whatsapp_green,
                            null
                        )
                    )
                    binding.supplierAccountImg.setOnClickListener {
                        takeScreenshot()
                    }
                    binding.supplierAccountText.setOnClickListener {
                        takeScreenshot()
                    }
                    binding.registeredSupplierContainer.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.indigo_lite
                        )
                    )
                    binding.registeredSupplierContainer.visible()
                    binding.commonLedgerDivider.visible()
                }
                else -> {
                    binding.supplierAccountText.text =
                        Html.fromHtml(getString(R.string.manage_supplier))
                    binding.supplierAccountImg.setImageDrawable(
                        resources.getDrawable(
                            R.drawable.ic_help_outline,
                            null
                        )
                    )
                    binding.supplierAccountImg.setOnClickListener {
                        trackLearnMoreClicked("Relationship", "Top")
                        supplierLearnMoreSubject.onNext(Unit)
                    }
                    binding.supplierAccountText.setOnClickListener {
                        trackLearnMoreClicked("Relationship", "Top")
                        supplierLearnMoreSubject.onNext(Unit)
                    }
                    binding.registeredSupplierContainer.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.indigo_lite
                        )
                    )
                    binding.registeredSupplierContainer.visible()
                    binding.commonLedgerDivider.visible()
                }
            }
        }

        if (state.isPayOnlineEducationShown == false && isPayOnlineEducationShown.not()) {
            isPayOnlineEducationShown = true
            showPayOnlineEducationToMerchantsFromCampaign()
        }

        if (state.showTakeCreditPaymentEducation && canShowTakeGiveCreditEducation && state.supplier != null && !state.supplier.addTransactionRestricted) {
            canShowTakeGiveCreditEducation = false
            showTakeCreditPaymentEducation()
            pushIntent(
                SupplierContract.Intent.RxPreferenceBoolean(
                    RxSharedPrefValues.SHOULD_SHOW_TAKE_CREDIT_PAYMENT_EDUCATION,
                    false,
                    Scope.Individual
                )
            )
        }

        if (canShowPayOnline(state) && isPayOnlineEducationShownOnce.not()) {
            isPayOnlineEducationShownOnce = true
            showPayOnlineEducationPublishSubject.onNext(Unit)
        }

        if (state.showSupplierStatementLoader) {
            if (progressDialog == null) {
                progressDialog = ProgressDialog.show(
                    context, "",
                    getString(
                        R.string
                            .account_report_loading
                    )
                )
            } else {
                progressDialog?.show()
            }
        } else {
            progressDialog?.dismiss()
        }
        if (state.isBlocked) {
            showBlockedDialog()
        } else {
            binding.blockContainer.gone()
        }

        showAlertForErrors(state)

        setToolBarIcons()

        binding.emptyContainer.isVisible = state.transactions.isEmpty()

        if (state.isChatEnabled) {
            binding.supplierScreenToolbar.rootChatContainer.visible()
            when {
                state.unreadMessageCount.isNotEmpty() -> {
                    binding.supplierScreenToolbar.unreadContianer.visible()
                    binding.supplierScreenToolbar.newContianer.gone()
                    binding.supplierScreenToolbar.unreadCount.visible()
                    binding.supplierScreenToolbar.unreadCount.text = state.unreadMessageCount
                }
                state.canShowChatNewSticker -> {
                    binding.supplierScreenToolbar.unreadContianer.gone()
                    binding.supplierScreenToolbar.newContianer.visible()
                }
                else -> {
                    binding.supplierScreenToolbar.unreadContianer.gone()
                }
            }
        } else {
            binding.supplierScreenToolbar.rootChatContainer.gone()
        }

        if (state.isBillEnabled) {
            binding.supplierScreenToolbar.profileImage.gone()
            binding.supplierScreenToolbar.rootBillContianer.visible()
            when {
                state.unreadBillCount != 0 -> {
                    binding.supplierScreenToolbar.unreadBillContianer.visible()
                    binding.supplierScreenToolbar.newBillContianer.gone()
                    binding.supplierScreenToolbar.unreadBillCount.visible()
                    binding.supplierScreenToolbar.unreadBillCount.text = state.unreadBillCount.toString()
                }
                state.canShowBillNewSticker -> {
                    binding.supplierScreenToolbar.unreadBillContianer.gone()
                    binding.supplierScreenToolbar.newBillContianer.visible()
                }
                else -> {
                    binding.supplierScreenToolbar.unreadBillContianer.gone()
                }
            }
        } else {
            binding.supplierScreenToolbar.profileImage.visible()
            binding.supplierScreenToolbar.rootBillContianer.gone()
        }

        addTransactionsRestricted(state)

        performCreditPaymentLayoutActions(state)
        handlePreNetworkOnboardingBanner(state)
        handleTransactionsSortSelectionUi(state)
    }

    internal fun handleTransactionsSortSelectionUi(state: State) {
        setSortTransactionsByVisibility(state.showTransactionSortSelection)
        if (state.showTransactionSortSelection) {
            val sortBy = when (state.transactionSortSelection) {
                GetSupplierScreenSortSelection.SupplierScreenSortSelection.CREATE_DATE -> getString(R.string.t_001_filter_sort_by_create_date)
                else -> getString(R.string.t_001_filter_sort_by_bill_date)
            }
            binding.tvTransactionsSortSelection.text = sortBy

            // Set recyclerview padding to avoid overlap
            binding.recyclerView.updatePadding(top = dpToPixel(56f).toInt())
            binding.recyclerView.clipToPadding = false
        }
    }

    internal fun setSortTransactionsByVisibility(visible: Boolean) {
        binding.tvTransactionsSortSelection.isVisible = visible
        binding.tvTransactionsSortSelectionLabel.isVisible = visible
    }

    private fun handlePreNetworkOnboardingBanner(state: State) {
        binding.preNetworkBanner.isVisible = state.showPreNetworkWarningBanner
    }

    private fun handleCashbackMessage(state: State) {
        binding.cashbackMessageTextView.apply {
            if (state.cashbackMessage.isNotNullOrBlank()) {
                text = state.cashbackMessage
                visible()
            } else {
                gone()
            }
        }
    }

    private fun performCreditPaymentLayoutActions(state: State) {
        if (binding.bottomButtonContainer.isVisible()) {
            send_or_receive_payment_ab.visible()
        }
        addTransactionsRestricted(state)
    }

    private fun trackLearnMoreClicked(source: String, position: String) {
        val listStatus = if (getCurrentState().transactions.isEmpty()) "Empty" else "Non Empty"
        tracker.trackSupplierLearnMore(
            supplierId,
            "Supplier",
            source,
            listStatus,
            position
        )
    }

    private fun handlingNetWork(state: State) {
        if (state.networkError) {
            binding.bottomButtonContainer.gone()
            view?.snackbar(
                getString(R.string.home_no_internet_msg),
                Snackbar.LENGTH_LONG
            )
        } else {
            binding.bottomButtonContainer.visible()
        }
    }

    private fun addTransactionsRestricted(state: State) {
        state.supplier?.let {
            if (it.addTransactionRestricted) {
                binding.bottomButtonContainer.gone()
                val l: ViewGroup.MarginLayoutParams =
                    binding.totalAmountContainer.layoutParams as ViewGroup.MarginLayoutParams
                l.setMargins(
                    DimensionUtil.dp2px(requireContext(), 16f).toInt(),
                    0,
                    DimensionUtil.dp2px(requireContext(), 16.0f).toInt(),
                    DimensionUtil.dp2px(requireContext(), 21f).toInt()
                )
                binding.totalAmountContainer.requestLayout()
                val lp: ViewGroup.MarginLayoutParams =
                    binding.recyclerView.layoutParams as ViewGroup.MarginLayoutParams
                lp.bottomMargin = DimensionUtil.dp2px(requireContext(), 40f).toInt()
                binding.recyclerView.layoutParams = lp
            } else {
                binding.bottomButtonContainer.visible()
            }
            showActionContainer(state)
        }
    }

    private fun renderTransactionDetails(state: State) {
        if (state.transactions.isNotEmpty()) {
            val totalAmount: Long = if (state.supplier?.balance == null) 0L else state.supplier.balance

            CurrencyUtil.renderV2(totalAmount, binding.total, 0)

            if (totalAmount >= 0) {
                binding.balanceText.text = requireContext().resources.getString(R.string.balance_advance)
            } else {
                binding.balanceText.text = requireContext().resources.getString(R.string.balance_due)
            }
            binding.totalAmountContainer.visible()

            binding.recyclerView.setPadding(0, 0, 0, DimensionUtil.dp2px(requireContext(), 60.0f).toInt())
            if (shouldAddPadding(state)) {
                binding.recyclerView.setPadding(0, 0, 0, DimensionUtil.dp2px(requireContext(), 200.0f).toInt())
            }
        } else {
            binding.totalAmountContainer.gone()
        }
    }

    private fun shouldAddPadding(state: State) =
        (state.supplier?.mobile.isNullOrEmpty().not() || canShowPayOnline(state))

    private fun showActionContainer(
        state: State,
    ) {
        // for in app notification pay_online button should not visible when parent container visibility is gone
        // By default pay_online visibility is kept gone and it should be visible only in-case txn are not empty
        if (state.transactions.isNullOrEmpty()) return
        if (canShowPayOnline(state)) {
            binding.shareReport.visible()
            setPayOnlineUi(state)
            binding.actionContainer.visible()
            binding.shareReport.setBackgroundColor(resources.getColor(R.color.white))
            binding.shareReport.setTextColor(resources.getColor(R.color.grey900))
            binding.shareReport.setStrokeColorResource(R.color.grey400)
        } else {
            binding.shareReport.visible()
            binding.actionContainer.visible()
            binding.payOnline.gone()
            binding.shareReport.setBackgroundColor(resources.getColor(R.color.green_primary))
            binding.shareReport.setTextColor(resources.getColor(R.color.white))
            binding.shareReport.setStrokeColorResource(R.color.green_primary)
        }
    }

    private fun setPayOnlineUi(state: State) {
        binding.apply {
            // TODO: clean this up later, not an ideal solution
            if (!payOnline.isVisible) {
                tracker.trackPayOnlinePageView(
                    accountId = supplierId,
                    screen = PropertyValue.RELATIONSHIP,
                    type = PropertyValue.ONLINE_PAYMENT,
                    relation = SupplierAnalyticsEvents.SupplierPropertyValue.SUPPLIER,
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
                ivPayOnlineLoading.clearAnimation()
                ivPayOnlineLoading.gone()
                payOnline.text = getString(R.string.pay_online)
            }
        }
    }

    private fun canShowPayOnline(state: State): Boolean {
        return if (state.isMerchantFromCollectionCampaign) {
            isSupplierDestinationPresent(state.collectionCustomerProfile)
        } else {
            true
        }
    }

    private fun showAlertForErrors(state: State) {
        if (state.error or state.isAlertVisible) {
            alert = when {
                state.isAlertVisible -> view?.snackbar(state.alertMessage, Snackbar.LENGTH_LONG)
                state.error -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_LONG)
                else -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_LONG)
            }
            alert?.show()
        }
    }

    private fun showBlockedDialog() {
        var blockDialog = childFragmentManager.findFragmentByTag(BlockedDialogFragment.TAG)
        if (blockDialog != null && blockDialog is BlockedDialogFragment) {
            blockDialog.render()
            binding.blockContainer.visible()
            return
        }
        blockDialog = if (getCurrentState().supplier != null && getCurrentState().supplier!!.blockedBySupplier) {
            BlockedDialogFragment.newInstance(
                BlockedDialogFragment.SCREEN_SUPPLIER,
                BlockedDialogFragment.TYPE_BLOCKED_BY
            )
        } else {
            BlockedDialogFragment.newInstance(
                BlockedDialogFragment.SCREEN_SUPPLIER,
                BlockedDialogFragment.TYPE_BLOCKED
            )
        }

        blockDialog.setListener(this)
        childFragmentManager.beginTransaction().replace(R.id.block_container, blockDialog).commit()
        block_container.visible()
        block_container.setOnClickListener {
            // blocks access to transaction
        }
    }

    @UiThread
    private fun showUnblockDialog() {
        val supplier = getCurrentState().supplier
        if (supplier != null) {
            val unblockDialogFragment = BlockRelationShipDialogFragment.newInstance(
                BlockRelationShipDialogFragment.SCREEN_SUPPLIER,
                BlockRelationShipDialogFragment.TYPE_UNBLOCK,
                supplier.name,
                supplier.profileImage,
                supplier.mobile
            )
            unblockDialogFragment.show(requireFragmentManager(), BlockRelationShipDialogFragment.TAG)
            unblockDialogFragment.setListener(object : BlockRelationShipDialogFragment.Listener {
                override fun onAction(action: String) {
                    if (action == getString(R.string.unblock)) {
                        unblockSubject.onNext(Unit)
                    }
                }
            })
        }
    }

    private fun showPayOnlineEducation() {
        supplierEventTracker.get().trackInAppNotificationDisplayed(
            PropertyValue.SUPPLIER_SCREEN, SupplierEventTracker.PAY_ONLINE_REMINDER_EDUCATION
        )

        lifecycleScope.launch {
            localInAppNotificationHandler.get()
                .generateTapTarget(
                    weakScreen = WeakReference(requireActivity()),
                    tapTarget = TapTargetLocal(
                        screenName = label,
                        targetView = WeakReference(binding.payOnline),
                        title = getString(R.string.pay_online_education_text),
                        titleGravity = Gravity.END,
                        titleTextSize = 16f,
                        padding = 22f,
                        listener = { _, state ->
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                // Add Events
                                pushIntent(
                                    SupplierContract.Intent.RxPreferenceBoolean(
                                        RxSharedPrefValues.IS_PAY_ONLINE_EDUCATION_SHOWN,
                                        true,
                                        Scope.Individual
                                    )
                                )
                                supplierEventTracker.get().trackInAppNotificationClicked(
                                    PropertyValue.SUPPLIER_SCREEN, SupplierEventTracker.PAY_ONLINE_REMINDER_EDUCATION, true
                                )
                            } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                                // Add Events
                                supplierEventTracker.get().trackInAppNotificationClicked(
                                    PropertyValue.SUPPLIER_SCREEN, SupplierEventTracker.PAY_ONLINE_REMINDER_EDUCATION, false
                                )
                                pushIntent(
                                    SupplierContract.Intent.RxPreferenceBoolean(
                                        RxSharedPrefValues.IS_PAY_ONLINE_EDUCATION_SHOWN,
                                        true,
                                        Scope.Individual
                                    )
                                )
                            }
                        }
                    )
                )
        }
    }

    private fun showPayOnlineEducationToMerchantsFromCampaign() {
        Observable.timer(300, TimeUnit.MILLISECONDS)
            .subscribeOn(ThreadUtils.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showCampaignEducationWithDelay() }
            .addTo(autoDisposable)
    }

    private fun showCampaignEducationWithDelay() {
        val state = if (isStateInitialized()) getCurrentState() else null
        if (state != null && (canShowPayOnline(state))) {
            supplierEventTracker.get().trackInAppNotificationDisplayed(
                PropertyValue.SUPPLIER_SCREEN, SupplierEventTracker.FIRST_SUPPLIER_CUSTOMER
            )
            lifecycleScope.launch {
                localInAppNotificationHandler.get()
                    .generateTapTarget(
                        weakScreen = WeakReference(requireActivity()),
                        tapTarget = TapTargetLocal(
                            screenName = label,
                            targetView = WeakReference(pay_online),
                            title = getString(R.string.now_you_can_securely),
                            titleGravity = Gravity.END,
                            titleTextSize = 16f,
                            padding = 22f,
                            listener = { _, state ->
                                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                    // Add Events
                                    pushIntent(
                                        SupplierContract.Intent.RxPreferenceBoolean(
                                            RxSharedPrefValues.IS_PAY_ONLINE_EDUCATION_SHOWN_FOR_CAMPAIGN,
                                            true,
                                            Scope.Individual
                                        )
                                    )
                                    supplierEventTracker.get().trackInAppNotificationClicked(
                                        PropertyValue.SUPPLIER_SCREEN, SupplierEventTracker.FIRST_SUPPLIER_CUSTOMER,
                                        true
                                    )
                                } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                                    // Add Events
                                    pushIntent(
                                        SupplierContract.Intent.RxPreferenceBoolean(
                                            RxSharedPrefValues.IS_PAY_ONLINE_EDUCATION_SHOWN_FOR_CAMPAIGN,
                                            true,
                                            Scope.Individual
                                        )
                                    )
                                    supplierEventTracker.get().trackInAppNotificationClicked(
                                        PropertyValue.SUPPLIER_SCREEN, SupplierEventTracker.FIRST_SUPPLIER_CUSTOMER,
                                        false
                                    )
                                }
                            }
                        )
                    )
            }
        }
    }

    private fun openWhatsAppPromotionShare(intent: Intent) {
        startActivity(intent)
    }

    private fun goToSupplierLearnMoreWebLink(value: String) {
        legacyNavigator.get().goToWebViewScreen(requireActivity(), value)
    }

    private fun showChatEducation() {
        if (binding.supplierScreenToolbar.rootChatContainer.isVisible()) {
            Analytics.track(
                AnalyticsEvents.IN_APP_NOTI_DISPLAYED,
                EventProperties.create()
                    .with(PropertyKey.TYPE, "chat")
                    .with(PropertyKey.SCREEN, "supplier ")
            )
            try {
                viewLifecycleOwner
            } catch (e: Exception) {
                return
            }

            lifecycleScope.launch {
                localInAppNotificationHandler.get()
                    .generateTapTarget(
                        weakScreen = WeakReference(requireActivity()),
                        tapTarget = TapTargetLocal(
                            screenName = label,
                            targetView = WeakReference(binding.supplierScreenToolbar.rootChatContainer),
                            title = getString(R.string.chat_education),
                            titleGravity = Gravity.END,
                            listener = { _, state ->
                                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                    Analytics.track(
                                        AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                        EventProperties.create()
                                            .with(PropertyKey.TYPE, "bill")
                                            .with("focal_area", true)
                                            .with(PropertyKey.SCREEN, "supplier")
                                    )
                                } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                                    Analytics.track(
                                        AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                        EventProperties.create()
                                            .with(PropertyKey.TYPE, "bill")
                                            .with("focal_area", false)
                                            .with(PropertyKey.SCREEN, "supplier")
                                    )
                                }
                            }
                        )
                    )
            }
        }
    }

    private fun showBillEducation() {
        if (binding.supplierScreenToolbar.rootChatContainer.isVisible) {
            Analytics.track(
                AnalyticsEvents.IN_APP_NOTI_DISPLAYED,
                EventProperties.create()
                    .with(PropertyKey.TYPE, "Bill Management")
                    .with(PropertyKey.SCREEN, "supplier ")
            )
            try {
                viewLifecycleOwner
            } catch (e: Exception) {
                return
            }
            lifecycleScope.launch {
                localInAppNotificationHandler.get()
                    .generateTapTarget(
                        weakScreen = WeakReference(requireActivity()),
                        tapTarget = TapTargetLocal(
                            screenName = label,
                            targetView = WeakReference(binding.supplierScreenToolbar.rootBillContianer),
                            title = getString(R.string.bills_education),
                            subtitle = getString(R.string.bills_education_details),
                            subtitleGravity = Gravity.END,
                            listener = { _, state ->
                                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                    Analytics.track(
                                        AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                        EventProperties.create()
                                            .with(PropertyKey.TYPE, "Bill Management")
                                            .with("focal_area", true)
                                            .with(PropertyKey.SCREEN, "supplier")
                                    )
                                    redirectToChatScreen()
                                } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                                    Analytics.track(
                                        AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                        EventProperties.create()
                                            .with(PropertyKey.TYPE, "Bill Management")
                                            .with("focal_area", false)
                                            .with(PropertyKey.SCREEN, "supplier")
                                    )
                                }
                            }
                        )
                    )
            }
        }
    }

    private fun setToolbarInfo(state: State) {
        if (state.supplier != null)
            binding.supplierScreenToolbar.root.profile_name.text = state.supplier.name

        if (state.isLoading) {
            binding.supplierScreenToolbar.root.shimmer_view_container.visible()
            binding.supplierScreenToolbar.root.profile_name.gone()
            binding.supplierScreenToolbar.root.profile_image.gone()
        } else {
            binding.supplierScreenToolbar.root.shimmer_view_container.gone()
            binding.supplierScreenToolbar.root.profile_name.visible()
            binding.supplierScreenToolbar.root.profile_image.visible()
        }

        binding.supplierScreenToolbar.root.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        if (state.supplier != null) {

            val defaultPic = TextDrawable
                .builder()
                .buildRound(
                    state.supplier.name.substring(0, 1).uppercase(Locale.getDefault()),
                    ColorGenerator.MATERIAL.getColor(state.supplier.name)
                )
            if (state.supplier.profileImage != null) {
                if (binding.supplierScreenToolbar.root.profile_image != null) {

                    imageLoader.get().context(this)
                        .load(state.supplier.profileImage)
                        .placeHolder(defaultPic)
                        .scaleType(IImageLoader.CIRCLE_CROP)
                        .into(binding.supplierScreenToolbar.root.profile_image)
                        .build()
                }

                GlideApp
                    .with(this)
                    .load(state.supplier.profileImage)
                    .circleCrop()
                    .placeholder(defaultPic)
                    .fallback(defaultPic)
                    .into(binding.supplierScreenToolbar.root.profile_image)
            } else {
                binding.supplierScreenToolbar.root.profile_image.setImageDrawable(defaultPic)
            }
        }
    }

    private fun takeScreenshot() {
        val screenShot = requireActivity().window.decorView.rootView
        val bitmap =
            Bitmap.createBitmap(screenShot.width, screenShot.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        screenShot.draw(canvas)
        shareAppPromotion.onNext(bitmap to requireContext().getString(R.string.app_promotion_link))
        val accountId = getCurrentState().supplier?.id ?: ""
        val mobile = getCurrentState().supplier?.mobile ?: ""
        tracker.trackEvents(
            Event.SHARED,
            screen = PropertyValue.SUPPLIER_SCREEN,
            relation = PropertyValue.SUPPLIER,
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.ACCOUNT_ID, accountId)
        )
    }

    private fun showTakeCreditPaymentEducation() {
        if (binding.bottomButtonContainer.isVisible().not()) {
            return
        }
        Analytics.track(
            AnalyticsEvents.IN_APP_NOTI_DISPLAYED,
            EventProperties.create()
                .with(PropertyKey.TYPE, "supplier_take_credit")
                .with(PropertyKey.SCREEN, "supplier_screen")
        )

        lifecycleScope.launch {
            localInAppNotificationHandler.get()
                .generateTapTarget(
                    weakScreen = WeakReference(requireActivity()),
                    tapTarget = TapTargetLocal(
                        screenName = label,
                        targetView = WeakReference(binding.sendOrReceivePaymentAb.addLayout),
                        title = getString(R.string.you_can_add_payment),
                        padding = 22f,
                        listener = { _, state ->
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                Analytics.track(
                                    AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                    EventProperties.create()
                                        .with(PropertyKey.TYPE, "supplier_take_credit")
                                        .with("focal_area", true)
                                        .with(PropertyKey.SCREEN, "supplier_screen")
                                )
                                pushIntent(
                                    SupplierContract.Intent.RxPreferenceBoolean(
                                        RxSharedPrefValues.SHOULD_SHOW_TAKE_CREDIT_PAYMENT_EDUCATION,
                                        false,
                                        Scope.Individual
                                    )
                                )
                                pushIntent(SupplierContract.Intent.SetTakeGiveCreditEducation(false))
                            } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                                Analytics.track(
                                    AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                    EventProperties.create()
                                        .with(PropertyKey.TYPE, "supplier_take_credit")
                                        .with("focal_area", false)
                                        .with(PropertyKey.SCREEN, "supplier_screen")
                                )
                                pushIntent(
                                    SupplierContract.Intent.RxPreferenceBoolean(
                                        RxSharedPrefValues.SHOULD_SHOW_TAKE_CREDIT_PAYMENT_EDUCATION,
                                        false,
                                        Scope.Individual
                                    )
                                )
                                pushIntent(SupplierContract.Intent.SetTakeGiveCreditEducation(false))
                            }
                        }
                    )
                )
        }
    }

    override fun onPause() {
        super.onPause()

        mediaPlayer?.release()
    }

    override fun onTransactionClicked(txnId: String, currentDue: Long) {
        transactionClicks.onNext(txnId to currentDue)
    }

    internal fun onAddPhoneClicked() {
        addMobileClicks.onNext(Unit)
    }

    override fun onLoadMoreClicked() {
        expandTransactions.onNext(Unit)
    }

    override fun onPrivacyClicked() {
        tracker.trackViewPrivacy(PropertyValue.RELATION_PAGE, PropertyValue.SUPPLIER)
        privacyClicks.onNext(Unit)
    }

    override fun chatWithUs(amount: String, paymentTime: String, txnId: String, status: String) {
        CustomerSupportOptionDialog
            .newInstance(
                amount = amount,
                paymentTime = paymentTime,
                txnId = txnId,
                status = status,
                accountId = getCurrentState().supplier?.id ?: "",
                ledgerType = LedgerType.SUPPLIER.value.lowercase(),
                source = SOURCE_LEDGER,
            ).show(
                childFragmentManager,
                CustomerSupportOptionDialog.TAG
            )
    }

    internal fun gotoLogin() {
        legacyNavigator.get().goToLoginScreenForAuthFailure(requireActivity())
    }

    private fun gotoAddTransaction(supplierId: String, txnType: Int) {
        kotlin.runCatching {
            findNavController(this).navigate(
                R.id.action_supplierScreen_to_addTxnScreen,
                bundleOf("customer_id" to supplierId, "transaction_type" to txnType)
            )
        }
    }

    internal fun goToWebActivity() {
        val state = getCurrentState()
        state.collectionCustomerProfile?.message_link?.let {
            legacyNavigator.get().goToWebViewScreen(requireActivity(), it)
        }
    }

    private fun shareReportIntent(intent: Intent) {
        hideSoftKeyboard()
        val state = getCurrentState()
        val accountId = state.supplier?.id ?: ""
        val mobileNumber = state.supplier?.mobile ?: ""
        tracker.trackEvents(
            Event.SEND_REPORT, relation = PropertyValue.SUPPLIER,
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.ACCOUNT_ID, accountId)
                .add(PropertyKey.TYPE, "Mini Statement")
                .add(PropertyKey.METHOD, "Whatsapp")
                .add(PropertyKey.SCREEN, PropertyValue.SUPPLIER_SCREEN)
                .add(PropertyKey.CONTENT, 1)
                .add(PropertyKey.PLATFORM, PropertyValue.WHATSAPP)
                .add(PropertyKey.PACKAGE_ID, intent.`package` ?: "whats app not installed")
                .add(PropertyKey.SHARE_TYPE, PropertyValue.SHARE)
        )
        activity?.startActivity(intent)
    }

    private fun gotoTransactionScreen(transaction: `in`.okcredit.merchant.suppliercredit.Transaction) {
        val type = when {
            transaction.payment -> PropertyValue.PAYMENT
            else -> PropertyValue.CREDIT
        }

        val status = when {
            transaction.deleted -> PropertyValue.DELETED
            transaction.collectionId.isNullOrBlank().not() -> PropertyValue.ONLINE_PAYMENT
            else -> "na"
        }

        accountingEventTracker.get().trackViewTransaction(
            screen = PropertyValue.RELATION_PAGE,
            relation = PropertyValue.SUPPLIER,
            mobile = mMobile,
            accountId = supplierId,
            type = type,
            status = status,
            customerSupportType = getCurrentState().supportType.value,
            customerSupportMessage = getString(R.string.t_002_i_need_help_generic),
            amount = transaction.amount.toString()
        )
        legacyNavigator.get().goToSupplierTransactionScreen(requireActivity(), transaction.id)
    }

    private fun gotoSupplierProfileForAddingMobile(supplierId: String) {
        tracker.trackSelectProfileV2(
            PropertyValue.SUPPLIER_SCREEN,
            PropertyValue.SUPPLIER,
            PropertyValue.RELATION_PAGE,
            PropertyValue.SUPPLIER,
            supplierId
        )
        legacyNavigator.get().gotoSupplierProfileForAddingMobile(requireActivity(), supplierId)
    }

    private fun gotoSupplierPrivacyScreen() {
        legacyNavigator.get().gotoPrivacyScreen(requireContext())
    }

    private fun gotoSupplierProfile(supplierId: String) {
        legacyNavigator.get().gotoSupplierProfile(requireActivity(), supplierId)
    }

    private fun goToMerchantProfileForSetupProfile() {
        legacyNavigator.get().goToMerchantProfileForSetupProfile(requireContext())
    }

    private fun gotoCallSupplier(mobile: String) {
        tracker.trackCallRelationShip(
            PropertyValue.RELATION_PAGE,
            PropertyValue.SUPPLIER,
            supplierId,
            getCurrentState().supplier?.state == Supplier.BLOCKED
        )
        Permission.requestCallPermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                    tracker.trackRuntimePermission(PropertyValue.CUSTOMER, Event.CALL, true)
                }

                override fun onPermissionGranted() {
                    val intent = Intent(Intent.ACTION_CALL)
                    intent.data = Uri.parse(getString(R.string.call_template, mobile))
                    startActivity(intent)
                }

                override fun onPermissionDenied() {
                    tracker.trackRuntimePermission(PropertyValue.CUSTOMER, Event.CALL, false)
                    longToast(R.string.call_permission_denied_supplier)
                }
            }
        )
    }

    // when transaction is deleted by supplier, visible notification is received
    // when user click on the notification , he is taken to this screen and we auto scroll to the deleted transaction
    private fun gotoDeletedTransaction(txnId: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            loop@ for ((index, value) in supplierController.adapter.copyOfModels.withIndex()) {

                if (value is DeleteTransactionViewModel_) {

                    val deleteTransaction =
                        (supplierController.adapter.getModelAtPosition(index) as DeleteTransactionViewModel_).transaction()

                    if (deleteTransaction.id == txnId) {
                        withContext(Dispatchers.Main) {
                            val lm = binding.recyclerView.layoutManager as? LinearLayoutManager
                            lm?.scrollToPositionWithOffset(index, 500)
                            delay(200)
                            binding.recyclerView.post {
                                binding.recyclerView.findViewHolderForAdapterPosition(index)?.let {
                                    val container = it.itemView
                                    AnimationUtils.shakeV1(container)
                                }
                            }
                        }
                        break@loop
                    }
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        pushIntent(SupplierContract.Intent.UpdateLastViewTime)
        return super.onBackPressed()
    }

    override fun onDestroy() {
        takeGiveCreditTimer.cancel()
        super.onDestroy()
    }

    override fun onBlockListenerAction(action: Int) {
        if (action == BlockedDialogFragment.ACTION_CALL) {
            callButtonClicks.onNext(Unit)
        } else {
            unblockDialogSubject.onNext(Unit)
            tracker.trackUnBlockRelation(
                Event.UNBLOCK_RELATION_CLICKED,
                PropertyValue.SUPPLIER,
                supplierId,
                PropertyValue.SUPPLIER_SCREEN
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CollectionConstants.SUPPLIER_COLLECTION_REQUEST_CODE) {
            data?.let {
                val supplierId = it.getStringExtra(MainActivity.ARG_SUPPLIER_ID)
                it.getStringExtra(MainActivity.ARG_PAYMENT_METHOD_TYPE)?.let {
                    Completable
                        .timer(100, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            showAddPaymentMethodDialog(supplierId)
                        }.addTo(autoDisposable)
                }
            }
        }
    }

    override fun onMenuItemClicked(menu: MenuOptions) {
        if (menu is MenuOptions.AccountChat) {
            redirectToChatScreen()
        }
    }

    override fun handleViewEvent(event: SupplierContract.ViewEvent) {
        when (event) {
            is SupplierContract.ViewEvent.GotoLogin -> gotoLogin()

            is ViewEvent.GotoAddTransaction -> gotoAddTransaction(event.supplierId, event.txnType)

            is ViewEvent.GotoTransactionScreen -> gotoTransactionScreen(
                event.transaction
            )

            is ViewEvent.GotoSupplierProfile -> gotoSupplierProfile(event.supplierId)

            is ViewEvent.GotoCallSupplier -> gotoCallSupplier(event.mobile)

            is ViewEvent.GotoSupplierProfileForAddingMobile -> gotoSupplierProfileForAddingMobile(
                event.supplierId
            )

            is ViewEvent.GotoSupplierPrivacyScreen -> gotoSupplierPrivacyScreen()

            is ViewEvent.GoToMerchantProfileForSetupProfile -> goToMerchantProfileForSetupProfile()

            is ViewEvent.GotoDeletedTransaction -> gotoDeletedTransaction(event.txnId)

            is ViewEvent.ShareReportIntent -> shareReportIntent(event.value)

            is ViewEvent.ShowUnblockDialog -> showUnblockDialog()

            is ViewEvent.ShowPayOnlineEducation -> showPayOnlineEducation()

            is ViewEvent.OpenWhatsAppPromotionShare -> openWhatsAppPromotionShare(event.intent)

            is ViewEvent.GoToSupplierLearnMoreWebLink -> goToSupplierLearnMoreWebLink(event.value)

            is ViewEvent.ShowChatEducation -> showChatEducation()

            is ViewEvent.ShowBillEducation -> showBillEducation()

            ViewEvent.GoToSupplierReport -> openSupplierReportPage()

            is ViewEvent.ShowAddPaymentMethodDialog -> showAddPaymentMethodDialog(
                event.supplierId
            )
            is ViewEvent.ShowBlindPayDialog -> showBlindPayDialog()

            is ViewEvent.GotoSupplierEditAmountScreen -> gotoPaymentEditAmountScreen(
                event.supplierId,
                event.balance,
                event.remainingDailyAmount,
                event.maxDailyAmount,
                event.riskType,
                event.kycStatus,
                event.kycRiskCategory,
                event.futureAmountLimit
            )
            is ViewEvent.GotoSupplierBlindPayEditAmountScreen -> gotoPaymentBlindPayEditAmountScreen(
                event.supplierId,
                event.balance,
                event.remainingDailyAmount,
                event.maxDailyAmount,
                event.riskType
            )
            is ViewEvent.ShowSupplierDestinationDialog -> showSupplierDestinationDialog(event.supplierId)
            is ViewEvent.OpenLimitReachedBottomSheet -> navigate(R.id.supplierLimitWarningBottomSheet)
            is ViewEvent.OpenExitDialog -> {
                CustomerSupportExitDialog
                    .newInstance(
                        ledgerType = LedgerType.SUPPLIER.value,
                        accountId = getCurrentState().supplier?.id ?: "",
                        source = event.exitSource
                    )
                    .show(childFragmentManager, CustomerSupportExitDialog.TAG)
            }
        }.exhaustive
    }

    private fun openSupplierReportPage() {
        val supplierId = getCurrentState().supplier?.id ?: ""
        findNavController(this).navigate(SupplierFragmentDirections.goToSupplierReportsScreen(supplierId))

        val state = getCurrentState()

        tracker.trackEvents(
            Event.ACCOUNT_REPORT_CLICK, relation = PropertyValue.SUPPLIER,
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.ACCOUNT_ID, supplierId)
                .add(PropertyKey.DUE_AMOUNT, state.supplier?.balance ?: "")
        )
    }

    private fun gotoPaymentEditAmountScreen(
        supplierId: String,
        balance: Long,
        remainingDailyAmount: Long,
        maxDailyAmount: Long,
        riskType: String,
        kycStatus: KycStatus,
        kycRiskCategory: KycRiskCategory,
        futureAmountLimit: Long,
    ) {

        getCurrentState().let {
            paymentNavigator.get().gotoJuspayPaymentEditAmountScreen(
                requireActivity(),
                supplierId,
                maxDailyAmount,
                remainingDailyAmount,
                balance,
                it.business?.id ?: "",
                riskType,
                it.collectionCustomerProfile?.linkId ?: "",
                it.supplier?.mobile ?: "",
                it.collectionCustomerProfile?.paymentAddress ?: "",
                it.collectionCustomerProfile?.type ?: "",
                it.collectionCustomerProfile?.name ?: "",
                LedgerType.SUPPLIER.value,
                kycStatus.value,
                kycRiskCategory.value,
                futureAmountLimit,
                this,
                supportType = getCurrentState().supportType.value,
                destinationUpdateAllowed = it.collectionCustomerProfile?.destinationUpdateAllowed ?: true,
            )
        }
    }

    private fun gotoPaymentBlindPayEditAmountScreen(
        supplierId: String,
        balance: Long,
        remainingDailyAmount: Long,
        maxDailyAmount: Long,
        riskType: String,
    ) {

        getCurrentState().let {
            paymentNavigator.get().gotoJuspayPaymentEditAmountScreen(
                requireActivity(),
                supplierId,
                maxDailyAmount,
                remainingDailyAmount,
                balance,
                it.business?.id ?: "",
                riskType,
                it.blindPayLinkId,
                it.supplier?.mobile ?: "",
                it.collectionCustomerProfile?.paymentAddress ?: "",
                it.collectionCustomerProfile?.type ?: "",
                it.collectionCustomerProfile?.name ?: "",
                LedgerType.SUPPLIER.value,
                KycStatus.NOT_SET.value,
                KycRiskCategory.NO_RISK.value,
                0,
                this,
                true,
                it.supplier?.profileImage ?: "",
                it.supplier?.name ?: "",
                supportType = getCurrentState().supportType.value,
                destinationUpdateAllowed = getCurrentState().collectionCustomerProfile?.destinationUpdateAllowed
                    ?: true,
            )
        }
    }

    override fun onEditDestinationClicked(supplierId: String) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            // delay is kept so that when edit amount bottom sheet will dismiss wont
            // clash with AddPaymentMethod bottom sheet
            delay(100L)
            showAddPaymentMethodDialog(supplierId)
        }
    }

    override fun onExitFromPaymentFlow(source: String) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            pushIntent(SupplierContract.Intent.OpenExitDialog(source))
        }
    }

    override fun onDestinationAddedSuccessfully() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            pushIntent(SupplierContract.Intent.GetCollectionProfileAfterSetDestination)
        }
    }

    override fun onPaymentTypeSelected(paymentType: PaymentType) {
        when (paymentType) {
            PaymentType.BLIND_PAY -> {
                supplierEventTracker.get()
                    .trackPaymentOption(
                        accountId = getCurrentState().supplier?.id ?: "",
                        getCurrentState().business?.id ?: "",
                        true
                    )
                pushIntent((SupplierContract.Intent.IsJuspayFeatureEnabled))
            }
            PaymentType.OTHERS -> {
                supplierEventTracker.get()
                    .trackPaymentOption(
                        accountId = getCurrentState().supplier?.id ?: "",
                        getCurrentState().business?.id ?: "",
                        false
                    )
                showAddPaymentMethodDialog(getCurrentState().supplier!!.id)
            }
        }
    }

    override fun onSortOptionSelected(sortSelection: String) {
        accountingEventTracker.get().trackSortByUpdated(sortSelection, SUPPLIER)
        pushIntent(SupplierContract.Intent.OnUpdateSortSelection(sortSelection))
        if (supplierController.adapter.itemCount > 0) {
            binding.recyclerView.scrollToPosition(supplierController.adapter.itemCount - 1)
        }
    }
}
