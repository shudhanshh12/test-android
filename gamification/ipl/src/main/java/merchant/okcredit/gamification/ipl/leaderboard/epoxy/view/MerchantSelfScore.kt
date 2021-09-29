package merchant.okcredit.gamification.ipl.leaderboard.epoxy.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.IplLeaderboardSelfScoreBinding
import merchant.okcredit.gamification.ipl.game.data.server.model.response.MerchantScore
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.TextDrawableUtils
import java.text.NumberFormat
import java.util.*
import kotlin.random.Random

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class MerchantSelfScore @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    deffStyle: Int = 0
) : FrameLayout(context, attributeSet, deffStyle) {

    private val binding = IplLeaderboardSelfScoreBinding.inflate(LayoutInflater.from(context), this, true)
    private val random: Int = Random.nextInt(50, 75)

    @ModelProp
    fun setSelfDetails(self: MerchantScore?) {
        val name = self?.name ?: "User"

        binding.apply {
            val defaultPic = TextDrawableUtils.getRoundTextDrawable(name)
            if (self?.profilePic.isNullOrBlank()) {
                ivProfileImage.setImageDrawable(defaultPic)
            } else {
                Glide.with(context)
                    .load(self?.profilePic)
                    .placeholder(defaultPic)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(ivProfileImage)
            }

            tvMerchantName.text = resources.getString(R.string.self_name, self?.name)
            val moneyEarned = self?.moneyEarned ?: 0f
            tvMoney.text = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(moneyEarned)
            tvPoints.text = self?.points.toString()

            val rank = self?.rank ?: Int.MAX_VALUE
            if (rank > 0) {
                tvRank.visible()
                tvRank.text = resources.getString(R.string.rank_with_prefix, rank)
                tvMotivation.gone()
                viewScoreDivider.gone()
            } else {
                tvRank.gone()
                tvMotivation.text = resources.getString(R.string.msg_rank_motivation, random)
                tvMotivation.visible()
                viewScoreDivider.visible()
            }
        }
    }
}
