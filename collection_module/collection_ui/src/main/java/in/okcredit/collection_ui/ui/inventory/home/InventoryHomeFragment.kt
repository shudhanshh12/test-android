package `in`.okcredit.collection_ui.ui.inventory.home

import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.PropertyValue.BILLS
import `in`.okcredit.collection_ui.analytics.InventoryEventTracker.PropertyValue.ITEMS
import `in`.okcredit.collection_ui.databinding.FragmentInventoryHomeBinding
import `in`.okcredit.collection_ui.ui.inventory.InventoryNavigationListener
import `in`.okcredit.collection_ui.ui.inventory.bills.InventoryBillFragment
import `in`.okcredit.collection_ui.ui.inventory.items.InventoryItemFragment
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import javax.inject.Inject

class InventoryHomeFragment : Fragment(R.layout.fragment_inventory_home) {

    internal val binding: FragmentInventoryHomeBinding by viewLifecycleScoped(FragmentInventoryHomeBinding::bind)

    private lateinit var onPageChangedCallback: ViewPager2.OnPageChangeCallback
    private var adapter: InventoryHomePagerAdapter? = null
    private var inventoryNavigationListener: InventoryNavigationListener? = null

    private var currentPage = InventoryHomeTab.BILL_TAB

    @Inject
    lateinit var inventoryEventTracker: Lazy<InventoryEventTracker>

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if (context is `in`.okcredit.collection_ui.ui.inventory.InventoryNavigationListener) {
            inventoryNavigationListener = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = listOf(
            InventoryHomeTab.BILL_TAB,
            InventoryHomeTab.ITEM_TAB,
        )

        setupViewPager(list)

        setupTabLayout(list)

        setClickListener()
    }

    private fun setClickListener() {
        binding.buttonAdd.setOnClickListener {
            if (currentPage == InventoryHomeTab.BILL_TAB) {
                inventoryEventTracker.get().trackBillingCreateNewBillClicked()
                inventoryNavigationListener?.gotoInventoryItemListScreen()
            } else {
                inventoryEventTracker.get().trackBillingAddNewItemClicked()
                getItemFragment()?.addItem()
            }
        }
    }

    private fun setupViewPager(inventoryHomeTab: List<InventoryHomeTab>) {
        adapter = InventoryHomePagerAdapter(
            childFragmentManager,
            lifecycle,
            listOf(
                InventoryHomeTab.BILL_TAB,
                InventoryHomeTab.ITEM_TAB,
            ),
        )
        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.adapter = adapter

        onPageChangedCallback = object : ViewPager2.OnPageChangeCallback() {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                when (inventoryHomeTab[position]) {
                    InventoryHomeTab.BILL_TAB -> {
                        inventoryEventTracker.get().trackBillingTabChanged(BILLS)
                        currentPage = InventoryHomeTab.BILL_TAB
                        binding.buttonAdd.text = getString(R.string.inventory_create_new_bill)
                    }
                    InventoryHomeTab.ITEM_TAB -> {
                        inventoryEventTracker.get().trackBillingTabChanged(ITEMS)
                        currentPage = InventoryHomeTab.ITEM_TAB
                        binding.buttonAdd.text = getString(R.string.inventory_create_new_item)
                    }
                }
            }
        }
        binding.viewPager.registerOnPageChangeCallback(onPageChangedCallback)
    }

    private fun setupTabLayout(inventoryHomeTab: List<InventoryHomeTab>) {
        InventoryHomeTabHelpers.setupTabLayout(
            binding.tabLayout,
            binding.viewPager,
            requireActivity(),
            inventoryHomeTab,
        )
    }

    private fun getItemFragment() = this.childFragmentManager
        .findFragmentByTag("f" + binding.viewPager.currentItem) as? InventoryItemFragment

    private fun getBillFragment() = this.childFragmentManager
        .findFragmentByTag("f" + binding.viewPager.currentItem) as? InventoryBillFragment

    fun loadBills() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            val billFrag = getBillFragment()
            billFrag?.loadBills()
        }
    }
}
