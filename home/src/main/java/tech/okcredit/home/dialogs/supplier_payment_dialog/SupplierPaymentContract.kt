package tech.okcredit.home.dialogs.supplier_payment_dialog

import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionDestinationType
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface SupplierPaymentContract {

    companion object {
        const val ARG_SUPPLIER_ID = "supplier_id"
    }

    data class State(
        val isLoading: Boolean = false,
        val isNetworkError: Boolean = false,
        val invalidBankAccountError: Boolean = false,
        val invalidBankAccountCode: Int = -1,
        val adoptionMode: String = CollectionDestinationType.UPI.value,
        val upiLoaderStatus: Boolean = false,
        val upiErrorServer: Boolean = false,
        val collectionCustomerProfile: CollectionCustomerProfile? = null,
        val isPayOnlineEducationShown: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val supplier: Supplier? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        object ShowLoading : PartialState()

        data class SetNetworkError(val isNetworkError: Boolean) : PartialState()

        data class SetCollectionCustomerProfile(val collectionCustomerProfile: CollectionCustomerProfile) : PartialState()

        data class SetSupplier(val supplier: Supplier) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
    }

    interface Navigator {
        fun gotoLogin()
    }

    sealed class ViewEvent : BaseViewEvent
}
