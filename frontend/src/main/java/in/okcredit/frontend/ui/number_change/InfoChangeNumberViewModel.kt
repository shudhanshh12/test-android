package `in`.okcredit.frontend.ui.number_change

import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class InfoChangeNumberViewModel @Inject constructor(
    initialState: InfoChangeNumberContract.State,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    private val getActiveBusiness: GetActiveBusiness,
    private val navigator: InfoChangeNumberContract.Navigator
) : BasePresenter<InfoChangeNumberContract.State, InfoChangeNumberContract.PartialState>(initialState) {

    private val reload: PublishSubject<Unit> = PublishSubject.create()

    override fun handle(): Observable<UiState.Partial<InfoChangeNumberContract.State>> {
        return mergeArray(

            // hide network error when network becomes available
            intent<InfoChangeNumberContract.Intent.Load>()
                .switchMap { checkNetworkHealth.get().execute(Unit) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        reload.onNext(Unit)
                        InfoChangeNumberContract.PartialState.ClearNetworkError
                    } else {
                        InfoChangeNumberContract.PartialState.NoChange
                    }
                },

            intent<InfoChangeNumberContract.Intent.Load>()
                .flatMap { getActiveBusiness.execute() }
                .map {
                    InfoChangeNumberContract.PartialState.Number(it.mobile)
                },

            intent<InfoChangeNumberContract.Intent.VerifyAndChange>()
                .flatMap { getActiveBusiness.execute() }
                .map {
                    navigator.goToOTPVerificationScreen(it.mobile)
                    InfoChangeNumberContract.PartialState.NoChange
                }

        )
    }

    override fun reduce(
        currentState: InfoChangeNumberContract.State,
        partialState: InfoChangeNumberContract.PartialState
    ): InfoChangeNumberContract.State {
        return when (partialState) {
            is InfoChangeNumberContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is InfoChangeNumberContract.PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is InfoChangeNumberContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is InfoChangeNumberContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is InfoChangeNumberContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false
            )
            is InfoChangeNumberContract.PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is InfoChangeNumberContract.PartialState.SetLoaderStatus -> currentState.copy(isLoading = partialState.status)
            is InfoChangeNumberContract.PartialState.NoChange -> currentState
            is InfoChangeNumberContract.PartialState.Number -> currentState.copy(mobile = partialState.mobile)
        }
    }
}
