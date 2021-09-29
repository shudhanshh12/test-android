package `in`.okcredit.collection_ui.ui.insights

import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CreditGraphicalDataProvider.GraphDuration
import `in`.okcredit.collection.contract.CreditGraphicalDataProvider.GraphResponse
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface CollectionInsightsContract {

    data class State(
        val collectionMerchantProfile: CollectionMerchantProfile? = null,
        val isCameraPermissionAllowed: Boolean = false,
        val onlineTxnAmount: Long? = 0L,
        val acceptedTxnAmount: Long? = 0L,
        val givenCreditAmount: Long? = 0L,
        val graphResponse: GraphResponse? = null,
        val dueCustomers: List<Customer> = mutableListOf(),
        val referralLink: String? = null,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val isAuthenticated: Boolean = false,
        val totalQRTransactionsBalance: Double = -1.0,
        val merchantId: String = "",
        val business: Business? = null,
        val kycRiskCategory: KycRiskCategory = KycRiskCategory.NO_RISK,
        val kycStatus: KycStatus = KycStatus.NOT_SET,
        val isLimitReached: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        data class SetCollectionMerchantProfile(val collectionMerchantProfile: CollectionMerchantProfile?) :
            PartialState()

        data class SetBusiness(val business: `in`.okcredit.merchant.contract.Business) : PartialState()

        data class SetTransactionsInsights(
            val onlineTxnAmount: Long?,
            val acceptedTxnAmount: Long?,
            val givenCreditAmount: Long?
        ) : PartialState()

        data class SetBarDataSet(val graphResponse: GraphResponse?) : PartialState()

        data class SetDueCustomerList(val dueCustomers: List<Customer>) : PartialState()

        data class SetReferralLink(val referralLink: String?) : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        data class SetIsAuthenticated(val isAuthenticated: Boolean) : PartialState()

        data class SetTransactionDetails(val totalQRTransactionsBalance: Double) : PartialState()

        data class SetMerchantId(val merchantId: String) : PartialState()

        data class SetKycDetails(val KycStatus: KycStatus, val kycRiskCategory: KycRiskCategory, val isKycLimitReached: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        // show alert
        object Load : Intent()

        object LoadKycDetails : Intent()

        // hide alert
        object HideAlert : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        object DeleteMerchantDestination : Intent()

        data class SelectGraphDuration(val graphDuration: GraphDuration) : Intent()

        data class SendReminders(
            val customerId: String,
            val reminderStringsObject: GetPaymentReminderIntent.ReminderStringsObject
        ) : Intent()

        data class ShowPaymentReminderDialog(val customer: Customer) : Intent()

        object HidePaymentReminderDialog : Intent()

        object SaveMerchantQR : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class Error(val network: Boolean = false) : ViewEvent()
    }

    interface Navigator {
        fun gotoLogin()

        fun gotoCollectionTutorialScreen()

        fun gotoCollectionTutorialScreenByClearingStack()

        fun openPaymentReminderIntent(intent: android.content.Intent)

        fun openPaymentReminderDialog(
            collectionCustomerProfile: CollectionCustomerProfile,
            customer: Customer
        )
    }
}
