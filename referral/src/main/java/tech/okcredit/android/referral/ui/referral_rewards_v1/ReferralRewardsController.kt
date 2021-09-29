package tech.okcredit.android.referral.ui.referral_rewards_v1

import `in`.okcredit.fileupload.usecase.IImageLoader
import com.airbnb.epoxy.AsyncEpoxyController
import tech.okcredit.android.referral.data.ReferredMerchant
import tech.okcredit.android.referral.ui.ReferralActivity
import tech.okcredit.android.referral.ui.referral_rewards_v1.views.ReferralRewardsItemModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class ReferralRewardsController @Inject constructor(
    private val imageLoader: IImageLoader,
    @ViewModelParam(ReferralActivity.ARG_TARGETED_REFERRAL_PHONE_NUMBER)
    private val scrollToPhoneNumberOnce: String?,
) : AsyncEpoxyController() {

    private val referredMerchants = mutableListOf<ReferredMerchant>()
    var onNotifyClicked: ((String) -> Unit)? = null
    var onEarnMoreClicked: (() -> Unit)? = null

    fun setMerchants(referredMerchants: List<ReferredMerchant>) {
        this.referredMerchants.clear()
        this.referredMerchants.addAll(referredMerchants)
        requestModelBuild()
    }

    override fun buildModels() {
        referredMerchants.forEach {
            ReferralRewardsItemModel(
                it,
                imageLoader,
                onNotifyClicked,
                onEarnMoreClicked,
                isExpandedInitially = (
                    scrollToPhoneNumberOnce != null &&
                        scrollToPhoneNumberOnce == it.phoneNumber
                    )
            ).apply {
                id(modelCountBuiltSoFar)
            }.addTo(this)
        }
    }
}
