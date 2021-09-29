package `in`.okcredit.merchant.customer_ui.ui.discount_info

import `in`.okcredit.merchant.customer_ui.ui.discount_info.CustomerAddTxnDiscountInfoDialogContract.*
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class CustomerAddTxnDiscountInfoDialogViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam("amount") val amount: String,
    @ViewModelParam("discounted_amount") val discounted_amount: String
) : BaseViewModel<State, PartialState, ViewEvents>(initialState) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Intent.Load>()
                .map {
                    PartialState.SetData(
                        amount,
                        discounted_amount,
                        amount.toLong().minus(discounted_amount.toLong()).toString()
                    )
                }
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is PartialState.SetData -> currentState.copy(
                creditAmount = partialState.creditAmount,
                discountAmount = partialState.discountAmount,
                netAmount = partialState.netAmount
            )
        }
    }
}
