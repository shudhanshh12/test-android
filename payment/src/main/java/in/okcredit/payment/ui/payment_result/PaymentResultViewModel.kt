package `in`.okcredit.payment.ui.payment_result

import `in`.okcredit.cashback.contract.usecase.CashbackLocalDataOperations
import `in`.okcredit.cashback.contract.usecase.GetCashbackRewardForPayment
import `in`.okcredit.cashback.contract.usecase.IsCustomerCashbackFeatureEnabled
import `in`.okcredit.cashback.contract.usecase.IsSupplierCashbackFeatureEnabled
import `in`.okcredit.collection.contract.GetBlindPayShareLink
import `in`.okcredit.collection.contract.GetCollectionActivationStatus
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_ACCOUNT_TYPE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_ACCOUNT_ID
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_MOBILE
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_STATUS_PAGE_VIEW
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.SEEN
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.UNSEEN
import `in`.okcredit.payment.contract.model.JuspayPollingStatus
import `in`.okcredit.payment.contract.model.PaymentModel
import `in`.okcredit.payment.contract.usecase.GetPaymentResult
import `in`.okcredit.payment.ui.payment_result.PaymentResultFragment.Companion.ARG_PAYMENT_ID_PAYMENT_RESULT
import `in`.okcredit.payment.ui.payment_result.PaymentResultFragment.Companion.ARG_PAYMENT_SHOW_TXN_CANCELLED
import `in`.okcredit.payment.ui.payment_result.PaymentResultFragment.Companion.ARG_PAYMENT_TYPE_RESULT
import `in`.okcredit.payment.usecases.SyncCollectionAndCustomerTransactions
import `in`.okcredit.payment.utils.CurrencyUtil
import `in`.okcredit.payment.utils.getWhatsAppMsg
import `in`.okcredit.rewards.contract.GetRewardById
import `in`.okcredit.rewards.contract.RewardsSyncer
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxCompletable
import merchant.okcredit.accounting.contract.model.LedgerType
import merchant.okcredit.accounting.contract.model.SupportType
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportData
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportType
import org.joda.time.DateTime
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.base.extensions.capitalizeWords
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PaymentResultViewModel @Inject constructor(
    private val initialState: PaymentResultContract.State,
    @ViewModelParam(ARG_PAYMENT_ACCOUNT_ID) val accountId: String,
    @ViewModelParam(ARG_PAYMENT_ID_PAYMENT_RESULT) val paymentId: String,
    @ViewModelParam(ARG_PAYMENT_TYPE_RESULT) val paymentType: String,
    @ViewModelParam(ARG_ACCOUNT_TYPE) val accountType: String,
    @ViewModelParam(ARG_PAYMENT_SHOW_TXN_CANCELLED) val showTxnCancelled: Boolean = false,
    @ViewModelParam(ARG_PAYMENT_MOBILE) val mobile: String,
    private val communicationApi: Lazy<CommunicationRepository>,
    private val getPaymentResult: Lazy<GetPaymentResult>,
    private val getCashbackRewardForPayment: Lazy<GetCashbackRewardForPayment>,
    private val rewardsSyncer: Lazy<RewardsSyncer>,
    private val isCustomerCashbackFeatureEnabled: Lazy<IsCustomerCashbackFeatureEnabled>,
    private val isSupplierCashbackFeatureEnabled: Lazy<IsSupplierCashbackFeatureEnabled>,
    private val getRewardById: Lazy<GetRewardById>,
    private val cashbackLocalDataOperations: Lazy<CashbackLocalDataOperations>,
    private val context: Lazy<Context>,
    private val getCollectionActivationStatus: Lazy<GetCollectionActivationStatus>,
    private var paymentAnalyticsEvents: Lazy<PaymentAnalyticsEvents>,
    private val getBlindPayShareLink: Lazy<GetBlindPayShareLink>,
    private val getCustomerSupportType: Lazy<GetCustomerSupportType>,
    private val getCustomerSupportData: Lazy<GetCustomerSupportData>,
    private val syncCollectionAndCustomerTransactions: Lazy<SyncCollectionAndCustomerTransactions>,
) : BaseViewModel<PaymentResultContract.State, PaymentResultContract.PartialState, PaymentResultContract.ViewEvents>(
    initialState
) {
    private var paymentResponse: PaymentModel.JuspayPaymentPollingModel? = null
    private var shouldTimerTick: Boolean = true
    private var wasRewardPopUpShownAtLeastOnce = false

    // TODO: Clean Directory Name and file names
    companion object {
        const val FILE_NAME = "reminder.jpg"
        const val FOLDER_NAME = "reminder_images"
    }

    override fun handle(): Observable<out UiState.Partial<PaymentResultContract.State>> {
        return Observable.mergeArray(
            shareScreenShotToWhatsapp(),
            fetchPaymentResult(),
            fetchRewardForPayment(),
            copyTxnIdToClipBoard(),
            clickedShareOrRetry(),
            startLoadingTimer(),
            haltLoadingTimer(),
            trackPaymentRewardStatusPageView(),
            trackPaymentRewardClicked(),
            setRewardPopUpShownAtLeastOnce(),
            loadIntent(),
            fetchBlindPayShareLink(),
            syncCollectionTransactionObservable(),
            getCustomerSupportType(),
            actionSupportClicked(),
            sendHelpWhatsAppMessage()
        )
    }

    private fun syncCollectionTransactionObservable() = intent<PaymentResultContract.Intent.SyncCollection>()
        .switchMap {
            wrap(
                syncCollectionAndCustomerTransactions.get().execute()
            )
        }.map {
            PaymentResultContract.PartialState.NoChange
        }

    private fun loadIntent(): Observable<PaymentResultContract.PartialState> {
        return intent<PaymentResultContract.Intent.Load>()
            .map {
                // in case of txn cancelled no need to call api (LoadData) as we know status already
                if (!showTxnCancelled) {
                    pushIntent(PaymentResultContract.Intent.LoadData)
                    PaymentResultContract.PartialState.NoChange
                } else {
                    paymentAnalyticsEvents.get().trackPaymentStatusPageView(
                        accountId = accountId,
                        screen = PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_STATUS,
                        relation = initialState.getRelationFrmAccountType(),
                        flow = PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION,
                        address = initialState.paymentAddress,
                        amount = "",
                        paymentId = paymentId,
                        provider = "Juspay",
                        status = PaymentAnalyticsEvents.PaymentPropertyValue.CANCELLED,
                        type = initialState.destinationType,
                        timeTaken = "0",
                        collectionStatus = getCollectionActivationStatus.get().execute().blockingFirst(),
                        easyPay = initialState.blindPayFlow,
                    )
                    PaymentResultContract.PartialState.SetJuspayPollingResponse(
                        null,
                        PaymentResultContract.UiScreenType.CANCELLED
                    )
                }
            }
    }

    private fun shareScreenShotToWhatsapp(): Observable<PaymentResultContract.PartialState> {
        return intent<PaymentResultContract.Intent.ShareScreenShot>()
            .switchMap {
                UseCase.wrapSingle(
                    communicationApi.get().goToWhatsApp(
                        ShareIntentBuilder(
                            shareText = it.sharingText,
                            phoneNumber = mobile,
                            imageFrom = ImagePath.ImageUriFromBitMap(
                                it.bitmap,
                                context.get(),
                                FOLDER_NAME,
                                FILE_NAME
                            )
                        )
                    )
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> PaymentResultContract.PartialState.NoChange
                    is Result.Success -> {
                        emitViewEvent(PaymentResultContract.ViewEvents.OpenWhatsAppPromotionShare(it.value))
                    }
                    is Result.Failure -> {
                        if (it.error is IntentHelper.NoWhatsAppError) {
                            emitViewEvent(PaymentResultContract.ViewEvents.ShowWhatsAppError)
                        } else {
                            emitViewEvent(PaymentResultContract.ViewEvents.ShowDefaultError)
                        }
                    }
                }

                PaymentResultContract.PartialState.NoChange
            }
    }

    private fun fetchPaymentResult() =
        intent<PaymentResultContract.Intent.LoadData>()
            .switchMap {
                pushIntent(PaymentResultContract.Intent.StartLoadingTimer)
                UseCase.wrapObservable(
                    getPaymentResult.get()
                        .execute(pId = paymentId, polling = true, type = paymentType)
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> {
                        paymentAnalyticsEvents.get().trackPaymentStatusWaitingPage(
                            accountId = accountId,
                            screen = PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_ADDRESS_DETAILS,
                            relation = initialState.getRelationFrmAccountType(),
                            flow = PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION,
                            easyPay = initialState.blindPayFlow,
                        )
                        PaymentResultContract.PartialState.NoChange
                    }
                    is Result.Success -> {

                        pushIntent(PaymentResultContract.Intent.HaltLoadingTimer)
                        pushIntent(PaymentResultContract.Intent.SyncCollection)
                        paymentResponse = it.value

                        // Clear cashback message details from cache, since they change on basis of payment history
                        if (it.value.status == JuspayPollingStatus.SUCCESS.value) {
                            cashbackLocalDataOperations.get().executeInvalidateLocalData().subscribe()
                        }

                        val uiScreenType = PaymentResultContract.UiScreenType.fromValue(it.value.status)

                        var supportMsg = ""
                        var supportNumber = ""
                        var supportType = ""

                        if (uiScreenType == PaymentResultContract.UiScreenType.FAILED || uiScreenType == PaymentResultContract.UiScreenType.PENDING) {
                            supportType = getCurrentState().supportType.value
                            supportNumber = getCurrentState().supportNumber
                            supportMsg = it.value.let {
                                getWhatsAppMsg(
                                    context.get(),
                                    amount = CurrencyUtil.formatV2(it.paymentInfo.paymentAmount?.toLong() ?: 0L),
                                    paymentTime = DateTimeUtils.formatLong(DateTime(it.paymentInfo.createTime.times(1000))),
                                    txnId = it.paymentId,
                                    status = it.status.capitalizeWords()
                                )
                            }
                        }

                        getCurrentState().let { state ->
                            paymentAnalyticsEvents.get().trackPaymentStatusPageView(
                                accountId = state.accountId,
                                screen = PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_STATUS,
                                relation = state.getRelationFrmAccountType(),
                                flow = PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION,
                                address = state.paymentAddress,
                                amount = it.value.paymentInfo.paymentAmount
                                    ?: "",
                                paymentId = it.value.paymentInfo.id ?: "",
                                provider = "Juspay",
                                status = PaymentResultFragment.getAnalyticsPropertyValueForScreenType(uiScreenType),
                                type = initialState.destinationType,
                                timeTaken = state.loadingTimerState.getTimeElapsed().toString(),
                                collectionStatus = getCollectionActivationStatus.get().execute().blockingFirst(),
                                easyPay = initialState.blindPayFlow,
                                customerSupportType = supportType,
                                customerSupportNumber = supportNumber,
                                customerSupportMessage = supportMsg,
                            )
                        }

                        PaymentResultContract.PartialState.SetJuspayPollingResponse(
                            it.value,
                            uiScreenType
                        )
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(PaymentResultContract.ViewEvents.GoToLogin)
                            }
                            isInternetIssue(it.error) -> emitViewEvent(
                                PaymentResultContract.ViewEvents.NetworkError
                            )
                            else -> emitViewEvent(
                                PaymentResultContract.ViewEvents.OtherError
                            )
                        }
                        PaymentResultContract.PartialState.NoChange
                    }
                }
            }

    private fun fetchRewardForPayment() = intent<PaymentResultContract.Intent.LoadRewardForPayment>()
        .switchMap { intent ->
            val cashbackFeatureObservable = if (accountType == LedgerType.CUSTOMER.value) {
                isCustomerCashbackFeatureEnabled.get().execute()
            } else {
                isSupplierCashbackFeatureEnabled.get().execute()
            }
            cashbackFeatureObservable.switchMap {
                if (it) {
                    getCashbackRewardForPayment.get().execute(intent.paymentId)
                } else {
                    Observable.empty()
                }
            }
        }
        .switchMap { rxCompletable { rewardsSyncer.get().syncRewards() }.andThen(Observable.just(it)) }
        .switchMap {
            emitViewEvent(PaymentResultContract.ViewEvents.GoToClaimRewardScreen(it))
            getRewardById.get().execute(it.id)
        }
        .map {
            PaymentResultContract.PartialState.SetRewardForPayment(it)
        }

    private fun fetchBlindPayShareLink() = intent<PaymentResultContract.Intent.GetBlindPayShareLink>()
        .switchMap {
            wrap(getBlindPayShareLink.get().execute(it.paymentId))
        }.map {
            when (it) {
                is Result.Success -> {
                    PaymentResultContract.PartialState.SetBlindPayShareLink(it.value)
                    emitViewEvent(PaymentResultContract.ViewEvents.TakeScreenShot(it.value))
                }
                is Result.Failure -> PaymentResultContract.PartialState.NoChange
                is Result.Progress -> PaymentResultContract.PartialState.NoChange
            }

            PaymentResultContract.PartialState.NoChange
        }

    private fun copyTxnIdToClipBoard() = intent<PaymentResultContract.Intent.CopyTxnIdToClipBoard>()
        .map {
            emitViewEvent(PaymentResultContract.ViewEvents.CopyTxnIdToClipBoard(paymentId))
            PaymentResultContract.PartialState.NoChange
        }

    private fun clickedShareOrRetry() = intent<PaymentResultContract.Intent.ClickedShareOrRetry>()
        .map {
            if (paymentResponse?.status == JuspayPollingStatus.FAILED.value || showTxnCancelled) {
                emitViewEvent(PaymentResultContract.ViewEvents.RetryPayment)
            } else {
                if (getCurrentState().blindPayFlow) {
                    pushIntent(PaymentResultContract.Intent.GetBlindPayShareLink(paymentId))
                } else {
                    emitViewEvent(PaymentResultContract.ViewEvents.TakeScreenShot())
                }
            }
            PaymentResultContract.PartialState.NoChange
        }

    private fun trackPaymentRewardStatusPageView(): Observable<PaymentResultContract.PartialState> {
        return intent<PaymentResultContract.Intent.TrackPaymentRewardStatusPageView>()
            .map {
                if (wasRewardPopUpShownAtLeastOnce) {

                    val state = getCurrentState()
                    val cashbackSeenStatus = if (state.reward?.isUnclaimed() == true) UNSEEN else SEEN

                    paymentAnalyticsEvents.get().trackPaymentRewardStatusPageView(
                        accountId = accountId,
                        relation = initialState.getRelationFrmAccountType(),
                        screen = PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_STATUS,
                        amount = state.juspayPaymentPollingModel?.paymentInfo?.paymentAmount ?: "",
                        paymentId = paymentId,
                        cashbackSeenStatus = cashbackSeenStatus,
                        rewardId = state.reward?.id ?: "",
                        rewardValue = state.reward?.amount.toString(),
                        collectionStatus = getCollectionActivationStatus.get().execute().blockingFirst(),
                        rewardStatus = state.reward?.status ?: ""
                    )
                }
                PaymentResultContract.PartialState.NoChange
            }
    }

    private fun setRewardPopUpShownAtLeastOnce(): Observable<PaymentResultContract.PartialState> {
        return intent<PaymentResultContract.Intent.SetRewardPopUpShownAtLeastOnce>()
            .map {
                wasRewardPopUpShownAtLeastOnce = true
                PaymentResultContract.PartialState.NoChange
            }
    }

    private fun trackPaymentRewardClicked(): Observable<PaymentResultContract.PartialState> {
        return intent<PaymentResultContract.Intent.TrackPaymentRewardClicked>()
            .map {
                val state = getCurrentState()
                val cashbackSeenStatus = if (state.reward?.isUnclaimed() == true) UNSEEN else SEEN

                paymentAnalyticsEvents.get().trackPaymentStatusRewardIconClicked(
                    accountId = accountId,
                    relation = initialState.getRelationFrmAccountType(),
                    screen = PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_STATUS,
                    amount = state.juspayPaymentPollingModel?.paymentInfo?.paymentAmount ?: "",
                    paymentId = paymentId,
                    cashbackSeenStatus = cashbackSeenStatus,
                    rewardId = state.reward?.id ?: "",
                    rewardValue = state.reward?.amount.toString(),
                    collectionStatus = getCollectionActivationStatus.get().execute().blockingFirst(),
                    rewardStatus = state.reward?.status ?: ""
                )

                PaymentResultContract.PartialState.NoChange
            }
    }

    private fun startLoadingTimer(): Observable<PaymentResultContract.PartialState> {
        return intent<PaymentResultContract.Intent.StartLoadingTimer>()
            .switchMap {
                initTimer(PaymentResultContract.LoadingTimerState.TIMER_COUNT_DOWN_LIMIT)
                    .filter { shouldTimerTick }
            }
            .map { timerState ->
                PaymentResultContract.PartialState.SetTimerState(timerState)
            }
    }

    private fun initTimer(countDownTotalTime: Int): Observable<PaymentResultContract.LoadingTimerState> {
        return Observable.intervalRange(0, countDownTotalTime.toLong() + 1, 0, 1, TimeUnit.SECONDS)
            .map { value ->
                PaymentResultContract.LoadingTimerState.TimerSet(countDownTotalTime - value.toInt())
            }
    }

    private fun haltLoadingTimer(): Observable<PaymentResultContract.PartialState> {
        return intent<PaymentResultContract.Intent.HaltLoadingTimer>()
            .map {
                shouldTimerTick = false
                PaymentResultContract.PartialState.NoChange
            }
    }

    private fun getCustomerSupportType(): Observable<PaymentResultContract.PartialState> {
        return intent<PaymentResultContract.Intent.Load>()
            .switchMap {
                wrap(getCustomerSupportType.get().execute())
            }
            .map {
                if (it is Result.Success) {
                    PaymentResultContract.PartialState.SetSupportData(
                        it.value,
                        getCustomerSupportData.get().getCustomerSupportNumber(it.value),
                        getCustomerSupportData.get().get24x7String()
                    )
                } else
                    PaymentResultContract.PartialState.NoChange
            }
    }

    private fun actionSupportClicked(): Observable<PaymentResultContract.PartialState> {
        return intent<PaymentResultContract.Intent.SupportClicked>()
            .map {
                getCurrentState().juspayPaymentPollingModel?.let { response ->
                    paymentAnalyticsEvents.get().trackCustomerSupportMsgClicked(
                        source = PAYMENT_STATUS_PAGE_VIEW,
                        type = getCurrentState().supportType.value,
                        txnId = response.paymentId,
                        amount = response.paymentInfo.paymentAmount ?: "",
                        relation = accountType.lowercase(),
                        status = response.status.lowercase(),
                        supportMsg = it.msg
                    )
                }
                if (getCurrentState().supportType == SupportType.CALL)
                    emitViewEvent(PaymentResultContract.ViewEvents.CallCustomerCare)
                else pushIntent(PaymentResultContract.Intent.SendWhatsAppMessage(it.msg, it.number))
                PaymentResultContract.PartialState.NoChange
            }
    }

    private fun sendHelpWhatsAppMessage() = intent<PaymentResultContract.Intent.SendWhatsAppMessage>()
        .switchMap {
            wrap(
                communicationApi.get().goToWhatsAppWithTextOnlyExtendedBahaviuor(
                    ShareIntentBuilder(
                        shareText = it.msg,
                        phoneNumber = it.number
                    )
                )
            )
        }
        .map {
            when (it) {
                is Result.Progress -> PaymentResultContract.PartialState.NoChange
                is Result.Success -> {
                    emitViewEvent(PaymentResultContract.ViewEvents.SendWhatsAppMessage(it.value))
                }
                is Result.Failure -> {
                    if (it.error is IntentHelper.NoWhatsAppError) {
                        emitViewEvent(PaymentResultContract.ViewEvents.ShowWhatsAppError)
                    } else {
                        emitViewEvent(PaymentResultContract.ViewEvents.ShowDefaultError)
                    }
                }
            }

            PaymentResultContract.PartialState.NoChange
        }

    override fun reduce(
        currentState: PaymentResultContract.State,
        partialState: PaymentResultContract.PartialState,
    ): PaymentResultContract.State {
        return when (partialState) {
            is PaymentResultContract.PartialState.NoChange -> currentState
            is PaymentResultContract.PartialState.SetJuspayPollingResponse -> currentState.copy(
                juspayPaymentPollingModel = partialState.response,
                uiScreenType = partialState.uiScreenType
            )
            is PaymentResultContract.PartialState.SetRewardForPayment -> currentState.copy(reward = partialState.reward)
            is PaymentResultContract.PartialState.SetTimerState -> currentState.copy(
                loadingTimerState = partialState.loadingTimerState
            )
            is PaymentResultContract.PartialState.SetBlindPayShareLink ->
                currentState.copy(blindPayShareLink = partialState.blindPayShareLink)
            is PaymentResultContract.PartialState.SetSupportData -> currentState.copy(
                supportType = partialState.supportType,
                supportNumber = partialState.supportNumber,
                support24x7String = partialState.support24x7String,
            )
        }
    }
}
