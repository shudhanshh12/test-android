package tech.okcredit.applock

import `in`.okcredit.shared.base.BaseScreen
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import tech.okcredit.android.base.activity.OkcActivity

class AppLockActivityV2 : OkcActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_lock)
        intent.extras?.let {
            val screen = it.getString(SCREEN)
            if (screen.isNullOrEmpty().not()) {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.applock_nav_host) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(Uri.parse(screen))
            }
        }
    }

    companion object {
        const val SCREEN = "SCREEN"
        const val ENTRY = "ENTRY"
    }

    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.applock_nav_host)
        if (navHostFragment?.childFragmentManager != null) {
            val fr: Fragment = navHostFragment.childFragmentManager.fragments[0]
            if (fr is BaseScreen<*>) {
                if (!fr.onBackPressed()) {
                    super.onBackPressed()
                }
            } else {
                super.onBackPressed()
            }
        }
    }
}
