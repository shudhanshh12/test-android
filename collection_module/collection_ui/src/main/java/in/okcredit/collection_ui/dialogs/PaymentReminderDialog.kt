package `in`.okcredit.collection_ui.dialogs

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.collection.contract.QrCodeBuilder
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.PaymentReminderDialogBinding
import `in`.okcredit.collection_ui.utils.setQrCode
import `in`.okcredit.shared.view.KycStatusView
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.payment_reminder_dialog.*
import tech.okcredit.android.base.extensions.*
import timber.log.Timber

class PaymentReminderDialog : DialogFragment() {

    internal val binding: PaymentReminderDialogBinding by viewLifecycleScoped(PaymentReminderDialogBinding::bind)

    internal var listener: CustomerListener? = null
    private lateinit var mCustomer: Customer
    private lateinit var mCollectionCustomerProfile: CollectionCustomerProfile

    private lateinit var kycStatus: KycStatus
    private lateinit var kycRiskCategory: KycRiskCategory
    private var isKycLimitReached: Boolean = false

    interface CustomerListener {
        fun onSendReminderClicked(
            customer: Customer,
            collectionCustomerProfile: CollectionCustomerProfile,
        )

        fun callIconClicked(customer: Customer)

        fun addMobileClicked(customer: Customer)

        fun onDismiss()

        fun onDismissKyc(eventName: String)

        fun onStartKyc(eventName: String)
    }

    @Parcelize
    data class PaymentReminderDialogData(
        val customer: Customer,
        val collectionCustomerProfile: CollectionCustomerProfile,
        val kycStatus: KycStatus,
        val kycRiskCategory: KycRiskCategory,
        val isLimitReached: Boolean,
    ) : Parcelable

    companion object {
        const val PAYMENT_REMINDER_DIALOG_DATA = "payment_reminder_dialog_data"
        const val TAG = "PaymentReminderDialog"

        fun newInstance(paymentReminderDialogData: PaymentReminderDialogData): PaymentReminderDialog {

            val bundle = Bundle().apply {
                putParcelable(PAYMENT_REMINDER_DIALOG_DATA, paymentReminderDialogData)
            }
            return PaymentReminderDialog().apply {
                arguments = bundle
            }
        }
    }

    fun initialize(listener: CustomerListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val paymentDialogData = arguments?.getParcelable<PaymentReminderDialogData>(PAYMENT_REMINDER_DIALOG_DATA)
            ?: throw Exception("PaymentDialogData in PaymentReminderDialog can't be null")

        mCustomer = paymentDialogData.customer
        mCollectionCustomerProfile = paymentDialogData.collectionCustomerProfile
        kycStatus = paymentDialogData.kycStatus
        kycRiskCategory = paymentDialogData.kycRiskCategory
        isKycLimitReached = paymentDialogData.isLimitReached
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return PaymentReminderDialogBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        render(mCustomer, mCollectionCustomerProfile)
    }

