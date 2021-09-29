package tech.okcredit.home.ui.home.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks
import kotlinx.android.synthetic.main.home_fragment_tap_to_sync.view.*
import tech.okcredit.home.R
import java.util.concurrent.TimeUnit

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class HomeScreenTapToSyncView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    interface Listener {
        fun onSyncNow()
    }

    init {
        LayoutInflater.from(ctx).inflate(R.layout.home_fragment_tap_to_sync, this, true)
    }

    @ModelProp
    fun setUnSyncCount(count: Int?) {
        title.text = context.getString(R.string.ce_home_notif_unsynced_activity)
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        sync_now.clicks()
            .throttleFirst(300, TimeUnit.MILLISECONDS)
            .doOnNext { listener?.onSyncNow() }
            .subscribe()

        rootView.clicks()
            .throttleFirst(300, TimeUnit.MILLISECONDS)
            .doOnNext { listener?.onSyncNow() }
            .subscribe()
    }
}
