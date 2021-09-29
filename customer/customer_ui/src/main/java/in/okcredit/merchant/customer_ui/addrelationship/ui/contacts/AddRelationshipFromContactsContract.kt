package `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts

import `in`.okcredit.customer.contract.RelationshipType
import `in`.okcredit.merchant.customer_ui.addrelationship.enum.AddRelationshipFailedError
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipEpoxyModels.ContactModel
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import androidx.annotation.StringRes
import tech.okcredit.contacts.contract.model.Contact

interface AddRelationshipFromContactsContract {

    companion object {
        const val ADD_CUSTOMER = 0
        const val ADD_SUPPLIER = 1
    }

    data class State(
        val isLoading: Boolean = false,
        val listContactModel: List<ContactModel> = emptyList(),
        val searchQuery: String? = null,
        val canShowSearchInput: Boolean = false,
        val relationshipType: Int? = null,
        val isFragmentOpenForResult: Boolean = false,
        val addRelationshipEpoxyModels: List<AddRelationshipEpoxyModels> = emptyList(),
        val source: String = "",
        val defaultMode: String = "",
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class SetContacts(val contactList: List<ContactModel>, val searchQuery: String? = null) : PartialState()
        data class ShowProgress(val show: Boolean) : PartialState()
        data class ShowSearchInput(val canShow: Boolean) : PartialState()
        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class ShowSearchInput(val canShow: Boolean) : Intent()
        data class AddRelationship(
            val mobile: String,
        ) : Intent()

        data class AddCustomer(
            val name: String,
            val mobile: String,
            val profileImage: String?,
        ) : Intent()

        data class AddSupplier(
            val name: String,
            val mobile: String,
            val profileImage: String?,
        ) : Intent()

        data class SearchQuery(val query: String) : Intent()
        data class RedirectToLedgerScreen(val relationshipId: String) : Intent()
        data class ReturnResult(val mobile: String) : Intent()
        object RelationshipAddedAfterOnboarding : Intent()
        object TrackSearchUsed : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object SetResultRelationshipAddedSuccessfully : ViewEvent()

        data class GoToCustomerFragment(val customerId: String) : ViewEvent()
        data class GoToSupplierFragment(val supplierId: String) : ViewEvent()
        data class ShowError(@StringRes val message: Int) : ViewEvent()

        data class AddRelationshipFailed(
            val id: String?,
            val name: String?,
            val mobile: String?,
            val profile: String?,
            val errorType: AddRelationshipFailedError,
            val exception: Throwable,
        ) : ViewEvent()

        data class ReturnResult(val contactModel: ContactModel) : ViewEvent()
    }
}

sealed class AddRelationshipEpoxyModels {

    object AddRelationshipHeader : AddRelationshipEpoxyModels()

    data class AddManuallyModel(
        val name: String? = null,
    ) : AddRelationshipEpoxyModels()

    data class ContactModel(
        val relationshipId: String? = null,
        val name: String,
        val mobile: String,
        val profileImage: String?,
        val showOkcreditIcon: Boolean,
        val balance: Long? = null,
        val relationshipType: RelationshipType? = null,
    ) : AddRelationshipEpoxyModels()
}

fun Contact.toContactModel(
    id: String? = null,
    balance: Long? = null,
    relationshipType: RelationshipType? = null,
) = ContactModel(
    name = this.name,
    mobile = this.mobile,
    profileImage = this.picUri ?: "",
    showOkcreditIcon = this.found,
    relationshipId = id,
    balance = balance,
    relationshipType = relationshipType,
)
