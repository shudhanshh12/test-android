package `in`.okcredit.onboarding.businessname

import `in`.okcredit.merchant.contract.BusinessConstants
import `in`.okcredit.merchant.contract.Request
import `in`.okcredit.merchant.contract.UpdateBusiness
import `in`.okcredit.merchant.device.usecase.IsCollectionCampaign
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject

class BusinessNameViewModel @Inject constructor(
    initialState: BusinessNameContract.State,
    private val navigator: BusinessNameContract.Navigator,
    private val onboardingPreferences: Lazy<OnboardingPreferencesImpl>,
    private val isCollectionCampaign: Lazy<IsCollectionCampaign>,
    private val updateBusiness: Lazy<UpdateBusiness>,
) : BasePresenter<BusinessNameContract.State, BusinessNameContract.PartialState>(initialState) {

    override fun handle(): Observable<UiState.Partial<BusinessNameContract.State>> {
        return mergeArray(
            checkCollectionCampaignMerchantObservable(),
            intent<BusinessNameContract.Intent.BusinessName>()
                .switchMap {
                    UseCase.wrapCompletable(
                        updateBusiness.get().execute(
                            Request(BusinessConstants.BUSINESS_NAME, it.businessName)
                        )
                    )
                }
                .flatMap {
                    when (it) {
                        is Result.Progress -> Observable.just(BusinessNameContract.PartialState.ShowLoading)
                        is Result.Success -> {
                            navigator.goToHome()
                            rxCompletable { onboardingPreferences.get().setNameEntered() }
                                .andThen(Observable.just(BusinessNameContract.PartialState.HideLoading))
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    Observable.just(BusinessNameContract.PartialState.HideLoading)
                                }
                                isInternetIssue(it.error) -> {
                                    Observable.just(BusinessNameContract.PartialState.SetNetworkError)
                                }
                                else -> Observable.just(BusinessNameContract.PartialState.ErrorState)
                            }
                        }
                    }
                },
            intent<BusinessNameContract.Intent.NameSkipped>()
                .switchMap {
                    UseCase.wrapCompletable(rxCompletable { onboardingPreferences.get().setSkippedNameScreen(true) })
                }
                .map {
                    navigator.goToHome()
                    BusinessNameContract.PartialState.NoChange
                },
        )
    }

    private fun checkCollectionCampaignMerchantObservable(): Observable<BusinessNameContract.PartialState>? {
        return intent<BusinessNameContract.Intent.Load>()
            .switchMap { UseCase.wrapSingle(isCollectionCampaign.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> {
                        BusinessNameContract.PartialState.NoChange
                    }
                    is Result.Success -> {
                        BusinessNameContract.PartialState.IsMerchantFromCollectionCampaign(it.value)
                    }
                    is Result.Failure -> {
                        BusinessNameContract.PartialState.NoChange
                    }
                }
            }
    }

    override fun reduce(
        currentState: BusinessNameContract.State,
        partialState: BusinessNameContract.PartialState,
    ): BusinessNameContract.State {
        return when (partialState) {
            is BusinessNameContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is BusinessNameContract.PartialState.HideLoading -> currentState.copy(isLoading = false)
            is BusinessNameContract.PartialState.ErrorState -> currentState.copy(
                isLoading = false,
                networkError = false,
                error = true
            )
            is BusinessNameContract.PartialState.SetNetworkError -> currentState.copy(
                isLoading = false,
                networkError = true,
                error = false
            )
            is BusinessNameContract.PartialState.NoChange -> currentState
            is BusinessNameContract.PartialState.IsMerchantFromCollectionCampaign -> currentState.copy(
                isMerchantFromCollectionCampaign = partialState.isMerchantFromCollectionCampaign
            )
        }
    }
}
