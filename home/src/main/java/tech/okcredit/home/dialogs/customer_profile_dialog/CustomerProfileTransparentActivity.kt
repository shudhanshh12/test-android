package tech.okcredit.home.dialogs.customer_profile_dialog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.home.databinding.CustomerProfileActivityBinding

class CustomerProfileTransparentActivity : OkcActivity() {

    companion object {

        const val CUSTOMER_ID = "customer_id"

        fun getIntent(context: Context, customerId: String?): Intent {
            val tempCustomerId = customerId ?: throw IllegalStateException("customer_id is null")
            return Intent(context, CustomerProfileTransparentActivity::class.java).apply {
                putExtra(CUSTOMER_ID, tempCustomerId)
            }
        }
    }

    private val binding: CustomerProfileActivityBinding by viewLifecycleScoped(CustomerProfileActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val customerId = intent?.getStringExtra(CUSTOMER_ID)
        if (customerId != null) {
            CustomerProfileDialog.showDialog(supportFragmentManager, customerId)
        }
    }
}
