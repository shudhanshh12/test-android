package tech.okcredit.home.ui.supplier_tab

import `in`.okcredit.analytics.*
import `in`.okcredit.backend.utils.BroadcastHelper
import `in`.okcredit.backend.utils.StringUtils
import `in`.okcredit.communication_inappnotification.contract.LocalInAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.ui.local.TapTargetLocal
import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents
import `in`.okcredit.supplier.supplier_profile_bottom_sheet.SupplierProfileBottomSheet
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.epoxy.EpoxyRecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.perf.metrics.AddTrace
import dagger.Lazy
import io.reactivex.Observable
import jp.wasabeef.recyclerview.animators.FadeInDownAnimator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Nullable
import tech.okcredit.android.base.extensions.executeIfFragmentViewAvailable
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.app_contract.AppConstants
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.home.R
import tech.okcredit.home.databinding.FragmentSupplierTabBinding
import tech.okcredit.home.dialogs.supplier_payment_dialog.SupplierPaymentDialog
import tech.okcredit.home.ui.analytics.HomeEventTracker
import tech.okcredit.home.ui.analytics.HomeTraces
import tech.okcredit.home.ui.home.HomeFragment
import tech.okcredit.home.ui.home.views.HomeSupplierView
import tech.okcredit.home.ui.home.views.NativePlayerView
import tech.okcredit.home.ui.payables_onboarding.PayablesOnboardingPage.ImageBacked
import tech.okcredit.home.ui.payables_onboarding.PayablesOnboardingPage.VideoBacked
import tech.okcredit.home.ui.payables_onboarding.PayablesOnboardingPagerAdapter
import tech.okcredit.home.ui.supplier_tab.SupplierTabContract.*
import tech.okcredit.home.ui.supplier_tab.view.YoutubeView
import timber.log.Timber
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.lang.ref.WeakReference
import javax.inject.Inject

