package merchant.okcredit.gamification.ipl.match.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.IplErrorBinding
import tech.okcredit.android.base.extensions.visible

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class MatchLoadErrorView @JvmOverloads constructor(
    context: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    interface RetryListener {
        fun retry()
    }

    private val binding = IplErrorBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.clError.visible()
    }

    @ModelProp
    fun setHasConnectivityIssues(hasConnectivityIssues: Boolean) {
        if (hasConnectivityIssues) {
            binding.apply {
                tvError.text = context.getString(R.string.interent_error)
                ivError.setImageResource(R.drawable.bg_network_error)
            }
        } else {
            binding.apply {
                tvError.text = context.getString(R.string.err_default)
                ivError.setImageResource(R.drawable.bg_server_error)
            }
        }
    }

    @CallbackProp
    fun setListener(listener: RetryListener?) {
        binding.mbRetry.setOnClickListener {
            listener?.retry()
        }
    }
}
