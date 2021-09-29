package tech.okcredit.home.ui.home.helpers

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import tech.okcredit.android.base.extensions.getColorFromAttr
import tech.okcredit.home.R
import tech.okcredit.home.ui.analytics.HomeEventTracker
import tech.okcredit.home.ui.homesearch.HomeConstants
import tech.okcredit.home.ui.payables_onboarding.HomeTabOrderList

object HomeTabHelpers {
    fun setupTabLayout(
        tabLayout: TabLayout,
        view_pager: ViewPager2,
        activity: FragmentActivity?,
        homeTabOrderList: HomeTabOrderList,
        homeEventTracker: HomeEventTracker,
        isExperimentEnabled: Boolean?
    ) {
        val tabTitles = homeTabOrderList.list.map {
            when (it) {
                HomeConstants.HomeTab.CUSTOMER_TAB -> activity?.getString(R.string.customer)?.toUpperCase()
                HomeConstants.HomeTab.SUPPLIER_TAB -> activity?.getString(R.string.suppliers)?.toUpperCase()
            }
        }

        TabLayoutMediator(tabLayout, view_pager) { tab, position ->
            tab.text = tabTitles[position]
            view_pager.setCurrentItem(tab.position, true)
            val tvTitle = tab.view.findViewById<TextView>(R.id.tvTabTitle)
            when (position) {
                0 -> {
                    setTabSelectedState(tvTitle, activity)
                }
                else -> {
                    setTabUnSelectedState(tvTitle, activity)
                }
            }
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val view = tab.customView
                val tvTitle = view?.findViewById<TextView>(R.id.tvTabTitle)
                setTabUnSelectedState(tvTitle, activity)
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                val view = tab.customView
                val tvTitle = view?.findViewById<TextView>(R.id.tvTabTitle)
                setTabSelectedState(tvTitle, activity)

                homeEventTracker.trackHomeTabClicked(
                    when (homeTabOrderList.list[tab.position]) {
                        HomeConstants.HomeTab.SUPPLIER_TAB -> HomeEventTracker.Objects.SUPPLIER_TAB
                        HomeConstants.HomeTab.CUSTOMER_TAB -> HomeEventTracker.Objects.CUSTOMER_TAB
                    },
                    isExperimentEnabled
                )
            }
        })
    }

    internal fun setTabSelectedState(tvTitle: TextView?, activity: FragmentActivity?) {
        tvTitle?.setTextColor(activity!!.getColorFromAttr(R.attr.colorPrimary))
    }

    internal fun setTabUnSelectedState(tvTitle: TextView?, activity: FragmentActivity?) {
        tvTitle?.setTextColor(ContextCompat.getColor(activity as Context, R.color.grey600))
    }
}
