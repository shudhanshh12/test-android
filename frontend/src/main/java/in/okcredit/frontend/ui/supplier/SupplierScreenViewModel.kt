package `in`.okcredit.frontend.ui.supplier

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.PropertyValue.FIRST
import `in`.okcredit.analytics.PropertyValue.NOT_AVAILABLE
import `in`.okcredit.analytics.PropertyValue.ONLINE_PAYMENT
import `in`.okcredit.analytics.PropertyValue.REGULAR
import `in`.okcredit.analytics.PropertyValue.RELATIONSHIP
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.GetWhatsAppShareIntent
import `in`.okcredit.backend._offline.usecase.ReactivateSupplier
import `in`.okcredit.backend._offline.usecase.UpdateSupplier
import `in`.okcredit.backend.collection_usecases.GetSupplierStatement
import `in`.okcredit.backend.contract.Features
import `in`.okcredit.backend.contract.Features.FEATURE_CUSTOMER_SUPPLIER_SCREEN_TXN_SORT_SETTING
import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.backend.contract.RxSharedPrefValues.IS_PAY_ONLINE_EDUCATION_SHOWN
import `in`.okcredit.backend.server.GetRiskDetails
import `in`.okcredit.backend.server.riskInternal.FutureLimit
import `in`.okcredit.backend.server.riskInternal.PaymentInstruments
import `in`.okcredit.cashback.contract.usecase.GetCashbackMessageDetails
import `in`.okcredit.cashback.contract.usecase.IsSupplierCashbackFeatureEnabled
import `in`.okcredit.collection.contract.*
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.pre_network_onboarding.CanShowPreNetworkOnboardingBannerSupplier
import `in`.okcredit.frontend.ui.supplier.SupplierContract.*
import `in`.okcredit.frontend.usecase.GetSupplierCollection
import `in`.okcredit.frontend.usecase.supplier.GetSupplierTransaction
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.customer_ui.usecase.GetCanShowChatNewSticker
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.merchant.suppliercredit.use_case.GetSupplierScreenSortSelection
import `in`.okcredit.merchant.suppliercredit.use_case.GetSupplierScreenSortSelection.SupplierScreenSortSelection
import `in`.okcredit.merchant.suppliercredit.use_case.GetSupplierScreenSortSelection.SupplierScreenSortSelection.BILL_DATE
import `in`.okcredit.merchant.suppliercredit.use_case.GetSupplierScreenSortSelection.SupplierScreenSortSelection.CREATE_DATE
import `in`.okcredit.merchant.suppliercredit.use_case.SetSupplierScreenSortSelection
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.shared.utils.AbFeatures
import `in`.okcredit.shared.utils.ScreenName
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierPropertyValue.JUSPAY_SUPPLIER_COLLECTION
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierPropertyValue.LIMIT_EXCEEDED
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierPropertyValue.RISK_API
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierPropertyValue.SUPPLIER
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierPropertyValue.SUPPLIER_SCREEN
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierPropertyValue.TXN_AMOUNT
import `in`.okcredit.supplier.utils.SupplierFeatures.SUPPLIER_JUSPAY_FEATURE
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.exceptions.CompositeException
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle
import merchant.okcredit.accounting.contract.model.LedgerType
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportType
import merchant.okcredit.accounting.usecases.GetCustomerSupportPreference
import merchant.okcredit.accounting.utils.AccountingSharedUtils.findFormattedDateOrTime
import merchant.okcredit.accounting.utils.AccountingSharedUtils.findUiTxnGravity
import merchant.okcredit.accounting.utils.AccountingSharedUtils.isSevenDaysPassed
import tech.okcredit.account_chat_contract.FEATURE
import tech.okcredit.account_chat_contract.IGetChatUnreadMessageCount
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.app_contract.AppConstants
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.feature_help.contract.GetContextualHelpIds
import tech.okcredit.home.usecase.GetSupplierKnowMoreWebLink
import tech.okcredit.use_case.GetAccountsTotalBills
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class SupplierScreenViewModel @Inject constructor(
    private val initialState: State,
    @ViewModelParam(MainActivity.ARG_SUPPLIER_ID) val supplierId: String,
    @ViewModelParam(MainActivity.REACTIVATE) val reactivate: Boolean,
    @ViewModelParam(MainActivity.ARG_TXN_ID) val txnId: String, // This is need to auto scroll recyclerview to this transaction id
    @ViewModelParam(MainActivity.NAME) val supplierName: String,
    private val defaultPreferences: Lazy<DefaultPreferences>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val getWhatsAppShareIntent: Lazy<GetWhatsAppShareIntent>,
    private val getSupplierStatement: Lazy<GetSupplierStatement>,
    private val reactivateSupplier: Lazy<ReactivateSupplier>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val abRepository: Lazy<AbRepository>,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    private val updateSupplier: Lazy<UpdateSupplier>,
    private val context: Lazy<Context>,
    private val tracker: Lazy<Tracker>,
    private val getSupplierCollectionProfileWithSync: Lazy<GetSupplierCollectionProfileWithSync>,
    private val getSupplierKnowMoreWebLink: Lazy<GetSupplierKnowMoreWebLink>,
    private val getCanShowChatNewStickerLazy: Lazy<GetCanShowChatNewSticker>,
    private val getChatUnreadMessages: Lazy<IGetChatUnreadMessageCount>,
    private val isMerchantFromCollectionCampaign: IsCollectionCampaignMerchant,
    private val getAccountsTotalBills: Lazy<GetAccountsTotalBills>,
    private val getRiskDetails: Lazy<GetRiskDetails>,
    private val supplierAnalyticsEvents: Lazy<SupplierAnalyticsEvents>,
    private val getSupplierCollection: Lazy<GetSupplierCollection>,
    private val isSupplierCashbackFeatureEnabled: Lazy<IsSupplierCashbackFeatureEnabled>,
    private val getCashbackMessageDetails: Lazy<GetCashbackMessageDetails>,
    private val getContextualHelpIds: Lazy<GetContextualHelpIds>,
    private val getBlindPayLinkId: Lazy<GetBlindPayLinkId>,
    private val getSupplierTransaction: Lazy<GetSupplierTransaction>,
    private val getCustomerSupportType: Lazy<GetCustomerSupportType>,
    private val getCustomerSupportPreference: Lazy<GetCustomerSupportPreference>,
    private val canShowPreNetworkOnboardingBanner: Lazy<CanShowPreNetworkOnboardingBannerSupplier>,
    private val getSupplierScreenSortSelection: Lazy<GetSupplierScreenSortSelection>,
    private val setSupplierScreenSortSelection: Lazy<SetSupplierScreenSortSelection>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    private var supplier: Supplier? = null
    private var mobile: String = ""
    private var supplierCollectionCustomerProfile: CollectionCustomerProfile? = null
    private val reload: PublishSubject<Unit> = PublishSubject.create()
    private var unSyncTransactions: MutableList<Transaction> = arrayListOf()
    private val showAlertPublishSubject: PublishSubject<String> = PublishSubject.create()
    private val supplierReactivateSubject: PublishSubject<Unit> = PublishSubject.create()
    private val showPayOnlineEducationPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    private val chatStickerSubject: PublishSubject<Unit> = PublishSubject.create()
    private val billStickerSubject: PublishSubject<Unit> = PublishSubject.create()
    private var transactionsSortSelection: SupplierScreenSortSelection? = null

    // when deleted transaction notification is clicked we take user to this screen , at the point 'show old' view should not be present
    // because 'show old' hides old transaction that has become zero , so we cannot scroll to any of those transactions if we want to.
    // To show all those transactions that is hidden inside 'show old' view we have to set expandTransaction=true only if we have
    // values in 'txnId' (which is use to auto scroll to deleted transaction)
    private var expandTransaction = false
    private var showTakeCreditEducation = false
    private var isCollectionCampaignMerchant = false
    private var isPageViewed = AtomicBoolean(false)
    private var juspayPaymentInstrument: PaymentInstruments? = null
    private var futureLimit: FutureLimit? = null
    private var riskCategory: String = ""
    private var exitSource = ""

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(
            observeContextualHelpIdsOnLoad(),
            observePreNetworkOnboardingWarningBanner(),
            // hide network error when network becomes available
            checkNetworkHealth.get()
                .execute(Unit)
                .filter { it is Result.Success }
                .map {
                    // network connected
                    reload.onNext(Unit)
                    PartialState.NoChange
                },

            /*********************** Loading for supplier app promotion ab ***********************/
            intent<Intent.Load>()
                .switchMap {
                    UseCase.wrapObservable(
                        abRepository.get().isFeatureEnabled(Features.UNREGISTERED_SUPPLIER_APP_PROMOTION)
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.ShowAppPromotion(it.value)
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            intent<Intent.Load>()
                .switchMap { wrap(getAccountsTotalBills.get().execute(supplierId)) }
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
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(ViewEvent.GotoLogin)
                                    PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> PartialState.NoChange
                                else -> PartialState.NoChange
                            }
                        }
                    }
                },

            // handle `load` screen intent
            Observable
                .merge(intent<Intent.Load>(), reload)
                .switchMapSingle { getSupplierScreenSortSelection.get().execute() }
                .switchMap { sortSelection ->
                    transactionsSortSelection = sortSelection
                    getSupplierStatement.get().execute(supplierId, sortSelection)
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {

                            supplier = it.value.supplier
                            mobile =
                                if (it.value.supplier.mobile.isNullOrEmpty()) "" else it.value.supplier.mobile.toString()
                            val isPlaySound = isPlaySound(it.value.transactions)
                            if (isPageViewed.get().not()) {
                                isPageViewed.set(true)
                                tracker.get().trackPageViewed(supplierId)
                            }
                            if (txnId.isNotBlank()) {
                                expandTransaction = true
                                pushIntent(Intent.GoToDeletedTransaction)
                            }
                            PartialState.ShowData(
                                it.value.supplier,
                                it.value.transactions,
                                it.value.lastIndexOfZeroBalanceDue,
                                isPlaySound,
                                transactionsSortSelection,
                            )
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> PartialState.SetNetworkError(true)
                                else -> PartialState.NoChange
                            }
                        }
                    }
                },

            // load merchant for AB
            intent<Intent.Load>()
                .switchMap { UseCase.wrapObservable(getActiveBusiness.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.SetBusiness(it.value, true)
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> PartialState.SetNetworkError(true)
                                else -> PartialState.NoChange
                            }
                        }
                    }
                },

            intent<Intent.Load>()
                .map {
                    if (reactivate) supplierReactivateSubject.onNext(Unit)
                    PartialState.NoChange
                },

            isMerchantFromCollectionCampaignObservable(),

            canShowPayOnlineEducationObservable(),

            getSupplierCollectionProfileObservable(),

            isBlindPayEnabledObservable(),

            getBlindPayLinkIdObservable(),

            showAlertPublishSubject
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<PartialState> { PartialState.HideAlert }
                        .startWith(PartialState.ShowAlert(it))
                },

            supplierReactivateSubject
                .switchMap { reactivateSupplier.get().execute(ReactivateSupplier.Request(supplierName, supplierId)) }
                .map { PartialState.NoChange },

            // handle `show alert` intent
            intent<Intent.ShowAlert>()
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<PartialState> { PartialState.HideAlert }
                        .startWith(PartialState.ShowAlert(it.message))
                },

            // stop media player
            intent<Intent.StopMediaPlayer>()
                .map {
                    PartialState.StopMediaPlayer
                },

            // updating last viewd time
            intent<Intent.UpdateLastViewTime>()
                .switchMap { wrap(supplierCreditRepository.get().markActivityAsSeen(supplierId)) }
                .map { PartialState.NoChange },

            intent<Intent.SetupMerchantProfile>()
                .map {
                    emitViewEvent(ViewEvent.GoToMerchantProfileForSetupProfile)
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
                    emitViewEvent(ViewEvent.GotoSupplierPrivacyScreen)
                    PartialState.NoChange
                },

            // navigate to add txn
            intent<Intent.GoToAddTxn>()
                .map {
                    emitViewEvent(ViewEvent.GotoAddTransaction(supplierId, it.txnType))
                    PartialState.NoChange
                },

            // navigate to supplier profile screen
            intent<Intent.GoToSupplierProfile>()
                .map {
                    emitViewEvent(ViewEvent.GotoSupplierProfile(supplierId))
                    PartialState.NoChange
                },

            // navigate to call supplier
            intent<Intent.GoToPhoneDialer>()
                .map {
                    emitViewEvent(ViewEvent.GotoCallSupplier(mobile))
                    PartialState.NoChange
                },

            // navigate to call supplier
            intent<Intent.ViewTransaction>()
                .switchMap {
                    UseCase.wrapSingle(getSupplierTransaction.get().execute(it.txnId).firstOrError())
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.GotoTransactionScreen(it.value))
                            PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(ViewEvent.GotoLogin)
                                    PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> PartialState.NoChange
                                else -> PartialState.ErrorState
                            }
                        }
                    }
                },

            // navigate to call supplier
            intent<Intent.AddMobile>()
                .map {
                    emitViewEvent(ViewEvent.GotoSupplierProfileForAddingMobile(supplierId))
                    PartialState.NoChange
                },
            /***********************   Show supplier take payment education  ***********************/
            intent<Intent.Load>()
                .switchMap
                {
                    wrap(
                        defaultPreferences.get()
                            .getBoolean(RxSharedPrefValues.SHOULD_SHOW_TAKE_CREDIT_PAYMENT_EDUCATION, Scope.Individual)
                            .asObservable().firstOrError()
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            showTakeCreditEducation = it.value
                            PartialState.ShowTakeCreditPaymentEducation(it.value)
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                    PartialState.NoChange
                },

            intent<Intent.RxPreferenceBoolean>()
                .switchMap { wrap(rxCompletable { defaultPreferences.get().set(it.key, it.value, it.scope) }) }
                .map {
                    PartialState.NoChange
                },
            intent<Intent.SetTakeGiveCreditEducation>()
                .map {
                    PartialState.ShowTakeCreditPaymentEducation(it.canShow && showTakeCreditEducation)
                },

            intent<Intent.ShowUnblockDialog>()
                .map {
                    emitViewEvent(ViewEvent.ShowUnblockDialog)
                    PartialState.ShowUnblockDialog
                },
            intent<Intent.Unblock>()
                .switchMap { UseCase.wrapCompletable(updateSupplier.get().execute(supplierId, Supplier.ACTIVE, true)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            tracker.get().trackUnBlockRelation(
                                Event.UNBLOCK_RELATION,
                                PropertyValue.SUPPLIER, supplierId, PropertyValue.SUPPLIER_SCREEN
                            )
                            PartialState.SetBlockState(false)
                        }

                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> {
                                    showAlertPublishSubject.onNext(context.get().getString(R.string.no_internet_msg))
                                    PartialState.NoChange
                                }
                                else -> PartialState.ErrorState
                            }
                        }
                    }
                },
            intent<Intent.ShareAppPromotion>()
                .switchMap {
                    UseCase.wrapSingle(
                        getWhatsAppShareIntent.get().execute(
                            GetWhatsAppShareIntent.WhatsAppShareRequest(
                                it.sharingText,
                                supplier!!.mobile,
                                it.bitmap,
                                "Supplier"
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.OpenWhatsAppPromotionShare(it.value))
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
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(ViewEvent.GotoLogin)
                                    PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    showAlertPublishSubject.onNext(context.get().getString(R.string.no_internet_msg))
                                    PartialState.NoChange
                                }
                                else -> {
                                    showAlertPublishSubject.onNext(context.get().getString(R.string.err_default))
                                    tracker.get().trackDebug("CustomerScreenPresenter SharePaymentLink ${it.error}")
                                    PartialState.NoChange
                                }
                            }
                        }
                    }
                },

            payOnlineEducationObservable(),

            intent<Intent.SupplierLearnMore>()
                .switchMap { wrap(getSupplierKnowMoreWebLink.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.GoToSupplierLearnMoreWebLink(it.value))
                            PartialState.NoChange
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                },
            intent<Intent.Load>()
                .switchMap {
                    getChatUnreadMessages.get().execute((supplierId))
                }
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
                        is Result.Failure -> PartialState.NoChange
                    }
                },
            intent<Intent.Load>()
                .switchMap {
                    UseCase.wrapObservable(abRepository.get().isFeatureEnabled(FEATURE.FEATURE_ACCOUNT_CHATS))
                }
                .doOnNext {
                    when (it) {
                        is Result.Success -> {
                            if (it.value) {
                                chatStickerSubject.onNext(Unit)
                            }
                        }
                    }
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.SetChatStatus(it.value)
                        is Result.Failure -> PartialState.NoChange
                    }
                },
            intent<Intent.Load>()
                .switchMap { wrap(abRepository.get().isFeatureEnabled(AbFeatures.BILL_MANAGER)) }
                .doOnNext {
                    when (it) {
                        is Result.Success -> {
                            if (it.value) {
                                billStickerSubject.onNext(Unit)
                            }
                        }
                    }
                }.map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.SetBillStatus(it.value)
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            billStickerSubject
                .switchMap { wrap(abRepository.get().isFeatureEnabled(AbFeatures.NEW_ON_BILL_ICON)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.CanShowBillNewSticker(it.value)
                        is Result.Failure -> PartialState.NoChange
                    }
                },
            chatStickerSubject
                .switchMap { getCanShowChatNewStickerLazy.get().execute(Unit) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.CanShowChatNewSticker(it.value)
                        is Result.Failure -> PartialState.NoChange
                    }
                },
            getChatEducation(),

            showNewReport(),

            getBillEducation(),

            payOnline(),

            isJuspayFeatureEnabled(),

            getRiskDetails(),

            gotoSupplierEditAmountScreen(),

            getCollectionProfileAfterSetDestination(),

            syncListCollectionFromServer(),

            setOngoingPaymentAttributes(),

            showCashbackMessageIfAvailable(),

            goToDeletedTransaction(),

            isJustPayFeatureEnabledForSupplierObserver(),

            getCustomerSupportType(),

            openExitDialog(),

            observeSortSelection(),

            loadSortTransactionsByFeatureFlag(),
        )
    }

    private fun loadSortTransactionsByFeatureFlag() = intent<Intent.Load>()
        .switchMap { abRepository.get().isFeatureEnabled(FEATURE_CUSTOMER_SUPPLIER_SCREEN_TXN_SORT_SETTING) }
        .map { PartialState.SetShowTransactionSortSelection(it) }

    private fun observeSortSelection() = intent<Intent.OnUpdateSortSelection>()
        .flatMap {
            wrap {
                val sortSelection = SupplierScreenSortSelection.convertToSortBy(it.sortSelection)
                setSupplierScreenSortSelection.get().execute(sortSelection)
                reload.onNext(Unit)
            }
        }
        .map { PartialState.NoChange }

    private fun observePreNetworkOnboardingWarningBanner() = intent<Intent.Load>()
        .switchMap { wrap(rxSingle { canShowPreNetworkOnboardingBanner.get().execute(supplierId) }) }
        .map {
            when (it) {
                is Result.Success -> {
                    if (it.value.first) {
                        tracker.get().trackEntryPointViewed(
                            source = "Supplier Relationship",
                            type = "Alert",
                            name = "Last Activity",
                            value = it.value.second
                        )
                    }
                    PartialState.SetPreNetworkOnboarding(it.value.first)
                }
                else -> PartialState.NoChange
            }
        }

    private fun observeContextualHelpIdsOnLoad() = intent<Intent.Load>().switchMap {
        wrap(getContextualHelpIds.get().execute(ScreenName.SupplierScreen.value))
    }.map {
        if (it is Result.Success) {
            return@map PartialState.SetContextualHelpIds(it.value)
        }
        PartialState.NoChange
    }

    private fun showCashbackMessageIfAvailable(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { isSupplierCashbackFeatureEnabled.get().execute() }
            .switchMap { isEnabled ->
                if (isEnabled) {
                    return@switchMap wrap(
                        getCashbackMessageDetails.get().execute()
                            .map { cashbackMessageDetails ->

                                val cashbackMessageType =
                                    if (cashbackMessageDetails.isFirstTransaction) FIRST else REGULAR
                                tracker.get().trackPayOnlineCashbackPageView(
                                    accountId = supplierId,
                                    screen = RELATIONSHIP,
                                    type = ONLINE_PAYMENT,
                                    relation = SUPPLIER,
                                    cashbackMessageType = cashbackMessageType,
                                    cashbackAmount = cashbackMessageDetails.cashbackAmount.toString(),
                                    minimumPaymentAmount = cashbackMessageDetails.minimumPaymentAmount.toString(),
                                )

                                getCashbackMessageDetails.get().getHumanReadableStringFromModel(cashbackMessageDetails)
                            }
                    )
                } else {
                    tracker.get().trackPayOnlineCashbackPageView(
                        accountId = supplierId,
                        screen = RELATIONSHIP,
                        type = ONLINE_PAYMENT,
                        relation = SUPPLIER,
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

    private fun goToDeletedTransaction(): Observable<PartialState> {
        return intent<Intent.GoToDeletedTransaction>()
            .sample(2, TimeUnit.SECONDS)
            .map {
                emitViewEvent(ViewEvent.GotoDeletedTransaction(txnId))
                PartialState.NoChange
            }
    }

    private fun showNewReport(): ObservableSource<UiState.Partial<State>> {
        return intent<Intent.NewShareReport>()
            .map {
                emitViewEvent(ViewEvent.GoToSupplierReport)
                PartialState.NoChange
            }
    }

    private fun getChatEducation(): ObservableSource<UiState.Partial<State>>? {
        // load call inapp tutorial visibility
        return Observable.timer(4, TimeUnit.SECONDS)
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
        return Observable.timer(6, TimeUnit.SECONDS)
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

    private fun isMerchantFromCollectionCampaignObservable(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap { UseCase.wrapObservable(isMerchantFromCollectionCampaign.execute()) }
            .map {
                when (it) {
                    is Result.Progress -> {
                        PartialState.NoChange
                    }
                    is Result.Success -> {
                        if (it.value) {
                            isCollectionCampaignMerchant = true
                            showPayOnlineEducationPublishSubject.onNext(Unit)
                        }
                        PartialState.IsMerchantFromCollectionCampaign(it.value)
                    }
                    is Result.Failure -> {
                        PartialState.NoChange
                    }
                }
            }
    }

    private fun canShowPayOnlineEducationObservable(): Observable<PartialState>? {
        return showPayOnlineEducationPublishSubject
            .switchMap {
                wrap(
                    defaultPreferences.get()
                        .getBoolean(RxSharedPrefValues.IS_PAY_ONLINE_EDUCATION_SHOWN_FOR_CAMPAIGN, Scope.Individual)
                        .asObservable().firstOrError()
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> PartialState.IsPayOnlineEducationShown(it.value)
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun isBlindPayEnabledObservable(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { wrap(abRepository.get().isFeatureEnabled(Features.BLIND_PAY)) }
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
        return intent<Intent.GetBlindPayLinkId>()
            .switchMap {
                UseCase.wrapSingle(
                    getBlindPayLinkId.get().execute(supplierId)
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

    private fun payOnlineEducationObservable(): ObservableSource<UiState.Partial<State>>? {
        return intent<Intent.ShowPayOnlineEducation>()
            .take(1)
            .switchMap {
                UseCase.wrapSingle(
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

    private fun getSupplierCollectionProfileObservable(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { UseCase.wrapObservable(getSupplierCollectionProfileWithSync.get().execute(supplierId, true)) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        val flow = getFlow()
                        val destination = it.value.paymentAddress.isNullOrBlank().not() &&
                            it.value.message_link.isNullOrBlank().not()

                        tracker.get().trackEvents(
                            Event.LOAD_PAYMENT_DESTINATION,
                            relation = PropertyValue.SUPPLIER,
                            propertiesMap = PropertiesMap.create()
                                .add(PropertyKey.ACCOUNT_ID, supplierId)
                                .add(PropertyKey.FLOW, flow)
                                .add(PropertyKey.DESTINATION, destination)
                        )
                        supplierCollectionCustomerProfile = it.value
                        PartialState.SetCollectionCustomerProfile(it.value)
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun getCollectionProfileAfterSetDestination(): Observable<PartialState>? {
        return intent<Intent.GetCollectionProfileAfterSetDestination>()
            .switchMap {
                UseCase.wrapObservable(getSupplierCollectionProfileWithSync.get().execute(supplierId, false))
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.SetPayOnlineLoading(true, riskCategory)
                    is Result.Success -> {
                        supplierCollectionCustomerProfile = it.value
                        pushIntent(Intent.PayOnline)
                        PartialState.SetCollectionCustomerProfile(it.value)
                    }
                    is Result.Failure -> {
                        showAlertPublishSubject.onNext(context.get().getString(R.string.supplier_other_error))
                        PartialState.SetPayOnlineLoading(false, riskCategory)
                    }
                }
            }
    }

    private fun getFlow(): String {
        return if (isCollectionCampaignMerchant) {
            AppConstants.PAYMENT_INSTALL_LINK_UTM_CAMPAIGN
        } else {
            ""
        }
    }

    private fun getRiskDetails(): Observable<PartialState> {
        return intent<Intent.GetRiskDetailsResponse>()
            .switchMap {
                wrap(getRiskDetails.get().execute("supplier_collection", "APP"))
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.SetPayOnlineLoading(true, riskCategory)
                    is Result.Success -> {
                        riskCategory = it.value.riskCategory
                        juspayPaymentInstrument = it.value.paymentInstruments.first { it.instrumentName == "juspay" }
                        futureLimit = it.value.futureLimit

                        val isJuspayPaymentInstrumentEnabled = juspayPaymentInstrument?.enabled
                            ?: false
                        val dailyLimitReached = juspayPaymentInstrument?.limitInfo?.dailyLimitReached
                            ?: false
                        val remainingAmountLimit = juspayPaymentInstrument?.limitInfo?.remainingDailyAmountLimit
                            ?: 0L
                        val dailyMaxAmountLimit = juspayPaymentInstrument?.limitInfo?.totalDailyAmountLimit
                            ?: 0L

                        val kycStatus = KycStatus.valueOf(it.value.kycInfo.kycStatus)

                        if (!dailyLimitReached) {
                            return@map dailyLimitNotReached(isJuspayPaymentInstrumentEnabled, kycStatus)
                        } else {
                            setPopUpDisplayWhenAmountLimitReached(
                                remainingAmountLimit,
                                dailyMaxAmountLimit
                            )
                            PartialState.SetPayOnlineLoading(false, riskCategory)
                        }
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(ViewEvent.GotoLogin)
                            }
                            isInternetIssue(it.error) -> {
                                showAlertPublishSubject.onNext(context.get().getString(R.string.no_internet_msg))
                            }
                            else -> {
                                supplierAnalyticsEvents.get()
                                    .trackSupplierTxnPageApiError(
                                        supplierId, it.error.message ?: "", RISK_API,
                                        SUPPLIER_SCREEN
                                    )
                                showAlertPublishSubject.onNext(context.get().getString(R.string.supplier_other_error))
                            }
                        }
                        PartialState.SetPayOnlineLoading(false, riskCategory)
                    }
                }
            }
    }

    private fun dailyLimitNotReached(
        isJuspayPaymentInstrumentEnabled: Boolean,
        kycStatus: KycStatus,
    ): PartialState {
        if (isJuspayPaymentInstrumentEnabled) {

            if (getCurrentState().isBlindPayEnabled && supplierCollectionCustomerProfile?.paymentAddress.isNullOrBlank()) {
                emitViewEvent(
                    ViewEvent.GotoSupplierBlindPayEditAmountScreen(
                        supplierId,
                        supplier?.balance ?: 0L,
                        juspayPaymentInstrument?.limitInfo?.remainingDailyAmountLimit ?: 0L,
                        juspayPaymentInstrument?.limitInfo?.totalDailyAmountLimit ?: 0L,
                        riskCategory,
                    )
                )
                return PartialState.SetPayOnlineLoading(false, riskCategory)
            } else {
                pushIntent(Intent.GotoSupplierEditAmountScreen(kycStatus))
            }
            return PartialState.NoChange
        } else {
            emitViewEvent(ViewEvent.ShowSupplierDestinationDialog(supplierId))
            return PartialState.SetPayOnlineLoading(false, riskCategory)
        }
    }

    private fun setPopUpDisplayWhenAmountLimitReached(remainingAmountLimit: Long, dailyMaxAmountLimit: Long) {
        supplierAnalyticsEvents.get().trackSupplierPaymentLimitPopDisplayed(
            accountId = supplierId,
            relation = SUPPLIER,
            screen = SUPPLIER_SCREEN,
            flow = JUSPAY_SUPPLIER_COLLECTION,
            dueAmount = supplier?.balance.toString(),
            type = LIMIT_EXCEEDED,
            userTxnLimit = dailyMaxAmountLimit.toString(),
            availTxnLimit = remainingAmountLimit.toString(),
            txnType = TXN_AMOUNT
        )
        emitViewEvent(
            ViewEvent.OpenLimitReachedBottomSheet
        )
    }

    private fun payOnline(): Observable<PartialState> {
        return intent<Intent.PayOnline>()
            .switchMap {
                wrap(
                    if (supplierCollectionCustomerProfile?.paymentAddress.isNullOrBlank()) {
                        getSupplierCollectionProfileWithSync.get().execute(supplierId, async = false).firstOrError()
                    } else {
                        Single.just(supplierCollectionCustomerProfile)
                    }
                )
            }
            .map {
                return@map when (it) {
                    is Result.Success -> {
                        this.supplierCollectionCustomerProfile = it.value
                        supplierAnalyticsEvents.get().trackSupplierOnlinePaymentClick(
                            accountId = supplierId,
                            dueAmount = initialState.supplier?.balance.toString(),
                            screen = SUPPLIER_SCREEN,
                            relation = SUPPLIER,
                            riskType = initialState.riskType,
                            isCashbackMessageVisible = getCurrentState().cashbackMessage.isNotNullOrBlank()
                        )
                        if (!supplierCollectionCustomerProfile?.paymentAddress.isNullOrBlank()) {
                            pushIntent((Intent.IsJuspayFeatureEnabled))
                        } else if (getCurrentState().isJustPayEnabled &&
                            getCurrentState().isBlindPayEnabled &&
                            getCurrentState().supplier?.mobile.isNotNullOrBlank()
                        ) {
                            pushIntent(Intent.GetBlindPayLinkId)
                        } else {
                            emitViewEvent(ViewEvent.ShowAddPaymentMethodDialog(supplierId))
                        }
                        PartialState.SetPayOnlineLoading(false, riskCategory)
                    }
                    is Result.Progress -> PartialState.SetPayOnlineLoading(true, riskCategory)
                    is Result.Failure -> PartialState.SetPayOnlineLoading(false, riskCategory)
                }
            }
    }

    private fun gotoSupplierEditAmountScreen(): Observable<PartialState> {
        return intent<Intent.GotoSupplierEditAmountScreen>()
            .map { data ->
                emitViewEvent(
                    ViewEvent.GotoSupplierEditAmountScreen(
                        supplierId,
                        supplier?.balance ?: 0L,
                        juspayPaymentInstrument?.limitInfo?.remainingDailyAmountLimit ?: 0L,
                        juspayPaymentInstrument?.limitInfo?.totalDailyAmountLimit ?: 0L,
                        riskCategory,
                        data.kycStatus,
                        KycRiskCategory.NO_RISK,
                        futureLimit?.totalAmountLimit ?: 0L
                    )
                )
                PartialState.SetPayOnlineLoading(false, riskCategory)
            }
    }

    private fun isJuspayFeatureEnabled(): Observable<PartialState> {
        return intent<Intent.IsJuspayFeatureEnabled>()
            .switchMap { UseCase.wrapObservable(abRepository.get().isFeatureEnabled(SUPPLIER_JUSPAY_FEATURE)) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        if (it.value) {
                            pushIntent(Intent.GetRiskDetailsResponse)
                        } else {
                            emitViewEvent(
                                ViewEvent.ShowSupplierDestinationDialog(
                                    supplierId
                                )
                            )
                        }
                        PartialState.NoChange
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun isJustPayFeatureEnabledForSupplierObserver(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { UseCase.wrapObservable(abRepository.get().isFeatureEnabled(SUPPLIER_JUSPAY_FEATURE)) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        PartialState.SetJustPayEnabled(it.value)
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun syncListCollectionFromServer(): Observable<PartialState> {
        return intent<Intent.Reload>()
            .switchMap {
                getSupplierCollection.get().execute(supplierId)
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        reload.onNext(Unit)
                        PartialState.NoChange
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun setOngoingPaymentAttributes(): Observable<PartialState> {
        return intent<Intent.SetOngoingPaymentAttribute>()
            .map {
                PartialState.SetOngoingPaymentAttribute(
                    it.ongoingPaymentAmount,
                    it.ongoingPaymentId,
                    it.ongoingPaymentType
                )
            }
    }

    private fun getCustomerSupportType(): Observable<PartialState> {
        return intent<SupplierContract.Intent.Load>()
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

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        val tempState = when (partialState) {
            is PartialState.ShowLoading -> currentState.copy(isLoading = true)
            // This is causing unnecessary rendering of frame if we use ViewEvent
            is PartialState.SetBusiness -> currentState.copy(business = partialState.business)
            is PartialState.ShowData -> currentState.copy(
                isLoading = false,
                transactions = partialState.supplierTransactionsWrapperImpl,
                supplier = partialState.supplier,
                playSound = partialState.playSound,
                isTxnExpanded = expandTransaction,
                isBlocked = partialState.supplier.state == Supplier.BLOCKED,
                lastZeroBalanceIndex = partialState.lastIndexOfZeroBalanceDue,
                transactionSortSelection = partialState.transactionSortSelection,
            )
            is PartialState.ErrorState -> currentState.copy(isLoading = false, error = true)
            is PartialState.SetNetworkError -> currentState.copy(networkError = partialState.networkError)
            is PartialState.StopMediaPlayer -> currentState.copy(playSound = false)
            is PartialState.ExpandTransactions -> currentState.copy(isTxnExpanded = true)
            is PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is PartialState.NoChange -> currentState
            is PartialState.ShowTakeCreditPaymentEducation -> currentState.copy(
                showTakeCreditPaymentEducation = partialState.showTakeCreditPaymentEducation
            )
            is PartialState.ShowSupplierStatementLoader -> currentState.copy(
                showSupplierStatementLoader = partialState.showSupplierStatementLoader
            )

            is PartialState.ShowUnblockDialog -> currentState.copy(showUnblockDialog = true)
            is PartialState.SetBlockState -> currentState.copy(
                isLoading = false,
                isBlocked = partialState.isBlocked
            )
            is PartialState.ShowAppPromotion -> currentState.copy(canShowAppPromotion = partialState.canShowAppPromotion)
            is PartialState.SetCollectionCustomerProfile -> currentState.copy(
                collectionCustomerProfile = partialState.collectionCustomerProfile
            )
            is PartialState.IsMerchantFromCollectionCampaign -> currentState.copy(
                isMerchantFromCollectionCampaign = partialState.isMerchantFromCollectionCampaign
            )
            is PartialState.IsPayOnlineEducationShown -> currentState.copy(
                isPayOnlineEducationShown = partialState.isPayOnlineEducationShown
            )
            is PartialState.SetUnreadMessageCount -> currentState.copy(
                unreadMessageCount = partialState.unreadMessageCount,
                firstUnseenMessageId = partialState.firstUnseenMessageId
            )
            is PartialState.CanShowChatNewSticker -> currentState.copy(canShowChatNewSticker = partialState.canShowChatNewSticker)
            is PartialState.SetChatStatus -> currentState.copy(isChatEnabled = partialState.isChatEnabled)
            is PartialState.CanShowBillNewSticker -> currentState.copy(canShowBillNewSticker = partialState.canShowBillNewSticker)
            is PartialState.SetBillStatus -> currentState.copy(isBillEnabled = partialState.isBillEnabled)
            is PartialState.SetBillUnseenCount -> currentState.copy(setBillUnseenCount = partialState.setBillUnseenCount)
            is PartialState.SetTotalAndUnseenBills -> currentState.copy(
                totalBills = partialState.totalBills, unreadBillCount = partialState.unseenBills
            )
            is PartialState.SetAccountUnseenBills -> currentState.copy(
                unreadBillCount = partialState.unreadBillCount
            )
            is PartialState.SetPayOnlineLoading -> currentState.copy(
                showPayOnlineButtonLoader = partialState.loading,
                riskType = partialState.riskType
            )
            is PartialState.SetOngoingPaymentAttribute -> currentState.copy(
                ongoingPaymentAmount = partialState.ongoingPaymentAmount,
                ongoingPaymentId = partialState.ongoingPaymentId,
                ongoingPaymentType = partialState.ongoingPaymentType
            )
            is PartialState.SetCashbackMessage -> currentState.copy(
                cashbackMessage = partialState.cashbackMessage
            )
            is PartialState.SetContextualHelpIds -> currentState.copy(contextualHelpIds = partialState.helpIds)
            is PartialState.SetBlindPayEnabled -> currentState.copy(isBlindPayEnabled = partialState.isBlindPayEnabled)
            is PartialState.SetBlindPayLinkId -> currentState.copy(blindPayLinkId = partialState.blindPayLinkId)
            is PartialState.SetJustPayEnabled -> currentState.copy(isJustPayEnabled = partialState.isJustPayEnabled)
            is PartialState.SetSupportType -> currentState.copy(supportType = partialState.type)
            is PartialState.SetPreNetworkOnboarding -> currentState.copy(
                showPreNetworkWarningBanner = partialState.canShow
            )
            is PartialState.SetShowTransactionSortSelection -> currentState.copy(
                showTransactionSortSelection = partialState.canShow
            )
        }

        return tempState.copy(
            supplierScreenList = prepareSupplierScreenTransactionList(tempState)
        )
    }

    private fun prepareSupplierScreenTransactionList(state: State): List<SupplierScreenItem> {
        val list = mutableListOf<SupplierScreenItem>()
        if (state.isLoading) {
            list.add(SupplierScreenItem.LoadingItem)
            return list
        }

        if (state.transactions.isEmpty()) {
            list.add(SupplierScreenItem.EmptyPlaceHolder(state.supplier?.name ?: ""))
            return list
        }

        if (!state.isTxnExpanded && state.lastZeroBalanceIndex > 0) {
            list.add(SupplierScreenItem.LoadMoreItem)
        }
        var lastTransactionDate = ""
        val startIndex = if (!state.isTxnExpanded && state.lastZeroBalanceIndex > 0) {
            state.lastZeroBalanceIndex + 1
        } else {
            0
        }
        for (index in startIndex until state.transactions.size) {
            val txnWrapper = state.transactions[index]
            val currentTransactionDate = if (transactionsSortSelection == CREATE_DATE) {
                DateTimeUtils.formatTx(txnWrapper.transaction.createTime, context.get())
            } else {
                DateTimeUtils.formatTx(txnWrapper.transaction.billDate, context.get())
            }
            // add date only if current txn date is not same as last txn date
            if (currentTransactionDate != lastTransactionDate) {
                lastTransactionDate = currentTransactionDate
                list.add(SupplierScreenItem.DateItem(currentTransactionDate))
            }

            when {
                transactionDeleted(txnWrapper) -> {
                    list.add(createDeletedTransaction(state, txnWrapper))
                }
                transactionProcessing(txnWrapper) -> {
                    list.add(createProcessingTransaction(txnWrapper))
                }
                else -> {
                    list.add(createTransactionItem(state, txnWrapper))
                }
            }
        }

        return list.toList()
    }

    private fun isBilledDateSortSelected() = transactionsSortSelection == BILL_DATE

    private fun transactionProcessing(txnWrapper: GetSupplierStatement.TransactionWrapper): Boolean {
        if (txnWrapper.transaction.isOnlineTransaction()) {
            val collectionStatus = txnWrapper.collection?.status

            return collectionStatus == CollectionStatus.PAID ||
                collectionStatus == CollectionStatus.PAYOUT_INITIATED ||
                collectionStatus == CollectionStatus.PAYOUT_FAILED
        }
        return false
    }

    private fun transactionDeleted(txnWrapper: GetSupplierStatement.TransactionWrapper): Boolean {

        if (txnWrapper.transaction.isOnlineTransaction()) {
            return txnWrapper.collection?.status == CollectionStatus.REFUNDED ||
                txnWrapper.collection?.status == CollectionStatus.REFUND_INITIATED ||
                txnWrapper.collection?.status == CollectionStatus.FAILED
        }
        return txnWrapper.transaction.deleted
    }

    private fun createDeletedTransaction(
        state: State,
        txnWrapper: GetSupplierStatement.TransactionWrapper,
    ): SupplierScreenItem.DeletedTransaction {
        val collectionStatus = if (txnWrapper.transaction.isOnlineTransaction()) {
            txnWrapper.collection?.status
        } else {
            null
        }

        val blindPay = txnWrapper.collection?.blindPay ?: false

        return SupplierScreenItem.DeletedTransaction(
            id = txnWrapper.transaction.id,
            txnGravity = findUiTxnGravity(txnWrapper.transaction.payment, LedgerType.SUPPLIER),
            isDirty = txnWrapper.transaction.syncing.not(),
            isDeletedBySupplier = txnWrapper.transaction.deletedBySupplier,
            amount = txnWrapper.transaction.amount,
            date = findFormattedDateOrTime(
                txnWrapper.transaction.createTime,
                txnWrapper.transaction.billDate,
                isBilledDateSortSelected()
            ),
            currentBalance = txnWrapper.currentDue,
            onlineTxn = txnWrapper.transaction.isOnlineTransaction(),
            supplierName = state.supplier?.name,
            collectionStatus = collectionStatus,
            isBlindPay = blindPay,
            dateTime = DateTimeUtils.formatLong(txnWrapper.transaction.billDate),
            shouldShowHelpOption = isSevenDaysPassed(txnWrapper.transaction.billDate).not(),
            accountId = supplierId,
            supportType = getCurrentState().supportType.value,
            paymentId = txnWrapper.collection?.paymentId ?: "",
        )
    }

    private fun createProcessingTransaction(
        txnWrapper: GetSupplierStatement.TransactionWrapper,
    ): SupplierScreenItem.ProcessingTransaction {

        return SupplierScreenItem.ProcessingTransaction(
            txnId = txnWrapper.transaction.id,
            amount = txnWrapper.transaction.amount,
            isBlindPay = txnWrapper.transaction.blindPay,
            payment = txnWrapper.transaction.payment,
            billDate = txnWrapper.transaction.billDate,
            createTime = txnWrapper.transaction.createTime,
            currentDue = txnWrapper.currentDue,
            shouldShowHelpOption = isSevenDaysPassed(txnWrapper.transaction.billDate).not(),
            accountId = supplierId,
            supportType = getCurrentState().supportType.value,
            paymentId = txnWrapper.collection?.paymentId ?: "",
        )
    }

    private fun createTransactionItem(
        state: State,
        txnWrapper: GetSupplierStatement.TransactionWrapper,
    ): SupplierScreenItem.TransactionItem {
        return SupplierScreenItem.TransactionItem(
            txnId = txnWrapper.transaction.id,
            payment = txnWrapper.transaction.payment,
            amount = txnWrapper.transaction.amount,
            note = txnWrapper.transaction.note,
            date = findFormattedDateOrTime(
                txnWrapper.transaction.createTime,
                txnWrapper.transaction.billDate,
                isBilledDateSortSelected()
            ),
            syncing = txnWrapper.transaction.syncing,
            receiptUrl = txnWrapper.transaction.receiptUrl,
            finalReceiptUrl = txnWrapper.transaction.finalReceiptUrl,
            isOnlineTxn = txnWrapper.transaction.isOnlineTransaction(),
            createdBySupplier = txnWrapper.transaction.createdBySupplier,
            currentDue = txnWrapper.currentDue,
            supplierName = state.supplier?.name,
            createTime = txnWrapper.transaction.createTime,
        )
    }

    // comparing latest txn with unsync list for checking playing sound
    private fun isPlaySound(transactions: List<GetSupplierStatement.TransactionWrapper>): Boolean {
        var isPlaySound = false

        unSyncTransactions.forEach { unSynced ->

            transactions.forEach {
                val amount = it.transaction.amount
                val sameTxn = it.transaction.createTime == unSynced.createTime
                val nowSynced = it.transaction.syncing

                if (sameTxn && nowSynced) {
                    isPlaySound = true
                }
            }
        }

        saveUnSyncTransactions(transactions)
        return isPlaySound
    }

    // Saving unSyncTransactions for playing sync sound
    private fun saveUnSyncTransactions(transactions: List<GetSupplierStatement.TransactionWrapper>) {
        unSyncTransactions.clear()

        transactions.forEach {
            if (!it.transaction.syncing) {
                unSyncTransactions.add(it.transaction)
            }
        }
    }
}
