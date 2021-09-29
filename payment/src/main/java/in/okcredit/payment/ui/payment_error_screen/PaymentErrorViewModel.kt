package `in`.okcredit.payment.ui.payment_error_screen

import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.payment.ui.payment_error_screen.PaymentErrorFragment.Companion.ARG_ACCOUNT_ID
import `in`.okcredit.payment.ui.payment_error_screen.PaymentErrorFragment.Companion.ARG_ERROR_MSG
import `in`.okcredit.payment.ui.payment_error_screen.PaymentErrorFragment.Companion.ARG_PAYMENT_ERROR_TYPE
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class PaymentErrorViewModel @Inject constructor(
    private val initialState: PaymentErrorContract.State,
    @ViewModelParam(ARG_PAYMENT_ERROR_TYPE) val errorType: String,
    @ViewModelParam(ARG_ACCOUNT_ID) val accountId: String,
    @ViewModelParam(ARG_ERROR_MSG) val errorMessage: String,
    private val paymentAnalyticsEvents: Lazy<PaymentAnalyticsEvents>,
) : BaseViewModel<PaymentErrorContract.State, PaymentErrorContract.PartialState, PaymentErrorContract.ViewEvents>(
    initialState
) {
    override fun handle(): Observable<out UiState.Partial<PaymentErrorContract.State>> {
        return Observable.mergeArray(
            loadObservable(),
            retryObservable()
        )
    }

    private fun loadObservable(): Observable<PaymentErrorContract.PartialState>? {
        return intent<PaymentErrorContract.Intent.Load>()
            .map {
                paymentAnalyticsEvents.get().trackLoadedPaymentErrorPage(
                    accountId = accountId,
                    screen = PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION,
                    relation = initialState.getRelationFrmAccountType(),
                    flow = PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION,
                    type = errorType
                )
                PaymentErrorContract.PartialState.SetErrorType(PaymentErrorType.fromValue((errorType)), errorMessage)
            }
    }

    private fun retryObservable(): Observable<PaymentErrorContract.PartialState>? {
        return intent<PaymentErrorContract.Intent.OnRetry>()
            .map {
                paymentAnalyticsEvents.get().trackClickedRetryPayment(
                    accountId = accountId,
                    screen = PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION,
                    relation = initialState.getRelationFrmAccountType(),
                    flow = PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION,
                    type = errorType
                )
                emitViewEvent(PaymentErrorContract.ViewEvents.OnRetry)
                PaymentErrorContract.PartialState.NoChange
            }
    }

    override fun reduce(
        currentState: PaymentErrorContract.State,
        partialState: PaymentErrorContract.PartialState
    ): PaymentErrorContract.State {
        return when (partialState) {
            PaymentErrorContract.PartialState.NoChange -> currentState
            is PaymentErrorContract.PartialState.SetErrorType -> currentState.copy(
                errorType = partialState.errorType,
                errorMessage = partialState.errorMsg
            )
        }
    }
}
