package `in`.okcredit.merchant.customer_ui.ui.discount_details.views

import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.customer_ui.R
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_bill_image.view.*
import tech.okcredit.android.base.utils.DimensionUtil

@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class ImageCarouselItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var transactionImage: merchant.okcredit.accounting.model.TransactionImage

    init {
        inflate(context, R.layout.item_bill_image, this)
    }

    interface Listener {
        fun onPictureClicked(transactionImage: merchant.okcredit.accounting.model.TransactionImage)
    }

    @ModelProp
    fun setData(transactionImage: merchant.okcredit.accounting.model.TransactionImage) {
        this.transactionImage = transactionImage
    }

    @AfterPropsSet
    fun setImage() {
        val requestOptions = RequestOptions().transforms(
            CenterCrop(),
            RoundedCorners(DimensionUtil.dp2px(context!!, 4.0f).toInt())
        )

        GlideApp.with(context!!)
            .load(transactionImage.imageUrl)
            .apply(requestOptions)
            .into(pic)
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        pic.setOnClickListener { listener?.onPictureClicked(transactionImage) }
    }
}
