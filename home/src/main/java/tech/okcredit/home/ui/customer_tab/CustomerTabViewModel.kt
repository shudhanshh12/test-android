package tech.okcredit.home.ui.customer_tab

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.model.CustomerLastActivity
import `in`.okcredit.backend._offline.usecase.GetUnSyncedCustomers
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.Features
import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.backend.contract.RxSharedPrefValues.PAYMENT_REMINDER_EDUCATION_SHOWN
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.*
import `in`.okcredit.customer.contract.BulkReminderAnalytics
import `in`.okcredit.customer.contract.GetBannerForBulkReminder
import `in`.okcredit.dynamicview.Targets
import `in`.okcredit.home.GetSupplierCreditEnabledCustomerIds
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.CLEAN
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.IMMUTABLE
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import `in`.okcredit.referral.contract.RewardsOnSignupTracker
import `in`.okcredit.referral.contract.usecase.CloseReferralTargetBanner
import `in`.okcredit.referral.contract.usecase.GetReferralTarget
import `in`.okcredit.referral.contract.usecase.TransactionInitiated
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.referral_views.model.Place
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.utils.AbFeatures
import `in`.okcredit.supplier.usecase.SyncSupplierEnabledCustomerIdsImpl
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.Optional
import tech.okcredit.home.R
import tech.okcredit.home.ui.customer_tab.CustomerTabContract.*
import tech.okcredit.home.ui.customer_tab.CustomerTabItem.*
import tech.okcredit.home.ui.homesearch.Sort
import tech.okcredit.home.usecase.GetActiveCustomers
import tech.okcredit.home.usecase.GetAppLockInAppVisibility
import tech.okcredit.home.usecase.GetCustomization
import tech.okcredit.home.usecase.GetHomeCustomerTabSortSelection
import tech.okcredit.home.usecase.SetHomeCustomerTabSortSelection
import tech.okcredit.home.usecase.UserStoryScheduleSyncIfEnabled
import tech.okcredit.home.utils.LifeCycle
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

