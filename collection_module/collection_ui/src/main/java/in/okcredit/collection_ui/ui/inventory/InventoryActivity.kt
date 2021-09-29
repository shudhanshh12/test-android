package `in`.okcredit.collection_ui.ui.inventory

import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.ActivityInventoryBinding
import `in`.okcredit.collection_ui.ui.inventory.create_bill.InventoryItemListFragment
import `in`.okcredit.collection_ui.ui.inventory.home.InventoryHomeFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.addFragment
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class InventoryActivity : OkcActivity(), InventoryNavigationListener {

    companion object {
        @JvmStatic
        fun getIntent(context: Context) = Intent(context, InventoryActivity::class.java)
    }

    private val binding: ActivityInventoryBinding by viewLifecycleScoped(ActivityInventoryBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun gotoInventoryItemListScreen() {
        supportFragmentManager.addFragment(
            fragment = InventoryItemListFragment(),
            holder = R.id.fcvRoot,
            addToBackStack = true,
        )
    }

    override fun loadBills() {
        val fragments = supportFragmentManager.fragments
        fragments.forEach {
            if (it is InventoryHomeFragment) {
                it.loadBills()
            }
        }
    }
}

interface InventoryNavigationListener {
    fun gotoInventoryItemListScreen()
    fun loadBills()
}
