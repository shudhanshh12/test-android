package `in`.okcredit.payment.ui.add_payment_dialog

import `in`.okcredit.collection.contract.CollectionDestinationType
import `in`.okcredit.collection.contract.CollectionServerErrors
import `in`.okcredit.collection.contract.SetPaymentOutDestination
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_ACCOUNT_TYPE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_ACCOUNT_ID
import `in`.okcredit.payment.R
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.BANK
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.INTERNAL_SUPPLIER_COLLECTION
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.INVALID_ACCOUNT_NUMBER
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.INVALID_IFSC
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.INVALID_PAYMENT_ADDRESS
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.I_DONT_KNOW
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.NOT_AWARE
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_ADDRESS_DETAILS
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.UPI
import `in`.okcredit.payment.ui.add_payment_dialog.AddPaymentDestinationContract.*
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import merchant.okcredit.accounting.contract.model.SupportType
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportData
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportType
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddPaymentDestinationViewModel @Inject constructor(
    private val initialState: State,
    @ViewModelParam(ARG_PAYMENT_ACCOUNT_ID) val accountId: String,
    @ViewModelParam(ARG_ACCOUNT_TYPE) val accountType: String,
    private val paymentAnalyticsEvents: Lazy<PaymentAnalyticsEvents>,
    private val setPaymentOutDestination: Lazy<SetPaymentOutDestination>,
    private val context: Lazy<Context>,
    private val getPaymentSupportType: Lazy<GetCustomerSupportType>,
    private val communicationRepository: Lazy<CommunicationRepository>,
    private val getCustomerSupportData: Lazy<GetCustomerSupportData>,
) : BaseViewModel<State, PartialState, ViewEvents>(initialState) {

    private val showAlertPublicSubject: PublishSubject<String> = PublishSubject.create()
    private var destinationType: String = ""

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(

            showAlertPublicSubject
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<PartialState> { PartialState.HideAlert }
                        .startWith(PartialState.ShowAlert(it))
                },

            intent<Intent.SetAdoptionMode>()
                .map {
                    getCurrentState().let { state ->
                        val adoptionValue = when (it.adoptionMode) {
                            CollectionDestinationType.UPI -> UPI
                            CollectionDestinationType.BANK -> BANK
                            CollectionDestinationType.I_DONT_KNOW -> NOT_AWARE
                            else -> UPI
                        }

                        paymentAnalyticsEvents.get().trackChoosePaymentOption(
                            accountId = state.accountId,
                            screen = PAYMENT_ADDRESS_DETAILS,
                            relation = state.getRelationFrmAccountType(),
                            flow = INTERNAL_SUPPLIER_COLLECTION,
                            value = adoptionValue,
                        )

                        if (state.supportType.value.isNotBlank()) {
                            val adoptionType = when (it.adoptionMode) {
                                CollectionDestinationType.UPI -> UPI.lowercase()
                                CollectionDestinationType.BANK -> BANK.lowercase()
                                CollectionDestinationType.I_DONT_KNOW -> I_DONT_KNOW.lowercase()
                                else -> UPI.lowercase()
                            }
                            paymentAnalyticsEvents.get().trackCustomerSupportOptMsgShown(
                                accountId = state.accountId,
                                supportType = state.supportType.value,
                                msg = state.supportMsg,
                                number = state.supportNumber,
                                type = adoptionType
                            )
                        }
                    }
                    PartialState.SetAdoptionMode(it.adoptionMode)
                },

            // set upi vpa
            setDestinationObservable(),
            shareRequestToWhatsapp(),
            intent<Intent.ClearUpiError>().map {
                PartialState.ShowInvalidUpiServerError(false)
            },
            getPaymentSupportType(),
            actionSupportClicked(),
            sendWhatsAppMessage()
        )
    }

    private fun setDestinationObservable(): Observable<PartialState> {
        return intent<Intent.SetPaymentVpa>()
            .switchMap {
                destinationType = it.paymentType
                UseCase.wrapCompletable(
                    setPaymentOutDestination.get().execute(
                        accountId,
                        accountType,
                        it.paymentType,
                        it.vpa
                    )
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.UpdateMerchantLoaderStatus(true)
                    is Result.Success -> {
                        paymentAnalyticsEvents.get().trackPaymentDetailsValidated(
                            accountId = accountId,
                            screen = PAYMENT_ADDRESS_DETAILS,
                            relation = initialState.getRelationFrmAccountType(),
                            flow = INTERNAL_SUPPLIER_COLLECTION,
                            type = destinationType
                        )
                        emitViewEvent(
                            ViewEvents.OnAccountAddedSuccessfully(
                                accountId,
                                CollectionDestinationType.UPI.value
                            )
                        )
                        PartialState.UpdateMerchantLoaderStatus(false)
                    }
                    is Result.Failure -> {
                        when {
                            it.error is CollectionServerErrors.InvalidAPaymentAddress -> {
                                paymentAnalyticsEvents.get().trackEnteredInvalidPaymentDetails(
                                    accountId = accountId,
                                    screen = PAYMENT_ADDRESS_DETAILS,
                                    relation = initialState.getRelationFrmAccountType(),
                                    flow = INTERNAL_SUPPLIER_COLLECTION,
                                    error = INVALID_PAYMENT_ADDRESS,
                                    type = destinationType
                                )
                                PartialState.ShowInvalidUpiServerError(true)
                            }
                            it.error is CollectionServerErrors.InvalidAccountNumber -> {
                                paymentAnalyticsEvents.get().trackEnteredInvalidPaymentDetails(
                                    accountId = accountId,
                                    screen = PAYMENT_ADDRESS_DETAILS,
                                    relation = initialState.getRelationFrmAccountType(),
                                    flow = INTERNAL_SUPPLIER_COLLECTION,
                                    error = INVALID_ACCOUNT_NUMBER,
                                    type = destinationType
                                )

                                PartialState.ShowInValidErrorStatus(
                                    true,
                                    AddPaymentDestinationContract.INVALID_ACCOUNT_NUMBER
                                )
                            }

                            it.error is CollectionServerErrors.InvalidIFSCcode -> {
                                paymentAnalyticsEvents.get().trackEnteredInvalidPaymentDetails(
                                    accountId = accountId,
                                    screen = PAYMENT_ADDRESS_DETAILS,
                                    relation = initialState.getRelationFrmAccountType(),
                                    flow = INTERNAL_SUPPLIER_COLLECTION,
                                    error = INVALID_IFSC,
                                    type = destinationType
                                )

                                PartialState.ShowInValidErrorStatus(
                                    true,
                                    AddPaymentDestinationContract.INVALID_IFSC_CODE
                                )
                            }
                            isInternetIssue(it.error) -> {
                                emitViewEvent(
                                    ViewEvents.ShowErrorMessage(
                                        context.get().getString(R.string.payment_no_internet_connection)
                                    )
                                )
                                PartialState.UpdateMerchantLoaderStatus(false)
                            }
                            else -> {
                                paymentAnalyticsEvents.get().trackEnteredInvalidPaymentDetails(
                                    accountId = accountId,
                                    screen = PAYMENT_ADDRESS_DETAILS,
                                    relation = initialState.getRelationFrmAccountType(),
                                    flow = INTERNAL_SUPPLIER_COLLECTION,
                                    error = it.error.message ?: context.get()
                                        .getString(R.string.payment_something_went_wrong),
                                    type = destinationType
                                )
                                emitViewEvent(
                                    ViewEvents.ShowErrorMessage(
                                        it.error.message ?: context.get()
                                            .getString(R.string.payment_something_went_wrong)
                                    )
                                )
                                PartialState.UpdateMerchantLoaderStatus(false)
                            }
                        }
                    }
                }
            }
    }

    private fun shareRequestToWhatsapp(): Observable<PartialState> {
        return intent<Intent.ShareRequestToWhatsApp>()
            .map {
                emitViewEvent(ViewEvents.ShareRequestToWhatsapp(it.sharingText))
                PartialState.NoChange
            }
    }

    private fun getPaymentSupportType(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap {
                wrap(getPaymentSupportType.get().execute())
            }
            .map {
                if (it is Result.Success) {
                    if (it.value.value.isNotBlank()) {
                        paymentAnalyticsEvents.get().trackCustomerSupportOptMsgShown(
                            accountId = accountId,
                            supportType = it.value.value,
                            msg = getCurrentState().supportMsg,
                            number = getCustomerSupportData.get().getCustomerSupportNumber(it.value),
                            type = UPI.lowercase()
                        )
                        PartialState.SetSupportData(
                            it.value,
                            getCustomerSupportData.get().getCustomerSupportNumber(it.value),
                            getCustomerSupportData.get().get24x7String()
                        )
                    } else {
                        PartialState.NoChange
                    }
                } else
                    PartialState.NoChange
            }
    }

    private fun actionSupportClicked(): Observable<PartialState> {
        return intent<Intent.SupportClicked>()
            .map {
                paymentAnalyticsEvents.get().trackCustomerSupportMsgClicked(
                    source = PaymentAnalyticsEvents.PaymentPropertyValue.CHOOSE_PAYMENT_OPTION,
                    type = getCurrentState().supportType.value,
                    relation = accountType.lowercase(),
                    supportMsg = it.msg
                )
                if (getCurrentState().supportType == SupportType.CALL)
                    emitViewEvent(ViewEvents.CallCustomerCare)
                else pushIntent(Intent.SendWhatsAppMessage(it.msg, it.number))
                PartialState.NoChange
            }
    }

    private fun sendWhatsAppMessage() = intent<Intent.SendWhatsAppMessage>()
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
                    emitViewEvent(ViewEvents.SendWhatsAppMessage(it.value))
                }
                is Result.Failure -> {
                    if (it.error is IntentHelper.NoWhatsAppError) {
                        emitViewEvent(ViewEvents.ShowWhatsAppError)
                    } else {
                        emitViewEvent(ViewEvents.ShowDefaultError)
                    }
                }
                else -> {
                }
            }

            PartialState.NoChange
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.ShowLoading -> currentState.copy(isLoading = true, errorMessage = "")
            is PartialState.SetNetworkError -> currentState.copy(
                isNetworkError = partialState.isNetworkError,
                upiLoaderStatus = false
            )
            is PartialState.NoChange -> currentState
            is PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is PartialState.UpdateMerchantLoaderStatus -> currentState.copy(
                upiLoaderStatus = partialState.upiLoaderStatus
            )
            is PartialState.SetAdoptionMode -> currentState.copy(
                adoptionMode = partialState.adoptionMode
            )
            is PartialState.ShowInValidErrorStatus -> currentState.copy(
                isLoading = false,
                invalidBankAccountError = partialState.invalidBankAccountError,
                invalidBankAccountCode = partialState.invalidBankAccountCode,
                upiLoaderStatus = false
            )
            is PartialState.ShowInvalidUpiServerError -> currentState.copy(
                upiErrorServer = partialState.upiErrorServer,
                upiLoaderStatus = false
            )
            is PartialState.SetErrorMessage -> currentState.copy(
                isLoading = false,
                upiLoaderStatus = false,
                errorMessage = partialState.errorMessage
            )
            is PartialState.SetSupportData -> currentState.copy(
                supportType = partialState.supportType,
                supportNumber = partialState.supportNumber,
                support24x7String = partialState.support24x7String,
            )
        }
    }
}
