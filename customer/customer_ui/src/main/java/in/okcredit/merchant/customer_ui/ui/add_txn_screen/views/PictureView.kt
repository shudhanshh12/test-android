package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views

import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.ItemPictureBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.jakewharton.rxbinding3.view.clicks
import tech.okcredit.android.base.utils.DimensionUtil
import tech.okcredit.camera_contract.CapturedImage

@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class PictureView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private lateinit var capturedImage: CapturedImage

    private val binding = ItemPictureBinding.inflate(LayoutInflater.from(context), this, true)

    interface Listener {
        fun onPictureClicked(capturedImage: CapturedImage)
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        binding.pic.clicks()
            .doOnNext {
                listener?.onPictureClicked(capturedImage)
            }
            .subscribe()
    }

    @ModelProp
    fun setPicture(capturedImage: CapturedImage) {
        this.capturedImage = capturedImage
        val defaultPic = ContextCompat.getDrawable(context, R.drawable.ic_account)

        GlideApp.with(context)
            .load(capturedImage.file.path)
            .transform(CenterCrop(), RoundedCorners(DimensionUtil.dp2px(context!!, 4.0f).toInt()))
            .placeholder(defaultPic)
            .thumbnail(0.20f)
            .fallback(defaultPic)
            .into(binding.pic)
    }
}
