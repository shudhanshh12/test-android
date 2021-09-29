package `in`.okcredit.merchant.customer_ui.ui.add_discount.views

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
import tech.okcredit.android.base.extensions.dpToPixel
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

    init {
        LayoutInflater.from(ctx).inflate(R.layout.item_picture, this, true)
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
            .transform(CenterCrop(), RoundedCorners(context.dpToPixel(4f).toInt()))
            .placeholder(defaultPic)
            .fallback(defaultPic)
            .into(binding.pic)
    }
}
