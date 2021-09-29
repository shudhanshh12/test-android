package tech.okcredit.home.ui.customer_tab.view

import androidx.annotation.LayoutRes
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import tech.okcredit.home.R

@EpoxyModelClass
open class ReferralSecondTransactionRewardModel : EpoxyModelWithHolder<SecondTransactionRewardViewHolder>() {

    @LayoutRes
    override fun getDefaultLayout() = R.layout.referral_second_transaction_reward

    override fun createNewHolder() = SecondTransactionRewardViewHolder()
}
