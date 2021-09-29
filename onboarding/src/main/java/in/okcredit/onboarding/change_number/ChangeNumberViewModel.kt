package `in`.okcredit.onboarding.change_number

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.onboarding.change_number.usecase.CheckNewNumberValid
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class ChangeNumberViewModel @Inject constructor(
    initialState: ChangeNumberContract.State,
    private val checkNetworkHealth: CheckNetworkHealth,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val tracker: Tracker,
    private val checkNewNumberValid: CheckNewNumberValid,
    private val navigator: ChangeNumberContract.Navigator,
) : BasePresenter<ChangeNumberContract.State, ChangeNumberContract.PartialState>(initialState) {

    private val reload: PublishSubject<Unit> = PublishSubject.create()
    private var merchantId = ""

    override fun handle(): Observable<UiState.Partial<ChangeNumberContract.State>> {
        return mergeArray(

            // hide network error when network becomes available
            intent<ChangeNumberContract.Intent.Load>()
                .switchMap { checkNetworkHealth.execute(Unit) }
                .switchMap { UseCase.wrapSingle(getActiveBusinessId.get().execute()) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        merchantId = it.value
                        reload.onNext(Unit)
                        ChangeNumberContract.PartialState.ClearNetworkError
                    } else {
                        ChangeNumberContract.PartialState.NoChange
                    }
                },

            intent<ChangeNumberContract.Intent.NewNumberEntered>()
                .switchMap {
                    UseCase.wrapSingle(checkNewNumberValid.execute(CheckNewNumberValid.Request(mobile = it.newNumber)))
                }
                .map {
                    when (it) {
                        is Result.Progress -> {
                            ChangeNumberContract.PartialState.ShowLoading
                        }
                        is Result.Success -> {

                            navigator.goToChangeNumberConfirmationScreen()
                            ChangeNumberContract.PartialState.SetLoaderStatus(false)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    ChangeNumberContract.PartialState.NoChange
                                }
                                it.error.message == "merchant_registered" || it.error.message == "merchant_exists" ||
                                    it.error.message == "user_registered" || it.error.message == "user_exists" -> {
                                    tracker.trackError("Mobile Update", PropertyValue.EXISTING_NUMBER)
                                    ChangeNumberContract.PartialState.SetMerchantExists
                                }
                                isInternetIssue(it.error) -> ChangeNumberContract.PartialState.SetNetworkError(true)
                                else -> ChangeNumberContract.PartialState.ErrorState
                            }
                        }
                    }
                }
        )
    }

    override fun reduce(
        currentState: ChangeNumberContract.State,
        partialState: ChangeNumberContract.PartialState,
    ): ChangeNumberContract.State {
        return when (partialState) {
            is ChangeNumberContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is ChangeNumberContract.PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is ChangeNumberContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is ChangeNumberContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is ChangeNumberContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false
            )
            is ChangeNumberContract.PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is ChangeNumberContract.PartialState.SetLoaderStatus -> currentState.copy(isLoading = partialState.status)
            is ChangeNumberContract.PartialState.NoChange -> currentState
            is ChangeNumberContract.PartialState.SetMerchantExists -> currentState.copy(
                merchantAlreadyExistsError = true,
                isLoading = false
            )
        }
    }
}
