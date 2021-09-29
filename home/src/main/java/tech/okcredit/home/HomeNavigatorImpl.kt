package tech.okcredit.home

import `in`.okcredit.home.HomeNavigator
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import tech.okcredit.android.base.extensions.navigate
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileDialog
import tech.okcredit.home.ui.activity.HomeSearchActivity
import tech.okcredit.home.ui.homesearch.HomeConstants
import tech.okcredit.home.ui.settings.SettingsActivity
import javax.inject.Inject

class HomeNavigatorImpl @Inject constructor() : HomeNavigator {

    override fun getSettingsIntent(context: Context) = Intent(context, SettingsActivity::class.java)

    override fun home(fragment: Fragment) {
        fragment.navigate(R.id.home_screen_flow)
    }

    override fun goToHomeSearchScreenForResult(fragment: Fragment, requestCode: Int, isCustomerSelection: Boolean) {
        fragment.startActivityForResult(
            HomeSearchActivity.startingIntent(
                context = fragment.requireContext(),
                tab = if (isCustomerSelection) HomeConstants.HomeTab.CUSTOMER_TAB else HomeConstants.HomeTab.SUPPLIER_TAB,
                accountSelection = true
            ),
            requestCode
        )
    }

    override fun goToCustomerProfileDialog(fragmentManager: FragmentManager, customerId: String, screen: String?) {
        CustomerProfileDialog.showDialog(fragmentManager, customerId, screen)
    }
}
