package tech.okcredit.home.ui.acccountV2.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.android.AndroidInjection
import dagger.android.HasAndroidInjector
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.replaceFragment
import tech.okcredit.home.R

class AccountActivity : OkcActivity(), HasAndroidInjector {
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val accountScreen = AccountFragment.newInstance()
        val notificationUrl = intent.extras?.getString(ARG_NOTIFICATION_URL)
        val bundle = Bundle()
        notificationUrl?.let {
            bundle.putString(ARG_NOTIFICATION_URL, it)
            accountScreen.arguments = bundle
        }
        replaceFragment(accountScreen, R.id.account_host)
    }

    companion object {
        const val ARG_NOTIFICATION_URL = "notification_url"
        fun startingIntent(context: Context): Intent {
            return Intent(context, AccountActivity::class.java)
        }
    }
}
