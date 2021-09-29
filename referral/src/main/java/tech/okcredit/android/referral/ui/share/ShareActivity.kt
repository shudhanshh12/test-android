package tech.okcredit.android.referral.ui.share

import android.content.Context
import android.content.Intent
import android.os.Bundle
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.referral.R

class ShareActivity : OkcActivity() {

    companion object {

        @JvmStatic
        fun starterIntent(context: Context) = Intent(context, ShareActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.share_activity)
    }
}
