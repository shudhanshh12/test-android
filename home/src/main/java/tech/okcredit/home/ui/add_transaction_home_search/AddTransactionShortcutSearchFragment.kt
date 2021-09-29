package tech.okcredit.home.ui.add_transaction_home_search

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.PropertyValue.CUSTOMER
import `in`.okcredit.analytics.Screen.ADD_TRANSACTION_SHORTCUT
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.SmsHelper
import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.server.internal.common.SupplierCreditServerErrors
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.utils.KeyboardUtil
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.exceptions.ExceptionUtils
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.contacts.contract.model.Contact
import tech.okcredit.home.R
import tech.okcredit.home.databinding.AddTransactionShortcutSearchFragmentBinding
import tech.okcredit.home.ui.add_transaction_home_search.AddTransactionShortcutSearchContract.State
import tech.okcredit.home.ui.add_transaction_home_search.AddTransactionShortcutSearchContract.ViewEvent
import tech.okcredit.home.ui.analytics.HomeEventTracker
import tech.okcredit.home.ui.analytics.HomeTraces
import tech.okcredit.home.ui.home.views.HomeCustomerView
import tech.okcredit.home.ui.home.views.HomeSupplierView
import tech.okcredit.home.ui.homesearch.dialogs.CyclicAccountDialog
import tech.okcredit.home.ui.homesearch.dialogs.MobileConflictDialog
import tech.okcredit.home.ui.homesearch.views.AddContactView
import tech.okcredit.home.ui.homesearch.views.ImportCustomerContactView
import tech.okcredit.home.ui.homesearch.views.NoUserFoundView
import tech.okcredit.home.utils.scrollToTopOnItemInsertItem
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.timerTask

