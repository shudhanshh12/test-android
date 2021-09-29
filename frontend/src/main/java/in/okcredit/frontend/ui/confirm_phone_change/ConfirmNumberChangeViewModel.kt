package `in`.okcredit.frontend.ui.confirm_phone_change

import `in`.okcredit.frontend.ui.confirm_phone_change.ConfirmNumberChangeContract.PartialState
import `in`.okcredit.frontend.ui.confirm_phone_change.ConfirmNumberChangeContract.State
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class ConfirmNumberChangeViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam("temp_new_number") val tempNewNumber: String,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    private val getActiveBusiness: GetActiveBusiness,
    private val navigator: ConfirmNumberChangeContract.Navigator
) : BasePresenter<State, PartialState>(initialState) {

    private val reload: PublishSubject<Unit> = PublishSubject.create()

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(

            // hide network error when network becomes available
            intent<ConfirmNumberChangeContract.Intent.Load>()
                .switchMap { checkNetworkHealth.get().execute(Unit) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        reload.onNext(Unit)
                        PartialState.ClearNetworkError
                    } else {
                        PartialState.NoChange
                    }
                },

            intent<ConfirmNumberChangeContract.Intent.Load>()
                .switchMap { getActiveBusiness.execute() }
                .map {
                    PartialState.MerchantStats(it, tempNewNumber)
                },

            // hide network error when network becomes available

            intent<ConfirmNumberChangeContract.Intent.VerfiyAndChange>()
                .map {
                    navigator.goToOTPVerificationScreen(tempNewNumber)
                    PartialState.NoChange
                }
        )
    }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is PartialState.ShowAlert -> currentState.copy(isAlertVisible = true, alertMessage = partialState.message)
            is PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false
            )
            is PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is PartialState.SetLoaderStatus -> currentState.copy(isLoading = partialState.status)
            is PartialState.NoChange -> currentState
            is PartialState.MerchantStats -> currentState.copy(
                business = partialState.business,
                tempNewNumber = partialState.tempNewNumber
            )
        }
    }
}
