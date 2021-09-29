package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views

import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.ItemPictureBinding
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.controller.models.AddBillModel
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.jakewharton.rxbinding3.view.clicks
import tech.okcredit.android.base.extensions.dpToPixel

@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class RoboflowPictureView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private lateinit var roboflowImage: AddBillModel.RoboflowPicture

    private val binding = ItemPictureBinding.inflate(LayoutInflater.from(context), this, true)

    private val cropMargin = context.dpToPixel(16f).toInt()
    private val strokeThickness = context.dpToPixel(2f).toInt()

    interface Listener {
        fun onRoboflowPictureClicked(roboflowImage: AddBillModel.RoboflowPicture)
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        binding.pic
            .clicks()
            .doOnNext { listener?.onRoboflowPictureClicked(roboflowImage) }
            .subscribe()
    }

    @ModelProp
    fun setPicture(roboflowImage: AddBillModel.RoboflowPicture) {
        this.roboflowImage = roboflowImage
        val defaultPic = ContextCompat.getDrawable(context, R.drawable.ic_account)

        GlideApp.with(context)
            .load(roboflowImage.image.file.path)
            .transform(
                RoboflowTransformer(
                    width = roboflowImage.width,
                    height = roboflowImage.height,
                    amountBox = roboflowImage.amountBox,
                    cropMargin = cropMargin,
                    strokeThickness = strokeThickness
                ),
                RoundedCorners(context.dpToPixel(4f).toInt())
            )
            .placeholder(defaultPic)
            .fallback(defaultPic)
            .into(binding.pic)
    }
}
