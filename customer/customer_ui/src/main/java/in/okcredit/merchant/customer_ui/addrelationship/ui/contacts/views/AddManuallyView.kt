package `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.views

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.AddManuallyItemviewBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class AddManuallyView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private var listener: AddManuallyListener? = null
    private var name: String? = null
    private val binding = AddManuallyItemviewBinding
        .inflate(LayoutInflater.from(context), this, true)

    init {
        binding.root.setOnClickListener {
            listener?.onAddManuallyClicked(name ?: "")
        }
    }

    @CallbackProp
    fun setListener(listener: AddManuallyListener?) {
        this.listener = listener
    }

    @ModelProp
    fun setName(name: String) {
        this.name = name
        if (name.isNotEmpty()) {
            binding.addManuallyText.text = context.getString(
                R.string.t_001_addrel_list_cta_add_manually,
                name
            )
        }
    }

    interface AddManuallyListener {
        fun onAddManuallyClicked(name: String)
    }
}
