package `in`.okcredit.payment.ui.juspay.juspayWorkerFragment

import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.GET_JUSPAY_ATTRIBUTE_INITIATE
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.GET_JUSPAY_ATTRIBUTE_PROCESS
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.GET_PAYMENT_ATTRIBUTE
import `in`.okcredit.payment.contract.ApiErrorType
import `in`.okcredit.payment.contract.model.PaymentAttributes
import `in`.okcredit.payment.contract.usecase.GetPaymentAttributeFromServer
import `in`.okcredit.payment.usecases.GetJuspayInitiateAttributeFromServer
import `in`.okcredit.payment.usecases.GetJuspayProcessPayloadFromServer
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import javax.inject.Inject

class JuspayWorkerViewModel @Inject constructor(
    initialState: JuspayWorkerContract.State,
    private val getJuspayAttributesResponse: Lazy<GetJuspayInitiateAttributeFromServer>,
    private val getJuspayProcessPayloadFromServer: Lazy<GetJuspayProcessPayloadFromServer>,
    private val getPaymentAttributeFromServer: Lazy<GetPaymentAttributeFromServer>,
    private val paymentAnalyticsEvents: Lazy<PaymentAnalyticsEvents>,
) : BaseViewModel<JuspayWorkerContract.State, JuspayWorkerContract.PartialState, JuspayWorkerContract.ViewEvent>(
    initialState
) {

    internal var paymentAttributesResponse: PaymentAttributes? = null
    internal var linkId: String = ""
    internal var amount: Long = 0L

    override fun handle(): Observable<out UiState.Partial<JuspayWorkerContract.State>> {
        return mergeArray(
            fetchJuspayInitiateDataFromServer(),
            getPaymentAttribute(),
            fetchJuspayProcessPayloadFromServer(),
            setJuspayWorkerState()
        )
    }

    private fun fetchJuspayInitiateDataFromServer(): Observable<JuspayWorkerContract.PartialState>? {
        return intent<JuspayWorkerContract.Intent.Load>()
            .take(1)
            .switchMap {
                getJuspayAttributesResponse.get().execute()
            }
            .map {
                when (it) {
                    is Result.Progress -> JuspayWorkerContract.PartialState.SetJuspayWorkerState(JuspayWorkerContract.JuspayWorkerState.JUSPAY_NO_STATE)
                    is Result.Success -> {
                        JuspayWorkerContract.PartialState.SetJuspayAttributesResponse(it.value)
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                JuspayWorkerContract.PartialState.SetApiErrorState(ApiErrorType.AUTH)
                            }
                            isInternetIssue(it.error) ->
                                JuspayWorkerContract.PartialState.SetApiErrorState(ApiErrorType.NETWORK)

                            else -> {
                                paymentAnalyticsEvents.get()
                                    .trackPaymentFlowApiError(
                                        "", it.error.message ?: "",
                                        GET_JUSPAY_ATTRIBUTE_INITIATE
                                    )
                                JuspayWorkerContract.PartialState.SetApiErrorState(ApiErrorType.OTHER)
                            }
                        }
                    }
                }
            }
    }

    private fun getPaymentAttribute(): Observable<JuspayWorkerContract.PartialState> =
        intent<JuspayWorkerContract.Intent.GetPaymentAttribute>()
            .switchMap {
                linkId = it.linkId
                amount = it.amount
                wrap(getPaymentAttributeFromServer.get().execute("APP", it.linkId))
            }
            .map {
                when (it) {
                    is Result.Progress -> JuspayWorkerContract.PartialState.NoChange
                    is Result.Success -> {
                        paymentAttributesResponse = it.value
                        pushIntent(JuspayWorkerContract.Intent.GetJuspayProcessPayload)
                        JuspayWorkerContract.PartialState.SetJuspayPaymentAttributes(it.value)
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                JuspayWorkerContract.PartialState.SetApiErrorState(ApiErrorType.AUTH)
                            }
                            isInternetIssue(it.error) ->
                                JuspayWorkerContract.PartialState.SetApiErrorState(ApiErrorType.NETWORK)

                            else -> {
                                paymentAnalyticsEvents.get()
                                    .trackPaymentFlowApiError(
                                        "", it.error.message ?: "",
                                        GET_PAYMENT_ATTRIBUTE
                                    )
                                JuspayWorkerContract.PartialState.SetApiErrorState(ApiErrorType.OTHER)
                            }
                        }
                    }
                }
            }

    private fun fetchJuspayProcessPayloadFromServer() = intent<JuspayWorkerContract.Intent.GetJuspayProcessPayload>()
        .switchMap {
            paymentAttributesResponse?.let {
                getJuspayProcessPayloadFromServer.get().execute(
                    paymentId = it.paymentId,
                    amount = amount.toDouble().div(100.0),
                    linkId = linkId
                )
            }
        }
        .map {
            when (it) {
                is Result.Progress -> JuspayWorkerContract.PartialState.NoChange
                is Result.Success -> {
                    JuspayWorkerContract.PartialState.SetJuspayProcessResponse(it.value)
                }
                is Result.Failure -> {
                    when {
                        isAuthenticationIssue(it.error) -> {
                            JuspayWorkerContract.PartialState.SetApiErrorState(ApiErrorType.AUTH)
                        }
                        isInternetIssue(it.error) ->
                            JuspayWorkerContract.PartialState.SetApiErrorState(ApiErrorType.NETWORK)

                        else -> {
                            paymentAnalyticsEvents.get()
                                .trackPaymentFlowApiError(
                                    "", it.error.message ?: "",
                                    GET_JUSPAY_ATTRIBUTE_PROCESS
                                )
                            JuspayWorkerContract.PartialState.SetApiErrorState(ApiErrorType.OTHER)
                        }
                    }
                }
            }
        }

    private fun setJuspayWorkerState() = intent<JuspayWorkerContract.Intent.SetJuspayWorkerState>()
        .map {
            JuspayWorkerContract.PartialState.SetJuspayWorkerState(it.state, it.juspayErrorType)
        }

    override fun reduce(
        currentState: JuspayWorkerContract.State,
        partialState: JuspayWorkerContract.PartialState
    ): JuspayWorkerContract.State {
        return when (partialState) {
            is JuspayWorkerContract.PartialState.NoChange -> currentState
            is JuspayWorkerContract.PartialState.SetJuspayAttributesResponse -> currentState.copy(
                getJuspayInitiateResponse = partialState.getJuspayAttributesResponse,
                juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.JUSPAY_INITIATE_STARTED
            )
            is JuspayWorkerContract.PartialState.SetJuspayProcessResponse -> currentState.copy(
                getJuspayProcessResponse = partialState.getJuspayAttributesResponse,
                juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_STARTED
            )
            is JuspayWorkerContract.PartialState.SetJuspayWorkerState -> currentState.copy(
                juspayWorkerState = partialState.state,
                juspayErrorType = partialState.errorType
            )
            is JuspayWorkerContract.PartialState.SetApiErrorState -> currentState.copy(
                juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.API_ERROR,
                apiErrorType = partialState.errorType
            )
            is JuspayWorkerContract.PartialState.SetJuspayPaymentAttributes -> currentState.copy(
                getPaymentAttributesResponse = partialState.getPaymentAttributesResponse
            )
        }
    }
}
