package tech.okcredit.home.ui.homesearch

import `in`.okcredit.merchant.suppliercredit.Supplier
import androidx.annotation.StringRes
import tech.okcredit.contacts.contract.model.Contact

sealed class HomeSearchItem {

    data class CustomerItem(
        val customerId: String,
        val name: String,
        val profileImage: String?,
        val balance: Long,
        val commonLedger: Boolean,
        val addTxnPermissionDenied: Boolean,
        val showReminderOption: Boolean,
        val showQROption: Boolean,
        val showFullQRCard: Boolean,
        val qrIntent: String?,
    ) : HomeSearchItem()

    data class SupplierItem(
        val id: String,
        val supplier: Supplier,
        val syncType: Int,
    ) : HomeSearchItem()

    data class NoUserFoundItem(
        val addRelationshipLoading: Boolean,
        val searchQuery: String,
    ) : HomeSearchItem()

    object FilterItem : HomeSearchItem()

    data class HeaderItem(
        @StringRes val title: Int,
    ) : HomeSearchItem()

    data class ContactsItem(
        val contact: Contact
    ) : HomeSearchItem()

    data class ImportCustomerContactItem(
        val source: HomeSearchContract.SOURCE
    ) : HomeSearchItem()

    object ShimmerListLoadingItem : HomeSearchItem()
}