class SupplierTabFragment :
    BaseFragment<State, ViewEvent, SupplierTabContract.Intent>(
        "SupplierTabFragment",
        R.layout.fragment_supplier_tab
    ),
    HomeSupplierView.SupplierSelectionListener,
    Listeners,
    YoutubeView.YoutubeListener,
    @Nullable NativePlayerView.NativeListener {

    private var supplierEducationRunnable: Runnable? = null

    @Inject
    lateinit var tracker: Lazy<Tracker>

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var homeEventTracker: Lazy<HomeEventTracker>

    @Inject
    lateinit var imageLoader: Lazy<IImageLoader>

    @Inject
    lateinit var broadcastHelper: Lazy<BroadcastHelper>

    @Inject
    lateinit var supplierAnalyticsEvents: Lazy<SupplierAnalyticsEvents>

    @Inject
    lateinit var localInAppNotificationHandler: Lazy<LocalInAppNotificationHandler>

    internal lateinit var controller: SupplierTabController

    private var backPressed: Boolean = false
    private var alertDialog: AlertDialog? = null
    private var isShownSupplierEducation = false
    private var isCampaignEducationShown = false
    private var youTubeState: String? = null
    private var videoUrl: String? = null
    private var isAttached: Boolean = false
    private var isFocused: Boolean = false

    internal val binding: FragmentSupplierTabBinding by viewLifecycleScoped(FragmentSupplierTabBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setupViewListeners()
    }

    private fun initView() {
        controller = SupplierTabController(
            fragment = this,
            tracker = tracker.get(),
            performanceTracker = performanceTracker,
            broadcastHelper = broadcastHelper.get()
        )
        binding.rvCustomer.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = controller.adapter
            itemAnimator = FadeInDownAnimator()
        }
    }

    private val dataObserver by lazy {
        object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                    if (isVisible) {
                        delay(300)
                        binding.rvCustomer.layoutManager?.scrollToPosition(0)
                    }
                }
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                // This is called when we do sorting
                binding.rvCustomer.scrollToPosition(0)
            }
        }
    }

    override fun onDestroyView() {
        controller.adapter.unregisterAdapterDataObserver(dataObserver)
        super.onDestroyView()
    }

    private fun setupViewListeners() = with(binding) {
        rvCustomer.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    (parentFragment as HomeFragment).recyclerviewScrollToBottom()
                } else {
                    (parentFragment as HomeFragment).recyclerviewScrollToTop()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                hideSoftKeyboard()
            }
        })

        controller.adapter.registerAdapterDataObserver(dataObserver)

        swipeToRefresh.setOnRefreshListener {
            tracker.get().trackRefresh(PropertyValue.HOME_PAGE)
            swipeToRefresh.isRefreshing = false
            (parentFragment as HomeFragment).onSyncNow()
        }
        learnMore.setOnClickListener {
            tracker.get().trackSupplierLearnMore(
                accountId = null,
                relation = "Supplier",
                screen = "Supplier List",
                listStatus = getListStatus(),
                position = "Bottom"
            )
            pushIntent(SupplierTabContract.Intent.SupplierLearnMore)
        }
        icKnowMoreSupplier.setOnClickListener {
            tracker.get().trackSupplierLearnMore(
                accountId = null,
                relation = "Supplier",
                screen = "Supplier List",
                listStatus = getListStatus(),
                position = "Bottom"
            )
            pushIntent(SupplierTabContract.Intent.SupplierLearnMore)
        }
    }

    private fun getListStatus(): String {
        return if (getCurrentState().supplier?.suppliers?.isNotEmpty() == true)
            "Not Empty"
        else "Empty"
    }

    override fun loadIntent(): UserIntent {
        return SupplierTabContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    fun showFirstSupplierEducation() {
        Analytics.track(
            AnalyticsEvents.IN_APP_NOTI_DISPLAYED,
            EventProperties.create()
                .with(PropertyKey.TYPE, "first_supplier")
                .with(PropertyKey.SCREEN, "home")
        )
        binding.rvCustomer.postDelayed(getHandlerAction(binding.rvCustomer), 2000)
    }

    private fun getHandlerAction(rvCustomer: EpoxyRecyclerView?): Runnable? {
        supplierEducationRunnable = Runnable {
            // TODO Add Check When Experiment is Enabled don't show this education
            if ((parentFragment as? HomeFragment)?.isSupplierSectionVisible() == true) {
                rvCustomer?.let { rvCustomer ->
                    getCurrentState().supplier?.suppliers?.let {
                        if (!isShownSupplierEducation && it.isNotEmpty()) {
                            isShownSupplierEducation = true
                            activity?.runOnUiThread {
                                val index = getFirstIndexOfHomeSupplierView(rvCustomer)
                                Timber.d("rvCustomer size : ${rvCustomer.getChildAt(index)}")

                                lifecycleScope.launch {
                                    localInAppNotificationHandler.get()
                                        .generateTapTarget(
                                            weakScreen = WeakReference(requireActivity()),
                                            tapTarget = TapTargetLocal(
                                                screenName = label,
                                                targetView = WeakReference(rvCustomer.getChildAt(index)),
                                                title = it[index].name + StringUtils.SINGLE_SPACE + getString(
                                                    R.string.supplier_education_2
                                                ),
                                                titleTypeFaceStyle = R.style.OKC_TextAppearance_Headline6,
                                                subtitle = getString(R.string.give_discount_education_secondary_text),
                                                listener = { _, state ->
                                                    executeIfFragmentViewAvailable {
                                                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                                            Analytics.track(
                                                                AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                                                EventProperties.create()
                                                                    .with(PropertyKey.TYPE, "first_supplier")
                                                                    .with("focal_area", true)
                                                                    .with(PropertyKey.SCREEN, "home")
                                                            )
                                                            onSupplierSelected(it[index].id, it[index].registered)
                                                        } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                                                            Analytics.track(
                                                                AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                                                EventProperties.create()
                                                                    .with(PropertyKey.TYPE, "first_supplier")
                                                                    .with("focal_area", false)
                                                                    .with(PropertyKey.SCREEN, "home")
                                                            )
                                                            onSupplierSelected(it[index].id, it[index].registered)
                                                        }
                                                    }
                                                }
                                            )
                                        )
                                }

                                (parentFragment as HomeFragment).setFirstSupplierEducationSeen()
                            }
                        }
                    }
                }
            }
        }
        return supplierEducationRunnable
    }

    fun showPayOnlineEducationHome() {
        homeEventTracker.get()
            .trackInAppNotificationDisplayed(PropertyValue.HOME_PAGE, HomeEventTracker.PAY_ONLINE_CUSTOMER)
        binding.rvCustomer.postDelayed(
            {
                binding.rvCustomer.let { rvCustomer ->
                    val state = if (isStateInitialized()) getCurrentState() else null
                    if (state != null && state.isMerchantFromCollectionCampaign) {
                        state.supplier?.suppliers?.let {
                            activity?.runOnUiThread {
                                if (!isCampaignEducationShown && it.isNotEmpty()) {
                                    isCampaignEducationShown = true
                                    val index = getFirstIndexOfHomeSupplierView(rvCustomer)

                                    val title = getString(R.string.merchant_added_your, it[index].name)

                                    lifecycleScope.launch {
                                        localInAppNotificationHandler.get()
                                            .generateTapTarget(
                                                weakScreen = WeakReference(requireActivity()),
                                                tapTarget = TapTargetLocal(
                                                    screenName = label,
                                                    targetView = WeakReference(rvCustomer.getChildAt(index)),
                                                    title = title,
                                                    listener = { _, state ->
                                                        executeIfFragmentViewAvailable {
                                                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                                                onSupplierSelected(it[index].id, it[index].registered)
                                                                homeEventTracker.get().trackInAppNotificationClicked(
                                                                    PropertyValue.HOME_PAGE,
                                                                    HomeEventTracker.PAY_ONLINE_CUSTOMER,
                                                                    true
                                                                )
                                                            } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                                                                onSupplierSelected(it[index].id, it[index].registered)
                                                                homeEventTracker.get().trackInAppNotificationClicked(
                                                                    PropertyValue.HOME_PAGE,
                                                                    HomeEventTracker.PAY_ONLINE_CUSTOMER,
                                                                    false
                                                                )
                                                            }
                                                        }
                                                    }
                                                )
                                            )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            1000
        )
    }

    private fun getFirstIndexOfHomeSupplierView(rvCustomer: RecyclerView): Int {
        return rvCustomer.children.indexOfFirst { it is HomeSupplierView }
            .takeUnless { it < 0 }
            ?: 0 // Specifically return 0 if not found
    }

    private fun initPayablesCarouselIfNeeded() = with(binding.payablesOnboardingCarousel) {
        if (adapter == null) {
            adapter = PayablesOnboardingPagerAdapter(
                childFragmentManager,
                lifecycle
            )
            TabLayoutMediator(binding.pageIndicator, this) { _, _ -> }.attach()

            PayablesOnboardingPagerAdapter.content.forEachIndexed { index, page ->
                binding.pageIndicator.getTabAt(index)?.setIcon(
                    when (page) {
                        is ImageBacked -> R.drawable.image_pager_selector
                        is VideoBacked -> R.drawable.video_pager_selector
                    }
                )
            }

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                private var first = true

                override fun onPageScrollStateChanged(state: Int) {}

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    if (first && positionOffset == 0f && positionOffsetPixels == 0) {
                        onPageSelected(currentItem)
                        first = false
                    }
                }

                override fun onPageSelected(position: Int) {
                    PayablesOnboardingPagerAdapter.content.getOrNull(position)?.also {
                        when (it) {
                            is ImageBacked -> supplierAnalyticsEvents.get()
                                .trackPayablesOnboardingCarouselTabSelected(position, "Image")
                            is VideoBacked -> supplierAnalyticsEvents.get()
                                .trackPayablesOnboardingCarouselTabSelected(position, "Video", it.youtubeId)
                        }
                    }
                }
            })
        }
    }

    /************************ Rendering in UI  ***********************/
    @AddTrace(name = HomeTraces.RENDER_HOME_SUPPLIER_TAB)
    override fun render(state: State) {

        controller.setState(state)

        state.supplier?.tabCount?.let { (parentFragment as HomeFragment).setTabHeaderCount(this, it) }

        state.supplier?.let {
            (parentFragment as HomeFragment).setSupplierCount(
                it.suppliers.size,
                if (it.suppliers.isNotEmpty()) it.suppliers[0].name else null,
                if (it.suppliers.isNotEmpty()) it.suppliers[0].balance else null
            )
        }

        state.supplier?.suppliers?.also {
            videoUrl = if (it.isEmpty()) {
                state.videoUrl1
            } else {
                state.videoUrl2
            }
            with(binding) {
                when {
                    it.isEmpty() -> {
                        if (state.canShowCarouselEducation) {
                            learnMore.isVisible = false
                            icKnowMoreSupplier.isVisible = false
                            emptyContainer.isVisible = false

                            payablesOnboardingUi.isVisible = true
                            initPayablesCarouselIfNeeded()
                        } else {
                            payablesOnboardingUi.isVisible = false

                            emptyContainer.isVisible = state.canShowSupplierTabVideo.not()

                            learnMore.isVisible = true
                            icKnowMoreSupplier.isVisible = true
                        }
                    }
                    it.size <= 3 -> {
                        emptyContainer.isVisible = false
                        payablesOnboardingUi.isVisible = false

                        learnMore.isVisible = true
                        icKnowMoreSupplier.isVisible = true
                    }
                    else -> {
                        learnMore.isVisible = false
                        icKnowMoreSupplier.isVisible = false
                        emptyContainer.isVisible = false
                        payablesOnboardingUi.isVisible = false
                    }
                }
            }
        }
    }

    override fun onSorted() {
    }

    override fun onSupplierSelected(supplierId: String, registered: Boolean) {
        gotoSupplierScreen(supplierId, registered)
    }

    override fun onSupplierProfileSelected(supplier: Supplier) {
        supplierAnalyticsEvents.get().trackSupplierProfileIconClicked(
            accountId = supplier.id,
            screen = SupplierAnalyticsEvents.SupplierPropertyValue.SUPPLIER,
            relation = SupplierAnalyticsEvents.SupplierPropertyValue.SUPPLIER
        )
        pushIntent(SupplierTabContract.Intent.OnProfileClick(supplier))
    }

    private fun openSupplierProfileDialog(supplier: Supplier) {
        if (supplier.state != Supplier.BLOCKED) {
            activity?.runOnUiThread {
                val bottomSheet = SupplierProfileBottomSheet.newInstance(supplier.id)
                bottomSheet.show(childFragmentManager, "SupplierProfileBottomSheet")
            }
        }
    }

    private fun openSupplierPaymentDialog(supplier: Supplier) {
        val paymentDialogFrag = SupplierPaymentDialog.newInstance(
            supplier.id
        )
        paymentDialogFrag.show(childFragmentManager, SupplierPaymentDialog.TAG)
        paymentDialogFrag.initialise(object : SupplierPaymentDialog.SupplierPaymentListener {
            override fun onCallIconClicked(supplier: Supplier) {
                tracker.get().trackCallRelationShip(
                    PropertyValue.SUPPLIER_SCREEN,
                    PropertyValue.SUPPLIER,
                    supplier.id
                )
                callSupplier(supplier)
            }
        })
    }

    private fun goToSupplierLearnMoreWebLink(value: String) {
        legacyNavigator.get().goToWebViewScreen(requireActivity(), value)
    }

    internal fun callSupplier(supplier: Supplier) {
        Permission.requestCallPermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                    tracker.get().trackRuntimePermission(PropertyValue.HOME_PAGE, Event.CALL, true)
                }

                override fun onPermissionGranted() {
                    val intent = Intent(Intent.ACTION_CALL)
                    intent.data = Uri.parse(getString(`in`.okcredit.supplier.R.string.call_template, supplier.mobile))
                    startActivity(intent)
                }

                override fun onPermissionDenied() {
                }

                override fun onPermissionPermanentlyDenied() {
                }
            }
        )
    }

    fun gotoLogin() {
        legacyNavigator.get().goToLoginScreenForAuthFailure(requireActivity())
    }

    private fun gotoSupplierScreen(supplierId: String, registered: Boolean) {
        activity?.runOnUiThread {
            val flow = if (getCurrentState().isMerchantFromCollectionCampaign) {
                AppConstants.PAYMENT_INSTALL_LINK_UTM_CAMPAIGN
            } else {
                null
            }
            hideSoftKeyboard()
            tracker.get().trackViewRelationship(
                list = PropertyValue.FALSE,
                relation = PropertyValue.SUPPLIER,
                search = PropertyValue.FALSE,
                accountId = supplierId,
                flow = flow,
                commonLedger = registered
            )

            legacyNavigator.get().goToSupplierScreen(requireActivity(), supplierId)
        }
    }

    companion object {
        fun newInstance(): SupplierTabFragment {
            return SupplierTabFragment()
        }
    }

    /************************ Lifecycle  ***********************/
    override fun onDestroy() {
        super.onDestroy()

        if (alertDialog != null && alertDialog?.isShowing == true) {
            alertDialog?.dismiss()
        }
    }

    override fun onPause() {
        super.onPause()
        supplierEducationRunnable?.let {
            binding.rvCustomer.removeCallbacks(it)
        }
        broadcastHelper.get().sendBroadcast(requireContext(), Intent(BroadcastHelper.IntentFilters.PlayerPause))
    }

    override fun videoStartedListener(youTubeState: String) {
        this.youTubeState = youTubeState
        tracker.get().trackSupplierEducationVideo(
            type = videoUrl,
            source = getCurrentState().sourceScreen,
            interaction = PropertyValue.STARTED,
            method = PropertyValue.VIDEO,
            format = PropertyValue.TEXT,
            error = null
        )
    }

    override fun videoPlayListener(youTubeState: String) {
        this.youTubeState = youTubeState
        tracker.get().trackSupplierEducationVideo(
            type = videoUrl,
            source = getCurrentState().sourceScreen,
            interaction = PropertyValue.PLAY,
            method = PropertyValue.VIDEO,
            format = PropertyValue.TEXT,
            error = null
        )
    }

    override fun videoPauseListener(youTubeState: String) {
        this.youTubeState = youTubeState
        if (!backPressed) {
            tracker.get().trackSupplierEducationVideo(
                type = videoUrl,
                source = getCurrentState().sourceScreen,
                interaction = PropertyValue.PAUSED,
                method = PropertyValue.VIDEO,
                format = PropertyValue.TEXT,
                error = null
            )
        }
    }

    override fun videoCompletedListener(youTubeState: String) {
        this.youTubeState = youTubeState
        tracker.get().trackSupplierEducationVideo(
            type = videoUrl,
            source = getCurrentState().sourceScreen,
            interaction = PropertyValue.COMPLETED,
            method = PropertyValue.VIDEO,
            format = PropertyValue.TEXT,
            error = null
        )
    }

    override fun onBackPressed(): Boolean {
        backPressed = true
        if (youTubeState.equals("PAUSED") || youTubeState.equals("PLAYING")) {
            tracker.get().trackSupplierEducationVideo(
                type = videoUrl,
                source = getCurrentState().sourceScreen,
                interaction = PropertyValue.CLOSE,
                method = PropertyValue.VIDEO,
                format = PropertyValue.TEXT,
                error = null
            )
        }
        tracker.get().trackSupplierEducationVideo(
            type = videoUrl,
            source = getCurrentState().sourceScreen,
            interaction = PropertyValue.COMPLETED,
            method = PropertyValue.TEXT,
            format = PropertyValue.TEXT,
            error = null
        )

        return true
    }

    fun onFocusRemoved() {
        isFocused = false
        pushIntent(SupplierTabContract.Intent.NativeVideoState(CONFIG.PAUSE))
    }

    override fun onVideoResume() {
        (parentFragment as? HomeFragment)?.onSupplierVideoResumed()
        pushIntent(SupplierTabContract.Intent.NativeVideoState(CONFIG.RESUME))
    }

    override fun onVideoPause() {
        (parentFragment as? HomeFragment)?.onSupplierVideoStopped()
        pushIntent(SupplierTabContract.Intent.NativeVideoState(CONFIG.PAUSE))
    }

    override fun onVideoAttached() {
        isAttached = true
        checkCanPlayVideo()
    }

    override fun onVideoDetached() {
        isAttached = false
    }

    override fun onVideoStarted() {
        tracker.get().trackVideoEvents(PropertyValue.STARTED, PropertyValue.SUPPLIER_TAB, PropertyValue.NATIVE)
    }

    override fun onVideoCompleted() {
        tracker.get().trackVideoEvents(PropertyValue.COMPLETED, PropertyValue.SUPPLIER_TAB, PropertyValue.NATIVE)
    }

    override fun onVideoErrorOccured() {
        tracker.get().trackVideoEvents(PropertyValue.FAIL, PropertyValue.SUPPLIER_TAB, PropertyValue.NATIVE)
    }

    fun onFocusGained() {
        isFocused = true
        checkCanPlayVideo()
    }

    private fun checkCanPlayVideo() {
        if (isFocused && isAttached) {
            pushIntent(SupplierTabContract.Intent.OnVideoAttached)
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GotoLogin -> gotoLogin()
            is ViewEvent.GoToSupplierLearnMoreWebLink -> goToSupplierLearnMoreWebLink(event.value)
            is ViewEvent.OpenSupplierProfileDialog -> openSupplierProfileDialog(event.supplier)
            is ViewEvent.OpenSupplierPaymentDialog -> openSupplierPaymentDialog(event.supplier)
        }
    }
}
