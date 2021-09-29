package tech.okcredit.home.ui.homesearch.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.getColorFromAttr
import tech.okcredit.home.R
import tech.okcredit.home.databinding.HomesearchNoUserFoundViewBinding
import tech.okcredit.home.utils.htmlText

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class NoUserFoundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = HomesearchNoUserFoundViewBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setNoUserFoundMessage(message: String) {
        binding.tvMessage.htmlText(message)
    }

    @ModelProp
    fun showLoader(show: Boolean) {
        if (show) {
            binding.pgLoader.visibility = View.VISIBLE
            binding.tvAdd.setTextColor(context!!.getColorFromAttr(R.attr.colorPrimary))
        } else {
            binding.pgLoader.visibility = View.GONE
            binding.tvAdd.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
    }

    @CallbackProp
    fun setListener(listener: AddListener?) {
        binding.cvAdd.setOnClickListener {
            listener?.onAddNewUser()
        }
    }

    interface AddListener {
        fun onAddNewUser() // can be customer or supplier
    }
}
