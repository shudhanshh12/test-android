package `in`.okcredit.collection_ui.ui.referral.education

import `in`.okcredit.collection.contract.CollectionEventTracker.Companion.CUSTOMER_SCREEN
import `in`.okcredit.collection.contract.ReferralEducationPreference
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionScreen.INVITE_N_EARN_SCREEN
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.utils.ScreenName
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.feature_help.contract.GetContextualHelpIds
import javax.inject.Inject

class ReferralEducationViewModel @Inject constructor(
    initialState: ReferralEducationContract.State,
    private val referralEducationPreference: Lazy<ReferralEducationPreference>,
    private val getContextualHelpIds: Lazy<GetContextualHelpIds>,
    private val collectionTracker: Lazy<CollectionTracker>,
) : BaseViewModel<ReferralEducationContract.State, ReferralEducationContract.PartialState, ReferralEducationContract.ViewEvents>(
    initialState
) {

    override fun handle(): Observable<out UiState.Partial<ReferralEducationContract.State>> {
        return Observable.mergeArray(
            loadObservables(),
            inviteClicked(),
            helpClickedObservable(),
        )
    }

    private fun loadObservables(): Observable<ReferralEducationContract.PartialState>? {
        return intent<ReferralEducationContract.Intent.Load>()
            .switchMap { wrap(referralEducationPreference.get().setReferralEducationShown()) }
            .map {
                ReferralEducationContract.PartialState.NoChange
            }
    }

    private fun inviteClicked(): Observable<ReferralEducationContract.PartialState>? {
        return intent<ReferralEducationContract.Intent.InviteClicked>()
            .map {
                collectionTracker.get().trackInviteNowCollectionClicked(
                    screen = INVITE_N_EARN_SCREEN,
                    source = if (getCurrentState().customerIdFrmLedger.isNotNullOrBlank()) CUSTOMER_SCREEN else CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN
                )
                emitViewEvent(ReferralEducationContract.ViewEvents.GotoReferralListScreen)
                ReferralEducationContract.PartialState.NoChange
            }
    }

    private fun helpClickedObservable() = intent<ReferralEducationContract.Intent.HelpClicked>()
        .switchMap {
            wrap(getContextualHelpIds.get().execute(ScreenName.CollectionTargetedReferralScreen.value))
        }.map {
            if (it is Result.Success) {
                emitViewEvent(ReferralEducationContract.ViewEvents.HelpClicked(it.value))
            }
            ReferralEducationContract.PartialState.NoChange
        }

    override fun reduce(
        currentState: ReferralEducationContract.State,
        partialState: ReferralEducationContract.PartialState,
    ): ReferralEducationContract.State {
        return when (partialState) {
            ReferralEducationContract.PartialState.NoChange -> currentState
        }
    }
}
