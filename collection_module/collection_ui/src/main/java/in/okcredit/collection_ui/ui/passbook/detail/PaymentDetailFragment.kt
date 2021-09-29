package `in`.okcredit.collection_ui.ui.passbook.detail

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection.contract.OnlinePaymentErrorCode
import `in`.okcredit.collection.contract.PayoutType
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.OnlineCollectionTracker
import `in`.okcredit.collection_ui.analytics.OnlineCollectionTracker.Screen.collectionPaymentTransaction
import `in`.okcredit.collection_ui.databinding.PaymentDetailFragmentBinding
import `in`.okcredit.collection_ui.ui.home.add.AddMerchantDestinationDialog
import `in`.okcredit.collection_ui.ui.passbook.add_to_khata.AddToKhataDialog
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsContract
import `in`.okcredit.collection_ui.ui.passbook.payments.views.OnlinePaymentsView
import `in`.okcredit.collection_ui.ui.passbook.refund.RefundConsentBottomSheet
import `in`.okcredit.communication_inappnotification.contract.LocalInAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.ui.local.TooltipLocal
import `in`.okcredit.home.HomeNavigator
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.lifecycleScope
import com.jakewharton.rxbinding3.view.clicks
import com.skydoves.balloon.ArrowOrientation
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.utils.DateTimeUtils
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToLong

