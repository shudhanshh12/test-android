package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views

import `in`.okcredit.merchant.customer_ui.databinding.AddBillItemViewBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class AddBillsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var listener: Listener? = null

    interface Listener {
        fun onAddBillsClicked()
    }

    private val binding: AddBillItemViewBinding =
        AddBillItemViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.apply {
            addBillTextview.setOnClickListener {
                listener?.onAddBillsClicked()
            }
        }
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        this.listener = listener
    }
}
