package `in`.okcredit.collection_ui.ui.passbook.payments.views

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection.contract.OnlinePaymentErrorCode
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.OnlinePaymentsViewBinding
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsContract
import `in`.okcredit.collection_ui.usecase.GetOnlinePayments
import `in`.okcredit.fileupload._id.GlideApp
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.addRipple
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.extensions.getDrawableCompact
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.DimensionUtil

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class OnlinePaymentsView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    companion object {
        const val TYPE_QR = "qr"
        const val TYPE_CUSTOMER_COLLECTION = "customer_collection"
        const val TYPE_SUPPLIER_COLLECTION = "supplier_collection"
    }

    private val binding: OnlinePaymentsViewBinding =
        OnlinePaymentsViewBinding.inflate(LayoutInflater.from(context), this)

    lateinit var collectionOnlinePayment: CollectionOnlinePayment

    private var listener: Listener? = null

    interface Listener {
        fun clickedOnlinePaymentsView(collectionOnlinePayment: CollectionOnlinePayment)
        fun addToKhata(paymentId: String)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        listener = null
    }

    init {
        addRipple()
        val horizontal = DimensionUtil.dp2px(context, 16.0f).toInt()
        val vertical = DimensionUtil.dp2px(context, 12.0f).toInt()
        setPadding(horizontal, vertical, horizontal, 0)

        binding.root.setOnClickListener { listener?.clickedOnlinePaymentsView(collectionOnlinePayment) }
        binding.addToKhata.setOnClickListener { listener?.addToKhata(collectionOnlinePayment.id) }
    }

    @ModelProp
    fun setOnlineCollection(onlineCollectionData: GetOnlinePayments.OnlineCollectionData) {
        this.collectionOnlinePayment = onlineCollectionData.collectionOnlinePayment
        setType(collectionOnlinePayment.type)
        setDate(collectionOnlinePayment.createdTime)
        setAmount(collectionOnlinePayment.amount, collectionOnlinePayment.type)
        setStatus(collectionOnlinePayment.status, onlineCollectionData)
        setInfo(onlineCollectionData)
    }

    private fun setType(type: String) {
        when {
            type.contains(TYPE_QR) -> {
                binding.paymentTypeTv.text = context?.getString(R.string.qr_payment)
                binding.paymentTypeImg.setImageDrawable(context.getDrawableCompact(R.drawable.ic_qr_code))
            }
            type == TYPE_CUSTOMER_COLLECTION || type == TYPE_SUPPLIER_COLLECTION -> {
                binding.paymentTypeTv.text = context?.getString(R.string.online_payment)
                binding.paymentTypeImg.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_collection_icon
                    )
                )
            }
            else -> {
                binding.paymentTypeTv.text = context?.getString(R.string.link_payment)
                binding.paymentTypeImg.setImageDrawable(context.getDrawableCompact(R.drawable.ic_icon_link_pay))
            }
        }
    }

    private fun setDate(dateTime: DateTime) {
        binding.date.text = DateTimeUtils.getFormat5(dateTime)
    }

    private fun setAmount(amount: Double, type: String) {
        binding.amount.text = context?.getString(R.string.rupees, CurrencyUtil.formatV2(amount.toLong()))
        if (type == TYPE_SUPPLIER_COLLECTION) {
            binding.imageArrow.setImageDrawable(context.getDrawableCompact(R.drawable.ic_give))
        } else {
            binding.imageArrow.setImageDrawable(context.getDrawableCompact(R.drawable.ic_take))
        }
    }

    private fun setAddToKhata(isTagged: Boolean, isComplete: Boolean, isSupplierPayment: Boolean) {
        if (isSupplierPayment || !isComplete) {
            binding.addToKhata.gone()
            binding.addedInKhata.gone()
            return
        }

        if (isTagged.not()) {
            binding.addToKhata.visible()
            binding.addedInKhata.gone()
        } else {
            binding.addToKhata.gone()
            binding.addedInKhata.visible()
        }
    }

    private fun setStatus(
        statusCode: Int,
        onlineCollectionData: GetOnlinePayments.OnlineCollectionData,
    ) {
        when (statusCode) {
            OnlinePaymentsContract.PaymentStatus.COMPLETE.value -> {
                binding.ivRefundSuccess.gone()
                binding.statusRefunded.gone()
                binding.statusSettlementPending.gone()
            }
            OnlinePaymentsContract.PaymentStatus.REFUNDED.value -> {
                binding.ivRefundSuccess.visible()
                binding.statusRefunded.visible()
                binding.statusRefunded.text = context.getText(R.string.refund_successful)
                binding.statusSettlementPending.gone()
            }
            OnlinePaymentsContract.PaymentStatus.REFUND_INITIATED.value -> {
                binding.ivRefundSuccess.gone()
                binding.statusRefunded.visible()
                binding.statusRefunded.text = context.getText(R.string.refund_initiated)
                binding.statusSettlementPending.gone()
            }
            else -> {
                binding.ivRefundSuccess.gone()
                binding.statusRefunded.gone()
                binding.statusSettlementPending.visible()
                if (statusCode == OnlinePaymentsContract.PaymentStatus.PAYOUT_FAILED.value) {
                    when (onlineCollectionData.collectionOnlinePayment.errorCode) {
                        OnlinePaymentErrorCode.EP001.value -> setSettleBlockedMessage()
                        OnlinePaymentErrorCode.EP002.value,
                        OnlinePaymentErrorCode.EP004.value,
                        -> setSettlePendingMessage()
                        else -> setSettlePendingMessage()
                    }
                } else {
                    setSettlePendingMessage()
                }
            }
        }
        setAddToKhata(
            onlineCollectionData.customer != null,
            statusCode == OnlinePaymentsContract.PaymentStatus.COMPLETE.value,
            onlineCollectionData.collectionOnlinePayment.type == TYPE_SUPPLIER_COLLECTION,
        )
    }

    private fun setSettleBlockedMessage() {
        binding.statusSettlementPending.text = context.getString(R.string.settlement_blocked)
        TextViewCompat.setCompoundDrawableTintList(
            binding.statusSettlementPending,
            ColorStateList.valueOf(context.getColorCompat(R.color.pending_red))
        )
    }

    private fun setSettlePendingMessage() {
        binding.statusSettlementPending.text = context.getString(R.string.settlement_pending)
        TextViewCompat.setCompoundDrawableTintList(
            binding.statusSettlementPending,
            ColorStateList.valueOf(context.getColorCompat(R.color.pending_yellow))
        )
    }

    private fun setInfo(onlineCollectionData: GetOnlinePayments.OnlineCollectionData) {
        if (onlineCollectionData.customer != null) {
            setCustomerInfo(onlineCollectionData.customer)
        } else {
            setUpiInfo(onlineCollectionData.collectionOnlinePayment.paymentSource)
        }
    }

    private fun setCustomerInfo(customer: Customer) {
        binding.customerName.text = customer.description ?: ""
        setProfilePic(customer.description, customer.profileImage)
    }

    private fun setUpiInfo(upiId: String?) {
        binding.customerName.text = upiId ?: ""
        setProfilePic(upiId)
    }

    private fun setProfilePic(name: String?, profilepic: String? = null) {
        name?.let {
            var text = if (it.length > 1) it.substring(0, 1) else it
            text = if (text.isEmpty() || text.toCharArray()[0].isDigit()) {
                "+"
            } else {
                text.uppercase()
            }
            val defaultPic = TextDrawable
                .builder()
                .buildRound(
                    text,
                    ColorGenerator.MATERIAL.getColor(it)
                )
            if (profilepic != null) {
                GlideApp
                    .with(context)
                    .load(profilepic)
                    .placeholder(defaultPic)
                    .error(defaultPic)
                    .fallback(defaultPic)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(binding.profileImg)
            } else {
                binding.profileImg.setImageDrawable(defaultPic)
            }
        }
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        this.listener = listener
    }
}
