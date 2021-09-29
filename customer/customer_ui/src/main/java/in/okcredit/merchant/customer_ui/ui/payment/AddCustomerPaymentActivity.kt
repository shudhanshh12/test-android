package `in`.okcredit.merchant.customer_ui.ui.payment

import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.backend.utils.QRCodeUtils
import `in`.okcredit.collection.contract.CollectionNavigator
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.collection.contract.MerchantDestinationListener
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.ActivityAddCustomerPaymentBinding
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTransactionFragment
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity
import `in`.okcredit.merchant.customer_ui.ui.payment.AddCustomerPaymentContract.*
import `in`.okcredit.merchant.customer_ui.ui.payment.AddCustomerPaymentEventsTracker.Companion.CUSTOMER_QR_SCREEN
import `in`.okcredit.merchant.customer_ui.ui.payment.success.PaymentSuccessActivity
import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.addTo
import `in`.okcredit.shared.view.KycStatusView
import `in`.okcredit.web.WebExperiment
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.accounting.model.Transaction.Companion.PAYMENT
import tech.okcredit.android.base.extensions.debounceClickListener
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.extensions.getDrawableCompact
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.setViewVisibility
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.utils.TextDrawableUtils
import tech.okcredit.app_contract.LegacyNavigator
import java.util.*
import javax.inject.Inject

