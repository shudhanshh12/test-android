package `in`.okcredit.collection_ui.ui.home.merchant_qr

import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Screen
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.backend.utils.QRCodeUtils
import `in`.okcredit.collection.contract.CollectionConstants
import `in`.okcredit.collection.contract.CollectionDestinationType
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.collection.contract.OnlineCollectionNotification
import `in`.okcredit.collection.contract.PayoutType
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.analytics.OnlineCollectionTracker
import `in`.okcredit.collection_ui.analytics.OnlineCollectionTracker.Screen.collectionQr
import `in`.okcredit.collection_ui.databinding.QrCodeFragmentBinding
import `in`.okcredit.collection_ui.dialogs.DeleteMerchantDestinationConfirmationDialog
import `in`.okcredit.collection_ui.dialogs.GlobalInfoDialog
import `in`.okcredit.collection_ui.dialogs.OnlineCollectionsOptionsDialog
import `in`.okcredit.collection_ui.dialogs.SuccessDialog
import `in`.okcredit.collection_ui.ui.home.CollectionHomeEventsTracker
import `in`.okcredit.collection_ui.ui.home.add.AddMerchantDestinationDialog
import `in`.okcredit.collection_ui.ui.home.merchant_qr.QrCodeContract.*
import `in`.okcredit.collection_ui.ui.home.usecase.FindInfoBannerForMerchantQr
import `in`.okcredit.collection_ui.ui.insights.CollectionInsightsActivity
import `in`.okcredit.collection_ui.ui.passbook.PassbookActivity
import `in`.okcredit.collection_ui.ui.passbook.payments.views.OnlinePaymentsView
import `in`.okcredit.collection_ui.ui.referral.TargetedReferralActivity
import `in`.okcredit.communication_inappnotification.contract.LocalInAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.ui.local.TooltipLocal
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.home.HomeNavigator
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.addTo
import `in`.okcredit.web.WebExperiment
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.skydoves.balloon.ArrowOrientation
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.qr_code_fragment.view.*
import kotlinx.coroutines.launch
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.utils.TextDrawableUtils
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.contract.AppLock
import tech.okcredit.contract.Constants
import tech.okcredit.contract.OnSetPinClickListener
import tech.okcredit.contract.OnUpdatePinClickListener
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class QrCodeFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "QRCodeScreen",
        R.layout.qr_code_fragment
    ),
    OnSetPinClickListener,
    OnUpdatePinClickListener {

    companion object {
        const val TAG = "QRCodeScreen"
        const val DELETE_COLLECTION = 151
        const val DELETE_COLLECTION_SET_PIN = 15001
        const val DELETE_COLLECTION_UPDATE_PIN = 15002

        const val HOME_SELECT_CUSTOMER = 15003
        const val HOME_SELECT_SUPPLIER = 15004

        const val MERCHANT_QR_SCREEN = "merchant_qr_screen"
    }

    internal val binding: QrCodeFragmentBinding by viewLifecycleScoped(QrCodeFragmentBinding::bind)

    @Inject
    internal lateinit var collectionTracker: Lazy<CollectionTracker>

    @Inject
    lateinit var onlineCollectionTracker: OnlineCollectionTracker

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var homeNavigator: Lazy<HomeNavigator>

    @Inject
    internal lateinit var appLock: Lazy<AppLock>

    @Inject
    internal lateinit var tracker: Tracker

    @Inject
    internal lateinit var collectionHomeEventsTracker: Lazy<CollectionHomeEventsTracker>

    @Inject
    lateinit var localInAppNotificationHandler: Lazy<LocalInAppNotificationHandler>

    private var currentQrIntent = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.disableScreanCapture()
        loadData()
        setListeners()
        listenForFCM()
    }

    private fun loadData() = loadIntents(
        Intent.Load,
        Intent.LoadKycDetails,
        Intent.LoadOnlinePaymentTotal,
        Intent.LoadNewOnlinePaymentsCount,
        Intent.LoadKycCompleted,
        Intent.LoadMerchant,
        Intent.LoadCollectionProfileMerchant,
        Intent.LoadQrFirst,
    )

    private fun listenForFCM() = OnlineCollectionNotification.toObservable()
        .subscribeOn(ThreadUtils.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map { onlinePayment ->
            if (isVisible) {
                val msg = getString(R.string.received_amount, CurrencyUtil.formatV2(onlinePayment.amount))
                showReceivedDialog(msg)
            }
        }.subscribe()
        .addTo(autoDisposable)

    private fun showReceivedDialog(msg: String) {
        val dialog = SuccessDialog.newInstance(msg)
        dialog.show(childFragmentManager, SuccessDialog.TAG)
    }

    private fun setListeners() {
        binding.apply {
            qrToolbar.toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
            onlineCollection.root.debounceClickListener {
                goToOnlinePaymentsScreen()
            }
            qrToolbar.insights.debounceClickListener {
                goToMerchantDestinationScreen()
            }
            ivSuccess.debounceClickListener {
                if (isStateInitialized() && getCurrentState().infoBanner is FindInfoBannerForMerchantQr.InfoBanner.RefundAlert) {
                    showAddBankDetailsAlertToolTip()
                }
            }
            imageQrIntent.debounceClickListener {
                if (!getCurrentState().merchantCollectionState.showQrLocked) {
                    binding.root.transitionToEnd()
                } else {
                    pushIntent(Intent.QrTapped)
                }
            }
            cardInfo.debounceClickListener {
                pushIntent(Intent.InfoCardTapped)
            }

            buttonSaveQr.debounceClickListener {
                requestStoragePermission()
                tracker.trackEvents(
                    CollectionTracker.CollectionEvent.SAVE_QR,
                    screen = CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN
                )
            }

            buttonRemind.debounceClickListener {
                collectionHomeEventsTracker.get().trackSendPaymentRequest(CollectionHomeEventsTracker.Type.RECEIVE)
                homeNavigator.get().goToHomeSearchScreenForResult(this@QrCodeFragment, HOME_SELECT_CUSTOMER, true)
            }

            buttonPayOnline.debounceClickListener {
                collectionHomeEventsTracker.get().trackSendPaymentRequest(CollectionHomeEventsTracker.Type.SEND)
                homeNavigator.get().goToHomeSearchScreenForResult(this@QrCodeFragment, HOME_SELECT_SUPPLIER, false)
            }

            buttonShareQr.debounceClickListener {
                pushIntent(Intent.ShareMerchantQR)
            }

            buttonOrderQr.debounceClickListener {
                pushIntent(Intent.OrderMerchantQR)
            }

            buttonAddBank.debounceClickListener {
                pushIntent(Intent.ShowAddMerchantDestinationScreen)
            }

            imageCloseExpandedQr.debounceClickListener {
                binding.root.transitionToStart()
            }

            qrToolbar.verticalDotIcon.debounceClickListener {
                tracker.trackEvents(
                    CollectionTracker.CollectionEvent.VIEW_COLLECTION_MORE,
                    screen = CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN
                )
                pushIntent(Intent.OpenBottomSheetDialog)
            }

            binding.root.addTransitionListener(object : MotionLayout.TransitionListener {
                override fun onTransitionStarted(motionLayout: MotionLayout, startId: Int, endId: Int) {
                }

                override fun onTransitionChange(motionLayout: MotionLayout, startId: Int, endId: Int, progress: Float) {
                }

                override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                    when (currentId) {
                        R.id.end -> {
                            collectionHomeEventsTracker.get().trackViewMerchantQr()
                            binding.viewExpandedQrBg.debounceClickListener { }
                        }
                        R.id.start -> {
                            collectionHomeEventsTracker.get().trackDismissMerchantQr()
                            binding.viewExpandedQrBg.setOnClickListener(null)
                        }
                    }
                }

                override fun onTransitionTrigger(
                    motionLayout: MotionLayout,
                    triggerId: Int,
                    positive: Boolean,
                    progress: Float,
                ) {
                }
            })
        }
    }

    internal fun trackKycBannerDisplayed(bannerType: String) {
        val state = getCurrentState()
        collectionTracker.get().trackEvents(
            eventName = CollectionTracker.CollectionEvent.KYC_BANNER_SHOWN,
            type = state.responseData.kycRiskCategory.value,
            screen = CollectionTracker.CollectionScreen.MERCHANT_QR,
            propertiesMap = PropertiesMap.create()
                .add("bannerType", bannerType)
        )
    }

    internal fun trackKycEvents(eventName: String) {
        val state = getCurrentState()
        collectionTracker.get().trackEvents(
            eventName = eventName,
            screen = CollectionTracker.CollectionScreen.MERCHANT_QR,
            propertiesMap = PropertiesMap.create()
                .add("merchant_id", state.responseData.business?.id ?: "")
                .add("kyc_status", state.responseData.kycStatus.value.lowercase())
                .add("risk_type", state.responseData.kycRiskCategory.value.lowercase())
        )
    }

    private fun gotoKycScreen() {
        legacyNavigator.get().goWebExperimentScreen(requireContext(), WebExperiment.Experiment.KYC.type)
    }

    internal fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            tracker.trackEvents(
                CollectionTracker.CollectionEvent.VIEW_STORAGE_PERMISSION,
                type = PropertyValue.STORAGE,
                screen = Screen.MERCHANT_DESTINATION_SCREEN
            )
        }
        Permission.requestStoragePermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                    tracker.trackRuntimePermission(
                        Screen.MERCHANT_DESTINATION_SCREEN,
                        PropertyValue.STORAGE,
                        true
                    )
                }

                override fun onPermissionGranted() {
                    Observable.timer(300, TimeUnit.MILLISECONDS)
                        .subscribeOn(ThreadUtils.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            pushIntent(Intent.SaveMerchantQR)
                        }.addTo(autoDisposable)
                }

                override fun onPermissionDenied() {
                    tracker.trackRuntimePermission(
                        Screen.MERCHANT_DESTINATION_SCREEN,
                        PropertyValue.STORAGE,
                        false
                    )
                }
            }
        )
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun render(state: State) {
        binding.root.setViewVisibility(R.id.progressLoading, if (state.loading) View.VISIBLE else View.GONE)
        if (state.loading) {
            return
        }

        setCollectionDetails(state.merchantCollectionState, state.showOrderQr)
        setInfoBanner(state)
        setOnlinePaymentBar(state.showOnlinePayments, state.onlinePaymentState)
        binding.root.setViewVisibility(R.id.buttonAddBank, if (state.showAddBankDetails) View.VISIBLE else View.GONE)
        binding.buttonAddBank.isVisible = state.showAddBankDetails
    }

    private fun setCollectionDetails(merchantCollectionState: MerchantCollectionState, showOrderQr: Boolean) {
        binding.upiId.visible()
        if (merchantCollectionState.paymentAddress.isNullOrEmpty()) {
            binding.textMerchantName.gone()
            binding.upiId.text = getString(R.string.no_bank_account_added)
        } else {
            binding.textMerchantName.visible()
            binding.textMerchantName.text = merchantCollectionState.merchantName
            binding.textExpandedMerchantName.text = merchantCollectionState.merchantName
            binding.textExpandedBankDetails.text = merchantCollectionState.paymentAddress
            binding.upiId.text = merchantCollectionState.paymentAddress
        }

        if (merchantCollectionState.showPaymentViews) {
            showAllPaymentViews()
        } else {
            hideAllPaymentViews()
        }

        if (!merchantCollectionState.showQrLocked && !merchantCollectionState.qrIntent.isNullOrEmpty()) {
            // this is done to avoid creating bitmap multiple times
            if (currentQrIntent != merchantCollectionState.qrIntent) {
                setQrCode(merchantCollectionState.qrIntent)
                currentQrIntent = merchantCollectionState.qrIntent
            }

            val defaultPic = TextDrawableUtils.getRoundTextDrawable(merchantCollectionState.merchantName)
            GlideApp.with(this)
                .load(merchantCollectionState.merchantProfileImage)
                .placeholder(defaultPic)
                .circleCrop()
                .error(defaultPic)
                .fallback(defaultPic)
                .into(binding.imageQrPerson)
            val expandedDefault = TextDrawableUtils.getRoundTextDrawable(merchantCollectionState.merchantName)
            GlideApp.with(this)
                .load(merchantCollectionState.merchantProfileImage)
                .placeholder(expandedDefault)
                .circleCrop()
                .error(expandedDefault)
                .fallback(expandedDefault)
                .into(binding.imageExpandedQrPerson)
            binding.imageQrPerson.visible()

            binding.buttonShareQr.visible()
            binding.buttonSaveQr.visible()
            binding.saveQrDivider.visible()
            binding.shareQrDivider.isVisible = showOrderQr
            binding.buttonOrderQr.isVisible = showOrderQr
            binding.buttonDivider.visible()
        } else {
            binding.imageQrPerson.gone()
            binding.buttonShareQr.gone()
            binding.buttonSaveQr.gone()
            binding.saveQrDivider.gone()
            binding.shareQrDivider.gone()
            binding.buttonOrderQr.gone()
            binding.buttonDivider.invisible()
            binding.imageQrIntent.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.kyc_qr_restricted
                )
            )
        }
        binding.layoutQrLocked.isVisible = merchantCollectionState.showQrLocked
    }

    private fun showAllPaymentViews() {
        binding.apply {
            qrToolbar.verticalDotIcon.visible()
        }
    }

    private fun hideAllPaymentViews() {
        binding.apply {
            qrToolbar.verticalDotIcon.gone()
        }
    }

    private fun setInfoBanner(state: State) {
        binding.ivSuccess.isVisible = !state.merchantCollectionState.paymentAddress.isNullOrBlank()
        binding.ivSuccess.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_success
            )
        )
        when (state.infoBanner) {
            is FindInfoBannerForMerchantQr.InfoBanner.DailyLimitAvailable -> {
                setCardInfo(
                    icon = R.drawable.ic_icon_info,
                    info = Html.fromHtml(
                        getString(
                            R.string.t_002_payments_MQR_Unlocked_daily,
                            CurrencyUtil.formatV2(state.infoBanner.limit)
                        )
                    ),
                    cardBackground = R.color.indigo_lite,
                    strokeColor = R.color.indigo_lite_1
                )
            }
            is FindInfoBannerForMerchantQr.InfoBanner.DailyLimitReached -> {
                setCardInfo(
                    icon = R.drawable.ic_icon_info,
                    info = Html.fromHtml(getString(R.string.t_002_payments_MQR_Unlocked_daily_limit_reached)),
                    cardBackground = R.color.red_lite,
                    strokeColor = R.color.red_lite_1
                )
            }
            is FindInfoBannerForMerchantQr.InfoBanner.KycDoneLimitReached -> {
                setCardInfo(
                    icon = R.drawable.ic_icon_info,
                    info = getString(R.string.t_002_payments_MQR_daily_limit_reached),
                    cardBackground = R.color.indigo_lite,
                    strokeColor = R.color.indigo_lite_1
                )
            }
            FindInfoBannerForMerchantQr.InfoBanner.KycFailed -> {
                setCardInfo(
                    icon = R.drawable.kyc_ic_pan,
                    info = Html.fromHtml(getString(R.string.t_002_payments_MQR_Unlocked_kyc_rejected)),
                    cardBackground = R.color.red_lite,
                    strokeColor = R.color.red_lite_1
                )
            }
            FindInfoBannerForMerchantQr.InfoBanner.KycPending -> {
                setCardInfo(
                    icon = R.drawable.kyc_ic_pan,
                    info = Html.fromHtml(getString(R.string.t_002_payments_MQR_Unlocked_kyc_submitted)),
                    cardBackground = R.color.orange_lite,
                    strokeColor = R.color.orange_lite_1
                )
            }
            is FindInfoBannerForMerchantQr.InfoBanner.RefundAlert -> {
                setCardInfo(
                    icon = R.drawable.ic_error,
                    info = Html.fromHtml(getString(R.string.t_002_payments_MQR_settlement_blocked)),
                    cardBackground = R.color.red_lite,
                    strokeColor = R.color.red_lite_1
                )
            }
            FindInfoBannerForMerchantQr.InfoBanner.TargetedReferral -> {
                setCardInfo(
                    icon = R.drawable.ic_gift_box_green,
                    info = getString(R.string.referral_msg_to_setup_payment),
                    cardBackground = R.color.green_lite,
                    strokeColor = R.color.green_lite_1
                )
            }
            is FindInfoBannerForMerchantQr.InfoBanner.TrialLimitAvailable -> {
                setCardInfo(
                    icon = R.drawable.ic_icon_info,
                    info = Html.fromHtml(
                        getString(
                            R.string.t_002_payments_MQR_Unlocked_trial,
                            CurrencyUtil.formatV2(state.infoBanner.limit)
                        )
                    ),
                    cardBackground = R.color.indigo_lite,
                    strokeColor = R.color.indigo_lite_1
                )
            }
            is FindInfoBannerForMerchantQr.InfoBanner.TrialLimitReached -> {
                setCardInfo(
                    icon = R.drawable.ic_icon_info,
                    info = Html.fromHtml(getString(R.string.t_002_payments_MQR_Unlocked_trial_limit_reached)),
                    cardBackground = R.color.red_lite,
                    strokeColor = R.color.red_lite_1
                )
            }
            else -> {
                binding.root.setViewVisibility(R.id.cardInfo, View.GONE)
            }
        }
    }

    private fun setCardInfo(
        @DrawableRes icon: Int,
        info: CharSequence,
        @ColorRes cardBackground: Int,
        @ColorRes strokeColor: Int,
    ) {
        binding.root.setViewVisibility(R.id.cardInfo, View.VISIBLE)
        binding.cardInfo.imageInfo.setImageDrawable(getDrawableCompact(icon))
        binding.cardInfo.textInfo.text = info
        binding.cardInfo.setCardBackgroundColor(requireContext().getColorCompat(cardBackground))
        binding.cardInfo.setStrokeColor(ColorStateList.valueOf(getColorCompat(strokeColor)))
    }

    private fun setOnlinePaymentBar(
        showOnlinePayments: Boolean,
        onlinePaymentState: OnlinePaymentState,
    ) {
        binding.apply {
            if (showOnlinePayments) {
                onlineCollection.badge.text = getString(R.string.online_payment_new_count, onlinePaymentState.newCount)
                if (onlinePaymentState.amount > 0) {
                    val type = if (onlinePaymentState.type == OnlinePaymentsView.TYPE_SUPPLIER_COLLECTION) {
                        getString(R.string.t_003_transaction_history_payment_given)
                    } else {
                        getString(R.string.t_003_transaction_history_payments_received)
                    }
                    val amount = CurrencyUtil.formatV2((onlinePaymentState.amount).toLong())
                    binding.onlineCollection.day.text =
                        getString(R.string.t_003_transaction_history_subtitle, type, amount)
                    onlineCollection.day.visible()
                } else {
                    onlineCollection.day.invisible()
                }

                if (onlinePaymentState.newCount > 0) {
                    onlineCollection.badge.visible()
                } else {
                    onlineCollection.badge.gone()
                }
                onlineCollection.root.visible()
            } else {
                onlineCollection.root.gone()
            }
        }
    }

    private fun setQrCode(qrIntent: String) {
        QRCodeUtils.getBitmapObservable(qrIntent, requireContext(), 270).subscribe {
            binding.imageQrIntent.setImageBitmap(it)
            binding.imageExpandedQr.setImageBitmap(it)
        }.addTo(autoDisposable)
    }

    private fun showAddBankDetailsAlertToolTip() {
        lifecycleScope.launch {
            localInAppNotificationHandler.get().generateTooltip(
                weakScreen = WeakReference(requireActivity()),
                tooltip = TooltipLocal(
                    targetView = WeakReference(binding.ivSuccess),
                    title = getString(R.string.add_bank_details_tooltip),
                    arrowOrientation = ArrowOrientation.TOP,
                    textSize = 14f,
                    backgroundColor = R.color.grey700,
                    screenName = label,
                    alignTop = false
                )
            )
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.GotoLogin -> gotoLogin()
            is ViewEvent.ShowSnackBar -> showSnackBar(getString(event.msg))
            ViewEvent.OpenBottomSheetDialog -> openBottomSheetDialog()
            ViewEvent.DeleteAccount -> goToVerificationScreen()
            ViewEvent.GoToCollectionsSetupScreen -> goToCollectionSetupScreen()
            is ViewEvent.ShareMerchant -> {
                startActivity(event.intent)
            }
            is ViewEvent.GoToAuthScreen -> {
                startActivityForResult(
                    appLock.get().appLock(
                        getString(R.string.enterpin_screen_deeplink),
                        requireActivity(),
                        TAG
                    ),
                    DELETE_COLLECTION
                )
            }
            is ViewEvent.GoToSetNewPinScreen -> {
                appLock.get().showSetNewPin(
                    requireActivity().supportFragmentManager,
                    this,
                    DELETE_COLLECTION_SET_PIN
                )
            }

            is ViewEvent.ShowUpdatePinDialog -> {
                appLock.get().showUpdatePin(
                    requireActivity().supportFragmentManager,
                    this,
                    DELETE_COLLECTION_UPDATE_PIN
                )
            }
            is ViewEvent.SyncDone -> pushIntent(Intent.CheckIsFourDigit())
            is ViewEvent.CheckFourDigitPinDone -> {
                if (event.isFourDigitPinSet) {
                    startActivityForResult(
                        appLock.get().appLock(
                            getString(R.string.enterpin_screen_deeplink),
                            requireActivity(),
                            TAG
                        ),
                        DELETE_COLLECTION
                    )
                } else {
                    pushIntent(Intent.UpdatePin)
                }
            }
            ViewEvent.ShowAddMerchantDestinationScreen -> showAddMerchantDestinationDialog()
            ViewEvent.GotoReferralEducationScreen -> moveToReferralEducation()
            ViewEvent.GotoReferralInviteListScreen -> moveToReferralInvitation()
            is ViewEvent.SendReminder -> startActivity(event.intent)
            ViewEvent.GoToKyc -> gotoKycScreen()
            is ViewEvent.OpenSupplierScreen -> {
                legacyNavigator.get().goToDeeplinkScreen(requireContext(), event.screen)
            }
            ViewEvent.OpenOrderQr -> {
                legacyNavigator.get().goToWebViewScreen(requireActivity(), getString(R.string.link_order_qr))
            }
            is ViewEvent.OpenWhatsAppForHelp -> startActivity(event.value)
        }
    }

    private fun moveToReferralInvitation() {
        startActivity(
            TargetedReferralActivity.getIntent(
                requireContext(),
                TargetedReferralActivity.REFERRAL_INVITE_LIST
            )
        )
    }

    private fun moveToReferralEducation() {
        startActivity(
            TargetedReferralActivity.getIntent(
                requireContext(),
                TargetedReferralActivity.REFERRAL_EDUCATION_SCREEN
            )
        )
    }

    private fun showSnackBar(msg: String) {
        view?.snackbar(msg, 2000)?.show()
    }

    private fun openBottomSheetDialog() {
        val bottomSheet = OnlineCollectionsOptionsDialog.newInstance(getCurrentState().responseData.liveSalesStatus)
        bottomSheet.show(requireActivity().supportFragmentManager, OnlineCollectionsOptionsDialog.TAG)

        bottomSheet.initialize(object : OnlineCollectionsOptionsDialog.CollectionsOptionsListener {
            override fun onUpdateAccount() {
                showOtpInfoPopup()
                tracker.trackEvents(
                    CollectionTracker.CollectionEvent.UPDATE_COLLECTION,
                    screen = CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN,
                )
            }

            override fun onDelete() {
                showDeleteMerchantDestinationDialog()
                tracker.trackEvents(
                    CollectionTracker.CollectionEvent.DELETE_COLLECTION,
                    screen = CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN
                )
            }

            override fun onShare() {
                pushIntent(Intent.ShareMerchantQR)
                tracker.trackEvents(
                    CollectionTracker.CollectionEvent.SHARE_QR,
                    screen = CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN
                )
            }

            override fun onSave() {
                requestStoragePermission()
                tracker.trackEvents(
                    CollectionTracker.CollectionEvent.SAVE_QR,
                    screen = CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN
                )
            }

            override fun goToOnlineStatementActivity() {
                goToAccountStatementActivity("collection_screen")
                tracker.trackEvents(
                    CollectionTracker.CollectionEvent.VIEW_COLLECTION_STATEMENT,
                    screen = CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN
                )
            }

            override fun onClose() {
            }
        })
    }

    internal fun showDeleteMerchantDestinationDialog() {
        val isKycCompleted = getCurrentState().responseData.kycStatus == KycStatus.COMPLETE
        val dialogFrag = DeleteMerchantDestinationConfirmationDialog.newInstance(isKycCompleted)
        dialogFrag.show(childFragmentManager, DeleteMerchantDestinationConfirmationDialog.TAG)

        dialogFrag.initialise(object : DeleteMerchantDestinationConfirmationDialog.DeleteMerchantDestinationListener {
            override fun onDelete() {
                callDeleteIntent()
                tracker.trackEvents(
                    CollectionTracker.CollectionEvent.CONFIRM_DELETE_COLLECTION,
                    type = getCurrentState().responseData.collectionMerchantProfile.type,
                    screen = CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN
                )
            }

            override fun onCancel() {
                tracker.trackEvents(
                    CollectionTracker.CollectionEvent.CANCEL_DELETE_COLLECTION,
                    type = getCurrentState().responseData.collectionMerchantProfile.type,
                    screen = CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN
                )
            }
        })
    }

    fun callDeleteIntent() {
        val state = getCurrentState()
        if (state.responseData.isSetPassword) {
            if (state.responseData.isMerchantPrefSync) {
                if (state.responseData.isFourDigitPinSet) {
                    pushIntent(Intent.DeleteAccountPressed)
                } else {
                    pushIntent(Intent.CheckIsFourDigit())
                }
            } else {
                pushIntent(Intent.SyncMerchantPref)
            }
        } else {
            pushIntent(Intent.SetNewPin)
        }
    }

    fun showOtpInfoPopup() {
        val state = getCurrentState()
        val isKycCompleted = getCurrentState().responseData.kycStatus == KycStatus.COMPLETE
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_lock)
        val descString: String
        val headingString: String
        if (state.responseData.collectionMerchantProfile.type == CollectionDestinationType.UPI.value) {
            descString = if (isKycCompleted.not()) {
                getString(R.string.update_upi_text)
            } else {
                getString(R.string.update_account_text_kyc)
            }
            headingString = getString(R.string.update_upi)
        } else {
            descString = if (isKycCompleted.not()) {
                getString(R.string.update_bank_text)
            } else {
                getString(R.string.update_account_text_kyc)
            }
            headingString = getString(R.string.update_bank)
        }

        GlobalInfoDialog.show(
            drawable = drawable!!,
            titleString = state.merchantCollectionState.paymentAddress ?: "",
            headingString = headingString,
            descString = descString,
            activity = requireActivity(),
            listener = object : GlobalInfoDialog.Listener {
                override fun onCancel() {
                    tracker.trackEvents(
                        CollectionTracker.CollectionEvent.CANCEL_UPDATE_COLLECTION,
                        screen = CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN
                    )
                }

                override fun onSuccess() {
                    tracker.trackEvents(
                        CollectionTracker.CollectionEvent.STARTED_UPDATE_COLLECTION,
                        screen = CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN
                    )
                    goToOtpScreen(CollectionConstants.UPDATE_PAYMENT_ADDRESS)
                }
            }
        ).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CollectionConstants.QR_SCANNER_REQUEST_CODE) {
            val dialogFragment: Fragment? = childFragmentManager.findFragmentByTag(AddMerchantDestinationDialog.TAG)

            if (dialogFragment != null) {
                data?.let {
                    val upiVpa = it.getStringExtra(CollectionConstants.UPI_ID)
                    val scanMethod = it.getStringExtra(CollectionConstants.METHOD)
                        ?: CollectionTracker.CollectionPropertyValue.CAMERA
                    (dialogFragment as AddMerchantDestinationDialog).setUpiVpaFromScanner(upiVpa, scanMethod)
                }
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == CollectionConstants.UPDATE_PAYMENT_ADDRESS) {
            showAddMerchantDestinationDialog()
        } else if (resultCode == Activity.RESULT_OK &&
            (
                requestCode == DELETE_COLLECTION ||
                    requestCode == DELETE_COLLECTION_SET_PIN ||
                    requestCode == DELETE_COLLECTION_UPDATE_PIN
                )
        ) {
            data?.let {
                if (it.getBooleanExtra(Constants.IS_AUTHENTICATED, false)) {
                    if (requestCode == DELETE_COLLECTION_UPDATE_PIN) {
                        collectionTracker.get().trackEvents(
                            eventName = CollectionTracker.CollectionEvent.SECURITY_PIN_CHANGED,
                            flow = CollectionInsightsActivity.MERCHANT_DESTINATION_SCREEN
                        )
                    }
                    if (requestCode == DELETE_COLLECTION_SET_PIN) {
                        collectionTracker.get().trackEvents(
                            eventName = CollectionTracker.CollectionEvent.SECURITY_PIN_SET,
                            flow = CollectionInsightsActivity.MERCHANT_DESTINATION_SCREEN
                        )
                    }

                    lifecycleScope.launchWhenResumed {
                        pushIntent(Intent.CheckPasswordSet)
                        pushIntent(Intent.DeleteAccount)
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == HOME_SELECT_SUPPLIER) {
            val supplierId = data?.getStringExtra(PropertyKey.ACCOUNT_ID)
            supplierId?.let {
                lifecycleScope.launchWhenResumed {
                    pushIntent(Intent.SupplierPayOnline(it))
                }
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == HOME_SELECT_CUSTOMER) {
            val customerId = data?.getStringExtra(PropertyKey.ACCOUNT_ID)
            customerId?.let {
                lifecycleScope.launchWhenResumed {
                    pushIntent(Intent.SendCustomerReminder(customerId))
                }
            }
        }
    }

    private fun showAddMerchantDestinationDialog(paymentMethodType: String? = null) {
        val paymentDialogFrag = AddMerchantDestinationDialog.newInstance(
            isUpdateCollection = true,
            paymentMethodType = paymentMethodType,
            source = collectionQr
        )
        paymentDialogFrag.show(childFragmentManager, AddMerchantDestinationDialog.TAG)
    }

    private fun goToOnlinePaymentsScreen() {
        onlineCollectionTracker.trackClickEventOnlineCollection()
        startActivity(android.content.Intent(requireContext(), PassbookActivity::class.java))
    }

    fun gotoLogin() {
        legacyNavigator.get().goToLoginScreenForAuthFailure(requireContext())
    }

    @UiThread
    fun goToAccountStatementActivity(sourceScreen: String?) {
        legacyNavigator.get().goToAccountStatementScreen(requireContext(), sourceScreen ?: "")
    }

    @UiThread
    fun goToOtpScreen(requestCode: Int) {
        legacyNavigator.get().goToOtpVerification(requireContext(), requestCode)
    }

    @UiThread
    fun goToVerificationScreen() {
        startActivityForResult(
            appLock.get().appLock(
                deeplink = getString(R.string.enterpin_screen_deeplink),
                context = requireActivity(),
                sourceScreen = CollectionInsightsActivity.MERCHANT_DESTINATION_SCREEN
            ),
            DELETE_COLLECTION
        )
    }

    private fun goToCollectionSetupScreen() {
        legacyNavigator.get().goToCollectionTutorialScreen(
            requireActivity(),
            Screen.MERCHANT_DESTINATION_SCREEN
        )
        requireActivity().finish()
    }

    private fun goToMerchantDestinationScreen() {
        startActivity(android.content.Intent(requireContext(), CollectionInsightsActivity::class.java))
    }

    override fun onSetPinClicked(requestCode: Int) {
        startActivityForResult(
            appLock.get().appLock(
                getString(R.string.changepin_screen_deeplink),
                requireActivity(),
                TAG
            ),
            requestCode
        )
    }

    override fun onDismissed() {
    }

    override fun onSetNewPinClicked(requestCode: Int) {
        startActivityForResult(
            appLock.get().appLock(
                getString(R.string.changepin_screen_deeplink),
                requireActivity(),
                TAG
            ),
            requestCode
        )
    }

    override fun onUpdateDialogDismissed() {
    }

    fun onMerchantDestinationAdded() {
        pushIntent(Intent.TriggerMerchantPayout(PayoutType.PAYOUT.value))
    }
}
