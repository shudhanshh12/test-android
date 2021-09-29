package `in`.okcredit.navigation

import `in`.okcredit.R
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.databinding.NavigationAcitivityBinding
import `in`.okcredit.shared.base.BaseScreen
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import dagger.Lazy
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.findFragmentById
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import javax.inject.Inject

class NavigationActivity : OkcActivity() {

    @Inject
    lateinit var tracker: Lazy<Tracker>

    private val binding: NavigationAcitivityBinding by viewLifecycleScoped(NavigationAcitivityBinding::inflate)

    companion object {

        @Deprecated(message = "Bring fragment inside global navigation graph and use actions instead")
        private const val ARG_DESTINATION = "destination"

        private const val DESTINATION_HOME = "home"
        private const val DESTINATION_LAUNCHER = "launcher"
        private const val ARG_PRE_NETWORK_ONBOARDING_NUDGE = "pre_network_onboarding_nudge"

        @Deprecated(message = "Bring fragment inside global navigation graph and use actions instead")
        @JvmStatic
        fun homeScreenIntent(context: Context) =
            Intent(context, NavigationActivity::class.java).apply {
                putExtra(
                    ARG_DESTINATION,
                    DESTINATION_HOME
                )
                action = Intent.ACTION_VIEW
            }

        @Deprecated(message = "Bring fragment inside global navigation graph and use actions instead")
        @JvmStatic
        fun navigateToHomeScreen(
            activity: Activity,
        ) {
            activity.startActivity(
                homeScreenIntent(
                    activity
                )
            )
        }

        @Deprecated(message = "Bring fragment inside global navigation graph and use actions instead")
        @JvmStatic
        fun launcherScreenIntent(context: Context) =
            Intent(context, NavigationActivity::class.java).apply {
                putExtra(
                    ARG_DESTINATION,
                    DESTINATION_LAUNCHER
                )
                action = Intent.ACTION_VIEW
            }

        @Deprecated(message = "Bring fragment inside global navigation graph and use actions instead")
        @JvmStatic
        fun navigateToLauncherScreen(activity: Activity) {
            activity.startActivity(
                launcherScreenIntent(
                    activity
                )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Base_OKCTheme)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpNavigation()
    }

    // This temporary. Once all fragment come under global navigation graph, we need to remove this and use navigation
    // component for navigating to home
    @Deprecated(message = "Bring fragment inside global navigation graph and use actions instead")
    private fun setUpNavigation() {
        val navHostFragment = findFragmentById(R.id.navHostFragment) as NavHostFragment
        val graphInflater = navHostFragment.navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.global_navigation_graph)
        val navController = navHostFragment.navController
        navGraph.startDestination = R.id.onboarding
        navController.graph = navGraph

        if (intent.extras?.getString(ARG_DESTINATION).equals(
                DESTINATION_HOME, true
            )
        ) {
            navController.navigate(R.id.home, intent.extras)
            finish()
        }
    }

    override fun onSupportNavigateUp() = findNavController(this, R.id.navHostFragment).navigateUp()

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        findNavController(this, R.id.navHostFragment).handleDeepLink(intent)
    }

    override fun onBackPressed() {
        val navHostFragment = findFragmentById(R.id.navHostFragment) as NavHostFragment
        val fragment = navHostFragment.childFragmentManager.fragments[0]
        if (fragment is BaseScreen<*>) {
            if (!fragment.onBackPressed()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}
