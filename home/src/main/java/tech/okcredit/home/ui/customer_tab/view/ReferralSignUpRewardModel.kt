package tech.okcredit.home.ui.customer_tab.view

import androidx.annotation.LayoutRes
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import tech.okcredit.home.R

@EpoxyModelClass
open class ReferralSignUpRewardModel : EpoxyModelWithHolder<AddCustomerRewardViewHolder>() {

    enum class RewardType {
        ADD_CUSTOMER,
        ADD_TRANSACTION
    }

    @EpoxyAttribute
    var type: RewardType = RewardType.ADD_CUSTOMER

    @LayoutRes
    override fun getDefaultLayout() = R.layout.referral_signup_reward

    override fun bind(holder: AddCustomerRewardViewHolder) {
        when (type) {
            RewardType.ADD_CUSTOMER -> holder.showAddCustomerReward()
            RewardType.ADD_TRANSACTION -> holder.showAddTransactionReward()
        }
    }

    override fun createNewHolder() = AddCustomerRewardViewHolder()
}
