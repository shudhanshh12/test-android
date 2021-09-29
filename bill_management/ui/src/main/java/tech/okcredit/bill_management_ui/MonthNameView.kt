package tech.okcredit.bill_management_ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks
import kotlinx.android.synthetic.main.month_name_view.view.*
import java.util.concurrent.TimeUnit

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class MonthNameView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    interface Listener {
        fun clickedFilledBillsView()
    }

    init {
        LayoutInflater.from(ctx).inflate(R.layout.month_name_view, this, true)
    }

    @ModelProp
    fun setName(value: String?) {
        month_name.text = value
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        rootView.clicks()
            .throttleFirst(50, TimeUnit.MILLISECONDS)
            .doOnNext { listener?.clickedFilledBillsView() }
            .subscribe()
    }
}
