package `in`.okcredit.collection_ui.ui.inventory

import `in`.okcredit.collection_ui.ui.inventory.add_item_dialog.AddInventoryItemBottomSheetDialog
import `in`.okcredit.collection_ui.ui.inventory.bills.InventoryBillFragment
import `in`.okcredit.collection_ui.ui.inventory.bills.InventoryBillModule
import `in`.okcredit.collection_ui.ui.inventory.create_bill.InventoryItemListFragment
import `in`.okcredit.collection_ui.ui.inventory.create_bill.InventoryItemListModule
import `in`.okcredit.collection_ui.ui.inventory.home.InventoryHomeFragment
import `in`.okcredit.collection_ui.ui.inventory.items.InventoryItemFragment
import `in`.okcredit.collection_ui.ui.inventory.items.InventoryItemModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class InventoryActivityModule {

    @ContributesAndroidInjector(modules = [InventoryBillModule::class])
    abstract fun billingBillFragment(): InventoryBillFragment

    @ContributesAndroidInjector
    abstract fun inventoryHomeFragment(): InventoryHomeFragment

    @ContributesAndroidInjector(modules = [InventoryItemModule::class])
    abstract fun billingItemFragment(): InventoryItemFragment

    @ContributesAndroidInjector(modules = [InventoryItemListModule::class])
    abstract fun billingItemListFragment(): InventoryItemListFragment

    @ContributesAndroidInjector
    abstract fun addBillItemBottomSheetDialog(): AddInventoryItemBottomSheetDialog
}
