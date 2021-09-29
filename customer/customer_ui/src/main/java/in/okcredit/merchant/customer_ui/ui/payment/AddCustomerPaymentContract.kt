package `in`.okcredit.merchant.customer_ui.ui.payment

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import androidx.annotation.StringRes

interface AddCustomerPaymentContract {

    data class State(
        val source: String = AddTxnContainerActivity.Source.CUSTOMER_SCREEN.value,
        val customerId: String = "",
        val customerName: String = "",
        val customerProfile: String? = null,
        val balanceDue: Long = 0L,
        val qrIntent: String? = null,
        val loading: Boolean = true,
        val kycLimitReached: Boolean = false,
        val kycStatus: KycStatus = KycStatus.NOT_SET,
        val kycRiskCategory: KycRiskCategory = KycRiskCategory.NO_RISK,
        val showQrLocked: Boolean = false,
        val customerCollections: List<Collection> = emptyList(),
    ) : UiState

    sealed class ViewEvent : BaseViewEvent {
        object ExpandQr : ViewEvent()

        data class ShowError(@StringRes val error: Int) : ViewEvent()

        data class ShowPaymentReceived(val collectionId: String, val amount: Long, val createTime: Long) : ViewEvent()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object ShowQrTapped : Intent()

        object MinimizeQr : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        object ShowProgress : PartialState()

        data class CustomerData(val customer: Customer) : PartialState()

        data class SetCustomerCollectionProfile(val customerCollectionProfile: CollectionCustomerProfile) :
            PartialState()

        data class SetKycRiskCategory(
            val kycStatus: KycStatus,
            val kycRiskCategory: KycRiskCategory,
            val limitReached: Boolean,
        ) : PartialState()

        data class SetCollectionActivated(val activated: Boolean) : PartialState()

        data class SetCustomerCollections(val value: List<Collection>) : PartialState()
    }
}
