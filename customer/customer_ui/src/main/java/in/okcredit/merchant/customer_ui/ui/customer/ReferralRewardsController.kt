package `in`.okcredit.merchant.customer_ui.ui.customer

import `in`.okcredit.shared.deeplink.InternalDeeplinkNavigationDelegator
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import `in`.okcredit.shared.referral_views.referralTargetBanner
import com.airbnb.epoxy.AsyncEpoxyController
import dagger.Lazy
import javax.inject.Inject

class ReferralRewardsController @Inject constructor(
    private val internalDeeplinkNavigator: Lazy<InternalDeeplinkNavigationDelegator>,
) : AsyncEpoxyController() {
    private var referralTargetBanner: ReferralTargetBanner? = null
    var onReferralTransactionInitiated: (() -> Unit)? = null
    var onReferralCloseClicked: (() -> Unit)? = null

    fun setReferralTargets(content: ReferralTargetBanner?) {
        this.referralTargetBanner = content
        cancelPendingModelBuild()
        requestModelBuild()
    }

    override fun buildModels() {
        referralTargetBanner?.let {
            referralTargetBanner {
                id("referralTargetBanner")
                target(it)
                deeplinkNavigator(internalDeeplinkNavigator.get())
                transactionListener(onReferralTransactionInitiated)
                closeListener(onReferralCloseClicked)
            }
        }
    }
}
