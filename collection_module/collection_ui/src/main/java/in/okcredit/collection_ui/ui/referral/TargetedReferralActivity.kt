package `in`.okcredit.collection_ui.ui.referral

import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.ActivityReferralsBinding
import `in`.okcredit.collection_ui.ui.referral.education.ReferralEducationFragment
import `in`.okcredit.collection_ui.ui.referral.invite_list.ReferralInviteListFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.replaceFragment
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class TargetedReferralActivity : OkcActivity(), NavigationListener {

    private val binding: ActivityReferralsBinding by viewLifecycleScoped(ActivityReferralsBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val customerId = intent.getStringExtra(EXTRA_CUSTOMER_ID_FROM_LEDGER)
            val screen = intent.getIntExtra(EXTRA_SCREEN, REFERRAL_EDUCATION_SCREEN)
            if (screen == REFERRAL_INVITE_LIST) {
                moveToReferralInvite(customerId)
            } else {
                moveToReferralEducation(customerId)
            }
        }
    }

    private fun moveToReferralEducation(customerId: String?) {
        val fragment = ReferralEducationFragment().apply {
            Bundle().apply {
                putString(EXTRA_CUSTOMER_ID_FROM_LEDGER, customerId)
            }
        }
        supportFragmentManager.replaceFragment(fragment, holder = R.id.fragmentHolder, addToBackStack = false)
    }

    override fun moveToReferralInvite(customerId: String?) {
        val fragment = ReferralInviteListFragment().apply {
            Bundle().apply {
                putString(EXTRA_CUSTOMER_ID_FROM_LEDGER, customerId)
            }
        }
        supportFragmentManager.replaceFragment(fragment, holder = R.id.fragmentHolder, addToBackStack = false)
    }

    companion object {
        fun getIntent(context: Context, screen: Int, customerId: String? = null,): Intent {
            return Intent(context, TargetedReferralActivity::class.java)
                .putExtra(EXTRA_CUSTOMER_ID_FROM_LEDGER, customerId)
                .putExtra(EXTRA_SCREEN, screen)
        }

        const val EXTRA_SCREEN = "screen"
        const val EXTRA_CUSTOMER_ID_FROM_LEDGER = "customer_id_frm_ledger"

        const val REFERRAL_EDUCATION_SCREEN = 1
        const val REFERRAL_INVITE_LIST = 2
    }
}

interface NavigationListener {
    fun moveToReferralInvite(customerId: String?)
}
