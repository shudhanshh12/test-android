package tech.okcredit.home.ui.home

import `in`.okcredit.analytics.SuperProperties
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.GetUnSyncedTransactionsCount
import `in`.okcredit.backend._offline.usecase.SubmitFeedbackImpl
import `in`.okcredit.backend._offline.usecase._sync_usecases.HomeScreenRefreshSync
import `in`.okcredit.backend._offline.usecase._sync_usecases.TransactionsSyncService
import `in`.okcredit.backend.contract.Features
import `in`.okcredit.backend.contract.HomeDataSyncWorker
import `in`.okcredit.backend.contract.HomeRefreshSyncWorker
import `in`.okcredit.backend.contract.NonActiveBusinessesDataSyncWorker
import `in`.okcredit.backend.contract.RxSharedPrefValues.*
import `in`.okcredit.collection.contract.CanShowAddBankDetailsPopUp
import `in`.okcredit.collection.contract.GetKycRiskCategory
import `in`.okcredit.collection.contract.GetKycStatus
import `in`.okcredit.collection.contract.IsCollectionCampaignMerchant
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.dynamicview.Targets
import `in`.okcredit.frontend.contract.FrontendConstants.ARG_SHOW_BULK_REMINDER
import `in`.okcredit.frontend.contract.FrontendConstants.ARG_SHOW_COLLECTION_POPUP
import `in`.okcredit.frontend.contract.FrontendConstants.ARG_SHOW_INAPP_REVIEW
import `in`.okcredit.individual.contract.GetIndividual
import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.merchant.contract.IsMultipleAccountEnabled
import `in`.okcredit.referral.contract.usecase.GetReferralLink
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.supplier.usecase.GetNotificationReminderForHome
import androidx.work.WorkInfo
import com.mixpanel.android.mpmetrics.InAppNotification
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.ObservableSource
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle
import merchant.okcredit.accounting.usecases.IsContactPermissionAskedOnce
import merchant.okcredit.accounting.usecases.SetContactPermissionAskedOnce
import merchant.okcredit.supplier.contract.PutNotificationReminderAction
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.home.ui.analytics.HomeEventTracker
import tech.okcredit.home.ui.home.HomeContract.*
import tech.okcredit.home.ui.homesearch.HomeConstants.HomeTab.CUSTOMER_TAB
import tech.okcredit.home.ui.homesearch.HomeConstants.HomeTab.SUPPLIER_TAB
import tech.okcredit.home.ui.payables_onboarding.HomeTabOrderList
import tech.okcredit.home.ui.payables_onboarding.helpers.TabOrderingHelper
import tech.okcredit.home.usecase.*
import tech.okcredit.home.usecase.home.GetReferralInAppNotification
import tech.okcredit.home.usecase.pre_network_onboarding.GetEligibilityPreNetworkOnboarding
import tech.okcredit.home.usecase.pre_network_onboarding.HideBigButtonAndNudge
import tech.okcredit.home.usecase.pre_network_onboarding.SetPreNetworkOnboardingNudgeShown
import tech.okcredit.home.widgets.filter_option.usecase.EnableFilterOptionVisibility
import tech.okcredit.home.widgets.filter_option.usecase.GetFilterEducationVisibility
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam(ARG_SHOW_COLLECTION_POPUP) val showCollectionPop: Lazy<Boolean>,
    @ViewModelParam(ARG_SHOW_INAPP_REVIEW) val showInAppReview: Lazy<Boolean>,
    @ViewModelParam(ARG_SHOW_BULK_REMINDER) val showBulkReminder: Lazy<Boolean>,
    private val getHomeMerchantData: Lazy<GetHomeMerchantData>,
    private val tracker: Lazy<Tracker>,
    private val homeScreenRefreshSync: Lazy<HomeScreenRefreshSync>,
    private val homeRefreshSyncWorker: Lazy<HomeRefreshSyncWorker>,
    private val transactionsSyncService: Lazy<TransactionsSyncService>,
    private val getUnSyncedTransactionsCount: Lazy<GetUnSyncedTransactionsCount>,
    private val ab: Lazy<AbRepository>,
    private val submitFeedback: Lazy<SubmitFeedbackImpl>,
    private val getMixpanelInAppNotification: Lazy<GetMixpanelInAppNotification>,
    private val setMerchantAppLockPreference: Lazy<SetMerchantAppLockPreference>,
    private val rxSharedPreference: Lazy<DefaultPreferences>,
    private val enableFilterOptionVisibility: Lazy<EnableFilterOptionVisibility>,
    private val getFilterEducationVisibility: Lazy<GetFilterEducationVisibility>,
    private val getReferralLink: Lazy<GetReferralLink>,
    private val getCustomization: Lazy<GetCustomization>,
    private val executeOnHomeLoad: Lazy<ExecuteOnHomeLoad>,
    private val homeDataSyncWorker: Lazy<HomeDataSyncWorker>,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    private val executeForceSyncAndMigration: Lazy<ExecuteForceSyncAndMigration>,
    private val isMerchantFromCollectionCampaign: Lazy<IsCollectionCampaignMerchant>,
    private val getReferralInAppNotification: Lazy<GetReferralInAppNotification>,
    private val getUploadButtonVisibility: Lazy<GetUploadButtonVisibility>,
    private val contactsRepository: Lazy<ContactsRepository>,
    private val checkForAppUpdateInteruption: Lazy<CheckForAppUpdateInteruption>,
    private val hideBigButtonAndNudge: Lazy<HideBigButtonAndNudge>,
    private val getKycStatus: Lazy<GetKycStatus>,
    private val getKycRiskCategory: Lazy<GetKycRiskCategory>,
    private val isMenuOnBottomNavigationEnabled: Lazy<IsMenuOnBottomNavigationEnabled>,
    private val tabOrderingHelper: Lazy<TabOrderingHelper>,
    private val canShowAddBankDetailsPopUp: Lazy<CanShowAddBankDetailsPopUp>,
    private val getUnSyncedCustomersCount: Lazy<GetUnSyncedCustomersCount>,
    private val getImmutableCustomersCount: Lazy<GetImmutableCustomersCount>,
    private val getBusinessIdList: Lazy<GetBusinessIdList>,
    private val setPreNetworkOnboardingNudgeShown: Lazy<SetPreNetworkOnboardingNudgeShown>,
    private val getNotificationReminderForHome: Lazy<GetNotificationReminderForHome>,
    private val putNotificationReminderAction: Lazy<PutNotificationReminderAction>,
    private val getEligibilityPreNetworkOnBoarding: Lazy<GetEligibilityPreNetworkOnboarding>,
    private val homeEventTracker: Lazy<HomeEventTracker>,
    private val nonActiveBusinessesDataSyncWorker: Lazy<NonActiveBusinessesDataSyncWorker>,
    private val schedulePeriodicSync: Lazy<SchedulePeriodicSync>,
    private val getIndividual: Lazy<GetIndividual>,
    private val isMultipleAccountEnabled: Lazy<IsMultipleAccountEnabled>,
    private val isContactPermissionAskedOnce: Lazy<IsContactPermissionAskedOnce>,
    private val setContactPermissionAskedOnce: Lazy<SetContactPermissionAskedOnce>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState.get()) {

    private val showPayOnlineEducationPublishSubject: PublishSubject<Unit> = PublishSubject.create()

    private lateinit var referralId: String

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(
            loadIntents(),
            setupViewPager(),
            getNudgeExperimentVariant(),
            observeCustomerTabEducationShown(),
            observeShowPreNetworkTooltip(),
            observeSyncStatusOnLoad(),
            checkForContactPermission(),
            setIfContactPermissionAskedOnce(),
            executeOnHomeLoad(),
            scheduleHomeDataSyncWorker(),
            schedulePeriodicDataSyncWorker(),
            scheduleNonActiveBusinessesDataSyncWorker(),
            observeUnSyncTransactionCount(),
            observeMixpanelInAppNotification(),
            observableForEnabledFeaturesOnLoad(),
            observeInternetConnectivityOnLoad(),
            observeMerchantDataOnLoad(),
            observeSyncHomeLoaderOnLoad(),
            observeOneTimeInAppReview(),
            observeFilterOptionVisibility(),
            showFilterOptionEducation(),
            observeSyncNowClick(),
            observeReferralId(),
            observeBulkReminder(),
            showSupplierTabEducation(),
            showAddSupplierEducation(),
            showFirstAddSupplierEducation(),
            canShowPayOnlineEducationObservable(),
            checkForAppUpdateInteruption(), // handle app update interuption
            setMerchantAppLockPreference(),
            setInAppDownloadLoaderVisibility(),
            observeSubmitFeedbackIntent(),
            observeShowInAppPopupIntent(),
            observeUpdateInAppNavigationObjectIntent(),
            observeSetRxPrefIntent(),
            showUserMigrationUploadButton(),
            loadToolbarCustomization(),
            getUtmCampaignObservable(),
            showReferralInAppNotification(),
            showAddOkCreditContactInApp(),
            canShowNewOnSupplierTab(),
            canShowImmediateUpdate(),
            hideUserMigrationUploadButton(),
            getNudgeExperimentVariant(),
            resetKycNotification(),
            showCompleteKyc(),
            showKycRisk(),
            showKycStatus(),
            canShowAddBankDetailsPopUp(),
            fetchUnSyncedCustomerCount(),
            fetchImmutableCustomerCount(),
            fetchBusinessCount(),
            fetchIndividualId(),
            notificationReminderObserver(),
            updateReminderNotification(),
            observeTrackPreNetworkIntent(),
            canShowMultipleAccountEntry(),
            observeTrackTwoCTAIntent(),
            canShowPayOnlineEducationObservable(),
        )
    }

    private fun observeSetRxPrefIntent() = intent<Intent.RxPreferenceBoolean>()
        .switchMap { wrap(rxCompletable { rxSharedPreference.get().set(it.key, it.value, it.scope) }) }
        .map {
            PartialState.NoChange
        }

    private fun observeUpdateInAppNavigationObjectIntent() = intent<Intent.UpdateInAppNavigationObject>()
        .map {
            PartialState.UpdateInAppNotificationUI(it.inAppNavigationObject)
        }

    private fun observeShowInAppPopupIntent() = intent<Intent.ShowInAppNavigationPopup>()
        .map {
            PartialState.ShowInAppNavigationPopup(it.showInAppNavigationPopup)
        }

    private fun observeSubmitFeedbackIntent() = intent<Intent.SubmitFeedback>()
        .switchMap { wrap(submitFeedback.get().schedule(it.feedback, it.rating)) }
        .map {
            PartialState.NoChange
        }

    private fun setInAppDownloadLoaderVisibility() = intent<Intent.SetInAppDownloadLoaderVisibility>()
        .map {
            PartialState.SetInAppDownloadLoaderVisibility(it.status)
        }

    private fun setMerchantAppLockPreference() = intent<Intent.Load>()
        .switchMap { setMerchantAppLockPreference.get().execute(Unit) }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> {
                    PartialState.NoChange
                }
                is Result.Failure -> {
                    when {
                        isAuthenticationIssue(it.error) -> {
                            emitViewEvent(ViewEvent.GotoLogin)
                            PartialState.NoChange
                        }
                        isInternetIssue(it.error) -> PartialState.NoChange

                        else -> {
                            PartialState.NoChange
                        }
                    }
                }
            }
        }

    private fun checkForAppUpdateInteruption() = intent<Intent.Load>()
        .switchMap { checkForAppUpdateInteruption.get().execute() }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> {
                    if (it.value) {
                        emitViewEvent(ViewEvent.AppUpdateInterrupted)
                    }
                    PartialState.NoChange
                }
                is Result.Failure -> PartialState.NoChange
            }
        }

    private fun showFirstAddSupplierEducation() = intent<Intent.Load>()
        .delay(3000, TimeUnit.MILLISECONDS)
        .switchMap {
            wrap(
                rxSharedPreference.get().getBoolean(SHOULD_SHOW_FIRST_SUPPLIER_EDUCATION, Scope.Individual)
                    .asObservable()
            )
        }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> PartialState.FirstSupplierEducation(it.value)
                is Result.Failure -> PartialState.NoChange
            }
        }

    private fun showAddSupplierEducation() = intent<Intent.Load>()
        .delay(3000, TimeUnit.MILLISECONDS)
        .switchMap {
            wrap(
                rxSharedPreference.get().getBoolean(SHOULD_SHOW_ADD_SUPPLIER_TAB_EDUCATION, Scope.Individual)
                    .asObservable()
            )
        }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> PartialState.AddSupplierEducation(it.value)
                is Result.Failure -> PartialState.NoChange
            }
        }

    private fun showSupplierTabEducation() = intent<Intent.Load>()
        .delay(3000, TimeUnit.MILLISECONDS)
        .switchMap {
            wrap(
                rxSharedPreference.get().getBoolean(SHOULD_SHOW_SUPPLIER_TAB_EDUCATION, Scope.Individual)
                    .asObservable()
            )
        }.map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> {
                    if (it.value) {
                        emitViewEvent(ViewEvent.ShowSupplierTabEducation)
                    }
                    PartialState.NoChange
                }
                is Result.Failure -> PartialState.NoChange
            }
            PartialState.NoChange
        }

    private fun checkForContactPermission() = intent<Intent.CheckForContactPermission>()
        .switchMap { wrap(rxSingle { isContactPermissionAskedOnce.get().execute() }) }
        .map {
            when (it) {
                is Result.Success -> PartialState.SetContactPermissionAskedOnce(it.value)
                else -> PartialState.NoChange
            }
        }

    private fun setIfContactPermissionAskedOnce() = intent<Intent.ContactPermissionAskedOnce>()
        .switchMap { wrap(rxSingle { setContactPermissionAskedOnce.get().execute(true) }) }
        .map {
            pushIntent(Intent.CheckForContactPermission)
            PartialState.NoChange
        }

    private fun notificationReminderObserver() = intent<Intent.Load>()
        .delay(1000, TimeUnit.MILLISECONDS)
        .switchMap {
            wrap(getNotificationReminderForHome.get().execute())
        }.map {
            when (it) {
                is Result.Success -> {
                    if (it.value.enabled) {
                        emitViewEvent(
                            ViewEvent.ShowNotificationReminder(
                                notificationReminderForUi = it.value.notificationReminderForUi,
                            )
                        )
                    }
                    PartialState.NoChange
                }
                else -> PartialState.NoChange
            }
        }

    private fun updateReminderNotification() = intent<Intent.UpdateReminderNotification>()
        .switchMap {
            wrap(putNotificationReminderAction.get().execute(it.notificationId, it.status.status))
        }.map {
            PartialState.NoChange
        }

    private fun observeCustomerTabEducationShown() = intent<Intent.CustomerTabEducationShown>()
        .switchMap { wrap(rxSingle { setPreNetworkOnboardingNudgeShown.get().execute() }) }
        .map {
            PartialState.NoChange
        }

    private fun loadIntents() = intent<Intent.Load>().map {
        pushIntent(Intent.LoadAddBankDetails)
        pushIntent(Intent.CheckForContactPermission)
        PartialState.NoChange
    }

    private fun canShowAddBankDetailsPopUp() = intent<Intent.LoadAddBankDetails>()
        .switchMap { wrap(canShowAddBankDetailsPopUp.get().execute()) }
        .map { result ->
            if (result is Result.Success && !result.value.isNullOrEmpty()) {
                emitViewEvent(ViewEvent.ShowAddBankPopUp(result.value))
            }
            PartialState.NoChange
        }

    // TODO move all setSuperProperties() calls into a separate usecase class
    private fun fetchImmutableCustomerCount() = intent<Intent.Load>()
        .take(1)
        .switchMap { getImmutableCustomersCount.get().execute() }
        .map {
            tracker.get()
                .setSuperProperties(SuperProperties.IMMUTABLE_RELATIONSHIPS_COUNT, it.toString())
            PartialState.NoChange
        }

    private fun fetchBusinessCount() = intent<Intent.Load>()
        .switchMap { wrap { getBusinessIdList.get().execute().first() } }
        .map {
            if (it is Result.Success) {
                tracker.get().setSuperProperties(SuperProperties.BUSINESS_COUNT, it.value.size.toString())
            }
            PartialState.NoChange
        }

    private fun fetchIndividualId() = intent<Intent.Load>()
        .switchMap { wrap { getIndividual.get().execute().first() } }
        .map {
            if (it is Result.Success) {
                tracker.get().setSuperPropertiesForIndividual(it.value.id)
            }
            PartialState.NoChange
        }

    private fun fetchUnSyncedCustomerCount() = intent<Intent.Load>()
        .switchMap {
            wrap(
                getUnSyncedCustomersCount.get().execute()
            )
        }
        .map {
            when (it) {
                is Result.Success -> {
                    tracker.get().setSuperProperties(SuperProperties.UNSYNC_RELATIONSHIPS_COUNT, it.value.toString())
                    PartialState.SetUnSyncCustomersCount(it.value)
                }
                else -> PartialState.NoChange
            }
        }

    private fun setupViewPager() = intent<Intent.SetupViewPager>()
        .switchMap { wrap(rxSingle { tabOrderingHelper.get().getHomeTabState() }) }
        .map {
            when (it) {
                is Result.Success -> {
                    emitViewEvent(ViewEvent.TrackPayablesExperimentStarted(it.value.isPayablesExperimentEnabled))
                    emitViewEvent(
                        ViewEvent.SetupViewPager(
                            it.value.tabOrderList,
                            it.value.isPayablesExperimentEnabled
                        )
                    )
                    PartialState.PayablesExperimentEnabled(
                        it.value.isPayablesExperimentEnabled,
                    )
                }
                is Result.Failure -> {
                    // TODO Track Backoff
                    emitViewEvent(
                        ViewEvent.SetupViewPager(
                            HomeTabOrderList(
                                listOf(
                                    CUSTOMER_TAB,
                                    SUPPLIER_TAB
                                )
                            ),
                            false
                        )
                    )
                    PartialState.NoChange
                }
                else -> {
                    PartialState.NoChange
                }
            }
        }

    private fun hideUserMigrationUploadButton(): Observable<UiState.Partial<State>>? {
        return intent<Intent.HideUploadButtonToolTip>().map {
            getUploadButtonVisibility.get().setUploadButtonTooltipShownPreference(true)
            PartialState.NoChange
        }
    }

    private fun showUserMigrationUploadButton(): Observable<UiState.Partial<State>>? {
        return intent<Intent.Load>()
            .switchMap { getUploadButtonVisibility.get().execute() }
            .map {
                when (it) {
                    is Result.Success -> {
                        if (it.value.canShowUploadButton &&
                            it.value.canShowUploadButtonToolTip
                        ) {
                            emitViewEvent(ViewEvent.ShowUploadButtonTooltip)
                        }
                        if (it.value.canShowUploadButton) {
                            emitViewEvent(ViewEvent.TrackUploadButtonViewed)
                        }
                        PartialState.SetCanShowUploadButtonAndTooltip(
                            it.value.canShowUploadButton,
                        )
                    }
                    else -> PartialState.NoChange
                }
            }
    }

    private fun observeOneTimeInAppReview(): ObservableSource<UiState.Partial<State>>? {
        return intent<Intent.Load>()
            .filter { showInAppReview.get() }
            .map {
                emitViewEvent(ViewEvent.ShowReviewDialog)
                PartialState.NoChange
            }
    }

    private fun observeBulkReminder() = intent<Intent.Load>()
        .filter { showBulkReminder.get() }
        .map {
            emitViewEvent(ViewEvent.ShowBulkReminder)
            PartialState.NoChange
        }

    private fun canShowNewOnSupplierTab(): ObservableSource<UiState.Partial<State>> {
        return intent<Intent.Load>()
            .switchMap { ab.get().isFeatureEnabled(Features.NEW_ON_SUPPLIER_TAB) }
            .map {
                PartialState.CanShowNewOnSupplierTab(it)
            }
    }

    private fun canShowImmediateUpdate(): ObservableSource<UiState.Partial<State>> {
        return intent<Intent.Load>()
            .switchMap { wrap(ab.get().isFeatureEnabled(Features.IMMEDIATE_UPDATE_ENABLED)) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        if (it.value)
                            emitViewEvent(ViewEvent.ShowImmediateUpdate)
                        PartialState.NoChange
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun showReferralInAppNotification(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap { getReferralInAppNotification.get().execute(Unit) }
            .map {
                when (it) {
                    is Result.Success -> {
                        if (it.value) {
                            emitViewEvent(ViewEvent.GoToReferralInAppNotification)
                        }
                        PartialState.NoChange
                    }
                    else -> PartialState.NoChange
                }
            }
    }

    private fun canShowPayOnlineEducationObservable(): Observable<PartialState> {
        return showPayOnlineEducationPublishSubject
            .delay(3000, TimeUnit.MILLISECONDS)
            .switchMap {
                wrap(
                    rxSharedPreference.get().getBoolean(IS_PAY_ONLINE_EDUCATION_HOME_SHOWN, Scope.Individual)
                        .asObservable()
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

    private fun getUtmCampaignObservable(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap { wrap(isMerchantFromCollectionCampaign.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        if (it.value) {
                            showPayOnlineEducationPublishSubject.onNext(Unit)
                        }
                        PartialState.IsMerchantFromCollectionCampaign(it.value)
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun observeReferralId(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { wrap(getReferralLink.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        referralId = it.value
                        PartialState.NoChange
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
            }
    }

    private fun observeSyncNowClick(): Observable<PartialState> {
        return intent<Intent.SyncNow>()
            .switchMap { wrap(homeRefreshSyncWorker.get().schedule()) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.SetRefreshLoaderVisibility(true)
                    is Result.Success -> PartialState.SetRefreshLoaderVisibility(false)
                    is Result.Failure -> PartialState.SetRefreshLoaderVisibility(false)
                }
            }
    }

    private fun observeFilterOptionVisibility(): Observable<PartialState> {
        return intent<Intent.OnResume>()
            .switchMap { enableFilterOptionVisibility.get().execute() }
            .map {
                when (it) {
                    is Result.Success -> PartialState.CanShowFilterOption(it.value)
                    else -> PartialState.NoChange
                }
            }
    }

    private fun showFilterOptionEducation(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap { getFilterEducationVisibility.get().execute() }
            .map {
                if (it is Result.Success) {
                    if (it.value) {
                        emitViewEvent(ViewEvent.ShowFilterEducation)
                    }
                }
                PartialState.NoChange
            }
    }

    private fun observeMixpanelInAppNotification(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { getMixpanelInAppNotification.get().execute(Unit) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        if (it.value != null) {
                            emitViewEvent(
                                ViewEvent.ShowMixPanelInAppNotification(
                                    it.value as InAppNotification,
                                    null,
                                    null
                                )
                            )
                        }
                        PartialState.NoChange
                    }
                    is Result.Failure -> {
                        PartialState.NoChange
                    }
                }
            }
    }

    private fun observeUnSyncTransactionCount(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { wrap(getUnSyncedTransactionsCount.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        tracker.get().setSuperProperties(SuperProperties.UNSYNC_TRANSACTIONS_COUNT, it.value.toString())
                        PartialState.SetUnSyncTxnCount(it.value)
                    }
                    else -> PartialState.NoChange
                }
            }
    }

    private fun executeOnHomeLoad(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { wrap(executeOnHomeLoad.get().execute()) }
            .map { PartialState.NoChange }
    }

    private fun scheduleHomeDataSyncWorker() = intent<Intent.Load>()
        .switchMap { wrap(homeDataSyncWorker.get().schedule()) }
        .map { PartialState.NoChange }

    private fun schedulePeriodicDataSyncWorker() = intent<Intent.Load>()
        .switchMap { wrap { schedulePeriodicSync.get().execute() } }
        .map { PartialState.NoChange }

    private fun scheduleNonActiveBusinessesDataSyncWorker() = intent<Intent.Load>()
        .switchMap { wrap(nonActiveBusinessesDataSyncWorker.get().schedule()) }
        .map { PartialState.NoChange }

    private fun observeSyncStatusOnLoad(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { wrap(transactionsSyncService.get().isSyncedAtLeastOnce()) }
            .flatMap {
                when (it) {
                    is Result.Progress -> Observable.just(PartialState.NoChange)
                    is Result.Success -> {
                        if (!it.value) {
                            emitViewEvent(ViewEvent.GoToSyncScreen)
                            Observable.just(PartialState.NoChange)
                        } else {
                            executeForceSyncAndMigration.get().execute()
                                .andThen(Observable.just(PartialState.NoChange))
                        }
                    }
                    is Result.Failure -> Observable.just(PartialState.NoChange)
                }
            }
    }

    private fun getNudgeExperimentVariant(): Observable<PartialState> {
        return intent<Intent.OnResume>()
            .switchMap { hideBigButtonAndNudge.get().execute() }
            .switchMap { wrap(rxSingle { getEligibilityPreNetworkOnBoarding.get().execute(it) }) }
            .map {
                when (it) {
                    is Result.Success -> {
                        if (it.value.eligibleForNudges && it.value.isPreNetworkUser) {
                            pushIntent(Intent.TrackPreNetworkViewed("Supplier"))
                            pushIntent(Intent.ShowPreNetworkToolTip(it.value.delayInToolTipShown))
                        } else if (it.value.isPreNetworkUser) {
                            pushIntent(Intent.TrackPreNetworkViewed("Customer"))
                        }
                        if (!it.value.hideBigButtonAndNudge) {
                            pushIntent(Intent.TrackTwoCTAViewed)
                        }
                        PartialState.SetCanShowAddRelationNudge(
                            showAddRelationNudge = it.value.hideBigButtonAndNudge
                        )
                    }
                    else -> {
                        PartialState.NoChange
                    }
                }
            }
    }

    private fun observeTrackTwoCTAIntent() = intent<Intent.TrackTwoCTAViewed>()
        .take(1)
        .map {
            homeEventTracker.get().trackViewAddRelationshipOnboardingCTA()
            PartialState.NoChange
        }

    private fun observeShowPreNetworkTooltip() = intent<Intent.ShowPreNetworkToolTip>()
        .take(1)
        .map {
            emitViewEvent(ViewEvent.ShowPreNetworkOnboardingNudges(it.delayInToolTipShown))
            PartialState.NoChange
        }

    private fun observeTrackPreNetworkIntent() = intent<Intent.TrackPreNetworkViewed>()
        .take(1)
        .map {
            homeEventTracker.get().trackPreNetworkTabViewed(it.tab)
            PartialState.NoChange
        }

    private fun observeSyncHomeLoaderOnLoad(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { wrap(homeScreenRefreshSync.get().getWorkStatus()) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        if (it.value.state == WorkInfo.State.RUNNING && it.value.runAttemptCount < 1) {
                            PartialState.SetRefreshLoaderVisibility(true)
                        } else if (it.value.state === WorkInfo.State.ENQUEUED && it.value.runAttemptCount < 1) {
                            PartialState.SetRefreshLoaderVisibility(true)
                        } else {
                            PartialState.SetRefreshLoaderVisibility(false)
                        }
                    }
                    is Result.Failure -> {
                        PartialState.NoChange
                    }
                }
            }
    }

    private fun observeMerchantDataOnLoad(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { getHomeMerchantData.get().execute(Unit) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        PartialState.ShowBusiness(it.value)
                    }
                    is Result.Failure -> {
                        when {
                            isAuthenticationIssue(it.error) -> {
                                emitViewEvent(ViewEvent.GotoLogin)
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

    private fun observeInternetConnectivityOnLoad(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { checkNetworkHealth.get().execute(Unit) }
            .map {
                when (it) {
                    is Result.Success -> PartialState.SetInternetConnectivityStatus(true)
                    is Result.Failure -> PartialState.SetInternetConnectivityStatus(false)
                    else -> PartialState.SetInternetConnectivityStatus(true)
                }
            }
    }

    private fun observableForEnabledFeaturesOnLoad(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { wrap(ab.get().isFeatureEnabled(Features.REWARD)) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> PartialState.SetRewardEnabled(it.value)
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> PartialState.NoChange
                            else -> PartialState.NoChange
                        }
                    }
                }
            }
    }

    private fun loadToolbarCustomization(): Observable<PartialState>? {
        return intent<Intent.Load>()
            .switchMap { wrap(getCustomization.get().execute(Targets.HOME_TOOLBAR_ACTION)) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> PartialState.ToolbarCustomization(it.value)
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun showAddOkCreditContactInApp(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap { wrap(contactsRepository.get().showAddOkCreditContactInApp()) }
            .map {
                if (it is Result.Success && it.value) {
                    emitViewEvent(ViewEvent.GoToAddOkCreditContactInAppNotification)
                }
                PartialState.NoChange
            }
    }

    private fun getKycDetails() = Observable.zip(
        getKycStatus.get().execute(),
        getKycRiskCategory.get().execute(),
        { kycStatus, kycRisk ->
            Pair(kycStatus, kycRisk)
        }
    )

    private fun showCompleteKyc() = intent<Intent.Load>()
        .switchMap { rxSharedPreference.get().getBoolean(SHOULD_SHOW_COMPLETE_KYC, Scope.Individual).asObservable() }
        .filter { it }
        .switchMap { getKycDetails() }
        .filter { it.first.value != KycStatus.COMPLETE.value && it.first.value != KycStatus.PENDING.value }
        .firstOrError()
        .map {
            emitViewEvent(ViewEvent.ShowKycCompleteDialog)
            PartialState.SetKycDetails(it.first, it.second.kycRiskCategory)
        }
        .toObservable()

    private fun showKycRisk() = intent<Intent.Load>()
        .switchMap { rxSharedPreference.get().getBoolean(SHOULD_SHOW_RISK_KYC, Scope.Individual).asObservable() }
        .filter { it }
        .switchMap {
            getKycDetails()
        }
        .filter { it.second.isLimitReached }
        .firstOrError()
        .map {
            emitViewEvent(ViewEvent.ShowKycRiskDialog(it.first, it.second))
            PartialState.SetKycDetails(it.first, it.second.kycRiskCategory)
        }
        .toObservable()

    private fun showKycStatus() = intent<Intent.Load>()
        .switchMap { rxSharedPreference.get().getBoolean(SHOULD_SHOW_KYC_STATUS, Scope.Individual).asObservable() }
        .filter { it }
        .switchMap {
            wrap(getKycStatus.get().execute())
        }
        .filter { it is Result.Success }
        .take(1)
        .map {
            if (it is Result.Success) {
                emitViewEvent(ViewEvent.ShowKycStatusDialog(it.value))
                PartialState.SetKycDetails(it.value)
            } else {
                PartialState.NoChange
            }
        }

    private fun resetKycNotification() = intent<Intent.ResetKycNotification>()
        .switchMap {
            wrap(
                rxCompletable {
                    rxSharedPreference.get().set(SHOULD_SHOW_COMPLETE_KYC, false, Scope.Individual)
                    rxSharedPreference.get().set(SHOULD_SHOW_RISK_KYC, false, Scope.Individual)
                    rxSharedPreference.get().set(SHOULD_SHOW_KYC_STATUS, false, Scope.Individual)
                }
            )
        }
        .map {
            PartialState.NoChange
        }

    private fun canShowMultipleAccountEntry() = intent<Intent.Load>()
        .switchMap { wrap(isMultipleAccountEnabled.get().execute()) }
        .map {
            if (it is Result.Success) {
                return@map PartialState.ShowMultipleAccountEntry(it.value)
            }
            PartialState.NoChange
        }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is PartialState.SetInternetConnectivityStatus -> currentState.copy(isConnectedToInternet = partialState.status)
            is PartialState.ShowBusiness -> currentState.copy(
                businessData = partialState.businessData
            )
            is PartialState.SetUnSyncTxnCount -> currentState.copy(unSyncTxnCount = partialState.count)
            is PartialState.SetUnSyncCustomersCount -> currentState.copy(unSyncCustomerCount = partialState.count)

            is PartialState.SetRewardEnabled -> currentState.copy(isRewardEnabled = partialState.isRewardEnabled)

            is PartialState.SetRefreshLoaderVisibility -> currentState.copy(homeSyncLoader = partialState.status)
            is PartialState.SetInAppDownloadLoaderVisibility -> currentState.copy(inAppDownloadLoader = partialState.status)
            is PartialState.ShowInAppNavigationPopup -> currentState.copy(showInAppNavigationPopup = partialState.showInAppNavigationPopup)
            is PartialState.UpdateInAppNotificationUI -> currentState.copy(inAppNavigationObject = partialState.inAppNotificationObject)

            is PartialState.AddSupplierEducation -> currentState.copy(showAddSupplierEducation = partialState.showAddSupplierEducation)
            is PartialState.FirstSupplierEducation -> currentState.copy(showFirstSupplierEducation = partialState.showFirstSupplierEducation)

            is PartialState.NoChange -> currentState

            is PartialState.ToolbarCustomization -> currentState.copy(toolbarCustomization = partialState.customization)

            is PartialState.IsMerchantFromCollectionCampaign -> currentState.copy(
                isMerchantFromCollectionCampaign = partialState.isMerchantFromCollectionCampaign
            )
            is PartialState.IsPayOnlineEducationShown -> currentState.copy(
                isPayOnlineEducationHomeShown = partialState.isPayOnlineEducationHomeShown
            )
            is PartialState.CanShowNewOnSupplierTab -> currentState.copy(
                canShowNewOnSupplierTab = partialState.canShowNewOnSupplierTab
            )
            is PartialState.SetCanShowUploadButtonAndTooltip -> currentState.copy(
                canShowUploadButton = partialState.canShowButton,
            )
            is PartialState.CanShowFilterOption -> currentState.copy(
                canShowFilterOption = partialState.canShow
            )
            is PartialState.SetCanShowAddRelationNudge -> currentState.copy(
                hideBigButtonAndNudge = partialState.showAddRelationNudge
            )
            is PartialState.PayablesExperimentEnabled -> currentState.copy(
                isPayablesExperimentEnabled = partialState.enabled,
            )
            is PartialState.SetKycDetails -> currentState.copy(
                kycStatus = partialState.kycStatus,
                kycRiskCategory = partialState.kycRiskCategory
            )
            is PartialState.ShowMultipleAccountEntry -> currentState.copy(
                canShowMultipleAccountsEntryPoint = partialState.canShow
            )
            is PartialState.SetContactPermissionAskedOnce -> currentState.copy(
                isContactPermissionAskedOnce = partialState.value
            )
        }
    }
}
