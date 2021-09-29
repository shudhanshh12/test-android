package `in`.okcredit.frontend.ui.supplier

import `in`.okcredit.backend.collection_usecases.GetSupplierStatement
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.merchant.suppliercredit.use_case.GetSupplierScreenSortSelection.SupplierScreenSortSelection
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import android.graphics.Bitmap
import merchant.okcredit.accounting.contract.model.SupportType
import tech.okcredit.android.base.preferences.Scope

interface SupplierContract {

    companion object {
        const val KEY_TRANSACTION_CREATE_TIME = "transaction_create_time"
    }

    data class State(
        val isLoading: Boolean = true,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val transactions: List<GetSupplierStatement.TransactionWrapper> = arrayListOf(),
        val supplier: Supplier? = null,
        val business: Business? = null,
        val error: Boolean = false,
        val networkError: Boolean = false,
        val privacyVisibility: Boolean = false,
        val playSound: Boolean = false,
        val isTxnExpanded: Boolean = false,
        val showTakeCreditPaymentEducation: Boolean = false,
        val showSupplierStatementLoader: Boolean = false,
        val showUnblockDialog: Boolean = false,
        val isBlocked: Boolean = false,
        val canShowAppPromotion: Boolean = false,
        val collectionCustomerProfile: CollectionCustomerProfile? = null,
        val canShowSupplierKnowMore: Boolean = false,
        val canShowTransactionArrows: Boolean = false,
        val isMerchantFromCollectionCampaign: Boolean = false,
        val isPayOnlineEducationShown: Boolean? = null,
        val unreadMessageCount: String = "",
        val canShowChatNewSticker: Boolean = false,
        val isChatEnabled: Boolean = false,
        val firstUnseenMessageId: String? = null,
        val canShowBillNewSticker: Boolean = false,
        val isBillEnabled: Boolean = false,
        val setBillUnseenCount: Int = 0,
        val totalBills: Int = 0,
        val unreadBillCount: Int = 0,
        val redirectToPayment: Boolean = false,
        val showPayOnlineButtonLoader: Boolean = false,
        val riskType: String = "",
        val cashbackMessage: String? = null,
        var ongoingPaymentAmount: Long = 0L,
        var ongoingPaymentId: String = "",
        var ongoingPaymentType: String = "",
        val contextualHelpIds: List<String> = emptyList(),
        val isBlindPayEnabled: Boolean = false,
        val isJustPayEnabled: Boolean = false,

        val blindPayLinkId: String = "",

        val lastZeroBalanceIndex: Int = 0,
        val supplierScreenList: List<SupplierScreenItem> = emptyList(),
        val customerSupportMsg: String = "",
        val supportType: SupportType = SupportType.NONE,
        val showPreNetworkWarningBanner: Boolean = false,
        val showTransactionSortSelection: Boolean = false,
        val transactionSortSelection: SupplierScreenSortSelection? = null,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        data class SetTotalAndUnseenBills(val totalBills: Int, val unseenBills: Int) : PartialState()

        data class SetAccountUnseenBills(val unreadBillCount: Int) : PartialState()

        object ShowLoading : PartialState()

        data class ShowData(
            val supplier: Supplier,
            val supplierTransactionsWrapperImpl: List<GetSupplierStatement.TransactionWrapper>,
            val lastIndexOfZeroBalanceDue: Int = 0,
            val playSound: Boolean,
            val transactionSortSelection: SupplierScreenSortSelection?,
        ) : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        data class SetBusiness(val business: Business, val isSupplierStatementVisited: Boolean) :
            PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        object StopMediaPlayer : PartialState()

        object ExpandTransactions : PartialState()

        data class ShowTakeCreditPaymentEducation(val showTakeCreditPaymentEducation: Boolean) : PartialState()

        data class ShowSupplierStatementLoader(val showSupplierStatementLoader: Boolean) : PartialState()

        object ShowUnblockDialog : PartialState()

        data class SetBlockState(val isBlocked: Boolean) : PartialState()

        data class ShowAppPromotion(val canShowAppPromotion: Boolean) : PartialState()

        data class SetCollectionCustomerProfile(val collectionCustomerProfile: CollectionCustomerProfile) :
            PartialState()

        data class SetBlindPayEnabled(val isBlindPayEnabled: Boolean) : PartialState()

        data class SetJustPayEnabled(val isJustPayEnabled: Boolean) : PartialState()

        data class SetBlindPayLinkId(val blindPayLinkId: String) : PartialState()

        data class IsMerchantFromCollectionCampaign(val isMerchantFromCollectionCampaign: Boolean) : PartialState()

        data class IsPayOnlineEducationShown(val isPayOnlineEducationShown: Boolean) : PartialState()

        data class SetUnreadMessageCount(val unreadMessageCount: String, val firstUnseenMessageId: String?) :
            PartialState()

        data class CanShowChatNewSticker(val canShowChatNewSticker: Boolean) : PartialState()

        data class SetChatStatus(val isChatEnabled: Boolean) : PartialState()

        data class CanShowBillNewSticker(val canShowBillNewSticker: Boolean) : PartialState()

        data class SetBillStatus(val isBillEnabled: Boolean) : PartialState()

        data class SetBillUnseenCount(val setBillUnseenCount: Int) : PartialState()

        data class SetPayOnlineLoading(val loading: Boolean, val riskType: String) : PartialState()

        data class SetCashbackMessage(val cashbackMessage: String) : PartialState()

        data class SetOngoingPaymentAttribute(
            var ongoingPaymentAmount: Long,
            var ongoingPaymentId: String,
            var ongoingPaymentType: String,
        ) : PartialState()

        data class SetContextualHelpIds(val helpIds: List<String>) : PartialState()

        data class SetSupportType(val type: SupportType) : PartialState()
        data class SetPreNetworkOnboarding(val canShow: Boolean) : PartialState()

        data class SetShowTransactionSortSelection(val canShow: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()
        object Reload : Intent()

        // showSupplierConflict alert
        data class ShowAlert(val message: String) : Intent()

        // goto addTxn screen
        data class GoToAddTxn(val txnType: Int) : Intent()

        // view txn
        data class ViewTransaction(val txnId: String, val currentDue: Long) : Intent()

        // go to supplier profile
        object GoToSupplierProfile : Intent()

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

        object UpdateLastViewTime : Intent()

        object SetupMerchantProfile : Intent()

        data class RxPreferenceBoolean(val key: String, val value: Boolean, val scope: Scope) : Intent()

        data class SetTakeGiveCreditEducation(val canShow: Boolean) : Intent()

        // unblock supplier
        object ShowUnblockDialog : Intent()

        object Unblock : Intent()

        data class ShareAppPromotion(val bitmap: Bitmap, val sharingText: String) : Intent()

        object SupplierLearnMore : Intent()

        object ShowPayOnlineEducation : Intent()

        object NewShareReport : Intent()

        object GetRiskDetailsResponse : Intent()

        object PayOnline : Intent()

        object IsJuspayFeatureEnabled : Intent()

        object GetBlindPayLinkId : Intent()

        data class GotoSupplierEditAmountScreen(val kycStatus: KycStatus) : Intent()

        object GetCollectionProfileAfterSetDestination : Intent()

        data class SetOngoingPaymentAttribute(
            var ongoingPaymentAmount: Long,
            var ongoingPaymentId: String,
            var ongoingPaymentType: String,
        ) : Intent()

        object GoToDeletedTransaction : Intent()

        data class OpenExitDialog(val exitSource: String) : Intent()

        data class OnUpdateSortSelection(val sortSelection: String) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        object GotoLogin : ViewEvent()

        data class GotoAddTransaction(val supplierId: String, val txnType: Int) : ViewEvent()

        data class GotoTransactionScreen(val transaction: Transaction) : ViewEvent()

        data class GotoSupplierProfile(val supplierId: String) : ViewEvent()

        data class GotoCallSupplier(val mobile: String) : ViewEvent()

        data class GotoSupplierProfileForAddingMobile(val supplierId: String) : ViewEvent()

        object GotoSupplierPrivacyScreen : ViewEvent()

        object GoToMerchantProfileForSetupProfile : ViewEvent()

        data class GotoDeletedTransaction(val txnId: String) : ViewEvent()

        data class ShareReportIntent(val value: android.content.Intent) : ViewEvent()

        object ShowUnblockDialog : ViewEvent()

        object ShowPayOnlineEducation : ViewEvent()

        data class OpenWhatsAppPromotionShare(val intent: android.content.Intent) : ViewEvent()

        data class GoToSupplierLearnMoreWebLink(val value: String) : ViewEvent()

        object ShowChatEducation : ViewEvent()

        object GoToSupplierReport : ViewEvent()

        object ShowBillEducation : ViewEvent()

        object ShowBlindPayDialog : ViewEvent()

        data class ShowAddPaymentMethodDialog(val supplierId: String) : ViewEvent()

        data class GotoSupplierEditAmountScreen(
            val supplierId: String,
            val balance: Long,
            val remainingDailyAmount: Long,
            val maxDailyAmount: Long,
            val riskType: String,
            val kycStatus: KycStatus,
            val kycRiskCategory: KycRiskCategory,
            val futureAmountLimit: Long,
        ) : ViewEvent()

        data class GotoSupplierBlindPayEditAmountScreen(
            val supplierId: String,
            val balance: Long,
            val remainingDailyAmount: Long,
            val maxDailyAmount: Long,
            val riskType: String,
        ) : ViewEvent()

        data class ShowSupplierDestinationDialog(val supplierId: String) : ViewEvent()

        object OpenLimitReachedBottomSheet : ViewEvent()

        data class OpenExitDialog(val exitSource: String) : ViewEvent()
    }
}
