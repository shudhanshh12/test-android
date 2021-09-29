package `in`.okcredit.merchant.rewards.ui

import `in`.okcredit.collection.contract.MerchantDestinationListener
import `in`.okcredit.merchant.rewards.R
import `in`.okcredit.merchant.rewards.databinding.RewardsActivityBinding
import android.content.Context
import android.content.Intent
import android.os.Bundle
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class RewardsActivity : OkcActivity(), MerchantDestinationListener {

    companion object {

        fun getIntent(context: Context) =
            Intent(context, RewardsActivity::class.java)

        fun start(context: Context) {
            val intent = Intent(context, RewardsActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val binding: RewardsActivityBinding by viewLifecycleScoped(RewardsActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onAccountAddedSuccessfully(eta: Long) {}

    override fun onCancelled() {
        shortToast(R.string.you_claim_rewards_menu)
        finish()
    }
}
