package tech.okcredit.home.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import tech.okcredit.home.ui.customer_tab.CustomerTabFragment
import tech.okcredit.home.ui.homesearch.HomeConstants.HomeTab
import tech.okcredit.home.ui.supplier_tab.SupplierTabFragment

class HomePagerAdapter(
    fragmentManager: FragmentManager,
    lifeCycle: Lifecycle,
    private val fragmentOrder: List<HomeTab>,
) : FragmentStateAdapter(fragmentManager, lifeCycle) {
    override fun getItemCount(): Int {
        return fragmentOrder.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (fragmentOrder[position]) {
            HomeTab.CUSTOMER_TAB -> CustomerTabFragment.newInstance()
            HomeTab.SUPPLIER_TAB -> SupplierTabFragment.newInstance()
        }
    }
}
