package tech.okcredit.android.referral.ui

import `in`.okcredit.referral.contract.ReferralNavigator
import android.content.Context
import androidx.fragment.app.FragmentManager
import tech.okcredit.android.referral.ui.referral_in_app_bottomsheet.ReferralInAppBottomSheet
import javax.inject.Inject

class ReferralNavigatorImpl @Inject constructor() : ReferralNavigator {
    override fun goToReferralScreen(context: Context) {
        ReferralActivity.start(context)
    }

    override fun showReferralInAppBottomSheet(fragmentManager: FragmentManager) {
        ReferralInAppBottomSheet.show(fragmentManager)
    }
}
