package `in`.okcredit.merchant.customer_ui.ui.customer.bottom_sheet.googlePay

import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.collection.contract.GetCollectionMerchantProfile
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.ui.customer.bottom_sheet.googlePay.CollectWithGooglePayContract.*
import `in`.okcredit.merchant.customer_ui.usecase.SendCollectWithGooglePay
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.network.ApiError
import javax.inject.Inject
import kotlin.math.absoluteValue

class CollectWithGooglePayViewModel @Inject constructor(
    @ViewModelParam(CollectWithGooglePayContract.ARG_CUSTOMER_ID) private val customerId: String,
    initialState: State,
    private val getCollectionMerchantProfile: Lazy<GetCollectionMerchantProfile>,
    private val getCustomer: Lazy<GetCustomer>,
    private val sendCollectWithGooglePay: Lazy<SendCollectWithGooglePay>,
) : BaseViewModel<State, PartialState, ViewEvents>(
    initialState
) {
    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            setEnteredAmount(),
            observeMerchantCollectionProfile(),
            observeCustomer(),
            collectWithGooglePayRequest(),
        )
    }

    private fun collectWithGooglePayRequest() = intent<Intent.CollectWithGooglePay>()
        .switchMap {
            val amount = it.amount.absoluteValue // convert to absolute value
            wrap(
                sendCollectWithGooglePay.get().execute(
                    customerId = customerId,
                    mobile = it.customerMobile,
                    amount = amount
                )
            )
        }
        .map {
            when (it) {
                is Result.Success -> {
                    emitViewEvent(ViewEvents.CollectWithGPayRequestSent)
                }
                is Result.Failure -> {
                    when {
                        isInternetIssue(it.error) -> emitViewEvent(ViewEvents.ShowError(R.string.interent_error))
                        customerNotPresentOnGpay(it.error) -> {
                            emitViewEvent(ViewEvents.ShowError(R.string.customer_not_present_on_gpay))
                            emitViewEvent(ViewEvents.DismissBottomSheet)
                        }
                        requestAlreadySent(it.error) -> {
                            emitViewEvent(ViewEvents.ShowError(R.string.collect_on_gpay_request_already_sent))
                            emitViewEvent(ViewEvents.DismissBottomSheet)
                        }
                        else -> emitViewEvent(ViewEvents.ShowError(R.string.err_default))
                    }
                }
                else -> PartialState.NoChange
            }
            PartialState.NoChange
        }

    private fun requestAlreadySent(error: Throwable): Boolean {
        return (error is ApiError && error.code == 409)
    }

    private fun customerNotPresentOnGpay(error: Throwable): Boolean {
        return (error is ApiError && error.code == 404)
    }

    private fun observeCustomer() = intent<Intent.Load>().switchMap {
        wrap(getCustomer.get().execute(customerId))
    }.map {
        return@map if (it is Result.Success) {
            PartialState.SetCustomer(it.value)
        } else {
            PartialState.NoChange
        }
    }

    private fun observeMerchantCollectionProfile() = intent<Intent.Load>().switchMap {
        wrap(getCollectionMerchantProfile.get().execute())
    }.map {
        return@map if (it is Result.Success) {
            PartialState.SetMerchantProfile(it.value)
        } else {
            PartialState.NoChange
        }
    }

    private fun setEnteredAmount(): Observable<PartialState>? {
        return intent<Intent.SetAmountEntered>()
            .map {
                PartialState.SetAmountEntered(it.amount)
            }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            PartialState.NoChange -> currentState
            is PartialState.SetAmountEntered -> currentState.copy(
                currentAmountSelected = partialState.amount
            )
            is PartialState.SetCustomer -> currentState.copy(
                accountId = partialState.customer.id,
                customerMobile = partialState.customer.mobile ?: "",
                dueBalance = partialState.customer.balanceV2.absoluteValue,
            )
            is PartialState.SetMerchantProfile -> currentState.copy(
                paymentAddress = partialState.merchantProfile.payment_address,
                destinationType = partialState.merchantProfile.type,
                name = partialState.merchantProfile.name ?: "",
            )
        }
    }
}
