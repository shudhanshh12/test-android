package merchant.okcredit.gamification.ipl.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import merchant.okcredit.gamification.ipl.databinding.IplActivityBinding
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class IplActivity : OkcActivity() {

    companion object {

        const val ACTION_WEEKLY_DRAW = "android.intent.action.VIEW_WEEKLY_DRAW"
        const val ACTION_LEADERBOARD = "android.intent.action.VIEW_LEADERBOARD"

        fun getIntent(context: Context) = Intent(context, IplActivity::class.java)

        @JvmStatic
        fun start(activity: Activity) {
            Intent.ACTION_AIRPLANE_MODE_CHANGED
            activity.startActivity(getIntent(activity))
        }

        fun getIntentForWeeklyDraw(context: Context): Intent = getIntent(context).setAction(ACTION_WEEKLY_DRAW)

        fun getIntentForLeaderboard(context: Context): Intent = getIntent(context).setAction(ACTION_LEADERBOARD)
    }

    private val binding: IplActivityBinding by viewLifecycleScoped(IplActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}
