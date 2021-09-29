package `in`.okcredit.collection_ui.ui.benefits

import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class CollectionsBenefitViewModel @Inject constructor(
    initialState: CollectionsBenefitContract.State,
    private val getPaymentReminderIntent: Lazy<GetPaymentReminderIntent>,
) : BaseViewModel<CollectionsBenefitContract.State, CollectionsBenefitContract.PartialState, CollectionsBenefitContract.ViewEvent>(
    initialState
) {

    override fun handle() = Observable.mergeArray(
        observeButtonClicked(),
        observeSendReminder(),
    )

    private fun observeSendReminder() = intent<CollectionsBenefitContract.Intent.SendReminder>().switchMap {
        wrap(getPaymentReminderIntent.get().execute(getCurrentState().customerId!!, "collections_contextual_trigger", null))
    }.map {
        if (it is Result.Success) {
            emitViewEvent(CollectionsBenefitContract.ViewEvent.SendReminder(it.value))
        }
        return@map CollectionsBenefitContract.PartialState.NoChange
    }

    private fun observeButtonClicked() = intent<CollectionsBenefitContract.Intent.SetupClicked>().map {
        if (getCurrentState().sendReminder) {
            pushIntent(CollectionsBenefitContract.Intent.SendReminder)
        } else {
            emitViewEvent(CollectionsBenefitContract.ViewEvent.ShowAddBankDetails)
        }
        return@map CollectionsBenefitContract.PartialState.NoChange
    }

    override fun reduce(
        currentState: CollectionsBenefitContract.State,
        partialState: CollectionsBenefitContract.PartialState,
    ): CollectionsBenefitContract.State {
        return currentState
    }
}
