package `in`.okcredit.collection_ui.ui.inventory.home

import `in`.okcredit.collection_ui.ui.inventory.bills.InventoryBillFragment
import `in`.okcredit.collection_ui.ui.inventory.items.InventoryItemFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class InventoryHomePagerAdapter(
    fragmentManager: FragmentManager,
    lifeCycle: Lifecycle,
    private val fragmentOrder: List<InventoryHomeTab>,
) : FragmentStateAdapter(fragmentManager, lifeCycle) {
    override fun getItemCount(): Int {
        return fragmentOrder.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (fragmentOrder[position]) {
            InventoryHomeTab.BILL_TAB -> InventoryBillFragment()
            InventoryHomeTab.ITEM_TAB -> InventoryItemFragment()
        }
    }
}

enum class InventoryHomeTab {
    BILL_TAB,
    ITEM_TAB
}
