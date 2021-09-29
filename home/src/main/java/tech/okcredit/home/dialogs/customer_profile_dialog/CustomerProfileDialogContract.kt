package tech.okcredit.home.dialogs.customer_profile_dialog

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface CustomerProfileDialogContract {

    companion object {
        const val ARG_CUSTOMER_ID = "customer_id"
    }

    data class State(
        val business: Business? = null,
        val customer: Customer? = null,
        val cleanCompanionDescription: String? = null, // If immutable, description of the clean profile that conflicts
        val collectionCustomerProfile: CollectionCustomerProfile? = null,
        val kycRiskCategory: KycRiskCategory = KycRiskCategory.NO_RISK,
        val kycStatus: KycStatus = KycStatus.NOT_SET,
        val isKycLimitReached: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class SetCustomerAndBusiness(
            val customer: Customer,
            val collectionCustomerProfile: CollectionCustomerProfile?,
            val cleanCompanionDescription: String? = null,
        ) : PartialState()

        data class SetBusiness(val business: Business) : PartialState()

        data class SetKycRiskCategory(
            val kycStatus: KycStatus,
            val kycRiskCategory: KycRiskCategory,
            val isKycLimitReached: Boolean,
        ) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class SendWhatsAppReminder(val customerId: String, val reminderMode: String?) : Intent()
    }

    interface Navigator {
        fun gotoLogin()
        fun goBack()
        fun showWhatsAppNotInstalled()
        fun shareReminder(intent: android.content.Intent)
    }

    object Source {
        const val CUSTOMER_SEARCH_PROFILE = "customer_search_profile"
        const val CUSTOMER_SEARCH_QR_ACTION = "customer_search_qr_action"
        const val CUSTOMER_SEARCH_QR_CARD = "customer_search_qr_card"
        const val HOME_PAGE = "Homepage"
    }
}
