package ${escapeKotlinIdentifiers(packageName)}.${featureName?lower_case}

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import tech.okcredit.base.dagger.di.qualifier.InitialState
import dagger.Lazy
import io.reactivex.Observable
import ${escapeKotlinIdentifiers(packageName)}.${featureName?lower_case}.${featureName}Contract.*
import ${escapeKotlinIdentifiers(packageName)}.${featureName?lower_case}.${featureName}Contract.PartialState.*
import javax.inject.Inject

class ${featureName}Presenter @Inject constructor(
    initialState: Lazy<State>
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Intent.Load>()
                .map { NoChange }
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is NoChange -> currentState
        }
    }

}
