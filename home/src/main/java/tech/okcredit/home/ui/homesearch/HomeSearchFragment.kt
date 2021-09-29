package tech.okcredit.home.ui.homesearch

import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.CollectionNavigator
import `in`.okcredit.customer.contract.CustomerNavigator
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.server.internal.common.SupplierCreditServerErrors
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.widget.textChanges
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.showSoftKeyboard
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.contacts.contract.model.Contact
import tech.okcredit.home.R
import tech.okcredit.home.databinding.HomeSearchFragmentBinding
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileDialog
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileDialogContract
import tech.okcredit.home.ui.analytics.HomeTraces
import tech.okcredit.home.ui.home.views.HomeSupplierView
import tech.okcredit.home.ui.homesearch.HomeSearchContract.*
import tech.okcredit.home.ui.homesearch.dialogs.CyclicAccountDialog
import tech.okcredit.home.ui.homesearch.dialogs.MobileConflictDialog
import tech.okcredit.home.ui.homesearch.views.AddContactView
import tech.okcredit.home.ui.homesearch.views.FilterView
import tech.okcredit.home.ui.homesearch.views.HomeSearchCustomerView
import tech.okcredit.home.ui.homesearch.views.ImportCustomerContactView
import tech.okcredit.home.ui.homesearch.views.NoUserFoundView
import tech.okcredit.home.utils.getAnalyticsRelationValue
import tech.okcredit.home.utils.scrollToTopOnItemInsertItem
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeSearchFragment :
    BaseFragment<State, ViewEvent, HomeSearchContract.Intent>("HomeSearchScreen", R.layout.home_search_fragment),
    HomeSearchCustomerView.SearchCustomerListener,
    FilterView.ClearFilterListener,
    AddContactView.AddLocalContactListener,
    HomeSupplierView.SupplierSelectionListener,
    ImportCustomerContactView.ImportContactListener,
    NoUserFoundView.AddListener {

    companion object {
        const val ARG_SOURCE = "source"
        const val ACCOUNT_SELECTION = "customer_selection"
        const val TAG = "HomeSearchFragment"
        const val COLLECTION_PAYMENT_LINK = "Collection Payment Link"

        fun newInstance() = HomeSearchFragment()
    }

    private val resetDataPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    private val addContactPublishSubject: PublishSubject<Contact> = PublishSubject.create()
    private val addRelationPublicSubject: PublishSubject<Unit> = PublishSubject.create()

    private lateinit var homeSearchController: HomeSearchControllerV2

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var customerNavigator: Lazy<CustomerNavigator>

    @Inject
    internal lateinit var collectionNavigator: Lazy<CollectionNavigator>

    private var isSearchAnalyticsFired = false

    private val binding: HomeSearchFragmentBinding by viewLifecycleScoped(HomeSearchFragmentBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeSearchController = HomeSearchControllerV2(
            fragment = this,
            tracker = tracker.get(),
            performanceTracker = performanceTracker.get()
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = homeSearchController.adapter
        setListeners()
        binding.rootLayout.setTracker(performanceTracker)
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            requireActivity().finish()
        }
        binding.btnClose.setOnClickListener {
            binding.searchInput.setText("")
            if (getCurrentState().isAccountSelection) {
                hideSoftKeyboard()
                pushIntent(HomeSearchContract.Intent.ShowSearchInput(false))
            }
        }
        binding.searchImg.setOnClickListener {
            pushIntent(HomeSearchContract.Intent.ShowSearchInput())
        }

        val epoxyVisibilityTracker = EpoxyVisibilityTracker()
        epoxyVisibilityTracker.attach(binding.recyclerView)

        binding.swipeRefresh.setOnRefreshListener {
            tracker.get().trackRefresh(PropertyValue.CONTACT)
            pushIntent(HomeSearchContract.Intent.ImportContact())

            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun loadIntent(): UserIntent {
        return HomeSearchContract.Intent.Load
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
                    HomeSearchContract.Intent.SearchQuery(it.toString())
                },

            addContactPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    HomeSearchContract.Intent.AddRelationFromContact(it, getCurrentState().source)
                },

            addRelationPublicSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    HomeSearchContract.Intent.AddRelationFromSearch(
                        getCurrentState().searchQuery,
                        getCurrentState().source
                    )
                },

            resetDataPublishSubject
                .map {
                    HomeSearchContract.Intent.ResetData
                }
        )
    }

    private fun trackSearch() {
        if (isSearchAnalyticsFired.not() && isStateInitialized()) {
            tracker.get().trackSearchRelationship(
                if (getCurrentState().source == SOURCE.HOME_CUSTOMER_TAB)
                    PropertyValue.CUSTOMER
                else PropertyValue.SUPPLIER
            )
        }
    }

    @AddTrace(name = HomeTraces.RenderSearch)
    override fun render(state: State) {
        binding.btnClose.isVisible = state.searchQuery.isNotEmpty()

        if (state.hideSearchInput) {
            hideSearchInput()
        } else {
            showSearchInput()
        }

        binding.selectCustomerTitle.text = if (state.source == SOURCE.HOME_SUPPLIER_TAB) {
            getString(R.string.select_supplier)
        } else {
            getString(R.string.select_customer)
        }
        homeSearchController.setData(state.itemList)
        homeSearchController.adapter.scrollToTopOnItemInsertItem(binding.recyclerView)
    }

    private fun showSearchInput() {
        binding.selectCustomerGrp.gone()
        binding.searchInputGrp.visible()
        binding.searchInput.requestFocus()
    }

    private fun hideSearchInput() {
        binding.selectCustomerGrp.visible()
        binding.searchInputGrp.gone()
    }

    override fun onPause() {
        super.onPause()
        hideSoftKeyboard()
    }

    private fun goToCustomerScreenAndCloseSearch(customerId: String, mobile: String?) {
        hideSoftKeyboard()
        tracker.get().trackViewRelationship(
            list = PropertyValue.FALSE,
            relation = PropertyValue.CUSTOMER,
            search = PropertyValue.TRUE,
            accountId = customerId,
            commonLedger = getCurrentState().supplierCreditEnabledCustomerIds.contains(customerId)
        )

        requireActivity().finish()
        legacyNavigator.get().goToCustomerScreen(requireActivity(), customerId)
    }

    internal fun goToCustomerScreen(customerId: String) {
        hideSoftKeyboard()
        tracker.get().trackViewRelationship(
            list = PropertyValue.FALSE,
            relation = PropertyValue.CUSTOMER,
            search = PropertyValue.TRUE,
            accountId = customerId,
            commonLedger = getCurrentState().supplierCreditEnabledCustomerIds.contains(customerId)
        )
        binding.searchInput.setText("")

        legacyNavigator.get().goToCustomerScreen(requireActivity(), customerId)
    }

    private fun gotoSupplierScreenAndCloseSearch(supplierId: String, registered: Boolean) {
        activity?.runOnUiThread {
            hideSoftKeyboard()
            tracker.get().trackViewRelationship(
                list = PropertyValue.FALSE,
                relation = PropertyValue.SUPPLIER,
                search = PropertyValue.TRUE,
                accountId = supplierId,
                commonLedger = registered
            )

            requireActivity().finish()
            legacyNavigator.get().goToSupplierScreen(requireContext(), supplierId)
        }
    }

    fun gotoSupplierScreen(supplierId: String, registered: Boolean) {
        hideSoftKeyboard()
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

    private fun importContact() {
        hideSoftKeyboard()
        Permission.requestContactPermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                    tracker.get().trackRuntimePermission(PropertyValue.HOME_PAGE, PropertyValue.CONTACT, true)
                }

                override fun onPermissionGranted() {
                    pushIntentWithDelay(HomeSearchContract.Intent.ImportContact(true))
                }

                override fun onPermissionDenied() {
                    tracker.get().trackRuntimePermission(PropertyValue.HOME_PAGE, PropertyValue.CONTACT, false)
                }
            }
        )
    }

    override fun onCustomerSelected(customerId: String) {
        if (getCurrentState().isAccountSelection) {
            tracker.get().trackAddRelationshipStartedFlows(
                relation = PropertyValue.CUSTOMER,
                search = PropertyValue.TRUE,
                type = PropertyValue.ADD_CLICKED,
                contact = PropertyValue.FALSE,
                flow = COLLECTION_PAYMENT_LINK
            )
            returnSelectedAccount(customerId, PropertyValue.CUSTOMER)
        } else {
            goToCustomerScreen(customerId)
        }
    }

    override fun showCustomerQr(customerId: String, source: String) {
        hideSoftKeyboard()
        if (source == CustomerProfileDialogContract.Source.CUSTOMER_SEARCH_PROFILE) {
            CustomerProfileDialog.showDialog(childFragmentManager, customerId, source)
        } else {
            pushIntent(HomeSearchContract.Intent.ShowCustomerQr(customerId, source))
        }
    }

    override fun sendReminder(customerId: String) {
        hideSoftKeyboard()
        pushIntent(HomeSearchContract.Intent.SendCustomerReminder(customerId))
    }

    private fun returnSelectedAccount(customerId: String, relation: String) {
        val data = Intent()
        data.putExtra(PropertyKey.RELATION, relation)
        data.putExtra(PropertyKey.ACCOUNT_ID, customerId)
        activity?.setResult(Activity.RESULT_OK, data)
        activity?.finish()
    }

    override fun onSupplierSelected(supplierId: String, registered: Boolean) {
        if (getCurrentState().isAccountSelection) {
            tracker.get().trackAddRelationshipStartedFlows(
                relation = PropertyValue.SUPPLIER,
                search = PropertyValue.TRUE,
                type = PropertyValue.ADD_CLICKED,
                contact = PropertyValue.FALSE,
                flow = COLLECTION_PAYMENT_LINK
            )
            returnSelectedAccount(supplierId, PropertyValue.SUPPLIER)
        } else {
            gotoSupplierScreen(supplierId, registered)
        }
    }

    override fun onSupplierProfileSelected(supplier: Supplier) {
        gotoSupplierScreen(supplier.id, supplier.registered)
    }

    override fun onFilterCleared(source: String) {
        var flow = "New"
        if (Sort.sortApplied) {
            flow = "Update"
        }
        Sort.reset()
        tracker.get().trackClearFilter(source, PropertyValue.CUSTOMER, flow)
        resetDataPublishSubject.onNext(Unit)
    }

    override fun onAddContact(contact: Contact) {
        var collectionFlow: String? = null
        if (getCurrentState().isAccountSelection) {
            collectionFlow = COLLECTION_PAYMENT_LINK
        }
        tracker.get().trackAddRelationshipStartedFlows(
            relation = getCurrentState().source.getAnalyticsRelationValue(),
            search = PropertyValue.TRUE,
            type = PropertyValue.ADD_CLICKED,
            contact = PropertyValue.TRUE,
            flow = collectionFlow
        )

        tracker.get().trackAddRelationship(
            getCurrentState().source.getAnalyticsRelationValue(),
            PropertyValue.ADD_CLICKED,
            PropertyValue.TRUE,
            contact.mobile
        )

        hideSoftKeyboard()
        addContactPublishSubject.onNext(contact)
    }

    override fun onImportContact() {
        tracker.get().trackImportContact(PropertyValue.HOME_PAGE, getCurrentState().source.getAnalyticsRelationValue())
        importContact()
    }

    override fun onAddNewUser() {
        tracker.get().trackAddRelationshipStartedFlows(
            relation = PropertyValue.CUSTOMER,
            search = PropertyValue.TRUE,
            type = PropertyValue.ADD_CLICKED,
            contact = PropertyValue.FALSE
        )
        tracker.get().trackAddRelationship(
            relation = PropertyValue.CUSTOMER,
            type = PropertyValue.ADD_CLICKED,
            contact = PropertyValue.FALSE
        )
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
                    goToCustomerScreen(customer.id)
                }
            }
        )
    }

    private fun showCyclicAccountForSupplier(supplier: Supplier) {
        CyclicAccountDialog.showSupplierConflict(
            activity, supplier,
            object : CyclicAccountDialog.Listener {
                override fun onViewClicked() {
                    activity?.runOnUiThread {
                        hideSoftKeyboard()
                        tracker.get().trackViewRelationshipV1(
                            list = PropertyValue.FALSE,
                            relation = PropertyValue.SUPPLIER,
                            search = PropertyValue.TRUE,
                            screen = PropertyValue.CUSTOMER,
                            accountId = supplier.id,
                            commonLedger = supplier.registered
                        )
                        legacyNavigator.get().goToSupplierScreen(context!!, supplier.id)
                    }
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
                    activity?.runOnUiThread {
                        hideSoftKeyboard()
                        tracker.get().trackViewRelationship(
                            PropertyValue.FALSE,
                            PropertyValue.SUPPLIER,
                            PropertyValue.FALSE,
                            supplier.id,
                            commonLedger = supplier.registered
                        )
                        legacyNavigator.get()
                            .startingSupplierScreenForReactivation(requireActivity(), supplier.id, null)
                    }
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
                        hideSoftKeyboard()
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
                        hideSoftKeyboard()
                        tracker.get().trackViewRelationshipV1(
                            list = PropertyValue.FALSE,
                            relation = PropertyValue.CUSTOMER,
                            search = PropertyValue.TRUE,
                            screen = PropertyValue.SUPPLIER,
                            accountId = it,
                            commonLedger = getCurrentState().supplierCreditEnabledCustomerIds.contains(it)
                        )
                        legacyNavigator.get().goToCustomerScreen(context!!, it)
                    }
                }
            }
        )
    }

    override fun onBackPressed(): Boolean {
        requireActivity().finish()
        return true
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

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.ShowInvalidMobileNumber -> showInvalidMobileNumber()
            is ViewEvent.ShowMobileConflictForCustomer -> showMobileConflictForCustomer(event.customer)
            is ViewEvent.ShowCyclicAccountForSupplier -> showCyclicAccountForSupplier(event.supplier)
            is ViewEvent.ShowCyclicAccountForDeletedSupplier -> showCyclicAccountForDeletedSupplier(
                event.supplier
            )
            is ViewEvent.ShowMobileConflictForSupplier -> showMobileConflictForSupplier(event.supplier)
            is ViewEvent.ShowInvalidName -> showInvalidName()
            is ViewEvent.ShowError -> showError()
            is ViewEvent.ShowInternetError -> showInternetError()
            is ViewEvent.ShowCyclicAccountForDeletedCustomer -> showCyclicAccountForDeletedCustomer(
                event.errorData
            )
            is ViewEvent.ShowCyclicAccount -> showCyclicAccount(event.info)
            is ViewEvent.ShowKeyboard -> showSoftKeyboard(binding.searchInput)
            is ViewEvent.GoToCustomerScreenAndCloseSearch -> goToCustomerScreenAndCloseSearch(
                event.customerId,
                event.mobile
            )
            is ViewEvent.GoToSupplierScreenAndCloseSearch -> gotoSupplierScreenAndCloseSearch(
                event.supplier.id,
                event.supplier.registered
            )
            is ViewEvent.ReturnAccountSelectionResult -> returnSelectedAccount(event.accountId, event.relation)
            is ViewEvent.ShowPaymentReceived -> showPaymentReceivedDialog(event.customerId, event.amount)
            is ViewEvent.SendReminder -> startActivity(event.intent)
            is ViewEvent.CustomerAddPayment -> {
                startActivity(
                    customerNavigator.get()
                        .getAddPaymentWithExpandedQr(requireContext(), event.customerId, event.source)
                )
            }
            is ViewEvent.CustomerQrDialog -> {
                CustomerProfileDialog.showDialog(childFragmentManager, event.customerId, event.source)
            }
        }
    }

    private fun showPaymentReceivedDialog(customerId: String?, amount: Long) {
        val msg = getString(R.string.received_amount, CurrencyUtil.formatV2(amount))
        collectionNavigator.get().showPaymentReceivedDialog(childFragmentManager, msg, customerId)
    }
}
