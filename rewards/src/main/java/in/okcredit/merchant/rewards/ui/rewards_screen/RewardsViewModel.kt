package `in`.okcredit.merchant.rewards.ui.rewards_screen

import `in`.okcredit.analytics.Event
import `in`.okcredit.merchant.rewards.R
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase.GetClaimErrorPreference
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase.SetClaimErrorPreference
import `in`.okcredit.merchant.rewards.ui.rewards_screen.usecase.GetAllRewards
import `in`.okcredit.merchant.rewards.ui.rewards_screen.usecase.GetHomeMerchantData
import `in`.okcredit.rewards.contract.RewardsSyncer
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.GetConnectionStatus
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.utils.ScreenName
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.feature_help.contract.GetContextualHelpIds
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RewardsViewModel @Inject constructor(
    initialState: RewardsContract.State,
    private val getConnectionStatus: Lazy<GetConnectionStatus>,
    private val getAllRewards: Lazy<GetAllRewards>,
    private val rewardsSync: Lazy<RewardsSyncer>,
    private val getHomeMerchantData: Lazy<GetHomeMerchantData>,
    private val getClaimErrorPreference: Lazy<GetClaimErrorPreference>,
    private val setClaimErrorPreference: Lazy<SetClaimErrorPreference>,
    private val getContextualHelpIds: Lazy<GetContextualHelpIds>,
) : BaseViewModel<RewardsContract.State, RewardsContract.PartialState, RewardsContract.ViewEvent>(
    initialState
) {

    private val showAlertPublishSubject: PublishSubject<Int> = PublishSubject.create()
    private var unclaimedRewards: Long = 0L

    override fun handle(): Observable<UiState.Partial<RewardsContract.State>> {
        return mergeArray(

            syncRewards(),
            showClaimError(),
            getAllRewards(),
            checkNetworkConnection(),
            showAlertPublishSubject(),
            loadMerchantData(),
            showMerchantDestinationDialog(),
            removeClaimError(),
            observeContextualHelpIdsOnLoad(),
        )
    }

    private fun observeContextualHelpIdsOnLoad() = intent<RewardsContract.Intent.Load>().switchMap {
        wrap(getContextualHelpIds.get().execute(ScreenName.RewardsScreen.value))
    }.map {
        if (it is Result.Success) {
            return@map RewardsContract.PartialState.SetContextualHelpIds(it.value)
        }
        RewardsContract.PartialState.NoChange
    }

    private fun syncRewards(): Observable<UiState.Partial<RewardsContract.State>> {
        return intent<RewardsContract.Intent.OnRefresh>()
            .switchMap { wrap(rxSingle { rewardsSync.get().syncRewards() }) }
            .map {
                if (it is Result.Success) {
                    pushIntent(RewardsContract.Intent.GetAllRewards)
                }
                RewardsContract.PartialState.NoChange
            }
    }

    private fun showAlertPublishSubject(): Observable<UiState.Partial<RewardsContract.State>> {
        return showAlertPublishSubject
            .switchMap {
                Observable.timer(3, TimeUnit.SECONDS)
                    .map<RewardsContract.PartialState> {
                        pushIntent(RewardsContract.Intent.RemoveClaimError)
                        RewardsContract.PartialState.HideAlert
                    }
                    .startWith(RewardsContract.PartialState.ShowAlert(it))
            }
    }

    private fun getAllRewards(): Observable<UiState.Partial<RewardsContract.State>> {
        return intent<RewardsContract.Intent.GetAllRewards>()
            .switchMap { wrap(getAllRewards.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> RewardsContract.PartialState.NoChange
                    is Result.Success -> {
                        unclaimedRewards = it.value.unclaimedRewards
                        if (unclaimedRewards > 0) {
                            pushIntent(RewardsContract.Intent.CheckCollectionStatus)
                        }
                        RewardsContract.PartialState.SetRewards(
                            it.value.rewards
                                .filter { rewardModel ->
                                    !rewardModel.isClaimed() ||
                                        !rewardModel.isBetterLuckNextTimeReward()
                                }
                                .convertToRewardsControllerModels(),
                            it.value.sumOfClaimedRewards,
                            it.value.unclaimedRewards
                        )
                    }
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> RewardsContract.PartialState.SetNetworkError(true)
                            else -> RewardsContract.PartialState.ErrorState
                        }
                    }
                }
            }
    }

    private fun checkNetworkConnection(): Observable<UiState.Partial<RewardsContract.State>> {
        return intent<RewardsContract.Intent.Load>()
            .switchMap { getConnectionStatus.get().execute() }
            .map {
                if (it is Result.Success) {
                    RewardsContract.PartialState.SetNetworkError(false)
                } else {
                    RewardsContract.PartialState.SetNetworkError(true)
                }
            }
    }

    private fun showClaimError(): Observable<UiState.Partial<RewardsContract.State>>? {
        return intent<RewardsContract.Intent.Load>()
            .switchMap { getClaimErrorPreference.get().execute() }.map {
                if (it is Result.Success && it.value) {
                    showAlertPublishSubject.onNext(
                        R.string.something_went_wrong_in_claiming_rewards
                    )
                }
                RewardsContract.PartialState.NoChange
            }
    }

    private fun removeClaimError(): Observable<UiState.Partial<RewardsContract.State>>? {
        return intent<RewardsContract.Intent.RemoveClaimError>().map {
            setClaimErrorPreference.get().execute(status = false)
            RewardsContract.PartialState.NoChange
        }
    }

    private fun loadMerchantData(): Observable<RewardsContract.PartialState>? {
        return intent<RewardsContract.Intent.CheckCollectionStatus>()
            .take(1)
            .switchMap { getHomeMerchantData.get().execute() }
            .map {
                when (it) {
                    is Result.Progress -> RewardsContract.PartialState.NoChange
                    is Result.Success -> {
                        if (!it.value.isAdaptedCollection) {
                            emitViewEvent(
                                RewardsContract.ViewEvent.ShowAddMerchantDestinationDialog(
                                    unclaimedRewards = unclaimedRewards,
                                    isUpdateCollection = false,
                                    source = Event.REWARD_SCREEN,
                                    paymentMethodType = null
                                )
                            )
                        }
                        RewardsContract.PartialState.SetCollectionAdopted(it.value.isAdaptedCollection)
                    }
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> RewardsContract.PartialState.SetNetworkError(true)

                            else -> {
                                Timber.e(it.error, "ErrorState")
                                RewardsContract.PartialState.ErrorState
                            }
                        }
                    }
                }
            }
    }

    private fun showMerchantDestinationDialog(): Observable<RewardsContract.PartialState> {
        return intent<RewardsContract.Intent.ShowAddMerchantDestinationDialog>()
            .take(1)
            .map {
                emitViewEvent(
                    RewardsContract.ViewEvent.ShowAddMerchantDestinationDialog(
                        unclaimedRewards = unclaimedRewards,
                        isUpdateCollection = it.isUpdateCollection,
                        source = it.source,
                        paymentMethodType = it.paymentMethodType
                    )
                )
                RewardsContract.PartialState.NoChange
            }
    }

    override fun reduce(
        currentState: RewardsContract.State,
        partialState: RewardsContract.PartialState,
    ): RewardsContract.State {
        return when (partialState) {
            is RewardsContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is RewardsContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is RewardsContract.PartialState.ErrorState -> currentState.copy(
                loader = false,
                error = true
            )
            is RewardsContract.PartialState.SetRewards -> currentState.copy(
                loader = false,
                rewards = partialState.rewards,
                sumOfClaimedRewards = partialState.amount,
                unclaimedRewards = partialState.unclaimedRewards,
                error = false
            )
            is RewardsContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.status,
                loader = false
            )
            is RewardsContract.PartialState.SetCollectionAdopted ->
                currentState.copy(isCollectionAdopted = partialState.enabled)
            is RewardsContract.PartialState.NoChange -> currentState
            is RewardsContract.PartialState.SetContextualHelpIds -> currentState.copy(contextualHelpIds = partialState.helpIds)
        }
    }
}
