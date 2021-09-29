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
import merchant.okcredit.gamification.ipl.databinding.IplLeaderboardMerchantScoreBinding
import merchant.okcredit.gamification.ipl.game.data.server.model.response.MerchantScore
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.TextDrawableUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class OtherMerchantScore @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    deffStyle: Int = 0
) : FrameLayout(context, attributeSet, deffStyle) {

    private val binding = IplLeaderboardMerchantScoreBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setScoreVisibility(hide: Boolean) {
        if (hide) {
            binding.tvMerchantScore.gone()
        } else {
            binding.tvMerchantScore.visible()
        }
    }

    @ModelProp
    fun setOtherMerchantDetails(otherMerchant: MerchantScore?) {
        binding.apply {
            val name = otherMerchant?.name ?: "User"

            val defaultPic = TextDrawableUtils.getRoundTextDrawable(name)
            if (otherMerchant?.profilePic.isNullOrBlank()) {
                ivProfileImage.setImageDrawable(defaultPic)
            } else {
                Glide.with(context)
                    .load(otherMerchant?.profilePic)
                    .placeholder(defaultPic)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(ivProfileImage)
            }

            tvMerchantName.text = otherMerchant?.name
            tvMerchantScore.text = resources.getString(R.string.score, otherMerchant?.points)
            tvRank.text = resources.getString(R.string.rank_with_prefix, otherMerchant?.rank)

            when {
                otherMerchant!!.isGoldPrize() -> {
                    rootLayout.setBackgroundColor(context.getColorCompat(R.color.orange_lite_1))
                }
                otherMerchant.is10KMoneyPrize() -> {
                    rootLayout.setBackgroundColor(context.getColorCompat(R.color.green_lite_1))
                }
                otherMerchant.isJerseyPrize() -> {
                    rootLayout.setBackgroundColor(context.getColorCompat(R.color.indigo_lite))
                }
                otherMerchant.isMiniBatPrize() -> {
                    rootLayout.setBackgroundColor(context.getColorCompat(R.color.red_lite))
                }
                else -> {
                    // rootLayout.setBackgroundColor(Color.TRANSPARENT)
                    rootLayout.setBackgroundColor(context.getColorCompat(R.color.green_lite_1))
                }
            }
        }
    }
}
