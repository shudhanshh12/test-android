package `in`.okcredit.payment.ui.juspay.juspay_payment_bottom_sheet

import `in`.okcredit.analytics.PropertyValue.COMPLETE_KYC
import `in`.okcredit.analytics.PropertyValue.FIRST
import `in`.okcredit.analytics.PropertyValue.NOT_AVAILABLE
import `in`.okcredit.analytics.PropertyValue.ONLINE_PAYMENT
import `in`.okcredit.analytics.PropertyValue.PAYMENT
import `in`.okcredit.analytics.PropertyValue.REGULAR
import `in`.okcredit.analytics.PropertyValue.RELATIONSHIP
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.cashback.contract.usecase.GetCashbackMessageDetails
import `in`.okcredit.cashback.contract.usecase.IsCustomerCashbackFeatureEnabled
import `in`.okcredit.cashback.contract.usecase.IsSupplierCashbackFeatureEnabled
import `in`.okcredit.collection.contract.ShouldShowCreditCardInfoForKyc
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.ENTER_AMOUNT_PAGE
import `in`.okcredit.payment.usecases.*
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import merchant.okcredit.accounting.contract.model.LedgerType
import merchant.okcredit.accounting.contract.model.SupportType
import merchant.okcredit.accounting.contract.model.isCustomer
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportData
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportType
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import java.util.*
import javax.inject.Inject

