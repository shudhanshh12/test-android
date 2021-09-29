package merchant.okcredit.gamification.ipl.view

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.gamification.ipl.view.IplContract.*
import merchant.okcredit.gamification.ipl.view.IplContract.PartialState.NoChange
import merchant.okcredit.gamification.ipl.view.usecase.HasEducationView
import merchant.okcredit.gamification.ipl.view.usecase.SetEducationView
import javax.inject.Inject

class IplViewModel @Inject constructor(
    initialState: Lazy<State>,
    private val setEducationView: Lazy<SetEducationView>,
    private val hasEducationView: Lazy<HasEducationView>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            hasEducationView(),
            setEducationView()
        )
    }

    private fun setEducationView(): Observable<PartialState> {
        return intent<Intent.EducationViewed>()
            .switchMap { setEducationView.get().execute() }
            .map { NoChange }
    }

    private fun hasEducationView() = intent<Intent.Load>()
        .switchMap { hasEducationView.get().execute() }
        .map {
            when (it) {
                is Result.Success -> {
                    if (it.value.not()) {
                        emitViewEvent(ViewEvent.ShowEducation)
                    }
                }
            }
            NoChange
        }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is NoChange -> currentState
        }
    }
}
