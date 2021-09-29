package tech.okcredit.home.ui.customer_tab.view

import android.view.View
import com.airbnb.epoxy.EpoxyHolder
import kotlinx.android.synthetic.main.referral_signup_reward.view.*
import tech.okcredit.home.R

class AddCustomerRewardViewHolder : EpoxyHolder() {

    lateinit var itemView: View

    override fun bindView(itemView: View) {
        this.itemView = itemView
    }

    fun showAddCustomerReward() {
        itemView.apply {
            rewardSubtitleTextView.setText(R.string.add_customer_rewards_subtitle)
            title.setText(R.string.add_customer_rewards_title)
            step1TextView.setText(R.string.numeric_1)
        }
    }

    fun showAddTransactionReward() {
        itemView.apply {
            rewardSubtitleTextView.setText(R.string.add_transaction_rewards_subtitle)
            title.setText(R.string.add_transaction_rewards_title)
            step1TextView.setText(R.string.tick_mark)
        }
    }
}
