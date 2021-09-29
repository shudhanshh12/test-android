package `in`.okcredit.collection_ui.ui.home.add

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.collection.contract.CollectionDestinationType
import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CollectionServerErrors
import `in`.okcredit.collection.contract.GetCollectionMerchantProfile
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.ui.home.add.AddMerchantDestinationContract.*
import `in`.okcredit.collection_ui.usecase.SetActiveDestination
import `in`.okcredit.collection_ui.usecase.ValidatePaymentAddress
import `in`.okcredit.collection_ui.utils.CollectionUtils
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.error.check
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.app_contract.AppConstants
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddMerchantDestinationViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam(AppConstants.ARG_PAYMENT_METHOD_TYPE) val paymentMethodType: String?,
    @ViewModelParam(AppConstants.ARG_IS_UPDATE_COLLECTION) val isUpdateCollection: Boolean,
    private val tracker: CollectionTracker,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val getCollectionMerchantProfile: Lazy<GetCollectionMerchantProfile>,
    private val validatePaymentAddress: Lazy<ValidatePaymentAddress>,
    private val setActiveDestination: Lazy<SetActiveDestination>,
    @ViewModelParam(AddMerchantDestinationDialog.ARG_ASYNC_REQUEST) private val asyncRequest: Boolean = false,
    @ViewModelParam(AddMerchantDestinationDialog.ARG_SOURCE) private val source: String = AddMerchantDestinationDialog.DEFAULT_SOURCE,
) : BaseViewModel<State, PartialState, ViewEvents>(initialState) {

    private val showAlertPublicSubject: PublishSubject<String> = PublishSubject.create()
    private val verifyPaymentAddressPublishSubject: PublishSubject<Triple<String, String, Boolean>> =
        PublishSubject.create()

    private lateinit var business: `in`.okcredit.merchant.contract.Business

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(

            showAlertPublicSubject
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<PartialState> { PartialState.HideAlert }
                        .startWith(PartialState.ShowAlert(it))
                },

            intent<Intent.Load>()
                .map {
                    val paymentType = if (paymentMethodType == CollectionDestinationType.BANK.value) {
                        CollectionTracker.CollectionPropertyValue.BANK
                    } else {
                        CollectionTracker.CollectionPropertyValue.UPI
                    }
                    tracker.trackStartedAdoptCollection(
                        type = paymentType,
                        source = source,
                        campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionTracker.CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
                        campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
                    )
                    if (!paymentMethodType.isNullOrBlank()) {
                        PartialState.SetAdoptionMode(paymentMethodType)
                    } else {
                        PartialState.NoChange
                    }
                },

            merchantObservable(),

            intent<Intent.SetAdoptionMode>()
                .map {
                    PartialState.SetAdoptionMode(it.adoptionMode)
                },

            // set upi vpa
            setUPIDestinationObservable(),

            // confirm bank account
            setBankDestinationObservable(),

            intent<Intent.ShowConfirmUI>()
                .map {
                    if (it.showConfirmUI) {
                        verifyPaymentAddressPublishSubject.onNext(
                            Triple(
                                it.paymentAddress,
                                it.paymentAddressType,
                                it.isUpdate
                            )
                        )
                        PartialState.NoChange
                    } else {
                        PartialState.ShowConfirmUI(false)
                    }
                },

            verifyPaymentAddressObservable(),

            intent<Intent.EnteredAccountNumber>()
                .map {
                    PartialState.EnteredAccountNumber(it.enteredAccountNumber)
                },

            intent<Intent.EnteredIfsc>()
                .map {
                    PartialState.EnteredIfsc(it.enteredIfsc, CollectionUtils.isValidIFSC(it.enteredIfsc))
                },

            intent<Intent.EnteredUPI>()
                .map {
                    PartialState.EnteredUPI(it.enteredUPI)
                },

            merchantCollectionObservable()
        )
    }

    private fun merchantObservable(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap { UseCase.wrapObservable(getActiveBusiness.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        this.business = it.value
                        PartialState.SetBusiness(it.value)
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(ViewEvents.GoToLogin)
                                PartialState.NoChange
                            }
                            isInternetIssue(it.error) -> PartialState.SetNetworkError(true)
                            else -> {
                                Timber.e(it.error, "ErrorState")
                                PartialState.ErrorState
                            }
                        }
                    }
                }
            }
    }

    private fun setBankDestinationObservable(): Observable<PartialState>? {
        return intent<Intent.ConfirmBankAccount>()
            .switchMap {
                setActiveDestination.get().execute(
                    CollectionMerchantProfile(
                        merchant_id = if (!it.merchantId.isNullOrBlank()) it.merchantId else business.id,
                        payment_address = it.paymentAddress,
                        type = CollectionDestinationType.BANK.value
                    ),
                    asyncRequest,
                    referralMerchant = getCurrentState().referredByMerchantId
                )
            }
            .switchMap {
                when (it) {
                    is Result.Progress -> Observable.just(PartialState.UpdateMerchantLoaderStatus(true))
                    is Result.Success -> {
                        if (isUpdateCollection) {
                            tracker.trackCollectionAdoptionUpdated(
                                type = PropertyValue.BANK,
                                screen = source,
                                campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionTracker.CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
                                campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
                            )
                        } else {
                            tracker.trackCollectionAdoptionCompleted(
                                type = PropertyValue.BANK,
                                screen = source,
                                campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionTracker.CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
                                campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
                            )
                        }
                        emitViewEvent(ViewEvents.OnAccountAddedSuccessfully)
                        Observable.just(PartialState.Success(it.value.eta ?: 0L))
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(ViewEvents.GoToLogin)
                                Observable.just(PartialState.NoChange)
                            }
                            isInternetIssue(it.error) -> {
                                showNetworkErrorObservable()
                            }
                            else -> {
                                onErrorObservable()
                            }
                        }
                    }
                }
            }
    }

    private fun showNetworkErrorObservable(): Observable<PartialState>? {
        return Observable.timer(2, TimeUnit.SECONDS)
            .map<PartialState> { PartialState.SetNetworkError(false) }
            .startWith(PartialState.SetNetworkError(true))
    }

    private fun onPaymentAddressValidationFail(): Observable<PartialState>? {
        return Observable.timer(2, TimeUnit.SECONDS)
            .map<PartialState> { PartialState.InvalidPaymentAddressError(false) }
            .startWith(PartialState.InvalidPaymentAddressError(true))
    }

    private fun onErrorObservable(): Observable<PartialState>? {
        return Observable.timer(2, TimeUnit.SECONDS)
            .map<PartialState> { PartialState.ServerAPIError(false) }
            .startWith(PartialState.ServerAPIError(true))
    }

    private fun setUPIDestinationObservable(): Observable<PartialState>? {
        return intent<Intent.SetUpiVpa>()
            .switchMap {
                setActiveDestination.get().execute(
                    CollectionMerchantProfile(
                        merchant_id = business.id,
                        payment_address = it.upiVpa,
                        type = CollectionDestinationType.UPI.value
                    ),
                    asyncRequest,
                    referralMerchant = getCurrentState().referredByMerchantId
                )
            }
            .switchMap {
                when (it) {
                    is Result.Progress -> Observable.just(PartialState.UpdateMerchantLoaderStatus(true))
                    is Result.Success -> {
                        if (isUpdateCollection) {
                            tracker.trackCollectionAdoptionUpdated(
                                type = PropertyValue.UPI,
                                screen = source,
                                campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionTracker.CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
                                campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
                            )
                        } else {
                            tracker.trackCollectionAdoptionCompleted(
                                type = PropertyValue.UPI,
                                screen = source,
                                campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionTracker.CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
                                campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
                            )
                        }
                        emitViewEvent(ViewEvents.OnAccountAddedSuccessfully)
                        Observable.just(PartialState.Success(it.value.eta ?: 0L))
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(ViewEvents.GoToLogin)
                                Observable.just(PartialState.UpdateMerchantLoaderStatus(false))
                            }
                            isInternetIssue(it.error) -> {
                                showNetworkErrorObservable()
                            }
                            else -> {
                                onErrorObservable()
                            }
                        }
                    }
                }
            }
    }

    private fun isInvalidPaymentAddress(throwable: Throwable): Boolean {
        return throwable.check<CollectionServerErrors.InvalidAPaymentAddress>() ||
            throwable.check<CollectionServerErrors.InvalidAccountNumber>() ||
            throwable.check<CollectionServerErrors.InvalidIFSCcode>()
    }

    private fun verifyPaymentAddressObservable(): Observable<PartialState>? {
        var isUpdate = false
        var paymentAddressType = ""
        return verifyPaymentAddressPublishSubject
            .switchMap {
                paymentAddressType = it.second
                isUpdate = it.third
                UseCase.wrapSingle(validatePaymentAddress.get().execute(it.first, it.second, it.third))
            }
            .switchMap {
                when (it) {
                    is Result.Progress -> Observable.just(PartialState.ShowVerifyLoader(true))
                    is Result.Success -> {
                        Observable.just(PartialState.SetPaymentAccountName(true, it.value.paymentAccountName))
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(ViewEvents.GoToLogin)
                                Observable.just(PartialState.ShowVerifyLoader(false))
                            }
                            isInternetIssue(it.error) -> {
                                showNetworkErrorObservable()
                            }
                            isInvalidPaymentAddress(it.error) -> {
                                tracker.trackEnteredInvalidCollectionDetails(
                                    isUpdate,
                                    paymentAddressType,
                                    campaign = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) CollectionTracker.CollectionPropertyValue.COLLECTION_TARGETED_REFERRAL else null,
                                    campaignSrc = if (getCurrentState().referredByMerchantId.isNotNullOrBlank()) getCurrentState().referredByMerchantId else null
                                )
                                onPaymentAddressValidationFail()
                            }
                            else -> {
                                onErrorObservable()
                            }
                        }
                    }
                }
            }
    }

    private fun merchantCollectionObservable(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap { UseCase.wrapObservable(getCollectionMerchantProfile.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        PartialState.SetCollectionMerchantProfile(it.value)
                    }
                    is Result.Failure -> {
                        when {
                            it.error is CollectionServerErrors.AddressNotFound -> {
                                PartialState.NoChange
                            }
                            isAuthenticationIssue(it.error) -> {
                                PartialState.NoChange
                            }
                            isInternetIssue(it.error) -> PartialState.SetNetworkError(
                                true
                            )
                            else -> PartialState.ErrorState
                        }
                    }
                }
            }
    }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.ErrorState -> currentState.copy(error = true)
            is PartialState.SetNetworkError -> currentState.copy(
                isNetworkError = partialState.isNetworkError,
                confirmLoaderStatus = false,
                showVerifyLoader = false
            )
            is PartialState.NoChange -> currentState
            is PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is PartialState.UpdateMerchantLoaderStatus -> currentState.copy(
                confirmLoaderStatus = partialState.confirmLoaderStatus
            )
            is PartialState.SetAdoptionMode -> currentState.copy(
                adoptionMode = partialState.adoptionMode
            )
            is PartialState.ServerAPIError -> currentState.copy(
                serverAPIError = partialState.serverAPIError,
                confirmLoaderStatus = false,
                showVerifyLoader = false
            )
            is PartialState.InvalidPaymentAddressError -> currentState.copy(
                invalidPaymentAddressError = partialState.invalidPaymentAddressError,
                confirmLoaderStatus = false,
                showVerifyLoader = false
            )
            is PartialState.SetCollectionMerchantProfile -> currentState.copy(
                collectionMerchantProfile = partialState.collectionMerchantProfile
            )
            is PartialState.SetBusiness -> currentState.copy(
                business = partialState.business
            )
            is PartialState.ShowConfirmUI -> currentState.copy(
                showConfirmUI = partialState.showConfirmUI,
                showVerifyLoader = false
            )
            is PartialState.ShowVerifyLoader -> currentState.copy(
                showVerifyLoader = partialState.showVerifyLoader
            )
            is PartialState.SetPaymentAccountName -> currentState.copy(
                showConfirmUI = partialState.showConfirmUI,
                showVerifyLoader = false,
                paymentAccountName = partialState.paymentAccountName
            )
            is PartialState.EnteredAccountNumber -> currentState.copy(enteredAccountNumber = partialState.enteredAccountNumber)
            is PartialState.EnteredIfsc -> currentState.copy(
                enteredIfsc = partialState.enteredIfsc,
                isValidIfsc = partialState.isValidIfsc
            )
            is PartialState.EnteredUPI -> currentState.copy(
                enteredUPI = partialState.enteredUPI
            )
            is PartialState.Success -> currentState.copy(success = true)
        }
    }
}
