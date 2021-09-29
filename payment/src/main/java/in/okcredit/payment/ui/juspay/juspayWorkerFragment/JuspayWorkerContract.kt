package `in`.okcredit.payment.ui.juspay.juspayWorkerFragment

import `in`.okcredit.payment.contract.ApiErrorType
import `in`.okcredit.payment.contract.JuspayErrorType
import `in`.okcredit.payment.contract.model.PaymentAttributes
import `in`.okcredit.payment.server.internal.PaymentApiMessages
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface JuspayWorkerContract {

    data class State(
        val getJuspayInitiateResponse: PaymentApiMessages.GetJuspayAttributesResponse? = null,
        val getJuspayProcessResponse: PaymentApiMessages.GetJuspayAttributesResponse? = null,
        val getPaymentAttributesResponse: PaymentAttributes? = null,
        val juspayWorkerState: JuspayWorkerState = JuspayWorkerState.JUSPAY_INITIATE_STARTED,
        val apiErrorType: ApiErrorType = ApiErrorType.NONE,
        val juspayErrorType: JuspayErrorType = JuspayErrorType.NONE,
    ) : UiState

    enum class JuspayWorkerState(val value: Int) {
        JUSPAY_NO_STATE(0),
        JUSPAY_INITIATE_STARTED(1),
        JUSPAY_INITIATE_FINISHED(2),
        JUSPAY_PROCESS_STARTED(3),
        JUSPAY_SDK_OPENED(4),
        JUSPAY_PROCESS_FINISHED(5),
        API_ERROR(6);

        companion object {
            val map = values().associateBy(JuspayWorkerState::value)
            fun fromValue(value: Int) = map[value] ?: JUSPAY_INITIATE_STARTED
        }
    }

    enum class JuspayPspFeature(val value: Int) {
        PROFILE(0),
        INCOMING_INTENT(1),
        APPROVE_COLLECT_REQUEST(2);

        companion object {
            val map = values().associateBy(JuspayPspFeature::value)
            fun fromValue(value: Int) = map[value] ?: PROFILE
        }
    }

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        data class SetJuspayAttributesResponse(val getJuspayAttributesResponse: PaymentApiMessages.GetJuspayAttributesResponse?) :
            PartialState()

        data class SetJuspayProcessResponse(val getJuspayAttributesResponse: PaymentApiMessages.GetJuspayAttributesResponse?) :
            PartialState()

        data class SetJuspayPaymentAttributes(val getPaymentAttributesResponse: PaymentAttributes) :
            PartialState()

        data class SetJuspayWorkerState(
            val state: JuspayWorkerState,
            val errorType: JuspayErrorType = JuspayErrorType.NONE,
        ) : PartialState()

        data class SetApiErrorState(val errorType: ApiErrorType) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object GetJuspayProcessPayload : Intent()
        data class GetPaymentAttribute(val linkId: String, val amount: Long) : Intent()
        data class SetJuspayWorkerState(
            val state: JuspayWorkerState,
            val juspayErrorType: JuspayErrorType = JuspayErrorType.NONE,
        ) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent
}
