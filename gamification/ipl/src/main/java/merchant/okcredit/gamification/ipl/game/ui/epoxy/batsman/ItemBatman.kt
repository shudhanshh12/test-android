package merchant.okcredit.gamification.ipl.game.ui.epoxy.batsman

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.IplItemBatsmanBinding
import merchant.okcredit.gamification.ipl.game.data.server.model.response.Player
import tech.okcredit.android.base.extensions.disable
import tech.okcredit.android.base.extensions.enable
import tech.okcredit.android.base.extensions.getColorCompat

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ItemBatman @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attributeSet, defStyle) {

    private val binding: IplItemBatsmanBinding = IplItemBatsmanBinding.inflate(LayoutInflater.from(context), this, true)

    private var listener: BatsmanListener? = null

    private var batsman: Player? = null

    init {
        binding.tvBatsman.setOnClickListener {
            listener?.onBatsmanSelected(batsman!!.id)
        }
    }

    @ModelProp
    fun setBatsmanName(batsman: Player) {
        this.batsman = batsman
        binding.tvBatsman.text = batsman.name
    }

    @ModelProp
    fun setSelectLoading(loading: Boolean) {
        binding.apply {
            if (loading) {
                disable()
            } else {
                tvBatsman.enable()
                tvBatsman.setBackgroundResource(R.drawable.bg_ipl_curved_green_6dp)
                tvBatsman.setTextColor(context.getColorCompat(R.color.grey900))
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
    fun setListener(listener: BatsmanListener?) {
        this.listener = listener
    }

    interface BatsmanListener {
        fun onBatsmanSelected(id: String)
    }

    private fun IplItemBatsmanBinding.disable() {
        tvBatsman.disable()
        tvBatsman.setBackgroundResource(R.drawable.bg_ipl_curved_grey_6dp)
        tvBatsman.setTextColor(context.getColorCompat(R.color.white))
    }
}
