package `in`.okcredit.frontend.ui.live_sales

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Screen
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.collection.contract.CollectionDestinationType
import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionServerErrors
import `in`.okcredit.collection.contract.GetCollectionMerchantProfile
import `in`.okcredit.collection.contract.GetCustomerCollectionProfile
import `in`.okcredit.collection.contract.IsUpiVpaValid
import `in`.okcredit.collection.contract.SetCollectionDestination
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.usecase.GetLiveSalesStatement
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.customer_ui.usecase.MarkCustomerAsSeen
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.exceptions.CompositeException
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LiveSalesViewModel @Inject constructor(
    initialState: LiveSalesContract.State,
    @ViewModelParam("customer_id") val customerId: String,
    private val getActiveBusiness: GetActiveBusiness,
    private val getCustomerCollectionProfile: GetCustomerCollectionProfile,
    private val getCustomer: GetCustomer,
    private val context: Context,
    private val markCustomerAsSeen: MarkCustomerAsSeen,
    private val getLiveSalesStatement: GetLiveSalesStatement,
    private val getPaymentReminderIntent: GetPaymentReminderIntent,
    private val getCollectionMerchantProfile: GetCollectionMerchantProfile,
    private val collectionRepository: CollectionRepository,
    private val tracker: Tracker,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    private val isValidUpi: IsUpiVpaValid,
    private val setCollectionDestination: SetCollectionDestination,
    private val navigator: LiveSalesContract.Navigator,
) : BasePresenter<LiveSalesContract.State, LiveSalesContract.PartialState>(initialState) {

    private val reload: PublishSubject<Unit> = PublishSubject.create()
    private val showAlertPublicSubject: PublishSubject<String> = PublishSubject.create()
    private val showQrCodePublishSubject: PublishSubject<Customer> = PublishSubject.create()
    private val showAlertPublishSubject: PublishSubject<String> = PublishSubject.create()
    private val setUpiDestinationPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    private val showErrorPublishSubject: PublishSubject<Unit> = PublishSubject.create()

    private var expandTransaction = false
    private lateinit var business: Business
    private var isAlertVisible = false
    private var merchantPaymentAddress: String? = null
    private var upiVpa = ""

    override fun handle(): Observable<UiState.Partial<LiveSalesContract.State>> {
        return mergeArray(
            // hide network error when network becomes available
            checkNetworkHealth.get()
                .execute(Unit)
                .filter { it is Result.Success }
                .map {
                    // network connected
                    reload.onNext(Unit)
                    LiveSalesContract.PartialState.SetNetworkError(false)
                },

            // handle `load` screen intent
            Observable
                .merge(intent<LiveSalesContract.Intent.Load>(), reload)
                .switchMap { getLiveSalesStatement.execute(customerId) }
                .map {
                    when (it) {
                        is Result.Progress -> {
                            LiveSalesContract.PartialState.NoChange
                        }
                        is Result.Success -> {
                            LiveSalesContract.PartialState.ShowData(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    LiveSalesContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> LiveSalesContract.PartialState.SetNetworkError(true)
                                else -> {
                                    Timber.e(it.error, "ErrorState")
                                    LiveSalesContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            // load customer
            intent<LiveSalesContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(getCustomer.execute(customerId)) }
                .map {
                    when (it) {
                        is Result.Progress -> LiveSalesContract.PartialState.NoChange
                        is Result.Success -> {
                            LiveSalesContract.PartialState.ShowCustomer(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    LiveSalesContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> LiveSalesContract.PartialState.SetNetworkError(true)
                                else -> {
                                    Timber.e(it.error, "ErrorState")
                                    LiveSalesContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            // load merchant for AB
            intent<LiveSalesContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(getActiveBusiness.execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> LiveSalesContract.PartialState.NoChange
                        is Result.Success -> {
                            this.business = it.value
                            LiveSalesContract.PartialState.SetBusiness(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    LiveSalesContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> LiveSalesContract.PartialState.SetNetworkError(true)
                                else -> {
                                    Timber.e(it.error, "ErrorState")
                                    LiveSalesContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            intent<LiveSalesContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(getCollectionMerchantProfile.execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> LiveSalesContract.PartialState.NoChange
                        is Result.Success -> {
                            this.merchantPaymentAddress = it.value.payment_address
                            LiveSalesContract.PartialState.SetMerchantPaymentAddress(it.value.payment_address)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    LiveSalesContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> LiveSalesContract.PartialState.SetNetworkError(
                                    true
                                )
                                else -> LiveSalesContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            intent<LiveSalesContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(collectionRepository.isCollectionActivated()) }
                .map {
                    when (it) {
                        is Result.Progress -> LiveSalesContract.PartialState.NoChange
                        is Result.Success -> {
                            LiveSalesContract.PartialState.SetCollectionActivated(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    LiveSalesContract.PartialState.NoChange
                                }
                                else -> {
                                    LiveSalesContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                },

            // handle `show alert` intent
            intent<LiveSalesContract.Intent.ShowAlert>()
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<LiveSalesContract.PartialState> { LiveSalesContract.PartialState.HideAlert }
                        .startWith(LiveSalesContract.PartialState.ShowAlert(it.message))
                },

            showAlertPublicSubject
                .switchMap {
                    Observable.timer(4, TimeUnit.SECONDS)
                        .map<LiveSalesContract.PartialState> { LiveSalesContract.PartialState.HideAlert }
                        .startWith(LiveSalesContract.PartialState.ShowAlert(it))
                },

            // updating last viewd time
            intent<LiveSalesContract.Intent.UpdateLastViewTime>()
                .switchMap {
                    UseCase.wrapObservable(markCustomerAsSeen.execute(customerId))
                }
                .map {
                    LiveSalesContract.PartialState.NoChange
                },

            // expand txs
            intent<LiveSalesContract.Intent.ExpandTransactions>()
                .map {
                    LiveSalesContract.PartialState.ExpandTransactions
                },

            // go to privacy screen
            intent<LiveSalesContract.Intent.GoToPrivacyScreen>()
                .map {
                    navigator.gotoCustomerPrivacyScreen()
                    LiveSalesContract.PartialState.NoChange
                },

            // set top txn
            intent<LiveSalesContract.Intent.SetScrollTopTransaction>()
                .map {
                    LiveSalesContract.PartialState.SetScrollTopTransaction(it.date)
                },

            // navigate to call customer
            intent<LiveSalesContract.Intent.ViewTransactionDetails>()
                .map {
                    navigator.gotoTransactionScreen(it.txnId, it.currentDue)
                    LiveSalesContract.PartialState.NoChange
                },

            intent<LiveSalesContract.Intent.ShowQrCodeDialog>()
                .switchMap { UseCase.wrapSingle(getCustomer.execute(customerId).firstOrError()) }
                .map {
                    when (it) {
                        is Result.Progress -> LiveSalesContract.PartialState.NoChange
                        is Result.Success -> {
                            isAlertVisible = true
                            showQrCodePublishSubject.onNext(it.value)
                            LiveSalesContract.PartialState.NoChange
                        }
                        is Result.Failure -> LiveSalesContract.PartialState.NoChange
                    }
                },

            intent<LiveSalesContract.Intent.HideQrCodeDialog>()
                .map {
                    isAlertVisible = false
                    LiveSalesContract.PartialState.NoChange
                },

            showQrCodePublishSubject
                .switchMap { customer ->
                    UseCase.wrapObservable(
                        getCustomerCollectionProfile.execute(customer.id).map {
                            Pair(customer, it)
                        }
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> LiveSalesContract.PartialState.NoChange
                        is Result.Success -> {

                            if (isAlertVisible) {
                                navigator.showQrCodePopup(
                                    it.value.first,
                                    it.value.second,
                                    business,
                                    merchantPaymentAddress
                                )
                            }
                            LiveSalesContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    LiveSalesContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> LiveSalesContract.PartialState.SetNetworkError(true)
                                else -> {
                                    Timber.e(it.error, "ErrorState")
                                    LiveSalesContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            /***********************   Send Whatsapp Payment Reminder  ***********************/
            intent<LiveSalesContract.Intent.SendWhatsAppReminder>()
                .switchMap { UseCase.wrapSingle(getPaymentReminderIntent.execute(it.customerId, "home", null)) }
                .map {
                    when (it) {
                        is Result.Progress -> LiveSalesContract.PartialState.NoChange
                        is Result.Success -> {
                            navigator.shareReminder(it.value)
                            LiveSalesContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                it.error is IntentHelper.NoWhatsAppError ||
                                    it.error is CompositeException && (it.error as CompositeException).exceptions.find { e -> e is IntentHelper.NoWhatsAppError } != null -> {
                                    showAlertPublishSubject.onNext(context.getString(R.string.whatsapp_not_installed))
                                    LiveSalesContract.PartialState.NoChange
                                }
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    LiveSalesContract.PartialState.NoChange
                                }
                                else -> {
                                    LiveSalesContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                },

            showAlertPublishSubject
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<LiveSalesContract.PartialState> { LiveSalesContract.PartialState.HideAlert }
                        .startWith(LiveSalesContract.PartialState.ShowAlert(it))
                },

            // showing error
            showErrorPublishSubject
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<LiveSalesContract.PartialState> { LiveSalesContract.PartialState.HideError }
                        .startWith(LiveSalesContract.PartialState.ShowError)
                },

            verifyUpiObservable(),

            // set upi vpa
            setUpiDestinationObservable(),

            // confirm bank account
            setBankDestinationObservable(),

            intent<LiveSalesContract.Intent.SetAdoptionMode>()
                .map {
                    LiveSalesContract.PartialState.SetAdoptionMode(it.adoptionMode)
                },

            intent<LiveSalesContract.Intent.SetCollectionPopupOpen>()
                .map {
                    LiveSalesContract.PartialState.SetCollectionPopupOpen(it.isCollectionPopupOpen)
                }
        )
    }

    private fun setBankDestinationObservable(): Observable<LiveSalesContract.PartialState>? {
        return intent<LiveSalesContract.Intent.ConfirmBankAccount>()
            .switchMap {
                UseCase.wrapObservable(
                    setCollectionDestination.execute(
                        CollectionMerchantProfile(
                            business.id,
                            payment_address = it.paymentAddress,
                            type = CollectionDestinationType.BANK.value
                        )
                    )
                )
            }
            .switchMap {
                when (it) {
                    is Result.Progress -> Observable.just(LiveSalesContract.PartialState.UpdateMerchantLoaderStatus(true))
                    is Result.Success -> {
                        tracker.trackEvents(
                            Event.COLLECTION_ADAPTION_COMPLETED,
                            type = PropertyValue.BANK,
                            source = Screen.LINK_PAY_SCREEN
                        )
                        showAlertPublicSubject.onNext(context.getString(R.string.bank_account_added_successfully))
                        Observable.just(LiveSalesContract.PartialState.SetCollectionActivated(true))
                    }
                    is Result.Failure -> {
                        when {
                            it.error is CollectionServerErrors.InvalidAccountNumber -> {
                                tracker.trackEvents(
                                    Event.INVALID_BANK_DETAILS,
                                    type = PropertyValue.INVALID_ACCOUNT_NUMBER,
                                    screen = Screen.LINK_PAY_SCREEN,
                                    relation = PropertyValue.MERCHANT
                                )
                                invalidAccountErrorObservable(LiveSalesContract.INVALID_ACCOUNT_NUMBER)
                            }

                            it.error is CollectionServerErrors.InvalidIFSCcode -> {
                                tracker.trackEvents(
                                    Event.INVALID_BANK_DETAILS,
                                    type = PropertyValue.INVALID_IFSC,
                                    screen = Screen.LINK_PAY_SCREEN,
                                    relation = PropertyValue.MERCHANT
                                )
                                invalidAccountErrorObservable(LiveSalesContract.INVALID_IFSC_CODE)
                            }

                            it.error is CollectionServerErrors.InvalidName -> {
                                tracker.trackInvalidBankDetails("invalid_name", false)
                                tracker.trackEvents(
                                    Event.INVALID_BANK_DETAILS,
                                    type = PropertyValue.INVALID_NAME,
                                    screen = Screen.LINK_PAY_SCREEN,
                                    relation = PropertyValue.MERCHANT
                                )
                                invalidAccountErrorObservable(LiveSalesContract.INVALID_NAME)
                            }

                            it.error is CollectionServerErrors.InvalidAccountOrIFSCcode -> {
                                tracker.trackInvalidBankDetails("invalid_account_number_or_ifsc", false)
                                tracker.trackEvents(
                                    Event.INVALID_BANK_DETAILS,
                                    type = PropertyValue.INVALID_ACCOUNT_OR_IFSC,
                                    screen = Screen.LINK_PAY_SCREEN,
                                    relation = PropertyValue.MERCHANT
                                )
                                invalidAccountErrorObservable(LiveSalesContract.INVALID_ACCOUNT_NUMBER_AND_IFSC_CODE)
                            }

                            isAuthenticationIssue(it.error) -> {
                                navigator.gotoLogin()
                                Observable.just(LiveSalesContract.PartialState.NoChange)
                            }
                            isInternetIssue(it.error) -> {
                                Observable.timer(2, TimeUnit.SECONDS)
                                    .map<LiveSalesContract.PartialState> {
                                        LiveSalesContract.PartialState.SetNetworkError(
                                            false
                                        )
                                    }
                                    .startWith(
                                        LiveSalesContract.PartialState.SetNetworkError(
                                            true
                                        )
                                    )
                            }
                            else -> {
                                tracker.trackDebug("LinkPay Add Bank error: ${it.error.message}")
                                showErrorPublishSubject.onNext(Unit)
                                Observable.just(LiveSalesContract.PartialState.NoChange)
                            }
                        }
                    }
                }
            }
    }

    private fun invalidAccountErrorObservable(invalidErrorType: Int): Observable<LiveSalesContract.PartialState>? {
        return Observable.timer(2, TimeUnit.SECONDS)
            .map<LiveSalesContract.PartialState> {
                LiveSalesContract.PartialState.ShowInValidErrorStatus(
                    false,
                    invalidErrorType
                )
            }
            .startWith(
                LiveSalesContract.PartialState.ShowInValidErrorStatus(
                    true,
                    invalidErrorType
                )
            )
    }

    private fun setUpiDestinationObservable(): Observable<LiveSalesContract.PartialState>? {
        return setUpiDestinationPublishSubject
            .switchMap {
                UseCase.wrapObservable(
                    setCollectionDestination.execute(
                        CollectionMerchantProfile(
                            business.id,
                            payment_address = upiVpa,
                            type = "upi"
                        )
                    )
                )
            }
            .switchMap {
                when (it) {
                    is Result.Progress -> Observable.just(
                        LiveSalesContract.PartialState.UpdateMerchantLoaderStatus(
                            true
                        )
                    )
                    is Result.Success -> {
                        tracker.trackEvents(
                            Event.COLLECTION_ADAPTION_COMPLETED,
                            type = PropertyValue.UPI,
                            source = Screen.LINK_PAY_SCREEN
                        )
                        showAlertPublicSubject.onNext(context.getString(R.string.upi_added_successfully))
                        Observable.just(LiveSalesContract.PartialState.SetCollectionActivated(true))
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                navigator.gotoLogin()
                                Observable.just(LiveSalesContract.PartialState.UpdateMerchantLoaderStatus(false))
                            }
                            it.error is CollectionServerErrors.InvalidAPaymentAddress -> {
                                tracker.trackEvents(
                                    Event.ENTERED_INVALID_COLLECTION_DETAILS,
                                    type = PropertyValue.INVALID_PAYMENT_ADDRESS,
                                    screen = Screen.LINK_PAY_SCREEN,
                                    relation = PropertyValue.MERCHANT
                                )
                                Observable.timer(2, TimeUnit.SECONDS)
                                    .map<LiveSalesContract.PartialState> {
                                        LiveSalesContract.PartialState.ShowInvalidUpiServerError(
                                            false
                                        )
                                    }
                                    .startWith(LiveSalesContract.PartialState.ShowInvalidUpiServerError(true))
                            }
                            isInternetIssue(it.error) -> {
                                Observable.timer(2, TimeUnit.SECONDS)
                                    .map<LiveSalesContract.PartialState> {
                                        LiveSalesContract.PartialState.SetNetworkError(
                                            false
                                        )
                                    }
                                    .startWith(
                                        LiveSalesContract.PartialState.SetNetworkError(
                                            true
                                        )
                                    )
                            }
                            else -> {
                                tracker.trackDebug("LinkPay Add UPI error: ${it.error.message}")
                                showErrorPublishSubject.onNext(Unit)
                                Observable.just(LiveSalesContract.PartialState.NoChange)
                            }
                        }
                    }
                }
            }
    }

    private fun verifyUpiObservable(): Observable<LiveSalesContract.PartialState>? {
        return intent<LiveSalesContract.Intent.SetUpiVpa>()
            .switchMap {
                upiVpa = it.upiVpa
                UseCase.wrapSingle(isValidUpi.execute(it.upiVpa))
            }
            .switchMap {
                when (it) {
                    is Result.Progress -> Observable.just(
                        LiveSalesContract.PartialState.UpdateMerchantLoaderStatus(
                            true
                        )
                    )
                    is Result.Success -> {
                        if (it.value.first) {
                            setUpiDestinationPublishSubject.onNext(Unit)
                            Observable.just(LiveSalesContract.PartialState.NoChange)
                        } else {
                            Observable.timer(2, TimeUnit.SECONDS)
                                .map<LiveSalesContract.PartialState> {
                                    LiveSalesContract.PartialState.ShowInvalidUpiServerError(
                                        false
                                    )
                                }
                                .startWith(LiveSalesContract.PartialState.ShowInvalidUpiServerError(true))
                        }
                    }
                    is Result.Failure -> {
                        when {
                            it.error is CollectionServerErrors.InvalidAPaymentAddress -> {
                                tracker.trackEvents(
                                    Event.ENTERED_INVALID_COLLECTION_DETAILS,
                                    type = PropertyValue.INVALID_PAYMENT_ADDRESS,
                                    screen = Screen.LINK_PAY_SCREEN,
                                    relation = PropertyValue.MERCHANT
                                )
                                Observable.timer(2, TimeUnit.SECONDS)
                                    .map<LiveSalesContract.PartialState> {
                                        LiveSalesContract.PartialState.ShowInvalidUpiServerError(
                                            false
                                        )
                                    }
                                    .startWith(LiveSalesContract.PartialState.ShowInvalidUpiServerError(true))
                            }
                            isAuthenticationIssue(it.error) -> {
                                navigator.gotoLogin()
                                Observable.just(LiveSalesContract.PartialState.UpdateMerchantLoaderStatus(false))
                            }
                            isInternetIssue(it.error) -> {
                                Observable.timer(2, TimeUnit.SECONDS)
                                    .map<LiveSalesContract.PartialState> {
                                        LiveSalesContract.PartialState.SetNetworkError(
                                            false
                                        )
                                    }
                                    .startWith(
                                        LiveSalesContract.PartialState.SetNetworkError(
                                            true
                                        )
                                    )
                            }
                            else -> {
                                tracker.trackDebug("LinkPay Verify UPI error: ${it.error.message}")
                                showErrorPublishSubject.onNext(Unit)
                                Observable.just(LiveSalesContract.PartialState.NoChange)
                            }
                        }
                    }
                }
            }
    }

    override fun reduce(
        currentState: LiveSalesContract.State,
        partialState: LiveSalesContract.PartialState
    ): LiveSalesContract.State {
        return when (partialState) {
            is LiveSalesContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is LiveSalesContract.PartialState.ShowCustomer -> currentState.copy(customer = partialState.customer)
            is LiveSalesContract.PartialState.SetBusiness -> currentState.copy(business = partialState.business)
            is LiveSalesContract.PartialState.ShowData -> currentState.copy(
                isLoading = false,
                transactions = partialState.transactions,
                isTxnExpanded = expandTransaction
            )
            is LiveSalesContract.PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is LiveSalesContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                upiLoaderStatus = false
            )
            is LiveSalesContract.PartialState.ExpandTransactions -> currentState.copy(isTxnExpanded = true)
            is LiveSalesContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is LiveSalesContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is LiveSalesContract.PartialState.SetScrollTopTransaction -> currentState.copy(
                scrollTopTransactionDate = partialState.date
            )
            is LiveSalesContract.PartialState.SetMerchantPaymentAddress -> currentState.copy(
                merchantPaymentAddress = partialState.merchantPaymentAddress
            )
            is LiveSalesContract.PartialState.SetCollectionActivated -> currentState.copy(
                isCollectionActivated = partialState.isCollectionActivated,
                upiLoaderStatus = false,
                isCollectionPopupOpen = false
            )
            is LiveSalesContract.PartialState.ShowInvalidUpiServerError -> currentState.copy(
                upiErrorServer = partialState.upiErrorServer,
                upiLoaderStatus = false
            )
            is LiveSalesContract.PartialState.UpdateMerchantLoaderStatus -> currentState.copy(upiLoaderStatus = partialState.upiLoaderStatus)
            is LiveSalesContract.PartialState.ShowError -> currentState.copy(
                error = true,
                isLoading = false,
                upiLoaderStatus = false
            )
            is LiveSalesContract.PartialState.HideError -> currentState.copy(error = false)
            is LiveSalesContract.PartialState.ShowInValidErrorStatus -> currentState.copy(
                isLoading = false,
                invalidBankAccountError = partialState.invalidBankAccountError,
                invalidBankAccountCode = partialState.invalidBankAccountCode,
                upiLoaderStatus = false
            )
            is LiveSalesContract.PartialState.SetAdoptionMode -> currentState.copy(adoptionMode = partialState.adoptionMode)
            is LiveSalesContract.PartialState.SetCollectionPopupOpen -> currentState.copy(isCollectionPopupOpen = partialState.isCollectionPopupOpen)
            is LiveSalesContract.PartialState.NoChange -> currentState
        }
    }
}