    private fun initListeners(customer: Customer, collectionCustomerProfile: CollectionCustomerProfile) {

        binding.tvSendReminder.setOnClickListener {
            if (whatsAppNotInstalledForLinkPay(customer)) return@setOnClickListener

            if (canSendReminder(customer)) {
                if (isAccountInAdvance(customer)) return@setOnClickListener
                listener?.onSendReminderClicked(customer, collectionCustomerProfile)
            } else {
                listener?.addMobileClicked(customer)
            }

            dismiss()
        }

        binding.callBtn.setOnClickListener {
            dismiss()
            listener?.callIconClicked(customer)
        }

        binding.kycStatusView.setListener(object : KycStatusView.Listener {
            override fun onBannerDisplayed(bannerType: String) {
                // do nothing
            }

            override fun onStartKyc(eventName: String) {
                listener?.onStartKyc(eventName)
                dismiss()
            }

            override fun onClose(eventName: String) {
                binding.kycStatusView.gone()
                listener?.onDismissKyc(eventName)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val deviceWidth = resources.displayMetrics.widthPixels
        dialog?.window?.setLayout(deviceWidth, ConstraintLayout.LayoutParams.WRAP_CONTENT)
    }

    fun render(
        customer: Customer,
        collectionCustomerProfile: CollectionCustomerProfile,
    ) {
        Timber.e("<<<PaymentReminderDialog render")

        binding.callBtn.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey600))

        binding.name.text = if (customer.isLiveSales) getString(R.string.link_pay) else customer.description

        when {
            customer.isLiveSales -> {
                binding.callBtn.visibility = View.GONE
                binding.tvSendReminder.text = getString(R.string.share_payment_link)
            }
            customer.mobile != null -> {
                binding.callBtn.visibility = View.VISIBLE
                binding.tvSendReminder.text = getString(R.string.send_reminder)
            }
            else -> {
                binding.tvSendReminder.visibility = View.VISIBLE
                binding.tvSendReminder.text = getString(R.string.add_number)
                binding.callBtn.visibility = View.GONE
            }
        }

        binding.tvQrDescription.text = if (customer.isLiveSales) {
            getString(R.string.ask_customer_to_pay_online)
        } else {
            getString(R.string.ask_customer_to_scan_qr_and_pay_online)
        }

        showQrCode(customer, collectionCustomerProfile)
        showKyc()
        initListeners(customer, collectionCustomerProfile)
        checkIfBlockedCustomer(customer)
    }

    private fun checkIfBlockedCustomer(customer: Customer) {
        if (customer.state == Customer.State.BLOCKED)
            disableSendReminder()
    }

    private fun disableSendReminder() {
        binding.tvSendReminder.disable()
    }

    private fun showQrCode(customer: Customer, collectionCustomerProfile: CollectionCustomerProfile) {
        val deviceWidth = requireContext().resources.displayMetrics.widthPixels

        val layoutParams = binding.image.layoutParams as RelativeLayout.LayoutParams
        val qrImgMargins = layoutParams.marginStart + layoutParams.marginEnd

        val qrIntent = QrCodeBuilder.getQrCode(
            qrIntent = collectionCustomerProfile.qr_intent,
            currentBalance = customer.balanceV2,
            lastPayment = customer.lastPayment,
        )

        if (isKycLimitReached.not() && qrIntent.isNotNullOrBlank()) {
            binding.image.setQrCode(qrIntent, requireContext(), (deviceWidth - qrImgMargins))
        } else {
            binding.image.gone()
            binding.tvQrDescription.gone()
        }
    }

    private fun showKyc() {
        if (kycRiskCategory != KycRiskCategory.NO_RISK || isKycLimitReached) {
            binding.kycStatusView.visible()
            binding.kycStatusView.setData(
                kycStatus = kycStatus.value,
                kycRiskCategory = kycRiskCategory.value,
                isLimitReached = isKycLimitReached
            )
        } else {
            binding.kycStatusView.gone()
        }
    }

    private fun whatsAppNotInstalledForLinkPay(customer: Customer): Boolean {
        if (customer.isLiveSales && !isWhatsAppOrWhatsAppBusinessInstalled()) {
            longToast(R.string.whatsapp_not_installed)
            return true
        }
        return false
    }

    private fun isAccountInAdvance(customer: Customer): Boolean {
        if (customer.isLiveSales.not() && customer.balanceV2 >= 0) {
            longToast(R.string.balance_in_advance_no_reminder)
            return true
        }
        return false
    }

    private fun canSendReminder(customer: Customer) =
        customer.mobile != null || customer.isLiveSales

    private fun isWhatsAppOrWhatsAppBusinessInstalled() =
        requireContext().isAppPackageInstalled("com.whatsapp") || requireContext().isAppPackageInstalled("com.whatsapp.w4b")

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onDismiss()
    }
}
