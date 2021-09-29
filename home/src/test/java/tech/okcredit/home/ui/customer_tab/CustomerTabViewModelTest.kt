package tech.okcredit.home.ui.customer_tab

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.GetUnSyncedCustomers
import `in`.okcredit.collection.contract.*
import `in`.okcredit.customer.contract.BulkReminderModel
import `in`.okcredit.customer.contract.GetBannerForBulkReminder
import `in`.okcredit.home.GetSupplierCreditEnabledCustomerIds
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import `in`.okcredit.referral.contract.usecase.CloseReferralTargetBanner
import `in`.okcredit.referral.contract.usecase.GetReferralTarget
import `in`.okcredit.referral.contract.usecase.TransactionInitiated
import `in`.okcredit.shared.base.BaseViewModel
import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.home.TestViewModel
import tech.okcredit.home.ui.customer_tab.CustomerTabContract.*
import tech.okcredit.home.usecase.*

class CustomerTabViewModelTest : TestViewModel<State, PartialState, CustomerTabViewEvent>() {
    private val initialState = State()
    private val mockContext: Context = mock()
    private val mockGetActiveCustomers: GetActiveCustomers = mock()
    private val mockCollectionRepository: CollectionRepository = mock()
    private val mockCheckLiveSalesActive: CheckLiveSalesActive = mock()
    private val mockGetUnSyncedCustomers: GetUnSyncedCustomers = mock()
    private val mockGetCustomerCollectionProfile: GetCustomerCollectionProfile = mock()
    private val mockTracker: Tracker = mock()
    private val mockGetAppLockInAppVisibility: GetAppLockInAppVisibility = mock()
    private val mockGetSupplierCreditEnabledCustomerIds: GetSupplierCreditEnabledCustomerIds = mock()
    private val mockAb: AbRepository = mock()
    private val mockOnboardingPreferences: OnboardingPreferences = mock()
    private val mockGetCustomization: GetCustomization = mock()
    private val mockGetReferralTarget: GetReferralTarget = mock()
    private val mockCloseReferralTargetBanner: CloseReferralTargetBanner = mock()
    private val mockTransactionInitiated: TransactionInitiated = mock()
    private val mockGetKycRiskCategory: GetKycRiskCategory = mock()
    private val mockUserStoryScheduleSyncIfEnabled: UserStoryScheduleSyncIfEnabled = mock()
    private val mockFetchPaymentTargetedReferral: FetchPaymentTargetedReferral = mock()
    private val mockCollectionEventTracker: CollectionEventTracker = mock()
    private val mockGetTargetedReferralList: GetTargetedReferralList = mock()
    private val mockGetHomeBannerForBulkReminder: GetBannerForBulkReminder = mock()

    @Test
    fun `LoadBulkReminderBanner Should Update the state if canShowBanner is true`() {
        runBlocking {
            // given
            val fakeBulkReminderModel = BulkReminderModel(
                totalBalanceDue = -20,
                defaulterSince = 14,
                totalReminders = 5,
                canShowBanner = true,
                canShowNotificationIcon = true
            )

            // when
            whenever(mockGetHomeBannerForBulkReminder.execute()).thenReturn(flowOf(fakeBulkReminderModel))

            pushIntent(Intent.LoadBulkReminderBanner)

            delay(33L)

            // then
            assertLastValue {
                it.bulkReminderState != null
            }
            assertLastValue {
                it.bulkReminderState?.totalBalanceDue == fakeBulkReminderModel.totalBalanceDue
            }
            assertLastValue {
                it.bulkReminderState?.defaulterSince == fakeBulkReminderModel.defaulterSince
            }
            assertLastValue {
                it.bulkReminderState?.showNotificationBadge == fakeBulkReminderModel.canShowNotificationIcon
            }
            assertLastValue {
                it.bulkReminderState?.totalReminders == fakeBulkReminderModel.totalReminders
            }
        }
    }

    @Test
    fun `LoadBulkReminderBanner Should set the state as null if canShowBanner is false`() {
        runBlocking {
            // given
            val fakeBulkReminderModel = BulkReminderModel(
                totalBalanceDue = 20,
                defaulterSince = 14,
                totalReminders = 5,
                canShowBanner = false,
                canShowNotificationIcon = true
            )

            // when
            whenever(mockGetHomeBannerForBulkReminder.execute()).thenReturn(flowOf(fakeBulkReminderModel))

            pushIntent(Intent.LoadBulkReminderBanner)

            // then
            assertLastValue {
                it.bulkReminderState == null
            }
        }
    }

    @Test
    fun `LoadBulkReminderBanner Should set the state as null if getHomeBannerForBulkReminder use case is failed`() {
        runBlocking {
            // given

            // when
            whenever(mockGetHomeBannerForBulkReminder.execute()).thenThrow(IllegalStateException("fake_exception"))

            pushIntent(Intent.LoadBulkReminderBanner)

            // then
            assertLastValue {
                it.bulkReminderState == null
            }
        }
    }

    override fun createViewModel(): BaseViewModel<State, PartialState, CustomerTabViewEvent> {
        return CustomerTabViewModel(
            initialState = { initialState },
            context = { mockContext },
            getActiveCustomers = { mockGetActiveCustomers },
            collectionRepository = { mockCollectionRepository },
            checkLiveSalesActive = { mockCheckLiveSalesActive },
            getUnSyncedCustomers = { mockGetUnSyncedCustomers },
            getCustomerCollectionProfile = { mockGetCustomerCollectionProfile },
            tracker = { mockTracker },
            getAppLockInAppVisibility = { mockGetAppLockInAppVisibility },
            getSupplierCreditEnabledCustomerIds = { mockGetSupplierCreditEnabledCustomerIds },
            ab = { mockAb },
            onboardingPreferences = { mockOnboardingPreferences },
            getCustomization = { mockGetCustomization },
            getReferralTarget = { mockGetReferralTarget },
            closeReferralTargetBanner = { mockCloseReferralTargetBanner },
            transactionInitiated = { mockTransactionInitiated },
            getKycRiskCategory = { mockGetKycRiskCategory },
            fetchPaymentTargetedReferral = { mockFetchPaymentTargetedReferral },
            collectionEventTracker = { mockCollectionEventTracker },
            getTargetedReferralList = { mockGetTargetedReferralList },
            getBannerForBulkReminder = { mockGetHomeBannerForBulkReminder },
            userStoryScheduleSyncIfEnabled = { mockUserStoryScheduleSyncIfEnabled },
            bulkReminderAnalytics = mock(),
            getActiveBusiness = mock(),
            referralOnSignupTracker = mock(),
            defaultPreferences = mock(),
            syncSupplierEnabledCustomerIds = mock(),
            getHomeCustomerTabSortSelection = mock(),
            setHomeCustomerTabSortSelection = mock(),
        )
    }
}
