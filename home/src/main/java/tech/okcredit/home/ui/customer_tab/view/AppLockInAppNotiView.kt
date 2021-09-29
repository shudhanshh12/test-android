package tech.okcredit.home.ui.customer_tab.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.item_applock_inapp.view.*
import tech.okcredit.home.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class AppLockInAppNotiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var listener: AppLockClickListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.item_applock_inapp, this, true)
        clAppLock.setOnClickListener {
            listener?.onAppLockSetup()
        }
        ivExit.setOnClickListener {
            listener?.onAppLockClose()
        }
    }

    interface AppLockClickListener {
        fun onAppLockSetup()
        fun onAppLockClose()
    }

    @CallbackProp
    fun setListener(listener: AppLockClickListener?) {
        this.listener = listener
    }
}
