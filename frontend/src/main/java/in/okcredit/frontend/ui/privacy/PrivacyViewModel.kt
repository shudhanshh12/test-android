package `in`.okcredit.frontend.ui.privacy

import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import javax.inject.Inject

class PrivacyViewModel @Inject constructor() :
    BasePresenter<PrivacyContract.State, PrivacyContract.PartialState>(PrivacyContract.State()) {

    override fun handle(): Observable<UiState.Partial<PrivacyContract.State>> {
        return mergeArray(
            // load page
            intent<PrivacyContract.Intent.Load>()
                .map {
                    PrivacyContract.PartialState.NoChange
                }
        )
    }

    override fun reduce(
        currentState: PrivacyContract.State,
        partialState: PrivacyContract.PartialState
    ): PrivacyContract.State {
        return when (partialState) {
            is PrivacyContract.PartialState.NoChange -> currentState
        }
    }
}
