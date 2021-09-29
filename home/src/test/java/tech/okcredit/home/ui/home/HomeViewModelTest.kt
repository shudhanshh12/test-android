package tech.okcredit.home.ui.home

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.GetUnSyncedTransactionsCount
import `in`.okcredit.backend._offline.usecase.SubmitFeedbackImpl
import `in`.okcredit.backend._offline.usecase._sync_usecases.HomeScreenRefreshSync
import `in`.okcredit.backend._offline.usecase._sync_usecases.TransactionsSyncService
import `in`.okcredit.backend.contract.HomeDataSyncWorker
import `in`.okcredit.backend.contract.HomeRefreshSyncWorker
import `in`.okcredit.backend.contract.NonActiveBusinessesDataSyncWorker
import `in`.okcredit.collection.contract.CanShowAddBankDetailsPopUp
import `in`.okcredit.collection.contract.GetKycRiskCategory
import `in`.okcredit.collection.contract.GetKycStatus
import `in`.okcredit.collection.contract.IsCollectionCampaignMerchant
import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.merchant.contract.IsMultipleAccountEnabled
import `in`.okcredit.referral.contract.usecase.GetReferralLink
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.supplier.usecase.GetNotificationReminderForHome
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import merchant.okcredit.supplier.contract.PutNotificationReminderAction
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.home.TestViewModel
import tech.okcredit.home.ui.home.HomeContract.*
import tech.okcredit.home.ui.payables_onboarding.helpers.TabOrderingHelper
import tech.okcredit.home.usecase.*
import tech.okcredit.home.usecase.home.GetReferralInAppNotification
import tech.okcredit.home.usecase.pre_network_onboarding.HideBigButtonAndNudge
import tech.okcredit.home.widgets.filter_option.usecase.EnableFilterOptionVisibility
import tech.okcredit.home.widgets.filter_option.usecase.GetFilterEducationVisibility

class HomeViewModelTest : TestViewModel<State, PartialState, ViewEvent>() {

