package `in`.okcredit.merchant.rewards.ui.rewards_dialog

import `in`.okcredit.collection.contract.MerchantDestinationListener
import `in`.okcredit.merchant.rewards.R
import `in`.okcredit.merchant.rewards.analytics.RewardsEventTracker
import `in`.okcredit.merchant.rewards.databinding.ClaimRewardActivityBinding
import `in`.okcredit.rewards.contract.RewardModel
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dagger.Lazy
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.replaceFragment
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import javax.inject.Inject

class ClaimRewardActivity : OkcActivity(), MerchantDestinationListener {

    companion object {

        const val EXTRA_REWARD = "reward"
        const val EXTRA_SOURCE = "source"
        const val EXTRA_REFERENCE_ID = "reference_id"

        @JvmStatic
        fun start(activity: Activity, reward: RewardModel, source: String) {
            start(activity, reward, source, "")
        }

        @JvmStatic
        fun start(activity: Activity, reward: RewardModel, source: String, referenceId: String) {
            activity.startActivity(
                Intent(activity, ClaimRewardActivity::class.java)
                    .putExtra(EXTRA_REWARD, reward)
                    .putExtra(EXTRA_SOURCE, source)
                    .putExtra(EXTRA_REFERENCE_ID, referenceId)
            )
            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    @Inject
    lateinit var tracker: Lazy<RewardsEventTracker>

    private val binding: ClaimRewardActivityBinding by viewLifecycleScoped(ClaimRewardActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val rewardModel = intent.extras!!.getParcelable<RewardModel>(EXTRA_REWARD)
        if (rewardModel != null) {
            tracker.get().trackClaimedRewardViewed(
                amount = rewardModel.amount,
                type = rewardModel.reward_type,
                status = rewardModel.status
            )
            replaceFragment(ClaimRewardsDialog(), R.id.fragment_container_view)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onAccountAddedSuccessfully(eta: Long) {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as? ClaimRewardsDialog
        fragment?.pushIntentForClaimReward()
    }

    override fun onCancelled() {
        shortToast(R.string.setup_bank_account_to_get_reward)
    }
}
