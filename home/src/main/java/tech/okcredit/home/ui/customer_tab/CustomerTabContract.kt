package tech.okcredit.home.ui.customer_tab

import `in`.okcredit.accounting_core.contract.QuickAddCustomerModel
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CustomerAdditionalInfo
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.dynamicview.data.model.Customization
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.core.model.Transaction
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import tech.okcredit.home.ui.homesearch.Sort
import tech.okcredit.home.usecase.GetActiveCustomers

interface CustomerTabContract {

    data class State(
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val customerTabDetails: GetActiveCustomers.Response? = null,
        val business: Business? = null,
        val error: Boolean = false,
        val networkError: Boolean = false,
        val isCollectionActivated: Boolean = false,
        val unSyncCustomerIds: List<String> = arrayListOf(),
        val isLiveSalesActive: Boolean = false,
        val liveSalesTutorialVisibility: Boolean = false,
        val supplierCreditEnabledCustomerIds: String = "",
        val paymentReminderEducationShown: Boolean = false,
        val appLockInAppNotification: Boolean = false,
        val isBannerCustomizationEnabled: Boolean = false,
        val showBannerCustomization: Boolean = false,
        val bannerCustomization: Customization? = null,
        val referralTargetBanner: ReferralTargetBanner? = null,
        val isProfilePicClickable: Boolean = true,
        val kycRiskCategory: KycRiskCategory = KycRiskCategory.NO_RISK,
        val showTransactionSyncedIconEducation: Boolean = false,
        val userStoriesEnabled: Boolean = false,
        val collectionCustomerReferralMap: Map<String, CustomerAdditionalInfo> = emptyMap(),
        val list: List<CustomerTabItem> = mutableListOf(),

        val bulkReminderState: BulkReminderState? = null,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class ShowCustomerTabDetails(
            val customer: List<Customer>,
            val searchQuery: String,
            val tabCount: Int,
            val liveSalesCustomer: Customer?,
            val lifecycle: Int,
        ) : PartialState()

        data class SetLiveSaleActiveStatus(val isLiveSalesActive: Boolean) : PartialState()
        data class SetNetworkError(val networkError: Boolean) : PartialState()
        data class SetBusiness(val business: Business) : PartialState()
        data class SetCollectionActivatedStatus(val status: Boolean) : PartialState()
        data class ShowAlert(val message: String) : PartialState()
        object HideAlert : PartialState()
        object ErrorState : PartialState()

        data class SetUnSyncCustomers(val customers: List<String>) : PartialState()
        data class SetLiveSalesTutorialVisibility(val status: Boolean) : PartialState()
        data class SetSupplierCreditEnabledCustomerIds(val customerIds: String) : PartialState()
        data class SetPaymentReminderEducationShown(val paymentReminderEducationShown: Boolean) : PartialState()
        data class SetAppLockInAppVisibility(val visibility: Boolean) : PartialState()
        object NoChange : PartialState()

        data class HomeBannerCustomization(val customization: Customization?) : PartialState()

        data class BannerCustomizationEnabled(val enabled: Boolean) : PartialState()

        data class SetTargetBanner(val referralTargetBanner: ReferralTargetBanner?) : PartialState()

        data class IsProfilePicClickable(val isProfilePicClickable: Boolean) : PartialState()

        data class SetKycRiskCategory(val kycRiskCategory: KycRiskCategory) : PartialState()

        data class ShowTransactionSyncedIconEducation(val show: Boolean) : PartialState()

        data class IsUserStoriesEnabled(val userStoriesEnabled: Boolean) : PartialState()

        data class SetCustomerCollectionReferralInfo(val list: List<CustomerAdditionalInfo>) : PartialState()

        data class SetBulkReminderState(val bulkReminderState: BulkReminderState?) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class LiveSaleClicked(val customerId: String) : Intent()
        data class LiveSaleQrSelected(val customer: Customer) : Intent()
        data class SetNewSort(val sort: Sort) : Intent()

        object AppLockInAppCancelled : Intent()

        data class QuickAddCustomerResponse(val customer: QuickAddCustomerModel?) :
            Intent()

        data class QuickAddCustomer(val customer: QuickAddCustomerModel?) : Intent()

        data class QuickAddTransaction(
            val customer: QuickAddCustomerModel,
            val amount: Long,
            val type: Transaction.Type,
        ) : Intent()

        object HideTargetBanner : Intent()

        object CloseTargetBanner : Intent()

        object LiveSalesTutorialShown : Intent()

        object LoadBulkReminderBanner : Intent()
        object TrackEntryPointViewed : Intent()
    }

    sealed class CustomerTabViewEvent : BaseViewEvent {
        object GotoLogin : CustomerTabViewEvent()

        data class GotoCustomerScreen(val customerId: String, val mobile: String?) : CustomerTabViewEvent()

        object GotoSupplierTab : CustomerTabViewEvent()

        data class GoLoLiveSalesScreen(val customerId: String) : CustomerTabViewEvent()

        data class GoToLiveSaleQRDialog(
            val collectionCustomerProfile: CollectionCustomerProfile,
            val customer: Customer,
            val business: Business?,
        ) : CustomerTabViewEvent()
    }

    interface Listeners {
        fun onNewSorted(sortType: Sort)
    }

    data class BulkReminderState(
        val totalBalanceDue: Long,
        val totalReminders: Int,
        val showNotificationBadge: Boolean,
        val defaulterSince: Int,
    )
}