    private val getHomeMerchantData: GetHomeMerchantData = mock()
    private val tracker: Tracker = mock()
    private val homeScreenRefreshSync: HomeScreenRefreshSync = mock()
    private val transactionsSyncService: TransactionsSyncService = mock()
    private val getUnSyncedTransactionsCount: GetUnSyncedTransactionsCount = mock()
    private val ab: AbRepository = mock()
    private val submitFeedback: SubmitFeedbackImpl = mock()
    private val getMixpanelInAppNotification: GetMixpanelInAppNotification = mock()
    private val setMerchantAppLockPreference: SetMerchantAppLockPreference = mock()
    private val rxSharedPreference: DefaultPreferences = mock()
    private val enableFilterOptionVisibility: EnableFilterOptionVisibility = mock()
    private val getFilterEducationVisibility: GetFilterEducationVisibility = mock()
    private val getReferralLink: GetReferralLink = mock()
    private val getCustomization: GetCustomization = mock()
    private val executeOnHomeLoad: ExecuteOnHomeLoad = mock()
    private val checkNetworkHealth: CheckNetworkHealth = mock()
    private val executeForceSyncAndMigration: ExecuteForceSyncAndMigration = mock()
    private val isMerchantFromCollectionCampaign: IsCollectionCampaignMerchant = mock()
    private val getReferralInAppNotification: GetReferralInAppNotification = mock()
    private val getUploadButtonVisibility: GetUploadButtonVisibility = mock()
    private val contactsRepository: ContactsRepository = mock()
    private val checkForAppUpdateInteruption: CheckForAppUpdateInteruption = mock()
    private val hideBigButtonAndNudge: HideBigButtonAndNudge = mock()
    private val getKycStatus: GetKycStatus = mock()
    private val getKycRiskCategory: GetKycRiskCategory = mock()
    private val isMenuOnBottomNavigationEnabled: IsMenuOnBottomNavigationEnabled = mock()
    private val tabOrderingHelper: TabOrderingHelper = mock()
    private val canShowAddBankDetailsPopUp: CanShowAddBankDetailsPopUp = mock()
    private val homeDataSyncWorker: HomeDataSyncWorker = mock()
    private val homeRefreshSyncWorker: HomeRefreshSyncWorker = mock()
    private val schedulePeriodicSync: SchedulePeriodicSync = mock()
    private val nonActiveBusinessesDataSyncWorker: NonActiveBusinessesDataSyncWorker = mock()
    private val getNotificationReminderForHome: GetNotificationReminderForHome = mock()
    private val putNotificationReminderAction: PutNotificationReminderAction = mock()
    private val isMultipleAccountEnabled: IsMultipleAccountEnabled = mock()
    private val getBusinessIdList: GetBusinessIdList = mock()
    override fun createViewModel() = HomeViewModel(
        initialState = { State() },
        showCollectionPop = { false },
        showInAppReview = { false },
        showBulkReminder = { false },
        getHomeMerchantData = { getHomeMerchantData },
        tracker = { tracker },
        homeScreenRefreshSync = { homeScreenRefreshSync },
        transactionsSyncService = { transactionsSyncService },
        getUnSyncedTransactionsCount = { getUnSyncedTransactionsCount },
        ab = { ab },
        submitFeedback = { submitFeedback },
        getMixpanelInAppNotification = { getMixpanelInAppNotification },
        setMerchantAppLockPreference = { setMerchantAppLockPreference },
        rxSharedPreference = { rxSharedPreference },
        enableFilterOptionVisibility = { enableFilterOptionVisibility },
        getFilterEducationVisibility = { getFilterEducationVisibility },
        getReferralLink = { getReferralLink },
        getCustomization = { getCustomization },
        executeOnHomeLoad = { executeOnHomeLoad },
        checkNetworkHealth = { checkNetworkHealth },
        executeForceSyncAndMigration = { executeForceSyncAndMigration },
        isMerchantFromCollectionCampaign = { isMerchantFromCollectionCampaign },
        getReferralInAppNotification = { getReferralInAppNotification },
        getUploadButtonVisibility = { getUploadButtonVisibility },
        contactsRepository = { contactsRepository },
        checkForAppUpdateInteruption = { checkForAppUpdateInteruption },
        hideBigButtonAndNudge = { hideBigButtonAndNudge },
        getKycStatus = { getKycStatus },
        getKycRiskCategory = { getKycRiskCategory },
        isMenuOnBottomNavigationEnabled = { isMenuOnBottomNavigationEnabled },
        tabOrderingHelper = { tabOrderingHelper },
        canShowAddBankDetailsPopUp = { canShowAddBankDetailsPopUp },
        homeDataSyncWorker = { homeDataSyncWorker },
        homeRefreshSyncWorker = { homeRefreshSyncWorker },
        getUnSyncedCustomersCount = mock(),
        getImmutableCustomersCount = mock(),
        getNotificationReminderForHome = { getNotificationReminderForHome },
        putNotificationReminderAction = { putNotificationReminderAction },
        setPreNetworkOnboardingNudgeShown = mock(),
        getEligibilityPreNetworkOnBoarding = mock(),
        homeEventTracker = mock(),
        schedulePeriodicSync = { schedulePeriodicSync },
        nonActiveBusinessesDataSyncWorker = { nonActiveBusinessesDataSyncWorker },
        isMultipleAccountEnabled = { isMultipleAccountEnabled },
        getBusinessIdList = { getBusinessIdList },
        isContactPermissionAskedOnce = mock(),
        setContactPermissionAskedOnce = mock(),
        getIndividual = mock()
    )

    @Test
    fun `show add bank details popup emits view event if list is not null and empty`() {
        whenever(canShowAddBankDetailsPopUp.execute()).thenReturn(Single.just(listOf("Customer1, Customer2")))
        pushIntent(Intent.LoadAddBankDetails)
        assertLastViewEvent(ViewEvent.ShowAddBankPopUp(listOf("Customer1, Customer2")))
    }
}
