package `in`.okcredit.frontend.ui.know_more

import `in`.okcredit.backend._offline.usecase.SubmitFeedbackImpl
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.frontend.usecase.GetKnowMoreVideoLinks
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.supplier.usecase.GetSupplier
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import timber.log.Timber
import javax.inject.Inject

class KnowMoreViewModel @Inject constructor(
    initialState: KnowMoreContract.State,
    @ViewModelParam("id") val id: String,
    private val submitFeedback: SubmitFeedbackImpl,
    private val getCustomer: GetCustomer,
    private val getSupplier: GetSupplier,
    @ViewModelParam("account_type") val accountType: String,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    private val getActiveBusiness: GetActiveBusiness,
    private val navigator: KnowMoreContract.Navigator,
    private val getKnowMoreVideoLinks: GetKnowMoreVideoLinks
) : BasePresenter<KnowMoreContract.State, KnowMoreContract.PartialState>(initialState) {

    private val reload: PublishSubject<Unit> = PublishSubject.create()

    override fun handle(): Observable<UiState.Partial<KnowMoreContract.State>> {
        return mergeArray(

            // hide network error when network becomes available
            intent<KnowMoreContract.Intent.Load>()
                .switchMap { checkNetworkHealth.get().execute(Unit) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        reload.onNext(Unit)
                        KnowMoreContract.PartialState.ClearNetworkError
                    } else {
                        KnowMoreContract.PartialState.NoChange
                    }
                },

            intent<KnowMoreContract.Intent.Load>()
                .switchMap { getKnowMoreVideoLinks.execute(Unit) }
                .map {
                    when (it) {
                        is Result.Success -> {
                            KnowMoreContract.PartialState.SetVideos(
                                it.value.commonLedgerBuyerVideo,
                                it.value.commonLedgerSellerVideo
                            )
                        }
                        else -> KnowMoreContract.PartialState.NoChange
                    }
                },

            intent<KnowMoreContract.Intent.Load>()
                .switchMap { getActiveBusiness.execute() }
                .map {

                    KnowMoreContract.PartialState.SetBusiness(it.name, it.profileImage)
                },

            intent<KnowMoreContract.Intent.Load>()
                .take(1)
                .map {
                    KnowMoreContract.PartialState.SetAccountTypeAndID(id, accountType)
                },

            // load customer
            intent<KnowMoreContract.Intent.Load>()
                .switchMap {
                    if (accountType == "customer")
                        UseCase.wrapObservable(getCustomer.execute(id))
                    else if (accountType == "supplier") {
                        getSupplier.execute(id)
                    } else {
                        UseCase.wrapCompletable(Completable.complete())
                    }
                }
                .map {
                    when (it) {
                        is Result.Progress -> KnowMoreContract.PartialState.NoChange
                        is Result.Success -> {
                            if (it.value is Supplier) {
                                KnowMoreContract.PartialState.SetCustomer(
                                    (it.value as Supplier).name,
                                    (it.value as Supplier).profileImage
                                )
                            } else if (it.value is Customer) {
                                KnowMoreContract.PartialState.SetCustomer(
                                    (it.value as Customer).description,
                                    (it.value as Customer).profileImage
                                )
                            } else {
                                KnowMoreContract.PartialState.NoChange
                            }
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    KnowMoreContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> KnowMoreContract.PartialState.SetNetworkError(true)
                                else -> {
                                    Timber.e(it.error, "ErrorState")
                                    KnowMoreContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            intent<KnowMoreContract.Intent.SubmitFeedback>()
                .switchMap { UseCase.wrapCompletable(submitFeedback.schedule(it.feedback, it.rating)) }
                .map {
                    navigator.goBackAfterAnimation()
                    KnowMoreContract.PartialState.NoChange
                }

        )
    }

    override fun reduce(
        currentState: KnowMoreContract.State,
        partialState: KnowMoreContract.PartialState
    ): KnowMoreContract.State {
        return when (partialState) {
            is KnowMoreContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is KnowMoreContract.PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is KnowMoreContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is KnowMoreContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is KnowMoreContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false
            )
            is KnowMoreContract.PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is KnowMoreContract.PartialState.SetLoaderStatus -> currentState.copy(isLoading = partialState.status)
            is KnowMoreContract.PartialState.NoChange -> currentState
            is KnowMoreContract.PartialState.SetAccountTypeAndID -> currentState.copy(
                accountID = partialState.id,
                accountType = partialState.accountType
            )
            is KnowMoreContract.PartialState.SetBusiness -> currentState.copy(
                merchantName = partialState.name,
                merchantPic = partialState.profileImage
            )
            is KnowMoreContract.PartialState.SetCustomer -> currentState.copy(
                customerName = partialState.name,
                customerPic = partialState.profileImage
            )
            is KnowMoreContract.PartialState.SetVideos -> currentState.copy(
                commonLedgerBuyerVideo = partialState.commonLedgerBuyerVideo,
                commonLedgerSellerVideo = partialState.commonLedgerSellerVideo
            )
        }
    }
}