class PaymentDetailFragment :
    BaseFragment<PaymentDetailContract.State, PaymentDetailContract.ViewEvents, PaymentDetailContract.Intent>(
        "PaymentDetailScreen",
        R.layout.payment_detail_fragment
    ) {
    private val binding: PaymentDetailFragmentBinding by viewLifecycleScoped(PaymentDetailFragmentBinding::bind)

    @Inject
    lateinit var homeNavigator: HomeNavigator

    @Inject
    lateinit var onlineCollectionTracker: OnlineCollectionTracker

    @Inject
    lateinit var localInAppNotificationHandler: Lazy<LocalInAppNotificationHandler>

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            binding.btnShare.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    PaymentDetailContract.Intent.SendWhatsApp(getShareBitmap())
                },
            binding.btnShareSolid.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    PaymentDetailContract.Intent.SendWhatsApp(getShareBitmap())
                },
            binding.tvContactUs.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    PaymentDetailContract.Intent.OpenWhatsAppForHelp
                },
            binding.tvAdd.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    PaymentDetailContract.Intent.ShowAddMerchantDestinationDialog
                },
            binding.ivAlert.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    PaymentDetailContract.Intent.ShowInvalidAddressToolTip
                },
            binding.tvRefundToCustomer.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    PaymentDetailContract.Intent.ShowRefundConsentBottomSheet
                }
        )
    }

    override fun loadIntent(): UserIntent? {
        return PaymentDetailContract.Intent.Load
    }

    private fun getShareBitmap(): Bitmap {
        val view = binding.detailContainer
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.layout(0, 0, view.width, view.height)
        val drawable = view.background
        if (drawable == null) {
            canvas.drawColor(Color.WHITE)
        } else {
            drawable.draw(canvas)
        }
        view.draw(canvas)
        return bitmap
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.disableScreanCapture()
        binding.btnAddToKhata.setOnClickListener {
            onlineCollectionTracker.trackClickAddToKhata(
                collectionPaymentTransaction,
                getCurrentState().collectionOnlinePayment?.id ?: "",
                getCurrentState().source
            )
            homeNavigator.goToHomeSearchScreenForResult(this, HOME_SEARCH_REQUEST_CODE, true)
        }

        binding.rootView.setTracker(performanceTracker)
    }

    override fun render(state: PaymentDetailContract.State) {
        state.collectionOnlinePayment?.let { collectionOnlinePayment ->
            setAmount(collectionOnlinePayment.amount)
            setTransactionId(collectionOnlinePayment.paymentId)
            setToAddress(collectionOnlinePayment.paymentSource ?: "")
            paymentStatus(collectionOnlinePayment, state.merchantPaymentAddress)
            setAddedDate(collectionOnlinePayment.createdTime)
            setUpdatedDate(collectionOnlinePayment.updatedTime)
            setType(collectionOnlinePayment.type)
        }
    }

    private fun setAmount(amount: Double?) {
        amount?.let { binding.amount.text = CurrencyUtil.formatV2(it.roundToLong()) }
    }

    private fun setTransactionId(id: String) {
        binding.txnId.text = id
    }

    private fun setToAddress(upi: String) {
        if (upi.isNotEmpty()) {
            binding.upiIdToTitle.text = upi
        } else {
            binding.grpPaidBy.gone()
        }
    }

    private fun paymentStatus(collectionOnlinePayment: CollectionOnlinePayment, merchantPaymentAddress: String) {
        when (collectionOnlinePayment.status) {
            OnlinePaymentsContract.PaymentStatus.REFUNDED.value -> setStatusRefunded()
            OnlinePaymentsContract.PaymentStatus.FAILED.value -> setStatusFailed()
            OnlinePaymentsContract.PaymentStatus.COMPLETE.value -> setStatusSuccess(collectionOnlinePayment.accountId.isNotEmpty())
            OnlinePaymentsContract.PaymentStatus.REFUND_INITIATED.value -> setStatusRefundInitiated()
            OnlinePaymentsContract.PaymentStatus.PAYOUT_FAILED.value -> setPayoutFailedUi(
                collectionOnlinePayment,
                merchantPaymentAddress
            )
            else -> setStatusProcessing(merchantPaymentAddress)
        }
        showAddToKhata(
            collectionOnlinePayment.status == OnlinePaymentsContract.PaymentStatus.COMPLETE.value &&
                collectionOnlinePayment.accountId.isEmpty() &&
                collectionOnlinePayment.type != OnlinePaymentsView.TYPE_SUPPLIER_COLLECTION
        )
    }

    private fun showAddToKhata(canShow: Boolean) {
        binding.btnAddToKhata.isVisible = (canShow)
        binding.btnShare.isVisible = (canShow)
        binding.shareContainer.isVisible = (canShow)
        binding.shareDivider.isVisible = (canShow)
    }

    private fun setStatusRefunded() {
        binding.apply {
            btnShareSolid.visible()
            rupeeSymbol.setTextColor(getColorCompat(R.color.grey900))
            amount.setTextColor(getColorCompat(R.color.grey900))
            type.setTextColor(getColorCompat(R.color.grey900))
            vwAmountDivider.visible()
            ivRefundSuccess.visible()
            ivRefunded.visible()
            grpContactUs.visible()
            grpRefundToCustomer.gone()
            tvStatus.visible()
            grpPaidTo.gone()
            ivAlert.gone()
            tvAdd.gone()

            amountContainer.setBackgroundColor(getColorCompat(R.color.white))

            tvPaymentStatus.text = getString(R.string.refund_successful)
            tvPaymentStatus.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                0,
                0
            )
            paymentStatusImg.setImageDrawable(
                getDrawableCompact(R.drawable.ic_icon_refund)
            )
            paymentStatusImg.imageTintList = getColorStateListCompat(R.color.pending_yellow)

            tvStatus.text = getString(R.string.refund_successful)
            tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_refund,
                0,
                0,
                0
            )

            TextViewCompat.setCompoundDrawableTintList(
                tvStatus,
                ColorStateList.valueOf(getColorCompat(R.color.pending_yellow))
            )
        }
    }

    private fun setStatusFailed() {
        binding.apply {
            tvPaymentStatus.text = getString(R.string.payment_failed)
            paymentStatusImg.setImageDrawable(getDrawableCompact(R.drawable.ic_error))

            paymentStatusImg.imageTintList = null
            amountContainer.setBackgroundColor(getColorCompat(R.color.white))
            ivRefunded.gone()
            grpContactUs.gone()
            grpPaymentStatusReason.gone()
            tvAdd.gone()
            tvStatus.gone()
            ivRefunded.gone()
            grpPaidTo.gone()
            grpRefundToCustomer.gone()

            rupeeSymbol.setTextColor(getColorCompat(R.color.green_primary))
            amount.setTextColor(getColorCompat(R.color.green_primary))
            type.setTextColor(getColorCompat(R.color.green_primary))
        }
    }

    private fun setStatusSuccess(isTagged: Boolean) {
        binding.apply {
            tvPaymentStatus.text = getString(R.string.payment_title_success)
            paymentStatusImg.setImageDrawable(
                getDrawableCompact(R.drawable.ic_success_green)
            )
            paymentStatusImg.imageTintList = null
            btnShareSolid.isVisible = (isTagged)

            amountContainer.setBackgroundColor(getColorCompat(R.color.white))
            ivRefunded.gone()
            grpContactUs.gone()
            grpPaymentStatusReason.gone()
            tvAdd.gone()
            tvStatus.gone()
            ivRefunded.gone()
            grpPaidTo.gone()
            grpRefundToCustomer.gone()

            rupeeSymbol.setTextColor(getColorCompat(R.color.green_primary))
            amount.setTextColor(getColorCompat(R.color.green_primary))
            type.setTextColor(getColorCompat(R.color.green_primary))
        }
    }

    private fun setPayoutFailedUi(collectionOnlinePayment: CollectionOnlinePayment, paymentAddress: String) {
        when (collectionOnlinePayment.errorCode) {
            OnlinePaymentErrorCode.EP001.value -> {
                setStatusPayoutFailedBaseUi(paymentAddress)
            }
            OnlinePaymentErrorCode.EP002.value -> {
                setStatusProcessing(paymentAddress)
                binding.grpRefundToCustomer.visible()
                binding.tvPaymentStatusReason.text = getString(R.string.settlement_blocked_due_to_bank_offline)
            }
            OnlinePaymentErrorCode.EP004.value -> {
                setStatusProcessing(paymentAddress)
                binding.grpRefundToCustomer.visible()
                binding.tvPaymentStatusReason.text =
                    getString(R.string.settlement_blocked_due_to_bank_issue_try_after_24_hrs)
            }
            else -> {
                setStatusProcessing(paymentAddress)
                binding.tvPaymentStatusReason.text = getString(R.string.payment_refund_reason_payout_initiated)
            }
        }
    }

    private fun setStatusPayoutFailedBaseUi(paymentAddress: String) {
        binding.apply {

            grpPaymentStatusReason.visible()
            grpContactUs.visible()
            grpRefundToCustomer.visible()
            tvStatus.visible()
            ivRefunded.gone()
            btnShareSolid.visible()

            if (paymentAddress.isNotNullOrBlank()) {
                grpPaidTo.visible()
                tvAdd.visible()
            } else {
                grpPaidTo.gone()
                tvAdd.gone()
            }

            tvPaymentStatus.text = getString(R.string.settlement_blocked)
            paymentStatusImg.setImageDrawable(getDrawableCompact(R.drawable.ic_pending))
            paymentStatusImg.imageTintList = ColorStateList.valueOf(getColorCompat(R.color.pending_red))

            if (paymentAddress.isNotEmpty()) {
                ivAlert.visible()
                tvPaidToAddress.text =
                    if (paymentAddress.length >= 15) paymentAddress.substring(0, 14) + "..." else paymentAddress
                tvPaidToAddress.setTextColor(getColorCompat(R.color.grey900))
            } else {
                ivAlert.gone()
                tvPaidToAddress.text = getString(R.string.add_bank_details)
                tvPaidToAddress.setTextColor(getColorCompat(R.color.grey400))
            }
            tvPaymentStatusReason.text = getString(R.string.settlement_blocked_reason)

            rupeeSymbol.setTextColor(getColorCompat(R.color.grey900))
            amount.setTextColor(getColorCompat(R.color.grey900))
            type.setTextColor(getColorCompat(R.color.grey900))
            tvStatus.text = requireContext().getString(R.string.settlement_blocked)
            tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_pending,
                0,
                0,
                0
            )

            TextViewCompat.setCompoundDrawableTintList(
                tvStatus,
                ColorStateList.valueOf(getColorCompat(R.color.pending_red))
            )

            amountContainer.setBackgroundColor(getColorCompat(R.color.red_lite))

            setPayoutFailedReason()
        }
    }

    private fun setPayoutFailedReason() {
        val content = SpannableStringBuilder(getString(R.string.your_money_is_with_okcredit))
        val addBankDetailsString = SpannableStringBuilder(getString(R.string.add_bank_details))
        addBankDetailsString.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.indigo_primary
                )
            ),
            0, addBankDetailsString.length, 0
        )

        addBankDetailsString.withClickableSpan(0, addBankDetailsString.length) {
            pushIntent(PaymentDetailContract.Intent.ShowAddMerchantDestinationDialog)
        }
        content.append(" ").append(addBankDetailsString).append(" " + getString(R.string.to_receive_money))
        binding.tvPaymentStatusReason.text = content
        binding.tvPaymentStatusReason.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setStatusProcessing(paymentAddress: String) {
        binding.apply {
            btnShareSolid.visible()
            grpPaymentStatusReason.visible()
            tvStatus.visible()
            grpContactUs.visible()
            grpRefundToCustomer.gone()
            ivAlert.gone()
            tvAdd.gone()
            ivRefunded.gone()

            if (paymentAddress.isNotNullOrBlank()) {
                grpPaidTo.visible()
            } else {
                grpPaidTo.gone()
            }

            tvPaymentStatus.text = getString(R.string.settlement_pending)
            paymentStatusImg.setImageDrawable(getDrawableCompact(R.drawable.ic_pending))
            paymentStatusImg.imageTintList = ColorStateList.valueOf(getColorCompat(R.color.pending_yellow))

            tvPaidToAddress.text = paymentAddress
            tvPaymentStatusReason.text = getString(R.string.settlement_blocked_reason)

            rupeeSymbol.setTextColor(getColorCompat(R.color.grey900))
            amount.setTextColor(getColorCompat(R.color.grey900))
            type.setTextColor(getColorCompat(R.color.grey900))
            tvStatus.text = requireContext().getString(R.string.settlement_pending)
            tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_pending,
                0,
                0,
                0
            )

            TextViewCompat.setCompoundDrawableTintList(
                tvStatus,
                ColorStateList.valueOf(getColorCompat(R.color.pending_yellow))
            )

            amountContainer.setBackgroundColor(getColorCompat(R.color.orange_lite))
            tvPaymentStatusReason.text = getString(R.string.payment_refund_reason_payout_initiated)
        }
    }

    private fun setStatusRefundInitiated() {
        binding.apply {
            tvPaymentStatus.text = getString(R.string.refund_initiated)
            paymentStatusImg.setImageDrawable(
                getDrawableCompact(R.drawable.ic_refund)
            )
            paymentStatusImg.imageTintList = getColorStateListCompat(R.color.pending_yellow)

            btnShareSolid.visible()
            grpContactUs.visible()
            grpPaymentStatusReason.visible()
            vwAmountDivider.visible()
            tvStatus.visible()
            ivRefunded.gone()
            grpPaidTo.gone()
            tvAdd.gone()
            grpRefundToCustomer.gone()
            ivAlert.gone()
            tvPaymentStatusReason.text = getString(R.string.payment_refund_initiated)

            rupeeSymbol.setTextColor(getColorCompat(R.color.grey900))
            amount.setTextColor(getColorCompat(R.color.grey900))
            type.setTextColor(getColorCompat(R.color.grey900))

            amountContainer.setBackgroundColor(getColorCompat(R.color.white))

            tvStatus.text = getString(R.string.refund_initiated)
            tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_refund,
                0,
                0,
                0
            )

            TextViewCompat.setCompoundDrawableTintList(
                tvStatus,
                ColorStateList.valueOf(getColorCompat(R.color.pending_yellow))
            )
        }
    }

    private fun setType(type: String) {
        when {
            type.contains(OnlinePaymentsView.TYPE_QR) -> {
                binding.addedByTitle.text = getString(R.string.added_by_qr)
                binding.type.text = context?.getString(R.string.qr_payment)
            }
            type == OnlinePaymentsView.TYPE_CUSTOMER_COLLECTION || type == OnlinePaymentsView.TYPE_SUPPLIER_COLLECTION -> {
                binding.addedByTitle.text = getString(R.string.added_by_online)
                binding.type.text = context?.getString(R.string.online_payment)
            }
            else -> {
                binding.addedByTitle.text = getString(R.string.added_by_link)
                binding.type.text = context?.getString(R.string.link_payment)
            }
        }
    }

    private fun setAddedDate(createdAt: DateTime) {
        binding.createdDate.text = DateTimeUtils.formatLong(createdAt)
    }

    private fun setUpdatedDate(updatedAt: DateTime) {
        binding.tvPaymentDate.text = DateTimeUtils.formatLong(updatedAt)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == HOME_SEARCH_REQUEST_CODE) {
            data?.let {
                val customerId = it.getStringExtra(CUSTOMER_ID)
                goToAddToKhataDialog(customerId)
            }
        }
    }

    private fun goToAddToKhataDialog(customerId: String?) {
        customerId?.let { id ->
            val dialog = AddToKhataDialog.newInstance(getCurrentState().source).apply {
                arguments = bundleOf(PAYMENT_ID to getCurrentState().collectionOnlinePayment?.id, CUSTOMER_ID to id)
            }
            dialog.show(childFragmentManager, AddToKhataDialog::class.java.simpleName)
        }
    }

    override fun handleViewEvent(event: PaymentDetailContract.ViewEvents) {
        when (event) {
            is PaymentDetailContract.ViewEvents.SendWhatsApp -> sendWhatsApp(event.intent)
            is PaymentDetailContract.ViewEvents.ShowError -> context?.shortToast(event.errorMsg)
            is PaymentDetailContract.ViewEvents.OpenWhatsAppForHelp -> startActivity(event.intent)
            PaymentDetailContract.ViewEvents.ShowAddMerchantDestinationDialog -> showAddMerchantDestinationDialog()
            PaymentDetailContract.ViewEvents.ShowInvalidAddressToolTip -> showInvalidAddressAlertToolTip()
            PaymentDetailContract.ViewEvents.ShowRefundConsentBottomSheet -> showRefundConsentBottomSheet()
        }
    }

    private fun sendWhatsApp(intent: Intent) {
        startActivity(intent)
    }

    private fun showAddMerchantDestinationDialog() {
        val paymentDialogFrag = AddMerchantDestinationDialog.newInstance(
            isUpdateCollection = true,
            paymentMethodType = null,
            source = collectionPaymentTransaction
        )
        paymentDialogFrag.show(childFragmentManager, AddMerchantDestinationDialog.TAG)
    }

    private fun showRefundConsentBottomSheet() {
        val dialog = getCurrentState().let { state ->
            state.collectionOnlinePayment?.let {
                RefundConsentBottomSheet.newInstance(
                    it.payoutId ?: "",
                    it.id,
                    it.paymentId,
                    getCurrentState().collectionOnlinePayment?.type ?: "",
                )
            }
        }

        dialog?.show(childFragmentManager, RefundConsentBottomSheet.TAG)
    }

    fun onMerchantDestinationAdded() {
        pushIntent(PaymentDetailContract.Intent.TriggerMerchantPayout(PayoutType.PAYOUT.value))
    }

    private fun showInvalidAddressAlertToolTip() {
        lifecycleScope.launch {
            localInAppNotificationHandler.get()
                .generateTooltip(
                    weakScreen = WeakReference(requireActivity()),
                    tooltip = TooltipLocal(
                        targetView = WeakReference(binding.ivAlert),
                        title = getString(R.string.add_bank_details_tooltip),
                        arrowOrientation = ArrowOrientation.TOP,
                        textSize = 13f,
                        backgroundColor = R.color.grey700,
                        screenName = label,
                        alignTop = false
                    )
                )
        }
    }

    companion object {

        fun getInstance(paymentId: String, customerId: String?, source: String) = PaymentDetailFragment().apply {
            arguments = Bundle().apply {
                putString(PAYMENT_ID, paymentId)
                putString(SOURCE, source)
                customerId?.let { putString(CUSTOMER_ID, it) }
            }
        }

        const val HOME_SEARCH_REQUEST_CODE = 9198
        const val CUSTOMER_ID = "customer_id"
        const val PAYMENT_ID = "payment_id"
        const val SOURCE = "source"
    }
}