class AddCustomerPaymentActivity :
    BaseActivity<State, ViewEvent, Intent>("AddCustomerPayment"),
    MerchantDestinationListener,
    AmountFocusChangeListener {

    internal val binding: ActivityAddCustomerPaymentBinding by viewLifecycleScoped(ActivityAddCustomerPaymentBinding::inflate)

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var collectionNavigator: Lazy<CollectionNavigator>

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.kycStatusView.setListener(object : KycStatusView.Listener {
            override fun onBannerDisplayed(bannerType: String) {
                tracker.get().trackKycBannerShown(
                    type = getCurrentState().kycRiskCategory.value,
                    screen = CUSTOMER_QR_SCREEN,
                    bannerType = bannerType
                )
            }

            override fun onStartKyc(eventName: String) {
                trackKycEvents(eventName)
                gotoKycScreen()
            }

            override fun onClose(eventName: String) {
                trackKycEvents(eventName)
            }
        })

        binding.imageShowQr.debounceClickListener {
            binding.root.transitionToEnd()
        }

        binding.imageBack.debounceClickListener {
            onBackPressed()
        }

        binding.root.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout, startId: Int, endId: Int) {
            }

            override fun onTransitionChange(motionLayout: MotionLayout, startId: Int, endId: Int, progress: Float) {
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                val addTransactionFragment: AddTransactionFragment? =
                    supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as AddTransactionFragment?
                when (currentId) {
                    R.id.end -> {
                        binding.textAddPayment.text = getString(R.string.t_002_add_payment_add_amount)
                        addTransactionFragment?.clearAmount()
                    }
                    R.id.start -> {
                        pushIntent(Intent.MinimizeQr)
                        binding.textAddPayment.text = getString(R.string.add_payment)
                        addTransactionFragment?.requestAmountFocus()
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

        binding.layoutQrLocked.debounceClickListener {
            collectionNavigator.get().showAddMerchantDestinationDialog(supportFragmentManager, CUSTOMER_QR_SCREEN)
        }
    }

    internal fun gotoKycScreen() {
        legacyNavigator.get().goWebExperimentScreen(this, WebExperiment.Experiment.KYC.type)
    }

    internal fun trackKycEvents(eventName: String) {
        val state = getCurrentState()
        val kycStatus = state.kycStatus
        val kycRiskCategory = state.kycRiskCategory
        tracker.get().trackEvents(
            eventName = eventName,
            screen = CUSTOMER_QR_SCREEN,
            source = getCurrentState().source,
            propertiesMap = PropertiesMap.create()
                .add("kyc_status", kycStatus.value.lowercase(Locale.getDefault()))
                .add("risk_type", kycRiskCategory.value.lowercase(Locale.getDefault()))
        )
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: State) {
        if (state.loading) return

        binding.textCustomerName.text = state.customerName
        binding.textScanAndPay.text = getString(R.string.t_002_add_payment_scan_pay, state.customerName)
        setCustomerBalance(state.balanceDue)
        setCustomerProfilePic(state.customerName, state.customerProfile)
        setInfoMessage(state)
        setQrLocked(state)
    }

    private fun setQrLocked(state: State) {
        if (state.showQrLocked) {
            binding.imageQrIntent.setImageDrawable(getDrawableCompact(R.drawable.kyc_qr_restricted))
            binding.root.getConstraintSet(R.id.start).setVisibility(R.id.layoutQrLocked, View.GONE)
            binding.root.getConstraintSet(R.id.end).setVisibility(R.id.layoutQrLocked, View.VISIBLE)
            binding.root.getConstraintSet(R.id.start).setVisibility(R.id.imageProfile, View.GONE)
            binding.root.getConstraintSet(R.id.start).setVisibility(R.id.imageProfile, View.GONE)
            binding.root.setViewVisibility(R.id.imageProfile, View.VISIBLE)
            binding.root.setViewVisibility(R.id.imageQrPerson, View.GONE)
        } else {
            state.qrIntent?.let { setQrCode(it) }
            binding.root.setViewVisibility(R.id.layoutQrLocked, View.GONE)
            binding.root.setViewVisibility(R.id.imageQrPerson, View.VISIBLE)
            binding.root.setViewVisibility(R.id.imageProfile, View.VISIBLE)

            val defaultPic = TextDrawableUtils.getRoundTextDrawable(state.customerName)
            GlideApp.with(this@AddCustomerPaymentActivity)
                .load(state.customerProfile)
                .placeholder(defaultPic)
                .circleCrop()
                .error(defaultPic)
                .fallback(defaultPic)
                .into(binding.imageQrPerson)
        }
    }

    private fun setInfoMessage(state: State) {
        if (state.kycStatus == KycStatus.FAILED || state.kycStatus == KycStatus.PENDING || state.kycLimitReached) {
            binding.kycStatusView.setData(
                kycStatus = state.kycStatus.value,
                kycRiskCategory = state.kycRiskCategory.value,
                isLimitReached = state.kycLimitReached,
                canShowBorder = true
            )
            binding.kycStatusView.hideCloseButton()
            binding.root.getConstraintSet(R.id.start).setVisibility(R.id.kycStatusView, View.GONE)
            binding.root.getConstraintSet(R.id.end).setVisibility(R.id.kycStatusView, View.VISIBLE)
            binding.root.getConstraintSet(R.id.start).setVisibility(R.id.textInfo, View.GONE)
            binding.root.getConstraintSet(R.id.end).setVisibility(R.id.textInfo, View.GONE)
        } else {
            binding.root.getConstraintSet(R.id.start).setVisibility(R.id.kycStatusView, View.GONE)
            binding.root.getConstraintSet(R.id.end).setVisibility(R.id.kycStatusView, View.GONE)
            binding.root.getConstraintSet(R.id.start).setVisibility(R.id.textInfo, View.GONE)
            binding.root.getConstraintSet(R.id.end).setVisibility(R.id.textInfo, View.VISIBLE)
            binding.textInfo.text = getString(R.string.t_002_add_payment_education, state.customerName)
        }
    }

    private fun setQrCode(qrIntent: String) {
        QRCodeUtils.getBitmapObservable(qrIntent, this, 205).subscribe {
            binding.imageQrIntent.setImageBitmap(it)
        }.addTo(autoDisposable)
    }

    private fun setCustomerBalance(balance: Long) {
        binding.textBalance.text = when {
            balance < 0 -> {
                buildSpannedString {
                    color(getColorCompat(R.color.tx_credit)) {
                        append(String.format("₹%s", CurrencyUtil.formatV2(balance)))
                    }
                    append(" ")
                    append(getString(R.string.due))
                }
            }
            else -> {
                buildSpannedString {
                    color(getColorCompat(R.color.tx_payment)) {
                        append(String.format("₹%s", CurrencyUtil.formatV2(balance)))
                    }
                    append(" ")
                    append(getString(R.string.advance))
                }
            }
        }
    }

    private fun setCustomerProfilePic(name: String, profileImage: String?) {
        if (name.isEmpty()) return

        val defaultPic = TextDrawableUtils.getRoundTextDrawable(name)

        GlideApp.with(this@AddCustomerPaymentActivity)
            .load(profileImage)
            .placeholder(defaultPic)
            .circleCrop()
            .error(defaultPic)
            .fallback(defaultPic)
            .thumbnail(0.25f)
            .into(binding.imageProfile)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val navHostFragment: AddTransactionFragment? =
            supportFragmentManager.primaryNavigationFragment as AddTransactionFragment?
        navHostFragment?.childFragmentManager?.primaryNavigationFragment?.onActivityResult(
            requestCode,
            resultCode,
            data
        )
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.ExpandQr -> binding.root.progress = 1.0f
            is ViewEvent.ShowError -> longToast(event.error)
            is ViewEvent.ShowPaymentReceived -> showPaymentSuccess(event)
        }
    }

    private fun showPaymentSuccess(event: ViewEvent.ShowPaymentReceived) {
        startActivity(
            PaymentSuccessActivity.getIntent(
                context = this,
                collectionId = event.collectionId,
                amount = event.amount,
                customerName = getCurrentState().customerName,
                paymentTime = event.createTime,
                balance = getCurrentState().balanceDue,
            )
        )
        finish()
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun onAccountAddedSuccessfully(eta: Long) {
    }

    override fun onCancelled() {
    }

    override fun onAmountFocusChange(hasFocus: Boolean) {
        if (hasFocus && binding.root.currentState == R.id.end) {
            binding.root.transitionToStart()
        }
    }

    companion object {

        fun getIntent(
            context: Context,
            customerId: String,
            source: String = AddTxnContainerActivity.Source.CUSTOMER_SCREEN.value,
            expandedQr: Boolean = false,
        ) = android.content.Intent(context, AddCustomerPaymentActivity::class.java).apply {
            putExtra(AddTxnContainerActivity.CUSTOMER_ID, customerId)
            putExtra(AddTxnContainerActivity.TRANSACTION_TYPE, PAYMENT)
            putExtra(AddTxnContainerActivity.SOURCE, source)
            putExtra(EXTRA_EXPANDED_QR, expandedQr)
        }

        const val EXTRA_EXPANDED_QR = "extra_expanded_qr"
    }
}
