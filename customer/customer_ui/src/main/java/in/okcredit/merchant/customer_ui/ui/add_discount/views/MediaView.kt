package `in`.okcredit.merchant.customer_ui.ui.add_discount.views

import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.analytics.EventProperties
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.merchant.customer_ui.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks
import kotlinx.android.synthetic.main.add_txn_fragment_media_view.view.*

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

    init {
        LayoutInflater.from(ctx).inflate(R.layout.add_txn_fragment_media_view, this, true)
    }

    @CallbackProp
    fun setListener(listener: Listener?) {

        gallery.clicks()
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

        camera.clicks()
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
