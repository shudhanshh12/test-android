package `in`.okcredit.merchant.customer_ui.ui.customer.views

import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey.REASON
import `in`.okcredit.analytics.PropertyKey.TXN_ID
import `in`.okcredit.analytics.PropertyValue.STACKTRACE
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.CustomerFragmentTxItemViewBinding
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerScreenItem
import `in`.okcredit.shared.performance.PerformanceTracker
import `in`.okcredit.shared.utils.SharedDrawableUtils
import android.content.Context
import android.graphics.Paint
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
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.analytics.AccountingEventTracker.PropertyValue.LEDGER_TXN
import merchant.okcredit.accounting.utils.AccountingSharedUtils.TxnGravity
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.*
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import javax.net.ssl.SSLHandshakeException

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TransactionView @JvmOverloads constructor(
    val ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private var listener: Listener? = null
    private var tracker: Tracker? = null
    private var accountingEventTracker: AccountingEventTracker? = null
    lateinit var transactionItem: CustomerScreenItem.TransactionItem

    private val TIMELIMIT = 900000 // 15 minutes in milliseconds

    private var transactionId: String = ""
    private var currentDue: Long = 0L
    private var isDiscountTxn: Boolean = false

    private val rotate: RotateAnimation by lazy {
        RotateAnimation(
            360f,
            0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
    }

    interface Listener {
        fun onTransactionClicked(txnId: String, currentDue: Long, isDiscountTxn: Boolean)
    }

    private val binding: CustomerFragmentTxItemViewBinding =
        CustomerFragmentTxItemViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        rotate.duration = 500
        rotate.repeatCount = Animation.INFINITE
        rotate.interpolator = LinearInterpolator()

        binding.txContainer.clOuter.debounceClickListener {
            listener?.onTransactionClicked(transactionId, currentDue, isDiscountTxn)
        }
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setTracker(tracker: Tracker) {
        this.tracker = tracker
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setAccountingTracker(tracker: AccountingEventTracker) {
        this.accountingEventTracker = tracker
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setPerformanceTracker(performanceTracker: PerformanceTracker) {
        binding.txContainer.clOuter.setTracker { performanceTracker }
    }

    @ModelProp
    fun setData(transactionItem: CustomerScreenItem.TransactionItem) {
        this.transactionId = transactionItem.id
        this.currentDue = transactionItem.currentBalance
        this.isDiscountTxn = transactionItem.discountTransaction
        this.transactionItem = transactionItem
    }

    @AfterPropsSet
    fun setDataAfterPropSet() {
        setContainerGravity(transactionItem.txnGravity)
        setAmountUi(transactionItem)
        setTransactionSyncStatus(transactionItem.isDirty)
        setTransactionTag(transactionItem.txnTag)
        setTransactionBillImage(transactionItem)
        setCashbackUi(transactionItem.cashbackGiven, transactionItem.customerId)

        binding.txContainer.apply {
            txDate.text = transactionItem.date

            if (Strings.isNullOrEmpty(transactionItem.note)) {
                txNote.visibility = View.GONE
            } else {
                txNote.visibility = View.VISIBLE
                txNote.text = transactionItem.note
            }
        }
    }

    private fun setCashbackUi(cashbackEligible: Boolean, customerId: String) {
        if (cashbackEligible) {
            accountingEventTracker?.trackCashbackMsgShown(
                customerId,
                LEDGER_TXN,
            )
        }
        binding.txContainer.buttonCashback.isVisible = cashbackEligible
    }

    private fun setContainerGravity(gravity: TxnGravity) {
        val constrainSet = ConstraintSet()
        constrainSet.clone(binding.txContainer.clOuter)
        if (gravity == TxnGravity.LEFT) {
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

        val paramsTxContainer = binding.txContainer.root.layoutParams as LayoutParams
        binding.txContainer.root.layoutParams = paramsTxContainer
        paramsTxContainer.gravity = if (gravity == TxnGravity.LEFT) {
            Gravity.START
        } else {
            Gravity.END
        }
        binding.txContainer.root.requestLayout()
    }

    private fun setAmountUi(transactionItem: CustomerScreenItem.TransactionItem) {
        binding.txContainer.apply {
            CurrencyUtil.renderV2(
                transactionItem.amount,
                txAmount,
                transactionItem.txnGravity == TxnGravity.LEFT
            )
            CurrencyUtil.renderArrowsV3(
                transactionItem.amount,
                arrows,
                transactionItem.txnGravity == TxnGravity.LEFT
            )
            totalAmount.text = when {
                (transactionItem.currentBalance * 100).toInt() == 0 -> {
                    "₹0 ${getString(R.string.due)}"
                }
                transactionItem.currentBalance > 0 -> {
                    "₹${CurrencyUtil.formatV2(transactionItem.currentBalance)} ${getString(R.string.due)}"
                }
                else -> {
                    "₹${CurrencyUtil.formatV2(transactionItem.currentBalance)} ${getString(R.string.advance)}"
                }
            }
            if (transactionItem.discountTransaction) {
                arrows.isVisible = false
                txAmount.showStrikeThrough(transactionItem.deletedTxn)
                txAmount.setTextColor(context.getColorCompat(R.color.grey900))
            } else {
                txAmount.showStrikeThrough(false)
                arrows.isVisible = true
            }
        }
    }

    fun TextView.showStrikeThrough(show: Boolean) {
        paintFlags =
            if (show) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }

    private fun setTransactionSyncStatus(isDirty: Boolean) {
        if (isDirty) {
            binding.txContainer.sync.setImageDrawable(
                SharedDrawableUtils.getDrawableWithColor(
                    context,
                    R.drawable.ic_sync_pending,
                    R.color.grey400
                )
            )
        } else {
            binding.txContainer.sync.setImageDrawable(
                SharedDrawableUtils.getDrawableWithColor(
                    context,
                    R.drawable.ic_single_tick,
                    R.color.grey400
                )
            )
        }
    }

    private fun setTransactionTag(txnTag: String?) {
        if (txnTag.isNullOrEmpty()) {
            binding.txContainer.txTag.gone()
            return
        }

        binding.txContainer.txTag.text = txnTag
        binding.txContainer.txTag.visible()
    }

    private fun setTransactionBillImage(transaction: CustomerScreenItem.TransactionItem) {
        binding.txContainer.apply {
            if (transaction.image.isNullOrEmpty()) {
                txBill.visibility = View.GONE
                txBillImageContainer.visibility = View.GONE
            } else {
                if (transaction.imageCount > 1) {
                    imageCount.text = "+${transaction.imageCount - 1}"
                    imageCountContainer.visibility = View.VISIBLE
                } else {
                    imageCountContainer.visibility = View.GONE
                }

                val requestOptions = RequestOptions().transform(
                    CenterInside(),
                    RoundedCorners(context.dpToPixel(12.0f).toInt())
                )

                txBill.visibility = View.GONE
                txBillImageContainer.visibility = View.VISIBLE

                val flowId = UUID.randomUUID().toString()
                tracker?.trackDebug(
                    "transaction_receipt_image_load",
                    hashMapOf(
                        TXN_ID to transactionId,
                        "image_url" to transaction.image,
                        "step" to "1",
                        "flow_id" to flowId
                    )
                )

                animStart(ivRefresh)
                Glide.with(ctx)
                    .load(transaction.image)
                    .placeholder(R.drawable.ic_img_place_holder)
                    .thumbnail(0.15f)
                    .timeout(TIMELIMIT)
                    .apply(requestOptions)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean,
                        ): Boolean {
                            animStop(ivRefresh)
                            e?.let {
                                handleGlideResponse(e, transaction.image)
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
                            animStop(ivRefresh)
                            makeTxnBillVisible()
                            tracker?.trackDebug(
                                "transaction_receipt_image_load",
                                hashMapOf(
                                    TXN_ID to transactionId,
                                    "image_url" to transaction.image,
                                    "step" to "2",
                                    "flow_id" to flowId,
                                    "load_successful" to "true"
                                )
                            )
                            return false
                        }
                    })
                    .into(txBillImage)
            }

            btnRetry.debounceClickListener {
                ivRefresh.startAnimation(rotate)
                makeRetryBtnInVisible()
                imageCountContainer.gone()
                if (Strings.isNullOrEmpty(transaction.image).not()) {
                    tracker?.trackEvents(
                        eventName = "Receipt : retry button clicked",
                        propertiesMap = PropertiesMap.create()
                            .add(TXN_ID, transactionId)
                            .add("img_url", transaction.image ?: "")
                    )
                    loadImage(
                        txBillImage,
                        transaction.image,
                        transaction.imageCount ?: 0
                    )
                }
            }
        }
    }

    private fun handleGlideResponse(exception: GlideException, url: String) {
        binding.txContainer.apply {
            RecordException.recordException(exception)
            if (exception.rootCauses.size > 0) {
                if (exception.rootCauses[0] is UnknownHostException ||
                    exception.rootCauses[0] is SSLHandshakeException ||
                    exception.rootCauses[0] is SocketTimeoutException
                ) {
                    makeRetryBtnVisible()
                    tracker?.trackEvents(
                        eventName = "Receipt : retry button shown",
                        propertiesMap = PropertiesMap.create()
                            .add(TXN_ID, transactionId)
                            .add("img_url", url)
                            .add("exception :", "${exception.message}")

                    )
                } else {
                    btnRetry.gone()
                    ivRefresh.gone()
                    txBillImage.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.placeholder_image))
                    txBillImage.visible()
                    tracker?.trackDebug(
                        "transaction_receipt_image_load",
                        hashMapOf(
                            TXN_ID to transactionId,
                            "image_url" to url,
                            "step" to "2",
                            "load_successful" to "false",
                            Pair(REASON, exception.message ?: ""),
                            Pair(STACKTRACE, exception.message ?: "")
                        )
                    )
                }
            }
        }
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
        binding.txContainer.apply {
            btnRetry.gone()
            ivRefresh.visible()
        }
    }

    fun makeTxnBillVisible() {
        binding.txContainer.apply {
            btnRetry.gone()
            ivRefresh.gone()
            txBillImage.visible()
        }
    }

    fun makeRetryBtnVisible() {
        binding.txContainer.apply {
            btnRetry.visible()
            ivRefresh.gone()
            txBillImage.visible()
            imageCountContainer.gone()
        }
    }

    fun loadImage(imageView: ImageView, url: String?, count: Int) {
        binding.txContainer.apply {
            Glide.with(ctx)
                .load(url)
                .placeholder(R.drawable.ic_img_place_holder)
                .thumbnail(0.15f)
                .timeout(TIMELIMIT)
                .transform(RoundedCorners(context.resources.getDimensionPixelSize(R.dimen.spacing_12)))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        animStop(ivRefresh)
                        e?.let {
                            handleRetryResponse(it, url, count)
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
                        ivRefresh.clearAnimation()
                        makeTxnBillVisible()

                        val trackUrl = url ?: "null"

                        tracker?.trackDebug(
                            "transaction_receipt_image_load",
                            hashMapOf(
                                TXN_ID to transactionId,
                                "image_url" to trackUrl,
                                "step" to "2",
                                "load_successful" to "true"
                            )
                        )

                        if (count > 1) {
                            imageCount.text = "+${count - 1}"
                            imageCountContainer.visibility = View.VISIBLE
                        } else {
                            imageCountContainer.visibility = View.GONE
                        }
                        return false
                    }
                })
                .into(imageView)
        }
    }

    fun handleRetryResponse(exception: GlideException, url: String?, count: Int) {
        binding.txContainer.apply {
            if (exception.rootCauses.size > 0) {
                if (exception.rootCauses[0] is UnknownHostException ||
                    exception.rootCauses[0] is SSLHandshakeException ||
                    exception.rootCauses[0] is SocketTimeoutException
                ) {
                    makeRetryBtnVisible()
                    tracker?.trackEvents(
                        eventName = "Receipt : retry button shown",
                        propertiesMap = PropertiesMap.create()
                            .add(TXN_ID, transactionId)
                            .add("img_url", url ?: "")
                            .add("exception :", "${exception.message}")

                    )
                    ctx.shortToast(getString(R.string.no_internet_connection))
                } else {
                    val trackUrl = url ?: ""
                    tracker?.trackDebug(
                        "transaction_receipt_image_load",
                        hashMapOf(
                            TXN_ID to transactionId,
                            "image_url" to trackUrl,
                            "step" to "2",
                            "load_successful" to "false",
                            Pair(REASON, exception.message ?: ""),
                            Pair(STACKTRACE, exception.message ?: "")
                        )
                    )

                    btnRetry.gone()
                    ivRefresh.gone()
                    txBillImage.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.placeholder_image))
                    txBillImage.visible()

                    if (count > 1) {
                        imageCount.text = "+${count - 1}"
                        imageCountContainer.visibility = View.VISIBLE
                    } else {
                        imageCountContainer.visibility = View.GONE
                    }
                }
            } else {
                ctx.shortToast(exception.message.toString())
            }
        }
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        this.listener = listener
    }
}
