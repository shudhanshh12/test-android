package merchant.okcredit.gamification.ipl.rewards

import `in`.okcredit.collection.contract.MerchantDestinationListener
import `in`.okcredit.rewards.contract.RewardModel
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.ClaimRewardActivityBinding
import merchant.okcredit.gamification.ipl.game.data.server.model.response.MysteryPrizeModel
import merchant.okcredit.gamification.ipl.rewards.mysteryprize.ClaimMysteryPrizeFragment
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.replaceFragment
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class ClaimRewardActivity : OkcActivity(), MerchantDestinationListener {

    companion object {

        const val EXTRA_REWARD = "reward"
        const val EXTRA_MYSTERY_PRIZE = "mystery_prize"
        const val EXTRA_SOURCE = "souce"

        @JvmStatic
        fun start(activity: Activity, reward: RewardModel, source: String) {
            activity.startActivity(
                Intent(activity, ClaimRewardActivity::class.java).putExtra(EXTRA_REWARD, reward)
                    .putExtra(EXTRA_SOURCE, source)
            )
            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        @JvmStatic
        fun start(activity: Activity, prize: MysteryPrizeModel, source: String) {
            activity.startActivity(
                Intent(activity, ClaimRewardActivity::class.java).putExtra(
                    EXTRA_MYSTERY_PRIZE,
                    prize
                ).putExtra(EXTRA_SOURCE, source)
            )
            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private val binding: ClaimRewardActivityBinding by viewLifecycleScoped(ClaimRewardActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (intent.extras!!.getParcelable<RewardModel>(EXTRA_REWARD) != null) {
            replaceFragment(ClaimRewardFragment.newInstance(), R.id.fragment_container_view)
        } else if (intent.extras!!.getParcelable<MysteryPrizeModel>(EXTRA_MYSTERY_PRIZE) != null) {
            replaceFragment(ClaimMysteryPrizeFragment.newInstance(), R.id.fragment_container_view)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onAccountAddedSuccessfully(eta: Long) {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as? ClaimRewardFragment
        fragment?.pushIntentForClaimReward()
    }

    override fun onCancelled() {
        shortToast(R.string.add_your_payment_details)
        finish()
    }
}
