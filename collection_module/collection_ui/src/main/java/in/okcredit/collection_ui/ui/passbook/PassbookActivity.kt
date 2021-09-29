package `in`.okcredit.collection_ui.ui.passbook

import `in`.okcredit.collection.contract.MerchantDestinationListener
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.ActivityPassbookBinding
import `in`.okcredit.collection_ui.ui.passbook.detail.PaymentDetailFragment
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentNavigationListener
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsFragment
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsFragment.Companion.SOURCE_MERCHANT_QR
import android.content.Context
import android.content.Intent
import android.os.Bundle
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.addFragment
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class PassbookActivity : OkcActivity(), OnlinePaymentNavigationListener, MerchantDestinationListener {

    private val binding: ActivityPassbookBinding by viewLifecycleScoped(ActivityPassbookBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        if (savedInstanceState == null) {
            val paymentId = intent.getStringExtra(EXTRA_PAYMENT_ID)
            val customerId = intent.getStringExtra(EXTRA_CUSTOMER_ID)
            val source = intent.getStringExtra(EXTRA_SOURCE) ?: SOURCE_MERCHANT_QR
            if (paymentId.isNullOrEmpty()) {
                showPaymentsList()
            } else {
                val fragment = PaymentDetailFragment.getInstance(paymentId, customerId, source)
                supportFragmentManager.addFragment(
                    fragment = fragment,
                    holder = R.id.fragmentHolder,
                    addToBackStack = false,
                )
            }
        }
    }

    private fun showPaymentsList() {
        val onlinePaymentsFragment = OnlinePaymentsFragment.newInstance(source = SOURCE_MERCHANT_QR)
        supportFragmentManager.addFragment(
            fragment = onlinePaymentsFragment,
            holder = R.id.fragmentHolder,
            addToBackStack = false,
        )
    }

    override fun moveToPaymentDetail(paymentId: String, customerId: String?) {
        val fragment = PaymentDetailFragment.getInstance(paymentId, customerId, SOURCE_MERCHANT_QR)
        supportFragmentManager.addFragment(
            fragment = fragment,
            holder = R.id.fragmentHolder,
            addToBackStack = true,
        )
    }

    override fun onAccountAddedSuccessfully(eta: Long) {
        val currentFragment: PaymentDetailFragment? =
            supportFragmentManager.findFragmentById(R.id.fragmentHolder) as PaymentDetailFragment?
        currentFragment?.onMerchantDestinationAdded()
    }

    override fun onCancelled() {}

    companion object {

        const val EXTRA_PAYMENT_ID = "extra_payment_id"
        const val EXTRA_CUSTOMER_ID = "extra_customer_id"
        const val EXTRA_SOURCE = "extra_source"

        fun getPaymentDetailIntent(context: Context, paymentId: String, customerId: String?, source: String): Intent {
            return Intent(context, PassbookActivity::class.java)
                .putExtra(EXTRA_PAYMENT_ID, paymentId)
                .putExtra(EXTRA_CUSTOMER_ID, customerId)
                .putExtra(EXTRA_SOURCE, source)
        }
    }
}
