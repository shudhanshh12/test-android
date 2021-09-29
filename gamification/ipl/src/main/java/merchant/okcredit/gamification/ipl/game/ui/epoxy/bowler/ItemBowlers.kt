package merchant.okcredit.gamification.ipl.game.ui.epoxy.bowler

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.IplItemBowlersBinding
import merchant.okcredit.gamification.ipl.game.data.server.model.response.Player
import tech.okcredit.android.base.extensions.disable
import tech.okcredit.android.base.extensions.enable
import tech.okcredit.android.base.extensions.getColorCompat

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ItemBowlers @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attributeSet, defStyle) {

    private val binding: IplItemBowlersBinding = IplItemBowlersBinding.inflate(LayoutInflater.from(context), this, true)

    private var listener: BowlersListener? = null

    private var bowler: Player? = null

    init {
        binding.tvBowlers.setOnClickListener {
            listener?.onBowlerSelected(bowler!!.id)
        }
    }

    @ModelProp
    fun setBowlersName(bowler: Player) {
        this.bowler = bowler
        binding.tvBowlers.text = bowler.name
    }

    @ModelProp
    fun setSelectLoading(loading: Boolean) {
        binding.apply {
            if (loading) {
                disable()
            } else {
                tvBowlers.enable()
                tvBowlers.setBackgroundResource(R.drawable.bg_ipl_curved_green_6dp)
                tvBowlers.setTextColor(context.getColorCompat(R.color.grey900))
            }
        }
    }

    @ModelProp
    fun setExpired(expired: Boolean) {
        binding.apply {
            if (expired) {
                disable()
            }
        }
    }

    @ModelProp
    fun setTeamColor(color: Int) {
        binding.ivTeamColor.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    @CallbackProp
    fun setListener(listener: BowlersListener?) {
        this.listener = listener
    }

    interface BowlersListener {
        fun onBowlerSelected(id: String)
    }

    private fun IplItemBowlersBinding.disable() {
        tvBowlers.disable()
        tvBowlers.setBackgroundResource(R.drawable.bg_ipl_curved_grey_6dp)
        tvBowlers.setTextColor(context.getColorCompat(R.color.white))
    }
}
