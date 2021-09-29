package tech.okcredit.home.ui.activity

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.collection.contract.CollectionNavigator
import `in`.okcredit.communication_inappnotification.contract.LocalInAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.ui.local.TapTargetLocal
import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.launch
import tech.okcredit.android.base.extensions.*
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.home.R
import tech.okcredit.home.databinding.HomeActivityBinding
import tech.okcredit.home.ui.activity.HomeActivityContract.*
import tech.okcredit.home.ui.activity.HomeActivityContract.Intent.*
import tech.okcredit.home.ui.activity.HomeActivityContract.ViewEvent.GoToLogin
import tech.okcredit.home.ui.activity.viewpager.BottomMenuItem
import tech.okcredit.home.ui.activity.viewpager.HomeActivityViewPagerAdapter
import tech.okcredit.home.ui.activity.viewpager.NavItem
import tech.okcredit.home.ui.activity.viewpager.NavItem.*
import tech.okcredit.home.ui.analytics.HomeEventTracker
import tech.okcredit.home.ui.business_health_dashboard.BusinessHealthDashboardAnalyticsTracker
import tech.okcredit.web.WebUrlNavigator
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.lang.ref.WeakReference
import javax.inject.Inject

class HomeActivity :
    BaseActivity<State, ViewEvent, Intent>("HomeActivity") {

    private var prevMenuItem: MenuItem? = null
    internal val binding: HomeActivityBinding by viewLifecycleScoped(HomeActivityBinding::inflate)

    @Inject
    internal lateinit var homeEventTracker: Lazy<HomeEventTracker>

    @Inject
    internal lateinit var businessHealthDashboardEventTracker: Lazy<BusinessHealthDashboardAnalyticsTracker>

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var webUrlNavigator: Lazy<WebUrlNavigator>

    @Inject
    lateinit var localInAppNotificationHandler: Lazy<LocalInAppNotificationHandler>

    @Inject
    internal lateinit var collectionNavigator: Lazy<CollectionNavigator>

    private lateinit var homeViewPagerAdapter: HomeActivityViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Base_OKCTheme)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun loadIntent(): UserIntent {
        return Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            Observable.just(OnResume)
        )
    }

    override fun render(state: State) {
    }

    private fun setBottomNavSelected(currentMenuItem: Int) {
        if (prevMenuItem == null) {
            prevMenuItem = binding.bottomNav.menu.findItem(HOME_FRAGMENT.id)
        }
        prevMenuItem?.isChecked = false
        prevMenuItem = binding.bottomNav.menu.findItem(currentMenuItem)
        prevMenuItem?.isChecked = true
    }

    override fun onBackPressed() {
        when {
            binding.homeViewPager.currentItem != 0 -> {
                binding.homeViewPager.currentItem = 0
                binding.bottomNav.menu.findItem(HOME_FRAGMENT.id).isChecked = true
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun gotoLogin() {
        legacyNavigator.get().goToLoginScreen(this)
        finishAffinity()
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is GoToLogin -> gotoLogin()
            ViewEvent.ShowDashboardEducation -> {
                showHomeDashboardEducation(
                    getString(R.string.dashboard_education_primary),
                    getString(R.string.dashboard_education_secondary)
                )
            }
            is ViewEvent.SetupViewPager -> setupViewPager(event.bottomNavItemsList)
            is ViewEvent.GoToWebScreen -> openWebUrl(event.webUrl)
        }
    }

    private fun openWebUrl(webUrl: String) {
        webUrlNavigator.get().openUrl(this, webUrl)
    }

    private fun setupViewPager(bottomNavItemsList: List<BottomMenuItem>) {

        homeViewPagerAdapter = HomeActivityViewPagerAdapter(
            fragmentManager = supportFragmentManager,
            lifeCycle = lifecycle,
            collectionNavigator = collectionNavigator
        )
        binding.homeViewPager.adapter = homeViewPagerAdapter
        binding.homeViewPager.isUserInputEnabled = false

        val bottomNavItems = arrayListOf<NavItem>()

        binding.bottomNav.menu.clear()

        bottomNavItemsList.forEach { bottomMenuItem ->
            val item = binding.bottomNav.menu.add(
                Menu.NONE, // Menu Group
                bottomMenuItem.navItem.id, // Unique Id
                Menu.NONE, // Category Order for sub-menu
                getString(bottomMenuItem.stringId) // Title
            )
            item.setIcon(bottomMenuItem.drawableId)
            MenuItemCompat.setContentDescription(item, getString(bottomMenuItem.contentDescriptionId))

            bottomNavItems.add(bottomMenuItem.navItem)
        }

        homeViewPagerAdapter.setData(bottomNavItems)

        binding.bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                HOME_FRAGMENT.id -> {
                    binding.homeViewPager.currentItem = bottomNavItems.indexOf(HOME_FRAGMENT)
                    homeEventTracker.get().trackBottomNavigationClickEvents("Ledger")
                }
                HOME_PAYMENTS.id -> {
                    binding.homeViewPager.currentItem = bottomNavItems.indexOf(HOME_PAYMENTS)
                    homeEventTracker.get().trackBottomNavigationClickEvents("Payments")
                }
                DASHBOARD_FRAGMENT.id -> {
                    binding.homeViewPager.currentItem = bottomNavItems.indexOf(DASHBOARD_FRAGMENT)
                    homeEventTracker.get().trackBottomNavigationClickEvents("Dashboard")
                    homeEventTracker.get().trackDashboardIconClicked("Bottom Navigation")
                }
                HOME_MENU_FRAGMENT.id -> {
                    binding.homeViewPager.currentItem = bottomNavItems.indexOf(HOME_MENU_FRAGMENT)
                    homeEventTracker.get().trackBottomNavigationClickEvents("Settings")
                }
                HOME_HELP_SUPPORT_FRAGMENT.id -> {
                    binding.homeViewPager.currentItem = bottomNavItems.indexOf(HOME_HELP_SUPPORT_FRAGMENT)
                    homeEventTracker.get().trackBottomNavigationClickEvents("Help")
                }
            }

            setBottomNavSelected(menuItem.itemId)
            return@setOnNavigationItemSelectedListener false
        }
    }

    private fun showHomeDashboardEducation(primaryText: String, secondaryText: String) {
        val item = binding.bottomNav.menu.findItem(DASHBOARD_FRAGMENT.id)
        tracker.get().trackInAppDisplayed("home_dashboard_education", PropertyValue.HOME_PAGE)

        lifecycleScope.launch {
            localInAppNotificationHandler.get()
                .generateTapTarget(
                    weakScreen = WeakReference(this@HomeActivity),
                    tapTarget = TapTargetLocal(
                        targetView = WeakReference(null),
                        targetViewId = item.itemId,
                        screenName = label,
                        title = primaryText,
                        subtitle = secondaryText,
                        titleGravity = Gravity.END,
                        subtitleGravity = Gravity.END,
                        listener = { _, state ->
                            when (state) {
                                MaterialTapTargetPrompt.STATE_FOCAL_PRESSED -> {
                                    tracker.get().trackInAppClickedV2(
                                        "home_dashboard_education",
                                        PropertyValue.HOME_PAGE, PropertyValue.TRUE
                                    )
                                }
                                MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED -> {
                                    tracker.get()
                                        .trackInAppClickedV2(
                                            "home_dashboard_education",
                                            PropertyValue.HOME_PAGE,
                                            PropertyValue.FALSE
                                        )
                                }
                                MaterialTapTargetPrompt.STATE_BACK_BUTTON_PRESSED -> {
                                    tracker.get().trackInAppCleared(
                                        "home_dashboard_education",
                                        PropertyValue.HOME_PAGE, PropertyValue.TRUE
                                    )
                                }
                            }
                        }
                    )
                )
        }
    }

    companion object {

        const val EXTRA_WEB_URL = "web_url"

        @JvmStatic
        fun getIntent(
            context: Context,
            webUrl: String? = null,
        ): android.content.Intent {
            return android.content.Intent(context, HomeActivity::class.java).apply {
                webUrl?.let { putExtra(EXTRA_WEB_URL, webUrl) }
            }
        }
    }
}
