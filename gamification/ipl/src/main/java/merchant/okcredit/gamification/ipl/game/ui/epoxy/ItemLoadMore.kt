package merchant.okcredit.gamification.ipl.game.ui.epoxy

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.ItemLoadMoreBinding
import tech.okcredit.android.base.extensions.disable
import tech.okcredit.android.base.extensions.enable
import tech.okcredit.android.base.extensions.getColorCompat

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ItemLoadMore @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attributeSet, defStyle) {

    private val binding: ItemLoadMoreBinding = ItemLoadMoreBinding.inflate(LayoutInflater.from(context), this, true)

    private var listener: LoadMoreListener? = null

    private var type =
        BATSMAN_LOAD_MORE

    init {
        binding.clLoadMore.setOnClickListener {
            if (type == BATSMAN_LOAD_MORE) {
                listener?.onBatsmanLoadMore()
            } else {
                listener?.onBowlersLoadMore()
            }
        }
    }

    @ModelProp
    fun setExpired(expired: Boolean) {
        if (expired) {
            disable()
        }
    }

    private fun disable() {
        binding.apply {
            clLoadMore.disable()
            clLoadMore.setBackgroundResource(R.drawable.bg_ipl_curved_grey_6dp)
            tvLoadMore.setTextColor(context.getColorCompat(R.color.white))
            tvLoadMore.compoundDrawables[2]?.setColorFilter(
                context.getColorCompat(R.color.white),
                PorterDuff.Mode.SRC_ATOP
            )
        }
    }

    private fun enable() {
        binding.apply {
            clLoadMore.enable()
            clLoadMore.setBackgroundResource(R.drawable.bg_ipl_curved_green_6dp)
            tvLoadMore.setTextColor(context.getColorCompat(R.color.grey900))
            tvLoadMore.compoundDrawables[2]?.setColorFilter(
                context.getColorCompat(R.color.grey900),
                PorterDuff.Mode.SRC_ATOP
            )
        }
    }

    @ModelProp
    fun setSelectLoading(loading: Boolean) {
        binding.apply {
            if (loading) {
                disable()
            } else {
                enable()
            }
        }
    }

    @ModelProp
    fun setType(type: Int) {
        this.type = type
    }

    @CallbackProp
    fun setListener(listener: LoadMoreListener?) {
        this.listener = listener
    }

    interface LoadMoreListener {
        fun onBatsmanLoadMore()
        fun onBowlersLoadMore()
    }

    companion object {
        const val BATSMAN_LOAD_MORE = 1
        const val BOWLERS_LOAD_MORE = 2
        const val LOAD_MORE_SIZE = 7
        const val MIN_ITEM_DISPLAY = 6
    }
}
