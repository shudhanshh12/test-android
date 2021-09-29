package `in`.okcredit.collection_ui.ui.passbook.detail

import `in`.okcredit.collection.contract.GetCollectionMerchantProfile
import `in`.okcredit.collection.contract.TriggerMerchantPayout
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.OnlineCollectionTracker
import `in`.okcredit.collection_ui.analytics.OnlineCollectionTracker.Screen.collectionPaymentTransaction
import `in`.okcredit.collection_ui.ui.passbook.detail.PaymentDetailContract.PartialState
import `in`.okcredit.collection_ui.usecase.GetCollectionOnlinePayment
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.feature_help.contract.GetSupportNumber
import javax.inject.Inject

class PaymentDetailViewModel @Inject constructor(
    private val initialState: PaymentDetailContract.State,
    private val getCollectionOnlinePayment: Lazy<GetCollectionOnlinePayment>,
    private val communicationRepository: Lazy<CommunicationRepository>,
    private val context: Lazy<Context>,
    private val getCollectionMerchantProfile: Lazy<GetCollectionMerchantProfile>,
    private val getSupportNumber: Lazy<GetSupportNumber>,
    private val triggerMerchantPayout: Lazy<TriggerMerchantPayout>,
    private val communicationApi: Lazy<CommunicationRepository>,
    private val onlineCollectionTracker: Lazy<OnlineCollectionTracker>,
) : BaseViewModel<PaymentDetailContract.State, PartialState, PaymentDetailContract.ViewEvents>(
    initialState
) {
    override fun handle(): Observable<out UiState.Partial<PaymentDetailContract.State>> {
        return Observable.mergeArray(
            getOnlinePayment(),
            sendWhatsApp(),
            getMerchantPaymentAddress(),
            openWhatsAppForHelp(),
            triggerMerchantPayout(),
            showAddMerchantDestinationDialog(),
            showInvalidAddressToolTip(),
            showRefundConsentBottomSheet(),
        )
    }

    private fun getOnlinePayment() = intent<PaymentDetailContract.Intent.Load>()
        .switchMap {
            getCollectionOnlinePayment.get().execute(initialState.txnId)
        }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> {
                    PartialState.SetCollectionOnlinePayment(it.value)
                }
                is Result.Failure -> {
                    when {
                        isAuthenticationIssue(it.error) -> PartialState.NoChange
                        else -> {
                            emitViewEvent(PaymentDetailContract.ViewEvents.ShowError(it.error.localizedMessage))
                            PartialState.NoChange
                        }
                    }
                }
            }
        }

    private fun sendWhatsApp() = intent<PaymentDetailContract.Intent.SendWhatsApp>()
        .switchMap {
            wrap(
                communicationRepository.get().goToWhatsApp(
                    ShareIntentBuilder(
                        imageFrom = ImagePath.ImageUriFromBitMap(
                            it.image, context.get(), "online_collection", "online_collection.jpg"
                        )
                    )
                )
            )
        }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> {
                    emitViewEvent(PaymentDetailContract.ViewEvents.SendWhatsApp(it.value))
                    PartialState.NoChange
                }
                is Result.Failure -> {
                    when {
                        isAuthenticationIssue(it.error) -> PartialState.NoChange
                        else -> {
                            emitViewEvent(PaymentDetailContract.ViewEvents.ShowError(it.error.localizedMessage))
                            PartialState.NoChange
                        }
                    }
                }
            }
        }

    private fun getMerchantPaymentAddress() = intent<PaymentDetailContract.Intent.Load>()
        .switchMap { wrap(getCollectionMerchantProfile.get().execute()) }
        .map {
            when (it) {
                is Result.Success -> {
                    PartialState.SetMerchantPaymentAddress(it.value.payment_address)
                }
                else -> {
                    PartialState.NoChange
                }
            }
        }

    private fun openWhatsAppForHelp() = intent<PaymentDetailContract.Intent.OpenWhatsAppForHelp>()
        .switchMap {
            onlineCollectionTracker.get().trackChatWithSupport(initialState.txnId, collectionPaymentTransaction, getCurrentState().source)
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
                        emitViewEvent(
                            PaymentDetailContract.ViewEvents.ShowError(
                                context.get()
                                    .getString(R.string.whatsapp_not_installed)
                            )
                        )
                    else
                        emitViewEvent(
                            PaymentDetailContract.ViewEvents.ShowError(
                                context.get()
                                    .getString(R.string.err_default)
                            )
                        )
                    PartialState.NoChange
                }
                is Result.Success -> {
                    emitViewEvent(PaymentDetailContract.ViewEvents.OpenWhatsAppForHelp(it.value))
                    PartialState.NoChange
                }
                else -> PartialState.NoChange
            }
        }

    private fun triggerMerchantPayout() = intent<PaymentDetailContract.Intent.TriggerMerchantPayout>()
        .switchMap {
            wrap(
                triggerMerchantPayout.get()
                    .executePayout(
                        it.payoutType,
                        collectionType = getCurrentState().collectionOnlinePayment?.type ?: "",
                        payoutId = "",
                        paymentId = ""
                    )
            )
        }.map {
            PartialState.NoChange
        }

    private fun showAddMerchantDestinationDialog() =
        intent<PaymentDetailContract.Intent.ShowAddMerchantDestinationDialog>()
            .map {
                emitViewEvent(PaymentDetailContract.ViewEvents.ShowAddMerchantDestinationDialog)
                PartialState.NoChange
            }

    private fun showInvalidAddressToolTip() =
        intent<PaymentDetailContract.Intent.ShowInvalidAddressToolTip>()
            .map {
                emitViewEvent(PaymentDetailContract.ViewEvents.ShowInvalidAddressToolTip)
                PartialState.NoChange
            }

    private fun showRefundConsentBottomSheet() =
        intent<PaymentDetailContract.Intent.ShowRefundConsentBottomSheet>()
            .map {
                onlineCollectionTracker.get().trackRefundToCustomerClicked(initialState.txnId, getCurrentState().source)
                emitViewEvent(PaymentDetailContract.ViewEvents.ShowRefundConsentBottomSheet)
                PartialState.NoChange
            }

    override fun reduce(
        currentState: PaymentDetailContract.State,
        partialState: PartialState,
    ): PaymentDetailContract.State {
        return when (partialState) {
            PartialState.NoChange -> currentState
            is PartialState.SetCollectionOnlinePayment -> currentState.copy(collectionOnlinePayment = partialState.collectionOnlinePayment)
            is PartialState.SetMerchantPaymentAddress -> currentState.copy(merchantPaymentAddress = partialState.address)
        }
    }
}
