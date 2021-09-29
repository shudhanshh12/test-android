package tech.okcredit.home.ui.home

import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.KycRisk
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.dynamicview.data.model.Customization
import `in`.okcredit.merchant.suppliercredit.model.NotificationReminderForUi
import `in`.okcredit.merchant.suppliercredit.server.internal.ApiEntityMapper
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import com.mixpanel.android.mpmetrics.InAppNotification
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.home.ui.payables_onboarding.HomeTabOrderList
import tech.okcredit.home.usecase.GetHomeMerchantData

//  Testing Scope  //

// 1. Test Shortcut
// 2. Test Analytics
// 3. Test OneTime Collection Adoption
// 4. Test Rewards and collection CTA

interface HomeContract {

    data class State(
        // Platform
        val isConnectedToInternet: Boolean = true,
        val inAppDownloadLoader: Boolean = false,
        val inAppNavigationObject: HomeFragment.InAppNavigationObject? = null,
        val showInAppNavigationPopup: Boolean = false,

        val isRewardEnabled: Boolean = false,
        val businessData: GetHomeMerchantData.Response? = null,

        val unSyncTxnCount: Int = 0,
        val unSyncCustomerCount: Int = 0,
        val homeSyncLoader: Boolean = false,

        // Activation
        val showAddSupplierEducation: Boolean = false,
        val showFirstSupplierEducation: Boolean = false,
        val toolbarCustomization: Customization? = null,
        val isPayOnlineEducationHomeShown: Boolean? = null,
        val isMerchantFromCollectionCampaign: Boolean = false,
        val canShowUploadButton: Boolean = false,
        val canShowUploadButtonTooltip: Boolean = false,
        val canShowFilterOption: Boolean = false,
        val canShowNewOnSupplierTab: Boolean = false,
        val hideBigButtonAndNudge: Boolean = true,
        val kycStatus: KycStatus = KycStatus.NOT_SET,
        val kycRiskCategory: KycRiskCategory = KycRiskCategory.NO_RISK,
        val isPayablesExperimentEnabled: Boolean? = null,
        val canShowMultipleAccountsEntryPoint: Boolean = false,
        val isContactPermissionAskedOnce: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        data class ShowBusiness(val businessData: GetHomeMerchantData.Response) : PartialState()

        data class SetUnSyncTxnCount(val count: Int) : PartialState()
        data class SetUnSyncCustomersCount(val count: Int) : PartialState()

        data class SetRefreshLoaderVisibility(val status: Boolean) : PartialState()

        data class SetInAppDownloadLoaderVisibility(val status: Boolean) : PartialState()

        data class SetInternetConnectivityStatus(val status: Boolean) : PartialState()

        object NoChange : PartialState()

        data class ShowInAppNavigationPopup(val showInAppNavigationPopup: Boolean) : PartialState()

        data class UpdateInAppNotificationUI(val inAppNotificationObject: HomeFragment.InAppNavigationObject?) :
            PartialState()

        data class AddSupplierEducation(val showAddSupplierEducation: Boolean) : PartialState()

        data class FirstSupplierEducation(val showFirstSupplierEducation: Boolean) : PartialState()

        data class ToolbarCustomization(val customization: Customization?) : PartialState()

        data class SetRewardEnabled(val isRewardEnabled: Boolean) : PartialState()

        data class IsMerchantFromCollectionCampaign(val isMerchantFromCollectionCampaign: Boolean) : PartialState()

        data class IsPayOnlineEducationShown(val isPayOnlineEducationHomeShown: Boolean) : PartialState()

        data class CanShowNewOnSupplierTab(val canShowNewOnSupplierTab: Boolean) : PartialState()

        data class SetCanShowUploadButtonAndTooltip(val canShowButton: Boolean) : PartialState()

        data class CanShowFilterOption(val canShow: Boolean) : PartialState()

        data class SetCanShowAddRelationNudge(val showAddRelationNudge: Boolean) :
            PartialState()

        data class SetKycDetails(
            val kycStatus: KycStatus,
            val kycRiskCategory: KycRiskCategory = KycRiskCategory.NO_RISK,
        ) : PartialState()

        data class PayablesExperimentEnabled(
            val enabled: Boolean?,
        ) : PartialState()

        data class ShowMultipleAccountEntry(val canShow: Boolean) : PartialState()

        data class SetContactPermissionAskedOnce(val value: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {

        object Load : Intent()

        object LoadAddBankDetails : Intent()

        object OnResume : Intent()

        object SyncNow : Intent()

        object SetupViewPager : Intent()

        data class WhatsApp(val contactPermissionAvailable: Boolean) : Intent()

        data class SetInAppDownloadLoaderVisibility(val status: Boolean) : Intent()

        data class SubmitFeedback(val feedback: String, val rating: Int) : Intent()

        data class ShowInAppNavigationPopup(val showInAppNavigationPopup: Boolean) : Intent()

        data class UpdateInAppNavigationObject(val inAppNavigationObject: HomeFragment.InAppNavigationObject?) :
            Intent()

        data class RxPreferenceBoolean(val key: String, val value: Boolean, val scope: Scope) : Intent()

        object HideUploadButtonToolTip : Intent()

        object ResetKycNotification : Intent()

        object CustomerTabEducationShown : Intent()
        data class ShowPreNetworkToolTip(val delayInToolTipShown: Long) : Intent()

        data class UpdateReminderNotification(
            val notificationId: String,
            val status: ApiEntityMapper.NotificationReminderStatus,
        ) : Intent()

        data class TrackPreNetworkViewed(val tab: String) : Intent()

        object ContactPermissionAskedOnce : Intent()
        object CheckForContactPermission : Intent()
        object TrackTwoCTAViewed : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GoToReferralInAppNotification : ViewEvent()

        object GoToAddOkCreditContactInAppNotification : ViewEvent()

        object ShowFilterEducation : ViewEvent()

        object ShowReviewDialog : ViewEvent()

        object ShowBulkReminder : ViewEvent()

        object GotoLogin : ViewEvent()

        data class ShowMixPanelInAppNotification(
            val inAppNotification: InAppNotification,
            val collectionMerchantProfile: CollectionMerchantProfile?,
            val rewardAmount: Long?,
        ) : ViewEvent()

        object GoToSyncScreen : ViewEvent()

        object ShowImmediateUpdate : ViewEvent()

        object ShowUploadButtonTooltip : ViewEvent()

        object TrackUploadButtonViewed : ViewEvent()

        object AppUpdateInterrupted : ViewEvent()

        object ShowSupplierTabEducation : ViewEvent()

        object ShowKycCompleteDialog : ViewEvent()

        data class ShowPreNetworkOnboardingNudges(val delayInToolTipShown: Long) : ViewEvent()

        data class ShowAddBankPopUp(val customerNames: List<String>) : ViewEvent()

        data class ShowKycRiskDialog(val kycStatus: KycStatus, val kycRisk: KycRisk) : ViewEvent()

        data class ShowKycStatusDialog(val kycStatus: KycStatus) : ViewEvent()

        data class SetupViewPager(
            val homeTabOrderList: HomeTabOrderList,
            val isPayablesExperimentEnabled: Boolean?,
        ) : ViewEvent()

        data class TrackPayablesExperimentStarted(val experimentEnabled: Boolean?) : ViewEvent()

        data class ShowNotificationReminder(
            val notificationReminderForUi: NotificationReminderForUi?,
        ) : ViewEvent()
    }
}
