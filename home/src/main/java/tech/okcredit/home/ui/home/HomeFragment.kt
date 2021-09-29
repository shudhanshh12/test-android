package tech.okcredit.home.ui.home

import `in`.okcredit.analytics.*
import `in`.okcredit.analytics.PropertyValue.HOME_PAGE
import `in`.okcredit.backend._offline.usecase.AppShortcutHelper
import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.backend.service.in_app_notification.MixPanelInAppNotificationTypes
import `in`.okcredit.backend.utils.BroadcastHelper
import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection.contract.rxPreference.CollectionRxPreference
import `in`.okcredit.communication_inappnotification.contract.LocalInAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.ui.local.TapTargetLocal
import `in`.okcredit.communication_inappnotification.contract.ui.local.TooltipLocal
import `in`.okcredit.customer.contract.CustomerNavigator
import `in`.okcredit.customer.contract.RelationshipType
import `in`.okcredit.dynamicview.DynamicViewKit
import `in`.okcredit.dynamicview.TargetSpec
import `in`.okcredit.dynamicview.component.toolbar.ToolbarComponentModel
import `in`.okcredit.fileupload.utils.IResourceFinder
import `in`.okcredit.frontend.contract.FrontendConstants
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.BusinessConstants
import `in`.okcredit.merchant.contract.BusinessNavigator
import `in`.okcredit.merchant.suppliercredit.model.NotificationReminderForUi
import `in`.okcredit.merchant.suppliercredit.server.internal.ApiEntityMapper
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import `in`.okcredit.referral.contract.ReferralNavigator
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.addTo
import `in`.okcredit.storesms.data.worker.SyncRawSmsWorker
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents
import `in`.okcredit.web.WebExperiment
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.END
import androidx.constraintlayout.widget.ConstraintSet.START
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
import com.bugfender.sdk.Bugfender
import com.bugfender.sdk.ui.FeedbackStyle
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import com.mixpanel.android.mpmetrics.InAppNotification
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.OnBalloonClickListener
import com.skydoves.balloon.createBalloon
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.app_contract.AppShortcutAdder
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.contacts.contract.ContactsNavigator
import tech.okcredit.contract.AppLock
import tech.okcredit.contract.AppLockTracker
import tech.okcredit.contract.Constants.IS_AUTHENTICATED
import tech.okcredit.contract.Constants.SECURITY_PIN_CHANGED
import tech.okcredit.contract.Constants.SECURITY_PIN_SET
import tech.okcredit.contract.OnSetPinClickListener
import tech.okcredit.contract.OnUpdatePinClickListener
import tech.okcredit.home.R
import tech.okcredit.home.databinding.FragmentHomeBinding
import tech.okcredit.home.dialogs.*
import tech.okcredit.home.dialogs.BottomSheetInAppRating.OnBottomSheetFragmentListener
import tech.okcredit.home.ui.activity.HomeSearchActivity
import tech.okcredit.home.ui.analytics.HomeEventTracker
import tech.okcredit.home.ui.analytics.HomeEventTracker.Companion.HOME_UN_SYNCED_ACTIVITY
import tech.okcredit.home.ui.analytics.HomeEventTracker.Companion.NOTIFICATION_CLEARED
import tech.okcredit.home.ui.analytics.HomeEventTracker.Companion.NOTIFICATION_CLICKED
import tech.okcredit.home.ui.analytics.HomeEventTracker.Companion.NOTIFICATION_DISPLAYED
import tech.okcredit.home.ui.analytics.HomeEventTracker.Companion.NOTIFICATION_TBD
import tech.okcredit.home.ui.analytics.HomeEventTracker.Companion.SET_NEW_PIN
import tech.okcredit.home.ui.analytics.HomeEventTracker.Companion.TYPE_BULK_REMINDER
import tech.okcredit.home.ui.analytics.HomeEventTracker.Companion.TYPE_SET_SECURITY_PIN
import tech.okcredit.home.ui.analytics.HomeEventTracker.Companion.TYPE_UPDATE_SECURITY_PIN
import tech.okcredit.home.ui.analytics.HomeEventTracker.Companion.UPDATE_NEW_PIN
import tech.okcredit.home.ui.analytics.HomeEventTracker.Objects
import tech.okcredit.home.ui.analytics.HomeTraces
import tech.okcredit.home.ui.customer_tab.CustomerTabFragment
import tech.okcredit.home.ui.customer_tab.CustomerTabFragment.FilterListener
import tech.okcredit.home.ui.home.HomeContract.*
import tech.okcredit.home.ui.home.dialog.AddBankDetailBottomSheet
import tech.okcredit.home.ui.home.helpers.HomeTabHelpers
import tech.okcredit.home.ui.home.supplier.SupplierSortFragment
import tech.okcredit.home.ui.home.supplier.SupplierSortFragment.SupplierSortListener
import tech.okcredit.home.ui.home.views.AccountSortBottomSheetFragment
import tech.okcredit.home.ui.home.views.AccountSortBottomSheetFragment.AccountSortListener
import tech.okcredit.home.ui.home.views.HomeScreenTapToSyncView
import tech.okcredit.home.ui.homesearch.HomeConstants
import tech.okcredit.home.ui.homesearch.HomeConstants.HomeTab.*
import tech.okcredit.home.ui.homesearch.Sort
import tech.okcredit.home.ui.payables_onboarding.HomeTabOrderList
import tech.okcredit.home.ui.reminder.bulk.BulkReminderBottomSheet
import tech.okcredit.home.ui.supplier_tab.SupplierTabFragment
import tech.okcredit.home.utils.TextDrawableUtils
import tech.okcredit.user_migration.contract.UserMigrationNavigator
import timber.log.Timber
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

class HomeFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "HomeFragment",
        contentLayoutId = R.layout.fragment_home
    ),
    InstallStateUpdatedListener,
    HomeScreenTapToSyncView.Listener,
    OnBottomSheetFragmentListener,
    FilterListener,
    AccountSortListener,
    SupplierSortListener,
    OnSetPinClickListener,
    OnUpdatePinClickListener {

    companion object {
        const val REQUEST_CODE_PLAY_STORE_UPDATE = 2
        const val REQUEST_CODE_PLAY_STORE_IMMEDIATE_UPDATE = 3

        private const val SYNC_RAW_SMS_WORKER_INTERVAL_IN_HOURS_KEY = "sync_raw_sms_worker_interval_in_hours"

        const val TAG = "HomeFragment"

        @JvmStatic
        fun newInstance() = HomeFragment()
    }

    data class InAppNavigationObject(
        val inAppNotificationType: String? = "",
        val inAppNavigationDesc: String? = "",
        val inAppNavigationButtonText: String? = "",
    )

    private var firstSupplierName: String? = null
    private var firstSupplierBalance: Long? = null
    private var isSupplierVideoPlaying: Boolean = false
    private var accountSortBottomSheetFragment: AccountSortBottomSheetFragment? = null
    private var supplierSortFragment: SupplierSortFragment? = null
    private var adapter: HomePagerAdapter? = null
    private lateinit var onPageChangedCallback: ViewPager2.OnPageChangeCallback

    private var isSinglePressed = false
    private var supplierCount = 0

    private val homeController = HomeController(this)

    @Inject
    internal lateinit var businessNavigator: Lazy<BusinessNavigator>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var collectionNavigator: Lazy<CollectionNavigator>

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    @Inject
    internal lateinit var homeEventTracker: Lazy<HomeEventTracker>

    @Inject
    internal lateinit var resourceFinder: Lazy<IResourceFinder>

    @Inject
    lateinit var appUpdateManager: Lazy<AppUpdateManager>

    @Inject
    internal lateinit var applock: Lazy<AppLock>

    @Inject
    internal lateinit var appLockTracker: Lazy<AppLockTracker>

    @Inject
    lateinit var mixpanelAPI: Lazy<MixpanelAPI>

    @Inject
    lateinit var dynamicViewKit: Lazy<DynamicViewKit>

    @Inject
    lateinit var onboardingPreferences: Lazy<OnboardingPreferences>

    @Inject
    lateinit var broadcastHelper: Lazy<BroadcastHelper>

    @Inject
    lateinit var shortcutHelper: Lazy<AppShortcutHelper>

    @Inject
    lateinit var localeManager: Lazy<LocaleManager>

    @Inject
    lateinit var localInAppNotificationHandler: Lazy<LocalInAppNotificationHandler>

    @Inject
    lateinit var userMigrationNavigator: Lazy<UserMigrationNavigator>

    @Inject
    lateinit var referralNavigator: Lazy<ReferralNavigator>

    @Inject
    lateinit var contactsNavigator: Lazy<ContactsNavigator>

    @Inject
    lateinit var customerNavigator: Lazy<CustomerNavigator>

    fun isSupplierSectionVisible() = binding.viewPager.currentItem == 1

    // to avoid race condition TODO Fix this later
    private var homeTabOrderList: HomeTabOrderList = HomeTabOrderList(
        listOf(CUSTOMER_TAB, SUPPLIER_TAB)
    )

    private val binding: FragmentHomeBinding by viewLifecycleScoped(FragmentHomeBinding::bind)

    private var supplierTabEducationTapTarget: MaterialTapTargetPrompt? = null

    @AddTrace(name = HomeTraces.OnViewCreated_Home)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            pushIntent(Intent.SetupViewPager)
        }

        setupViewListeners()
        initClickListener()
        tracker.get().trackViewRelationship(
            PropertyValue.TRUE,
            PropertyValue.CUSTOMER,
            PropertyValue.FALSE,
            commonLedger = false
        )
        homeEventTracker.get().homeScreenViewed()

        with(binding.rvUnSyncedTransactions) {
            layoutManager = LinearLayoutManager(context)
            adapter = homeController.adapter
        }

        binding.inappNotifCrossIcon.setOnClickListener {
            if (getCurrentState().inAppNavigationObject?.inAppNotificationType == MixPanelInAppNotificationTypes.INAPP_MERCHANT_PROFILE) {
                tracker.get().trackEvents(
                    Event.IN_APP_NOTI_CLEARED,
                    type = PropertyValue.MERCHANT_PROFILE,
                    screen = HOME_PAGE
                )
            } else if (getCurrentState().inAppNavigationObject?.inAppNotificationType == MixPanelInAppNotificationTypes.INAPP_ADDRESS) {
                tracker.get().trackEvents(
                    Event.IN_APP_NOTI_CLEARED,
                    type = PropertyValue.MERCHANT_ADDRESS,
                    screen = HOME_PAGE
                )
            }

            pushIntent(Intent.UpdateInAppNavigationObject(InAppNavigationObject("", "", "")))
            binding.inappContainer.gone()
        }

        binding.inappNavigateText.setOnClickListener {
            if (getCurrentState().inAppNavigationObject?.inAppNotificationType == MixPanelInAppNotificationTypes.INAPP_MERCHANT_PROFILE) {
                tracker.get().trackEvents(
                    Event.IN_APP_NOTI_CLICKED,
                    type = PropertyValue.MERCHANT_PROFILE,
                    screen = HOME_PAGE
                )
                gotoMerchantProfileAndShowImage()
            } else if (getCurrentState().inAppNavigationObject?.inAppNotificationType == MixPanelInAppNotificationTypes.INAPP_ADDRESS) {
                tracker.get().trackEvents(
                    Event.IN_APP_NOTI_CLICKED,
                    type = PropertyValue.MERCHANT_ADDRESS,
                    screen = HOME_PAGE
                )
                goToAddressScreen()
            }

            pushIntent(Intent.UpdateInAppNavigationObject(InAppNavigationObject("", "", "")))
        }
        binding.rootLayout.setTracker(performanceTracker)
        setupRawSmsSyncer()
    }

    private fun showAppReviewPopup() {
        val reviewManager = ReviewManagerFactory.create(requireContext())

        val requestFlow = reviewManager.requestReviewFlow()
        requestFlow.addOnCompleteListener { request ->
            if (isAdded && request.isSuccessful) {
                val flow = reviewManager.launchReviewFlow(requireActivity(), request.result)
                homeEventTracker.get().trackInAppReviewViewed()
                flow.addOnSuccessListener {
                    homeEventTracker.get().trackInAppReviewDone()
                }
            }
        }
    }

    private fun initClickListener() {
        binding.imgToolbarBusiness.setOnClickListener {
            businessNavigator.get().showSwitchBusinessDialog(childFragmentManager, "home_screen")
        }
    }

    private fun setupViewPager(homeTabOrder: HomeTabOrderList) {
        adapter = HomePagerAdapter(
            childFragmentManager,
            lifecycle,
            homeTabOrder.list,
        )
        binding.viewPager.offscreenPageLimit = OFFSCREEN_PAGE_LIMIT_DEFAULT
        binding.viewPager.adapter = adapter

        onPageChangedCallback = object : ViewPager2.OnPageChangeCallback() {
            private var first = true

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (first && positionOffset == 0f && positionOffsetPixels == 0) {
                    onPageSelected(binding.viewPager.currentItem)
                    first = false
                }
            }

            override fun onPageSelected(position: Int) {
                when (homeTabOrderList.list[position]) {
                    CUSTOMER_TAB -> {
                        val state = if (isStateInitialized()) getCurrentState() else null
                        if (state?.hideBigButtonAndNudge == false &&
                            !state.canShowUploadButton
                        ) {
                            binding.btnAddRelationship.text = getString(R.string.t_001_addrel_cta_add_from_contacts)
                        } else {
                            binding.btnAddRelationship.text = getString(R.string.add_customer)
                        }
                        tracker.get()
                            .trackViewRelationship(
                                PropertyValue.TRUE, PropertyValue.CUSTOMER, PropertyValue.FALSE,
                                commonLedger = false
                            )
                        showInAppNavigationPopup(true)
                        homeEventTracker.get()
                            .trackHomeTabViewed(
                                Objects.CUSTOMER_TAB,
                                state?.isPayablesExperimentEnabled
                            )
                    }
                    SUPPLIER_TAB -> {
                        val state = if (isStateInitialized()) getCurrentState() else null
                        val supplierButtonText = if (
                            state != null &&
                            state.isMerchantFromCollectionCampaign
                        ) {
                            getString(R.string.add_seller)
                        } else {
                            getString(R.string.add_supplier)
                        }

                        if (state?.hideBigButtonAndNudge == false &&
                            !state.canShowUploadButton
                        ) {
                            binding.btnAddRelationship.text = getString(R.string.t_001_addrel_cta_add_from_contacts)
                        } else {
                            binding.btnAddRelationship.text = supplierButtonText
                        }

                        tracker.get()
                            .trackViewRelationship(
                                PropertyValue.TRUE, PropertyValue.SUPPLIER, PropertyValue.FALSE,
                                commonLedger = false
                            )
                        showInAppNavigationPopup(false)
                        val isPayablesExperimentEnabled = if (isStateInitialized())
                            getCurrentState().isPayablesExperimentEnabled
                        else null
                        homeEventTracker.get()
                            .trackHomeTabViewed(
                                Objects.SUPPLIER_TAB,
                                isPayablesExperimentEnabled
                            )
                    }
                }
                showAddFabBottomButton()
                onFocusChanged(position)
            }
        }
        binding.viewPager.registerOnPageChangeCallback(onPageChangedCallback)
        AnimationUtils.bounce(binding.ivHand)
    }

    private fun onFocusChanged(position: Int) {
        when (homeTabOrderList.list[position]) {
            SUPPLIER_TAB -> {
                getSupplierTabFragment()?.onFocusGained()
                hideSoftKeyboard()
            }
            CUSTOMER_TAB -> {
                getSupplierTabFragment()?.onFocusRemoved()
            }
        }
    }

    private fun showFirstSupplierEducation() {
        if (SUPPLIER_TAB == homeTabOrderList.list[binding.viewPager.currentItem])
            getSupplierTabFragment()?.showFirstSupplierEducation()
    }

    private fun showPayOnlineEducationHome() {
        if (SUPPLIER_TAB == homeTabOrderList.list[binding.viewPager.currentItem]) {
            getSupplierTabFragment()?.showPayOnlineEducationHome()
            pushIntent(
                Intent.RxPreferenceBoolean(
                    RxSharedPrefValues.IS_PAY_ONLINE_EDUCATION_HOME_SHOWN,
                    true,
                    Scope.Individual
                )
            )
        }
    }

    private fun setupTabLayout(homeTabOrder: HomeTabOrderList, isPayablesExperimentEnabled: Boolean?) {
        binding.accountHeaderContainer.visible()
        HomeTabHelpers.setupTabLayout(
            binding.tabLayout,
            binding.viewPager,
            activity,
            homeTabOrder,
            homeEventTracker.get(),
            isPayablesExperimentEnabled
        )
    }

    private fun setupRawSmsSyncer() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            if (ContextCompat.checkSelfPermission(
                    this@HomeFragment.requireContext(),
                    android.Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                SyncRawSmsWorker.scheduleSyncRawSMStoServer(
                    this@HomeFragment.requireContext(),
                    syncIntervalInHours = firebaseRemoteConfig.get().getLong(SYNC_RAW_SMS_WORKER_INTERVAL_IN_HOURS_KEY)
                )
            }
        }
    }

    private fun setupViewListeners() {
        binding.sortFilter.clicks()
            .throttleFirst(300, MILLISECONDS)
            .subscribe {
                when (homeTabOrderList.list[binding.viewPager.currentItem]) {
                    CUSTOMER_TAB -> {
                        var flow = "New"
                        if (Sort.sortApplied) {
                            flow = "Update"
                        }
                        tracker.get().trackSelectFilter(PropertyValue.CUSTOMER, flow)
                        accountSortBottomSheetFragment = AccountSortBottomSheetFragment.newInstance()
                        accountSortBottomSheetFragment?.show(childFragmentManager, AccountSortBottomSheetFragment.TAG)
                    }
                    SUPPLIER_TAB -> {
                        broadcastHelper.get()
                            .sendBroadcast(
                                requireContext(),
                                android.content.Intent(BroadcastHelper.IntentFilters.PlayerPause)
                            )
                        supplierSortFragment = SupplierSortFragment.newInstance()
                        supplierSortFragment?.show(childFragmentManager, SupplierSortFragment.TAG)
                    }
                }
            }

        binding.btnAddRelationship
            .clicks()
            .throttleFirst(400, MILLISECONDS)
            .map { onAddRelationshipClicked() }
            .subscribe()

        binding.addRelationshipManually
            .clicks()
            .throttleFirst(400, MILLISECONDS)
            .map {
                pushIntent(Intent.ContactPermissionAskedOnce)
                openAddRelationshipActivity(
                    true,
                    isContactPermissionGranted = false
                )
            }
            .subscribe()

        binding.upload
            .clicks()
            .throttleFirst(400, MILLISECONDS)
            .map {
                homeEventTracker.get().trackUserMigrationInteracted("Upload Button")
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    homeEventTracker.get().trackStoragePermissionDialog()
                    Permission.requestStoragePermission(
                        activity as AppCompatActivity,
                        object : IPermissionListener {
                            override fun onPermissionGrantedFirstTime() {
                            }

                            override fun onPermissionGranted() {
                                homeEventTracker.get().trackStoragePermissionInteracted("Granted")
                                userMigrationNavigator.get().showUploadOptionBottomSheet(childFragmentManager)
                            }

                            override fun onPermissionDenied() {
                                homeEventTracker.get().trackStoragePermissionInteracted("Denied")
                            }

                            override fun onPermissionPermanentlyDenied() {
                            }
                        }
                    )
                } else {
                    userMigrationNavigator.get().showUploadOptionBottomSheet(childFragmentManager)
                }
            }.subscribe()

        binding.toolbarLayer
            .clicks()
            .throttleFirst(300, MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startSearchActivity() }

        binding.ivSearchIcon.setOnClickListener { startSearchActivity() }
    }

    private fun startSearchActivity() {
        context?.startActivity(
            HomeSearchActivity.startingIntent(
                requireActivity(),
                homeTabOrderList.list[binding.viewPager.currentItem]
            )
        )
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            Observable.just(Intent.OnResume),
        )
    }

    private fun showSupplierTabEducation(subText: String, focalRadius: Float) {
        Analytics.track(
            AnalyticsEvents.IN_APP_NOTI_DISPLAYED,
            EventProperties.create()
                .with(PropertyKey.TYPE, "supplier_tab")
                .with(PropertyKey.SCREEN, "home")
        )

        lifecycleScope.launch {
            if (supplierTabEducationTapTarget == null) {

                supplierTabEducationTapTarget =
                    localInAppNotificationHandler.get()
                        .generateTapTarget(
                            weakScreen = WeakReference(requireActivity()),
                            tapTarget = TapTargetLocal(
                                screenName = label,
                                targetView = WeakReference(binding.tabLayout.getTabAt(1)?.customView),
                                title = getString(R.string.home_tab_supplier_section),
                                subtitle = subText,
                                focalRadius = focalRadius,
                                listener = { _, state ->
                                    if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                        Analytics.track(
                                            AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                            EventProperties.create()
                                                .with(PropertyKey.TYPE, "supplier_tab")
                                                .with("focal_area", true)
                                                .with(PropertyKey.SCREEN, "home")
                                        )
                                        pushIntent(
                                            Intent.RxPreferenceBoolean(
                                                RxSharedPrefValues.SHOULD_SHOW_SUPPLIER_TAB_EDUCATION,
                                                false,
                                                Scope.Individual
                                            )
                                        )
                                        openSupplierTab()
                                    } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                                        Analytics.track(
                                            AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                            EventProperties.create()
                                                .with(PropertyKey.TYPE, "supplier_tab")
                                                .with("focal_area", false)
                                                .with(PropertyKey.SCREEN, "home")
                                        )
                                        pushIntent(
                                            Intent.RxPreferenceBoolean(
                                                RxSharedPrefValues.SHOULD_SHOW_SUPPLIER_TAB_EDUCATION,
                                                false,
                                                Scope.Individual
                                            )
                                        )
                                        try {
                                            openSupplierTab()
                                        } catch (e: java.lang.Exception) {
                                        }
                                    }
                                }
                            )
                        )
            }
        }
    }

    internal fun openSupplierTab() {
        hideSoftKeyboard()
        homeTabOrderList.list.indexOf(SUPPLIER_TAB)
            .takeUnless { it < 0 }
            ?.let { binding.viewPager.setCurrentItem(it, true) }
    }

    internal fun openCustomerTab() {
        hideSoftKeyboard()
        homeTabOrderList.list.indexOf(CUSTOMER_TAB)
            .takeUnless { it < 0 }
            ?.let { binding.viewPager.setCurrentItem(it, true) }
    }

    private fun onAddRelationshipClicked() {
        if (
            !Permission.isContactPermissionAlreadyGranted(requireContext()) &&
            !getCurrentState().isContactPermissionAskedOnce
        ) {
            pushIntent(Intent.ContactPermissionAskedOnce)
            tracker.get().trackContactsPermissionPopUp(
                "Add Relation",
                "Home Screen",
                source = "Fab",
                relation = if (CUSTOMER_TAB == homeTabOrderList.list[binding.viewPager.currentItem])
                    "Customer" else "Supplier",
                defaultMode = "Unknown"
            )
            Permission.requestContactPermission(
                requireActivity(),
                object : IPermissionListener {
                    override fun onPermissionGrantedFirstTime() {
                    }

                    override fun onPermissionGranted() {
                        openAddRelationshipActivity(
                            isContactPermissionGranted = true
                        )
                    }

                    override fun onPermissionDenied() {
                        openAddRelationshipActivity(
                            isContactPermissionGranted = false
                        )
                    }

                    override fun onPermissionPermanentlyDenied() {
                    }
                }
            )
        } else {
            openAddRelationshipActivity(
                isContactPermissionGranted = Permission.isContactPermissionAlreadyGranted(requireContext())
            )
        }
    }

    internal fun openAddRelationshipActivity(
        showManualFlow: Boolean = false,
        isContactPermissionGranted: Boolean,
    ) {
        when (homeTabOrderList.list[binding.viewPager.currentItem]) {
            CUSTOMER_TAB -> {
                tracker.get().trackAddRelationshipStartedFlows(
                    relation = PropertyValue.CUSTOMER,
                    search = PropertyValue.FALSE,
                    type = PropertyValue.FAB,
                    contact = PropertyValue.FALSE,
                    source = PropertyValue.FAB,
                    flow = "Add Relation",
                    defaultMode = if (isContactPermissionGranted) "Contact" else "Manual"
                )
                this.startActivity(
                    customerNavigator.get().goToAddRelationshipActivity(
                        requireContext(),
                        RelationshipType.ADD_CUSTOMER,
                        false,
                        showManualFlow = showManualFlow
                    )
                )
            }
            SUPPLIER_TAB -> {
                tracker.get().trackAddRelationshipStartedFlows(
                    relation = PropertyValue.SUPPLIER,
                    search = PropertyValue.FALSE,
                    type = PropertyValue.FAB,
                    contact = PropertyValue.FALSE,
                    source = PropertyValue.FAB,
                    flow = "Add Relation",
                    defaultMode = if (isContactPermissionGranted) "Contact" else "Manual"
                )
                this.startActivity(
                    customerNavigator.get().goToAddRelationshipActivity(
                        requireContext(),
                        RelationshipType.ADD_SUPPLIER,
                        false,
                        showManualFlow = showManualFlow
                    )
                )
            }
        }
    }

    private fun showAddSupplierEducation() {
        Analytics.track(
            AnalyticsEvents.IN_APP_NOTI_DISPLAYED,
            EventProperties.create()
                .with(PropertyKey.TYPE, "add_supplier")
                .with(PropertyKey.SCREEN, "home")
        )

        lifecycleScope.launch {
            localInAppNotificationHandler.get()
                .generateTapTarget(
                    weakScreen = WeakReference(requireActivity()),
                    tapTarget = TapTargetLocal(
                        screenName = label,
                        targetView = WeakReference(binding.btnAddRelationship),
                        title = getString(R.string.supplier_tab_education),
                        titleGravity = Gravity.END,
                        padding = 22f,
                        listener = { _, state ->
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                Analytics.track(
                                    AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                    EventProperties.create()
                                        .with(PropertyKey.TYPE, "add_supplier")
                                        .with("focal_area", true)
                                        .with(PropertyKey.SCREEN, "home")
                                )
                                this@HomeFragment.startActivity(
                                    customerNavigator.get().goToAddRelationshipActivity(
                                        requireContext(),
                                        RelationshipType.ADD_SUPPLIER,
                                        false,
                                        showManualFlow = false
                                    )
                                )
                            } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                                Analytics.track(
                                    AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                    EventProperties.create()
                                        .with(PropertyKey.TYPE, "add_supplier")
                                        .with("focal_area", false)
                                        .with(PropertyKey.SCREEN, "home")
                                )
                                this@HomeFragment.startActivity(
                                    customerNavigator.get().goToAddRelationshipActivity(
                                        requireContext(),
                                        RelationshipType.ADD_SUPPLIER,
                                        false,
                                        showManualFlow = false
                                    )
                                )
                            }
                        }
                    )
                )
            pushIntent(
                Intent.RxPreferenceBoolean(
                    RxSharedPrefValues.SHOULD_SHOW_ADD_SUPPLIER_TAB_EDUCATION,
                    false,
                    Scope.Individual
                )
            )
        }
    }

    @AddTrace(name = HomeTraces.RENDER_HOME)
    override fun render(state: State) {

        binding.apply {
            if (adapter?.itemCount == 2) {
                if (state.isMerchantFromCollectionCampaign) {
                    tabLayout.getTabAt(1)?.customView?.findViewById<TextView>(R.id.tvTabTitle)
                        ?.text = getString((R.string.seller)).toUpperCase()
                } else {
                    tabLayout.getTabAt(1)?.customView?.findViewById<TextView>(R.id.tvTabTitle)
                        ?.text = getString(R.string.suppliers).toUpperCase()
                }
            }

            renderFullSizeAddCustomerCTA(state)

            if (state.canShowFilterOption) {
                sortFilter.visible()
                tabLayout.visible()
            }
        }

        homeController.setState(state)

        binding.upload.isVisible = state.canShowUploadButton &&
            CUSTOMER_TAB == homeTabOrderList.list[binding.viewPager.currentItem]

        state.inAppNavigationObject?.let {
            if (!state.inAppNavigationObject.inAppNotificationType.isNullOrEmpty() &&
                state.showInAppNavigationPopup
            ) {
                binding.inappNotifDesc.text = state.inAppNavigationObject.inAppNavigationDesc
                binding.inappNavigateText.text = state.inAppNavigationObject.inAppNavigationButtonText

                val drawable =
                    when (state.inAppNavigationObject.inAppNotificationType) {
                        MixPanelInAppNotificationTypes.INAPP_MERCHANT_PROFILE -> {
                            R.drawable.ic_camera_56
                        }
                        MixPanelInAppNotificationTypes.INAPP_ADDRESS -> {
                            R.drawable.ic_location
                        }
                        else -> {
                            R.drawable.ic_screen_lock_portrait
                        }
                    }

                binding.inappNotifIcon.setImageResource(drawable)
                binding.inappNotifIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey800))

                binding.inappContainer.visible()
            } else {
                binding.inappContainer.gone()
            }
        }

        renderMultipleAccountsEntryPoint(state)

        renderToolbarIconCustomization(state)

        if (state.isPayOnlineEducationHomeShown == false && supplierCount > 0) {
            openSupplierTab()
            showPayOnlineEducationHome()
        } else if (SUPPLIER_TAB == homeTabOrderList.list[binding.viewPager.currentItem] &&
            isSupplierVideoPlaying.not()
        ) {
            if (state.showFirstSupplierEducation && supplierCount > 0) {
                showFirstSupplierEducation()
            } else if (state.showAddSupplierEducation) {
                showAddSupplierEducation()
            }
        }
    }

    private fun renderFullSizeAddCustomerCTA(state: State) {

        binding.ivHand.isVisible = !state.hideBigButtonAndNudge &&
            !state.canShowUploadButton

        if (!state.hideBigButtonAndNudge &&
            !state.canShowUploadButton
        ) {
            binding.btnAddRelationship.text = getString(R.string.t_001_addrel_cta_add_from_contacts)
            binding.addRelationshipManually.visible()
            binding.inappContainer.isVisible = false
            val constraints = ConstraintSet()
            constraints.clone(binding.rootLayout)
            constraints.clear(R.id.btnAddRelationship)
            constraints.connect(
                R.id.btnAddRelationship,
                START,
                ConstraintSet.PARENT_ID,
                START,
                48
            )
            constraints.connect(
                R.id.btnAddRelationship,
                END,
                ConstraintSet.PARENT_ID,
                END,
                48
            )
            constraints.connect(
                R.id.btnAddRelationship,
                ConstraintSet.BOTTOM,
                R.id.add_relationship_manually,
                ConstraintSet.TOP,
                48
            )
            constraints.connect(
                R.id.btnAddRelationship,
                ConstraintSet.TOP,
                R.id.add_relationship_guideline,
                ConstraintSet.BOTTOM
            )
            constraints.constrainWidth(R.id.btnAddRelationship, ConstraintSet.MATCH_CONSTRAINT)
            constraints.constrainHeight(R.id.btnAddRelationship, ConstraintSet.WRAP_CONTENT)
            constraints.applyTo(binding.rootLayout)
        } else {
            binding.addRelationshipManually.gone()
            setAddRelationshipText()
            val constraints = ConstraintSet()
            constraints.clone(binding.rootLayout)
            constraints.clear(R.id.btnAddRelationship)
            binding.ivHand.gone()
            constraints.connect(
                R.id.btnAddRelationship,
                END,
                ConstraintSet.PARENT_ID,
                END,
                48
            )
            constraints.connect(
                R.id.btnAddRelationship,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM,
                48
            )
            constraints.constrainWidth(R.id.btnAddRelationship, ConstraintSet.WRAP_CONTENT)
            constraints.constrainHeight(R.id.btnAddRelationship, ConstraintSet.WRAP_CONTENT)
            constraints.applyTo(binding.rootLayout)
        }
    }

    private fun setAddRelationshipText() {
        if (homeTabOrderList.list[binding.viewPager.currentItem] == CUSTOMER_TAB) {
            binding.btnAddRelationship.text = getString(R.string.add_customer)
        } else {
            binding.btnAddRelationship.text = getString(R.string.add_supplier)
        }
        binding.btnAddRelationship.icon = getDrawableCompact(R.drawable.ic_add_customer)
    }

    private val balloon by lazy {
        createBalloon(requireContext()) {
            setArrowSize(8)
            setWidthRatio(0.7f)
            setArrowPosition(0.5f)
            setArrowOrientation(ArrowOrientation.BOTTOM)
            setCornerRadius(8f)
            setMarginBottom(2)
            setAlpha(1f)
            setPadding(8)
            setAutoDismissDuration(7000L)
            setText(getString(R.string.user_migration_tooltip_string))
            setTextColorResource(R.color.white)
            setBackgroundColorResource(R.color.indigo_1)
            setBalloonAnimation(BalloonAnimation.FADE)
            setLifecycleOwner(lifecycleOwner)
        }
    }

    private fun showUploadButtonTooltip() {
        if (!balloon.isShowing) {
            balloon.apply {
                this.showAlignTop(binding.upload, 0, 0)
            }
            balloon.setOnBalloonClickListener { OnBalloonClickListener { balloon.dismiss() } }
            if (homeTabOrderList.list[binding.viewPager.currentItem] != SUPPLIER_TAB) {
                balloon.dismiss()
            }
        }
    }

    private fun renderToolbarIconCustomization(state: State) {
        if (state.toolbarCustomization != null) {
            binding.toolbarDynamicView.visible()
            val spec = TargetSpec(
                state.toolbarCustomization.target,
                allowedComponents = setOf(
                    ToolbarComponentModel::class.java
                )
            )
            dynamicViewKit.get().render(binding.toolbarDynamicView, state.toolbarCustomization.component, spec)
        } else {
            binding.toolbarDynamicView.invisible()
        }
    }

    fun gotoLogin() {
        legacyNavigator.get().goToLoginScreen(requireActivity())
        activity?.finishAffinity()
    }

    fun goToInsurance() {
        legacyNavigator.get().goWebExperimentScreen(requireContext(), WebExperiment.Experiment.INSURANCE.type)
    }

    private fun goToSyncScreen() {
        legacyNavigator.get().goToSyncScreen(requireActivity(), true)
    }

    private fun goToAddressScreen() {
        if (Permission.isLocationPermissionAlreadyGranted(requireActivity())) {
            legacyNavigator.get().goToMerchantInputScreen(
                context = requireContext(),
                inputType = BusinessConstants.ADDRESS,
                inputTitle = "Address"
            )
        } else {
            legacyNavigator.get().goToMerchantPageAndAskPermission(requireContext())
        }
    }

    private fun gotoMerchantProfileAndShowImage() {
        legacyNavigator.get().goToMerchantProfileAndShowProfileImage(requireContext())
    }

    private fun showRatingInAppNotification(isSmileyType: Boolean) {
        try {
            tracker.get().trackInAppDisplayed(HomeEventTracker.RATING, HOME_PAGE, HomeEventTracker.RATEUS_SOURCE)
            val bottomSheet = BottomSheetInAppRating.newInstance()
            bottomSheet.initialise(this, tracker.get(), isSmileyType)
            bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
        } catch (e: Exception) {
            tracker.get().trackError(HOME_PAGE, "Rating Popup", e)
        }
    }

    internal fun updateApp() {
        appUpdateManager.get().registerListener(this)
        appUpdateManager.get().appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.get().startUpdateFlowForResult(
                    it,
                    AppUpdateType.FLEXIBLE,
                    requireActivity(),
                    REQUEST_CODE_PLAY_STORE_UPDATE
                )
            }
        }
    }

    private fun showMixPanelInAppNotification(inAppNotification: InAppNotification) {
        when (inAppNotification.type) {
            InAppNotification.Type.MINI -> {
                val notificationTitle = inAppNotification.body

                if (notificationTitle == MixPanelInAppNotificationTypes.TUTORIAL_ACCOUNT_STATEMENT &&
                    getCurrentState().hideBigButtonAndNudge.not()
                ) {
                    tracker.get().trackInAppDisplayed("account_statement")
                    AccountStatementTutorialDialog.show(
                        requireActivity(),
                        object : AccountStatementTutorialDialog.Listener {

                            override fun onCancelClicked() {
                                tracker.get().trackInAppClicked("account_statement", false)
                            }

                            override fun onSubmitClicked() {
                                tracker.get().trackInAppClicked("account_statement", true)
                                Analytics.track(
                                    AnalyticsEvents.VIEW_ACCOUNT_STATEMENT,
                                    EventProperties.create().with(PropertyKey.SOURCE, "popup")
                                )
                                legacyNavigator.get().goToAccountStatementScreen(requireContext(), "")
                            }
                        }
                    ).show()
                } else if (notificationTitle == MixPanelInAppNotificationTypes.INAPP_SYSTEM_MAINTENANCE) {
                    tracker.get().trackInAppDisplayed("system_maintenance")
                    SystemMaintenanceDialog.show(
                        requireActivity(),
                        object : SystemMaintenanceDialog.Listener {
                            override fun onCancelClicked() {
                                tracker.get().trackInAppClicked("system_maintenance", false)
                            }
                        }
                    ).show()
                } else if (notificationTitle == MixPanelInAppNotificationTypes.INAPP_RATING_SMILEY) {
                    showRatingInAppNotification(true)
                } else if (notificationTitle == MixPanelInAppNotificationTypes.INAPP_RATING_STAR) {
                    showRatingInAppNotification(false)
                } else if (notificationTitle == MixPanelInAppNotificationTypes.INAPP_REVIEW) {
                    showAppReviewPopup()
                } else if (notificationTitle == MixPanelInAppNotificationTypes.APP_UPDATE) {

                    appUpdateManager.get().appUpdateInfo.addOnSuccessListener {
                        if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && it.isUpdateTypeAllowed(
                                AppUpdateType.FLEXIBLE
                            )
                        ) {
                            tracker.get().trackInAppDisplayed("app_update")
                            SystemUpdateDialog.show(
                                requireActivity(),
                                object : SystemUpdateDialog.Listener {
                                    override fun onCancelClicked() {
                                        tracker.get().trackInAppClicked("app_update", false)
                                    }

                                    override fun onOkClicked() {
                                        tracker.get().trackInAppClicked("app_update", true)
                                        updateApp()
                                    }
                                }
                            ).show()
                        }
                    }
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.SET_NEW_PIN) {
                    homeEventTracker.get().trackUpdatePin(eventName = NOTIFICATION_TBD, type = TYPE_SET_SECURITY_PIN)
                    showSetPinDialog()
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.UPDATE_NEW_PIN) {
                    homeEventTracker.get().trackUpdatePin(eventName = NOTIFICATION_TBD, type = TYPE_UPDATE_SECURITY_PIN)
                    applock.get()
                        .showUpdatePin(
                            requireActivity().supportFragmentManager,
                            this,
                            HomeConstants.UPDATE_SECURITY_PIN,
                            HOME_PAGE
                        )
                    homeEventTracker.get()
                        .trackUpdatePin(eventName = NOTIFICATION_DISPLAYED, type = TYPE_SET_SECURITY_PIN)
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.INAPP_MERCHANT_PROFILE) {

                    tracker.get().trackEvents(
                        Event.IN_APP_NOTI_DISPLAYED,
                        type = PropertyValue.MERCHANT_PROFILE,
                        screen = HOME_PAGE
                    )
                    showInAppNavigationPopup(true)
                    pushIntent(
                        Intent.UpdateInAppNavigationObject(
                            InAppNavigationObject(
                                MixPanelInAppNotificationTypes.INAPP_MERCHANT_PROFILE,
                                getString(R.string.add_business_photo_and_card), getString(R.string.add_now)
                            )
                        )
                    )
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.INAPP_ADDRESS) {

                    tracker.get().trackEvents(
                        Event.IN_APP_NOTI_DISPLAYED,
                        type = PropertyValue.MERCHANT_ADDRESS,
                        screen = HOME_PAGE
                    )
                    showInAppNavigationPopup(false)
                    pushIntent(
                        Intent.UpdateInAppNavigationObject(
                            InAppNavigationObject(
                                MixPanelInAppNotificationTypes.INAPP_ADDRESS,
                                getString(R.string.add_address_for_business), getString(R.string.add_now)
                            )
                        )
                    )
                } else if (notificationTitle == MixPanelInAppNotificationTypes.ENABLE_SYSTEM_LOCK_SCREEN) {
                    tracker.get().trackInAppDisplayed(type = PropertyValue.APP_LOCK, screen = HOME_PAGE)
                    UpdateSecurityBottomSheetDialog.showUpdate(
                        requireActivity(),
                        object : UpdateSecurityBottomSheetDialog.Listener {
                            override fun onSecurityEnabled() {
                                context?.let {
                                    tracker.get().trackInAppClickedV2(
                                        type = PropertyValue.APP_LOCK,
                                        screen = HOME_PAGE,
                                        focalArea = PropertyValue.TRUE,
                                        value = PropertyValue.ENABLE
                                    )

                                    legacyNavigator.get().goToSystemAppLockScreen(
                                        it,
                                        FrontendConstants.LOCK_SETUP_INAPP_CARD
                                    )
                                }
                            }

                            override fun onFocalArea() { // just for tracking
                                tracker.get().trackInAppClickedV2(
                                    type = PropertyValue.APP_LOCK,
                                    screen = HOME_PAGE,
                                    focalArea = PropertyValue.FALSE
                                )
                            }

                            override fun onDismissed() { // just for tracking
                                tracker.get().trackInAppClearedV1(
                                    type = PropertyValue.APP_LOCK,
                                    method = PropertyValue.BACKPRESSED
                                )
                            }
                        }
                    )
                } else if (notificationTitle == MixPanelInAppNotificationTypes.INAPP_RECHARGE_SMS) {
                    actionRechargeMessageInApp()
                } else if (notificationTitle == MixPanelInAppNotificationTypes.INAPP_MONEY_TRANSFER_SMS) {
                    actionMoneyTransferMessageInApp()
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.FEEDBACK_TECH_ISSUES) {
                    val feedbackStyle = FeedbackStyle()
                    feedbackStyle.setAppBarColors(R.color.white, R.color.grey900, R.color.grey900, R.color.primary)
                    feedbackStyle.setScreenColors(R.color.white, R.color.grey900)
                    feedbackStyle.setInputColors(R.color.grey50, R.color.grey900, R.color.grey600)
                    tracker.get().trackDebug("Feedback_tech")

                    context?.startActivity(
                        Bugfender.getUserFeedbackActivityIntent(
                            context,
                            "Feedback",
                            "Please Share your feedback?",
                            "Are you facing any technical issues?",
                            "Details?",
                            "Send",
                            feedbackStyle
                        )
                    )
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.INAPP_TUTORIAL_REMINDER) {
                    pushIntentWithDelay(
                        Intent.RxPreferenceBoolean(
                            RxSharedPrefValues.REMIND_EDUCATION,
                            false,
                            Scope.Individual
                        )
                    )
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.PAY_ONLINE_REMINDER_EDUCATION) {
                    pushIntentWithDelay(
                        Intent.RxPreferenceBoolean(
                            RxSharedPrefValues.IS_PAY_ONLINE_EDUCATION_SHOWN,
                            false,
                            Scope.Individual
                        )
                    )
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.SEND_PAYMENT_REMINDER_EDUCATION) {
                    pushIntentWithDelay(
                        Intent.RxPreferenceBoolean(
                            RxSharedPrefValues.PAYMENT_REMINDER_EDUCATION_SHOWN,
                            false,
                            Scope.Individual
                        )
                    )
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.SHOW_INAPP_INSURANCE) {
                    tracker.get().trackInAppDisplayedV1(type = PropertyValue.INSURANCE, screen = HOME_PAGE)
                    showInsurancePopup()
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.SHOW_ONLINE_COLLECTION_POPUP) {
                    pushIntentWithDelay(
                        Intent.RxPreferenceBoolean(
                            CollectionRxPreference.IS_ONLINE_COLLECTION_EDUCATION_SHOWN,
                            false,
                            Scope.Individual
                        )
                    )
                } else if (notificationTitle == MixPanelInAppNotificationTypes.INAPP_MERCHANT_ADDRESS) {
                    tracker.get().trackInAppDisplayed(type = "inapp_merchant_address", screen = HOME_PAGE)
                    MerchantAddressRequestBottomSheetDialog.show(childFragmentManager)
                } else if (notificationTitle == MixPanelInAppNotificationTypes.ADD_TRANSACTION_SHORTCUT) {
                    showAddTransactionShortcutPopup()
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.SHOW_PAY_ONLINE_EDUCATION_HOME) {
                    pushIntentWithDelay(
                        Intent.RxPreferenceBoolean(
                            RxSharedPrefValues.IS_PAY_ONLINE_EDUCATION_HOME_SHOWN,
                            false,
                            Scope.Individual
                        )
                    )
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.SHOW_PAY_ONLINE_EDUCATION_FOR_CAMPAIGN) {
                    pushIntentWithDelay(
                        Intent.RxPreferenceBoolean(
                            RxSharedPrefValues.IS_PAY_ONLINE_EDUCATION_SHOWN_FOR_CAMPAIGN,
                            false,
                            Scope.Individual
                        )
                    )
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.SHOW_REPORT_ICON_EDUCATION) {
                    pushIntentWithDelay(
                        Intent.RxPreferenceBoolean(
                            RxSharedPrefValues.IS_REPORT_ICON_EDUCATION_SHOWN,
                            false,
                            Scope.Individual
                        )
                    )
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.SHOW_DATE_SELECTION_EDUCATION_EDUCATION) {
                    pushIntentWithDelay(
                        Intent.RxPreferenceBoolean(
                            RxSharedPrefValues.IS_DATE_RANGE_EDUCATION_SHOWN,
                            false,
                            Scope.Individual
                        )
                    )
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.SHOW_REPORT_SHARE_EDUCATION) {
                    pushIntentWithDelay(
                        Intent.RxPreferenceBoolean(
                            RxSharedPrefValues.IS_REPORT_SHARE_EDUCATION_SHOWN,
                            false,
                            Scope.Individual
                        )
                    )
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.SHOW_EDIT_AMOUNT_EDUCATION) {
                    pushIntentWithDelay(
                        Intent.RxPreferenceBoolean(
                            RxSharedPrefValues.IS_EDIT_AMOUNT_EDUCATION_SHOWN,
                            false,
                            Scope.Individual
                        )
                    )
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.SHOW_DELETE_TXN_EDUCATION) {
                    pushIntentWithDelay(
                        Intent.RxPreferenceBoolean(
                            RxSharedPrefValues.IS_DELETE_TXN_EDUCATION_SHOWN,
                            false,
                            Scope.Individual
                        )
                    )
                } else if (inAppNotification.body == MixPanelInAppNotificationTypes.BULK_REMINDER) {
                    homeEventTracker.get().trackUpdatePin(eventName = NOTIFICATION_TBD, type = TYPE_BULK_REMINDER)
                    showBulkReminderBottomSheet()
                }
                mixpanelAPI.get().people.trackNotificationSeen(inAppNotification)
            }
            InAppNotification.Type.TAKEOVER -> {
                mixpanelAPI.get().people.showGivenNotification(inAppNotification, requireActivity())
            }
            else -> {
                RecordException.recordException(
                    RuntimeException(
                        "Mixpanel/Unknown Notification Type: ${inAppNotification.type}: id: ${inAppNotification.id}"
                    )
                )
            }
        }
    }

    private fun showInAppNavigationPopup(show: Boolean) {
        pushIntent(Intent.ShowInAppNavigationPopup(show))
    }

    private fun showAddTransactionShortcutPopup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (shortcutHelper.get().isShortcutAlreadyPinned(AppShortcutAdder.Shortcut.ADD_TRANSACTION.id).not()) {
                tracker.get().trackInAppDisplayed(type = "add_transaction_shortcut", screen = HOME_PAGE)
                AddTransactionShortcutRequestBottomSheet.show(childFragmentManager)
            } else {
                tracker.get().trackDebug(
                    "add_transaction_shortcut notification not shown",
                    mapOf(PropertyKey.REASON to "Shortcut already present")
                )
            }
        } else {
            tracker.get().trackDebug(
                "add_transaction_shortcut notification not shown",
                mapOf(PropertyKey.REASON to "Android OS version is ${Build.VERSION.SDK_INT}")
            )
        }
    }

    private fun actionMoneyTransferMessageInApp() {
        val merchantId = if (getCurrentState().businessData != null) {
            getCurrentState().businessData!!.business.id
        } else ""
        MoneyTransferSmsBottomSheetDialog.start(childFragmentManager, merchantId)
    }

    private fun actionRechargeMessageInApp() {
        val merchantId = if (getCurrentState().businessData != null) {
            getCurrentState().businessData!!.business.id
        } else ""
        MobileRechargeSmsBottomSheetDialog.start(childFragmentManager, merchantId)
    }

    private fun showInsurancePopup() {
        val insuranceBottomSheetDialog = InsuranceBottomSheetDialog.newInstance()

        childFragmentManager.beginTransaction()
            .add(insuranceBottomSheetDialog, InsuranceBottomSheetDialog.TAG)
            .commitAllowingStateLoss()

        insuranceBottomSheetDialog.setListener(object : InsuranceBottomSheetDialog.InsuranceActionListener {
            override fun onFirstActionClick() {
                tracker.get().trackV1(Event.CLICKED_INSURANCE, PropertyValue.INAPPNOTIFICATION)
                goToInsurance()
            }

            override fun onSecondActionClick() {
            }
        })
    }

    private fun getCustomerTabFragment() = this@HomeFragment.childFragmentManager
        .findFragmentByTag("f" + binding.viewPager.currentItem) as? CustomerTabFragment

    private fun getSupplierTabFragment() = this@HomeFragment.childFragmentManager
        .findFragmentByTag("f" + binding.viewPager.currentItem) as? SupplierTabFragment

    override fun onSyncNow() {
        tracker.get().trackTransactionDetails(
            eventName = Event.SYNC_CLICKED,
            type = HOME_UN_SYNCED_ACTIVITY,
            status = PropertyValue.SUCCESS,
            screen = HOME_PAGE
        )
        if (isStateInitialized() && getCurrentState().isConnectedToInternet) {
            pushIntent(Intent.SyncNow)
        } else {
            view?.snackbar(getString(R.string.no_internet_msg), Snackbar.LENGTH_SHORT)?.show()
        }
    }

    fun setTabHeaderCount(fragment: Fragment, count: Int) {
        var fragmentPosition = 0

        when (fragment) {
            is CustomerTabFragment -> fragmentPosition = 0
            is SupplierTabFragment -> fragmentPosition = 1
        }

        val view = binding.tabLayout.getTabAt(fragmentPosition)?.customView
        val tvBadge: TextView? = view?.findViewById(R.id.tvTabBadge)
        val supplierNew: TextView? = view?.findViewById(R.id.supplier_new)

        if (count == 0) tvBadge?.gone()
        else tvBadge?.visible()

        tvBadge?.text = count.toString()

        // for 1 digit, we set text size = 12sp
        // for more than 2 digits, we set text size = 10sp
        when (count.toString().length) {
            1 -> {
                tvBadge?.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resourceFinder.get().getDimension(R.dimen.text_size_12).toFloat()
                )
            }
            else -> {
                tvBadge?.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resourceFinder.get().getDimension(R.dimen.text_size_10).toFloat()
                )
            }
        }

        if (count == 0 && fragmentPosition == 1 && getCurrentState().canShowNewOnSupplierTab) {
            supplierNew?.visible()
        } else {
            supplierNew?.gone()
        }
    }

    fun toggleFilter(customerCount: Int) {
        if (customerCount != 0 || supplierCount != 0) {
            if (Sort.sortfilter.isNotEmpty()) {
                setFilterCountProp()
            } else {
                removeFilterProp()
            }
        }
    }

    private fun removeFilterProp() {
        binding.sortFilter.setFilterCount(0)
    }

    private fun setFilterCountProp() {
        binding.sortFilter.setFilterCount(Sort.sortfilter.size)
    }

    fun showAddFabBottomButton() {
        binding.btnAddRelationship.extend()
        binding.btnAddRelationship.show()
    }

    fun recyclerviewScrollToBottom() {
        binding.btnAddRelationship.shrink()
    }

    fun recyclerviewScrollToTop() {
        binding.btnAddRelationship.extend()
    }

    override fun onDestroy() {
        // TODO : fix IO operation done on main thread
        onboardingPreferences.get().setIsFreshLogin(false)
        super.onDestroy()
        appUpdateManager.get().unregisterListener(this)
        Timber.i("destroyed")
    }

    // To exit app , user has to press 2 times back button
    override fun onBackPressed(): Boolean {
        return if (!isSinglePressed) {
            shortToast(R.string.touch_again_exit)
            isSinglePressed = true
            Completable.timer(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    isSinglePressed = false
                }.addTo(autoDisposable)
            true
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            HomeConstants.GO_TO_SUPPLIER_TAB -> {
                if (adapter?.itemCount == 2) {
                    if (homeTabOrderList.list[binding.viewPager.currentItem] == CUSTOMER_TAB)
                        binding.viewPager.currentItem = 1
                }
            }
        }
        if (requestCode == HomeConstants.SET_NEW_SECURITY_PIN || requestCode == HomeConstants.UPDATE_SECURITY_PIN) {
            data?.let {
                if (data.getBooleanExtra(IS_AUTHENTICATED, false)) {
                    if (requestCode == HomeConstants.SET_NEW_SECURITY_PIN)
                        appLockTracker.get().trackEvents(eventName = SECURITY_PIN_SET, source = HOME_PAGE)
                    if (requestCode == HomeConstants.UPDATE_SECURITY_PIN)
                        appLockTracker.get().trackEvents(eventName = SECURITY_PIN_CHANGED, source = HOME_PAGE)
                }
            }
        }
    }

    override fun goToPlayStoreForRateUs() {
        legacyNavigator.get().goToPlayStore(requireActivity())
    }

    override fun submitRatingAndFeedback(feedback: String, rating: Int) {
        pushIntent(Intent.SubmitFeedback(feedback, rating))
    }

    private fun updateAppIfInterupted() {
        appUpdateManager.get()
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager.get().startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        requireActivity(),
                        REQUEST_CODE_PLAY_STORE_IMMEDIATE_UPDATE
                    )
                }
            }
    }

    override fun applySort(sort: Sort, isFilterApplied: Boolean) {
        var sortBy = "Default"
        if (Sort.isDefaultSortByApplied.not()) {
            sortBy = Sort.sortBy
        }
        var flow = "New"
        if (isFilterApplied) {
            flow = "Update"
        }
        tracker.get().trackUpdateFilter(PropertyValue.CUSTOMER, sortBy, Sort.sortfilter.joinToString(), flow)

        if (sort.sortfilter.size > 0) {
            setFilterCountProp()
        } else removeFilterProp()
        accountSortBottomSheetFragment?.dismiss()
        getNewCustomerSort(sort)
    }

    private fun getNewSupplierSort() {
        when (homeTabOrderList.list[binding.viewPager.currentItem]) {
            SUPPLIER_TAB -> getSupplierTabFragment()?.onSorted()
        }
    }

    private fun getNewCustomerSort(sort: Sort) {
        getCustomerTabFragment()?.onNewSorted(sort)
    }

    override fun cancelSort(sort: Sort, source: String) {
        var flow = "New"
        if (Sort.sortApplied) {
            flow = "Update"
        }
        tracker.get().trackCancelFilter(PropertyValue.CUSTOMER, flow, source)

        accountSortBottomSheetFragment?.dismiss()
    }

    override fun clearFilter(source: String) {
        val flow = if (Sort.sortApplied) {
            "Update"
        } else {
            "New"
        }
        tracker.get().trackClearFilter(source, PropertyValue.CUSTOMER, flow)
        binding.sortFilter.setFilterCount(0)
        Sort.reset()
        accountSortBottomSheetFragment?.reset()
        accountSortBottomSheetFragment?.dismiss()
        getNewCustomerSort(Sort)
    }

    private fun showFilterEducation() {
        tracker.get().trackInAppDisplayedV1(type = PropertyValue.CUSTOMER_FILTER, screen = HOME_PAGE)
        lifecycleScope.launch {
            localInAppNotificationHandler.get()
                .generateTapTarget(
                    weakScreen = WeakReference(requireActivity()),
                    tapTarget = TapTargetLocal(
                        screenName = label,
                        targetView = WeakReference(binding.sortFilter),
                        title = getString(R.string.new_filter_options),
                        titleGravity = Gravity.END,
                        padding = 22f,
                        listener = { _, state ->
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                Analytics.track(
                                    Event.IN_APP_NOTI_CLICKED,
                                    EventProperties.create()
                                        .with(PropertyKey.TYPE, PropertyValue.CUSTOMER_FILTER)
                                        .with("focal_area", true)
                                        .with(PropertyKey.SCREEN, HOME_PAGE)
                                )
                            } else if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                                Analytics.track(
                                    Event.IN_APP_NOTI_CLICKED,
                                    EventProperties.create()
                                        .with("type", PropertyValue.CUSTOMER_FILTER)
                                        .with("focal_area", false)
                                        .with("screen", HOME_PAGE)
                                )
                            }
                        }
                    )
                )

            pushIntent(Intent.RxPreferenceBoolean(RxSharedPrefValues.SHOULD_SHOW_FILTER_EDUCATION, false, Scope.Individual))
        }
    }

    override fun applySupplierSort() {
        getNewSupplierSort()
    }

    fun setSupplierCount(size: Int, name: String?, balance: Long?) {
        supplierCount = size
        firstSupplierBalance = balance
        firstSupplierName = name
    }

    fun setFirstSupplierEducationSeen() {
        pushIntent(
            Intent.RxPreferenceBoolean(RxSharedPrefValues.SHOULD_SHOW_FIRST_SUPPLIER_EDUCATION, false, Scope.Individual)
        )
    }

    fun onSupplierVideoStopped() {
        isSupplierVideoPlaying = false
    }

    fun onSupplierVideoResumed() {
        isSupplierVideoPlaying = true
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.SetupViewPager -> {
                homeTabOrderList = event.homeTabOrderList
                setupViewPager(event.homeTabOrderList)
                setupTabLayout(event.homeTabOrderList, event.isPayablesExperimentEnabled)
            }

            is ViewEvent.GoToReferralInAppNotification -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    referralNavigator.get().showReferralInAppBottomSheet(childFragmentManager)
                }
            }
            is ViewEvent.GoToAddOkCreditContactInAppNotification -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    contactsNavigator.get().showAddOkCreditContactInAppBottomSheet(childFragmentManager)
                }
            }

            is ViewEvent.GotoLogin -> gotoLogin()

            is ViewEvent.ShowMixPanelInAppNotification -> showMixPanelInAppNotification(event.inAppNotification)

            is ViewEvent.ShowFilterEducation -> showFilterEducation()

            is ViewEvent.GoToSyncScreen -> goToSyncScreen()

            is ViewEvent.ShowImmediateUpdate -> showImmediateUpdateDialog()

            is ViewEvent.AppUpdateInterrupted -> updateAppIfInterupted()

            is ViewEvent.ShowBulkReminder -> showBulkReminderBottomSheet()

            is ViewEvent.ShowUploadButtonTooltip -> showUploadButtonTooltip()

            is ViewEvent.TrackUploadButtonViewed -> homeEventTracker.get().trackUserMigrationViewed()

            is ViewEvent.ShowKycCompleteDialog -> showKycStatus()

            is ViewEvent.ShowKycRiskDialog -> showKycRiskDialog(event.kycStatus, event.kycRisk)

            is ViewEvent.ShowKycStatusDialog -> showKycStatusDialog(event.kycStatus)

            is ViewEvent.ShowSupplierTabEducation -> showSupplierTabEducation()

            is ViewEvent.TrackPayablesExperimentStarted ->
                homeEventTracker.get().trackPayablesExperimentStarted(event.experimentEnabled)

            is ViewEvent.ShowAddBankPopUp -> showAddBankPopUp(event.customerNames)

            is ViewEvent.ShowNotificationReminder -> showReminderNotification(
                event.notificationReminderForUi,
            )
            is ViewEvent.ShowPreNetworkOnboardingNudges -> showPreNetworkOnboardingNudges(event.delayInToolTipShown)
        }
    }

    private fun customerTabEducation(
        tooltipText: String,
        arrowPosition: Float,
        tabViewedForAnalytics: String,
        currentTab: HomeConstants.HomeTab
    ) {
        lifecycleScope.launch {
            localInAppNotificationHandler.get()
                .generateTooltip(
                    weakScreen = WeakReference(requireActivity()),
                    tooltip = TooltipLocal(
                        screenName = label,
                        targetView = WeakReference(binding.tabLayout.getChildAt(0)),
                        arrowPosition = arrowPosition,
                        title = tooltipText,
                        textSize = 14f,
                        backgroundColor = R.color.indigo_1,
                        autoDismissTime = 6000L,
                        dismissOnClicked = true,
                        padding = 10,
                        clickListener = {
                            homeEventTracker.get().trackNotificationClicked(
                                "tool_tip",
                                "",
                                tabViewedForAnalytics,
                            )
                            if (currentTab == CUSTOMER_TAB) {
                                openSupplierTab()
                            } else {
                                openCustomerTab()
                            }
                        },
                        alignTop = false
                    )
                )
        }
    }

    private fun showPreNetworkOnboardingNudges(delayInToolTipShown: Long) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            openSupplierTab()

            delay(delayInToolTipShown)
            binding.ivHand.gone()

            var tooltipText = ""
            var tooltipArrowPosition = 0f
            var tabViewedForAnalytics = ""
            var currentTab: HomeConstants.HomeTab = CUSTOMER_TAB

            if (CUSTOMER_TAB == homeTabOrderList.list[binding.viewPager.currentItem]) {
                tooltipText = getString(R.string.t_001_pre_nw_onboarding_edu_supp_tab)
                tooltipArrowPosition = 0.55f
                tabViewedForAnalytics = "View Supplier Tab"
                currentTab = CUSTOMER_TAB
            } else {
                tooltipText = getString(R.string.t_001_pre_nw_onboarding_edu_cust_tab)
                tooltipArrowPosition = 0.2f
                tabViewedForAnalytics = "View Customer Tab"
                currentTab = SUPPLIER_TAB
            }
            customerTabEducation(tooltipText, tooltipArrowPosition, tabViewedForAnalytics, currentTab)
            homeEventTracker.get().trackNotificationDisplayed(
                "tool_tip",
                "",
                tabViewedForAnalytics,
            )
            pushIntent(Intent.CustomerTabEducationShown)
        }
    }

    private fun showAddBankPopUp(customerNames: List<String>) {
        AddBankDetailBottomSheet.getInstance(customerNames)
            .show(childFragmentManager, AddBankDetailBottomSheet::class.java.simpleName)
    }

    private val kycListener = object : KycDialogListener {
        override fun onDisplayed(eventName: String, campaign: String) {
            trackKycEventDisplayed(eventName, campaign)
        }

        override fun onConfirmKyc(dontAskAgain: Boolean, eventName: String) {
            onDismiss()
            trackKycEvents(eventName)
            legacyNavigator.get().goWebExperimentScreen(requireContext(), WebExperiment.Experiment.KYC.type)
        }

        override fun onCancelKyc(dontAskAgain: Boolean, eventName: String) {
            onDismiss()
            trackKycEvents(eventName)
        }

        override fun onDismissKyc(eventName: String) {
            onDismiss()
            trackKycEvents(eventName)
        }

        private fun onDismiss() {
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                pushIntent(Intent.ResetKycNotification)
            }
        }
    }

    internal fun trackKycEventDisplayed(eventName: String, campaign: String) {
        val state = getCurrentState()
        tracker.get()
            .trackEvents(
                eventName = eventName,
                screen = HOME_PAGE,
                propertiesMap = PropertiesMap.create()
                    .add("merchant_id", state.businessData?.business?.id ?: "")
                    .add("kyc_status", state.kycStatus.value.toLowerCase())
                    .add("risk_type", state.kycRiskCategory.value.toLowerCase())
                    .add("_campaign_id", campaign)
            )
    }

    internal fun trackKycEvents(eventName: String) {
        val state = getCurrentState()
        if (state.kycRiskCategory == KycRiskCategory.NO_RISK) {
            tracker.get()
                .trackEvents(
                    eventName = eventName,
                    screen = HOME_PAGE,
                    propertiesMap = PropertiesMap.create()
                        .add("merchant_id", state.businessData?.business?.id ?: "")
                        .add("kyc_status", state.kycStatus.value.toLowerCase())
                )
        } else {
            tracker.get()
                .trackEvents(
                    eventName = eventName,
                    screen = HOME_PAGE,
                    propertiesMap = PropertiesMap.create()
                        .add("merchant_id", state.businessData?.business?.id ?: "")
                        .add("kyc_status", state.kycStatus.value.toLowerCase())
                        .add("risk_type", state.kycRiskCategory.value.toLowerCase())
                )
        }
    }

    private fun showKycStatus() {
        collectionNavigator.get().showKycDialog(
            childFragmentManager,
            kycListener,
            kycDialogMode = KycDialogMode.Complete
        )
    }

    private fun showKycRiskDialog(kycStatus: KycStatus, kycRisk: KycRisk) {
        if (kycRisk.kycRiskCategory != KycRiskCategory.NO_RISK) {
            collectionNavigator.get().showKycDialog(
                childFragmentManager,
                kycListener,
                kycDialogMode = KycDialogMode.Risk,
                kycStatus = kycStatus,
                kycRiskCategory = kycRisk.kycRiskCategory
            )
        }
    }

    private fun showKycStatusDialog(kycStatus: KycStatus) {
        if (kycStatus != KycStatus.NOT_SET) {
            collectionNavigator.get().showKycDialog(
                childFragmentManager,
                kycListener,
                kycDialogMode = KycDialogMode.Status,
                kycStatus = kycStatus
            )
        }
    }

    private fun showBulkReminderBottomSheet() {
        tracker.get().trackEvents(
            Event.IN_APP_NOTI_DISPLAYED,
            type = TYPE_BULK_REMINDER,
            screen = HOME_PAGE,
            source = PropertyValue.INAPPNOTIFICATION,
        )
        BulkReminderBottomSheet.start(this@HomeFragment.requireContext())
    }

    private fun showSetPinDialog() {
        homeEventTracker.get().trackUpdatePin(eventName = NOTIFICATION_DISPLAYED, type = TYPE_SET_SECURITY_PIN)
        applock.get()
            .showSetNewPin(
                requireActivity().supportFragmentManager,
                this,
                HomeConstants.SET_NEW_SECURITY_PIN,
                HOME_PAGE
            )
    }

    private fun showImmediateUpdateDialog() {
        appUpdateManager.get().appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.get().startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    requireActivity(),
                    REQUEST_CODE_PLAY_STORE_IMMEDIATE_UPDATE
                )
            }
        }
    }

    override fun onStateUpdate(installState: InstallState) {
        installState.let {
            if (it.installStatus() == InstallStatus.DOWNLOADED) {
                appUpdateManager.get().completeUpdate()
            }

            pushIntent(Intent.SetInAppDownloadLoaderVisibility(it.installStatus() == InstallStatus.DOWNLOADING))
        }
    }

    override fun onSetPinClicked(requestCode: Int) {
        homeEventTracker.get()
            .trackUpdatePin(NOTIFICATION_CLICKED, type = TYPE_SET_SECURITY_PIN, value = SET_NEW_PIN)
        requireActivity().runOnUiThread {
            startActivityForResult(
                applock.get().appLock(
                    getString(R.string.changepin_screen_deeplink), requireActivity(),
                    HOME_PAGE
                ),
                requestCode
            )
        }
    }

    override fun onDismissed() {
        homeEventTracker.get().trackUpdatePin(eventName = NOTIFICATION_CLEARED, type = TYPE_SET_SECURITY_PIN)
    }

    override fun onSetNewPinClicked(requestCode: Int) {
        homeEventTracker.get()
            .trackUpdatePin(NOTIFICATION_CLICKED, type = TYPE_UPDATE_SECURITY_PIN, value = UPDATE_NEW_PIN)
        requireActivity().runOnUiThread {
            startActivityForResult(
                applock.get().appLock(getString(R.string.changepin_screen_deeplink), requireActivity(), HOME_PAGE),
                requestCode
            )
        }
    }

    override fun onUpdateDialogDismissed() {
        homeEventTracker.get().trackUpdatePin(eventName = NOTIFICATION_CLEARED, type = TYPE_UPDATE_SECURITY_PIN)
    }

    private fun showSupplierTabEducation() {
        if (binding.viewPager.currentItem == 0) {
            if (supplierCount == 0) {
                showSupplierTabEducation(getString(R.string.supplier_subtext_1), 150f)
            } else {
                firstSupplierBalance?.let { balance ->
                    firstSupplierName?.let { firstSupplierName ->
                        var firstSupplierBalance = balance.div(100L)
                        when {
                            firstSupplierBalance > 0 -> {
                                showSupplierTabEducation(
                                    getString(
                                        R.string.supplier_subtext_4,
                                        firstSupplierBalance.toString(),
                                        firstSupplierName
                                    ),
                                    170f
                                )
                            }
                            firstSupplierBalance < 0 -> {
                                firstSupplierBalance = firstSupplierBalance.times(-1)
                                showSupplierTabEducation(
                                    getString(
                                        R.string.supplier_subtext_3,
                                        firstSupplierBalance.toString(),
                                        firstSupplierName
                                    ),
                                    170f
                                )
                            }
                            else -> showSupplierTabEducation(getString(R.string.supplier_subtext_2), 150f)
                        }
                    }
                }
            }
            pushIntent(
                Intent.RxPreferenceBoolean(
                    RxSharedPrefValues.SHOULD_SHOW_SUPPLIER_TAB_EDUCATION,
                    false,
                    Scope.Individual
                )
            )
        }
    }

    private fun loadBusiness(business: Business?) {
        if (business == null) return
        val defaultPic = TextDrawableUtils.getRoundTextDrawable(business.name)
        Glide.with(this)
            .load(business.profileImage)
            .circleCrop()
            .placeholder(defaultPic)
            .into(binding.imgToolbarBusiness)
    }

    private fun renderMultipleAccountsEntryPoint(state: State) {
        // Using GONE for imgToolbarBusiness makes toolbar_layer UI misaligned,
        // therefore, need to use INVISIBLE & constraintSet for toolbarDynamicView's end contraint
        ConstraintSet().apply {
            clone(binding.root)
            if (state.canShowMultipleAccountsEntryPoint) {
                setVisibility(binding.imgToolbarBusiness.id, ConstraintSet.VISIBLE)
                connect(binding.toolbarDynamicView.id, END, binding.imgToolbarBusiness.id, START)
                loadBusiness(state.businessData?.business)
            } else {
                setVisibility(binding.imgToolbarBusiness.id, ConstraintSet.INVISIBLE)
                connect(binding.toolbarDynamicView.id, END, binding.endGuideline.id, START)
            }
            applyTo(binding.root)
        }
    }

    private fun showReminderNotification(notificationReminderForUi: NotificationReminderForUi?) {
        if (notificationReminderForUi == null)
            return

        homeEventTracker.get().trackInAppNotificationReminderView(
            accountId = notificationReminderForUi.accountId,
            dueAmount = notificationReminderForUi.balanceInPaisa,
            lastPaymentDate = notificationReminderForUi.lastPaymentDate,
            lastPaymentAmount = notificationReminderForUi.lastPaymentInPaisa,
            totalReminderLeft = notificationReminderForUi.totalNotificationCount
        )

        val notificationReminderDialog = NotificationReminderDialog.newInstance(notificationReminderForUi)
        notificationReminderDialog.show(childFragmentManager, NotificationReminderDialog.TAG)

        notificationReminderDialog.initialise(object : NotificationReminderDialog.NotificationReminderListener {
            override fun onPayNowClicked() {
                Timber.i("Pay now clicked!")
                homeEventTracker.get().trackInAppNotificationReminderAction(
                    accountId = notificationReminderForUi.accountId,
                    dueAmount = notificationReminderForUi.balanceInPaisa,
                    lastPaymentDate = notificationReminderForUi.lastPaymentDate,
                    lastPaymentAmount = notificationReminderForUi.lastPaymentInPaisa,
                    totalReminderLeft = notificationReminderForUi.totalNotificationCount,
                    action = SupplierAnalyticsEvents.SupplierPropertyValue.PAY_NOW
                )

                pushIntent(
                    Intent.UpdateReminderNotification(
                        notificationReminderForUi.notificationId,
                        ApiEntityMapper.NotificationReminderStatus.PAYNOW
                    )
                )

                legacyNavigator.get()
                    .goToSupplierPaymentScreen(requireContext(), notificationReminderForUi.accountId)
            }

            override fun onDismissed() {
                Timber.i("Pay dialog dismissed")

                homeEventTracker.get().trackInAppNotificationReminderAction(
                    accountId = notificationReminderForUi.accountId,
                    dueAmount = notificationReminderForUi.balanceInPaisa,
                    lastPaymentDate = notificationReminderForUi.lastPaymentDate,
                    lastPaymentAmount = notificationReminderForUi.lastPaymentInPaisa,
                    totalReminderLeft = notificationReminderForUi.totalNotificationCount,
                    action = SupplierAnalyticsEvents.SupplierPropertyValue.DISMISS
                )

                pushIntent(
                    Intent.UpdateReminderNotification(
                        notificationReminderForUi.notificationId,
                        ApiEntityMapper.NotificationReminderStatus.DISMISSED
                    )
                )
            }
        })
    }
}
