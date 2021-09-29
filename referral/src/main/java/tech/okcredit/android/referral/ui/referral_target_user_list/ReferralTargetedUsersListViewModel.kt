package tech.okcredit.android.referral.ui.referral_target_user_list

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.referral.ui.referral_target_user_list.ReferralTargetedUsersListContract.Intent
import tech.okcredit.android.referral.ui.referral_target_user_list.ReferralTargetedUsersListContract.PartialState
import tech.okcredit.android.referral.ui.referral_target_user_list.ReferralTargetedUsersListContract.State
import tech.okcredit.android.referral.ui.referral_target_user_list.ReferralTargetedUsersListContract.ViewEvents
import tech.okcredit.android.referral.ui.referral_target_user_list.usecase.GetShareToWhatsAppStatusVisibility
import tech.okcredit.android.referral.ui.referral_target_user_list.usecase.GetTargetUsers
import tech.okcredit.android.referral.usecase.GetReferralIntent
import javax.inject.Inject

class ReferralTargetedUsersListViewModel @Inject constructor(
    initialState: State,
    private val getTargetUsers: Lazy<GetTargetUsers>,
    private val getShareToWhatsAppStatusVisibility: Lazy<GetShareToWhatsAppStatusVisibility>,
    private val getShareIntent: Lazy<GetReferralIntent>,
) : BaseViewModel<State, PartialState, ViewEvents>(initialState) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            getReferralUser(),
            showShareWhatsAppStatus(),
            observeInviteButtonClicked(),
            shareReferral(),
            showRewards(),
        )
    }

    private fun observeInviteButtonClicked() = intent<Intent.SendInviteToWhatsApp>()
        .switchMap {
            getShareIntent.get().getWhatsAppIntent(it.targetUser.id, it.targetUser.phoneNumber)
        }.map {
            when (it) {
                is Result.Progress -> PartialState.ShowProgress
                is Result.Success -> {
                    emitViewEvent(ViewEvents.ReferralIntent(it.value))
                    PartialState.HideProgress
                }
                is Result.Failure -> {
                    emitViewEvent(ViewEvents.ShareFailure)
                    PartialState.HideProgress
                }
            }
        }

    private fun showShareWhatsAppStatus() = intent<Intent.Load>()
        .switchMap {
            getShareToWhatsAppStatusVisibility.get().execute()
        }.map {
            if (it is Result.Success) {
                PartialState.ShowGenericShare(it.value)
            } else {
                PartialState.NoChange
            }
        }

    private fun getReferralUser() = intent<Intent.Load>()
        .switchMap {
            wrap(getTargetUsers.get().execute())
        }.map {
            when (it) {
                is Result.Progress -> PartialState.ShowProgress
                is Result.Success -> PartialState.TargetedUsers(it.value)
                is Result.Failure -> PartialState.TargetedUsers(emptyList())
            }
        }

    private fun shareReferral(): Observable<PartialState> = intent<Intent.ShareReferral>()
        .switchMap {
            getShareIntent.get().getWhatsAppIntent()
        }.map {
            when (it) {
                is Result.Progress -> PartialState.ShowProgress
                is Result.Success -> {
                    emitViewEvent(ViewEvents.ReferralIntent(it.value))
                    PartialState.HideProgress
                }
                is Result.Failure -> {
                    emitViewEvent(ViewEvents.ShareFailure)
                    PartialState.HideProgress
                }
            }
        }

    private fun showRewards(): Observable<PartialState> {
        // todo Check if Collection is active and Unclaimed rewards
        return intent<Intent.GoToReferralRewardsForPhoneNumber>()
            .map {
                emitViewEvent(ViewEvents.GoToReferralRewardsForPhoneNumber(it.phoneNumber))
                PartialState.NoChange
            }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.ShowProgress -> currentState.copy(showProgress = true)
            is PartialState.HideProgress -> currentState.copy(showProgress = false)
            is PartialState.TargetedUsers -> currentState.copy(
                showProgress = false,
                targetedUsers = partialState.targetedUsers,
                showGenericShare = currentState.genericShareEnabled && partialState.targetedUsers.isNotEmpty(),
                showTargetedUserList = partialState.targetedUsers.isNotEmpty()
            )
            is PartialState.ShowGenericShare -> currentState.copy(
                genericShareEnabled = partialState.enabled,
                showGenericShare = partialState.enabled && currentState.targetedUsers?.isNotEmpty() == true
            )
        }
    }
}
