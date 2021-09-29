package tech.okcredit.android.referral.ui

import `in`.okcredit.collection.contract.MerchantDestinationListener
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.referral.R
import tech.okcredit.android.referral.databinding.ReferralActivityBinding

class ReferralActivity : OkcActivity(), MerchantDestinationListener {

    companion object {

        const val ARG_TARGETED_REFERRAL_PHONE_NUMBER = "targeted referral phone number"

        fun getIntent(context: Context) =
            Intent(context, ReferralActivity::class.java)

        fun start(context: Context) {
            val intent = Intent(context, ReferralActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val binding: ReferralActivityBinding by viewLifecycleScoped(ReferralActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.referral_screen_container, NavHostFragment.create(R.navigation.referral_flow))
            .commit()
    }

    override fun onAccountAddedSuccessfully(eta: Long) {}

    override fun onCancelled() {}
}
