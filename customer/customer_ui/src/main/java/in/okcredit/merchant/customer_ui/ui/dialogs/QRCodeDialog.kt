package `in`.okcredit.merchant.customer_ui.ui.dialogs

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.setQrCode
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.collection.contract.QrCodeBuilder
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.shared.view.KycStatusView
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.qr_code_dialog.*
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible

class QRCodeDialog : DialogFragment() {

    private lateinit var mCustomer: Customer
    private lateinit var mCollectionCustomerProfile: CollectionCustomerProfile
    private lateinit var mKycStatus: KycStatus
    private lateinit var mKycRiskCategory: KycRiskCategory
    private var mIsKycLimitReached: Boolean = false

    internal var listener: QrCodeDismissListener? = null

    interface QrCodeDismissListener {
        fun onDismiss(eventName: String? = null)
        fun onStartKyc(eventName: String)
        fun onKycBannerDisplayed(bannerType: String)
    }

    @Parcelize
    data class QRCodeDialogData(
        val customer: Customer,
        val collectionCustomerProfile: CollectionCustomerProfile,
        val kycStatus: KycStatus,
        val kycRiskCategory: KycRiskCategory,
        val isKycLimitReached: Boolean
    ) : Parcelable

    companion object {
        const val QR_CODE_DIALOG_DATA = "qr_code_dialog_data"
        const val TAG = "QRCodeDialog"

        fun newInstance(qrCodeDialogData: QRCodeDialogData): QRCodeDialog {

            val bundle = Bundle()
            bundle.putParcelable(QR_CODE_DIALOG_DATA, qrCodeDialogData)

            val qrCodeDialog = QRCodeDialog()
            qrCodeDialog.arguments = bundle

            return qrCodeDialog
        }
    }

    fun initialize(listener: QrCodeDismissListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val paymentDialogData = arguments?.getParcelable<QRCodeDialogData>(QR_CODE_DIALOG_DATA)
            ?: throw Exception("QRCodeDialogData in QRCodeDialog can't be null")

        mCustomer = paymentDialogData.customer
        mCollectionCustomerProfile = paymentDialogData.collectionCustomerProfile
        mKycStatus = paymentDialogData.kycStatus
        mKycRiskCategory = paymentDialogData.kycRiskCategory
        mIsKycLimitReached = paymentDialogData.isKycLimitReached
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.qr_code_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        render(mCustomer, mCollectionCustomerProfile)
    }

    override fun onStart() {
        super.onStart()
        val deviceWidth = resources.displayMetrics.widthPixels
        dialog?.window?.setLayout(deviceWidth, RelativeLayout.LayoutParams.WRAP_CONTENT)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onDismiss()
    }

    fun render(
        customer: Customer,
        collectionCustomerProfile: CollectionCustomerProfile
    ) {

        name.text = customer.description

        val deviceWidth = requireContext().resources.displayMetrics.widthPixels
        val layoutParams = image.layoutParams as RelativeLayout.LayoutParams
        val qrImgMargins = layoutParams.marginStart + layoutParams.marginEnd

        if (mIsKycLimitReached.not()) {
            val qrIntent = QrCodeBuilder.getQrCode(
                qrIntent = collectionCustomerProfile.qr_intent,
                currentBalance = customer.balanceV2,
                lastPayment = customer.lastPayment,
            )
            image.setQrCode(qrIntent, requireContext(), (deviceWidth - qrImgMargins))
            image.visible()
        } else {
            image.gone()
            name.gone()
            tv_qr_description.gone()
        }
        showKyc()
    }

    private fun showKyc() {
        if (mKycStatus == KycStatus.PENDING || mKycStatus == KycStatus.FAILED || mIsKycLimitReached) {
            kyc_status_view.setData(mKycStatus.value, mKycRiskCategory.value, mIsKycLimitReached, true)
            kyc_status_view.setListener(object : KycStatusView.Listener {
                override fun onBannerDisplayed(bannerType: String) {
                    listener?.onKycBannerDisplayed(bannerType)
                }

                override fun onStartKyc(eventName: String) {
                    listener?.onStartKyc(eventName)
                }

                override fun onClose(eventName: String) {
                    if (image.isVisible.not()) {
                        dismiss()
                    }
                    listener?.onDismiss(eventName)
                }
            })
            kyc_status_view.visible()
        } else {
            kyc_status_view.gone()
        }
    }
}
