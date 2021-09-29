package tech.okcredit.feedback.feedback

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import tech.okcredit.userSupport.usecses.SubmitFeedback
import javax.inject.Inject

class FeedbackViewModel @Inject constructor(
    initialState: FeedbackContract.State,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    private val submitFeedback: Lazy<SubmitFeedback>
) : BaseViewModel<FeedbackContract.State, FeedbackContract.PartialState, FeedbackContract.ViewEvent>(
    initialState
) {

    private val reload: PublishSubject<Unit> = PublishSubject.create()

    override fun handle(): Observable<UiState.Partial<FeedbackContract.State>> {
        return mergeArray(

            // hide network error when network becomes available
            intent<FeedbackContract.Intent.Load>()
                .switchMap { checkNetworkHealth.get().execute(Unit) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        reload.onNext(Unit)
                        FeedbackContract.PartialState.ClearNetworkError
                    } else {
                        FeedbackContract.PartialState.NoChange
                    }
                },

            intent<FeedbackContract.Intent.SubmitFeedback>()
                .switchMap { UseCase.wrapCompletable(submitFeedback.get().schedule(it.feedbackMessage, "suggestion")) }
                .map {
                    emitViewEvent(FeedbackContract.ViewEvent.GoBackAfterAnimation)
                    FeedbackContract.PartialState.NoChange
                }

        )
    }

    override fun reduce(
        currentState: FeedbackContract.State,
        partialState: FeedbackContract.PartialState
    ): FeedbackContract.State {
        return when (partialState) {
            is FeedbackContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is FeedbackContract.PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is FeedbackContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false
            )
            is FeedbackContract.PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is FeedbackContract.PartialState.NoChange -> currentState
        }
    }
}
