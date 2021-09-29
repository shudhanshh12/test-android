package tech.okcredit.home.ui.activity.viewpager

import `in`.okcredit.collection.contract.CollectionNavigator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import dagger.Lazy
import tech.okcredit.help.helpHome.HelpHomeFragment
import tech.okcredit.home.ui.activity.viewpager.NavItem.*
import tech.okcredit.home.ui.dashboard.DashboardFragment
import tech.okcredit.home.ui.home.HomeFragment
import tech.okcredit.home.ui.menu.HomeMenuFragment

class HomeActivityViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifeCycle: Lifecycle,
    private val collectionNavigator: Lazy<CollectionNavigator>,
) : FragmentStateAdapter(fragmentManager, lifeCycle) {

    private val bottomNavItems = mutableListOf<NavItem>()

    fun setData(newItems: List<NavItem>) {
        this.bottomNavItems.clear()
        this.bottomNavItems.addAll(newItems)
    }

    override fun getItemCount(): Int {
        return bottomNavItems.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (bottomNavItems[position]) {
            HOME_FRAGMENT -> HomeFragment.newInstance()

            DASHBOARD_FRAGMENT -> DashboardFragment.newInstance()

            HOME_MENU_FRAGMENT -> HomeMenuFragment.newInstance()

            HOME_PAYMENTS -> collectionNavigator.get().paymentsContainerFragment()

            HOME_HELP_SUPPORT_FRAGMENT -> HelpHomeFragment.newInstance(showBackButton = false)
        }
    }
}
