package `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet

import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.MenuOptionBottomSheetContract.*
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.usecase.GetInviteOptionVisibility
import `in`.okcredit.referral.contract.usecase.GetReferralVersion
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.referral.usecase.GetReferralIntent
import javax.inject.Inject

class MenuOptionBottomSheetViewModel @Inject constructor(
    initialState: State,
    private val getInviteOptionVisibility: Lazy<GetInviteOptionVisibility>,
    private val getShareContent: Lazy<GetReferralIntent>,
    private val getReferralVersion: Lazy<GetReferralVersion>

) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {
    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            getReferralVersion(),
            getInviteOptionVisibility(),
            observeInviteButtonClicked(),
            observeShowMore(),
        )
    }

    private fun getReferralVersion(): Observable<UiState.Partial<State>> {
        return intent<Intent.Load>().switchMap {
            getReferralVersion.get().execute()
        }.map {
            PartialState.SetReferralVersion(referralVersion = it)
        }
    }

    private fun getInviteOptionVisibility(): Observable<UiState.Partial<State>> {
        return intent<Intent.CustomerModel>()
            .switchMap {
                getInviteOptionVisibility.get().execute(it.customer)
            }.map {
                if (it is Result.Success) {
                    PartialState.ShowInviteOption(it.value)
                } else {
                    PartialState.NoChange
                }
            }
    }

    private fun observeInviteButtonClicked(): Observable<UiState.Partial<State>> {
        return intent<Intent.SendInviteToWhatsApp>()
            .switchMap {
                getShareContent.get().getWhatsAppIntent(it.targetUser.id, it.targetUser.phoneNumber)
            }.map {
                if (it is Result.Success) {
                    emitViewEvent(ViewEvent.SendInviteToTargetedUser(it.value))
                }
                PartialState.NoChange
            }
    }

    private fun observeShowMore() = intent<Intent.MoreOptionClicked>()
        .map {
            PartialState.ShowHiddenMenuOptions(getCurrentState().showHiddenMenuOptions.not())
        }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.ShowInviteOption -> currentState.copy(canShowInviteOption = partialState.canShow)
            is PartialState.SetReferralVersion -> currentState.copy(referralVersion = partialState.referralVersion)
            is PartialState.ShowHiddenMenuOptions -> currentState.copy(showHiddenMenuOptions = partialState.show)
        }
    }
}
