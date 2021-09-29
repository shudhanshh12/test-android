package `in`.okcredit.merchant.customer_ui.ui.customer

import `in`.okcredit.backend._offline.model.DueInfo
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.customer_ui.usecase.CollectionTriggerVariant
import `in`.okcredit.merchant.customer_ui.usecase.GetCollectionNudgeForCustomerScreen
import `in`.okcredit.merchant.customer_ui.usecase.GetCollectionNudgeOnDueDateCrossed
import `in`.okcredit.merchant.customer_ui.usecase.GetCustomerMenuOptions
import `in`.okcredit.merchant.customer_ui.usecase.GetCustomerScreenSortSelection.CustomerScreenSortSelection
import `in`.okcredit.merchant.customer_ui.utils.calender.MonthView
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import androidx.annotation.StringRes
import merchant.okcredit.accounting.contract.model.SupportType
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import tech.okcredit.android.base.preferences.Scope

interface CustomerContract {

    data class State(
        val isLoading: Boolean = true,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val lastZeroBalanceIndex: Int = 0,
        val transactions: List<Transaction> = listOf(),
        val collectionsMap: Map<String, Collection> = mapOf(),
        val customer: Customer? = null,
        val business: Business? = null,
        val error: Boolean = false,
        val playSound: Boolean = false,
        val isTxnExpanded: Boolean = false,
        val isCollectionActivated: Boolean = false,
        val isVoiceTransactionEnabled: Boolean = false,
        val isSupplierCreditEnabledForCustomer: Boolean = false,
        val dueInfo: DueInfo? = null,
        val canShowVoiceError: Boolean = false,
        val showCustomerStatementLoader: Boolean = false,
        val customerCollectionProfile: CollectionCustomerProfile? = null,
        val referralId: String? = null,
        val showUnblockDialog: Boolean = false,
        val isBlocked: Boolean = false,
        val isDiscountEnabled: Boolean = false,
        val showCustomerMenuEducation: Boolean = false,
        val showGiveDiscountEducation: Boolean = false,
        val canGetCalendarPermission: Boolean = false,
        val canShowCollectionDate: Boolean = true,
        val canShowTransactionArrows: Boolean = false,
        val unreadMessageCount: String = "",
        val canShowChatNewSticker: Boolean = false,
        val canShowBillNewSticker: Boolean = false,
        val isChatEnabled: Boolean = false,
        val firstUnseenMessageId: String? = null,
        val showCustomerReport: Boolean = false,
        val isSupplierCollectionEnabled: Boolean = false,
        val canShowCreditPaymentLayout: Boolean = true,
        val isSingleListEnabled: Boolean = false,
        val isBillEnabled: Boolean = false,
        val totalBills: Int = 0,
        val unreadBillCount: Int = 0,
        val showTransactionNudge: Boolean = false,
        val referralTargetBanner: ReferralTargetBanner? = null,
        val showCollectionNudge: GetCollectionNudgeForCustomerScreen.Show = GetCollectionNudgeForCustomerScreen.Show.NONE,
        val cashbackMessage: String? = null,
        val showSubscriptions: Boolean = true,
        val kycStatus: KycStatus = KycStatus.NOT_SET,
        val kycRiskCategory: KycRiskCategory = KycRiskCategory.NO_RISK,
        val isKycLimitReached: Boolean = false,
        val canShowKycDialogOnRemind: Boolean = true,
        val isRoboflowFeatureEnabled: Boolean = false,
        val showPayOnlineButtonLoader: Boolean = false,
        val isCustomerPayOnlinePaymentEnable: Boolean = false,
        val redirectToPayment: Boolean = false,
        val showCollectionWithGpay: Boolean = false,
        val collectWithGPayEnabled: Boolean = false,
        val contextualHelpIds: List<String> = emptyList(),
        val isBlindPayEnabled: Boolean = false,
        val blindPayLinkId: String = "",
        val showAddBankDetails: Boolean = false,
        val showBankAddedAcknowledgeCard: Boolean = false,
        val customerScreenList: List<CustomerScreenItem> = emptyList(),
        val contextualTrigger: CollectionTriggerVariant = CollectionTriggerVariant.NONE,
        val statusTargetedReferral: Int = 0,
        val isJustPayEnabled: Boolean = false,
        val menuOptionsResponse: GetCustomerMenuOptions.MenuOptionsResponse = GetCustomerMenuOptions.MenuOptionsResponse(),
        val showSortTransactionsBy: Boolean = false,
        val sortTransactionsBy: CustomerScreenSortSelection? = null,
        val collectionMerchantProfile: CollectionMerchantProfile = CollectionMerchantProfile.empty(),
        // TODO: 23/09/21 set the settlement type from settlement use case
        val settlementType: String = "",

        // onboarding -----
        val cleanCompanionDescription: String? = null,
        val showOnboardingNudges: Boolean = false,
        val supportType: SupportType = SupportType.NONE,
        val shouldShowCashbackBanner: Boolean = false,
        val cashbackBannerClosed: Boolean = false,
        val showPreNetworkWarningBanner: Boolean = false,
        val destinationUpdateAllowed: Boolean = true,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        data class ShowData(
            val transaction: List<Transaction>,
            val lastZeroBalanceIndex: Int,
            val playSound: Boolean,
            val sortTransactionsBy: CustomerScreenSortSelection
        ) : PartialState()

        data class ShowCustomer(
            val customer: Customer,
            val cleanCompanionDescription: String?,
        ) : PartialState()

        object ErrorState : PartialState()

        data class SetTargetBanner(val content: ReferralTargetBanner?) : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        data class SetBusiness(val business: Business) : PartialState()

        data class SetSupplierCreditEnabledStatus(val status: Boolean) : PartialState()

        data class SetBlindPayEnabled(val isBlindPayEnabled: Boolean) : PartialState()

        data class SetBlindPayLinkId(val blindPayLinkId: String) : PartialState()

        data class SetJustPayEnabled(val isJustPayEnabled: Boolean) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        object StopMediaPlayer : PartialState()

        object ExpandTransactions : PartialState()

        data class SetCollections(val collections: List<Collection>) : PartialState()

        data class SetDiscountStatus(val status: Boolean) : PartialState()

        data class SetCollectionActivatedStatus(val status: Boolean) : PartialState()

        data class CustomerDueInfo(val dueInfo: DueInfo) : PartialState()

        data class SetReferralId(val referralId: String) : PartialState()

        data class ShowVoiceError(val canShowError: Boolean) : PartialState()

        data class SetCustomerCollectionProfile(
            val customerCollectionProfile: CollectionCustomerProfile,
        ) : PartialState()

        data class SetBlockState(val isBlocked: Boolean) : PartialState()

        data class GetCalenderPermission(val canGetCalendarPermission: Boolean) : PartialState()

        data class ShowGiveDiscountEducation(val showGiveDiscountEducation: Boolean) : PartialState()

        data class ShowCustomerMenuEducation(val showCustomerMenuEducation: Boolean) : PartialState()

        data class CanShowCollectionDate(val canShowCollectionDate: Boolean) : PartialState()

        data class SetUnreadMessageCount(val unreadMessageCount: String, val firstUnseenMessageId: String?) :
            PartialState()

        data class CanShowChatNewSticker(val canShowChatNewSticker: Boolean) : PartialState()

        data class CanShowBillNewSticker(val canShowBillNewSticker: Boolean) : PartialState()

        data class SetChatStatus(val isChatEnabled: Boolean) : PartialState()

        data class SetBillStatus(val isBillEnabled: Boolean) : PartialState()

        data class CanShowCreditPaymentLayout(val canShowCreditPaymentLayout: Boolean) : PartialState()

        data class IsSingleListEnabled(val isSingleListEnabled: Boolean) : PartialState()

        data class SetTotalAndUnseenBills(val totalBills: Int, val unseenBills: Int) : PartialState()

        data class SetCollectionNudge(val canShowCollectionNudge: GetCollectionNudgeForCustomerScreen.Show) :
            PartialState()

        data class SetCashbackMessage(val cashbackMessage: String) : PartialState()

        data class SubscriptionFeature(val enabled: Boolean) : PartialState()

        data class isRoboflowFeatureEnabled(val enabled: Boolean) : PartialState()

        data class SetCustomer(val customer: Customer) : PartialState()

        data class SetKyc(
            val kycStatus: KycStatus,
            val kycRiskCategory: KycRiskCategory,
            val isKycLimitReached: Boolean,
        ) : PartialState()

        data class SetCanShowKycDialog(val canShowKycDialog: Boolean) : PartialState()

        data class SetPayOnlineLoading(val loading: Boolean) : PartialState()

        data class SetCustomerPayOnlinePaymentEnabled(val isCustomerPayOnlinePaymentEnable: Boolean) : PartialState()

        data class SetVoiceTransactionEnabled(val enabled: Boolean) : PartialState()

        data class CanShowCollectWithGooglePay(val show: Boolean) : PartialState()

        data class SetContextualHelpIds(val helpIds: List<String>) : PartialState()

        data class SetCollectionContextualTrigger(val contextualTrigger: CollectionTriggerVariant) : PartialState()

        data class SetCustomerPaymentIntentTrigger(val trigger: Boolean) : PartialState()

        data class SetStatusForTargetedReferralCustomer(val statusTargetedReferral: Int) : PartialState()

        data class SetEligibilityOnboardingNudges(val canShow: Boolean) : PartialState()

        data class SetShowPreNetworkWarningBanner(val canShow: Boolean) : PartialState()

        data class SetMenuOptions(val menuOptions: GetCustomerMenuOptions.MenuOptionsResponse) : PartialState()

        data class SetSupportType(val type: SupportType) : PartialState()

        data class SetCashbackBannerClosed(val cashbackBannerClosed: Boolean) : PartialState()

        data class SetDestinationUpdateAllowed(val destinationUpdateAllowed: Boolean) : PartialState()

        data class SetShowSortTransactionsBy(val canShow: Boolean) : PartialState()

        data class SetMerchantCollectionProfile(val collectionMerchantProfile: CollectionMerchantProfile) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        object LoadGooglePay : Intent()

        object LoadReportFromBalanceWidgetExpt : Intent()

        object LoadKycDetails : Intent()

        object LoadContextualHelp : Intent()

        object LoadCollectionContextualTrigger : Intent()

        object LoadCustomerPaymentIntent : Intent()

        object SyncDueInfo : Intent()

        object OnResume : Intent()

        // goto addTxn screen
        data class GoToAddTxn(val txnType: Int) : Intent()

        // view txn
        data class ViewTransaction(val txnId: String, val currentDue: Long) : Intent()

        // go to supplier profile
        object GoToCustomerProfile : Intent()

        // call supplier
        object GoToPhoneDialer : Intent()

        // add mobile
        object AddMobile : Intent()

        // stop media player
        object StopMediaPlayer : Intent()

        // expand txs
        object ExpandTransactions : Intent()

        // go to privacy screen
        object GoToPrivacyScreen : Intent()

        // share payment link
        data class SharePaymentLink(
            val reminderMode: String,
            val reminderStringsObject: GetPaymentReminderIntent.ReminderStringsObject,
        ) : Intent()

        object UpdateLastViewTime : Intent()

        data class SetReminderMode(val mode: String) : Intent()

        // mark customer shared
        object MarkCustomerShared : Intent()

        object ShowQrCodeDialog : Intent()

        object ShowDueDatePickerIntent : Intent()

        data class OnDueDateChange(val pair: Pair<MonthView.CapturedDate, Customer>) : Intent()

        data class SubmitVoiceInput(val voiceInputText: String) : Intent()

        data class VoiceInputState(val canShowVoiceError: Boolean) : Intent()

        data class ViewDiscount(val txnId: String, val currentDue: Long) : Intent()

        object ShowUnblockDialog : Intent()

        object Unblock : Intent()

        object HideQrCodeDialog : Intent()

        data class RxPreferenceBoolean(val key: String, val value: Boolean, val scope: Scope) : Intent()

        object SendCollectionReminderClicked : Intent()

        data class ShowCustomerReport(val source: String) : Intent()

        object ShowPayOnlineEducation : Intent()

        object HideTargetBanner : Intent()

        object CloseTargetBanner : Intent()

        object UpdateMobileAndRemind : Intent()

        object ForceRemind : Intent()

        object PayOnline : Intent()

        object GetPaymentOutLinkDetail : Intent()

        object IsJuspayFeatureEnabled : Intent()

        object GetRiskDetailsResponse : Intent()

        object DontShowKycDialogOnRemind : Intent()

        data class DisableAutoDueDateDialog(val disable: Boolean = true) : Intent()

        object OpenWhatsAppForHelp : Intent()

        object TriggerMerchantPayout : Intent()

        data class GotoPaymentEditAmountScreen(val kycStatus: KycStatus) : Intent()

        object CollectionDestinationAdded : Intent()

        object GetBlindPayLinkId : Intent()

        object ShowSetupCollection : Intent()

        data class GetCleanCustomerIfImmutable(val mobile: String?) : Intent()

        object DeleteImmutableAccount : Intent()

        object GotoReferralScreen : Intent()

        object UpdateLedgerSeen : Intent()

        object SendNotificationReminder : Intent()

        object SyncCustomerCollections : Intent()

        object SyncCustomerTransactions : Intent()

        data class OpenExitDialog(val exitSource: String) : Intent()

        object CashbackBannerClosed : Intent()

        object LoadSortTransactionsByFeatureFlag : Intent()

        data class SortTransactionsByOptionSelected(val customerScreenSortSelection: String) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class GotoCustomerProfile(val customerId: String) : ViewEvent()

        data class GotoTransactionDetailFragment(val transaction: Transaction) : ViewEvent()

        data class GotoAddTransactionThroughVoice(
            val customerId: String,
            val txnType: Int,
            val amount: Int,
        ) : ViewEvent()

        data class GotoLegacyAddTransaction(
            val customerId: String,
            val txnType: Int,
        ) : ViewEvent()

        data class GotoCallCustomer(val mobile: String) : ViewEvent()

        object ShowQrCodePopup : ViewEvent()

        data class GotoCustomerProfileForAddingMobile(val customerId: String) : ViewEvent()

        object GotoCollectionOnboarding : ViewEvent()

        object GotoCustomerPrivacyScreen : ViewEvent()

        data class GotoDeletedTransaction(val txnId: String) : ViewEvent()

        data class OpenPaymentReminderIntent(val intent: android.content.Intent) : ViewEvent()

        data class ShareReportIntent(val intent: android.content.Intent) : ViewEvent()

        object GoToHomeScreen : ViewEvent()

        object ShowUnblockDialog : ViewEvent()

        data class GoToDiscountScreen(val txnId: String, val currentDue: Long) : ViewEvent()

        object ShowCollectionDateEducation : ViewEvent()

        object ShowRemindEducation : ViewEvent()

        object OnReminderClicked : ViewEvent()

        object ShowOnlineCollectionEducation : ViewEvent()

        object ShowChatEducation : ViewEvent()

        data class GoToCustomerReport(val source: String) : ViewEvent()

        object ShowBuyerTxnAlert : ViewEvent()

        object ShowReportIconEducation : ViewEvent()

        object ShowPayOnlineEducation : ViewEvent()

        object ShowBillEducation : ViewEvent()

        object ShowSetupCollectionDialog : ViewEvent()

        data class ShowPaymentPendingDialog(
            val customer: Customer,
            val dueInfo: DueInfo,
            val showVariant: GetCollectionNudgeOnDueDateCrossed.Show,
        ) : ViewEvent()

        object ForceRemind : ViewEvent()

        object ShowAddPaymentMethodDialog : ViewEvent()

        data class GotoPaymentEditAmountScreen(
            val linkId: String,
            val paymentAddress: String,
            val paymentType: String,
            val paymentName: String,
            val remainingDailyAmount: Long,
            val maxDailyAmount: Long,
            val riskType: String,
            val kycStatus: KycStatus,
            val kycRiskCategory: KycRiskCategory,
            val futureAmountLimit: Long,
        ) : ViewEvent()

        data class GotoCustomerBlindPayEditAmountScreen(
            val paymentAddress: String,
            val paymentType: String,
            val paymentName: String,
            val remainingDailyAmount: Long,
            val maxDailyAmount: Long,
            val riskType: String,
        ) : ViewEvent()

        data class ShowWebFlowDestinationDialog(
            val messageLink: String,
            val paymentAddress: String,
            val paymentType: String,
            val paymentName: String,
        ) : ViewEvent()

        data class OpenLimitReachedBottomSheet(
            val remainingLimit: Long,
            val maxLimit: Long,
            val showTxnAmountWarning: Boolean,
        ) : ViewEvent()

        data class ShowAutoDueDateDialog(val activeDate: DateTime) : ViewEvent()

        data class CollectWithGPayError(@StringRes val errDefault: Int) : ViewEvent()

        object ShowDueDatePickerDialog : ViewEvent()

        object EnableReportFromBalanceWidgetExp : ViewEvent()

        object CollectWithGPayRequestSent : ViewEvent()

        data class ShowError(val msg: String) : ViewEvent()

        data class OpenWhatsAppForHelp(val intent: android.content.Intent) : ViewEvent()

        object ShowBlindPayDialog : ViewEvent()

        object AccountDeletedSuccessfully : ViewEvent()

        object GotoReferralEducationScreen : ViewEvent()

        object GotoReferralInviteListScreen : ViewEvent()

        data class OpenExitDialog(val exitSource: String) : ViewEvent()

        data class GoToAddPaymentWithQr(val customerId: String, val expandedQr: Boolean = false) : ViewEvent()

        object CashbackBannerClosed : ViewEvent()
    }

    companion object {
        const val REACTIVATE = "reactivate"
        const val ARG_TXN_ID = "txn_id"
        const val NAME = "name"
        const val ARG_SOURCE = "source"
        const val ARG_COLLECTION_ID = "collection_id"
        const val ARG_CUSTOMER_ID = "customer_id"
        const val FEATURE_CUSTOMER_SCREEN_VOICE_TRANSACTION = "voice_transaction_legacy"
    }
}
