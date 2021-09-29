package `in`.okcredit.merchant.customer_ui.ui.transaction_details.views

import `in`.okcredit.merchant.customer_ui.databinding.ItemBillImageBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.jakewharton.rxbinding3.view.clicks
import merchant.okcredit.accounting.model.TransactionImage
import tech.okcredit.android.base.utils.DimensionUtil

@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class ImageCarouselItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var transactionImage: TransactionImage

    private val binding: ItemBillImageBinding =
        ItemBillImageBinding.inflate(LayoutInflater.from(context), this, true)

    interface Listener {
        fun onPictureClicked(transactionImage: TransactionImage)
    }

    @ModelProp
    fun setData(transactionImage: TransactionImage) {
        this.transactionImage = transactionImage
    }

    @AfterPropsSet
    fun setImage() {
        val requestOptions = RequestOptions().transforms(
            CenterCrop(),
            RoundedCorners(DimensionUtil.dp2px(context!!, 4.0f).toInt())
        )

        Glide.with(context)
            .load(transactionImage.imageUrl)
            .apply(requestOptions)
            .into(binding.pic)
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        binding.pic.clicks()
            .doOnNext {
                listener?.onPictureClicked(transactionImage)
            }
            .subscribe()
    }
}