class PaymentEditAmountViewModel @Inject constructor(
    private val initialState: PaymentEditAmountContract.State,
    @ViewModelParam(PaymentEditAmountContract.ARG_ACCOUNT_TYPE) val ledgerType: LedgerType,
    @ViewModelParam(PaymentEditAmountContract.ARG_ACCOUNT_ID) val accountId: String,
    private val isCustomerCashbackFeatureEnabled: Lazy<IsCustomerCashbackFeatureEnabled>,
    private val isSupplierCashbackFeatureEnabled: Lazy<IsSupplierCashbackFeatureEnabled>,
    private val tracker: Lazy<Tracker>,
    private val getCashbackMessageDetails: Lazy<GetCashbackMessageDetails>,
    private val shouldShowKycBannerOnPaymentEditAmountPage: Lazy<ShouldShowKycBannerOnPaymentEditAmountPage>,
    private val shouldShowCreditCardInfoForKyc: Lazy<ShouldShowCreditCardInfoForKyc>,
    private val getKycBannerType: Lazy<GetKycBannerType>,
    private val isPaymentEditAmountKycBannerEnabled: Lazy<IsPaymentEditAmountKycBannerEnabled>,
    private val getCustomerSupportType: Lazy<GetCustomerSupportType>,
    private val communicationRepository: Lazy<CommunicationRepository>,
    private val paymentAnalyticsEvents: Lazy<PaymentAnalyticsEvents>,
    private val getCustomerSupportData: Lazy<GetCustomerSupportData>,
) : BaseViewModel<PaymentEditAmountContract.State, PaymentEditAmountContract.PartialState, PaymentEditAmountContract.ViewEvents>(
    initialState
) {

    private val currentAmountSelectedBehaviorSubject = BehaviorSubject.createDefault<Long>(0)

    override fun handle(): Observable<out UiState.Partial<PaymentEditAmountContract.State>> {
        return Observable.mergeArray(
            setEnteredAmount(),
            trackSummaryEvent(),
            shouldShowCreditCardInfoForKyc(),
            showCashbackMessageIfAvailable(),
            handleKycData(),
            handleKycEntryPointClick(),
            closeKycBannerClick(),
            getCustomerSupportType(),
            actionSupportClicked(),
            sendWhatsAppMessage(),
        )
    }

    private fun trackSummaryEvent(): Observable<PaymentEditAmountContract.PartialState> {
        return intent<PaymentEditAmountContract.Intent.TrackPageSummaryEvent>()
            .map {
                emitViewEvent(
                    PaymentEditAmountContract.ViewEvents.TrackPageSummaryEvent(
                        type = it.type,
                        number = it.number
                    )
                )
                PaymentEditAmountContract.PartialState.NoChange
            }
    }

    private fun setEnteredAmount(): Observable<PaymentEditAmountContract.PartialState>? {
        return intent<PaymentEditAmountContract.Intent.SetAmountEntered>()
            .map {
                currentAmountSelectedBehaviorSubject.onNext(it.amount)
                PaymentEditAmountContract.PartialState.SetAmountEntered(it.amount)
            }
    }

    private fun shouldShowCreditCardInfoForKyc(): Observable<PaymentEditAmountContract.PartialState> {
        return intent<PaymentEditAmountContract.Intent.Load>()
            .switchMap { shouldShowCreditCardInfoForKyc.get().execute() }
            .map {
                PaymentEditAmountContract.PartialState.SetShouldShowCreditCardInfoForKyc(it)
            }
    }

    private fun showCashbackMessageIfAvailable(): Observable<PaymentEditAmountContract.PartialState> {
        return intent<PaymentEditAmountContract.Intent.Load>()
            .switchMap {
                if (ledgerType.isCustomer()) {
                    return@switchMap isCustomerCashbackFeatureEnabled.get().execute()
                } else {
                    return@switchMap isSupplierCashbackFeatureEnabled.get().execute()
                }
            }
            .switchMap { isEnabled ->
                if (isEnabled) {
                    return@switchMap wrap(
                        getCashbackMessageDetails.get().execute()
                            .map { cashbackMessageDetails ->

                                val cashbackMessageType =
                                    if (cashbackMessageDetails.isFirstTransaction) FIRST else REGULAR
                                tracker.get().trackEnterAmountCashbackPageView(
                                    accountId = accountId,
                                    screen = RELATIONSHIP,
                                    type = ONLINE_PAYMENT,
                                    relation = initialState.getRelationFrmAccountType(),
                                    cashbackMessageType = cashbackMessageType,
                                    cashbackAmount = cashbackMessageDetails.cashbackAmount.toString(),
                                    minimumPaymentAmount = cashbackMessageDetails.minimumPaymentAmount.toString(),
                                )

                                getCashbackMessageDetails.get().getHumanReadableStringFromModel(cashbackMessageDetails)
                            }
                    )
                } else {
                    tracker.get().trackEnterAmountCashbackPageView(
                        accountId = accountId,
                        screen = RELATIONSHIP,
                        type = ONLINE_PAYMENT,
                        relation = initialState.getRelationFrmAccountType(),
                        cashbackMessageType = NOT_AVAILABLE,
                        cashbackAmount = "",
                        minimumPaymentAmount = "",
                    )
                    return@switchMap wrap(Single.just(""))
                }
            }
            .map {
                when (it) {
                    is Result.Progress -> PaymentEditAmountContract.PartialState.NoChange
                    is Result.Success -> PaymentEditAmountContract.PartialState.SetCashbackMessage(it.value)
                    is Result.Failure -> PaymentEditAmountContract.PartialState.NoChange
                }
            }
    }

    private fun closeKycBannerClick(): Observable<PaymentEditAmountContract.PartialState> {
        return intent<PaymentEditAmountContract.Intent.CloseKycInfoBanner>()
            .switchMap {
                shouldShowKycBannerOnPaymentEditAmountPage.get().setValue(false).andThen(Observable.just(Unit))
            }
            .map {
                tracker.get().trackKycEntryPointDismissed(
                    riskType = initialState.kycRiskCategory.value.toLowerCase(Locale.getDefault()),
                    screen = PAYMENT,
                    relation = initialState.getRelationFrmAccountType(),
                    dailyLimitLeft = initialState.remainingDailyLimit,
                )
                PaymentEditAmountContract.PartialState.NoChange
            }
    }

    private fun handleKycData(): Observable<PaymentEditAmountContract.PartialState> {
        return intent<PaymentEditAmountContract.Intent.Load>()
            .switchMap {
                Observable.combineLatest(
                    isPaymentEditAmountKycBannerEnabled.get().execute(),
                    currentAmountSelectedBehaviorSubject,
                    shouldShowKycBannerOnPaymentEditAmountPage.get().execute(),
                    { isKycBannerEnabled: Boolean, amount: Long, shouldShowKycBannerOnPaymentEditAmountPage: Boolean ->
                        Triple(isKycBannerEnabled, amount, shouldShowKycBannerOnPaymentEditAmountPage)
                    }
                )
            }
            .map { (isKycBannerEnabled, currentAmountSelected, shouldShowKycBannerOnPaymentEditAmountPage) ->
                getKycBannerType.get().execute(
                    isKycBannerEnabled,
                    initialState.kycStatus,
                    initialState.kycRiskCategory,
                    shouldShowKycBannerOnPaymentEditAmountPage,
                    currentAmountSelected,
                    initialState.maxDailyLimit,
                    initialState.remainingDailyLimit,
                    initialState.futureAmountLimit,
                )
            }
            .map { kycBannerType ->
                if (kycBannerType.hasKycEntryPoint()) {
                    tracker.get().trackEntryPointViewed(
                        source = PAYMENT,
                        type = COMPLETE_KYC,
                        relation = initialState.getRelationFrmAccountType(),
                        kycMessageType = kycBannerType.getTypeForAnalytics(),
                    )
                }
                PaymentEditAmountContract.PartialState.SetKycBannerType(kycBannerType)
            }
    }

    private fun handleKycEntryPointClick(): Observable<PaymentEditAmountContract.PartialState> {
        return intent<PaymentEditAmountContract.Intent.KycEntryPointClicked>()
            .map {

                emitViewEvent(PaymentEditAmountContract.ViewEvents.GoToKycWebScreen)

                val state = getCurrentState()
                tracker.get().trackEntryPointClicked(
                    source = PAYMENT,
                    type = COMPLETE_KYC,
                    relation = initialState.getRelationFrmAccountType(),
                    kycMessageType = state.kycBannerType.getTypeForAnalytics(),
                    riskType = state.kycRiskCategory.value.toLowerCase(Locale.getDefault()),
                    dailyLimitLeft = state.remainingDailyLimit,
                )

                PaymentEditAmountContract.PartialState.NoChange
            }
    }

    private fun getCustomerSupportType(): Observable<PaymentEditAmountContract.PartialState> {
        return intent<PaymentEditAmountContract.Intent.Load>()
            .switchMap {
                wrap(getCustomerSupportType.get().execute())
            }
            .map {
                if (it is Result.Success) {
                    val supportNumber = getCustomerSupportData.get().getCustomerSupportNumber(it.value)
                    val support24x7String = getCustomerSupportData.get().get24x7String()
                    pushIntent(PaymentEditAmountContract.Intent.TrackPageSummaryEvent(it.value.value, supportNumber))
                    if (it.value.value.isNotBlank())
                        PaymentEditAmountContract.PartialState.SetSupportData(
                            it.value,
                            supportNumber,
                            support24x7String
                        )
                    else PaymentEditAmountContract.PartialState.NoChange
                } else
                    PaymentEditAmountContract.PartialState.NoChange
            }
    }

    private fun actionSupportClicked(): Observable<PaymentEditAmountContract.PartialState> {
        return intent<PaymentEditAmountContract.Intent.SupportClicked>()
            .map {
                paymentAnalyticsEvents.get().trackCustomerSupportMsgClicked(
                    source = ENTER_AMOUNT_PAGE,
                    type = getCurrentState().supportType.value,
                    txnId = "",
                    amount = "",
                    relation = ledgerType.value.lowercase(),
                    status = "",
                    supportMsg = it.msg
                )
                if (getCurrentState().supportType == SupportType.CALL)
                    emitViewEvent(PaymentEditAmountContract.ViewEvents.CallCustomerCare)
                else pushIntent(PaymentEditAmountContract.Intent.SendWhatsAppMessage(it.msg, it.number))
                PaymentEditAmountContract.PartialState.NoChange
            }
    }

    private fun sendWhatsAppMessage() = intent<PaymentEditAmountContract.Intent.SendWhatsAppMessage>()
        .switchMap {
            wrap(
                communicationRepository.get().goToWhatsApp(
                    ShareIntentBuilder(
                        shareText = it.msg,
                        phoneNumber = it.number
                    )
                )
            )
        }
        .map {
            when (it) {
                is Result.Success -> {
                    emitViewEvent(PaymentEditAmountContract.ViewEvents.SendWhatsAppMessage(it.value))
                }
                is Result.Failure -> {
                    if (it.error is IntentHelper.NoWhatsAppError) {
                        emitViewEvent(PaymentEditAmountContract.ViewEvents.ShowWhatsAppError)
                    } else {
                        emitViewEvent(PaymentEditAmountContract.ViewEvents.ShowDefaultError)
                    }
                }

                else -> {
                }
            }

            PaymentEditAmountContract.PartialState.NoChange
        }

    override fun reduce(
        currentState: PaymentEditAmountContract.State,
        partialState: PaymentEditAmountContract.PartialState,
    ): PaymentEditAmountContract.State {
        return when (partialState) {
            PaymentEditAmountContract.PartialState.NoChange -> currentState
            is PaymentEditAmountContract.PartialState.SetAmountEntered -> currentState.copy(
                currentAmountSelected = partialState.amount
            )
            is PaymentEditAmountContract.PartialState.SetCashbackMessage -> currentState.copy(
                cashbackMessage = partialState.cashbackMessage
            )
            is PaymentEditAmountContract.PartialState.SetKycBannerType -> currentState.copy(
                kycBannerType = partialState.kycBannerType
            )
            is PaymentEditAmountContract.PartialState.SetShouldShowCreditCardInfoForKyc -> currentState.copy(
                shouldShowCreditCardInfoForKyc = partialState.shouldShowCreditCardInfoForKyc
            )
            is PaymentEditAmountContract.PartialState.SetSupportData -> currentState.copy(
                supportType = partialState.supportType,
                supportNumber = partialState.supportNumber,
                support24x7String = partialState.support24x7String,
            )
        }
    }
}
