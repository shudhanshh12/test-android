package `in`.okcredit.merchant.customer_ui.ui.customer

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.PropertyValue.CUSTOMER
import `in`.okcredit.analytics.PropertyValue.CUSTOMER_SCREEN
import `in`.okcredit.analytics.PropertyValue.FIRST
import `in`.okcredit.analytics.PropertyValue.NOT_AVAILABLE
import `in`.okcredit.analytics.PropertyValue.ONLINE_PAYMENT
import `in`.okcredit.analytics.PropertyValue.REGULAR
import `in`.okcredit.analytics.PropertyValue.RELATIONSHIP
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.model.DueInfo
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.backend._offline.usecase.GetTransaction
import `in`.okcredit.backend._offline.usecase.ReactivateCustomer
import `in`.okcredit.backend._offline.usecase.UpdateCustomer
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.Features
import `in`.okcredit.backend.contract.Features.CUSTOMER_JUSPAY_FEATURE
import `in`.okcredit.backend.contract.Features.FEATURE_CUSTOMER_SUPPLIER_SCREEN_TXN_SORT_SETTING
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.backend.contract.RxSharedPrefValues.IS_PAY_ONLINE_EDUCATION_SHOWN
import `in`.okcredit.backend.server.GetRiskDetails
import `in`.okcredit.backend.server.riskInternal.FutureLimit
import `in`.okcredit.backend.server.riskInternal.PaymentInstruments
import `in`.okcredit.cashback.contract.usecase.GetCashbackMessageDetails
import `in`.okcredit.cashback.contract.usecase.IsCustomerCashbackFeatureEnabled
import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection.contract.CollectionStatus.PAYOUT_FAILED
import `in`.okcredit.collection.contract.CollectionStatus.PAYOUT_INITIATED
import `in`.okcredit.merchant.contract.BusinessScopedPreferenceWithActiveBusinessId
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import `in`.okcredit.merchant.customer_ui.onboarding.usecase.CustomerImmutableHelper
import `in`.okcredit.merchant.customer_ui.onboarding.usecase.GetEligibilityOnboardingNudge
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerContract.*
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerContract.Companion.FEATURE_CUSTOMER_SCREEN_VOICE_TRANSACTION
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase.IsRoboflowFeatureEnabled
import `in`.okcredit.merchant.customer_ui.ui.subscription.usecase.SubscriptionFeatureEnabled
import `in`.okcredit.merchant.customer_ui.usecase.*
import `in`.okcredit.merchant.customer_ui.usecase.GetCustomerScreenSortSelection.CustomerScreenSortSelection
import `in`.okcredit.merchant.customer_ui.usecase.GetCustomerScreenSortSelection.CustomerScreenSortSelection.BILL_DATE
import `in`.okcredit.merchant.customer_ui.usecase.GetCustomerScreenSortSelection.CustomerScreenSortSelection.CREATE_DATE
import `in`.okcredit.merchant.customer_ui.usecase.SyncCustomerTransactionOrCollection.SyncData.*
import `in`.okcredit.merchant.customer_ui.usecase.pre_network_onboarding.CanShowPreNetworkOnboardingBanner
import `in`.okcredit.referral.contract.RewardsOnSignupTracker
import `in`.okcredit.referral.contract.usecase.CloseReferralTargetBanner
import `in`.okcredit.referral.contract.usecase.GetReferralLink
import `in`.okcredit.referral.contract.usecase.GetReferralTarget
import `in`.okcredit.referral.contract.usecase.TransactionInitiated
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.referral_views.model.Place
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.shared.utils.AbFeatures
import `in`.okcredit.shared.utils.ScreenName
import android.content.Context
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.exceptions.CompositeException
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.analytics.AccountingEventTracker.PropertyValue.BALANCE_WIDGET
import merchant.okcredit.accounting.contract.model.LedgerType
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportType
import merchant.okcredit.accounting.model.Transaction
import merchant.okcredit.accounting.usecases.GetCustomerSupportPreference
import merchant.okcredit.accounting.utils.AccountingSharedUtils
import merchant.okcredit.accounting.utils.AccountingSharedUtils.TxnGravity
import merchant.okcredit.accounting.utils.AccountingSharedUtils.findFormattedDateOrTime
import merchant.okcredit.accounting.utils.AccountingSharedUtils.findUiTxnGravity
import merchant.okcredit.supplier.contract.PutNotificationReminder
import tech.okcredit.account_chat_contract.FEATURE.FEATURE_ACCOUNT_CHATS
import tech.okcredit.account_chat_contract.IGetChatUnreadMessageCount
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.extensions.isConnectedToInternet
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_IS_COLLECTION_DATE_SHOWN
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_SHOULD_SHOW_AUTO_DUE_DATE
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.Optional
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.bills.IGetAccountsTotalBills
import tech.okcredit.feature_help.contract.GetContextualHelpIds
import tech.okcredit.feature_help.contract.GetSupportNumber
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CustomerScreenViewModel @Inject constructor(
    private val initialState: State,
    @ViewModelParam("customer_id") var customerId: String,
    @ViewModelParam(CustomerContract.REACTIVATE) val reactivate: Boolean,
    @ViewModelParam(CustomerContract.ARG_TXN_ID) val txnId: String, // This is need to auto scroll recyclerview to this transaction id
    @ViewModelParam(CustomerContract.NAME) val customerName: String,
    @ViewModelParam(CustomerContract.ARG_SOURCE) val sourceScreen: String?,
    @ViewModelParam(CustomerContract.ARG_COLLECTION_ID) val collectionId: String?,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val getCustomerCollectionProfile: Lazy<GetCustomerCollectionProfile>,
    private val getCustomer: Lazy<GetCustomer>,
    private val getPaymentReminderIntent: Lazy<GetPaymentReminderIntent>,
    private val context: Lazy<Context>,
    private val defaultPreferences: Lazy<DefaultPreferences>,
    private val businessScopedPreferenceWithActiveBusinessId: Lazy<BusinessScopedPreferenceWithActiveBusinessId>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val markCollectionShared: Lazy<MarkCollectionShared>,
    private val markCustomerAsSeen: Lazy<MarkCustomerAsSeen>,
    private val reactivateCustomer: Lazy<ReactivateCustomer>,
    private val isSupplierCreditEnabledCustomer: Lazy<IsSupplierCreditEnabledCustomer>,
    private val getReferralLink: Lazy<GetReferralLink>,
    private val ab: Lazy<AbRepository>,
    private val getCustomerStatement: Lazy<GetCustomerStatement>,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    private val tracker: Lazy<Tracker>,
    private val updateDueInfo: Lazy<UpdateDueInfo>,
    private val submitVoiceInput: Lazy<PostVoiceInput>,
    private val getCustomerDueInfo: Lazy<GetCustomerDueInfo>,
    private val updateCustomer: Lazy<UpdateCustomer>,
    private val getCanShowCollectionDate: Lazy<GetCanShowCollectionDate>,
    private val getChatUnreadMessages: Lazy<IGetChatUnreadMessageCount>,
    private val checkOnlineEducationToShow: Lazy<CheckOnlineEducationToShow>,
    private val getCanShowBuyerTxnAlert: Lazy<GetCanShowBuyerTxnAlert>,
    private val getCanShowChatNewStickerLazy: Lazy<GetCanShowChatNewSticker>,
    private val getLastTransactionAddedByCustomer: Lazy<GetLastTransactionAddedByCustomer>,
    private val getSupplierCollectionProfileWithSync: Lazy<GetSupplierCollectionProfileWithSync>,
    private val nullifyDueDate: Lazy<NullifyDueDate>,
    private val getReferralTarget: Lazy<GetReferralTarget>,
    private val closeReferralTargetBanner: Lazy<CloseReferralTargetBanner>,
    private val transactionInitiated: Lazy<TransactionInitiated>,
    private val canShowCreditPaymentLayout: Lazy<CanShowCreditPaymentLayout>,
    private val getAccountsTotalBills: Lazy<IGetAccountsTotalBills>,
    private val getCollectionNudgeForCustomerScreen: Lazy<GetCollectionNudgeForCustomerScreen>,
    private val getCollectionNudgeOnSetDueDate: Lazy<GetCollectionNudgeOnSetDueDate>,
    private val subscriptionFeatureEnabled: Lazy<SubscriptionFeatureEnabled>,
    private val getCollectionNudgeOnDueDateCrossed: Lazy<GetCollectionNudgeOnDueDateCrossed>,
    private val isRoboflowFeatureEnabled: Lazy<IsRoboflowFeatureEnabled>,
    private val getKycStatus: Lazy<GetKycStatus>,
    private val getKycRiskCategory: Lazy<GetKycRiskCategory>,
    private val getPaymentOutLinkDetail: Lazy<GetPaymentOutLinkDetail>,
    private val getRiskDetails: Lazy<GetRiskDetails>,
    private val isCustomerCashbackFeatureEnabled: Lazy<IsCustomerCashbackFeatureEnabled>,
    private val getCashbackMessageDetails: Lazy<GetCashbackMessageDetails>,
    private val analyticsEvents: Lazy<CustomerEventTracker>,
    private val checkAutoDueDateGenerated: Lazy<CheckAutoDueDateGenerated>,
    private val reportFromBalanceWidgetExpt: Lazy<ReportFromBalanceWidgetExpt>,
    private val showCollectWithGPay: Lazy<CanShowCollectWithGPay>,
    private val sendCollectionEvent: Lazy<SendCollectionEvent>,
    private val communicationApi: Lazy<CommunicationRepository>,
    private val getSupportNumber: Lazy<GetSupportNumber>,
    private val triggerMerchantPayout: Lazy<TriggerMerchantPayout>,
    private val getContextualHelpIds: Lazy<GetContextualHelpIds>,
    private val getBlindPayLinkId: Lazy<GetBlindPayLinkId>,
    private val showCollectionContextualTrigger: Lazy<ShowCollectionContextualTrigger>,
    private val showCustomerPaymentIntentTrigger: Lazy<ShowCustomerPaymentIntentTrigger>,
    private val customerImmutableHelper: Lazy<CustomerImmutableHelper>,
    private val referralEducationPreference: Lazy<ReferralEducationPreference>,
    private val getStatusForTargetedReferralCustomer: Lazy<GetStatusForTargetedReferralCustomer>,
    private val updateCustomerReferralLedgerSeen: Lazy<UpdateCustomerReferralLedgerSeen>,
    private val getEligibilityOnboardingNudges: Lazy<GetEligibilityOnboardingNudge>,
    private val collectionEventTracker: Lazy<CollectionEventTracker>,
    private val referralSignupTracker: Lazy<RewardsOnSignupTracker>,
    private val getCustomerMenuOptions: Lazy<GetCustomerMenuOptions>,
    private val putNotificationReminder: Lazy<PutNotificationReminder>,
    private val getCustomerSupportType: Lazy<GetCustomerSupportType>,
    private val getCustomerSupportPreference: Lazy<GetCustomerSupportPreference>,
    private val setCashbackBannerClosed: Lazy<SetCashbackBannerClosed>,
    private val getCashbackBannerClosed: Lazy<GetCashbackBannerClosed>,
    private val accountingEventTracker: Lazy<AccountingEventTracker>,
    private val syncCustomerTransactionOrCollection: Lazy<SyncCustomerTransactionOrCollection>,
    private val syncTransaction: Lazy<SyncTransactionsImpl>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
    private val canShowPreNetworkOnboardingBanner: Lazy<CanShowPreNetworkOnboardingBanner>,
    private val getTransaction: Lazy<GetTransaction>,
    private val getCollectionsOfCustomerOrSupplier: Lazy<GetCollectionsOfCustomerOrSupplier>,
    private val getCustomerScreenSortSelection: Lazy<GetCustomerScreenSortSelection>,
    private val setCustomerScreenSortSelection: Lazy<SetCustomerScreenSortSelection>,
    private val getCollectionMerchantProfile: Lazy<GetCollectionMerchantProfile>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    private var showCustomerMenuEducation: Boolean = false
    private var mobile: String = ""
    private var merchantId: String = ""
    private var isDeepLinkOpenedOnce: Boolean? = false
    private var isAlertVisible = false
    private val chatStickerSubject: PublishSubject<Unit> = PublishSubject.create()
    private val billStickerSubject: PublishSubject<Unit> = PublishSubject.create()
    private val nullifyDueDateSubject: PublishSubject<Boolean> = PublishSubject.create()
    private val reload: PublishSubject<Unit> = PublishSubject.create()
    private val showAlertPublicSubject: PublishSubject<String> = PublishSubject.create()
    private var unSyncTransactions: MutableList<Transaction> = arrayListOf()
    private val markCustomerSeenPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    private val customerReactivateSubject: PublishSubject<Unit> = PublishSubject.create()
    private val showAlertPublishSubject: PublishSubject<String> = PublishSubject.create()
    private val checkLastTransactionAddedByCustomer: PublishSubject<Boolean> = PublishSubject.create()
    private val shouldShowPaymentPendingDialogSubject: PublishSubject<DueInfo> = PublishSubject.create()
    private var paymentOutLinkDetailResponse: ApiMessages.PaymentOutLinkDetailResponse? = null
    private var juspayPaymentInstrument: PaymentInstruments? = null
    private var futureLimit: FutureLimit? = null
    private var riskCategory: String = ""
    private var giftIconShown: Boolean = false
    private var exitSource = ""
    private var cashbackBannerShown = false
    private var transactionsSortBy: CustomerScreenSortSelection? = null

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(
            observeSyncCustomerTransactions(),
            observeSyncCustomerCollections(),
            syncCustomerTransactionOrCollectionOnLoad(),
            observeLoadIntents(),
            checkForSubscriptionFeatureEnabled(),
            observeReminderEvent(),
            observeContextualHelpIdsOnLoad(),
            observeCollectionContextualTrigger(),
            observeShowCustomerPaymentIntentTrigger(),
            observePreNetworkWarningBanner(),
            observeCollectionMerchantProfile(),
            // hide network error when network becomes available
            checkNetworkHealth.get()
                .execute(Unit)
                .filter { it is Result.Success }
                .map {
                    // network connected
                    reload.onNext(Unit)
                    PartialState.NoChange
                },

            intent<Intent.Load>()
                .switchMap {
                    wrap(getCustomerDueInfo.get().execute(GetCustomerDueInfo.Request(customerId)))
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            shouldShowPaymentPendingDialogSubject.onNext(it.value)
                            PartialState.CustomerDueInfo(it.value)
                        }

                        is Result.Failure -> {
                            PartialState.ErrorState
                        }
                    }
                },
            intent<Intent.Load>()
                .switchMap { getChatUnreadMessages.get().execute((customerId)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            var count = it.value.first
                            val id = it.value.second
                            if (count == "0") {
                                PartialState.SetUnreadMessageCount("", null)
                            } else if (count.isNotEmpty() && id.isNullOrEmpty().not()) {
                                if (count.toInt() > 9) {
                                    count = "9+"
                                }
                                PartialState.SetUnreadMessageCount(count, it.value.second!!)
                            } else PartialState.NoChange
                        }

                        is Result.Failure -> {
                            PartialState.ErrorState
                        }
                    }
                },

            intent<Intent.Load>()
                .switchMap { wrap(getReferralLink.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.SetReferralId(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> PartialState.NoChange
                                else -> PartialState.NoChange
                            }
                        }
                    }
                },

            // Expand Txns when user coming from deeplink.
            intent<Intent.Load>()
                .map {
                    if (txnId.isNotBlank()) {
                        PartialState.ExpandTransactions
                    } else {
                        PartialState.NoChange
                    }
                },
            intent<Intent.Load>()
                .switchMap { wrap(getAccountsTotalBills.get().execute(customerId)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {

                            PartialState.SetTotalAndUnseenBills(
                                it.value.totalCount,
                                it.value.unseenCount
                            )
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> PartialState.NoChange
                                else -> PartialState.NoChange
                            }
                        }
                    }
                },

            // handle `load` screen intent
            intent<Intent.Load>()
                .switchMapSingle { getCustomerScreenSortSelection.get().execute() }
                .flatMap { sortBy ->
                    loadTransactionData(sortBy)
                },

            reload
                .switchMapSingle { getCustomerScreenSortSelection.get().execute() }
                .flatMap { sortBy ->
                    loadTransactionData(sortBy)
                },

            nullifyDueDateSubject.filter { it }.throttleFirst(100, TimeUnit.MILLISECONDS)
                .switchMap {
                    nullifyDueDate.get().execute(customerId)
                }.map {
                    PartialState.NoChange
                },
            intent<Intent.Load>()
                .switchMap {
                    wrap(
                        getCollectionsOfCustomerOrSupplier.get().execute(customerId)
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.SetCollections(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> PartialState.NoChange
                                else -> PartialState.ErrorState
                            }
                        }
                    }
                },

            intent<Intent.Load>()
                .map {
                    if (reactivate) customerReactivateSubject.onNext(Unit)
                    PartialState.NoChange
                },

            intent<Intent.VoiceInputState>()
                .map {
                    PartialState.ShowVoiceError(it.canShowVoiceError)
                },

            customerReactivateSubject
                .switchMap {
                    Timber.i("customerReactivateSubject 2 $reactivate")
                    wrap(reactivateCustomer.get().execute(customerName, customerId, null))
                }
                .map {
                    when (it) {
                        is Result.Progress -> {
                            PartialState.NoChange
                        }

                        is Result.Success -> {
                            PartialState.NoChange
                        }
                        else -> {
                            PartialState.NoChange
                        }
                    }
                },

            // load merchant for AB
            intent<Intent.Load>()
                .switchMap { wrap(getActiveBusiness.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            merchantId = it.value.id
                            markCustomerSeenPublishSubject.onNext(Unit)
                            PartialState.SetBusiness(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                else -> {
                                    Timber.e(it.error, "ErrorState")
                                    PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            // load customer
            intent<Intent.Load>()
                .switchMap {
                    getCustomer.get().execute(customerId)
                }
                .switchMap { customer ->
                    wrap(
                        rxSingle {
                            customerImmutableHelper.get()
                                .getCleanCustomerDescriptionIfImmutable(customer)
                        }
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            mobile =
                                if (it.value.first.mobile.isNullOrEmpty()) ""
                                else it.value.first.mobile!!

                            if (it.value.first.balanceV2 >= 0) {
                                nullifyDueDateSubject.onNext(true)
                            }

                            PartialState.ShowCustomer(
                                it.value.first,
                                cleanCompanionDescription = it.value.second
                            )
                        }
                        is Result.Failure -> {
                            Timber.e(it.error, "ErrorState")
                            PartialState.ErrorState
                        }
                    }
                },
            intent<Intent.Load>()
                .delay(5, TimeUnit.SECONDS)
                .switchMap {
                    wrap(
                        defaultPreferences.get()
                            .getBoolean(RxSharedPrefValues.COLLECTION_DATE_EDUCATION, Scope.Individual)
                            .asObservable().firstOrError()
                    )
                }
                .flatMap {
                    if (it is Result.Success && it.value) {
                        emitViewEvent(ViewEvent.ShowCollectionDateEducation)
                        rxCompletable {
                            defaultPreferences.get()
                                .remove(RxSharedPrefValues.COLLECTION_DATE_EDUCATION, Scope.Individual)
                        }.andThen(Observable.just(PartialState.NoChange))
                    } else {
                        Observable.just(PartialState.NoChange)
                    }
                },
            intent<Intent.Load>()
                .delay(5, TimeUnit.SECONDS)
                .switchMap {
                    wrap(
                        defaultPreferences.get().getBoolean(RxSharedPrefValues.REMIND_EDUCATION, Scope.Individual)
                            .asObservable().firstOrError()
                    )
                }
                .flatMap {
                    if (it is Result.Success && it.value) {
                        emitViewEvent(ViewEvent.ShowRemindEducation)
                        rxCompletable {
                            defaultPreferences.get().remove(RxSharedPrefValues.REMIND_EDUCATION, Scope.Individual)
                        }.andThen(Observable.just(PartialState.NoChange))
                    } else {
                        Observable.just(PartialState.NoChange)
                    }
                },

            // Update Customer lastViewTime
            markCustomerSeenPublishSubject
                .switchMap { markCustomerAsSeen.get().execute(customerId) }
                .map {
                    PartialState.NoChange
                },

            intent<Intent.Load>()
                .switchMap { wrap(collectionRepository.get().isCollectionActivated()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.SetCollectionActivatedStatus(it.value)
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            intent<Intent.Load>()
                .switchMap { wrap(isSupplierCreditEnabledCustomer.get().execute(customerId)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.SetSupplierCreditEnabledStatus(it.value)
                        }
                        is Result.Failure -> {
                            PartialState.NoChange
                        }
                    }
                },

            intent<Intent.Load>()
                .switchMap { wrap(ab.get().isFeatureEnabled(FEATURE_ACCOUNT_CHATS)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            if (it.value)
                                chatStickerSubject.onNext(Unit)
                            PartialState.SetChatStatus(it.value)
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                },
            intent<Intent.Load>()
                .switchMap { wrap(ab.get().isFeatureEnabled(AbFeatures.BILL_MANAGER)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            if (it.value) {
                                billStickerSubject.onNext(Unit)
                            }
                            PartialState.SetBillStatus(it.value)
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            intent<Intent.Load>()
                .switchMap { wrap(ab.get().isFeatureEnabled(Features.GIVE_DISCOUNT)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.SetDiscountStatus(it.value)
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            intent<Intent.Load>()
                .switchMap {
                    wrap(
                        getCustomerCollectionProfile.get().execute(customerId)
                    )
                }
                .map {

                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.SetCustomerCollectionProfile(it.value)
                        }
                        is Result.Failure -> {
                            Timber.e(it.error, "ErrorState")
                            PartialState.ErrorState
                        }
                    }
                },
            intent<Intent.Load>()
                .switchMap { getCanShowCollectionDate.get().execute(Unit) }
                .flatMap {
                    when (it) {
                        is Result.Progress -> Observable.just(PartialState.NoChange)
                        is Result.Success -> {
                            if (it.value) {
                                //  setting value as false to match current prod build user experience
                                // TODO - Set value true, this fix requires product's decision
                                businessScopedPreferenceWithActiveBusinessId.get()
                                    .setBoolean(defaultPreferences.get(), PREF_BUSINESS_IS_COLLECTION_DATE_SHOWN, false)
                            } else {
                                Completable.complete()
                            }
                                .andThen(Observable.just(PartialState.CanShowCollectionDate(it.value)))
                        }
                        is Result.Failure -> {
                            Timber.e(it.error, "ErrorState")
                            Observable.just(PartialState.ErrorState)
                        }
                    }
                },

            showAlertPublishSubject
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<PartialState> { PartialState.HideAlert }
                        .startWith(PartialState.ShowAlert(it))
                },

            intent<Intent.SharePaymentLink>()
                .switchMap {
                    wrap(
                        getPaymentReminderIntent.get().execute(
                            customerId,
                            "customer_screen",
                            it.reminderMode,
                            it.reminderStringsObject,
                            getCurrentState().customerCollectionProfile?.cashbackEligible ?: false,
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.OpenPaymentReminderIntent(it.value))
                            PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                it.error is IntentHelper.NoWhatsAppError ||
                                    it.error is CompositeException && (it.error as CompositeException).exceptions.find { e -> e is IntentHelper.NoWhatsAppError } != null -> {
                                    showAlertPublishSubject.onNext(
                                        context.get().getString(R.string.whatsapp_not_installed)
                                    )
                                    PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    showAlertPublishSubject.onNext(context.get().getString(R.string.no_internet_msg))
                                    PartialState.NoChange
                                }
                                else -> {
                                    showAlertPublishSubject.onNext(context.get().getString(R.string.err_default))
                                    Timber.e(it.error, "CustomerScreenPresenter SharePayment Link")
                                    tracker.get().trackDebug("CustomerScreenPresenter SharePaymentLink ${it.error}")
                                    PartialState.NoChange
                                }
                            }
                        }
                    }
                },

            showAlertPublicSubject
                .switchMap {
                    Observable.timer(4, TimeUnit.SECONDS)
                        .map<PartialState> { PartialState.HideAlert }
                        .startWith(PartialState.ShowAlert(it))
                },

            // stop media player
            intent<Intent.StopMediaPlayer>()
                .map {
                    PartialState.StopMediaPlayer
                },

            // updating last viewd time
            intent<Intent.UpdateLastViewTime>()
                .switchMap { wrap(markCustomerAsSeen.get().execute(customerId)) }
                .map {
                    PartialState.NoChange
                },

            // expand txs
            intent<Intent.ExpandTransactions>()
                .map {
                    PartialState.ExpandTransactions
                },

            // go to privacy screen
            intent<Intent.GoToPrivacyScreen>()
                .map {
                    emitViewEvent(ViewEvent.GotoCustomerPrivacyScreen)
                    PartialState.NoChange
                },

            // navigate to add txn
            intent<Intent.GoToAddTxn>()
                .switchMap { goToAddTxn ->
                    wrap(
                        if (goToAddTxn.txnType == Transaction.PAYMENT) {
                            ab.get().isFeatureEnabled(Features.CUSTOMER_QR_PAYMENT).firstOrError()
                                .map { it to Transaction.PAYMENT }
                        } else {
                            Single.just(false to Transaction.CREDIT)
                        }
                    )
                }
                .map {
                    if (it is Result.Success) {
                        if (it.value.first) {
                            emitViewEvent(ViewEvent.GoToAddPaymentWithQr(customerId))
                        } else {
                            emitViewEvent(ViewEvent.GotoLegacyAddTransaction(customerId, it.value.second))
                        }
                    }
                    PartialState.NoChange
                },

            // navigate to customer profile screen
            intent<Intent.GoToCustomerProfile>()
                .map {
                    emitViewEvent(ViewEvent.GotoCustomerProfile(customerId))
                    PartialState.NoChange
                },

            // navigate to call customer
            intent<Intent.GoToPhoneDialer>()
                .map {
                    emitViewEvent(ViewEvent.GotoCallCustomer(mobile))
                    PartialState.NoChange
                },

            // navigate to call customer
            intent<Intent.ViewTransaction>()
                .switchMap { wrap(getTransaction.get().execute(it.txnId).firstOrError()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.GotoTransactionDetailFragment(it.value))
                            PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> PartialState.NoChange
                                else -> PartialState.ErrorState
                            }
                        }
                    }
                },

            // navigate to call customer
            intent<Intent.ViewDiscount>()
                .map {
                    emitViewEvent(ViewEvent.GoToDiscountScreen(it.txnId, it.currentDue))
                    PartialState.NoChange
                },
            // navigate to call customer
            intent<Intent.AddMobile>()
                .map {
                    emitViewEvent(ViewEvent.GotoCustomerProfileForAddingMobile(customerId))
                    PartialState.NoChange
                },

            intent<Intent.MarkCustomerShared>()
                .map {
                    markCollectionShared.get().execute(customerId)
                    PartialState.NoChange
                },
            intent<Intent.ShowDueDatePickerIntent>()
                .map {
                    emitViewEvent(ViewEvent.ShowDueDatePickerDialog)
                    PartialState.NoChange
                },

            intent<Intent.OnDueDateChange>()
                .switchMap { updateDueInfo.get().execute(UpdateDueInfo.Request(it.pair)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            pushIntent(Intent.ShowSetupCollection)
                            PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> {
                                    showAlertPublishSubject.onNext(context.get().getString(R.string.no_internet_msg))
                                    PartialState.NoChange
                                }
                                else -> {
                                    Timber.e(it.error, "ErrorState")
                                    PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            intent<Intent.SubmitVoiceInput>()
                .switchMap { submitVoiceInput.get().execute(PostVoiceInput.Request(it.voiceInputText)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            if (it.value.body() != null) {
                                val body = it.value.body()
                                if (body?.status == "SUCCESS") {
                                    if (body.type == "credit") {
                                        tracker.get().trackVoiceTransactionCompleted(
                                            customerId,
                                            PropertyValue.CUSTOMER,
                                            "credit",
                                            body.amount
                                        )
                                        emitViewEvent(
                                            ViewEvent.GotoAddTransactionThroughVoice(
                                                customerId,
                                                Transaction.CREDIT,
                                                body.amount
                                            )
                                        )
                                    } else if (body.type == "payment") {
                                        tracker.get().trackVoiceTransactionCompleted(
                                            customerId,
                                            PropertyValue.CUSTOMER,
                                            "payment",
                                            body.amount
                                        )
                                        emitViewEvent(
                                            ViewEvent.GotoAddTransactionThroughVoice(
                                                customerId,
                                                Transaction.PAYMENT,
                                                body.amount
                                            )
                                        )
                                    }
                                    PartialState.ShowVoiceError(false)
                                } else {
                                    PartialState.ShowVoiceError(true)
                                }
                            } else {
                                PartialState.ShowVoiceError(true)
                            }
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> {
                                    showAlertPublishSubject.onNext(context.get().getString(R.string.no_internet_msg))
                                    PartialState.NoChange
                                }
                                else -> {
                                    Timber.e(it.error, "ErrorState")
                                    PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            intent<Intent.SetReminderMode>()
                .switchMap { wrap(updateCustomer.get().execute(customerId, it.mode)) }
                .map {
                    PartialState.NoChange
                },

            intent<Intent.ShowQrCodeDialog>()
                .switchMap { ab.get().isFeatureEnabled(Features.CUSTOMER_QR_PAYMENT) }
                .map {
                    if (it) {
                        emitViewEvent(ViewEvent.GoToAddPaymentWithQr(customerId, true))
                    } else {
                        emitViewEvent(ViewEvent.ShowQrCodePopup)
                    }
                    PartialState.NoChange
                },

            intent<Intent.HideQrCodeDialog>()
                .map {
                    isAlertVisible = false
                    PartialState.NoChange
                },

            intent<Intent.Unblock>()
                .switchMap { wrap(updateCustomer.get().execute(customerId, Customer.State.ACTIVE)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            tracker.get().trackUnBlockRelation(
                                Event.UNBLOCK_RELATION,
                                PropertyValue.CUSTOMER,
                                customerId,
                                PropertyValue.CUSTOMER_SCREEN
                            )
                            PartialState.SetBlockState(false)
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> {
                                    showAlertPublishSubject.onNext(context.get().getString(R.string.no_internet_msg))
                                    PartialState.NoChange
                                }
                                else -> {
                                    Timber.e(it.error, "ErrorState")
                                    showAlertPublishSubject.onNext(context.get().getString(R.string.err_default))
                                    PartialState.NoChange
                                }
                            }
                        }
                    }
                },

            intent<Intent.ShowUnblockDialog>().map {
                emitViewEvent(ViewEvent.ShowUnblockDialog)
                PartialState.NoChange
            },
            intent<Intent.RxPreferenceBoolean>()
                .switchMap {
                    wrap(rxCompletable { defaultPreferences.get().set(it.key, it.value, it.scope) })
                }
                .map {
                    PartialState.NoChange
                },
            intent<Intent.Load>()
                .switchMap {
                    wrap(
                        defaultPreferences.get()
                            .getBoolean(RxSharedPrefValues.CUSTOMER_MENU_EDUCATION, Scope.Individual)
                            .asObservable().firstOrError()
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            showCustomerMenuEducation = it.value
                            PartialState.ShowCustomerMenuEducation(it.value)
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                    PartialState.NoChange
                },
            intent<Intent.Load>()
                .switchMap {
                    wrap(
                        defaultPreferences.get()
                            .getBoolean(RxSharedPrefValues.GIVE_DISCOUNT_EDUCATION, Scope.Individual)
                            .asObservable().firstOrError()
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.ShowGiveDiscountEducation(it.value)
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                    PartialState.NoChange
                },
            intent<Intent.Load>()
                .switchMap {
                    wrap(
                        defaultPreferences.get().getBoolean(RxSharedPrefValues.CALENDAR_PERMISSION, Scope.Individual)
                            .asObservable().firstOrError()
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.GetCalenderPermission(it.value)
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            canShowOnlineCollectionEducationObservable(),

            chatStickerSubject
                .switchMap {
                    getCanShowChatNewStickerLazy.get().execute(Unit)
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.CanShowChatNewSticker(it.value)
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                },
            billStickerSubject
                .switchMap { wrap(ab.get().isFeatureEnabled(AbFeatures.NEW_ON_BILL_ICON)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.CanShowBillNewSticker(it.value)
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            showCustomerReportABExperimentObservable(),

            reportShareEducationObservable(),

            getChatEducation(),

            payOnlineEducationObservable(),

            canShowBuyerTxnAlert(),

            checkLastTransationAddedByCustomerValid(),

            creditPaymentLayoutVisibilityObservable(),

            intent<Intent.Load>()
                .switchMap { wrap(ab.get().isFeatureEnabled(AbFeatures.SINGLE_LIST)) }
                .map {
                    when (it) {
                        is Result.Success -> {
                            PartialState.IsSingleListEnabled(it.value)
                        }
                        else -> PartialState.NoChange
                    }
                },

            getBillEducation(),

            getTargetBanner(),

            hideReferralTargetBanner(),

            closeReferralTargetBanner(),

            showCollectionNudge(),

            setShouldShowPaymentPendingDialog(),

            observeRoboflowExperimentEnabled(),

            makeUpdateMobileAndRemind(),

            makeForceRemind(),

            canShowKycDialogOnRemind(),

            dontShowKycDialogOnRemind(),

            getKycRisks(),

            payOnline(),

            getPaymentOutLinkDetailResponse(),

            goToPaymentEditAmountScreen(),

            isJuspayFeatureEnabled(),

            getRiskDetails(),

            showCashbackMessageIfAvailable(),

            isCustomerPayOnlinePaymentEnabled(),

            syncDueInfo(),

            disableAutoDueDateDialog(),

            isVoiceTransactionEnabled(),

            reportFromBalanceWidgetExpt(),

            observeShowCollectWithGooglePay(),

            openWhatsAppForHelp(),

            triggerMerchantPayout(),

            isBlindPayEnabledObservable(),

            getBlindPayLinkIdObservable(),

            showSetupCollectionDialog(),

            observeDeleteCustomerAccountIntent(),

            gotoReferralScreen(),

            getStatusForTargetedReferralCustomer(),

            updateCustomerReferralLedgerSeen(),

            getEligibilityOnboardingNudges(),

            isJustPayFeatureEnabledForCustomer(),

            loadMenuOptions(),

            sendNotificationReminder(),

            getCustomerSupportType(),

            openExitDialog(),

            setCashbackBannerClosed(),

            getCashbackBannerClosed(),

            loadSortTransactionsByFeatureFlag(),

            observeSortTransactionsByOption(),
        )
    }

    private fun observeCollectionMerchantProfile() = intent<Intent.Load>().switchMap {
        wrap(getCollectionMerchantProfile.get().execute())
    }.map {
        return@map if (it is Result.Success) {
            PartialState.SetMerchantCollectionProfile(it.value)
        } else {
            PartialState.NoChange
        }
    }

    private fun loadTransactionData(customerScreenSortSelection: CustomerScreenSortSelection): Observable<PartialState> {
        transactionsSortBy = customerScreenSortSelection
        return getCustomerStatement.get().execute(
            GetCustomerStatement.Request(
                customerId,
                sourceScreen,
                collectionId,
                customerScreenSortSelection
            )
        ).map {
            val partialState = when (it) {
                is Result.Progress -> {
                    PartialState.NoChange
                }
                is Result.Success -> {
                    val isPlaySound = isPlaySound(it.value.customerStatement)
                    if (isDeepLinkOpenedOnce == false &&
                        sourceScreen == GetCustomerStatement.FROM_DEEP_LINK &&
                        it.value.transaction != null
                    ) {
                        isDeepLinkOpenedOnce = true
                        emitViewEvent(ViewEvent.GotoTransactionDetailFragment(it.value.transaction as Transaction))
                    }

                    if (txnId.isNotBlank()) {
                        emitViewEvent(ViewEvent.GotoDeletedTransaction(txnId))
                    }

                    checkIfNewOnlineTransactionAdded(getCurrentState().transactions, it.value.customerStatement)

                    PartialState.ShowData(
                        it.value.customerStatement,
                        it.value.lastIndexOfZeroBalanceDue,
                        isPlaySound,
                        customerScreenSortSelection,
                    )
                }
                is Result.Failure -> {
                    when (it.error) {
                        is GetCustomerStatement.TransactionNotFoundException -> {
                            emitViewEvent(ViewEvent.GoToHomeScreen)
                            PartialState.NoChange
                        }
                        else -> {
                            Timber.e(it.error, "ErrorState")
                            PartialState.ErrorState
                        }
                    }
                }
            }
            partialState
        }
    }

    private fun checkIfNewOnlineTransactionAdded(
        currentTransactions: List<Transaction>,
        newTransactions: List<Transaction>,
    ) {
        // list being loaded for first time return
        if (currentTransactions.isEmpty()) return

        // no new txn have been added, probably change in existing transactions
        if (currentTransactions.size == newTransactions.size) {
            return
        }

        // check difference between new list and current list
        val diff = newTransactions.subtract(currentTransactions)
        diff.forEach {
            if (it.isOnlinePaymentTransaction) {
                analyticsEvents.get().trackNewOnlinePaymentShownOnLedger(customerId, it.collectionId!!)
            }
        }
    }

    private fun observeSyncCustomerCollections() = intent<Intent.SyncCustomerCollections>()
        .switchMap {
            wrap(
                getActiveBusinessId.get().execute().doOnSuccess { businessId ->
                    collectionSyncer.get().scheduleSyncCollections(
                        syncType = CollectionSyncer.SYNC_CUSTOMER_COLLECTIONS,
                        source = CollectionSyncer.Source.CUSTOMER_SCREEN,
                        businessId = businessId
                    )
                }
            )
        }
        .map { PartialState.NoChange }

    private fun observeSyncCustomerTransactions() = intent<Intent.SyncCustomerTransactions>().switchMap {
        wrap(syncTransaction.get().execute(source = "customer_screen"))
    }.map {
        PartialState.NoChange
    }

    private fun syncCustomerTransactionOrCollectionOnLoad() = intent<Intent.Load>().switchMap {
        syncCustomerTransactionOrCollection.get().execute(customerId)
    }.map {
        when (it) {
            COLLECTION -> pushIntent(Intent.SyncCustomerCollections)
            TRANSACTION -> pushIntent(Intent.SyncCustomerTransactions)
            BOTH -> {
                pushIntent(Intent.SyncCustomerCollections)
                pushIntent(Intent.SyncCustomerTransactions)
            }
            NONE -> {
                // do nothing
            }
        }
        PartialState.NoChange
    }

    private fun observePreNetworkWarningBanner() = intent<Intent.Load>()
        .switchMap { wrap(rxSingle { canShowPreNetworkOnboardingBanner.get().execute(customerId) }) }
        .map {
            when (it) {
                is Result.Success -> {
                    if (it.value.first) {
                        tracker.get().trackEntryPointViewed(
                            source = "Customer Relationship",
                            type = "Alert",
                            name = "Last Activity",
                            value = it.value.second
                        )
                    }
                    PartialState.SetShowPreNetworkWarningBanner(it.value.first)
                }
                else -> PartialState.NoChange
            }
        }

    private fun getEligibilityOnboardingNudges() = intent<Intent.Load>()
        .switchMap { wrap(getEligibilityOnboardingNudges.get().execute()) }
        .map {
            when (it) {
                is Result.Success -> PartialState.SetEligibilityOnboardingNudges(it.value)
                else -> PartialState.NoChange
            }
        }

    private fun observeDeleteCustomerAccountIntent() = intent<Intent.DeleteImmutableAccount>()
        .switchMap { wrap(rxCompletable { customerImmutableHelper.get().deleteImmutableAccount(customerId) }) }
        .map {
            if (it is Result.Success) {
                emitViewEvent(ViewEvent.AccountDeletedSuccessfully)
            }
            PartialState.NoChange
        }

    private fun observeShowCustomerPaymentIntentTrigger() = intent<Intent.LoadCustomerPaymentIntent>().switchMap {
        wrap(showCustomerPaymentIntentTrigger.get().execute(customerId))
    }.map {
        if (it is Result.Success) {
            return@map PartialState.SetCustomerPaymentIntentTrigger(it.value)
        }
        PartialState.NoChange
    }

    private fun observeCollectionContextualTrigger() = intent<Intent.LoadCollectionContextualTrigger>().switchMap {
        wrap(showCollectionContextualTrigger.get().execute(customerId))
    }.map {
        if (it is Result.Success) {
            if (getCurrentState().contextualTrigger == CollectionTriggerVariant.NONE && it.value.first != CollectionTriggerVariant.NONE) {
                val type =
                    if (it.value.first == CollectionTriggerVariant.SETUP_CREDIT_COLLECTION) "collections_not_setup" else "collections_setup"
                analyticsEvents.get().trackContextualTriggerShown(
                    accountId = customerId,
                    type = type,
                    keyword = it.value.second
                )
            }
            return@map PartialState.SetCollectionContextualTrigger(it.value.first)
        }
        PartialState.NoChange
    }

    private fun observeContextualHelpIdsOnLoad() = intent<Intent.LoadContextualHelp>().switchMap {
        wrap(getContextualHelpIds.get().execute(ScreenName.CustomerScreen.value))
    }.map {
        if (it is Result.Success) {
            return@map PartialState.SetContextualHelpIds(it.value)
        }
        PartialState.NoChange
    }

    private fun observeReminderEvent() = intent<Intent.SharePaymentLink>().switchMap {
        wrap(sendCollectionEvent.get().execute(customerId, SendCollectionEvent.EVENT_REMINDER))
    }.map {
        pushIntent(Intent.SendNotificationReminder)
        PartialState.NoChange
    }

    private fun observeLoadIntents() = intent<Intent.Load>().map {
        pushIntent(Intent.LoadGooglePay)
        pushIntent(Intent.LoadContextualHelp)
        pushIntent(Intent.LoadCollectionContextualTrigger)
        pushIntent(Intent.LoadCustomerPaymentIntent)
        pushIntent(Intent.LoadSortTransactionsByFeatureFlag)
        PartialState.NoChange
    }

    private fun isVoiceTransactionEnabled() = intent<Intent.Load>()
        .switchMap { wrap(ab.get().isFeatureEnabled(FEATURE_CUSTOMER_SCREEN_VOICE_TRANSACTION)) }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> PartialState.SetVoiceTransactionEnabled(it.value)
                is Result.Failure -> PartialState.NoChange
            }
        }

    private fun reportFromBalanceWidgetExpt() = intent<Intent.LoadReportFromBalanceWidgetExpt>()
        .switchMap { wrap(reportFromBalanceWidgetExpt.get().execute()) }
        .map {
            when (it) {
                is Result.Success -> {
                    emitViewEvent(ViewEvent.EnableReportFromBalanceWidgetExp)
                    PartialState.NoChange
                }
                else -> PartialState.NoChange
            }
        }

    private fun checkForSubscriptionFeatureEnabled() = intent<Intent.Load>().switchMap {
        subscriptionFeatureEnabled.get().execute()
    }.map {
        PartialState.SubscriptionFeature(it)
    }

    private fun showCashbackMessageIfAvailable(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { isCustomerCashbackFeatureEnabled.get().execute() }
            .switchMap { isEnabled ->
                if (isEnabled) {
                    return@switchMap wrap(
                        getCashbackMessageDetails.get().execute()
                            .map { cashbackMessageDetails ->

                                val cashbackMessageType =
                                    if (cashbackMessageDetails.isFirstTransaction) FIRST else REGULAR
                                tracker.get().trackPayOnlineCashbackPageView(
                                    accountId = customerId,
                                    screen = RELATIONSHIP,
                                    type = ONLINE_PAYMENT,
                                    relation = CUSTOMER,
                                    cashbackMessageType = cashbackMessageType,
                                    cashbackAmount = cashbackMessageDetails.cashbackAmount.toString(),
                                    minimumPaymentAmount = cashbackMessageDetails.minimumPaymentAmount.toString(),
                                )

                                getCashbackMessageDetails.get().getHumanReadableStringFromModel(cashbackMessageDetails)
                            }
                    )
                } else {
                    tracker.get().trackPayOnlineCashbackPageView(
                        accountId = customerId,
                        screen = RELATIONSHIP,
                        type = ONLINE_PAYMENT,
                        relation = CUSTOMER,
                        cashbackMessageType = NOT_AVAILABLE,
                        cashbackAmount = "",
                        minimumPaymentAmount = "",
                    )
                    return@switchMap wrap(Single.just(""))
                }
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> PartialState.SetCashbackMessage(it.value)
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun hideReferralTargetBanner(): Observable<PartialState> {
        return intent<Intent.HideTargetBanner>()
            .switchMap { wrap(transactionInitiated.get().execute()) }
            .map {
                when (it) {
                    is Result.Success -> PartialState.SetTargetBanner(null)
                    else -> PartialState.NoChange
                }
            }
    }

    private fun closeReferralTargetBanner(): Observable<PartialState> {
        return intent<Intent.CloseTargetBanner>()
            .switchMap { wrap(closeReferralTargetBanner.get().execute()) }
            .map {
                when (it) {
                    is Result.Success -> PartialState.SetTargetBanner(null)
                    else -> PartialState.NoChange
                }
            }
    }

    private fun getTargetBanner(): Observable<PartialState> {
        return intent<Intent.Load>()
            .take(1)
            .switchMap { wrap(getReferralTarget.get().execute(Place.CUSTOMER_SCREEN)) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        if (it.value is Optional.Present) {
                            referralSignupTracker.get().trackTargetBannerViewed()
                            PartialState.SetTargetBanner(
                                (it.value as Optional.Present<ReferralTargetBanner>).`object`
                            )
                        } else {
                            PartialState.SetTargetBanner(null)
                        }
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun checkLastTransationAddedByCustomerValid(): Observable<PartialState.NoChange>? {
        return checkLastTransactionAddedByCustomer.switchMap {
            if (it) {
                getLastTransactionAddedByCustomer.get().execute(customerId)
            } else wrap(Single.just(false))
        }.map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> {
                    if (it.value) {
                        emitViewEvent(ViewEvent.ShowBuyerTxnAlert)
                    }
                    PartialState.NoChange
                }
                is Result.Failure -> PartialState.NoChange
            }
        }
    }

    private fun creditPaymentLayoutVisibilityObservable(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap {
                canShowCreditPaymentLayout.get().execute(customerId)
            }.map {
                PartialState.CanShowCreditPaymentLayout(it)
            }
    }

    private fun canShowBuyerTxnAlert(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap {
                getCanShowBuyerTxnAlert.get().execute(GetCanShowBuyerTxnAlert.Request(customerId))
            }.map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        if (it.value && context.get().isConnectedToInternet()) {
                            checkLastTransactionAddedByCustomer.onNext(true)
                        }
                        PartialState.NoChange
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun getChatEducation(): ObservableSource<UiState.Partial<State>>? {
        // load call inapp tutorial visibility
        return Observable.timer(2, TimeUnit.SECONDS)
            .switchMap {
                wrap(
                    defaultPreferences.get().getBoolean(RxSharedPrefValues.SHOULD_SHOW_CHAT_TUTORIAL, Scope.Individual)
                        .asObservable().firstOrError()
                )
            }
            .flatMap {
                if (it is Result.Success && it.value && mobile.isNotEmpty()) {
                    emitViewEvent(ViewEvent.ShowChatEducation)
                    rxCompletable {
                        defaultPreferences.get().remove(RxSharedPrefValues.SHOULD_SHOW_CHAT_TUTORIAL, Scope.Individual)
                    }.andThen(Observable.just(PartialState.NoChange))
                } else {
                    Observable.just(PartialState.NoChange)
                }
            }
    }

    private fun getBillEducation(): ObservableSource<UiState.Partial<State>>? {
        // load call inapp tutorial visibility
        return Observable.timer(3, TimeUnit.SECONDS)
            .switchMap {
                wrap(
                    defaultPreferences.get().getBoolean(RxSharedPrefValues.SHOULD_SHOW_BILL_TUTORIAL, Scope.Individual)
                        .asObservable().firstOrError()
                )
            }
            .flatMap {
                if (it is Result.Success && it.value && mobile.isNotEmpty()) {
                    emitViewEvent(ViewEvent.ShowBillEducation)
                    rxCompletable {
                        defaultPreferences.get().remove(RxSharedPrefValues.SHOULD_SHOW_BILL_TUTORIAL, Scope.Individual)
                    }.andThen(Observable.just(PartialState.NoChange))
                } else {
                    Observable.just(PartialState.NoChange)
                }
            }
    }

    private fun reportShareEducationObservable(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap {
                wrap(
                    defaultPreferences.get()
                        .getBoolean(RxSharedPrefValues.IS_REPORT_ICON_EDUCATION_SHOWN, Scope.Individual)
                        .asObservable().firstOrError()
                )
            }.map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        if (it.value.not()) {
                            emitViewEvent(ViewEvent.ShowReportIconEducation)
                        }
                        PartialState.NoChange
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun showCustomerReportABExperimentObservable(): ObservableSource<UiState.Partial<State>>? {
        return intent<Intent.ShowCustomerReport>()
            .map {
                emitViewEvent(ViewEvent.GoToCustomerReport(it.source))
                PartialState.NoChange
            }
    }

    private fun canShowOnlineCollectionEducationObservable(): Observable<PartialState.NoChange>? {
        return intent<Intent.SendCollectionReminderClicked>()
            .switchMap {
                wrap(checkOnlineEducationToShow.get().execute().firstOrError())
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        if (it.value) {
                            emitViewEvent(ViewEvent.ShowOnlineCollectionEducation)
                        } else {
                            emitViewEvent(ViewEvent.OnReminderClicked)
                        }
                    }
                    is Result.Failure -> PartialState.NoChange
                }
                PartialState.NoChange
            }
    }

    // comparing latest txn with unsync list for checking playing sound
    private fun isPlaySound(transactions: List<Transaction>): Boolean {
        var isPlaySound = false
        for (unSyncTransaction in unSyncTransactions) {
            for (item in transactions) {
                // Hack: assuming created time is an identical field
                if (item.createdAt == unSyncTransaction.createdAt && !item.isDirty) {
                    isPlaySound = true
                }
            }
        }
        saveUnSyncTransactions(transactions)
        return isPlaySound
    }

    // Saving unSyncTransactions for playing sync sound
    private fun saveUnSyncTransactions(transactions: List<Transaction>) {
        unSyncTransactions.clear()
        for (item in transactions) {
            if (item.isDirty) {
                unSyncTransactions.add(item)
            }
        }
    }

    private fun payOnlineEducationObservable(): ObservableSource<UiState.Partial<State>>? {
        return intent<Intent.ShowPayOnlineEducation>()
            .take(1)
            .switchMap {
                wrap(
                    defaultPreferences.get().getBoolean(IS_PAY_ONLINE_EDUCATION_SHOWN, Scope.Individual, true)
                        .asObservable().firstOrError()
                )
            }
            .delay(2, TimeUnit.SECONDS)
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        if (it.value.not()) {
                            emitViewEvent(ViewEvent.ShowPayOnlineEducation)
                        }
                        PartialState.NoChange
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun showCollectionNudge() = intent<Intent.Load>()
        .switchMap { getCollectionNudgeForCustomerScreen.get().execute(Unit) }
        .map {
            when (it) {
                is Result.Success -> PartialState.SetCollectionNudge(it.value)
                else -> PartialState.SetCollectionNudge(GetCollectionNudgeForCustomerScreen.Show.NONE)
            }
        }

    private fun showSetupCollectionDialog() = intent<Intent.ShowSetupCollection>()
        .switchMap { getCollectionNudgeOnSetDueDate.get().execute(Unit) }
        .map {
            if (it is Result.Success && it.value) {
                emitViewEvent(ViewEvent.ShowSetupCollectionDialog)
            }
            PartialState.NoChange
        }

    private fun setShouldShowPaymentPendingDialog() = shouldShowPaymentPendingDialogSubject
        .delay(500, TimeUnit.MILLISECONDS)
        .switchMap {
            getCollectionNudgeOnDueDateCrossed.get().execute(it)
        }
        .takeUntil { it is Result.Success }
        .map {
            when (it) {
                is Result.Success -> {
                    emitViewEvent(
                        ViewEvent.ShowPaymentPendingDialog(
                            it.value.customer,
                            it.value.dueInfo,
                            it.value.show
                        )
                    )
                    PartialState.NoChange
                }
                else -> PartialState.NoChange
            }
        }

    private fun observeRoboflowExperimentEnabled() = intent<Intent.Load>()
        .switchMap { wrap(isRoboflowFeatureEnabled.get().execute()) }
        .map {
            when (it) {
                is Result.Success -> PartialState.isRoboflowFeatureEnabled(it.value)
                else -> PartialState.NoChange
            }
        }

    private fun makeUpdateMobileAndRemind() = intent<Intent.UpdateMobileAndRemind>()
        .switchMap { wrap(getCustomer.get().execute(customerId).firstOrError()) }
        .map {
            when (it) {
                is Result.Success -> {
                    emitViewEvent(ViewEvent.ForceRemind)
                    PartialState.SetCustomer(it.value)
                }
                else -> PartialState.NoChange
            }
        }

    private fun makeForceRemind() = intent<Intent.ForceRemind>()
        .throttleFirst(300, TimeUnit.MILLISECONDS)
        .map {
            emitViewEvent(ViewEvent.ForceRemind)
            PartialState.NoChange
        }

    private fun payOnline(): Observable<PartialState> {
        return intent<Intent.PayOnline>()
            .map {
                if (paymentOutLinkDetailResponse != null) {
                    if (!paymentOutLinkDetailResponse?.paymentOutLink?.destination?.paymentAddress.isNullOrBlank()) {
                        pushIntent((Intent.IsJuspayFeatureEnabled))
                    } else if (getCurrentState().isJustPayEnabled &&
                        getCurrentState().isBlindPayEnabled &&
                        getCurrentState().customer?.mobile.isNotNullOrBlank()
                    ) {
                        pushIntent((CustomerContract.Intent.GetBlindPayLinkId))
                    } else {
                        emitViewEvent(ViewEvent.ShowAddPaymentMethodDialog)
                    }
                } else {
                    pushIntent(Intent.GetPaymentOutLinkDetail)
                }

                PartialState.NoChange
            }
    }

    private fun getPaymentOutLinkDetailResponse(): Observable<PartialState>? {
        return intent<Intent.GetPaymentOutLinkDetail>()
            .switchMap {
                wrap(getPaymentOutLinkDetail.get().execute(customerId, "CUSTOMER"))
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        paymentOutLinkDetailResponse = it.value
                        pushIntent(Intent.PayOnline)
                        PartialState.SetDestinationUpdateAllowed(
                            destinationUpdateAllowed = it.value.paymentOutLink?.destinationUpdateAllowed ?: true
                        )
                    }
                    is Result.Failure -> {
                        if (it.error is CollectionServerErrors.DestinationNotSet) {
                            emitViewEvent(ViewEvent.ShowAddPaymentMethodDialog)
                        } else {
                            analyticsEvents.get()
                                .trackSupplierTxnPageApiError(
                                    customerId, it.error.message ?: "",
                                    CustomerEventTracker.GET_PAYOUT_LINK_DETAILS,
                                    CUSTOMER_SCREEN
                                )
                            showAlertPublishSubject.onNext(
                                context.get().getString(R.string.err_default)
                            )
                        }

                        PartialState.NoChange
                    }
                }
            }
    }

    private fun isJuspayFeatureEnabled(): Observable<PartialState> {
        return intent<Intent.IsJuspayFeatureEnabled>()
            .switchMap { wrap(ab.get().isFeatureEnabled(CUSTOMER_JUSPAY_FEATURE)) }
            .map { result ->
                when (result) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        if (result.value) {
                            pushIntent(Intent.GetRiskDetailsResponse)
                        } else {
                            paymentOutLinkDetailResponse?.paymentOutLink?.let { paymentOutLink ->
                                emitViewEvent(
                                    ViewEvent.ShowWebFlowDestinationDialog(
                                        paymentOutLink.profile?.messageLink ?: "",
                                        paymentOutLink.destination?.paymentAddress ?: "",
                                        paymentOutLink.destination?.type ?: "",
                                        paymentOutLink.destination?.name ?: ""
                                    )
                                )
                            }
                        }
                        PartialState.NoChange
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun isJustPayFeatureEnabledForCustomer(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { wrap(ab.get().isFeatureEnabled(CUSTOMER_JUSPAY_FEATURE)) }
            .map { result ->
                when (result) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        PartialState.SetJustPayEnabled(result.value)
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun getRiskDetails(): Observable<PartialState> {
        return intent<Intent.GetRiskDetailsResponse>()
            .switchMap {
                wrap(getRiskDetails.get().execute("supplier_collection", "APP"))
            }
            .map { result ->
                when (result) {
                    is Result.Progress -> PartialState.SetPayOnlineLoading(true)
                    is Result.Success -> {
                        riskCategory = result.value.riskCategory
                        juspayPaymentInstrument =
                            result.value.paymentInstruments.first { instruments -> instruments.instrumentName == "juspay" }
                        futureLimit = result.value.futureLimit

                        val isJuspayPaymentInstrumentEnabled = juspayPaymentInstrument?.enabled
                            ?: false
                        val dailyLimitReached = juspayPaymentInstrument?.limitInfo?.dailyLimitReached
                            ?: false
                        val remainingAmountLimit = juspayPaymentInstrument?.limitInfo?.remainingDailyAmountLimit
                            ?: 0L
                        val dailyMaxAmountLimit = juspayPaymentInstrument?.limitInfo?.totalDailyAmountLimit
                            ?: 0L

                        val kycStatus = KycStatus.valueOf(result.value.kycInfo.kycStatus)

                        if (!dailyLimitReached) {
                            return@map dailyLimitNotReached(isJuspayPaymentInstrumentEnabled, kycStatus)
                        } else {
                            setPopUpDisplayWhenAmountLimitReached(
                                remainingAmountLimit,
                                dailyMaxAmountLimit
                            )
                            PartialState.SetPayOnlineLoading(false)
                        }
                    }
                    is Result.Failure -> {
                        when {
                            isInternetIssue(result.error) -> {
                                showAlertPublishSubject.onNext(context.get().getString(R.string.no_internet_msg))
                            }
                            else -> {
                                analyticsEvents.get().trackSupplierTxnPageApiError(
                                    customerId, result.error.message ?: "",
                                    CustomerEventTracker.RISK_API,
                                    CUSTOMER_SCREEN
                                )
                                showAlertPublishSubject.onNext(context.get().getString(R.string.err_default))
                            }
                        }
                        PartialState.SetPayOnlineLoading(false)
                    }
                }
            }
    }

    private fun dailyLimitNotReached(
        isJuspayPaymentInstrumentEnabled: Boolean,
        kycStatus: KycStatus,
    ): PartialState {
        if (isJuspayPaymentInstrumentEnabled) {
            if (getCurrentState().isBlindPayEnabled && paymentOutLinkDetailResponse?.paymentOutLink?.destination?.paymentAddress.isNullOrBlank()) {
                emitViewEvent(
                    ViewEvent.GotoCustomerBlindPayEditAmountScreen(
                        paymentOutLinkDetailResponse?.paymentOutLink?.destination?.paymentAddress ?: "",
                        paymentOutLinkDetailResponse?.paymentOutLink?.destination?.type ?: "",
                        paymentOutLinkDetailResponse?.paymentOutLink?.destination?.name ?: "",
                        juspayPaymentInstrument?.limitInfo?.remainingDailyAmountLimit ?: 0L,
                        juspayPaymentInstrument?.limitInfo?.totalDailyAmountLimit ?: 0L,
                        riskCategory,
                    )
                )
                return PartialState.SetPayOnlineLoading(false)
            } else {
                pushIntent(Intent.GotoPaymentEditAmountScreen(kycStatus))
            }
            return PartialState.NoChange
        } else {
            paymentOutLinkDetailResponse?.paymentOutLink?.let {
                emitViewEvent(
                    ViewEvent.ShowWebFlowDestinationDialog(
                        it.profile?.messageLink ?: "",
                        it.destination?.paymentAddress ?: "",
                        it.destination?.type ?: "",
                        it.destination?.name ?: ""
                    )
                )
            }
            return PartialState.SetPayOnlineLoading(false)
        }
    }

    private fun goToPaymentEditAmountScreen(): Observable<PartialState> {
        return intent<Intent.GotoPaymentEditAmountScreen>()
            .map { data ->
                emitViewEvent(
                    ViewEvent.GotoPaymentEditAmountScreen(
                        paymentOutLinkDetailResponse?.paymentOutLink?.profile?.linkId ?: "",
                        paymentOutLinkDetailResponse?.paymentOutLink?.destination?.paymentAddress ?: "",
                        paymentOutLinkDetailResponse?.paymentOutLink?.destination?.type ?: "",
                        paymentOutLinkDetailResponse?.paymentOutLink?.destination?.name ?: "",
                        juspayPaymentInstrument?.limitInfo?.remainingDailyAmountLimit ?: 0L,
                        juspayPaymentInstrument?.limitInfo?.totalDailyAmountLimit ?: 0L,
                        riskCategory,
                        data.kycStatus,
                        KycRiskCategory.NO_RISK,
                        futureLimit?.totalAmountLimit ?: 0L,
                    )
                )
                PartialState.SetPayOnlineLoading(false)
            }
    }

    private fun isCustomerPayOnlinePaymentEnabled(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap { wrap(ab.get().isFeatureEnabled(Features.CUSTOMER_ONLINE_PAYMENT)) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> PartialState.SetCustomerPayOnlinePaymentEnabled(it.value)
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun setPopUpDisplayWhenAmountLimitReached(remainingAmountLimit: Long, dailyMaxAmountLimit: Long) {
        analyticsEvents.get().trackSupplierPaymentLimitPopDisplayed(
            accountId = customerId,
            relation = CustomerEventTracker.RELATION_CUSTOMER,
            screen = CustomerEventTracker.CUSTOMER_SCREEN,
            flow = CustomerEventTracker.JUSPAY_SUPPLIER_COLLECTION,
            dueAmount = initialState.customer?.balanceV2.toString(),
            type = CustomerEventTracker.LIMIT_EXCEEDED,
            userTxnLimit = dailyMaxAmountLimit.toString(),
            availTxnLimit = remainingAmountLimit.toString(),
            txnType = CustomerEventTracker.TXN_AMOUNT
        )
        emitViewEvent(
            ViewEvent.OpenLimitReachedBottomSheet(
                remainingAmountLimit,
                dailyMaxAmountLimit,
                true
            )
        )
    }

    private fun canShowKycDialogOnRemind() = intent<Intent.Load>()
        .switchMap {
            defaultPreferences.get().getBoolean(RxSharedPrefValues.KYC_DONT_ASK_AGAIN_ON_REMIND, Scope.Individual)
                .asObservable()
        }
        .map {
            PartialState.SetCanShowKycDialog(it.not())
        }

    private fun dontShowKycDialogOnRemind() = intent<Intent.DontShowKycDialogOnRemind>()
        .flatMap {
            rxCompletable {
                defaultPreferences.get().set(RxSharedPrefValues.KYC_DONT_ASK_AGAIN_ON_REMIND, true, Scope.Individual)
            }.andThen(Observable.just(PartialState.SetCanShowKycDialog(false)))
        }

    private fun getKycDetails() = Observable.zip(
        getKycStatus.get().execute(),
        getKycRiskCategory.get().execute(),
        { kycStatus, kycRisk ->
            kotlin.Pair(kycStatus, kycRisk)
        }
    )

    private fun getKycRisks() = intent<Intent.LoadKycDetails>()
        .switchMap { getKycDetails() }
        .map {
            PartialState.SetKyc(it.first, it.second.kycRiskCategory, it.second.isLimitReached)
        }

    private fun disableAutoDueDateDialog() = intent<Intent.DisableAutoDueDateDialog>()
        .switchMap {
            wrap(
                businessScopedPreferenceWithActiveBusinessId.get()
                    .setBoolean(
                        defaultPreferences.get(),
                        PREF_BUSINESS_SHOULD_SHOW_AUTO_DUE_DATE + customerId,
                        it.disable
                    )
            )
        }
        .map {
            PartialState.NoChange
        }

    private fun observeShowCollectWithGooglePay() = intent<Intent.LoadGooglePay>()
        .switchMap {
            wrap(
                showCollectWithGPay.get().execute()
            )
        }.map {
            when (it) {
                is Result.Success -> PartialState.CanShowCollectWithGooglePay(it.value)
                else -> PartialState.NoChange
            }
        }

    private fun syncDueInfo() = intent<Intent.SyncDueInfo>()
        .switchMap {
            wrap(checkAutoDueDateGenerated.get().execute(customerId))
        }
        .map {
            if (it is Result.Success) {
                emitViewEvent(ViewEvent.ShowAutoDueDateDialog(it.value))
            }
            PartialState.NoChange
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
                    if (it.error is IntentHelper.NoWhatsAppError) {
                        emitViewEvent(ViewEvent.ShowError(context.get().getString(R.string.whatsapp_not_installed)))
                    } else {
                        emitViewEvent(ViewEvent.ShowError(context.get().getString(R.string.err_default)))
                    }
                    PartialState.NoChange
                }
                is Result.Success -> {
                    emitViewEvent(ViewEvent.OpenWhatsAppForHelp(it.value))
                    PartialState.NoChange
                }
                else -> PartialState.NoChange
            }
        }

    private fun triggerMerchantPayout() = intent<Intent.CollectionDestinationAdded>()
        .switchMap {
            wrap(
                triggerMerchantPayout.get()
                    .executePayout(
                        PayoutType.PAYOUT.value,
                        collectionType = "merchant_qr",
                        payoutId = "",
                        paymentId = ""
                    )
            )
        }
        .map {
            PartialState.NoChange
        }

    private fun gotoReferralScreen() = intent<Intent.GotoReferralScreen>()
        .switchMap {
            collectionEventTracker.get()
                .trackCollectionReferralGiftClicked(
                    customerId,
                    CollectionEventTracker.CUSTOMER_SCREEN,
                    ReferralStatus.fromValue(getCurrentState().statusTargetedReferral).toString()
                )
            wrap(
                referralEducationPreference.get()
                    .shouldShowReferralEducationScreen()
            )
        }
        .map {
            if (it is Result.Success) {
                if (it.value)
                    emitViewEvent(ViewEvent.GotoReferralEducationScreen)
                else {
                    emitViewEvent(ViewEvent.GotoReferralInviteListScreen)
                }

                PartialState.NoChange
            } else PartialState.NoChange
        }

    private fun getStatusForTargetedReferralCustomer() = intent<Intent.Load>()
        .switchMap {
            wrap(
                getStatusForTargetedReferralCustomer.get().execute(customerId)
            )
        }
        .map {
            if (it is Result.Success) {
                if (it.value == ReferralStatus.LINK_CREATED.value)
                    pushIntent(Intent.UpdateLedgerSeen)
                PartialState.SetStatusForTargetedReferralCustomer(it.value)
            } else PartialState.NoChange
        }

    private fun updateCustomerReferralLedgerSeen() = intent<Intent.UpdateLedgerSeen>()
        .switchMap {
            wrap(
                updateCustomerReferralLedgerSeen.get().execute(customerId)
            )
        }
        .map {
            PartialState.NoChange
        }

    private fun sendNotificationReminder() = intent<Intent.SendNotificationReminder>()
        .switchMap {
            wrap(putNotificationReminder.get().execute(customerId))
        }.map {
            PartialState.NoChange
        }

    private fun isBlindPayEnabledObservable(): Observable<PartialState> {
        return intent<CustomerContract.Intent.Load>()
            .switchMap { wrap(ab.get().isFeatureEnabled(Features.BLIND_PAY)) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        PartialState.SetBlindPayEnabled(it.value)
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun getBlindPayLinkIdObservable(): ObservableSource<UiState.Partial<State>> {
        return intent<CustomerContract.Intent.GetBlindPayLinkId>()
            .switchMap {
                UseCase.wrapSingle(
                    getBlindPayLinkId.get().execute(customerId)
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        emitViewEvent(ViewEvent.ShowBlindPayDialog)
                        PartialState.SetBlindPayLinkId(it.value)
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun loadMenuOptions() = intent<Intent.Load>()
        .switchMap {
            wrap(getCustomerMenuOptions.get().execute(customerId))
        }
        .map {
            if (it is Result.Success) {
                return@map PartialState.SetMenuOptions(it.value)
            }
            return@map PartialState.NoChange
        }

    private fun getCustomerSupportType(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap {
                wrap(getCustomerSupportType.get().execute())
            }
            .map {
                if (it is Result.Success) {
                    PartialState.SetSupportType(it.value)
                } else
                    PartialState.NoChange
            }
    }

    private fun openExitDialog(): Observable<PartialState> {
        return intent<Intent.OpenExitDialog>()
            .switchMap {
                exitSource = it.exitSource
                wrap(getCustomerSupportPreference.get().shouldShowCustomerSupportExitDialog())
            }
            .map {
                if (it is Result.Success) {
                    if (it.value)
                        emitViewEvent(ViewEvent.OpenExitDialog(exitSource))
                }
                PartialState.NoChange
            }
    }

    private fun setCashbackBannerClosed(): Observable<PartialState> {
        return intent<Intent.CashbackBannerClosed>()
            .switchMap {
                wrap(setCashbackBannerClosed.get().execute(customerId))
            }
            .map {
                if (it is Result.Success) {
                    emitViewEvent(ViewEvent.CashbackBannerClosed)
                }
                PartialState.NoChange
            }
    }

    private fun getCashbackBannerClosed(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap {
                wrap(getCashbackBannerClosed.get().execute(customerId))
            }
            .map {
                if (it is Result.Success)
                    PartialState.SetCashbackBannerClosed(it.value)
                else PartialState.NoChange
            }
    }

    private fun loadSortTransactionsByFeatureFlag(): Observable<PartialState> {
        return intent<Intent.LoadSortTransactionsByFeatureFlag>()
            .switchMap { ab.get().isFeatureEnabled(FEATURE_CUSTOMER_SUPPLIER_SCREEN_TXN_SORT_SETTING) }
            .map { PartialState.SetShowSortTransactionsBy(it) }
    }

    private fun observeSortTransactionsByOption(): Observable<PartialState> {
        return intent<Intent.SortTransactionsByOptionSelected>()
            .switchMapSingle {
                rxSingle {
                    val sortSelection = CustomerScreenSortSelection.convertToSortBy(it.customerScreenSortSelection)
                    setCustomerScreenSortSelection.get().execute(sortSelection)
                    return@rxSingle sortSelection
                }
            }
            .flatMap { sortBy -> loadTransactionData(sortBy) }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        val tempState = when (partialState) {
            is PartialState.SetBusiness -> currentState.copy(business = partialState.business)
            is PartialState.ShowData -> {
                // reset bank added card if new transaction added
                val showBankAddedAcknowledgeCard =
                    currentState.showBankAddedAcknowledgeCard && currentState.transactions.size != partialState.transaction.size
                currentState.copy(
                    isLoading = false,
                    transactions = partialState.transaction,
                    showOnboardingNudges = partialState.transaction.isEmpty(),
                    lastZeroBalanceIndex = partialState.lastZeroBalanceIndex,
                    playSound = partialState.playSound,
                    showBankAddedAcknowledgeCard = showBankAddedAcknowledgeCard,
                    sortTransactionsBy = partialState.sortTransactionsBy,
                )
            }
            is PartialState.ShowCustomer -> currentState.copy(
                customer = partialState.customer,
                cleanCompanionDescription = partialState.cleanCompanionDescription,
                isBlocked = partialState.customer.state == Customer.State.BLOCKED
            )
            is PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is PartialState.StopMediaPlayer -> currentState.copy(playSound = false)
            is PartialState.ExpandTransactions -> currentState.copy(isTxnExpanded = true)
            is PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is PartialState.SetCollections -> currentState.copy(collectionsMap = partialState.collections.associateBy { it.id })
            is PartialState.SetCollectionActivatedStatus -> currentState.copy(
                isCollectionActivated = partialState.status,
            )
            is PartialState.SetSupplierCreditEnabledStatus -> currentState.copy(
                isSupplierCreditEnabledForCustomer = partialState.status
            )
            is PartialState.NoChange -> currentState
            is PartialState.CustomerDueInfo -> currentState.copy(dueInfo = partialState.dueInfo)
            is PartialState.ShowVoiceError -> currentState.copy(canShowVoiceError = partialState.canShowError)
            is PartialState.SetCustomerCollectionProfile -> currentState.copy(
                customerCollectionProfile = partialState.customerCollectionProfile,
                collectWithGPayEnabled = partialState.customerCollectionProfile.googlePayEnabled,
            )
            is PartialState.SetReferralId -> currentState.copy(referralId = partialState.referralId)
            is PartialState.SetBlockState -> currentState.copy(isBlocked = partialState.isBlocked)
            is PartialState.SetDiscountStatus -> currentState.copy(isDiscountEnabled = partialState.status)
            is PartialState.ShowGiveDiscountEducation -> currentState.copy(showGiveDiscountEducation = partialState.showGiveDiscountEducation)
            is PartialState.ShowCustomerMenuEducation -> currentState.copy(showCustomerMenuEducation = partialState.showCustomerMenuEducation)
            is PartialState.GetCalenderPermission -> currentState.copy(canGetCalendarPermission = partialState.canGetCalendarPermission)
            is PartialState.CanShowCollectionDate -> currentState.copy(canShowCollectionDate = partialState.canShowCollectionDate)
            is PartialState.SetUnreadMessageCount -> currentState.copy(
                unreadMessageCount = partialState.unreadMessageCount,
                firstUnseenMessageId = partialState.firstUnseenMessageId
            )
            is PartialState.CanShowBillNewSticker -> currentState.copy(canShowBillNewSticker = partialState.canShowBillNewSticker)
            is PartialState.CanShowChatNewSticker -> currentState.copy(canShowChatNewSticker = partialState.canShowChatNewSticker)
            is PartialState.SetChatStatus -> currentState.copy(isChatEnabled = partialState.isChatEnabled)
            is PartialState.SetBillStatus -> currentState.copy(isBillEnabled = partialState.isBillEnabled)
            is PartialState.CanShowCreditPaymentLayout -> currentState.copy(
                canShowCreditPaymentLayout = partialState.canShowCreditPaymentLayout
            )
            is PartialState.IsSingleListEnabled -> currentState.copy(
                isSingleListEnabled = partialState.isSingleListEnabled
            )
            is PartialState.SetTotalAndUnseenBills -> currentState.copy(
                totalBills = partialState.totalBills, unreadBillCount = partialState.unseenBills
            )
            is PartialState.SetTargetBanner -> currentState.copy(
                referralTargetBanner = partialState.content,
            )
            is PartialState.SubscriptionFeature -> currentState.copy(
                showSubscriptions = partialState.enabled
            )
            is PartialState.SetCollectionNudge -> currentState.copy(showCollectionNudge = partialState.canShowCollectionNudge)
            is PartialState.isRoboflowFeatureEnabled -> currentState.copy(
                isRoboflowFeatureEnabled = partialState.enabled
            )
            is PartialState.SetCustomer -> currentState.copy(customer = partialState.customer)

            is PartialState.SetKyc -> currentState.copy(
                kycStatus = partialState.kycStatus,
                kycRiskCategory = partialState.kycRiskCategory,
                isKycLimitReached = partialState.isKycLimitReached
            )

            is PartialState.SetCanShowKycDialog -> currentState.copy(canShowKycDialogOnRemind = partialState.canShowKycDialog)
            is PartialState.SetPayOnlineLoading -> currentState.copy(
                showPayOnlineButtonLoader = partialState.loading
            )
            is PartialState.SetCustomerPayOnlinePaymentEnabled -> currentState.copy(
                isCustomerPayOnlinePaymentEnable = partialState.isCustomerPayOnlinePaymentEnable
            )
            is PartialState.SetCashbackMessage -> currentState.copy(
                cashbackMessage = partialState.cashbackMessage
            )
            is PartialState.SetVoiceTransactionEnabled -> currentState.copy(
                isVoiceTransactionEnabled = partialState.enabled
            )
            is PartialState.CanShowCollectWithGooglePay -> currentState.copy(
                showCollectionWithGpay = partialState.show
            )
            is PartialState.SetContextualHelpIds -> currentState.copy(
                contextualHelpIds = partialState.helpIds
            )

            is PartialState.SetBlindPayEnabled -> currentState.copy(isBlindPayEnabled = partialState.isBlindPayEnabled)

            is PartialState.SetBlindPayLinkId -> currentState.copy(blindPayLinkId = partialState.blindPayLinkId)

            is PartialState.SetMenuOptions -> currentState.copy(
                menuOptionsResponse = partialState.menuOptions
            )
            is PartialState.SetCollectionContextualTrigger -> {
                // if current trigger is setup collection and new trigger is pay online then merchant has added bank details
                // sho show bank added card
                val showBankAddedAcknowledgeCard = if (!currentState.showBankAddedAcknowledgeCard) {
                    (
                        currentState.contextualTrigger == CollectionTriggerVariant.SETUP_CREDIT_COLLECTION ||
                            currentState.contextualTrigger == CollectionTriggerVariant.SETUP_PAYMENT_COLLECTION
                        ) &&
                        (
                            partialState.contextualTrigger == CollectionTriggerVariant.COLLECT_CREDIT_ONLINE ||
                                partialState.contextualTrigger == CollectionTriggerVariant.COLLECT_PAYMENT_ONLINE
                            )
                } else {
                    currentState.showBankAddedAcknowledgeCard
                }
                currentState.copy(
                    showBankAddedAcknowledgeCard = showBankAddedAcknowledgeCard,
                    contextualTrigger = partialState.contextualTrigger
                )
            }
            is PartialState.SetCustomerPaymentIntentTrigger -> {
                val showBankAddedAcknowledgeCard = if (!currentState.showBankAddedAcknowledgeCard) {
                    currentState.showAddBankDetails && !partialState.trigger
                } else {
                    currentState.showBankAddedAcknowledgeCard
                }
                currentState.copy(
                    showBankAddedAcknowledgeCard = showBankAddedAcknowledgeCard,
                    showAddBankDetails = partialState.trigger
                )
            }
            is PartialState.SetStatusForTargetedReferralCustomer -> currentState.copy(
                statusTargetedReferral = partialState.statusTargetedReferral
            )
            is PartialState.SetEligibilityOnboardingNudges -> currentState.copy(
                showOnboardingNudges = partialState.canShow
            )

            is PartialState.SetJustPayEnabled -> currentState.copy(isJustPayEnabled = partialState.isJustPayEnabled)
            is PartialState.SetSupportType -> currentState.copy(supportType = partialState.type)
            is PartialState.SetCashbackBannerClosed -> currentState.copy(cashbackBannerClosed = partialState.cashbackBannerClosed)
            is PartialState.SetShowPreNetworkWarningBanner -> currentState.copy(
                showPreNetworkWarningBanner = partialState.canShow
            )
            is PartialState.SetDestinationUpdateAllowed -> currentState.copy(
                destinationUpdateAllowed = partialState.destinationUpdateAllowed
            )
            is PartialState.SetShowSortTransactionsBy -> currentState.copy(
                showSortTransactionsBy = partialState.canShow
            )
            is PartialState.SetMerchantCollectionProfile -> currentState.copy(
                collectionMerchantProfile = partialState.collectionMerchantProfile
            )
        }
        return tempState.copy(
            customerScreenList = prepareCustomerScreenTransactionList(tempState),
            shouldShowCashbackBanner = shouldShowCashbackBanner(tempState)
        )
    }

    private fun shouldShowCashbackBanner(state: State): Boolean {
        state.collectionsMap.forEach {
            if (it.value.cashbackGiven) {
                return false
            }
        }
        val totalAmount = if (state.customer?.balanceV2 == null) 0L else state.customer.balanceV2
        if (totalAmount >= 0) return false

        val cashbackEligible = state.customerCollectionProfile?.cashbackEligible ?: false
        if (cashbackEligible.not()) return false

        if (state.cashbackBannerClosed) return false

        if (cashbackBannerShown.not()) {
            cashbackBannerShown = true
            accountingEventTracker.get().trackCashbackMsgShown(
                customerId,
                BALANCE_WIDGET,
            )
        }
        return true
    }

    private fun prepareCustomerScreenTransactionList(state: State): List<CustomerScreenItem> {
        val list = mutableListOf<CustomerScreenItem>()
        if (state.isLoading) {
            list.add(CustomerScreenItem.LoadingItem)
            return list
        }

        if (state.transactions.isEmpty()) {
            list.add(CustomerScreenItem.EmptyPlaceHolder(state.customer?.description))
            return list
        }

        if (!state.isTxnExpanded && state.lastZeroBalanceIndex > 0) {
            list.add(CustomerScreenItem.LoadMoreItem)
        }
        var lastTransactionDate = ""
        val startIndex = if (!state.isTxnExpanded && state.lastZeroBalanceIndex > 0) {
            state.lastZeroBalanceIndex + 1
        } else {
            0
        }
        for (index in startIndex until state.transactions.size) {
            val transaction = state.transactions[index]
            val currentTransactionDate = if (transactionsSortBy == CREATE_DATE) {
                DateTimeUtils.formatTx(transaction.createdAt, context.get())
            } else {
                DateTimeUtils.formatTx(transaction.billDate, context.get())
            }
            // add date only if current txn date is not same as last txn date
            if (currentTransactionDate != lastTransactionDate) {
                lastTransactionDate = currentTransactionDate
                list.add(CustomerScreenItem.DateItem(currentTransactionDate))
            }

            when {
                transactionDeleted(state, transaction) -> {
                    list.add(createDeletedTransaction(state, transaction))
                }
                transactionProcessing(state, transaction) -> {
                    list.add(createProcessingTransaction(state, transaction))
                }
                else -> {
                    list.add(createTransactionItem(state, transaction))
                }
            }
        }

        checkForActionOrInfoNudgeItem(state, list)
        return list.toList()
    }

    private fun checkForActionOrInfoNudgeItem(state: State, list: MutableList<CustomerScreenItem>) {
        when {
            state.statusTargetedReferral !in listOf(
                ReferralStatus.DEFAULT.value,
                ReferralStatus.REWARD_SUCCESS.value
            ) -> {
                if (giftIconShown.not()) {
                    giftIconShown = true
                    collectionEventTracker.get().trackCollectionReferralGiftShown(
                        customerId,
                        CollectionEventTracker.CUSTOMER_SCREEN,
                        CollectionEventTracker.CUSTOMER,
                        ReferralStatus.fromValue(state.statusTargetedReferral).toString()
                    )
                }

                when (state.statusTargetedReferral) {
                    1 -> {
                        list.add(
                            CustomerScreenItem.InfoNudgeItem(
                                1,
                                TxnGravity.LEFT,
                                state.customer?.description
                            )
                        )
                    }
                    2 -> {
                        list.add(
                            CustomerScreenItem.InfoNudgeItem(
                                2,
                                TxnGravity.LEFT,
                                state.customer?.description
                            )
                        )
                    }
                    4 -> {
                        list.add(
                            CustomerScreenItem.InfoNudgeItem(
                                3,
                                TxnGravity.LEFT,
                                state.customer?.description
                            )
                        )
                    }
                    else -> {
                        list.add(
                            CustomerScreenItem.InfoNudgeItem(
                                4,
                                TxnGravity.LEFT,
                                state.customer?.description
                            )
                        )
                    }
                }
            }
            state.showAddBankDetails -> {
                list.add(CustomerScreenItem.RequestActionItem(0))
            }
            state.showBankAddedAcknowledgeCard -> {
                list.add(CustomerScreenItem.AcknowledgeActionItem(0))
            }
            state.contextualTrigger == CollectionTriggerVariant.SETUP_CREDIT_COLLECTION -> {
                list.add(CustomerScreenItem.RequestActionItem(1))
            }
            state.contextualTrigger == CollectionTriggerVariant.SETUP_PAYMENT_COLLECTION -> {
                list.add(CustomerScreenItem.RequestActionItem(2))
            }
            state.contextualTrigger == CollectionTriggerVariant.COLLECT_CREDIT_ONLINE -> {
                list.add(CustomerScreenItem.InfoNudgeItem(0, TxnGravity.RIGHT))
            }
            state.contextualTrigger == CollectionTriggerVariant.COLLECT_PAYMENT_ONLINE -> {
                list.add(CustomerScreenItem.InfoNudgeItem(5, TxnGravity.LEFT))
            }
        }
    }

    private fun createTransactionItem(
        state: State,
        transaction: Transaction,
    ): CustomerScreenItem.TransactionItem {
        val cashbackGiven = if (transaction.isOnlinePaymentTransaction) {
            state.collectionsMap[transaction.collectionId]?.cashbackGiven ?: false
        } else {
            false
        }
        return CustomerScreenItem.TransactionItem(
            id = transaction.id,
            txnGravity = findUiTxnGravity(
                transaction.type == Transaction.PAYMENT || transaction.type == Transaction.RETURN,
                LedgerType.CUSTOMER
            ),
            amount = transaction.amountV2,
            date = findFormattedDateOrTime(transaction.createdAt, transaction.billDate, isBilledDateSortSelected()),
            isDirty = transaction.isDirty,
            image = findReceiptPath(transaction),
            imageCount = transaction.receiptUrl?.size ?: 0,
            note = transaction.note,
            txnTag = findTransactionTag(transaction, state.customer?.description),
            currentBalance = transaction.currentDue,
            discountTransaction = transaction.isDiscountTransaction,
            deletedTxn = transaction.isDeleted,
            cashbackGiven = cashbackGiven,
            customerId = customerId,
        )
    }

    private fun isBilledDateSortSelected() = transactionsSortBy == BILL_DATE

    private fun findTransactionTag(transaction: Transaction, customerName: String?): String? {
        return when {
            transaction.isDiscountTransaction && transaction.isDeleted -> {
                context.get().getString(R.string.discount_deleted)
            }
            transaction.isDiscountTransaction -> {
                context.get().getString(R.string.discount_offered)
            }
            transaction.amountUpdated -> {
                context.get().getString(R.string.edited)
            }
            transaction.isSubscriptionTransaction -> {
                context.get().getString(R.string.subscription)
            }
            transaction.isOnlinePaymentTransaction -> {
                context.get().getString(R.string.online_payment_transaction)
            }
            transaction.isCreatedByCustomer -> {
                String.format(
                    context.get().getString(R.string.added_by),
                    ellipsizeName(customerName)
                )
            }
            else -> {
                null
            }
        }
    }

    private fun ellipsizeName(name: String?): String {
        val maxLength = 10
        return when {
            name.isNullOrBlank() -> ""
            name.length > maxLength -> name.substring(0, maxLength) + "..."
            else -> name
        }
    }

    private fun findReceiptPath(transaction: Transaction): String? {
        if (transaction.receiptUrl.isNullOrEmpty()) return null

        val firstImage = transaction.receiptUrl!![0]
        return firstImage.imageUrl
    }

    private fun createProcessingTransaction(
        state: State,
        transaction: Transaction,
    ): CustomerScreenItem.ProcessingTransaction {
        val collectionStatus = if (transaction.isOnlinePaymentTransaction) {
            state.collectionsMap[transaction.collectionId]?.status
        } else {
            null
        }

        val errorCode = if (transaction.isOnlinePaymentTransaction) {
            state.collectionsMap[transaction.collectionId]?.errorCode
        } else {
            null
        }
        val blindPay = state.collectionsMap[transaction.collectionId]?.blindPay ?: false

        var action = if (AccountingSharedUtils.isSevenDaysPassed(transaction.billDate)) {
            CustomerScreenItem.ProcessingTransactionAction.NONE
        } else {
            CustomerScreenItem.ProcessingTransactionAction.HELP
        }
        var statusTitle = context.get().getString(R.string.settlement_pending)
        var statusDescription = if (blindPay) {
            context.get().getString(R.string.blind_pay_payment_processing_customer)
        } else {
            context.get().getString(R.string.payment_refund_reason_payout_initiated)
        }

        when {
            collectionStatus == PAYOUT_FAILED -> {
                when (errorCode) {
                    OnlinePaymentErrorCode.EP001.value -> {
                        action = CustomerScreenItem.ProcessingTransactionAction.ADD_BANK
                        statusTitle = context.get().getString(R.string.settlement_blocked)
                        statusDescription = context.get().getString(R.string.settlement_blocked_reason)
                    }
                    OnlinePaymentErrorCode.EP002.value -> {
                        statusDescription = context.get().getString(R.string.settlement_blocked_due_to_bank_offline)
                    }
                    OnlinePaymentErrorCode.EP004.value -> {
                        statusDescription =
                            context.get().getString(R.string.settlement_blocked_due_to_bank_issue_try_after_24_hrs)
                    }
                }
            }
            state.collectionMerchantProfile.remainingLimit <= 0 -> {
                action = CustomerScreenItem.ProcessingTransactionAction.KYC
                statusDescription = context.get().getString(R.string.t_002_daily_settlement_pending_delayed_ledger_text)
            }
            state.settlementType.isNotEmpty() -> { // TODO: 23/09/21 add proper check for settlement type here
                action = CustomerScreenItem.ProcessingTransactionAction.NONE
                statusDescription = context.get().getString(R.string.t_002_daily_settlement_pending_ledger_text)
            }
        }

        val paymentId = if (transaction.isOnlinePaymentTransaction) {
            state.collectionsMap[transaction.collectionId]?.paymentId ?: ""
        } else {
            ""
        }

        return CustomerScreenItem.ProcessingTransaction(
            id = transaction.id,
            paymentId = paymentId,
            txnGravity = findUiTxnGravity(
                isPayment = transaction.type == Transaction.PAYMENT || transaction.type == Transaction.RETURN,
                ledgerType = LedgerType.CUSTOMER
            ),
            amount = transaction.amountV2,
            date = findFormattedDateOrTime(transaction.createdAt, transaction.billDate, isBilledDateSortSelected()),
            currentBalance = transaction.currentDue,
            dateTime = DateTimeUtils.formatLong(transaction.billDate),
            statusTitle = statusTitle,
            statusNote = statusDescription,
            action = action,
        )
    }

    private fun createDeletedTransaction(
        state: State,
        transaction: Transaction,
    ): CustomerScreenItem.DeletedTransaction {
        val collectionStatus = if (transaction.isOnlinePaymentTransaction) {
            state.collectionsMap[transaction.collectionId]?.status
        } else {
            null
        }

        val blindPay = state.collectionsMap[transaction.collectionId]?.blindPay ?: false

        val paymentId = if (transaction.isOnlinePaymentTransaction) {
            state.collectionsMap[transaction.collectionId]?.paymentId ?: ""
        } else {
            ""
        }

        return CustomerScreenItem.DeletedTransaction(
            id = transaction.id,
            paymentId = paymentId,
            txnGravity = findUiTxnGravity(
                transaction.type == Transaction.PAYMENT || transaction.type == Transaction.RETURN,
                LedgerType.CUSTOMER
            ),
            isDirty = transaction.isDirty,
            isDeletedByCustomer = transaction.isDeletedByCustomer,
            amount = transaction.amountV2,
            date = findFormattedDateOrTime(transaction.createdAt, transaction.billDate, isBilledDateSortSelected()),
            currentBalance = transaction.currentDue,
            onlineTxn = transaction.isOnlinePaymentTransaction,
            customerName = state.customer?.description,
            collectionStatus = collectionStatus,
            isBlindPay = blindPay,
            accountId = customerId,
            shouldShowHelpOption = AccountingSharedUtils.isSevenDaysPassed(transaction.billDate).not(),
            supportType = getCurrentState().supportType.value,
            dateTime = DateTimeUtils.formatLong(transaction.billDate),
        )
    }

    private fun transactionProcessing(state: State, transaction: Transaction): Boolean {
        if (transaction.isOnlinePaymentTransaction) {
            val collectionStatus = state.collectionsMap[transaction.collectionId]?.status

            return collectionStatus == null ||
                collectionStatus == CollectionStatus.PAID ||
                collectionStatus == PAYOUT_INITIATED ||
                collectionStatus == PAYOUT_FAILED
        }
        return false
    }

    private fun transactionDeleted(state: State, transaction: Transaction): Boolean {
        if (transaction.transactionCategory == Transaction.DISCOUNT) {
            return false
        }

        if (transaction.isOnlinePaymentTransaction) {
            val collectionStatus = state.collectionsMap[transaction.collectionId]?.status
            return collectionStatus == CollectionStatus.REFUNDED ||
                collectionStatus == CollectionStatus.REFUND_INITIATED ||
                collectionStatus == CollectionStatus.FAILED
        }
        return transaction.isDeleted
    }
}
