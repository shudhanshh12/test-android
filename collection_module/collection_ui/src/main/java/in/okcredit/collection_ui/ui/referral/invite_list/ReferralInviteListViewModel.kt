package `in`.okcredit.collection_ui.ui.referral.invite_list

import `in`.okcredit.collection.contract.CollectionEventTracker.Companion.CUSTOMER_SCREEN
import `in`.okcredit.collection.contract.FetchPaymentTargetedReferral
import `in`.okcredit.collection.contract.GetTargetedReferralInfoList
import `in`.okcredit.collection.contract.ReferralStatus
import `in`.okcredit.collection.contract.ShareTargetedReferral
import `in`.okcredit.collection.contract.TargetedCustomerReferralInfo
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionScreen.REFERRAL_INVITE_LIST
import `in`.okcredit.collection_ui.ui.referral.invite_list.views.TargetedReferralInviteListItem
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.utils.ScreenName
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.feature_help.contract.GetContextualHelpIds
import javax.inject.Inject

class ReferralInviteListViewModel @Inject constructor(
    initialState: ReferralInviteListContract.State,
    private val getTargetedReferralInfoList: Lazy<GetTargetedReferralInfoList>,
    private val communicationRepository: Lazy<CommunicationRepository>,
    private val sharedTargetedReferral: Lazy<ShareTargetedReferral>,
    private val fetchPaymentTargetedReferral: Lazy<FetchPaymentTargetedReferral>,
    private val getContextualHelpIds: Lazy<GetContextualHelpIds>,
    private val collectionTracker: Lazy<CollectionTracker>,
) : BaseViewModel<ReferralInviteListContract.State, ReferralInviteListContract.PartialState, ReferralInviteListContract.ViewEvents>(
    initialState
) {

    private var inviteClickedTargetedCustomerInfo: TargetedCustomerReferralInfo? = null
    private var whatsappShown: Boolean = false

    override fun handle(): Observable<out UiState.Partial<ReferralInviteListContract.State>> {
        return Observable.mergeArray(
            loadObservable(),
            loadReferralList(),
            inviteOnWhatsApp(),
            gotoRewardScreen(),
            shareTargetedReferral(),
            fetchPaymentTargetedReferral(),
            helpClickedObservable(),
            onInviteBtnClicked(),
        )
    }

    private fun loadObservable(): Observable<ReferralInviteListContract.PartialState>? {
        return intent<ReferralInviteListContract.Intent.Load>()
            .map {
                ReferralInviteListContract.PartialState.NoChange
            }
    }

    private fun loadReferralList() = intent<ReferralInviteListContract.Intent.Load>()
        .switchMap {
            wrap(getTargetedReferralInfoList.get().execute())
        }
        .map { result ->
            if (result is Result.Success) {
                getCurrentState().customerIdFrmLedger?.let { customerIdFrmLedgerId ->
                    var ledgerCustomerInfo: TargetedCustomerReferralInfo? = null
                    val remainingList = arrayListOf<TargetedCustomerReferralInfo>()

                    result.value.forEach {
                        if (it.id == customerIdFrmLedgerId)
                            ledgerCustomerInfo = it
                        else
                            remainingList.add(it)
                    }

                    ledgerCustomerInfo?.let {
                        if (it.status == ReferralStatus.LINK_CREATED.value && whatsappShown.not()) {
                            whatsappShown = true
                            pushIntent(ReferralInviteListContract.Intent.InviteOnWhatsApp(it))
                        }
                        return@map ReferralInviteListContract.PartialState.SetReferralInfoList(
                            remainingList,
                            ledgerCustomerInfo
                        )
                    }
                }
                ReferralInviteListContract.PartialState.SetReferralInfoList(result.value, null)
            } else ReferralInviteListContract.PartialState.NoChange
        }

    private fun onInviteBtnClicked(): Observable<ReferralInviteListContract.PartialState.NoChange> {
        return intent<ReferralInviteListContract.Intent.OnInviteBtnClicked>()
            .map {
                if (it.targetedCustomerReferralInfo.status == ReferralStatus.LINK_CREATED.value) {
                    collectionTracker.get().trackShareCollectionInvite(
                        it.targetedCustomerReferralInfo.id,
                        source = if (getCurrentState().customerIdFrmLedger.isNotNullOrBlank()) CUSTOMER_SCREEN else CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN,
                        screen = REFERRAL_INVITE_LIST
                    )
                } else {
                    collectionTracker.get().trackRemindCollectionReferral(
                        it.targetedCustomerReferralInfo.id,
                        source = if (getCurrentState().customerIdFrmLedger.isNotNullOrBlank()) CUSTOMER_SCREEN else CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN,
                        screen = REFERRAL_INVITE_LIST
                    )
                }

                pushIntent(ReferralInviteListContract.Intent.InviteOnWhatsApp(it.targetedCustomerReferralInfo))
                ReferralInviteListContract.PartialState.NoChange
            }
    }

    private fun inviteOnWhatsApp(): Observable<ReferralInviteListContract.PartialState.NoChange> {
        return intent<ReferralInviteListContract.Intent.InviteOnWhatsApp>()
            .switchMap {
                inviteClickedTargetedCustomerInfo = it.targetedCustomerReferralInfo
                wrap(
                    communicationRepository.get().goToWhatsAppWithTextOnlyExtendedBahaviuor(
                        ShareIntentBuilder(
                            shareText = it.targetedCustomerReferralInfo.youtubeLink + "\n" + it.targetedCustomerReferralInfo.message +
                                "\n" + it.targetedCustomerReferralInfo.link,
                            phoneNumber = it.targetedCustomerReferralInfo.mobile
                        )
                    )
                )
            }
            .map {
                if (it is Result.Success) {
                    emitViewEvent(ReferralInviteListContract.ViewEvents.InviteOnWhatsApp(it.value))
                    inviteClickedTargetedCustomerInfo?.let {
                        if (it.status < ReferralStatus.LINK_SHARED.value) {
                            pushIntent(ReferralInviteListContract.Intent.ShareTargetedReferral(it.customerMerchantId))
                        }
                    }
                } else if (it is Result.Failure) {
                    if (it.error is IntentHelper.NoWhatsAppError) {
                        emitViewEvent(
                            ReferralInviteListContract.ViewEvents.ShowWhatsAppError
                        )
                    } else {
                        emitViewEvent(
                            ReferralInviteListContract.ViewEvents.ShowSomethingWrongError
                        )
                    }
                }

                ReferralInviteListContract.PartialState.NoChange
            }
    }

    private fun gotoRewardScreen() = intent<ReferralInviteListContract.Intent.GotoRewardScreen>()
        .map {
            emitViewEvent(ReferralInviteListContract.ViewEvents.GotoRewardScreen)
            ReferralInviteListContract.PartialState.NoChange
        }

    private fun shareTargetedReferral() = intent<ReferralInviteListContract.Intent.ShareTargetedReferral>()
        .switchMap {
            wrap(sharedTargetedReferral.get().execute(it.customerMerchantId))
        }
        .map {
            ReferralInviteListContract.PartialState.NoChange
        }

    private fun fetchPaymentTargetedReferral() = intent<ReferralInviteListContract.Intent.Resume>()
        .switchMap {
            wrap(
                fetchPaymentTargetedReferral.get()
                    .execute()
            )
        }
        .map {
            ReferralInviteListContract.PartialState.NoChange
        }

    private fun helpClickedObservable() = intent<ReferralInviteListContract.Intent.HelpClicked>().switchMap {
        wrap(getContextualHelpIds.get().execute(ScreenName.CollectionTargetedReferralScreen.value))
    }.map {
        if (it is Result.Success) {
            emitViewEvent(ReferralInviteListContract.ViewEvents.HelpClicked(it.value))
        }
        ReferralInviteListContract.PartialState.NoChange
    }

    override fun reduce(
        currentState: ReferralInviteListContract.State,
        partialState: ReferralInviteListContract.PartialState,
    ): ReferralInviteListContract.State {
        val tempState = when (partialState) {
            ReferralInviteListContract.PartialState.NoChange -> currentState
            is ReferralInviteListContract.PartialState.SetReferralInfoList -> currentState.copy(
                referralList = partialState.list,
                customerInfoFrmLedger = partialState.customerInfoFrmLedger
            )
        }

        return tempState.copy(
            rewardAmount = getRewardAmount(tempState), rewardBtnActive = isRewardBtnActive(tempState),
            list = buildInviteList(tempState)
        )
    }

    private fun getRewardAmount(state: ReferralInviteListContract.State): Long {
        var rewardAmount = 0L
        state.referralList.forEach {
            if (it.status == ReferralStatus.REWARD_SUCCESS.value) rewardAmount += it.amount
        }
        return rewardAmount
    }

    private fun isRewardBtnActive(state: ReferralInviteListContract.State): Boolean {
        if (state.rewardAmount > 0) return true

        state.customerInfoFrmLedger?.let {
            if (it.status >= ReferralStatus.REWARD_CREATED.value)
                return true
        }

        state.referralList.forEach {
            if (it.status >= ReferralStatus.REWARD_CREATED.value)
                return true
        }

        return false
    }

    private fun buildInviteList(state: ReferralInviteListContract.State): List<TargetedReferralInviteListItem> {
        val list = mutableListOf<TargetedReferralInviteListItem>()
        list.add(TargetedReferralInviteListItem.HeaderViewItem())
        if (state.customerIdFrmLedger.isNotNullOrBlank() && state.customerInfoFrmLedger != null) {
            list.add(TargetedReferralInviteListItem.CustomerReferralItem(state.customerInfoFrmLedger))
            if (state.referralList.isNotEmpty())
                list.add(TargetedReferralInviteListItem.ListHeadingItem(comingFrmLedger = true))
        } else {
            list.add(TargetedReferralInviteListItem.ListHeadingItem(comingFrmLedger = false))
        }
        state.referralList.forEach {
            list.add(TargetedReferralInviteListItem.CustomerReferralItem(it))
        }
        return list
    }
}