class AddTransactionShortcutSearchFragment :
    BaseFragment<State, ViewEvent, AddTransactionShortcutSearchContract.Intent>(
        "AddTransactionShortcutSearchScreen",
        R.layout.add_transaction_shortcut_search_fragment
    ),
    HomeCustomerView.CustomerSelectionListener,
    AddContactView.AddLocalContactListener,
    HomeSupplierView.SupplierSelectionListener,
    ImportCustomerContactView.ImportContactListener,
    NoUserFoundView.AddListener {

    val importContactPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    private val addContactPublishSubject: PublishSubject<Contact> = PublishSubject.create()
    private val addRelationPublicSubject: PublishSubject<Unit> = PublishSubject.create()
    private var sendReminderPublishSubject = PublishSubject.create<String>()

    internal val binding: AddTransactionShortcutSearchFragmentBinding by viewLifecycleScoped(
        AddTransactionShortcutSearchFragmentBinding::bind
    )

    companion object {
        const val ARG_REFERRAL_TARGETS = "referral targets"
        const val ARG_ADD_TRANSACTION_SHORTCUT_SOURCE = "add_transaction_shortcut_source"

        @JvmStatic
        fun newInstance(source: String?): AddTransactionShortcutSearchFragment {
            val args = Bundle().apply {
                putString(ARG_ADD_TRANSACTION_SHORTCUT_SOURCE, source)
            }

            return AddTransactionShortcutSearchFragment().apply {
                arguments = args
            }
        }
    }

    private lateinit var homeSearchController: AddTransactionShortcutSearchController

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var imageLoader: Lazy<IImageLoader>

    @Inject
    internal lateinit var smsHelper: Lazy<SmsHelper>

    @Inject
    internal lateinit var homeTracker: Lazy<HomeEventTracker>

    private var isSearchAnalyticsFired = false
    private var isPageLoadEventFired = false

    private var isComingFromReferralTargets: Boolean = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        homeSearchController =
            AddTransactionShortcutSearchController(
                this,
                activity,
                performanceTracker,
                tracker.get(),
            )
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = homeSearchController.adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchInput.postDelayed(
            {
                KeyboardVisibilityEvent.showKeyboard(context, binding.searchInput, binding.rootLayout)
            },
            100
        )
        binding.rootLayout.setTracker(performanceTracker)
    }

    override fun loadIntent(): UserIntent {
        return AddTransactionShortcutSearchContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            binding.searchInput
                .textChanges()
                .debounce(300, TimeUnit.MILLISECONDS)
                .doOnNext {
                    trackSearch()
                }
                .map {
                    AddTransactionShortcutSearchContract.Intent.SearchQuery(it.toString())
                },

            importContactPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { AddTransactionShortcutSearchContract.Intent.ImportContact },

            addContactPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    AddTransactionShortcutSearchContract.Intent.AddRelationFromContact(it)
                },

            addRelationPublicSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    AddTransactionShortcutSearchContract.Intent.AddRelationFromSearch(getCurrentState().searchQuery)
                },

            sendReminderPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    AddTransactionShortcutSearchContract.Intent.SendWhatsAppReminder(it)
                },

            binding.btnClose.clicks()
                .map {
                    AddTransactionShortcutSearchContract.Intent.OnBackPressed
                },

            binding.btnHome.clicks()
                .map {
                    AddTransactionShortcutSearchContract.Intent.GoToHomeScreen
                }
        )
    }

    private fun trackSearch() {
        if (isSearchAnalyticsFired.not() && isStateInitialized()) {
            if (isComingFromReferralTargets) {
                tracker.get().trackAddTransactionSearchShortcutRelationship("Referral Targets")
            } else {
                tracker.get().trackAddTransactionSearchShortcutRelationship("Shortcut")
            }
        }
    }

    @AddTrace(name = HomeTraces.RenderSearch)
    override fun render(state: State) {
        val epoxyVisibilityTracker = EpoxyVisibilityTracker()
        epoxyVisibilityTracker.attach(binding.recyclerView)

        isComingFromReferralTargets = state.isComingFromReferralTargets

        binding.swipeRefresh.setOnRefreshListener {
            tracker.get().trackRefresh(PropertyValue.CONTACT)
            importContactPublishSubject.onNext(Unit)

            binding.swipeRefresh.isRefreshing = false
        }

        homeSearchController.setState(state)
        homeSearchController.adapter.scrollToTopOnItemInsertItem(binding.recyclerView)

        trackPageLoad()
    }

    private fun trackPageLoad() {
        val state = getCurrentState()
        if (isPageLoadEventFired.not() && state.isSuggestedCustomersLoading.not()) {
            isPageLoadEventFired = true
            homeTracker.get().addTransactionShortcutPageLoad(state.suggestedCustomers.size)
        }
    }

    override fun onPause() {
        super.onPause()

        KeyboardVisibilityEvent.hideKeyboard(activity, binding.rootLayout)
    }

    private fun gotoLogin() {
        legacyNavigator.get().goToLoginScreenForAuthFailure(requireActivity())
    }

    override fun onBackPressed(): Boolean {
        if (isComingFromReferralTargets) {
            goToHomeScreen()
        } else {
            requireActivity().finishAffinity()
        }
        return true
    }

    private fun gotoCustomerScreenAndCloseSearch(customerId: String, mobile: String?) {
        KeyboardUtil.hideKeyboard(requireActivity(), binding.rootLayout)
        tracker.get().trackViewRelationship(
            list = PropertyValue.FALSE,
            relation = PropertyValue.CUSTOMER,
            search = PropertyValue.TRUE,
            accountId = customerId,
            flow = if (isComingFromReferralTargets) "Referral Targets" else "ShortCut",
            commonLedger = getCurrentState().supplierCreditEnabledCustomerIds.contains(customerId),
        )

        findNavController(this).popBackStack()
        legacyNavigator.get().goToCustomerScreen(requireActivity(), customerId)
    }

    fun gotoCustomerScreen(customerId: String, mobile: String?) {
        activity?.runOnUiThread {
            KeyboardUtil.hideKeyboard(requireActivity(), binding.rootLayout)
            tracker.get().trackViewRelationship(
                list = PropertyValue.FALSE,
                relation = CUSTOMER,
                search = PropertyValue.TRUE,
                accountId = customerId,
                flow = if (isComingFromReferralTargets) "Referral Targets" else "ShortCut",
                commonLedger = getCurrentState().supplierCreditEnabledCustomerIds.contains(customerId),
            )
            binding.searchInput.setText("")

            legacyNavigator.get().goToCustomerScreen(requireActivity(), customerId)
        }
    }

    @UiThread
    fun gotoAddTransactionScreen(customer: Customer) {
        trackRelationClick(customer)
        binding.searchInput.setText("")
        activity?.runOnUiThread {
            try {
                hideSoftKeyboard()
            } catch (e: Exception) {
                ExceptionUtils.logException("Add Txn Shortcut CTA Keyboard", e)
            }

            legacyNavigator.get()
                .goToAddTransactionScreen(requireContext(), customer.id, isFromAddTransactionShortcut = true)
            if (isComingFromReferralTargets) {
                requireActivity().finish()
            }
        }
    }

    private fun trackRelationClick(customer: Customer) {
        val state = getCurrentState()
        val source = if (state.searchQuery.isBlank()) "List" else "Search"
        val flow = if (isComingFromReferralTargets) "Referral Targets" else "Shortcut"
        val isSuggested = state.suggestedCustomers.map { it.id }.contains(customer.id)
        homeTracker.get().trackAddTransactionShortcutRelationClicked(source, flow, isSuggested)
        tracker.get()
            .trackAddTransactionFlowsStarted("", CUSTOMER, customer.id, ADD_TRANSACTION_SHORTCUT)
    }

    fun gotoSupplierScreen(supplierId: String, registered: Boolean) {

        activity?.runOnUiThread {
            KeyboardUtil.hideKeyboard(requireActivity(), binding.rootLayout)
            tracker.get().trackViewRelationship(
                list = PropertyValue.FALSE,
                relation = PropertyValue.SUPPLIER,
                search = PropertyValue.TRUE,
                accountId = supplierId,
                commonLedger = registered
            )
            binding.searchInput.setText("")

            legacyNavigator.get().goToSupplierScreen(requireContext(), supplierId)
        }
    }

    private fun importContact() {
        KeyboardUtil.hideKeyboard(requireActivity(), binding.rootLayout)
        Permission.requestContactPermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                    tracker.get()
                        .trackRuntimePermission("AddTransactionShortcutSearchScreen", PropertyValue.CONTACT, true)
                }

                override fun onPermissionGranted() {
                    Timer().schedule(
                        timerTask {
                            importContactPublishSubject.onNext(Unit)
                        },
                        500
                    )
                }

                override fun onPermissionDenied() {
                    tracker.get()
                        .trackRuntimePermission("AddTransactionShortcutSearchScreen", PropertyValue.CONTACT, false)
                }
            }
        )
    }

    /****************************************************************
     * Listeners (for child views)
     ****************************************************************/

    override fun onCustomerSelected(customer: Customer) {
        if (customer.state != Customer.State.BLOCKED) {
            gotoAddTransactionScreen(customer)
        } else {
            gotoCustomerScreen(customer.id, customer.mobile)
        }
    }

    override fun onCustomerProfileSelected(customer: Customer) {
        onCustomerSelected(customer)
    }

    private fun shareReminder(intent: Intent) {
        activity?.startActivity(intent)
    }

    override fun onSupplierSelected(supplierId: String, registered: Boolean) {
        gotoSupplierScreen(supplierId, registered)
    }

    override fun onSupplierProfileSelected(supplier: Supplier) {
        gotoSupplierScreen(supplier.id, supplier.registered)
    }

    override fun onAddContact(contact: Contact) {
        tracker.get().trackAddRelationshipStartedFlows(
            relation = PropertyValue.CUSTOMER,
            search = PropertyValue.TRUE,
            type = PropertyValue.ADD_CLICKED,
            contact = PropertyValue.TRUE
        )

        tracker.get().trackAddRelationship(
            PropertyValue.CUSTOMER,
            PropertyValue.ADD_CLICKED,
            PropertyValue.TRUE,
            contact.mobile
        )

        binding.searchInput.setText("")
        KeyboardUtil.hideKeyboard(requireActivity(), binding.rootLayout)
        addContactPublishSubject.onNext(contact)
    }

    override fun onImportContact() {
        tracker.get().trackImportContact("AddTransactionShortcutSearchScreen", "")
        importContact()
    }

    override fun onAddNewUser() {
        tracker.get().trackAddRelationshipStartedFlows(
            relation = PropertyValue.CUSTOMER,
            search = PropertyValue.TRUE,
            type = PropertyValue.ADD_CLICKED,
            contact = PropertyValue.FALSE,
            source = if (isComingFromReferralTargets) "Referral Targets" else "ShortCut"
        )
        tracker.get().trackAddRelationship(
            relation = PropertyValue.CUSTOMER,
            type = PropertyValue.ADD_CLICKED,
            contact = PropertyValue.FALSE,
            source = if (isComingFromReferralTargets) "Referral Targets" else "ShortCut"
        )
        binding.searchInput.setText("")
        addRelationPublicSubject.onNext(Unit)
    }

    private fun showInvalidMobileNumber() {
        view?.snackbar(getString(R.string.invalid_mobile), Snackbar.LENGTH_LONG)?.show()
    }

    private fun showMobileConflictForCustomer(customer: Customer) {
        MobileConflictDialog.showCustomerConflict(
            activity, customer,
            object : MobileConflictDialog.Listener {
                override fun onViewClicked() {
                    gotoCustomerScreen(customer.id, customer.mobile)
                }
            }
        )
    }

    private fun showCyclicAccountForSupplier(supplier: Supplier) {
        CyclicAccountDialog.showSupplierConflict(
            activity, supplier,
            object : CyclicAccountDialog.Listener {
                override fun onViewClicked() {
                    KeyboardUtil.hideKeyboard(requireActivity(), binding.rootLayout)
                    tracker.get().trackViewRelationshipV1(
                        list = PropertyValue.FALSE,
                        relation = PropertyValue.SUPPLIER,
                        search = PropertyValue.TRUE,
                        screen = "AddTransactionShortcutSearchScreen::Customer",
                        accountId = supplier.id,
                        commonLedger = supplier.registered
                    )
                    legacyNavigator.get().goToSupplierScreen(context!!, supplier.id)
                }
            }
        )
    }

    private fun showCyclicAccountForDeletedSupplier(supplier: Supplier) {
        CyclicAccountDialog.showSupplierConflict(
            requireActivity(),
            supplier,
            object : CyclicAccountDialog.Listener {
                override fun onViewClicked() {
                    KeyboardUtil.hideKeyboard(requireActivity(), binding.rootLayout)
                    tracker.get().trackViewRelationship(
                        PropertyValue.FALSE,
                        PropertyValue.SUPPLIER,
                        PropertyValue.FALSE,
                        supplier.id,
                        flow = if (isComingFromReferralTargets) "Referral Targets" else "ShortCut",
                        commonLedger = supplier.registered

                    )
                    legacyNavigator.get()
                        .startingSupplierScreenForReactivation(requireActivity(), supplier.id, null)
                }
            }
        )
    }

    private fun showMobileConflictForSupplier(supplier: Supplier) {
        MobileConflictDialog.showSupplierConflict(
            requireActivity(),
            supplier,
            object : MobileConflictDialog.Listener {
                override fun onViewClicked() {
                    gotoSupplierScreen(supplier.id, supplier.registered)
                }
            }
        )
    }

    private fun showCyclicAccountForDeletedCustomer(errorData: SupplierCreditServerErrors.Error?) {
        CyclicAccountDialog.showCustomerConflict(
            requireActivity(),
            errorData,
            object : CyclicAccountDialog.Listener {
                override fun onViewClicked() {
                    errorData?.id?.let {
                        KeyboardUtil.hideKeyboard(requireActivity(), binding.rootLayout)
                        tracker.get().trackViewRelationship(
                            PropertyValue.FALSE,
                            PropertyValue.CUSTOMER,
                            PropertyValue.FALSE,
                            it,
                            commonLedger = getCurrentState().supplierCreditEnabledCustomerIds.contains(it)
                        )
                        legacyNavigator.get().startingCustomerScreenForReactivation(activity as Activity, it, null)
                    }
                }
            }
        )
    }

    private fun showCyclicAccount(info: SupplierCreditServerErrors.Error?) {

        CyclicAccountDialog.showCustomerConflict(
            requireActivity(), info,
            object : CyclicAccountDialog.Listener {
                override fun onViewClicked() {
                    info?.id?.let {
                        KeyboardUtil.hideKeyboard(requireActivity(), binding.rootLayout)
                        tracker.get().trackViewRelationshipV1(
                            list = PropertyValue.FALSE,
                            relation = PropertyValue.CUSTOMER,
                            search = PropertyValue.TRUE,
                            screen = "AddTransactionShortcutSearchScreen::Supplier",
                            accountId = it,
                            commonLedger = getCurrentState().supplierCreditEnabledCustomerIds.contains(it)
                        )
                        legacyNavigator.get().goToCustomerScreen(context!!, it)
                    }
                }
            }
        )
    }

    private fun showInvalidName() {
        view?.snackbar(getString(R.string.invalid_name), Snackbar.LENGTH_LONG)?.show()
    }

    private fun showError() {
        view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_LONG)?.show()
    }

    private fun showInternetError() {
        view?.snackbar(getString(R.string.no_internet_msg), Snackbar.LENGTH_LONG)?.show()
    }

    private fun goToHomeScreen() {
        requireActivity().finish()
        activity?.overridePendingTransition(0, R.anim.slide_out_left)
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.ShowInvalidMobileNumber -> showInvalidMobileNumber()
            is ViewEvent.ShowMobileConflictForCustomer -> showMobileConflictForCustomer(
                event.customer
            )
            is ViewEvent.ShowCyclicAccountForSupplier -> showCyclicAccountForSupplier(
                event.supplier
            )
            is ViewEvent.ShowCyclicAccountForDeletedSupplier -> showCyclicAccountForDeletedSupplier(
                event.supplier
            )
            is ViewEvent.ShowMobileConflictForSupplier -> showMobileConflictForSupplier(
                event.supplier
            )
            is ViewEvent.ShowInvalidName -> showInvalidName()
            is ViewEvent.ShowError -> showError()
            is ViewEvent.ShowInternetError -> showInternetError()
            is ViewEvent.ShowCyclicAccountForDeletedCustomer -> showCyclicAccountForDeletedCustomer(
                event.errorData
            )
            is ViewEvent.ShowCyclicAccount -> showCyclicAccount(event.info)
            is ViewEvent.GotoLogin -> gotoLogin()
            is ViewEvent.GotoCustomerScreenAndCloseSearch -> gotoCustomerScreenAndCloseSearch(
                event.customerId,
                event.mobile
            )
            is ViewEvent.ShareReminder -> shareReminder(event.intent)
            is ViewEvent.GotoHomeScreen -> goToHomeScreen()
            is ViewEvent.OnBackPressed -> onBackPressed()
        }
    }
}
