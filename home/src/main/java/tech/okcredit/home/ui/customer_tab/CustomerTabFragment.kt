package tech.okcredit.home.ui.customer_tab

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.PropertyValue.CUSTOMER_SYNC_STATUS
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionNavigator
import `in`.okcredit.customer.contract.BulkReminderAnalytics
import `in`.okcredit.customer.contract.CustomerNavigator
import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.frontend.contract.FrontendConstants
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.*
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.referral.contract.RewardsOnSignupTracker
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.delay
import org.jetbrains.annotations.Nullable
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.home.R
import tech.okcredit.home.databinding.FragmentCustomerTabBinding
import tech.okcredit.home.dialogs.LiveSalesQRDialog
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileDialog
import tech.okcredit.home.ui.analytics.HomeTraces
import tech.okcredit.home.ui.bulk_reminder.BulkReminderBanner
import tech.okcredit.home.ui.customer_tab.CustomerTabContract.CustomerTabViewEvent
import tech.okcredit.home.ui.customer_tab.CustomerTabContract.State
import tech.okcredit.home.ui.customer_tab.view.AppLockInAppNotiView
import tech.okcredit.home.ui.customer_tab.view.LiveSalesHomeItemView
import tech.okcredit.home.ui.home.HomeFragment
import tech.okcredit.home.ui.home.views.EmptyFilteredCustomerView
import tech.okcredit.home.ui.home.views.HomeCustomerView
import tech.okcredit.home.ui.home.views.HomeCustomerViewV2
import tech.okcredit.home.ui.home.views.HomeSupplierView
import tech.okcredit.home.ui.homesearch.Sort
import tech.okcredit.home.ui.homesearch.views.FilterView
import tech.okcredit.home.utils.LifeCycle
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CustomerTabFragment :
    BaseFragment<State, CustomerTabViewEvent, CustomerTabContract.Intent>(
        "CustomerTabFragment",
        R.layout.fragment_customer_tab
    ),
    HomeCustomerView.CustomerSelectionListener,
    CustomerTabContract.Listeners,
    LiveSalesHomeItemView.CustomerSelectionListener,
    FilterView.ClearFilterListener,
    @Nullable HomeSupplierView.SupplierSelectionListener,
    @Nullable EmptyFilteredCustomerView.Listener,
    @Nullable AppLockInAppNotiView.AppLockClickListener,
    HomeCustomerViewV2.CustomerSelectionListener,
    BulkReminderBanner.BulkReminderBannerListener {

    private var resetCustomerList = PublishSubject.create<Sort>()

    @Inject
    lateinit var tabController: Lazy<CustomerTabControllerV2>

    @Inject
    lateinit var tracker: Lazy<Tracker>

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var imageLoader: Lazy<IImageLoader>

    @Inject
    lateinit var collectionNavigator: Lazy<CollectionNavigator>

    @Inject
    lateinit var customerNavigator: Lazy<CustomerNavigator>

    @Inject
    lateinit var bulkReminderAnalytics: Lazy<BulkReminderAnalytics>

    @Inject
    lateinit var referralSignupTracker: Lazy<RewardsOnSignupTracker>

    private var alert: Snackbar? = null

    private var abTutorialAutoPlayDispose: Disposable? = null

    private var alertDialog: AlertDialog? = null

    internal val binding: FragmentCustomerTabBinding by viewLifecycleScoped(FragmentCustomerTabBinding::bind)

    companion object {

        @JvmStatic
        fun newInstance() = CustomerTabFragment()
    }

    @AddTrace(name = HomeTraces.OnViewCreated_HomeCustomerTab)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setupViewListeners()
    }

    private fun initView() {
        binding.rvCustomer.layoutManager = LinearLayoutManager(activity)
        binding.rvCustomer.adapter = tabController.get().adapter
        tabController.get().onReferralCloseClicked = { closeReferralTargetBanner() }
        tabController.get().onReferralTransactionInitiated = { onReferralTargetBannerClicked() }
    }

    private val dataObserver by lazy {
        object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                    delay(300)
                    if (isVisible) {
                        binding.rvCustomer.layoutManager?.scrollToPosition(0)
                    }
                }
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                    delay(300)
                    if (isVisible) {
                        binding.rvCustomer.layoutManager?.scrollToPosition(0)
                    }
                }
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                // This is called when we do sorting
                binding.rvCustomer.layoutManager?.scrollToPosition(0)
            }
        }
    }

    private fun setupViewListeners() {
        binding.rvCustomer.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    (parentFragment as? HomeFragment)?.recyclerviewScrollToBottom()
                } else {
                    (parentFragment as? HomeFragment)?.recyclerviewScrollToTop()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                hideSoftKeyboard()
            }
        })

        tabController.get().adapter.registerAdapterDataObserver(dataObserver)
        binding.swipeToRefresh.setOnRefreshListener {
            tracker.get().trackRefresh(PropertyValue.HOME_PAGE)
            binding.swipeToRefresh.isRefreshing = false
            (parentFragment as HomeFragment).onSyncNow()
        }
    }

    override fun onDestroyView() {
        tabController.get().adapter.unregisterAdapterDataObserver(dataObserver)
        super.onDestroyView()
    }

    override fun loadIntent(): UserIntent {
        return CustomerTabContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            resetCustomerList
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    CustomerTabContract.Intent.SetNewSort(it)
                },
        )
    }

    @AddTrace(name = HomeTraces.RENDER_HOME_CUSTOMER_TAB)
    override fun render(state: State) {
        tabController.get().setData(state.list)
        state.customerTabDetails?.customers?.let {
            (parentFragment as HomeFragment).toggleFilter(it.size)
        }
        state.customerTabDetails?.tabCount?.let { (parentFragment as HomeFragment).setTabHeaderCount(this, it) }

        binding.swipeToRefresh.isEnabled = isUserInNormalFlow(state)

        if (state.networkError or state.error or state.isAlertVisible) {
            alert = when {
                state.networkError -> view?.snackbar(
                    getString(R.string.home_no_internet_msg),
                    Snackbar.LENGTH_INDEFINITE
                )
                state.isAlertVisible -> view?.snackbar(state.alertMessage, Snackbar.LENGTH_INDEFINITE)
                else -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_INDEFINITE)
            }
            alert?.show()
        } else {
            alert?.dismiss()
        }
    }

    private fun isUserInNormalFlow(state: State) =
        state.customerTabDetails?.lifeCycle == LifeCycle.NORMAL_FLOW

    override fun onLiveSaleQrSelected(customerId: String) {
        if (getCurrentState().isCollectionActivated) {
            onCustomerProfileSelected(customerId)
        }

        tracker.get().trackEvents(
            Event.VIEW_LINK_PAY,
            screen = PropertyValue.HOME_PAGE,
            relation = PropertyValue.CUSTOMER,
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.ACCOUNT_ID, customerId)
                .add(PropertyKey.COLLECTION_ADOPTED, getCurrentState().isCollectionActivated)
        )
    }

    override fun onLiveItemClicked(customerId: String) {
        pushIntent(CustomerTabContract.Intent.LiveSaleClicked(customerId))
        tracker.get().trackEvents(
            Event.VIEW_LINK_PAY_QR, screen = PropertyValue.HOME_PAGE,
            propertiesMap = PropertiesMap.create()
                .add(PropertyKey.ACCOUNT_ID, customerId)
                .add(PropertyKey.COLLECTION_ADOPTED, getCurrentState().isCollectionActivated)
        )
    }

    override fun onLiveSaleTutorialSeen() {
        pushIntentWithDelay(CustomerTabContract.Intent.LiveSalesTutorialShown)
    }

    override fun onNewSorted(sortType: Sort) {
        resetCustomerList.onNext(sortType)
    }

    override fun onCustomerSelected(customer: Customer) {
        gotoCustomerScreen(customer.id, customer.mobile)
    }

    override fun onCustomerProfileSelected(customer: Customer) {
        if (customer.state != Customer.State.BLOCKED) {
            val fragment = childFragmentManager.findFragmentByTag(CustomerProfileDialog.TAG)
            if (fragment == null || !fragment.isVisible) {
                CustomerProfileDialog.showDialog(childFragmentManager, customer.id)
            }
        }
    }

    override fun onSupplierSelected(supplierId: String, registered: Boolean) {
        gotoSupplierScreen(supplierId, registered)
    }

    override fun onSupplierProfileSelected(supplier: Supplier) {
        gotoSupplierScreen(supplier.id, supplier.registered)
    }

    private fun goToLiveSaleQRDialog(
        customerCollectionProfile: CollectionCustomerProfile,
        customer: Customer,
        business: Business?,
    ) {
        tracker.get().trackViewLiveSaleQr(PropertyValue.HOME_PAGE)
        LiveSalesQRDialog.inject(imageLoader.get(), tracker.get())
        alertDialog =
            LiveSalesQRDialog.show(activity as AppCompatActivity, customer, customerCollectionProfile, business)
        alertDialog?.show()
    }

    /************************ Navigation  ***********************/
    fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.get().goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    private fun gotoCustomerScreen(customerId: String, mobile: String?) {

        activity?.runOnUiThread {
            tracker.get().trackViewRelationship(
                list = PropertyValue.FALSE,
                relation = PropertyValue.CUSTOMER,
                search = PropertyValue.FALSE,
                accountId = customerId,
                commonLedger = getCurrentState().supplierCreditEnabledCustomerIds.contains(customerId),
                customerSyncStatus = getCustomerSyncStatus(
                    getCurrentState().customerTabDetails?.customers?.firstOrNull { it.id == customerId }
                )
            )
            legacyNavigator.get().goToCustomerScreen(requireActivity(), customerId)
        }
    }

    private fun gotoSupplierScreen(supplierId: String, registered: Boolean) {
        activity?.runOnUiThread {
            hideSoftKeyboard()
            tracker.get().trackViewRelationshipV1(
                list = PropertyValue.FALSE,
                relation = PropertyValue.SUPPLIER,
                search = PropertyValue.TRUE,
                screen = PropertyValue.CUSTOMER,
                accountId = supplierId,
                commonLedger = registered
            )
            legacyNavigator.get().goToSupplierScreen(requireActivity(), supplierId)
        }
    }

    private fun goLoLiveSalesScreen() {
        tracker.get().trackViewLiveSales()
        collectionNavigator.get().goToOnlinePaymentsList(requireContext())
        activity?.runOnUiThread {
            activity?.runOnUiThread {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (alertDialog != null && alertDialog?.isShowing == true) {
            alertDialog?.dismiss()
        }
    }

    override fun onPause() {
        super.onPause()
        abTutorialAutoPlayDispose?.dispose()
    }

    override fun onFilterCleared(source: String) {
        (parentFragment as FilterListener).clearFilter(source)
    }

    interface FilterListener {
        fun clearFilter(source: String)
    }

    override fun onClearFilterCleared(sort: Sort, source: String) {
        (parentFragment as FilterListener).clearFilter(source)
    }

    override fun onAppLockSetup() {
        tracker.get().trackV1(Event.APPLOCK_CARD_CLICKED, PropertyValue.HOME_PAGE)
        context?.let { legacyNavigator.get().goToSystemAppLockScreen(it, FrontendConstants.LOCK_SETUP_INAPP_CARD) }
    }

    override fun onAppLockClose() {
        tracker.get().trackAppLockCardCancelled(PropertyValue.HOME_PAGE, PropertyValue.CARD)
        pushIntent(CustomerTabContract.Intent.AppLockInAppCancelled)
    }

    override fun handleViewEvent(event: CustomerTabViewEvent) {
        when (event) {
            is CustomerTabViewEvent.GotoLogin -> gotoLogin()
            is CustomerTabViewEvent.GotoSupplierTab -> (parentFragment as? HomeFragment)?.openSupplierTab()
            is CustomerTabViewEvent.GoLoLiveSalesScreen -> goLoLiveSalesScreen()
            is CustomerTabViewEvent.GoToLiveSaleQRDialog -> goToLiveSaleQRDialog(
                event.collectionCustomerProfile,
                event.customer,
                event.business
            )
        }
    }

    private fun onReferralTargetBannerClicked() {
        referralSignupTracker.get().trackTargetBannerInteracted("Banner Clicked")
        pushIntent(CustomerTabContract.Intent.HideTargetBanner)
    }

    private fun closeReferralTargetBanner() {
        referralSignupTracker.get().trackTargetBannerInteracted("Close Button")
        pushIntent(CustomerTabContract.Intent.CloseTargetBanner)
    }

    override fun onCustomerSelected(customerId: String) {
        gotoCustomerScreen(customerId, null)
    }

    override fun onCustomerProfileSelected(customerId: String) {
        val isProfilePicClickable = if (isStateInitialized()) {
            getCurrentState().isProfilePicClickable
        } else {
            false
        }
        val customer =
            if (isStateInitialized()) {
                getCurrentState().customerTabDetails?.customers?.firstOrNull { it.id == customerId }
            } else {
                null
            }
        if (customer != null && customer.state != Customer.State.BLOCKED && isProfilePicClickable) {
            tracker.get().trackEvents(
                Event.PROFILE_ICON_CLICKED,
                screen = PropertyValue.HOME_PAGE_SCREEN,
                relation = PropertyValue.CUSTOMER,
                propertiesMap = PropertiesMap.create()
                    .add(PropertyKey.ACCOUNT_ID, customerId)
                    .add(CUSTOMER_SYNC_STATUS, getCustomerSyncStatus(customer))
            )
            val fragment = childFragmentManager.findFragmentByTag(CustomerProfileDialog.TAG)
            if (fragment == null || !fragment.isVisible) {
                CustomerProfileDialog.showDialog(childFragmentManager, customerId)
            }
        }
    }

    private fun getCustomerSyncStatus(customer: Customer?): String {
        return when (customer?.customerSyncStatus) {
            IMMUTABLE.code -> "Immutable"
            DIRTY.code -> "Dirty"
            CLEAN.code -> "Clean"
            else -> "Unknown"
        }
    }

    override fun bulkReminderBannerClicked() {
        val state = if (isStateInitialized()) getCurrentState() else null
        bulkReminderAnalytics.get().trackEntryPointClicked(
            false,
            state?.bulkReminderState?.totalReminders ?: 0
        )
        customerNavigator.get().goToBulkReminderV2Activity(requireContext())
    }

    override fun bulkReminderBellIconClicked() {
        val state = if (isStateInitialized()) getCurrentState() else null
        bulkReminderAnalytics.get().trackEntryPointClicked(
            true,
            state?.bulkReminderState?.totalReminders ?: 0
        )
        customerNavigator.get().goToBulkReminderV2Activity(requireContext())
    }
}