class CustomerTabViewModel @Inject constructor(
    initialState: Lazy<State>,
    private val context: Lazy<Context>,
    private val getActiveCustomers: Lazy<GetActiveCustomers>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val defaultPreferences: Lazy<DefaultPreferences>,
    private val checkLiveSalesActive: Lazy<CheckLiveSalesActive>,
    private val getUnSyncedCustomers: Lazy<GetUnSyncedCustomers>,
    private val getCustomerCollectionProfile: Lazy<GetCustomerCollectionProfile>,
    private val tracker: Lazy<Tracker>,
    private val getAppLockInAppVisibility: Lazy<GetAppLockInAppVisibility>,
    private val getSupplierCreditEnabledCustomerIds: Lazy<GetSupplierCreditEnabledCustomerIds>,
    private val ab: Lazy<AbRepository>,
    private val onboardingPreferences: Lazy<OnboardingPreferences>,
    private val getCustomization: Lazy<GetCustomization>,
    private val getReferralTarget: Lazy<GetReferralTarget>,
    private val closeReferralTargetBanner: Lazy<CloseReferralTargetBanner>,
    private val transactionInitiated: Lazy<TransactionInitiated>,
    private val syncSupplierEnabledCustomerIds: Lazy<SyncSupplierEnabledCustomerIdsImpl>,
    private val getKycRiskCategory: Lazy<GetKycRiskCategory>,
    private val userStoryScheduleSyncIfEnabled: Lazy<UserStoryScheduleSyncIfEnabled>,
    private val fetchPaymentTargetedReferral: Lazy<FetchPaymentTargetedReferral>,
    private val collectionEventTracker: Lazy<CollectionEventTracker>,
    private val getTargetedReferralList: Lazy<GetTargetedReferralList>,
    private val getBannerForBulkReminder: Lazy<GetBannerForBulkReminder>,
    private val bulkReminderAnalytics: Lazy<BulkReminderAnalytics>,
    private val setHomeCustomerTabSortSelection: Lazy<SetHomeCustomerTabSortSelection>,
    private val getHomeCustomerTabSortSelection: Lazy<GetHomeCustomerTabSortSelection>,
    private val referralOnSignupTracker: Lazy<RewardsOnSignupTracker>,
) : BaseViewModel<State, PartialState, CustomerTabViewEvent>(initialState.get()) {

    private var newSortAndSearchQuerySubject = PublishSubject.create<Triple<ArrayList<String>, String, String>>()
    private val showAlertPublishSubject: PublishSubject<String> = PublishSubject.create()
    private var business: Business? = null

    // need to track giftIconShown for analytics purpose
    private var giftIconShown: Boolean = false

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(

            /*********************** Load Customers ***********************/
            loadIntents(),

            Observable.merge(
                intent<Intent.Load>()
                    .switchMapSingle { getHomeCustomerTabSortSelection.get().execute() }
                    .map { sortBy -> Triple(ArrayList(Sort.sortfilter), sortBy, "") },
                newSortAndSearchQuerySubject
                    .throttleLatest(300, TimeUnit.MILLISECONDS) // for handling backpressure
            ).switchMap { getCustomerListWithSortAndSearchQuery(it) },

            getReferralTarget(),

            /*********************** Show App Lock In app Notification ***********************/
            intent<Intent.Load>()
                .take(1)
                .switchMap { getAppLockInAppVisibility.get().execute(Unit) }
                .map {
                    when (it) {
                        is Result.Success -> {
                            if (it.value) {
                                tracker.get().trackAppLockCardDisplayed(PropertyValue.HOME_PAGE, PropertyValue.CARD)
                            }
                            PartialState.SetAppLockInAppVisibility(it.value)
                        }
                        else -> PartialState.NoChange
                    }
                },

            /*********************** Load Livasales feature status ***********************/
            intent<Intent.Load>()
                .switchMap { wrap(checkLiveSalesActive.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.SetLiveSaleActiveStatus(it.value)
                        }
                        is Result.Failure -> {
                            PartialState.NoChange
                        }
                    }
                },

            /*********************** Load supplier credit enabled meta info ***********************/
            intent<Intent.Load>()
                .switchMap { ab.get().isFeatureEnabled(AbFeatures.SINGLE_LIST) }
                .filter { it }
                .switchMap { wrap(syncSupplierEnabledCustomerIds.get().execute()) }
                .map {
                    PartialState.NoChange
                },

            intent<Intent.Load>()
                .switchMap { wrap(getActiveBusiness.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            business = it.value
                            PartialState.SetBusiness(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> PartialState.NoChange

                                else -> {
                                    Timber.e(it.error, "ErrorState")
                                    PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            /***********************   Load UnSynced Customers  ***********************/
            intent<Intent.Load>()
                .switchMap { wrap(getUnSyncedCustomers.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.SetUnSyncCustomers(it.value)
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    emitViewEvent(CustomerTabViewEvent.GotoLogin)
                                    PartialState.NoChange
                                }
                                else -> {
                                    PartialState.NoChange
                                }
                            }
                        }
                    }
                },

            /*********************** Load Collection Profile ***********************/
            intent<Intent.Load>()
                .switchMap { wrap(collectionRepository.get().isCollectionActivated()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.SetCollectionActivatedStatus(it.value)
                        }
                        is Result.Failure -> {
                            PartialState.NoChange
                        }
                    }
                },

            /*********************** Loading livesales tutorial visibility ***********************/
            intent<Intent.Load>()
                .switchMap {
                    wrap(
                        defaultPreferences.get()
                            .getBoolean(RxSharedPrefValues.SHOULD_SHOW_LIVE_SALES_TUTORIAL, Scope.Individual)
                            .asObservable().firstOrError()
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            if (it.value) {
                                PartialState.SetLiveSalesTutorialVisibility(true)
                            } else {
                                PartialState.NoChange
                            }
                        }
                        is Result.Failure -> {
                            PartialState.NoChange
                        }
                    }
                },

            /***********************  Get SupplierCreditEnabled CustomerIds ***********************/
            intent<Intent.Load>()
                .switchMap { wrap(getSupplierCreditEnabledCustomerIds.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.SetSupplierCreditEnabledCustomerIds(it.value)
                        }
                        is Result.Failure -> {
                            PartialState.NoChange
                        }
                    }
                },

            intent<Intent.Load>()
                .switchMap {
                    wrap(
                        defaultPreferences.get().getBoolean(PAYMENT_REMINDER_EDUCATION_SHOWN, Scope.Individual, true)
                            .asObservable().firstOrError()
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            if (it.value) {
                                PartialState.SetPaymentReminderEducationShown(it.value)
                            } else {
                                PartialState.NoChange
                            }
                        }
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            intent<Intent.SetNewSort>()
                .switchMap {
                    newSortAndSearchQuerySubject.onNext(Triple(ArrayList(it.sort.sortfilter), it.sort.sortBy, ""))
                    wrap { setHomeCustomerTabSortSelection.get().execute(it.sort.sortBy) }
                }
                .map {
                    PartialState.NoChange
                },

            intent<Intent.LiveSaleClicked>()
                .map {
                    emitViewEvent(CustomerTabViewEvent.GoLoLiveSalesScreen(it.customerId))
                    PartialState.NoChange
                },

            showAlertPublishSubject
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<PartialState> { PartialState.HideAlert }
                        .startWith(PartialState.ShowAlert(it))
                },

            intent<Intent.AppLockInAppCancelled>()
                .flatMap { wrap(rxCompletable { onboardingPreferences.get().setInAppLockCancelled(true) }) }
                .map {
                    PartialState.SetAppLockInAppVisibility(false)
                },
            isLiveSaleQrSelected(),
            isBannerCustomizationEnabled(),
            loadBannerCustomization(),
            hideReferralTargetBanner(),
            closeReferralTargetBanner(),
            isProfilePicClickable(),
            getKycRiskCategory(),
            observeLiveSalesTutorialShown(),
            isUserStoriesEnabled(),
            getPaymentTargetedReferral(),
            fetchPaymentTargetedReferral(),
            getHomeBannerForBulkReminder(),
            observeTrackEntryPointViewed(),
        )
    }

    private fun getCustomerListWithSortAndSearchQuery(
        sortAndSearchQuery: Triple<java.util.ArrayList<String>, String, String>,
    ): Observable<PartialState> {
        Sort.sortBy = sortAndSearchQuery.second
        return getActiveCustomers.get().execute(GetActiveCustomers.Request(null, sortAndSearchQuery))
            .map {
                when (it) {
                    is Result.Progress -> {
                        PartialState.NoChange
                    }
                    is Result.Success -> {
                        PartialState.ShowCustomerTabDetails(
                            customer = it.value.customers,
                            searchQuery = it.value.searchQuery,
                            tabCount = it.value.tabCount,
                            liveSalesCustomer = it.value.liveSalesCustomer,
                            lifecycle = it.value.lifeCycle
                        )
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(CustomerTabViewEvent.GotoLogin)
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

    private fun loadIntents() = intent<Intent.Load>()
        .map {
            pushIntent(Intent.LoadBulkReminderBanner)
            PartialState.NoChange
        }

    private fun getHomeBannerForBulkReminder() = intent<Intent.LoadBulkReminderBanner>()
        .switchMap { wrap(getBannerForBulkReminder.get().execute().asObservable()) }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> {
                    if (it.value.canShowBanner) {
                        PartialState.SetBulkReminderState(
                            BulkReminderState(
                                totalBalanceDue = it.value.totalBalanceDue,
                                totalReminders = it.value.totalReminders,
                                showNotificationBadge = it.value.canShowNotificationIcon,
                                defaulterSince = it.value.defaulterSince
                            )
                        )
                    } else {
                        PartialState.SetBulkReminderState(null)
                    }
                }
                is Result.Failure -> PartialState.SetBulkReminderState(null)
            }
        }

    private fun observeLiveSalesTutorialShown(): Observable<PartialState> {
        return intent<Intent.LiveSalesTutorialShown>()
            .switchMap {
                wrap(
                    rxCompletable {
                        defaultPreferences.get()
                            .remove(RxSharedPrefValues.SHOULD_SHOW_LIVE_SALES_TUTORIAL, Scope.Individual)
                    }
                )
            }
            .map { PartialState.NoChange }
    }

    private fun isLiveSaleQrSelected(): Observable<PartialState> {
        return intent<Intent.LiveSaleQrSelected>()
            .switchMap { input ->
                wrap(
                    getCustomerCollectionProfile.get().execute(input.customer.id).firstOrError()
                        .map { profile ->
                            Pair(profile, input.customer)
                        }
                )
            }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        emitViewEvent(
                            CustomerTabViewEvent.GoToLiveSaleQRDialog(
                                it.value.first,
                                it.value.second,
                                business
                            )
                        )
                        PartialState.NoChange
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(CustomerTabViewEvent.GotoLogin)
                                PartialState.NoChange
                            }
                            else -> {
                                PartialState.NoChange
                            }
                        }
                    }
                }
            }
    }

    private fun loadBannerCustomization(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { wrap(getCustomization.get().execute(Targets.HOME_BANNER)) }
            .map {
                when (it) {
                    is Result.Success -> PartialState.HomeBannerCustomization(it.value)
                    else -> PartialState.NoChange
                }
            }
    }

    private fun isBannerCustomizationEnabled(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap { wrap(ab.get().isFeatureEnabled(Features.HOME_BANNER_CUSTOMIZATION)) }
            .map {
                when (it) {
                    is Result.Success -> PartialState.BannerCustomizationEnabled(it.value)
                    else -> PartialState.NoChange
                }
            }
    }

    private fun isProfilePicClickable(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { ab.get().isFeatureEnabled(AbFeatures.SINGLE_LIST) }
            .map {
                PartialState.IsProfilePicClickable(it.not())
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

    private fun getReferralTarget(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { wrap(getReferralTarget.get().execute(Place.HOME_SCREEN)) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        if (it.value is Optional.Present) {
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

    private fun getKycRiskCategory() = intent<Intent.Load>()
        .switchMap { wrap(getKycRiskCategory.get().execute()) }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> {
                    PartialState.SetKycRiskCategory(it.value.kycRiskCategory)
                }
                is Result.Failure -> {
                    PartialState.NoChange
                }
            }
        }

    private fun isUserStoriesEnabled(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap {
                wrap(userStoryScheduleSyncIfEnabled.get().execute())
            }
            .map {
                when (it) {
                    is Result.Success -> PartialState.IsUserStoriesEnabled(it.value)
                    else -> PartialState.NoChange
                }
            }
    }

    private fun getPaymentTargetedReferral() = intent<Intent.Load>()
        .switchMap {
            wrap(getTargetedReferralList.get().execute())
        }
        .map {
            if (it is Result.Success && it.value.isNotEmpty()) {
                PartialState.SetCustomerCollectionReferralInfo(it.value)
            } else {
                PartialState.NoChange
            }
        }

    private fun fetchPaymentTargetedReferral() = intent<Intent.Load>()
        .switchMap {
            wrap(fetchPaymentTargetedReferral.get().execute())
        }
        .map {
            PartialState.NoChange
        }

    private fun observeTrackEntryPointViewed() = intent<Intent.TrackEntryPointViewed>()
        .take(1)
        .map {
            bulkReminderAnalytics.get().trackEntryPointViewed()
            PartialState.NoChange
        }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        // this is done so that we can build HomeCustomerItem list from latest state
        val tempState = when (partialState) {
            is PartialState.ShowCustomerTabDetails -> {
                currentState.copy(
                    networkError = false, error = false,
                    customerTabDetails = GetActiveCustomers.Response(
                        customers = partialState.customer,
                        searchQuery = partialState.searchQuery,
                        tabCount = partialState.tabCount,
                        lifeCycle = partialState.lifecycle,
                        liveSalesCustomer = partialState.liveSalesCustomer
                    )
                )
            }
            is PartialState.SetLiveSaleActiveStatus -> currentState.copy(isLiveSalesActive = partialState.isLiveSalesActive)
            is PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is PartialState.ErrorState -> currentState.copy(error = true)
            is PartialState.SetNetworkError -> currentState.copy(
                error = false,
                networkError = true
            )
            is PartialState.SetBusiness -> currentState.copy(business = partialState.business)
            is PartialState.SetUnSyncCustomers -> currentState.copy(unSyncCustomerIds = partialState.customers)
            is PartialState.SetCollectionActivatedStatus -> currentState.copy(isCollectionActivated = partialState.status)
            is PartialState.SetLiveSalesTutorialVisibility -> currentState.copy(
                liveSalesTutorialVisibility = partialState.status
            )
            is PartialState.SetSupplierCreditEnabledCustomerIds -> currentState.copy(
                supplierCreditEnabledCustomerIds = partialState.customerIds
            )
            is PartialState.SetAppLockInAppVisibility -> currentState.copy(
                appLockInAppNotification = partialState.visibility
            )
            is PartialState.SetPaymentReminderEducationShown -> currentState.copy(
                paymentReminderEducationShown = partialState.paymentReminderEducationShown
            )
            is PartialState.NoChange -> currentState
            is PartialState.BannerCustomizationEnabled -> currentState.copy(
                isBannerCustomizationEnabled = partialState.enabled,
                showBannerCustomization = partialState.enabled
            )
            is PartialState.HomeBannerCustomization -> currentState.copy(
                bannerCustomization = partialState.customization
            )
            is PartialState.SetTargetBanner -> currentState.copy(
                referralTargetBanner = partialState.referralTargetBanner,
            )
            is PartialState.IsProfilePicClickable -> currentState.copy(
                isProfilePicClickable = partialState.isProfilePicClickable
            )
            is PartialState.SetKycRiskCategory -> currentState.copy(
                kycRiskCategory = partialState.kycRiskCategory
            )
            is PartialState.ShowTransactionSyncedIconEducation -> currentState.copy(
                showTransactionSyncedIconEducation = partialState.show
            )
            is PartialState.IsUserStoriesEnabled -> currentState.copy(
                userStoriesEnabled = partialState.userStoriesEnabled
            )
            is PartialState.SetCustomerCollectionReferralInfo -> currentState.copy(
                collectionCustomerReferralMap = partialState.list.associateBy { it.id }
            )
            is PartialState.SetBulkReminderState -> currentState.copy(
                bulkReminderState = partialState.bulkReminderState
            )
        }

        return tempState.copy(
            list = buildCustomerList(tempState)
        )
    }

    private fun buildCustomerList(state: State): List<CustomerTabItem> {
        val list = mutableListOf<CustomerTabItem>()

        // always user-stories should be on top
        addUserStoriesItem(state, list)
        if (state.customerTabDetails == null) {
            return list
        }

        addDynamicBanner(state, list)
        addBulkReminderBanner(state, list)
        addCustomerTrialItem(state, list)
        addReferralFullView(state, list)
        addReferralTargetBanner(state, list)
        addAppLockItem(state, list)
        addLiveSalesItem(state, list)
        addFilterItem(state, list)
        addCustomerListWithState(state, list)
        return list
    }

    private fun addBulkReminderBanner(state: State, list: MutableList<CustomerTabItem>) {
        if (state.bulkReminderState != null) {
            pushIntent(Intent.TrackEntryPointViewed)
            list.add(BulkReminderBanner(state.bulkReminderState))
        }
    }

    private fun addUserStoriesItem(state: State, list: MutableList<CustomerTabItem>) {
        if (state.userStoriesEnabled) {
            list.add(UserStoriesItem)
        }
    }

    private fun addCustomerTrialItem(state: State, list: MutableList<CustomerTabItem>) {
        if (state.customerTabDetails == null) return

        if (state.customerTabDetails.lifeCycle == LifeCycle.TRIAL_ADD_CUSTOMER ||
            state.customerTabDetails.lifeCycle == LifeCycle.SHOW_ADD_CUSTOMER_REWARD
        ) {
            if (state.referralTargetBanner == null) {
                list.add(AddCustomerTutorialItem)
            }
        }
    }

    private fun addReferralFullView(state: State, list: MutableList<CustomerTabItem>) {
        if (state.customerTabDetails == null) return

        if (state.customerTabDetails.lifeCycle == LifeCycle.TRIAL_ADD_CUSTOMER ||
            state.customerTabDetails.lifeCycle == LifeCycle.SHOW_ADD_CUSTOMER_REWARD
        ) {
            if (state.referralTargetBanner != null) {
                referralOnSignupTracker.get().trackFullBannerViewed()
                list.add(
                    ReferralTargetBannerItem(
                        fullView = true,
                        referralTargetBanner = state.referralTargetBanner,
                    )
                )
            }
        }
    }

    private fun addReferralTargetBanner(state: State, list: MutableList<CustomerTabItem>) {
        if (state.customerTabDetails?.lifeCycle != LifeCycle.NORMAL_FLOW) return

        if (state.referralTargetBanner != null &&
            !(state.showBannerCustomization && state.bannerCustomization != null)
        ) {
            referralOnSignupTracker.get().trackTargetBannerViewed()
            list.add(
                ReferralTargetBannerItem(
                    fullView = false,
                    referralTargetBanner = state.referralTargetBanner,
                )
            )
        }
    }

    private fun addFilterItem(state: State, list: MutableList<CustomerTabItem>) {
        if (state.customerTabDetails?.lifeCycle == LifeCycle.SORT && (Sort.sortfilter.isNotEmpty())) {
            list.add(FilterItem)
            list.add(EmptyFilterItem)
        } else {
            if (Sort.sortApplied && Sort.sortfilter.isNotEmpty()) {
                list.add(FilterItem)
            }
        }
    }

    private fun addLiveSalesItem(state: State, list: MutableList<CustomerTabItem>) {
        if (state.customerTabDetails?.lifeCycle == LifeCycle.SORT && (Sort.sortfilter.isNotEmpty())) {
            return
        }
        if (state.isLiveSalesActive && state.customerTabDetails?.liveSalesCustomer != null) {
            list.add(
                LiveSalesItem(
                    id = state.customerTabDetails.liveSalesCustomer.id,
                    balance = state.customerTabDetails.liveSalesCustomer.balanceV2,
                    liveSalesTutorialVisibility = state.liveSalesTutorialVisibility
                )
            )
        }
    }

    private fun addAppLockItem(state: State, list: MutableList<CustomerTabItem>) {
        if (state.customerTabDetails?.lifeCycle != LifeCycle.NORMAL_FLOW) return

        if (!state.isBannerCustomizationEnabled) {
            if (state.appLockInAppNotification) {
                list.add(AppLockItem)
            }
        }
    }

    private fun addDynamicBanner(state: State, list: MutableList<CustomerTabItem>) {
        if (state.showBannerCustomization && state.bannerCustomization != null) {
            list.add(DynamicViewItem(state.bannerCustomization))
        }
    }

    private fun addCustomerListWithState(state: State, list: MutableList<CustomerTabItem>) {
        if (state.customerTabDetails?.lifeCycle != LifeCycle.NORMAL_FLOW) return

        state.customerTabDetails.customers.forEachIndexed { _, customer ->
            val subTitlePair = findSubTitleAndType(state, customer)
            val commonLedger = state.supplierCreditEnabledCustomerIds.contains(customer.id)
            val addTxnPermissionDenied = if (commonLedger) {
                customer.isAddTransactionPermissionDenied()
            } else {
                false
            }

            val targetedCollectionReferralStatus = state.collectionCustomerReferralMap[customer.id]?.status
            val customerItem = HomeCustomerItem(
                customerId = customer.id,
                profileImage = customer.profileImage,
                name = customer.description,
                balance = customer.balanceV2,
                commonLedger = commonLedger,
                subtitle = subTitlePair.first,
                type = subTitlePair.second,
                unreadCount = (customer.newActivityCount).toInt(),
                addTxnPermissionDenied = addTxnPermissionDenied,
                showReferralIcon = showReferralIcon(targetedCollectionReferralStatus, customer.id)
            )
            list.add(customerItem)
        }
        giftIconShown = true
    }

    private fun showReferralIcon(targetedCollectionReferralStatus: Int?, customerId: String): Boolean {
        if (targetedCollectionReferralStatus == null)
            return false

        if (targetedCollectionReferralStatus == ReferralStatus.DEFAULT.value)
            return false

        if (targetedCollectionReferralStatus == ReferralStatus.REWARD_SUCCESS.value)
            return false

        if (targetedCollectionReferralStatus == ReferralStatus.REWARD_FAILED.value)
            return false

        if (giftIconShown.not()) {
            collectionEventTracker.get().trackCollectionReferralGiftShown(
                customerId,
                CollectionEventTracker.HOME_PAGE,
                CollectionEventTracker.CUSTOMER,
                ReferralStatus.fromValue(targetedCollectionReferralStatus).toString()
            )
        }

        return true
    }

    private fun findSubTitleAndType(state: State, customer: Customer): Pair<String, SubtitleType> {
        val subtitleType: SubtitleType
        val subTitle: String

        if (customer.customerSyncStatus != CLEAN.code) {
            return setCustomerSyncState(customer)
        }

        val collectionReferral = state.collectionCustomerReferralMap[customer.id]
        if (collectionReferral != null && collectionReferral.status == ReferralStatus.LINK_CREATED.value && !collectionReferral.ledgerSeen) {
            return context.get().getString(
                R.string.invite_earn_share_list_subtext,
                CurrencyUtil.formatV2(collectionReferral.amount)
            ) to SubtitleType.COLLECTION_TARGETED_REFERRAL
        }

        if (customer.dueActive && customer.dueInfo_activeDate != null && customer.balanceV2 < 0) {
            return setDueDate(customer, state)
        }

        if (customer.lastActivity != customer.createdAt) {
            val lastActivityStringId =
                CustomerLastActivity.getActivityFromCodeWithCustomerSubtitleAb(customer.lastActivityMetaInfo)
            val blankString = ""
            val amount = if (customer.lastAmount != null) CurrencyUtil.formatV2(customer.lastAmount!!) else ""
            subTitle = when {

                customer.lastActivity == null -> {
                    context.get().resources.getQuantityString(
                        lastActivityStringId,
                        DATE_TYPE_TODAY_OR_YESTERDAY,
                        amount,
                        blankString
                    )
                }
                LocalDate.now().compareTo(LocalDate(customer.lastActivity)) == 0 -> {
                    context.get().resources.getQuantityString(
                        lastActivityStringId,
                        DATE_TYPE_TODAY_OR_YESTERDAY,
                        amount,
                        context.get().getString(R.string.today)
                    )
                }
                LocalDate.now().minusDays(1).compareTo(LocalDate(customer.lastActivity)) == 0 -> {
                    context.get().resources.getQuantityString(
                        lastActivityStringId,
                        DATE_TYPE_TODAY_OR_YESTERDAY,
                        amount,
                        context.get().getString(R.string.yesterday)
                    )
                }
                else -> {
                    context.get().resources.getQuantityString(
                        lastActivityStringId,
                        DATE_TYPE_DATE,
                        amount,
                        customer.lastActivity!!.toString(DateTimeFormat.forPattern("dd MMM, YYYY"))
                    )
                }
            }

            subtitleType = if (state.unSyncCustomerIds.contains(customer.id)) {
                SubtitleType.TRANSACTION_SYNC_PENDING
            } else {
                SubtitleType.TRANSACTION_SYNC_DONE
            }
        } else {
            subTitle = customer.createdAt?.toString(DateTimeFormat.forPattern("dd MMM, YYYY")) ?: ""
            subtitleType = SubtitleType.CUSTOMER_ADDED
        }

        return subTitle to subtitleType
    }

    private fun setCustomerSyncState(customer: Customer): Pair<String, SubtitleType> {
        return if (customer.customerSyncStatus == IMMUTABLE.code) {
            context.get()
                .getString(R.string.ce_home_list_sub_txt_unable_to_add_customer) to SubtitleType.IMMUTABLE_CUSTOMER
        } else {
            val subTitle = customer.createdAt?.toString(DateTimeFormat.forPattern("dd MMM, YYYY")) ?: ""
            subTitle to SubtitleType.DIRTY_CUSTOMER
        }
    }

    private fun setDueDate(customer: Customer, state: State): Pair<String, SubtitleType> {
        var subtitleType: SubtitleType = SubtitleType.DUE_DATE_INCOMING
        var subTitle = ""
        when {
            DateTimeUtils.isCurrentDate(customer.dueInfo_activeDate) -> {
                subtitleType = SubtitleType.DUE_TODAY
            }
            DateTimeUtils.isDatePassed(customer.dueInfo_activeDate) -> {
                subTitle = getCollectionDateStrings(customer.dueInfo_activeDate, context.get())
                subtitleType = SubtitleType.DUE_DATE_PASSED
            }
            DateTimeUtils.isFutureDate(customer.dueInfo_activeDate) -> {
                subTitle = DateTimeUtils.getFormat2(context.get(), customer.dueInfo_activeDate) ?: ""
                subtitleType = SubtitleType.DUE_DATE_INCOMING
            }
        }
        if (state.unSyncCustomerIds.contains(customer.id)) {
            subtitleType = SubtitleType.TRANSACTION_SYNC_PENDING
        }
        return subTitle to subtitleType
    }

    private fun getCollectionDateStrings(dueInfoActiveDate: DateTime?, context: Context): String {
        val days = abs(Days.daysBetween(DateTime.now(), dueInfoActiveDate).days)
        if (days in 1..30) {
            return context.resources.getQuantityString(R.plurals.pending_day, days, days)
        } else if (days > 30) {
            val monthValue = (days / 30)
            return context.resources.getQuantityString(R.plurals.pending_month, monthValue, monthValue)
        }
        return ""
    }

    companion object {
        const val DATE_TYPE_TODAY_OR_YESTERDAY = 3
        const val DATE_TYPE_DATE = 1
    }
}
