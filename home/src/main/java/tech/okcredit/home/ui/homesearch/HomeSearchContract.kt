package tech.okcredit.home.ui.homesearch

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.server.internal.common.SupplierCreditServerErrors
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import android.content.Intent
import tech.okcredit.contacts.contract.model.Contact
import tech.okcredit.home.usecase.GetHomeSearchData
import java.util.concurrent.ConcurrentHashMap

interface HomeSearchContract {

    enum class SOURCE(val value: String) {
        HOME_CUSTOMER_TAB("HOME_CUSTOMER_TAB"),
        HOME_SUPPLIER_TAB("HOME_SUPPLIER_TAB"),
    }

    data class State(
        val isLoading: Boolean = true,
        val source: SOURCE = SOURCE.HOME_CUSTOMER_TAB,
        val isContactsLoading: Boolean = false,
        val addRelationLoading: Boolean = false,
        val isContactsPermissionGranted: Boolean = true,
        val customers: List<GetHomeSearchData.CustomerWithQrIntent> = listOf(),
        val suppliers: List<Supplier> = listOf(),
        val contacts: List<Contact> = listOf(),
        val unSyncCustomerIds: List<String> = listOf(),
        val unSyncSupplierIds: List<String> = listOf(),
        val supplierCreditEnabledCustomerIds: String = "",
        val searchQuery: String = "",
        val billCountMap: ConcurrentHashMap<String, Long>? = null,
        val kycRiskCategory: KycRiskCategory = KycRiskCategory.NO_RISK,
        val isAccountSelection: Boolean = false,
        val hideSearchInput: Boolean = false,
        val itemList: List<HomeSearchItem> = listOf(),
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object HideLoading : PartialState()

        object NoChange : PartialState()

        data class SetViewModelParams(val source: SOURCE, val isAccountSelection: Boolean = false) : PartialState()

        data class SetData(
            val customers: List<GetHomeSearchData.CustomerWithQrIntent>,
            val suppliers: List<Supplier>,
            val contacts: List<Contact>,
        ) : PartialState()

        data class UpdateSearchQuery(
            val searchQuery: String,
        ) : PartialState()

        data class SetUnSyncCustomers(val customers: List<String>) : PartialState()

        data class SetUnSyncSuppliers(val suppliers: List<String>) : PartialState()

        data class SetSupplierCreditEnabledCustomerIds(val customerIds: String) : PartialState()

        data class SetImportContactVisibility(val status: Boolean, val isPermissionGranted: Boolean = false) :
            PartialState()

        data class SetContactPermissionStatus(val status: Boolean) : PartialState()

        data class SetBillCountMap(val billCountMap: ConcurrentHashMap<String, Long>) : PartialState()

        data class SetKycRiskCategory(val kycRiskCategory: KycRiskCategory) : PartialState()

        data class SetSearchInput(val hide: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class ImportContact(val isPermissionGranted: Boolean = false) : Intent()

        data class AddRelationFromContact(val contact: Contact, val source: SOURCE) : Intent()

        data class AddRelationFromSearch(val query: String, val source: SOURCE) : Intent()

        data class SearchQuery(val searchQuery: String) : Intent()

        data class SendWhatsAppReminder(val customerId: String) : Intent()

        object ResetData : Intent()

        data class ShowSearchInput(val canShow: Boolean = true) : Intent()

        data class SendCustomerReminder(val customerId: String) : Intent()

        data class ShowCustomerQr(val customerId: String, val source: String) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        data class GoToCustomerScreenAndCloseSearch(val customerId: String, val mobile: String?) : ViewEvent()

        data class GoToSupplierScreenAndCloseSearch(val supplier: Supplier) : ViewEvent()

        object ShowInvalidMobileNumber : ViewEvent()

        data class ShowMobileConflictForCustomer(val customer: Customer) : ViewEvent()

        data class ShowCyclicAccountForSupplier(val supplier: Supplier) : ViewEvent()

        data class ShowCyclicAccountForDeletedSupplier(val supplier: Supplier) : ViewEvent()

        data class ShowMobileConflictForSupplier(val supplier: Supplier) : ViewEvent()

        object ShowInvalidName : ViewEvent()

        object ShowError : ViewEvent()

        object ShowKeyboard : ViewEvent()

        object ShowInternetError : ViewEvent()

        data class CustomerAddPayment(val customerId: String, val source: String) : ViewEvent()

        data class CustomerQrDialog(val customerId: String, val source: String) : ViewEvent()

        data class ShowCyclicAccountForDeletedCustomer(val errorData: SupplierCreditServerErrors.Error?) : ViewEvent()

        data class ShowCyclicAccount(val info: SupplierCreditServerErrors.Error?) : ViewEvent()

        data class ReturnAccountSelectionResult(val accountId: String, val relation: String) : ViewEvent()

        data class ShowPaymentReceived(val amount: Long, val customerId: String?) : ViewEvent()

        data class SendReminder(val intent: android.content.Intent) : ViewEvent()
    }
}
