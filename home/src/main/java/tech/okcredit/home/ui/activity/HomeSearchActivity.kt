package tech.okcredit.home.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.android.AndroidInjection
import dagger.android.HasAndroidInjector
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.replaceFragment
import tech.okcredit.home.R
import tech.okcredit.home.ui.homesearch.HomeConstants.HomeTab
import tech.okcredit.home.ui.homesearch.HomeSearchContract
import tech.okcredit.home.ui.homesearch.HomeSearchFragment

class HomeSearchActivity : OkcActivity(), HasAndroidInjector {

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_search)

        val searchScreen = HomeSearchFragment.newInstance().also {
            it.arguments = this.intent.extras ?: Bundle()
        }
        replaceFragment(
            fragment = searchScreen,
            layoutId = R.id.search_host,
            tag = HomeSearchFragment.TAG
        )
    }

    companion object {
        fun startingIntent(context: Context, tab: HomeTab, accountSelection: Boolean = false): Intent {
            val source = when (tab) {
                HomeTab.CUSTOMER_TAB -> HomeSearchContract.SOURCE.HOME_CUSTOMER_TAB.value
                HomeTab.SUPPLIER_TAB -> HomeSearchContract.SOURCE.HOME_SUPPLIER_TAB.value
            }

            return Intent(context, HomeSearchActivity::class.java)
                .apply {
                    this.putExtra(HomeSearchFragment.ARG_SOURCE, source)
                    this.putExtra(HomeSearchFragment.ACCOUNT_SELECTION, accountSelection)
                }
        }
    }
}
