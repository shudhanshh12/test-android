package tech.okcredit.bill_management_ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.jakewharton.rxbinding3.view.clicks
import kotlinx.android.synthetic.main.filledbills_view.view.*
import org.joda.time.DateTime
import tech.okcredit.android.base.TempCurrencyUtil
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.invisible
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.ImageCache
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.sdk.store.database.LocalBill
import tech.okcredit.sdk.store.database.TxnType
import java.util.concurrent.TimeUnit

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class FilledBillsView @JvmOverloads constructor(
    val ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private lateinit var lastSeenTime: String
    private lateinit var role: String
    private lateinit var localBillList: Array<out LocalBill>

    private var imageCache: ImageCache? = null

    interface Listener {
        fun clickedFilledBillsView(billId: String)
    }

    init {
        LayoutInflater.from(ctx).inflate(R.layout.filledbills_view, this, true)
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setImageCache(imageCache: ImageCache) {
        this.imageCache = imageCache
    }

    @ModelProp
    fun setItems(vararg list: LocalBill) {
        this.localBillList = list
    }

    @ModelProp
    fun setRole(role: String) {
        this.role = role
    }

    @ModelProp
    fun setLastSeenTime(lastSeenTime: String) {
        this.lastSeenTime = lastSeenTime
    }

    @AfterPropsSet
    fun setData() {

        Glide.with(ctx)
            .load(localBillList[0].localBillDocList[0].imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .fallback(R.drawable.placeholder_image)
            .thumbnail(0.15f)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Handler().postDelayed(
                        {
                            Glide.with(ctx)
                                .load(localBillList[0].localBillDocList[0].url)
                                .placeholder(R.drawable.placeholder_image)
                                .fallback(R.drawable.placeholder_image)
                                .thumbnail(0.15f)
                                .into(bill_one)
                        },
                        300
                    )

                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            }).into(bill_one)

        if (localBillList.size == 1) {
            cvContainer2.invisible()
            cvContainer.visible()
            val bill1 = localBillList[0]
            setTextures1(bill1)
        } else {
            cvContainer2.visible()
            cvContainer.visible()
            progress_circular.gone()
            progress_circular_2.gone()

            Glide.with(ctx)
                .load(localBillList[1].localBillDocList[0].imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .fallback(R.drawable.placeholder_image)
                .thumbnail(0.15f)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Handler().postDelayed(
                            {
                                Glide.with(ctx)
                                    .load(localBillList[1].localBillDocList[0].url)
                                    .placeholder(R.drawable.placeholder_image)
                                    .fallback(R.drawable.placeholder_image)
                                    .thumbnail(0.15f)
                                    .into(bill_two)
                            },
                            300
                        )

                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                }).into(bill_two)
            setTextures1(localBillList[0])
            setTextures2(localBillList[1])
        }
    }

    private fun setTextures2(bill1: LocalBill) {
        if (bill1.createdByMe.not() && bill1.createdAt > lastSeenTime) {
            label_2.visible()
            updated_2.gone()
            amount_2.gone()
            arrows_2.gone()
        } else if (bill1.createdByMe.not() && bill1.updatedAt != null && bill1.updatedAt!! > lastSeenTime) {
            label_2.gone()
            updated_2.visible()
            amount_2.gone()
            arrows_2.gone()
        } else if (bill1.amount != null && bill1.amount != "0") {
            label_2.gone()
            updated_2.gone()
            amount_2.visible()
            amount_2.text = TempCurrencyUtil.formatV2(bill1.amount!!.toLong())
            arrows_2.visible()
            if (bill1.txnType == TxnType.CREDIT && role == BILL_INTENT_EXTRAS.CUSTOMER) {
                val drawable =
                    arrows.context.getDrawable(R.drawable.ic_take)
                arrows_2.setImageDrawable(drawable)
                arrows_2.rotation = 180f
                amount_2.setTextColor(ContextCompat.getColor(context, R.color.red_primary))
            } else if (bill1.txnType == TxnType.PAYMENT && role == BILL_INTENT_EXTRAS.CUSTOMER) {
                val drawable =
                    arrows_2.context.getDrawable(R.drawable.ic_give)
                arrows_2.rotation = 180f
                arrows_2.setImageDrawable(drawable)
                amount_2.setTextColor(ContextCompat.getColor(context, R.color.green_primary))
            } else if (bill1.txnType == TxnType.PAYMENT && role == BILL_INTENT_EXTRAS.SUPPLIER) {
                val drawable =
                    arrows_2.context.getDrawable(R.drawable.ic_give)
                arrows_2.setImageDrawable(drawable)
                amount_2.setTextColor(ContextCompat.getColor(context, R.color.green_primary))
            } else if (bill1.txnType == TxnType.CREDIT && role == BILL_INTENT_EXTRAS.CUSTOMER) {
                val drawable =
                    arrows_2.context.getDrawable(R.drawable.ic_take)
                arrows_2.setImageDrawable(drawable)
                amount_2.setTextColor(ContextCompat.getColor(context, R.color.red_primary))
            }
        } else {
            amount_2.gone()
            arrows_2.gone()
        }
        if (bill1.localBillDocList.size > 1) {
            image_count_container_2.visible()
            image_count_2.visible()
            image_count_2.text = "+" + (bill1.localBillDocList.size - 1).toString()
        } else {
            image_count_container_2.gone()
            image_count_2.gone()
        }
        date_2.visible()
        date_2.text = DateTimeUtils.getFormat1(DateTime(bill1.billDate?.toLong()))
        bill1.createdByMe.let {
            if (it) {
                placeholder_2.gone()
            } else {
                placeholder_2.visible()
            }
        }
    }

    private fun setTextures1(bill1: LocalBill) {
        if (bill1.createdByMe.not() && bill1.createdAt > lastSeenTime) {
            label.visible()
            updated.gone()
            amount.gone()
            arrows.gone()
        } else if (bill1.createdByMe.not() && bill1.updatedAt != null && bill1.updatedAt!! > lastSeenTime) {
            label.gone()
            updated.visible()
            amount.gone()
            arrows.gone()
        } else if (bill1.amount != null && bill1.amount != "0") {
            label.gone()
            updated.gone()
            amount.visible()
            arrows.visible()
            amount.text = TempCurrencyUtil.formatV2(bill1.amount!!.toLong())

            if (bill1.txnType == TxnType.CREDIT && role == BILL_INTENT_EXTRAS.CUSTOMER) {
                val drawable = arrows.context.getDrawable(R.drawable.ic_take)
                arrows.setImageDrawable(drawable)
                arrows.rotation = 180f
                amount.setTextColor(ContextCompat.getColor(context, R.color.red_primary))
            } else if (bill1.txnType == TxnType.PAYMENT && role == BILL_INTENT_EXTRAS.CUSTOMER) {
                val drawable = arrows.context.getDrawable(R.drawable.ic_give)
                arrows.rotation = 180f
                arrows.setImageDrawable(drawable)
                amount.setTextColor(ContextCompat.getColor(context, R.color.green_primary))
            } else if (bill1.txnType == TxnType.PAYMENT && role == BILL_INTENT_EXTRAS.SUPPLIER) {
                val drawable = arrows.context.getDrawable(R.drawable.ic_give)
                arrows.setImageDrawable(drawable)
                amount.setTextColor(ContextCompat.getColor(context, R.color.green_primary))
            } else if (bill1.txnType == TxnType.CREDIT && role == BILL_INTENT_EXTRAS.CUSTOMER) {
                val drawable = arrows.context.getDrawable(R.drawable.ic_take)
                arrows.setImageDrawable(drawable)
                amount.setTextColor(ContextCompat.getColor(context, R.color.red_primary))
            }
        } else {
            label.gone()
            updated.gone()
            amount.gone()
            arrows.gone()
        }

        if (bill1.localBillDocList.size > 1) {
            image_count_container.visible()
            image_count.visible()
            image_count.text = "+" + (bill1.localBillDocList.size - 1).toString()
        } else {
            image_count_container.gone()
            image_count.gone()
        }
        date.visible()
        date.text = DateTimeUtils.getFormat1(DateTime(bill1.billDate?.toLong()))
        bill1.createdByMe.let {
            if (it) {
                placeholder.gone()
            } else {
                placeholder.visible()
            }
        }
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        cvContainer.clicks()
            .throttleFirst(50, TimeUnit.MILLISECONDS)
            .doOnNext { listener?.clickedFilledBillsView(localBillList[0].id) }
            .subscribe()
        cvContainer2.clicks()
            .throttleFirst(50, TimeUnit.MILLISECONDS)
            .doOnNext { listener?.clickedFilledBillsView(localBillList[1].id) }
            .subscribe()
    }
}
