package tech.okcredit.home.ui.add_transaction_home_search

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.server.internal.common.SupplierCreditServerErrors
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import tech.okcredit.contacts.contract.model.Contact

interface AddTransactionShortcutSearchContract {

    data class State(
        val isLoading: Boolean = true,
        val isContactsLoading: Boolean = false,
        val addRelationLoading: Boolean = false,
        val isContactsPermissionGranted: Boolean = true,
        val isCollectionActivated: Boolean = false,
        val business: Business? = null,
        val suggestedCustomers: List<Customer> = listOf(),
        val showSuggestedCustomers: Boolean = true,
        val isSuggestedCustomersLoading: Boolean = true,
        val customers: List<Customer> = listOf(),
        val suppliers: List<Supplier> = listOf(),
        val contacts: List<Contact> = listOf(),
        val unSyncCustomerIds: List<String> = arrayListOf(),
        val unSyncSupplierIds: List<String> = arrayListOf(),
        val supplierCreditEnabledCustomerIds: String = "",
        val searchQuery: String = "",
        val isProfilePicClickable: Boolean = true,
        val isComingFromReferralTargets: Boolean = false,
        val isSupplierCreditEnabled: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object HideLoading : PartialState()

        object NoChange : PartialState()

        data class SetBusiness(val business: Business) : PartialState()

        data class SetCollectionActivatedStatus(val status: Boolean) : PartialState()

        data class SetData(
            val customers: List<Customer>,
            val suppliers: List<Supplier>,
            val contacts: List<Contact>
        ) : PartialState()

        data class SetSuggestedCustomers(
            val suggestedCustomers: List<Customer>
        ) : PartialState()

        data class UpdateSearchQuery(
            val searchQuery: String
        ) : PartialState()

        data class SetUnSyncCustomers(val customers: List<String>) : PartialState()

        data class SetUnSyncSuppliers(val suppliers: List<String>) : PartialState()

        data class SetSupplierCreditEnabledCustomerIds(val customerIds: String) : PartialState()

        data class SetImportContactVisibility(val status: Boolean) : PartialState()

        data class SetContactPermissionStatus(val status: Boolean) : PartialState()

        data class IsProfilePicClickable(val isProfilePicClickable: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object ImportContact : Intent()

        data class AddRelationFromContact(val contact: Contact) : Intent()

        data class AddRelationFromSearch(val query: String) : Intent()

        data class SearchQuery(val searchQuery: String) : Intent()

        data class SendWhatsAppReminder(val customerId: String) : Intent()

        object OnBackPressed : Intent()

        object GoToHomeScreen : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        data class GotoCustomerScreenAndCloseSearch(val customerId: String, val mobile: String?) : ViewEvent()

        data class ShareReminder(val intent: android.content.Intent) : ViewEvent()

        object ShowInvalidMobileNumber : ViewEvent()

        data class ShowMobileConflictForCustomer(val customer: Customer) : ViewEvent()

        data class ShowCyclicAccountForSupplier(val supplier: Supplier) : ViewEvent()

        data class ShowCyclicAccountForDeletedSupplier(val supplier: Supplier) : ViewEvent()

        data class ShowMobileConflictForSupplier(val supplier: Supplier) : ViewEvent()

        object ShowInvalidName : ViewEvent()

        object ShowError : ViewEvent()

        object ShowInternetError : ViewEvent()

        data class ShowCyclicAccountForDeletedCustomer(val errorData: SupplierCreditServerErrors.Error?) : ViewEvent()

        data class ShowCyclicAccount(val info: SupplierCreditServerErrors.Error?) : ViewEvent()

        object GotoLogin : ViewEvent()

        object OnBackPressed : ViewEvent()

        object GotoHomeScreen : ViewEvent()
    }
}
