package merchant.okcredit.gamification.ipl.match.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.RewardsViewBinding
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.rewards.IplRewardsController
import merchant.okcredit.gamification.ipl.rewards.IplRewardsControllerModel

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class RewardsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = RewardsViewBinding.inflate(LayoutInflater.from(context), this, true)

    private val rewardsController = IplRewardsController()

    init {
        binding.rewardsRecyclerView.apply {
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State,
                ) {
                    val spacing = view.context.resources.getDimensionPixelSize(R.dimen.spacing_10)
                    outRect.top = spacing
                    outRect.bottom = spacing
                    outRect.right = spacing
                    outRect.left = spacing
                }
            })

            adapter = rewardsController.adapter
        }
    }

    @ModelProp
    fun setRewards(rewards: List<IplRewardsControllerModel>) {
        rewardsController.setData(rewards)
    }

    @CallbackProp
    fun setSource(source: String?) {
        rewardsController.source = source
    }

    @CallbackProp
    fun setEventTracker(eventTracker: IplEventTracker?) {
        rewardsController.eventTracker = eventTracker
    }
}
