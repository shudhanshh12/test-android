package `in`.okcredit.collection_ui.ui.home.merchant_qr

import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.collection_ui.ui.home.usecase.FindInfoBannerForMerchantQr
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import android.content.Intent
import androidx.annotation.StringRes

interface QrCodeContract {

    data class State(
        val loading: Boolean = false,
        val responseData: ResponseData = ResponseData(),
        val infoBanner: FindInfoBannerForMerchantQr.InfoBanner = FindInfoBannerForMerchantQr.InfoBanner.None,
        val merchantCollectionState: MerchantCollectionState = MerchantCollectionState(),
        val showAddBankDetails: Boolean = false, // show add bank details if merchant payment is blank or there is a refund alert
        val showOnlinePayments: Boolean = false,
        val onlinePaymentState: OnlinePaymentState = OnlinePaymentState(),
        val showOrderQr: Boolean = false,
    ) : UiState

    data class MerchantCollectionState(
        val merchantName: String = "",
        val merchantProfileImage: String? = null,
        val paymentAddress: String? = null,
        val qrIntent: String? = null,
        val showPaymentViews: Boolean = true, // show other payment views only if merchant payment is not blank, there is no refund alert
        val showQrLocked: Boolean = false,
    )

    data class OnlinePaymentState(
        val newCount: Int = 0,
        val amount: Double = 0.0,
        val type: String? = null,
    )

    data class ResponseData(
        val collectionMerchantProfile: CollectionMerchantProfile = CollectionMerchantProfile.empty(),
        val business: Business? = null,
        val kycStatus: KycStatus = KycStatus.NOT_SET,
        val kycRiskCategory: KycRiskCategory = KycRiskCategory.NO_RISK,
        val liveSalesStatus: Boolean = false,
        val isSetPassword: Boolean = false,
        val isFourDigitPinSet: Boolean = false,
        val isMerchantPrefSync: Boolean = false,
        val totalOnlinePaymentTxnCount: Int = 0,
        val unSettleAmountDueToInvalidBank: Double = 0.0,
        val showReferralBanner: Boolean = false,
    )

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        object ErrorState : PartialState()

        data class SetCollectionMerchantProfile(val collectionMerchantProfile: CollectionMerchantProfile) :
            PartialState()

        data class SetBusiness(val business: Business) : PartialState()

        data class SetShowOrderQr(val show: Boolean) : PartialState()

        data class SetKycRiskCategory(
            val kycStatus: KycStatus,
            val kycRiskCategory: KycRiskCategory,
            val isKycLimitReached: Boolean,
        ) : PartialState()

        data class SetNewCount(val count: Int) : PartialState()

        data class SetIsPasswordEnabled(val isSetPassword: Boolean) : PartialState()

        data class SetIsFourDigitPin(val isFourDigitPinSet: Boolean) : PartialState()

        data class SetIsMerchantPrefSync(val isMerchantPrefSync: Boolean) : PartialState()

        data class SetEducationType(val educationType: EducationType) : PartialState()

        data class SetUnSettleAmountDueToInvalidBank(val amount: Double) : PartialState()

        data class ShouldShowReferralBanner(val show: Boolean) : PartialState()

        data class SetTotalOnlinePaymentCounts(val count: Int) : PartialState()

        data class SetLatestPayment(val onlinePayment: CollectionOnlinePayment) : PartialState()

        data class SetInfoBanner(val infoBanner: FindInfoBannerForMerchantQr.InfoBanner) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object LoadQrFirst : Intent()

        object LoadCollectionProfileMerchant : Intent()

        object LoadMerchant : Intent()

        object LoadKycCompleted : Intent()

        object LoadNewOnlinePaymentsCount : Intent()

        object LoadOnlinePaymentTotal : Intent()

        object LoadKycDetails : Intent()

        object CheckPasswordSet : Intent()

        object DeleteAccountPressed : Intent()

        object DeleteAccount : Intent()

        object OpenBottomSheetDialog : Intent()

        object ShareMerchantQR : Intent()

        object SendMerchantQR : Intent()

        object SaveMerchantQR : Intent()

        data class CheckIsFourDigit(val fromLoad: Boolean = false) : Intent()

        object SyncMerchantPref : Intent()

        object SetNewPin : Intent()

        object UpdatePin : Intent()

        object ShowAddMerchantDestinationScreen : Intent()

        object CheckCollectionActivationAfterDeletion : Intent()

        data class TriggerMerchantPayout(val payoutType: String) : Intent()

        data class SendCustomerReminder(val customerId: String) : Intent()

        data class SupplierPayOnline(val supplierId: String) : Intent()

        object OpenWhatsAppForHelp : Intent()

        object GotoReferralScreen : Intent()

        object InfoCardTapped : Intent()

        object OrderMerchantQR : Intent()

        object QrTapped : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GotoLogin : ViewEvent()
        object DeleteAccount : ViewEvent()
        object OpenBottomSheetDialog : ViewEvent()
        object GoToCollectionsSetupScreen : ViewEvent()
        data class ShowSnackBar(@StringRes val msg: Int) : ViewEvent()
        data class ShareMerchant(val intent: android.content.Intent) : ViewEvent()
        data class CheckFourDigitPinDone(val isFourDigitPinSet: Boolean) : ViewEvent()
        object SyncDone : ViewEvent()
        object GoToSetNewPinScreen : ViewEvent()
        object ShowUpdatePinDialog : ViewEvent()
        object GoToAuthScreen : ViewEvent()
        data class SendReminder(val intent: android.content.Intent) : ViewEvent()
        data class OpenSupplierScreen(val screen: String) : ViewEvent()
        data class OpenWhatsAppForHelp(val value: android.content.Intent) : ViewEvent()
        object ShowAddMerchantDestinationScreen : ViewEvent()
        object GotoReferralEducationScreen : ViewEvent()
        object GotoReferralInviteListScreen : ViewEvent()
        object GoToKyc : ViewEvent()
        object OpenOrderQr : ViewEvent()
    }

    enum class Education {
        OnlineCollection, Menu, SaveSend, None
    }

    enum class EducationType {
        TapTarget, ToolTip
    }
}
