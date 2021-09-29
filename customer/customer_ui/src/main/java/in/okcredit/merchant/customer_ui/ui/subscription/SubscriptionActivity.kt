package `in`.okcredit.merchant.customer_ui.ui.subscription

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.data.server.model.response.Subscription
import `in`.okcredit.merchant.customer_ui.databinding.SubscriptionActivityBinding
import `in`.okcredit.merchant.customer_ui.ui.subscription.SubscriptionEventTracker.Companion.RELATIONSHIP_PAGE
import `in`.okcredit.merchant.customer_ui.ui.subscription.detail.SubscriptionDetailContract
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class SubscriptionActivity : OkcActivity() {

    private val binding: SubscriptionActivityBinding by viewLifecycleScoped(SubscriptionActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (intent.hasExtra("subscription")) {
            val subscription = intent.getParcelableExtra("subscription") as Subscription?
            val customerId = intent.getStringExtra("customer_id")

            if (subscription != null) {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.subscription_list, true)
                    .build()
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(
                    R.id.action_subscription_detail,
                    Bundle().apply {
                        putParcelable(SubscriptionDetailContract.ARG_SUBSCRIPTION_OBJECT, subscription)
                        putString(SubscriptionDetailContract.ARG_SUBSCRIPTION_ID, subscription.id)
                        putString(SubscriptionDetailContract.ARG_SOURCE, RELATIONSHIP_PAGE)
                        putString(SubscriptionDetailContract.ARG_CUSTOMER_ID, customerId)
                    },
                    navOptions
                )
            }
        }
    }

    companion object {
        @JvmStatic
        fun getIntent(context: Context, customerId: String) = Intent(context, SubscriptionActivity::class.java).apply {
            putExtra("customer_id", customerId)
        }

        @JvmStatic
        fun getIntent(context: Context, subscription: Subscription, customerId: String) =
            Intent(context, SubscriptionActivity::class.java).apply {
                putExtra("subscription", subscription)
                putExtra("customer_id", customerId)
            }
    }
}
