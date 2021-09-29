package merchant.okcredit.gamification.ipl.game.ui

import `in`.okcredit.collection.contract.MerchantDestinationListener
import android.content.Context
import android.content.Intent
import android.os.Bundle
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.GameActivityBinding
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class GameActivity : OkcActivity(), MerchantDestinationListener, TitleListener {

    companion object {

        // Please update path in DeeplinkUrl as well, if you update it here
        const val MATCH_ID = "match_id"

        fun getIntent(context: Context, matchId: String) =
            Intent(context, GameActivity::class.java).putExtra(MATCH_ID, matchId)

        @JvmStatic
        fun start(context: Context, matchId: String) {
            context.startActivity(getIntent(context, matchId))
        }
    }

    private val binding: GameActivityBinding by viewLifecycleScoped(GameActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onAccountAddedSuccessfully(eta: Long) {
        val fragment = supportFragmentManager.findFragmentById(R.id.ipl_fragment) as? GameFragment
        fragment?.onBoosterTaskReflectWaitingTime(eta)
    }

    override fun onCancelled() {
    }

    override fun updateTitle(title: String) {
        binding.toolbarTitle.text = title
    }
}
