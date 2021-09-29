package `in`.okcredit.frontend.ui._dialogs

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.setQrCode
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.QrCodeBuilder
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.databinding.LinkPayBottomSheetDialogBinding
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import kotlinx.android.parcel.Parcelize
import timber.log.Timber

class LinkPayBottomSheetPaymentDialog : ExpandedBottomSheetDialogFragment() {
    private var listener: CustomerListener? = null
    private lateinit var mCustomer: Customer
    private var merchantPaymentAddress: String? = null
    private lateinit var mCollectionCustomerProfile: CollectionCustomerProfile
    private lateinit var binding: LinkPayBottomSheetDialogBinding

    interface CustomerListener {
        fun onSendReminderClicked(
            customer: Customer,
            collectionCustomerProfile: CollectionCustomerProfile
        )

        fun onDismiss()
    }

    @Parcelize
    data class LinkPayReminderDialogData(
        val customer: Customer,
        val collectionCustomerProfile: CollectionCustomerProfile,
        val merchantPaymentAddress: String?
    ) : Parcelable

    companion object {
        const val PAYMENT_REMINDER_DIALOG_DATA = "payment_reminder_dialog_data"
        const val TAG = "LinkPayBottomSheetPaymentDialog"

        fun newInstance(linkPayReminderDialogData: LinkPayReminderDialogData): LinkPayBottomSheetPaymentDialog {

            val bundle = Bundle().apply {
                putParcelable(PAYMENT_REMINDER_DIALOG_DATA, linkPayReminderDialogData)
            }

            return LinkPayBottomSheetPaymentDialog().apply {
                arguments = bundle
            }
        }
    }

    fun initialize(listener: CustomerListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val paymentDialogData = arguments?.getParcelable<LinkPayReminderDialogData>(PAYMENT_REMINDER_DIALOG_DATA)
            ?: throw Exception("PaymentDialogData in LinkPayBottomSheetPaymentDialog can't be null")

        mCustomer = paymentDialogData.customer
        mCollectionCustomerProfile = paymentDialogData.collectionCustomerProfile
        merchantPaymentAddress = paymentDialogData.merchantPaymentAddress
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = LinkPayBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        render(mCustomer, mCollectionCustomerProfile)
    }

    fun render(
        customer: Customer,
        collectionCustomerProfile: CollectionCustomerProfile
    ) {
        Timber.e("<<<LinkPayBottomSheetPaymentDialog render")

        binding.name.text = context!!.getString(R.string.link_pay)
        binding.tvPaymentAddress.text = merchantPaymentAddress
        binding.llPaymentAddress.visibility = if (merchantPaymentAddress.isNullOrBlank()) View.GONE else View.VISIBLE

        val deviceWidth = context!!.resources.displayMetrics.widthPixels

        val layoutParams = binding.image.layoutParams as RelativeLayout.LayoutParams
        val qrImgMargins = layoutParams.marginStart + layoutParams.marginEnd

        val qrIntent = QrCodeBuilder.getQrCode(
            qrIntent = collectionCustomerProfile.qr_intent,
            currentBalance = customer.balanceV2,
            lastPayment = customer.lastPayment,
        )
        binding.image.setQrCode(qrIntent, context!!, (deviceWidth - qrImgMargins))
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onDismiss()
    }
}
