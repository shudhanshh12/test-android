package `in`.okcredit.home

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

interface HomeNavigator {

    fun getSettingsIntent(context: Context): Intent

    fun home(fragment: Fragment)

    fun goToHomeSearchScreenForResult(fragment: Fragment, requestCode: Int, isCustomerSelection: Boolean = false)

    fun goToCustomerProfileDialog(fragmentManager: FragmentManager, customerId: String, screen: String? = null)
}
