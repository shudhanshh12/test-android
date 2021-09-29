package merchant.okcredit.gamification.ipl.match.views

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.Glide
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.ItemMatchBinding
import merchant.okcredit.gamification.ipl.game.data.server.model.response.Match
import merchant.okcredit.gamification.ipl.game.ui.GameActivity
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.utils.IplUtils
import tech.okcredit.android.base.animation.AnimationUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class MatchView @JvmOverloads constructor(
    context: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ItemMatchBinding = ItemMatchBinding.inflate(LayoutInflater.from(context), this, true)

    private var eventTracker: IplEventTracker? = null

    init {
        Handler().postDelayed(
            {
                AnimationUtils.leftRightMotion(binding.ivHand)
            },
            ANIMATION_DELAY
        )
    }

    @ModelProp
    fun setMatch(match: Match) {
        binding.apply {
            title.text = match.seriesName

            startTime.text = IplUtils.getStartTime(match.startTime)

            homeTeamName.text = match.homeTeam.shortName
            Glide.with(context)
                .load(match.homeTeam.logoLink)
                .placeholder(R.drawable.ic_person_placeholder)
                .into(homeTeamLogo)

            awayTeamName.text = match.awayTeam.shortName
            Glide.with(context)
                .load(match.awayTeam.logoLink)
                .placeholder(R.drawable.ic_person_placeholder)
                .into(awayTeamLogo)

            play.setOnClickListener {
                startGameActivity(match.id)
            }

            root.setOnClickListener {
                startGameActivity(match.id)
            }
        }
    }

    @CallbackProp
    fun setEventTracker(eventTracker: IplEventTracker?) {
        this.eventTracker = eventTracker
    }

    private fun startGameActivity(matchId: String) {
        // TODO try navigation component
        GameActivity.start(context, matchId)
        eventTracker?.matchSelected()
    }

    companion object {
        const val ANIMATION_DELAY = 1000L
    }
}
