package `in`.okcredit.payment.ui.payment_result

import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.payment.contract.model.PaymentModel
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import android.graphics.Bitmap
import merchant.okcredit.accounting.contract.model.LedgerType
import merchant.okcredit.accounting.contract.model.SupportType

interface PaymentResultContract {

    data class State(
        val juspayPaymentPollingModel: PaymentModel.JuspayPaymentPollingModel? = null,
        val uiScreenType: UiScreenType = UiScreenType.LOADING,
        val loadingTimerState: LoadingTimerState = LoadingTimerState.TimerNotSet,
        val riskType: String = "",
        val accountId: String = "",
        val paymentAddress: String = "",
        val destinationType: String = "",
        val name: String = "",
        val mobile: String = "",
        val accountType: String = "",
        val reward: RewardModel? = null,
        val blindPayFlow: Boolean = false,
        val blindPayShareLink: String = "",
        val supportType: SupportType = SupportType.NONE,
        val supportNumber: String = "",
        val support24x7String: String = "",
    ) : UiState {
        fun getRelationFrmAccountType(): String {
            return if (accountType == LedgerType.SUPPLIER.value) PaymentAnalyticsEvents.PaymentPropertyValue.SUPPLIER
            else PaymentAnalyticsEvents.PaymentPropertyValue.CUSTOMER
        }

        fun isSupplier(): Boolean = (accountType == LedgerType.SUPPLIER.value)
    }

    enum class UiScreenType(val value: String) {
        LOADING("LOADING"),
        SUCCESS("SUCCESS"),
        FAILED("FAILED"),
        PENDING("PENDING"),
        CANCELLED("CANCELLED");

        companion object {
            val map = values().associateBy(UiScreenType::value)
            fun fromValue(value: String) = map[value] ?: PENDING
        }
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class SetJuspayPollingResponse(
            val response: PaymentModel.JuspayPaymentPollingModel?,
            val uiScreenType: UiScreenType,
        ) : PartialState()

        data class SetTimerState(val loadingTimerState: LoadingTimerState) : PartialState()

        data class SetRewardForPayment(val reward: RewardModel) : PartialState()

        data class SetBlindPayShareLink(val blindPayShareLink: String) : PartialState()

        data class SetSupportData(
            val supportType: SupportType,
            val supportNumber: String,
            val support24x7String: String,
        ) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object LoadData : Intent()
        object StartLoadingTimer : Intent()
        object HaltLoadingTimer : Intent()
        data class LoadRewardForPayment(val paymentId: String) : Intent()
        data class ShareScreenShot(val bitmap: Bitmap, val sharingText: String?) : Intent()
        object CopyTxnIdToClipBoard : Intent()
        object ClickedShareOrRetry : Intent()
        object TrackPaymentRewardStatusPageView : Intent()
        object TrackPaymentRewardClicked : Intent()
        object SetRewardPopUpShownAtLeastOnce : Intent()
        data class GetBlindPayShareLink(val paymentId: String) : Intent()
        object SyncCollection : Intent()
        data class SupportClicked(val msg: String, val number: String) : Intent()
        data class SendWhatsAppMessage(val msg: String, val number: String) : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        object GoToLogin : ViewEvents()

        data class GoToClaimRewardScreen(val reward: RewardModel) : ViewEvents()

        data class OpenWhatsAppPromotionShare(val intent: android.content.Intent) : ViewEvents()

        data class ShowToast(val msg: String) : ViewEvents()

        data class CopyTxnIdToClipBoard(val txnId: String) : ViewEvents()

        data class TakeScreenShot(val shareLink: String? = null) : ViewEvents()

        object RetryPayment : ViewEvents()

        object NetworkError : ViewEvents()

        object OtherError : ViewEvents()

        object ShowWhatsAppError : ViewEvents()

        object ShowDefaultError : ViewEvents()

        data class SendWhatsAppMessage(val intent: android.content.Intent) : ViewEvents()

        object CallCustomerCare : ViewEvents()
    }

    sealed class LoadingTimerState {
        object TimerNotSet : LoadingTimerState()
        data class TimerSet(val countDownValue: Int) : LoadingTimerState()

        companion object {
            const val TIMER_COUNT_DOWN_LIMIT = 12
        }

        fun getTimeElapsed(): Int {
            return when (this) {
                is TimerSet -> TIMER_COUNT_DOWN_LIMIT - this.countDownValue
                is TimerNotSet -> 0
            }
        }
    }
}
