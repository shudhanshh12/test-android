package `in`.okcredit.referral.contract

import android.content.Context
import androidx.fragment.app.FragmentManager

interface ReferralNavigator {
    fun goToReferralScreen(context: Context)

    fun showReferralInAppBottomSheet(fragmentManager: FragmentManager)
}
