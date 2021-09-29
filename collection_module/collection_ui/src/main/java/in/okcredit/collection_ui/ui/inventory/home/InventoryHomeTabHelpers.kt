package `in`.okcredit.collection_ui.ui.inventory.home

import `in`.okcredit.collection_ui.R
import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import tech.okcredit.android.base.extensions.getColorFromAttr

object InventoryHomeTabHelpers {
    fun setupTabLayout(
        tabLayout: TabLayout,
        view_pager: ViewPager2,
        activity: FragmentActivity,
        tabOrderList: List<InventoryHomeTab>,
    ) {
        val tabTitles = tabOrderList.map {
            when (it) {
                InventoryHomeTab.BILL_TAB -> activity.getString(R.string.inventory_bills).uppercase()
                InventoryHomeTab.ITEM_TAB -> activity.getString(R.string.inventory_items).uppercase()
            }
        }

        TabLayoutMediator(tabLayout, view_pager) { tab, position ->
            tab.text = tabTitles[position]
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
