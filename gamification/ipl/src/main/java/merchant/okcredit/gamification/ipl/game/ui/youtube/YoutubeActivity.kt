package merchant.okcredit.gamification.ipl.game.ui.youtube

import `in`.okcredit.collection.contract.MerchantDestinationListener
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.YoutubeActivityBinding
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.replaceFragment
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class YoutubeActivity : OkcActivity(), MerchantDestinationListener {

    companion object {

        const val EXTRA_SOURCE = "source"
        const val EXTRA_YOUTUBE_LINK = "youtube_link"

        @JvmStatic
        fun start(activity: Activity, youtubeLink: String, source: String) {
            activity.startActivity(
                Intent(activity, YoutubeActivity::class.java)
                    .putExtra(EXTRA_YOUTUBE_LINK, youtubeLink)
                    .putExtra(EXTRA_SOURCE, source)
            )
            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private val binding: YoutubeActivityBinding by viewLifecycleScoped(YoutubeActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val fragment = YoutubeFragment.newInstance()
        val bundle = Bundle()
        bundle.putString(EXTRA_YOUTUBE_LINK, intent.getStringExtra(EXTRA_YOUTUBE_LINK))
        bundle.putString(EXTRA_SOURCE, intent.getStringExtra(EXTRA_SOURCE))
        fragment.arguments = bundle
        replaceFragment(fragment, R.id.fragment_container_view)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onAccountAddedSuccessfully(eta: Long) {
    }

    override fun onCancelled() {
        shortToast(R.string.add_your_payment_details)
        finish()
    }
}
