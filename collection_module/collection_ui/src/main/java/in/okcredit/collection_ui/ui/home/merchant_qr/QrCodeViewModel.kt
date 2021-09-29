package `in`.okcredit.collection_ui.ui.home.merchant_qr

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.backend.contract.DeepLinkUrl
import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN
import `in`.okcredit.collection_ui.ui.home.CollectionHomeEventsTracker
import `in`.okcredit.collection_ui.ui.home.merchant_qr.QrCodeContract.*
import `in`.okcredit.collection_ui.ui.home.merchant_qr.QrCodeContract.PartialState.*
import `in`.okcredit.collection_ui.ui.home.merchant_qr.QrCodeFragment.Companion.MERCHANT_QR_SCREEN
import `in`.okcredit.collection_ui.ui.home.usecase.FindInfoBannerForMerchantQr
import `in`.okcredit.collection_ui.ui.home.usecase.ShouldShowOrderQr
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsContract
import `in`.okcredit.collection_ui.usecase.*
import `in`.okcredit.collection_ui.usecase.ShouldShowReferralBanner
import `in`.okcredit.individual.contract.IndividualRepository
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import tech.okcredit.android.auth.usecases.IsPasswordSet
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.contract.MerchantPrefSyncStatus
import tech.okcredit.feature_help.contract.GetSupportNumber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class QrCodeViewModel @Inject constructor(
    initialState: State,
    private val context: Lazy<Context>,
    private val getCollectionMerchantProfile: Lazy<GetCollectionMerchantProfile>,
    private val tracker: Lazy<Tracker>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val getMerchantQRIntent: Lazy<GetMerchantQRIntent>,
    private val getKycRiskCategory: Lazy<GetKycRiskCategory>,
    private val setCollectionDestination: Lazy<SetCollectionDestination>,
    private val saveMerchantQROnDevice: Lazy<SaveMerchantQROnDevice>,
    private val getNewOnlinePayments: Lazy<GetNewOnlinePayments>,
    private val getMerchantPreference: Lazy<IndividualRepository>,
    private val merchantPrefSyncStatus: Lazy<MerchantPrefSyncStatus>,
    private val isPasswordSet: Lazy<IsPasswordSet>,
    private val getKycStatus: Lazy<GetKycStatus>,
    private val sendCollectionEvent: Lazy<SendCollectionEvent>,
    private val getUnSettledAmountDueToInvalidBankDetails: Lazy<GetUnSettledAmountDueToInvalidBankDetails>,
    private val collectionTracker: Lazy<CollectionTracker>,
    private val triggerMerchantPayout: Lazy<TriggerMerchantPayout>,
    private val shouldShowReferralBanner: Lazy<ShouldShowReferralBanner>,
    private val referralEducationPreference: Lazy<ReferralEducationPreference>,
    private val getTotalOnlinePaymentCount: Lazy<GetTotalOnlinePaymentCount>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
    private val isCollectionActivatedOrOnlinePaymentExist: Lazy<IsCollectionActivatedOrOnlinePaymentExist>,
    private val getLastOnlinePayment: Lazy<GetLastOnlinePayment>,
    private val shouldShowOrderQr: Lazy<ShouldShowOrderQr>,
    private val getPaymentReminderIntent: Lazy<GetPaymentReminderIntent>,
    private val findInfoBannerForMerchantQr: Lazy<FindInfoBannerForMerchantQr>,
    private val collectionHomeEventsTracker: Lazy<CollectionHomeEventsTracker>,
    private val getSupportNumber: Lazy<GetSupportNumber>,
    private val communicationApi: Lazy<CommunicationRepository>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    private var viewMerchantDestinationEventFired = false

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            checkForOnlinePaymentActivationOnLoad(),
            syncMerchantQrAndPaymentOnLoad(),
            getCollectionProfileMerchant(),
            getMerchant(),
            openBottomSheetDialog(),
            deleteAccountPressed(),
            deleteAccount(),
            sendMerchantQr(),
            shareMerchantQr(),
            saveMerchantQr(),
            getKycRiskCategory(),
            getNewOnlinePaymentsCount(),
            getLatestOnlinePayment(),
            askToUpdatePin(),
            askToSetNewPin(),
            checkIsFourDigitPinOnLoad(),
            checkIsPasswordSetOnLoad(),
            syncMerchantPrefOnLoad(),
            checkIsFourDigitPinOnCheckIsFourDigit(),
            syncMerchantPrefOnSyncMerchantPref(),
            checkUnsettledAmountDueToInvalidBankDetails(),
            showAddMerchantDestinationScreen(),
            checkCollectionActivationAfterDeletion(),
            sendCollectionEvent(),
            triggerMerchantPayout(),
            shouldShowReferralBanner(),
            gotoReferralScreen(),
            getOnlinePaymentCount(),
            checkIsPasswordSet(),
            observeInfoCardTapped(),
            observeShowOrderQr(),
            observePaymentReminderIntent(),
            observeSupplierPayOnline(),
            observeQrTapped(),
            observeFindInfoBanner(),
            observeOrderQr(),
            openWhatsAppForHelp()
        )
    }

    private fun observeFindInfoBanner() = intent<Intent.Load>().switchMap {
        wrap(findInfoBannerForMerchantQr.get().execute())
    }.map {
        return@map if (it is Result.Success) {
            if (it.value != FindInfoBannerForMerchantQr.InfoBanner.None) {
                collectionHomeEventsTracker.get().trackInfoMessageDisplayed(it.value)
            }
            SetInfoBanner(it.value)
        } else {
            NoChange
        }
    }

    private fun observeShowOrderQr() = intent<Intent.Load>()
        .switchMap {
            wrap { shouldShowOrderQr.get().execute() }
        }.map {
            return@map if (it is Result.Success) {
                SetShowOrderQr(it.value)
            } else {
                NoChange
            }
        }

    private fun observePaymentReminderIntent() = intent<Intent.SendCustomerReminder>().switchMap {
        collectionHomeEventsTracker.get()
            .trackSelectContactSendPaymentRequest(CollectionHomeEventsTracker.Type.RECEIVE, it.customerId)
        wrap(getPaymentReminderIntent.get().execute(it.customerId, MERCHANT_QR_SCREEN, null))
    }.map {
        if (it is Result.Success) {
            emitViewEvent(ViewEvent.SendReminder(it.value))
        }
        return@map NoChange
    }

    private fun observeSupplierPayOnline() = intent<Intent.SupplierPayOnline>().map {
        collectionHomeEventsTracker.get()
            .trackSelectContactSendPaymentRequest(CollectionHomeEventsTracker.Type.SEND, it.supplierId)
        val deeplink = DeepLinkUrl.SUPPLIER_ONLINE_PAYMENT.replace("{supplier_id}", it.supplierId)
        emitViewEvent(ViewEvent.OpenSupplierScreen(deeplink))
        return@map NoChange
    }

    private fun syncMerchantQrAndPaymentOnLoad() = intent<Intent.Load>()
        .switchMap { wrap { collectionSyncer.get().scheduleSyncOnlinePayments(CollectionSyncer.Source.MERCHANT_QR) } }
        .map { NoChange }

    private fun checkForOnlinePaymentActivationOnLoad() = intent<Intent.Load>().map {
        pushIntent(Intent.CheckCollectionActivationAfterDeletion)
        NoChange
    }

    private fun emitNetworkError(): PartialState {
        emitViewEvent(
            ViewEvent.ShowSnackBar(R.string.no_internet_connection)
        )
        return NoChange
    }

    private fun checkIsFourDigitPinOnLoad() = intent<Intent.Load>()
        .map {
            pushIntent(Intent.CheckIsFourDigit(true))
            NoChange
        }

    private fun checkIsPasswordSetOnLoad() = intent<Intent.Load>()
        .map {
            pushIntent(Intent.CheckPasswordSet)
            NoChange
        }

    private fun syncMerchantPrefOnLoad() = intent<Intent.Load>()
        .compose(syncMerchantPref(true))

    private fun checkIsFourDigitPinOnCheckIsFourDigit() = intent<Intent.CheckIsFourDigit>()
        .switchMap { intent ->
            wrap {
                getMerchantPreference.get().getPreference(PreferenceKey.FOUR_DIGIT_PIN)
                    .map {
                        intent.fromLoad to it.toBoolean()
                    }
                    .first()
            }
        }.map {
            when (it) {
                is Result.Progress -> NoChange
                is Result.Success -> {
                    if (it.value.first.not()) {
                        emitViewEvent(ViewEvent.CheckFourDigitPinDone(it.value.second))
                    }
                    SetIsFourDigitPin(it.value.second)
                }
                is Result.Failure -> {
                    if (isInternetIssue(it.error)) {
                        emitNetworkError()
                    } else
                        ErrorState
                }
            }
        }

    private fun syncMerchantPrefOnSyncMerchantPref() = intent<Intent.SyncMerchantPref>()
        .compose(syncMerchantPref(false))

    private fun askToUpdatePin() = intent<Intent.UpdatePin>()
        .map {
            emitViewEvent(ViewEvent.ShowUpdatePinDialog)
            NoChange
        }

    private fun askToSetNewPin() = intent<Intent.SetNewPin>()
        .map {
            emitViewEvent(ViewEvent.GoToSetNewPinScreen)
            NoChange
        }

    private fun checkIsPasswordSet() = intent<Intent.CheckPasswordSet>()
        .switchMap { wrap(isPasswordSet.get().execute()) }
        .map {
            when (it) {
                is Result.Progress -> NoChange
                is Result.Success -> SetIsPasswordEnabled(it.value)
                is Result.Failure -> NoChange
            }
        }

    private fun getCollectionProfileMerchant() = intent<Intent.LoadCollectionProfileMerchant>()
        .switchMap { wrap(getCollectionMerchantProfile.get().execute()) }
        .map {
            when (it) {
                is Result.Progress -> NoChange
                is Result.Success -> {
                    if (viewMerchantDestinationEventFired.not()) {
                        tracker.get().trackEvents(
                            Event.VIEW_COLLECTION_PROFILE,
                            type = it.value.type,
                            screen = MERCHANT_DESTINATION_SCREEN,
                            propertiesMap = PropertiesMap.create()
                                .add(PropertyKey.AB_VARIANT, PropertyValue.MERCHANT_DESTINATION_INSIGHT_V2)
                        )
                        viewMerchantDestinationEventFired = true
                    }

                    SetCollectionMerchantProfile(it.value)
                }
                is Result.Failure -> {
                    when {
                        isAuthenticationIssue(it.error) -> {
                            NoChange
                        }
                        isInternetIssue(it.error) -> NoChange
                        else -> ErrorState
                    }
                }
            }
        }

    private fun getMerchant() = intent<Intent.LoadMerchant>()
        .switchMap { wrap(getActiveBusiness.get().execute()) }
        .map {
            when (it) {
                is Result.Progress -> NoChange
                is Result.Success -> {
                    SetBusiness(it.value)
                }
                is Result.Failure -> {
                    when {
                        isAuthenticationIssue(it.error) -> {
                            NoChange
                        }
                        isInternetIssue(it.error) -> NoChange
                        else -> ErrorState
                    }
                }
            }
        }

    private fun getKycDetails() = Observable.zip(
        getKycStatus.get().execute(),
        getKycRiskCategory.get().execute(),
        { kycStatus, kycRisk ->
            Pair(kycStatus, kycRisk)
        }
    )

    private fun getKycRiskCategory() = intent<Intent.LoadKycDetails>()
        .switchMap { getKycDetails() }
        .map {
            SetKycRiskCategory(it.first, it.second.kycRiskCategory, it.second.isLimitReached)
        }

    private fun openBottomSheetDialog() = intent<Intent.OpenBottomSheetDialog>()
        .throttleFirst(300, TimeUnit.MILLISECONDS)
        .map {
            emitViewEvent(ViewEvent.OpenBottomSheetDialog)
            NoChange
        }

    private fun deleteAccountPressed() = intent<Intent.DeleteAccountPressed>()
        .throttleFirst(300, TimeUnit.MILLISECONDS)
        .map {
            emitViewEvent(ViewEvent.DeleteAccount)
            NoChange
        }

    private fun sendMerchantQr() = intent<Intent.SendMerchantQR>()
        .throttleFirst(300, TimeUnit.MILLISECONDS)
        .switchMap {
            wrap(getMerchantQRIntent.get().execute(true))
        }.map {
            when (it) {
                is Result.Progress -> NoChange
                is Result.Success -> {
                    emitViewEvent(ViewEvent.ShareMerchant(it.value))
                    NoChange
                }
                is Result.Failure -> NoChange
            }
        }

    private fun shareMerchantQr() = intent<Intent.ShareMerchantQR>()
        .throttleFirst(300, TimeUnit.MILLISECONDS)
        .switchMap {
            wrap(getMerchantQRIntent.get().execute())
        }.map {
            when (it) {
                is Result.Progress -> NoChange
                is Result.Success -> {
                    emitViewEvent(ViewEvent.ShareMerchant(it.value))
                    NoChange
                }
                is Result.Failure -> NoChange
            }
        }

    private fun sendCollectionEvent() = intent<Intent.SendMerchantQR>()
        .throttleFirst(300, TimeUnit.MILLISECONDS)
        .switchMap {
            wrap(sendCollectionEvent.get().execute(null, SendCollectionEvent.EVENT_MERCHANT_QR))
        }.map {
            NoChange
        }

    private fun saveMerchantQr() = intent<Intent.SaveMerchantQR>()
        .switchMap {
            wrap(saveMerchantQROnDevice.get().execute())
        }.map {
            when (it) {
                is Result.Progress -> NoChange
                is Result.Success -> {
                    emitViewEvent(ViewEvent.ShowSnackBar(R.string.merchant_qr_saved_successfully))
                    NoChange
                }
                is Result.Failure -> {
                    emitViewEvent(ViewEvent.ShowSnackBar(R.string.err_default))
                    NoChange
                }
            }
        }

    private fun deleteAccount() = intent<Intent.DeleteAccount>()
        .switchMap {
            val merchantId = getCurrentState().responseData.business?.id
            wrap(
                setCollectionDestination.get().execute(CollectionMerchantProfile(merchantId!!))
            )
        }
        .map {
            when (it) {
                is Result.Progress -> NoChange
                is Result.Success -> {
                    pushIntent(Intent.CheckCollectionActivationAfterDeletion)
                    tracker.get().trackEvents(
                        CollectionTracker.CollectionEvent.COLLECTION_DELETED,
                        type = it.value.type,
                        screen = MERCHANT_DESTINATION_SCREEN
                    )
                    collectionTracker.get().setUserProperty(CollectionEventTracker.KYC_VERIFIED, "false")
                    NoChange
                }
                is Result.Failure -> {
                    when {
                        isInternetIssue(it.error) -> emitNetworkError()
                        else -> {
                            ViewEvent.ShowSnackBar(R.string.err_default)
                            NoChange
                        }
                    }
                }
            }
        }

    private fun getNewOnlinePaymentsCount() = intent<Intent.LoadNewOnlinePaymentsCount>()
        .switchMap { getNewOnlinePayments.get().execute(Unit) }
        .map {
            when (it) {
                is Result.Progress -> NoChange
                is Result.Success -> {
                    val count = it.value.size
                    SetNewCount(count)
                }
                is Result.Failure -> {
                    when {
                        isAuthenticationIssue(it.error) -> {
                            NoChange
                        }
                        isInternetIssue(it.error) -> NoChange
                        else -> ErrorState
                    }
                }
            }
        }

    private fun getLatestOnlinePayment() = intent<Intent.Load>()
        .switchMap { wrap(getLastOnlinePayment.get().execute()) }
        .map { result ->
            return@map if (result is Result.Success) {
                SetLatestPayment(result.value)
            } else {
                NoChange
            }
        }

    private fun syncMerchantPref(isFromLoad: Boolean): ObservableTransformer<Intent, PartialState> {
        return ObservableTransformer<Intent, PartialState> { upstream ->
            upstream.switchMap {
                wrap(merchantPrefSyncStatus.get().execute())
            }
                .map {
                    when (it) {
                        is Result.Progress -> NoChange
                        is Result.Success -> {
                            if (isFromLoad.not())
                                emitViewEvent(ViewEvent.SyncDone)
                            SetIsMerchantPrefSync(true)
                        }
                        is Result.Failure -> {
                            if (isInternetIssue(it.error)) {
                                if (isFromLoad.not()) {
                                    emitNetworkError()
                                } else {
                                    NoChange
                                }
                            } else
                                ErrorState
                        }
                    }
                }
        }
    }

    private fun checkUnsettledAmountDueToInvalidBankDetails() = intent<Intent.Load>()
        .switchMap {
            wrap(
                getUnSettledAmountDueToInvalidBankDetails.get()
                    .execute(
                        OnlinePaymentErrorCode.EP001.value,
                        OnlinePaymentsContract.PaymentStatus.PAYOUT_FAILED.value
                    )
            )
        }
        .map {
            if (it is Result.Success) {
                SetUnSettleAmountDueToInvalidBank(it.value)
            } else
                NoChange
        }

    private fun showAddMerchantDestinationScreen() = intent<Intent.ShowAddMerchantDestinationScreen>()
        .map {
            emitViewEvent(ViewEvent.ShowAddMerchantDestinationScreen)
            NoChange
        }

    private fun checkCollectionActivationAfterDeletion() = intent<Intent.CheckCollectionActivationAfterDeletion>()
        .switchMap {
            wrap(
                isCollectionActivatedOrOnlinePaymentExist.get().execute()
                    .firstOrError()
            )
        }
        .map {
            when (it) {
                is Result.Success -> {
                    // if collection not activated go to setup screen else stay here only
                    if (!it.value) {
                        emitViewEvent(ViewEvent.GoToCollectionsSetupScreen)
                    }
                    NoChange
                }
                else -> NoChange
            }
        }

    private fun triggerMerchantPayout() = intent<Intent.TriggerMerchantPayout>()
        .switchMap {
            wrap(
                triggerMerchantPayout.get().executePayout(
                    it.payoutType,
                    "merchant_qr",
                    "",
                    ""
                )
            )
        }
        .map {
            NoChange
        }

    private fun observeInfoCardTapped() = intent<Intent.InfoCardTapped>()
        .map {
            when (getCurrentState().infoBanner) {
                is FindInfoBannerForMerchantQr.InfoBanner.DailyLimitReached -> {
                    emitViewEvent(ViewEvent.GoToKyc)
                }
                FindInfoBannerForMerchantQr.InfoBanner.KycFailed -> {
                    emitViewEvent(ViewEvent.GoToKyc)
                }
                is FindInfoBannerForMerchantQr.InfoBanner.RefundAlert -> {
                    emitViewEvent(ViewEvent.ShowAddMerchantDestinationScreen)
                }
                FindInfoBannerForMerchantQr.InfoBanner.TargetedReferral -> {
                    pushIntent(Intent.GotoReferralScreen)
                }
                FindInfoBannerForMerchantQr.InfoBanner.KycPending -> {
                    pushIntent(Intent.OpenWhatsAppForHelp)
                }
                is FindInfoBannerForMerchantQr.InfoBanner.TrialLimitReached -> {
                    emitViewEvent(ViewEvent.GoToKyc)
                }
                else -> {
                }
            }
            return@map NoChange
        }

    private fun observeOrderQr() = intent<Intent.OrderMerchantQR>()
        .map {
            collectionHomeEventsTracker.get().trackOrderQr()
            emitViewEvent(ViewEvent.OpenOrderQr)
            NoChange
        }

    private fun openWhatsAppForHelp() = intent<Intent.OpenWhatsAppForHelp>()
        .switchMap {
            wrap(
                communicationApi.get().goToWhatsApp(
                    ShareIntentBuilder(
                        shareText = context.get().getString(R.string.help_whatsapp_msg),
                        phoneNumber = getSupportNumber.get().supportNumber
                    )
                )
            )
        }
        .map {
            when (it) {
                is Result.Failure -> {
                    if (it.error is IntentHelper.NoWhatsAppError)
                        emitViewEvent(ViewEvent.ShowSnackBar(R.string.whatsapp_not_installed))
                    else
                        emitViewEvent(ViewEvent.ShowSnackBar(R.string.err_default))
                    NoChange
                }
                is Result.Success -> {
                    emitViewEvent(ViewEvent.OpenWhatsAppForHelp(it.value))
                    NoChange
                }
                else -> NoChange
            }
        }

    private fun observeQrTapped() = intent<Intent.QrTapped>()
        .map {
            when (getCurrentState().infoBanner) {
                is FindInfoBannerForMerchantQr.InfoBanner.DailyLimitAvailable -> {
                    emitViewEvent(ViewEvent.GoToKyc)
                }
                is FindInfoBannerForMerchantQr.InfoBanner.DailyLimitReached -> {
                    emitViewEvent(ViewEvent.GoToKyc)
                }
                FindInfoBannerForMerchantQr.InfoBanner.KycFailed -> {
                    emitViewEvent(ViewEvent.GoToKyc)
                }
                is FindInfoBannerForMerchantQr.InfoBanner.RefundAlert -> {
                    emitViewEvent(ViewEvent.ShowAddMerchantDestinationScreen)
                }
                is FindInfoBannerForMerchantQr.InfoBanner.TrialLimitAvailable -> {
                    emitViewEvent(ViewEvent.GoToKyc)
                }
                is FindInfoBannerForMerchantQr.InfoBanner.TrialLimitReached -> {
                    emitViewEvent(ViewEvent.GoToKyc)
                }
                else -> {
                }
            }
            return@map NoChange
        }

    private fun gotoReferralScreen() = intent<Intent.GotoReferralScreen>()
        .switchMap {
            wrap(
                referralEducationPreference.get().shouldShowReferralEducationScreen()
            )
        }
        .map {
            if (it is Result.Success) {
                if (it.value)
                    emitViewEvent(ViewEvent.GotoReferralEducationScreen)
                else {
                    emitViewEvent(ViewEvent.GotoReferralInviteListScreen)
                }
                collectionTracker.get()
                    .trackCollectionReferralGiftClicked(accountId = "", screen = MERCHANT_DESTINATION_SCREEN, "")
                NoChange
            } else NoChange
        }

    private fun shouldShowReferralBanner() = intent<Intent.Load>()
        .switchMap {
            wrap(shouldShowReferralBanner.get().execute())
        }
        .map {
            if (it is Result.Success) PartialState.ShouldShowReferralBanner(show = it.value)
            else NoChange
        }

    private fun getOnlinePaymentCount() = intent<Intent.Load>()
        .switchMap { wrap(getTotalOnlinePaymentCount.get().execute()) }
        .map {
            if (it is Result.Success) {
                SetTotalOnlinePaymentCounts(it.value)
            } else {
                NoChange
            }
        }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return QrCodeStateReducer.reduce(currentState, partialState)
    }
}
