package `in`.okcredit.frontend.ui.add_supplier_transaction.views

import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.analytics.EventProperties
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.merchant.customer_ui.databinding.AddTxnFragmentMediaViewBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class MediaView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    interface Listener {
        fun onCameraClickedFromBottomSheet()
        fun onGalleryClickedFromBottomSheet()
    }

    private val binding: AddTxnFragmentMediaViewBinding =
        AddTxnFragmentMediaViewBinding.inflate(LayoutInflater.from(context), this)

    @CallbackProp
    fun setListener(listener: Listener?) {

        binding.gallery.clicks()
            .doOnNext {
                Analytics.track(
                    AnalyticsEvents.ADD_RECEIPT,
                    EventProperties.create()
                        .with(PropertyKey.TYPE, "gallery")
                        .with(PropertyKey.SOURCE, "default")
                )
                listener?.onGalleryClickedFromBottomSheet()
            }
            .subscribe()

        binding.camera.clicks()
            .doOnNext {
                Analytics.track(
                    AnalyticsEvents.ADD_RECEIPT,
                    EventProperties.create()
                        .with(PropertyKey.TYPE, "camera")
                        .with(PropertyKey.SOURCE, "default")
                )
                listener?.onCameraClickedFromBottomSheet()
            }
            .subscribe()
    }
}
