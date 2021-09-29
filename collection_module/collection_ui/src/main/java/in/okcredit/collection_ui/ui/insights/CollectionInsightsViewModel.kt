package `in`.okcredit.collection_ui.ui.insights

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection.contract.CreditGraphicalDataProvider.GraphDuration
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.usecase.GetAllDueCustomersByLastPayment
import `in`.okcredit.collection_ui.usecase.GetCreditGraphicalData
import `in`.okcredit.collection_ui.usecase.ScheduleSyncCollections
import `in`.okcredit.collection_ui.usecase.SetCollectionDestinationImpl
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.referral.contract.usecase.GetReferralLink
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.exceptions.CompositeException
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.communication.handlers.IntentHelper
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CollectionInsightsViewModel @Inject constructor(
    initialState: CollectionInsightsContract.State,
    private val getCollectionMerchantProfile: GetCollectionMerchantProfile,
    private val getActiveBusiness: GetActiveBusiness,
    private val navigator: CollectionInsightsContract.Navigator,
    private val checkNetworkHealth: CheckNetworkHealth,
    private val tracker: Tracker,
    private val setCollectionDestination: SetCollectionDestinationImpl,
    private val getCreditGraphicalData: GetCreditGraphicalData,
    private val getAllDueCustomersByLastPayment: GetAllDueCustomersByLastPayment,
    private val getPaymentReminderIntent: GetPaymentReminderIntent,
    private val context: Context,
    private val getCustomerCollectionProfile: GetCustomerCollectionProfile,
    private val getReferralLink: GetReferralLink,
    private val authService: AuthService,
    private val getKycStatus: Lazy<GetKycStatus>,
    private val getKycRiskCategory: Lazy<GetKycRiskCategory>,
    private val scheduleSyncCollections: Lazy<ScheduleSyncCollections>,
) : BaseViewModel<CollectionInsightsContract.State, CollectionInsightsContract.PartialState, CollectionInsightsContract.ViewEvent>(
    initialState
) {

    private val reload: PublishSubject<Unit> = PublishSubject.create()
    private val showAlertPublishSubject: PublishSubject<String> = PublishSubject.create()
    private val showPaymentReminderPublishSubject: PublishSubject<Customer> = PublishSubject.create()

    private lateinit var business: `in`.okcredit.merchant.contract.Business
    private var viewCollectionInsightsEventFired = false
    private var isAlertVisible = false

    override fun handle(): Observable<UiState.Partial<CollectionInsightsContract.State>> {
        return mergeArray(

            // hide network error when network becomes available
            intent<CollectionInsightsContract.Intent.Load>()
                .switchMap { checkNetworkHealth.execute(Unit) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        reload.onNext(Unit)
                    }
                    CollectionInsightsContract.PartialState.NoChange
                },

            intent<CollectionInsightsContract.Intent.Load>()
                .switchMap { wrap(getCreditGraphicalData.execute(GraphDuration.WEEK)) }
                .map {
                    when (it) {
                        is Result.Progress -> CollectionInsightsContract.PartialState.NoChange
                        is Result.Success -> {
                            CollectionInsightsContract.PartialState.SetBarDataSet(it.value)
                        }
                        is Result.Failure -> CollectionInsightsContract.PartialState.NoChange
                    }
                },

            intent<CollectionInsightsContract.Intent.Load>()
                .switchMap { wrap(getActiveBusiness.execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> CollectionInsightsContract.PartialState.NoChange
                        is Result.Success -> {
                            business = it.value
                            CollectionInsightsContract.PartialState.SetMerchantId(it.value.id)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    CollectionInsightsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    emitViewEvent(CollectionInsightsContract.ViewEvent.Error(true))
                                }
                                else -> emitViewEvent(CollectionInsightsContract.ViewEvent.Error(false))
                            }
                            CollectionInsightsContract.PartialState.NoChange
                        }
                    }
                },

            // load merchant for AB
            intent<CollectionInsightsContract.Intent.Load>()
                .switchMap { wrap(getCollectionMerchantProfile.execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> CollectionInsightsContract.PartialState.NoChange
                        is Result.Success -> {
                            if (viewCollectionInsightsEventFired.not()) {
                                tracker.trackEvents(
                                    Event.VIEW_COLLECTION_PROFILE,
                                    type = it.value.type,
                                    screen = CollectionTracker.CollectionScreen.INSIGHTS_RELATIONSHIP,
                                    propertiesMap = PropertiesMap.create()
                                        .add(PropertyKey.AB_VARIANT, PropertyValue.MERCHANT_DESTINATION_INSIGHT_V2)
                                )
                                viewCollectionInsightsEventFired = true
                            }

                            CollectionInsightsContract.PartialState.SetCollectionMerchantProfile(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                it.error is CollectionServerErrors.AddressNotFound -> {
                                    CollectionInsightsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    emitViewEvent(CollectionInsightsContract.ViewEvent.Error(true))
                                }
                                else -> emitViewEvent(CollectionInsightsContract.ViewEvent.Error(false))
                            }
                            CollectionInsightsContract.PartialState.NoChange
                        }
                    }
                },

            // Syncing Collection in Merchant Destination screen on initial Load
            intent<CollectionInsightsContract.Intent.Load>()
                .switchMap {
                    wrap(scheduleSyncCollections.get().execute(CollectionSyncer.Source.MERCHANT_PROFILE))
                }.map {
                    CollectionInsightsContract.PartialState.NoChange
                },

            intent<CollectionInsightsContract.Intent.Load>()
                .switchMap {
                    wrap(getAllDueCustomersByLastPayment.execute())
                }
                .map {
                    when (it) {
                        is Result.Progress -> CollectionInsightsContract.PartialState.NoChange
                        is Result.Success -> {
                            CollectionInsightsContract.PartialState.SetDueCustomerList(it.value)
                        }

                        is Result.Failure -> CollectionInsightsContract.PartialState.NoChange
                    }
                },

            intent<CollectionInsightsContract.Intent.Load>()
                .switchMap { wrap(getReferralLink.execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> CollectionInsightsContract.PartialState.NoChange
                        is Result.Success -> {
                            CollectionInsightsContract.PartialState.SetReferralLink(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    CollectionInsightsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> CollectionInsightsContract.PartialState.NoChange
                                else -> CollectionInsightsContract.PartialState.NoChange
                            }
                        }
                    }
                },

            intent<CollectionInsightsContract.Intent.Load>()
                .map {
                    if (authService.isAuthenticated()) {
                        CollectionInsightsContract.PartialState.SetIsAuthenticated(true)
                    } else {
                        CollectionInsightsContract.PartialState.NoChange
                    }
                },

            intent<CollectionInsightsContract.Intent.SelectGraphDuration>()
                .switchMap { wrap(getCreditGraphicalData.execute(it.graphDuration)) }
                .map {
                    when (it) {
                        is Result.Progress -> CollectionInsightsContract.PartialState.NoChange
                        is Result.Success -> {
                            CollectionInsightsContract.PartialState.SetBarDataSet(it.value)
                        }
                        is Result.Failure -> CollectionInsightsContract.PartialState.NoChange
                    }
                },

            intent<CollectionInsightsContract.Intent.Load>()
                .switchMap { wrap(getActiveBusiness.execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> CollectionInsightsContract.PartialState.NoChange
                        is Result.Success -> {
                            CollectionInsightsContract.PartialState.SetBusiness(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    CollectionInsightsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    emitViewEvent(CollectionInsightsContract.ViewEvent.Error(true))
                                }
                                else -> emitViewEvent(CollectionInsightsContract.ViewEvent.Error(false))
                            }
                            CollectionInsightsContract.PartialState.NoChange
                        }
                    }
                },

            // Delete upi with token
            intent<CollectionInsightsContract.Intent.DeleteMerchantDestination>()
                .switchMap {
                    wrap(
                        setCollectionDestination.execute(
                            CollectionMerchantProfile(
                                business.id
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> CollectionInsightsContract.PartialState.NoChange
                        is Result.Success -> {
                            navigator.gotoCollectionTutorialScreenByClearingStack()
                            tracker.trackEvents(
                                CollectionTracker.CollectionEvent.COLLECTION_DELETED,
                                type = it.value.type,
                                screen = CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN
                            )
                            CollectionInsightsContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    CollectionInsightsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    emitViewEvent(CollectionInsightsContract.ViewEvent.Error(true))
                                }
                                else -> emitViewEvent(CollectionInsightsContract.ViewEvent.Error(false))
                            }
                            CollectionInsightsContract.PartialState.NoChange
                        }
                    }
                },

            intent<CollectionInsightsContract.Intent.SendReminders>()
                .switchMap {
                    wrap(
                        getPaymentReminderIntent.execute(
                            it.customerId,
                            CollectionTracker.CollectionScreen.INSIGHTS_RELATIONSHIP,
                            null,
                            it.reminderStringsObject
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> CollectionInsightsContract.PartialState.NoChange
                        is Result.Success -> {
                            navigator.openPaymentReminderIntent(it.value)
                            CollectionInsightsContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                it.error is IntentHelper.NoWhatsAppError ||
                                    it.error is CompositeException && (it.error as CompositeException).exceptions.find { e -> e is IntentHelper.NoWhatsAppError } != null -> {
                                    showAlertPublishSubject.onNext(context.resources.getString(R.string.whatsapp_not_installed))
                                    CollectionInsightsContract.PartialState.NoChange
                                }
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    CollectionInsightsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    showAlertPublishSubject.onNext(context.resources.getString(R.string.no_internet_msg))
                                    CollectionInsightsContract.PartialState.NoChange
                                }
                                else -> {
                                    showAlertPublishSubject.onNext(context.getString(R.string.err_default))
                                    Timber.e(it.error, "CustomerScreenPresenter SharePayment Link")
                                    tracker.trackDebug("CustomerScreenPresenter SharePaymentLink ${it.error}")
                                    CollectionInsightsContract.PartialState.NoChange
                                }
                            }
                        }
                    }
                },

            showAlertPublishSubject
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<CollectionInsightsContract.PartialState> { CollectionInsightsContract.PartialState.HideAlert }
                        .startWith(CollectionInsightsContract.PartialState.ShowAlert(it))
                },

            intent<CollectionInsightsContract.Intent.ShowAlert>()
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<CollectionInsightsContract.PartialState> { CollectionInsightsContract.PartialState.HideAlert }
                        .startWith(CollectionInsightsContract.PartialState.ShowAlert(it.message))
                },

            intent<CollectionInsightsContract.Intent.ShowPaymentReminderDialog>()
                .map {
                    isAlertVisible = true
                    showPaymentReminderPublishSubject.onNext(it.customer)
                    CollectionInsightsContract.PartialState.NoChange
                },

            intent<CollectionInsightsContract.Intent.HidePaymentReminderDialog>()
                .map {
                    isAlertVisible = false
                    CollectionInsightsContract.PartialState.NoChange
                },

            showPaymentReminderPublishSubject
                .switchMap { customer ->
                    wrap(
                        getCustomerCollectionProfile.execute(customer.id)
                            .map {
                                Pair(it, customer)
                            }
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> CollectionInsightsContract.PartialState.NoChange
                        is Result.Success -> {
                            if (isAlertVisible) {
                                getCurrentState().collectionMerchantProfile?.let { profile ->
                                    if (profile.payment_address.isNullOrEmpty()) {
                                        navigator.openPaymentReminderDialog(
                                            it.value.first.copy(qr_intent = null),
                                            it.value.second
                                        )
                                    } else {
                                        navigator.openPaymentReminderDialog(
                                            it.value.first,
                                            it.value.second
                                        )
                                    }
                                }
                            }
                            CollectionInsightsContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    CollectionInsightsContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    emitViewEvent(CollectionInsightsContract.ViewEvent.Error(true))
                                }
                                else -> emitViewEvent(CollectionInsightsContract.ViewEvent.Error(false))
                            }
                            CollectionInsightsContract.PartialState.NoChange
                        }
                    }
                },
            getKycRiskDetails(),
        )
    }

    private fun getKycDetails() = Observable.zip(
        getKycStatus.get().execute(),
        getKycRiskCategory.get().execute(),
        { kycStatus, kycRisk ->
            Pair(kycStatus, kycRisk)
        }
    )

    private fun getKycRiskDetails() = intent<CollectionInsightsContract.Intent.LoadKycDetails>()
        .switchMap { getKycDetails() }
        .filter { it.second.kycRiskCategory != KycRiskCategory.NO_RISK }
        .map {
            CollectionInsightsContract.PartialState.SetKycDetails(
                it.first,
                it.second.kycRiskCategory,
                it.second.isLimitReached
            )
        }

    override fun reduce(
        currentState: CollectionInsightsContract.State,
        partialState: CollectionInsightsContract.PartialState,
    ): CollectionInsightsContract.State {
        return when (partialState) {
            is CollectionInsightsContract.PartialState.SetCollectionMerchantProfile -> currentState.copy(
                collectionMerchantProfile = partialState.collectionMerchantProfile
            )
            is CollectionInsightsContract.PartialState.SetBusiness -> currentState.copy(
                business = partialState.business
            )
            is CollectionInsightsContract.PartialState.SetTransactionsInsights -> currentState.copy(
                onlineTxnAmount = partialState.onlineTxnAmount,
                acceptedTxnAmount = partialState.acceptedTxnAmount,
                givenCreditAmount = partialState.givenCreditAmount
            )
            is CollectionInsightsContract.PartialState.SetBarDataSet -> currentState.copy(graphResponse = partialState.graphResponse)
            is CollectionInsightsContract.PartialState.SetDueCustomerList -> currentState.copy(dueCustomers = partialState.dueCustomers)
            is CollectionInsightsContract.PartialState.SetReferralLink -> currentState.copy(referralLink = partialState.referralLink)
            is CollectionInsightsContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is CollectionInsightsContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is CollectionInsightsContract.PartialState.NoChange -> currentState
            is CollectionInsightsContract.PartialState.SetIsAuthenticated -> currentState.copy(isAuthenticated = partialState.isAuthenticated)
            is CollectionInsightsContract.PartialState.SetTransactionDetails -> currentState.copy(
                totalQRTransactionsBalance = partialState.totalQRTransactionsBalance
            )
            is CollectionInsightsContract.PartialState.SetMerchantId -> currentState.copy(merchantId = partialState.merchantId)
            is CollectionInsightsContract.PartialState.SetKycDetails -> currentState.copy(
                kycStatus = partialState.KycStatus,
                kycRiskCategory = partialState.kycRiskCategory,
                isLimitReached = partialState.isKycLimitReached
            )
        }
    }
}
