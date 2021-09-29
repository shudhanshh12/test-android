package tech.okcredit.help.help_details

import `in`.okcredit.backend.contract.SubmitFeedback
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import tech.okcredit.app_contract.AppConstants
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.userSupport.SupportRepository
import tech.okcredit.userSupport.data.LikeState
import timber.log.Timber
import javax.inject.Inject

class HelpDetailsViewModel @Inject constructor(
    initialState: HelpDetailsContract.State,
    private val userSupport: Lazy<SupportRepository>,
    private val submitFeedback: Lazy<SubmitFeedback>,
    @ViewModelParam(AppConstants.ARG_SOURCE) val source: String,
    @ViewModelParam(HELP_ITEM_ID) val helpItemId: String,
    private val checkNetworkHealth: CheckNetworkHealth
) : BaseViewModel<HelpDetailsContract.State, HelpDetailsContract.PartialState, HelpDetailsContract.ViewEvent>(initialState) {

    companion object {
        const val HELP_ITEM_ID = "help_item_id"
        const val ARG_SOURCE = "source"
    }

    private val reload: PublishSubject<Unit> = PublishSubject.create()

    override fun handle(): Observable<UiState.Partial<HelpDetailsContract.State>> {
        return mergeArray(

            // hide network error when network becomes available
            intent<HelpDetailsContract.Intent.Load>()
                .switchMap { checkNetworkHealth.execute(Unit) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        reload.onNext(Unit)
                        HelpDetailsContract.PartialState.ClearNetworkError
                    } else {
                        HelpDetailsContract.PartialState.NoChange
                    }
                },
            intent<HelpDetailsContract.Intent.Load>()
                .map {
                    HelpDetailsContract.PartialState.setSourceScreen(source)
                },
            // load screen
            intent<HelpDetailsContract.Intent.Load>()
                .switchMap {
                    UseCase.wrapObservable(userSupport.get().getHelpItem(helpItemId))
                }
                .map {
                    when (it) {
                        is Result.Progress -> HelpDetailsContract.PartialState.NoChange
                        is Result.Success -> {
                            Timber.d("<<<<HelpItem: ${it.value}")
                            HelpDetailsContract.PartialState.SetHelperItemDetail(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    HelpDetailsContract.ViewEvent.GotoLogin
                                    HelpDetailsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> HelpDetailsContract.PartialState.SetNetworkError(true)
                                else -> HelpDetailsContract.PartialState.ErrorState
                            }
                        }
                    }
                },
            intent<HelpDetailsContract.Intent.OnLikeClick>()
                .map {
                    HelpDetailsContract.PartialState.isHelpItemFeedBackLike(isLiked = true)
                },
            intent<HelpDetailsContract.Intent.OnDisLikeClick>()
                .map {
                    HelpDetailsContract.PartialState.isHelpItemFeedBackLike(isLiked = false)
                },
            intent<HelpDetailsContract.Intent.SubmitFeedback>()
                .switchMap {
                    UseCase.wrapCompletable(submitFeedback.get().schedule(it.feedback, it.rating))
                }
                .map {
                    emitViewEvent(HelpDetailsContract.ViewEvent.GoBackAfterAnimation)
                    HelpDetailsContract.PartialState.NoChange
                }
        )
    }

    override fun reduce(
        currentState: HelpDetailsContract.State,
        partialState: HelpDetailsContract.PartialState
    ): HelpDetailsContract.State {
        return when (partialState) {
            is HelpDetailsContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is HelpDetailsContract.PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is HelpDetailsContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is HelpDetailsContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is HelpDetailsContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false
            )
            is HelpDetailsContract.PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is HelpDetailsContract.PartialState.NoChange -> currentState
            is HelpDetailsContract.PartialState.SetHelperItemDetail -> currentState.copy(helpItemV2 = partialState.helpItemV2)
            is HelpDetailsContract.PartialState.setSourceScreen -> currentState.copy(sourceScreen = partialState.source)
            is HelpDetailsContract.PartialState.isHelpItemFeedBackLike -> if (partialState.isLiked) currentState.copy(
                likeState = LikeState.LIKE
            ) else if (!partialState.isLiked) currentState.copy(likeState = LikeState.DISLIKE) else currentState.copy(
                likeState = LikeState.NORMAL
            )
        }
    }
}
