package tech.okcredit.home.ui.menu

import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.PropertyValue.MENU_ITEM
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.collection.contract.CollectionNavigator
import `in`.okcredit.collection.contract.KycDialogListener
import `in`.okcredit.collection.contract.KycDialogMode
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.dynamicview.DynamicViewKit
import `in`.okcredit.dynamicview.TargetSpec
import `in`.okcredit.dynamicview.component.menu.MenuComponentModel
import `in`.okcredit.dynamicview.component.recycler.RecyclerComponentModel
import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.merchant.contract.BusinessEvents
import `in`.okcredit.merchant.contract.BusinessNavigator
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.web.WebExperiment
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.TextDrawableUtils
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.home.BuildConfig
import tech.okcredit.home.R
import tech.okcredit.home.databinding.MenuFragmentBinding
import tech.okcredit.home.dialogs.BottomSheetInAppRating
import tech.okcredit.home.models.KycMenuItem
import tech.okcredit.home.ui.acccountV2.ui.AccountActivity
import tech.okcredit.home.ui.analytics.HomeEventTracker
import tech.okcredit.home.ui.menu.HomeMenuContract.*
import tech.okcredit.home.ui.settings.SettingsActivity
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeMenuFragment :
    BaseFragment<HomeMenuState, HomeMenuViewEvent, HomeMenuIntent>(
        "HomeMenuScreen",
        R.layout.menu_fragment
    ),
    BottomSheetInAppRating.OnBottomSheetFragmentListener {

    companion object {
        const val TAG = "HomeMenuFragment"
        private const val SOURCE = "Home Menu Tab"

        @JvmStatic
        fun newInstance() = HomeMenuFragment()
    }

    @Inject
    lateinit var dynamicViewKit: Lazy<DynamicViewKit>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var businessNavigator: Lazy<BusinessNavigator>

    @Inject
    lateinit var imageLoader: Lazy<IImageLoader>

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    @Inject
    internal lateinit var homeEventTracker: Lazy<HomeEventTracker>

    @Inject
    internal lateinit var collectionNavigator: Lazy<CollectionNavigator>

    private val binding: MenuFragmentBinding by viewLifecycleScoped(MenuFragmentBinding::bind)

    private var shouldTrackKycEntryPointView = false // makeshift solution to only track event when view is created

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            versionTextView.text =
                getString(
                    R.string.side_menu_version,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                    if (BuildConfig.DEBUG) BuildConfig.FLAVOR + BuildConfig.BUILD_TYPE else ""
                )
            profileView.setOnClickListener { openMerchantProfile() }
            accountTextView.setOnClickListener { openAccounts() }
            tvSettings.setOnClickListener {
                pushIntent(HomeMenuIntent.SettingsClicked)
            }
            helpSupportText.setOnClickListener { goToHelpScreen() }

            collectionTextView.setOnClickListener {
                pushIntent(HomeMenuIntent.CollectionClicked)
            }
            shouldTrackKycEntryPointView = true
            kycTextView.setOnClickListener {
                pushIntent(HomeMenuIntent.ShowKycStatusDialog)
            }
            cardCreateBusiness.setOnClickListener {
                goToCreateBusinessDialog()
            }
            textBilling.setOnClickListener {
                startActivity(
                    collectionNavigator.get().billingHomeIntent(requireContext())
                )
            }
        }
        binding.rootView.setTracker(performanceTracker)
    }

    private fun goToCreateBusinessDialog() {
        tracker.get().trackEvents(
            eventName = BusinessEvents.CREATE_BUSINESS_STARTED,
            propertiesMap = PropertiesMap.create().add(BusinessEvents.Key.KEY_SOURCE, BusinessEvents.Value.LEFT_DRAWER)
        )
        businessNavigator.get().showCreateBusinessDialog(childFragmentManager)
    }

    private fun goToHelpScreen() {
        legacyNavigator.get().goToHelpHomeScreen(requireActivity(), arrayListOf(), PropertyValue.HELP_SCREEN)
    }

    private fun gotoCollectionAdoptionScreen() {
        startActivity(collectionNavigator.get().qrCodeIntent(requireContext()))
    }

    private fun goToSettings() {
        val intent = Intent(requireContext(), SettingsActivity::class.java)
        startActivity(intent)
    }

    private val kycListener = object : KycDialogListener {
        override fun onDisplayed(eventName: String, campaign: String) {
            val kycMenuItem = getCurrentState().kycMenuItem
            if (kycMenuItem is KycMenuItem.Unavailable) return

            val kycStatus = (kycMenuItem as KycMenuItem.Available).kycStatus

            if (kycStatus == KycStatus.NOT_SET) {
                RecordException.recordException(
                    IllegalStateException(
                        "KycDialog should not be shown for status NOT_SET"
                    )
                )
                return
            }

            tracker.get().trackKycDialogPopup(
                type = kycStatus.value,
                source = MENU_ITEM,
            )
        }

        override fun onConfirmKyc(dontAskAgain: Boolean, eventName: String) {
            if (eventName.isEmpty()) return
            trackKycEvents(eventName)
        }

        override fun onCancelKyc(dontAskAgain: Boolean, eventName: String) {
            trackKycEvents(eventName)
        }

        override fun onDismissKyc(eventName: String) {
            trackKycEvents(eventName)
        }
    }

    internal fun trackKycEvents(eventName: String) {
        val state = getCurrentState()
        val kycMenuItem = (state.kycMenuItem as KycMenuItem.Available)
        val kycStatus = kycMenuItem.kycStatus
        val kycRiskCategory = kycMenuItem.kycRisk.kycRiskCategory
        tracker.get()
            .trackEvents(
                eventName = eventName,
                screen = PropertyValue.HOME_PAGE,
                source = MENU_ITEM,
                propertiesMap = PropertiesMap.create()
                    .add("kyc_status", kycStatus.value.lowercase(Locale.getDefault()))
                    .add("risk_type", kycRiskCategory.value.lowercase(Locale.getDefault()))
            )
    }

    private fun showKycStatusDialog() {
        val state = getCurrentState()
        val kycMenuItem = state.kycMenuItem
        if (kycMenuItem is KycMenuItem.Unavailable) {
            RecordException.recordException(
                IllegalStateException(
                    "ShowKycStatusDialog viewevent should not be triggered when KycMenuItem is Unavailable"
                )
            )
            return
        }

        val kycMenuItemTypeAvailable = (kycMenuItem as KycMenuItem.Available)
        val kycStatus = kycMenuItemTypeAvailable.kycStatus
        val kycRiskCategory = kycMenuItemTypeAvailable.kycRisk.kycRiskCategory

        if (kycStatus != KycStatus.NOT_SET) {
            collectionNavigator.get().showKycDialog(
                childFragmentManager,
                kycListener,
                kycDialogMode = KycDialogMode.Status,
                kycStatus = kycStatus,
                kycRiskCategory = kycRiskCategory,
                shouldShowCreditCardInfoForKyc = state.shouldShowCreditCardInfoForKyc,
            )
        } else {
            val queryParams = mapOf(SOURCE to MENU_ITEM)
            legacyNavigator.get().goWebExperimentScreen(
                requireContext(),
                WebExperiment.Experiment.KYC_SUPPLIER.type,
                queryParams
            )
        }
    }

    private fun openMerchantProfile() {
        legacyNavigator.get().goToMerchantProfile(requireContext())
    }

    private fun openAccounts() {
        val intent = Intent(requireContext(), AccountActivity::class.java)
        startActivity(intent)
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            binding.callCustomerCare.clicks().throttleFirst(300, TimeUnit.MILLISECONDS).map {
                homeEventTracker.get().trackCallCustomerCareClicked(SOURCE)
                HomeMenuIntent.CallHelp
            }
        )
    }

    override fun render(state: HomeMenuState) {
        renderHeader(state)
        renderCustomization(state)
        renderKyc(state.kycMenuItem)
        renderInventory(state)
        renderHelp(state)
    }

    private fun renderInventory(state: HomeMenuState) {
        binding.textBilling.isVisible = state.showInventoryAndBilling
    }

    private fun renderHeader(state: HomeMenuState) {
        binding.apply {
            state.business?.let {
                if (it.name == it.mobile) {
                    phoneTextView.visibility = View.INVISIBLE
                } else {
                    phoneTextView.visibility = View.VISIBLE
                }
                nameTextView.text = it.name
                phoneTextView.text = it.mobile

                val defaultPic = TextDrawableUtils.getRoundTextDrawable(it.name)
                if (it.profileImage.isNullOrBlank()) {
                    photoImageView.setImageDrawable(defaultPic)
                } else {
                    imageLoader.get().context(activity as AppCompatActivity)
                        .load(it.profileImage)
                        .placeHolder(defaultPic)
                        .scaleType(IImageLoader.CIRCLE_CROP)
                        .into(photoImageView)
                        .buildNormal()
                }
            }
            cardCreateBusiness.isVisible = state.canShowCreateBusiness
        }
    }

    private fun renderKyc(kycMenuItem: KycMenuItem) {
        if (shouldTrackKycEntryPointView) {
            shouldTrackKycEntryPointView = false
            trackKycEntryPointView()
        }
        when (kycMenuItem) {
            is KycMenuItem.Unavailable -> binding.kycTextView.gone()
            is KycMenuItem.Available -> {

                val (drawable, kycText) = when (kycMenuItem.kycStatus) {
                    KycStatus.NOT_SET -> {
                        Pair(R.drawable.icon_kyc_success, R.string.kyc_status_not_set)
                    }
                    KycStatus.FAILED -> Pair(R.drawable.icon_kyc_failed, R.string.kyc_status_failed)
                    KycStatus.COMPLETE -> Pair(R.drawable.icon_kyc_success, R.string.kyc_status_complete)
                    KycStatus.PENDING -> Pair(R.drawable.icon_kyc_pending, R.string.kyc_status_pending)
                }

                binding.kycTextView.apply {
                    setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
                    text = getString(kycText)
                    visible()
                }
            }
        }
    }

    private fun renderHelp(state: HomeMenuState) {
        binding.apply {
            callCustomerCare.isVisible = state.showCallCustomerCare()
            if (state.showCallCustomerCare()) {
                helpSupportText.setText(R.string.t_001_help_chat_and_faq)
            } else {
                helpSupportText.setText(R.string.help_support)
            }
        }
    }

    private fun renderCustomization(state: HomeMenuState) {
        binding.apply {
            state.customization?.let {
                val spec = TargetSpec(
                    it.target,
                    setOf(
                        RecyclerComponentModel::class.java,
                        MenuComponentModel::class.java
                    )
                )
                dynamicViewKit.get().render(dynamicView, it.component, spec)
            }
        }
    }

    private fun trackKycEntryPointView() {
        val kycMenuItem = getCurrentState().kycMenuItem
        if (kycMenuItem is KycMenuItem.Unavailable) return

        val kycStatus = (kycMenuItem as KycMenuItem.Available).kycStatus
        tracker.get().trackEntryPointViewed(
            source = MENU_ITEM,
            type = if (kycStatus == KycStatus.NOT_SET) PropertyValue.COMPLETE_KYC else PropertyValue.KYC_STATUS,
        )
    }

    override fun loadIntent(): UserIntent? {
        return HomeMenuIntent.Load
    }

    override fun handleViewEvent(event: HomeMenuViewEvent) {
        when (event) {
            is HomeMenuViewEvent.GoToCollectionScreen -> {
                startActivity(collectionNavigator.get().qrCodeIntent(requireContext()))
            }
            is HomeMenuViewEvent.GoToSettingsScreen -> {
                goToSettings()
            }
            HomeMenuViewEvent.ShowKycStatusDialog -> showKycStatusDialog()
            HomeMenuViewEvent.CallHelp -> callHelp()
        }
    }

    private fun callHelp() {
        permissionResult.launch(Manifest.permission.CALL_PHONE)
    }

    private val permissionResult: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (result) {
            // Call support directly
            tracker.get().trackCallPermissionGranted(SOURCE)
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse(getString(R.string.call_template, getCurrentState().helpNumber))
            }
            startActivity(intent)
        } else {
            // Open dialer
            tracker.get().trackCallPermissionDenied(SOURCE)
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse(getString(R.string.call_template, getCurrentState().helpNumber))
            }
            startActivity(intent)
        }
    }

    override fun goToPlayStoreForRateUs() {
        legacyNavigator.get().goToPlayStore(requireActivity())
    }

    override fun submitRatingAndFeedback(feedback: String, rating: Int) {
        pushIntent(HomeMenuIntent.SubmitFeedback(feedback, rating))
    }
}
