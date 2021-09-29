package tech.okcredit.home.ui.business_health_dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.android.AndroidInjection
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.replaceFragment
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.home.R
import tech.okcredit.home.databinding.ActivityBusinessHealthDashboardBinding

class BusinessHealthDashboardActivity : OkcActivity() {

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, BusinessHealthDashboardActivity::class.java)
        }
    }

    private val binding: ActivityBusinessHealthDashboardBinding
        by viewLifecycleScoped(ActivityBusinessHealthDashboardBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        replaceFragment(
            fragment = BusinessHealthDashboardFragment.newInstance(),
            layoutId = R.id.frame_layout_container,
            tag = BusinessHealthDashboardFragment.TAG
        )
    }
}
