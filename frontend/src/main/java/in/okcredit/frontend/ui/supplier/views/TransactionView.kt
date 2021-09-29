package `in`.okcredit.frontend.ui.supplier.views

import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyKey.REASON
import `in`.okcredit.analytics.PropertyKey.TXN_ID
import `in`.okcredit.analytics.PropertyValue.STACKTRACE
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.ui.supplier.SupplierScreenItem
import `in`.okcredit.frontend.utils.DimensionUtil
import `in`.okcredit.frontend.utils.DrawableUtil
import `in`.okcredit.merchant.customer_ui.databinding.CustomerFragmentTxItemViewBinding
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.common.base.Strings
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.getString
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.visible
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TransactionView @JvmOverloads constructor(
    val ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    internal lateinit var txnItem: SupplierScreenItem.TransactionItem

    private var tracker: Tracker? = null

    private val TIME_LIMIT = 900000 // 15 minutes in milliseconds

    private var listener: Listener? = null

    private val rotate: RotateAnimation =
        RotateAnimation(360f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)

    interface Listener {
        fun onTransactionClicked(txnId: String, currentDue: Long)
    }

    private val binding: CustomerFragmentTxItemViewBinding =
        CustomerFragmentTxItemViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        rotate.duration = 500
        rotate.repeatCount = Animation.INFINITE
        rotate.interpolator = LinearInterpolator()

        rootView.setOnClickListener {
            listener?.onTransactionClicked(txnItem.txnId, txnItem.currentDue)
        }
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setTracker(tracker: Tracker) {
        this.tracker = tracker
    }

    @ModelProp
    fun setData(txnItem: SupplierScreenItem.TransactionItem) {
        this.txnItem = txnItem
    }

    @AfterPropsSet
    fun setDataOncePropSet() {
        setUiGravity()
        CurrencyUtil.renderV2(txnItem.amount, binding.txContainer.txAmount, txnItem.payment)
        CurrencyUtil.renderArrowsV2(txnItem.amount, binding.txContainer.arrows, txnItem.payment)

        binding.txContainer.txDate.text = txnItem.date

        if (Strings.isNullOrEmpty(txnItem.note)) {
            binding.txContainer.txNote.visibility = View.GONE
        } else {
            binding.txContainer.txNote.visibility = View.VISIBLE
            binding.txContainer.txNote.text = txnItem.note
        }

        if (txnItem.syncing) {
            binding.txContainer.sync.setImageDrawable(
                DrawableUtil.getDrawableWithColor(
                    context,
                    `in`.okcredit.merchant.customer_ui.R.drawable.ic_single_tick,
                    R.color.grey400
                )
            )
        } else {
            binding.txContainer.sync.setImageDrawable(
                DrawableUtil.getDrawableWithColor(
                    context,
                    R.drawable.ic_sync_pending,
                    R.color.grey400
                )
            )
        }

        if (txnItem.finalReceiptUrl == null || Strings.isNullOrEmpty(txnItem.finalReceiptUrl)) {
            binding.txContainer.txBill.visibility = View.GONE
            binding.txContainer.txBillImageContainer.visibility = View.GONE
        } else {
            binding.txContainer.imageCountContainer.visibility = View.GONE
            val requestOptions = RequestOptions().transform(
                CenterInside(),
                RoundedCorners(DimensionUtil.dp2px(ctx, 12.0f).toInt())
            )

            binding.txContainer.txBill.visibility = View.GONE
            binding.txContainer.txBillImageContainer.visibility = View.VISIBLE
            animStart(binding.txContainer.ivRefresh)

            tracker?.trackDebug(
                "transaction_receipt_image_load",
                hashMapOf(
                    TXN_ID to txnItem.txnId,
                    "image_url" to "${txnItem.receiptUrl}",
                    "screen" to "supplier Transaction",
                    "step" to "1"
                )
            )
            Glide.with(ctx)
                .load(txnItem.finalReceiptUrl)
                .thumbnail(0.15f)
                .timeout(TIME_LIMIT)
                .placeholder(R.drawable.ic_img_place_holder)
                .apply(requestOptions)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        animStop(binding.txContainer.ivRefresh)
                        e?.let {
                            handleGlideResponse(e, txnItem.finalReceiptUrl)
                        }
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        animStop(binding.txContainer.ivRefresh)
                        makeTxnBillVisible()
                        tracker?.trackDebug(
                            "transaction_receipt_image_load",
                            hashMapOf(
                                PropertyKey.TXN_ID to txnItem.txnId,
                                "image_url" to "${txnItem.receiptUrl}",
                                "screen" to "supplier Transaction",
                                "step" to "2",
                                "load_successful" to "true"
                            )
                        )
                        return false
                    }
                })
                .into(binding.txContainer.txBillImage)
        }
        when {
            txnItem.isOnlineTxn -> {
                binding.txContainer.txTag.visibility = View.VISIBLE
                binding.txContainer.txTag.text = context.getString(R.string.online_payment_transaction)
            }
            txnItem.createdBySupplier -> {
                binding.txContainer.txTag.visibility = View.VISIBLE
                val str = String.format(
                    context.getString(R.string.added_by),
                    ellipsizeName(txnItem.supplierName)
                )
                binding.txContainer.txTag.text = str
            }
            else -> {
                binding.txContainer.txTag.visibility = View.GONE
            }
        }

        binding.txContainer.btnRetry.setOnClickListener {
            animStart(binding.txContainer.ivRefresh)
            makeRetryBtnInVisible()
            tracker?.trackEvents(
                eventName = "Receipt : retry button clicked",
                propertiesMap = PropertiesMap.create()
                    .add("screen", "supplier Transaction")
                    .add("img_url", txnItem.receiptUrl ?: "")
                    .add(TXN_ID, txnItem.txnId)

            )
            loadImage(binding.txContainer.txBillImage, txnItem.finalReceiptUrl)
        }

        if (Strings.isNullOrEmpty(txnItem.finalReceiptUrl)) {
            binding.txContainer.txBill.visibility = View.GONE
        } else {
            binding.txContainer.txBill.visibility = View.VISIBLE
        }

        when {
            (txnItem.currentDue * 100).toInt() == 0 ->
                "₹0 ${context.resources.getString(R.string.due)}".also { binding.txContainer.totalAmount.text = it }
            txnItem.currentDue > 0 ->
                "₹${CurrencyUtil.formatV2(txnItem.currentDue)} ${context.resources.getString(R.string.due)}".also {
                    binding.txContainer.totalAmount.text = it
                }
            else ->
                "₹${CurrencyUtil.formatV2(txnItem.currentDue)} ${context.resources.getString(R.string.advance)}".also {
                    binding.txContainer.totalAmount.text = it
                }
        }
    }

    private fun setUiGravity() {
        // set root container gravity
        val paramsTxContainer = binding.txContainer.root.layoutParams as LayoutParams
        binding.txContainer.root.layoutParams = paramsTxContainer
        paramsTxContainer.gravity = if (!txnItem.payment) {
            Gravity.START
        } else {
            Gravity.END
        }
        binding.txContainer.root.requestLayout()

        // set bottom container gravity
        val constrainSet = ConstraintSet()
        constrainSet.clone(binding.txContainer.clOuter)
        if (!txnItem.payment) {
            constrainSet.clear(R.id.bottom_container, ConstraintSet.END)
            constrainSet.connect(
                R.id.bottom_container,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                0
            )
        } else {
            constrainSet.clear(R.id.bottom_container, ConstraintSet.START)
            constrainSet.connect(
                R.id.bottom_container,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                0
            )
        }
        constrainSet.applyTo(binding.txContainer.clOuter)
    }

    fun animStart(animView: View) {
        animView.startAnimation(rotate)
        animView.visible()
    }

    fun animStop(animView: View) {
        animView.clearAnimation()
        animView.gone()
    }

    fun makeRetryBtnInVisible() {
        binding.txContainer.btnRetry.gone()
        binding.txContainer.ivRefresh.visible()
    }

    fun makeTxnBillVisible() {
        binding.txContainer.btnRetry.gone()
        binding.txContainer.ivRefresh.gone()
        binding.txContainer.txBillImage.visible()
    }

    fun makeRetryBtnVisible() {
        binding.txContainer.btnRetry.visible()
        binding.txContainer.ivRefresh.gone()
        binding.txContainer.txBillImage.visible()
    }

    private fun handleGlideResponse(exception: GlideException, url: String?) {
        RecordException.recordException(exception)
        if (exception.rootCauses.size > 0) {
            if (exception.rootCauses.get(0) is UnknownHostException || exception.rootCauses.get(0) is SSLHandshakeException || exception.rootCauses.get(
                    0
                ) is SocketTimeoutException
            ) {
                makeRetryBtnVisible()

                tracker?.trackEvents(
                    eventName = "Receipt : retry button shown",
                    propertiesMap = PropertiesMap.create()
                        .add(TXN_ID, txnItem.txnId)
                        .add("img_url", url ?: "")
                        .add("screen", "supplier Transaction")
                        .add("exception :", "${exception.message}")
                )
            } else {
                binding.txContainer.btnRetry.gone()
                binding.txContainer.ivRefresh.gone()
                binding.txContainer.txBillImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        ctx,
                        R.drawable.placeholder_image
                    )
                )
                binding.txContainer.txBillImage.visible()

                tracker?.trackDebug(
                    "transaction_receipt_image_load",
                    hashMapOf(
                        TXN_ID to txnItem.txnId,
                        "image_url" to "$url",
                        "screen" to "supplier Transaction",
                        "step" to "2",
                        "load_successful" to "false",
                        Pair(REASON, exception.message ?: ""),
                        Pair(STACKTRACE, exception.message ?: "")
                    )
                )
            }
        }
    }

    fun loadImage(imageView: ImageView, url: String?) {
        Glide.with(ctx)
            .load(url)
            .placeholder(R.drawable.ic_img_place_holder)
            .thumbnail(0.15f)
            .timeout(TIME_LIMIT)
            .transform(RoundedCorners(context.resources.getDimensionPixelSize(R.dimen.spacing_12)))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean,
                ): Boolean {
                    binding.txContainer.ivRefresh.clearAnimation()
                    makeRetryBtnVisible()
                    e?.let {
                        handleRetryResponse(it, url)
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean,
                ): Boolean {
                    binding.txContainer.ivRefresh.clearAnimation()
                    makeTxnBillVisible()
                    tracker?.trackDebug(
                        "transaction_receipt_image_load",
                        hashMapOf(
                            PropertyKey.TXN_ID to txnItem.txnId,
                            "image_url" to "$url",
                            "screen" to "supplier Transaction",
                            "step" to "2",
                            "load_successful" to "true"
                        )
                    )
                    return false
                }
            })
            .into(imageView)
    }

    fun handleRetryResponse(exception: GlideException, url: String?) {
        if (exception.rootCauses.size > 0) {
            if (exception.rootCauses.get(0) is UnknownHostException || exception.rootCauses.get(0) is SSLHandshakeException || exception.rootCauses.get(
                    0
                ) is SocketTimeoutException
            ) {
                makeRetryBtnVisible()
                ctx.shortToast(getString(R.string.no_internet_connection))
                tracker?.trackEvents(
                    eventName = "Receipt : retry button shown",
                    propertiesMap = PropertiesMap.create()
                        .add(TXN_ID, txnItem.txnId)
                        .add("img_url", url ?: "")
                        .add("screen", "supplier Transaction")
                        .add("exception :", "${exception.message}")
                )
            } else {
                binding.txContainer.btnRetry.gone()
                binding.txContainer.ivRefresh.gone()
                binding.txContainer.txBillImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        ctx,
                        R.drawable.placeholder_image
                    )
                )
                binding.txContainer.txBillImage.visible()

                tracker?.trackDebug(
                    "transaction_receipt_image_load",
                    hashMapOf(
                        TXN_ID to txnItem.txnId,
                        "image_url" to "$url",
                        "screen" to "supplier Transaction",
                        "step" to "2",
                        "load_successful" to "false",
                        Pair(REASON, exception.message ?: ""),
                        Pair(STACKTRACE, exception.message ?: "")
                    )
                )
            }
        } else {
            ctx.shortToast(exception.message.toString())
        }
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    private fun ellipsizeName(name: String?): String {
        val maxLength = 10
        return when {
            name.isNullOrBlank() -> ""
            name.length > maxLength -> name.substring(0, maxLength) + "..."
            else -> name
        }
    }
}
