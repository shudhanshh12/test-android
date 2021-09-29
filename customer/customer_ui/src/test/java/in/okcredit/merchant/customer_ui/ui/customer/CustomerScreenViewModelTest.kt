package `in`.okcredit.merchant.customer_ui.ui.customer

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.backend._offline.usecase.GetTransaction
import `in`.okcredit.backend._offline.usecase.ReactivateCustomer
import `in`.okcredit.backend._offline.usecase.UpdateCustomer
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.backend.server.GetRiskDetails
import `in`.okcredit.cashback.contract.usecase.GetCashbackMessageDetails
import `in`.okcredit.cashback.contract.usecase.IsCustomerCashbackFeatureEnabled
import `in`.okcredit.collection.contract.*
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.TestViewModel
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase.IsRoboflowFeatureEnabled
import `in`.okcredit.merchant.customer_ui.ui.subscription.usecase.SubscriptionFeatureEnabled
import `in`.okcredit.merchant.customer_ui.usecase.*
import `in`.okcredit.referral.contract.usecase.GetReferralLink
import `in`.okcredit.referral.contract.usecase.GetReferralTarget
import `in`.okcredit.shared.referral_views.model.Place
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import merchant.okcredit.supplier.contract.PutNotificationReminder
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.utils.Optional
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.feature_help.contract.GetContextualHelpIds
import tech.okcredit.feature_help.contract.GetSupportNumber

class CustomerScreenViewModelTest :
    TestViewModel<CustomerContract.State, CustomerContract.PartialState, CustomerContract.ViewEvent>() {

    private val getCustomerStatement: GetCustomerStatement = mock()

    private val checkNetworkHealth: CheckNetworkHealth = mock()

    private val getActiveBusiness: GetActiveBusiness = mock()

    private val getCustomer: GetCustomer = mock()

    private val getCustomerCollectionProfile: GetCustomerCollectionProfile = mock()

    private val getPaymentReminderIntent: GetPaymentReminderIntent = mock()

    private val defaultPreferences: DefaultPreferences = mock()

    private val collectionRepository: CollectionRepository = mock()

    private val markCollectionShared: MarkCollectionShared = mock()

    private val markCustomerAsSeen: MarkCustomerAsSeen = mock()

    private val reactivateCustomer: ReactivateCustomer = mock()

    private val isSupplierCreditEnabledCustomer: IsSupplierCreditEnabledCustomer = mock()

    private val getReferralLink: GetReferralLink = mock()

    private val ab: AbRepository = mock()

    private val tracker: Tracker = mock()

    private val updateDueInfo: UpdateDueInfo = mock()

    private val submitVoiceInput: PostVoiceInput = mock()

    private val getCustomerDueinfo: GetCustomerDueInfo = mock()

    private val updateCustomer: UpdateCustomer = mock()

    private val context: Context = mock(Context::class.java)

    private val getTransaction: GetTransaction = mock()

    private val getReferralTarget: GetReferralTarget = mock()

    private val optionalTargetsBannerReferral: Optional<ReferralTargetBanner> = mock()

    private val subscriptionFeatureEnabled: SubscriptionFeatureEnabled = mock()

    private val getCollectionNudgeForCustomerScreen: GetCollectionNudgeForCustomerScreen = mock()

    private val getCollectionNudgeOnSetDueDate: GetCollectionNudgeOnSetDueDate = mock()

    private val getCollectionNudgeOnDueDateCrossed: GetCollectionNudgeOnDueDateCrossed = mock()

    private val roboflowEnabled: IsRoboflowFeatureEnabled = mock()

    private val getKycStatus: GetKycStatus = mock()
    private val getKycRiskCategory: GetKycRiskCategory = mock()

    private val isCustomerCashbackFeatureEnabled: IsCustomerCashbackFeatureEnabled = mock()
    private val getCashbackMessageDetails: GetCashbackMessageDetails = mock()

    private val checkAutoDueDateGenerated: CheckAutoDueDateGenerated = mock()

    private val getPaymentOutLinkDetail: GetPaymentOutLinkDetail = mock()
    private val getRiskDetails: GetRiskDetails = mock()
    private val supplierAnalyticsEvents: CustomerEventTracker = mock()

    private val reportFromBalanceWidgetExpt: ReportFromBalanceWidgetExpt = mock()
    private val canShowCollectWithGPay: CanShowCollectWithGPay = mock()

    private val collectionEvent: SendCollectionEvent = mock()
    private val communicationApi: CommunicationRepository = mock()
    private val getSupportNumber: GetSupportNumber = mock()

    private val triggerMerchantPayout: TriggerMerchantPayout = mock()
    private val getContextualHelpIds: GetContextualHelpIds = mock()
    private val showCustomerPaymentIntentTrigger: ShowCustomerPaymentIntentTrigger = mock()

    private val showCollectionContextualTrigger: ShowCollectionContextualTrigger = mock()

    private val referralEducationPreference: ReferralEducationPreference = mock()
    private val getStatusForTargetedReferralCustomer: GetStatusForTargetedReferralCustomer = mock()
    private val getBlindPayLinkId: GetBlindPayLinkId = mock()
    private val putNotificationReminder: PutNotificationReminder = mock()
    private val getCollectionsOfCustomerOrSupplier: GetCollectionsOfCustomerOrSupplier = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    override fun initDependencies() {
        whenever(getCustomer.execute("1234")).thenReturn(Observable.just(TestData.CUSTOMER))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))

        whenever(getReferralTarget.execute(Place.CUSTOMER_SCREEN)).thenReturn(
            Single.just(
                optionalTargetsBannerReferral
            )
        )
        `when`(checkNetworkHealth.execute(Unit)).thenReturn(UseCase.wrapObservable(Observable.just(Unit)))
    }

    @Test
    fun `add txn should navigate to add txn navigation`() {
        // setup
        val txType = merchant.okcredit.accounting.model.Transaction.CREDIT
        // provide intent
        pushIntent(CustomerContract.Intent.GoToAddTxn(txType))

        // expectations
        assertLastViewEvent<CustomerContract.ViewEvent.GotoLegacyAddTransaction>()
    }

    @Test
    fun `go to customer profile intent should navigate to customer profile`() {
        // provide intent
        pushIntent(CustomerContract.Intent.GoToCustomerProfile)

        // expectations
        assertLastViewEvent<CustomerContract.ViewEvent.GotoCustomerProfile>()
    }

    @Test
    fun `go to phone dialer intent should navigate to phone dialer`() {
        // provide intent
        pushIntent(CustomerContract.Intent.GoToPhoneDialer)

        // expectations
        assertLastViewEvent<CustomerContract.ViewEvent.GotoCallCustomer>()
    }

    @Test
    fun `view tx intent should navigate to tx page`() {
        val txnId = "xyz"
        val currentDue = 0L

        whenever(getTransaction.execute(txnId)).thenReturn(Observable.just(TestData.TRANSACTION1))

        // provide intent
        pushIntent(CustomerContract.Intent.ViewTransaction(txnId, currentDue))

        // expectations
        assertLastViewEvent<CustomerContract.ViewEvent.GotoTransactionDetailFragment>()
    }

    @Test
    fun `view discount intent should navigate to discount page page`() {
        val discountId = "abcd"
        val currentDue = 0L

        // provide intent
        pushIntent(CustomerContract.Intent.ViewDiscount(discountId, currentDue))

        // expectations
        assertLastViewEvent<CustomerContract.ViewEvent.GoToDiscountScreen>()
    }

    @Test
    fun `ShowUnblockDialog intent should emit ShowUnblockDialog View Effect`() {
        // provide intent
        pushIntent(CustomerContract.Intent.ShowUnblockDialog)

        // expectations
        assertLastViewEvent<CustomerContract.ViewEvent.ShowUnblockDialog>()
    }

    @Test
    fun `subscription feature enabled true should change state to true`() {
        whenever(subscriptionFeatureEnabled.execute()).thenReturn(Observable.just(true))

        val initialState = lastState()
        // provide intent
        pushIntent(CustomerContract.Intent.Load)

        // expectations
        assertThat(
            lastState() == initialState.copy(
                showSubscriptions = true
            )
        )
    }

    @Test
    fun `subscription feature returns false should change state to false`() {
        whenever(subscriptionFeatureEnabled.execute()).thenReturn(Observable.just(false))

        val initialState = lastState()
        // provide intent
        pushIntent(CustomerContract.Intent.Load)

        // expectations
        assertThat(
            lastState() == initialState.copy(
                showSubscriptions = false
            )
        )
    }

    @Test
    fun `roboflow feature returns false should change state to false`() {
        whenever(roboflowEnabled.execute()).thenReturn(Observable.just(false))

        val initialState = lastState()
        // provide intent
        pushIntent(CustomerContract.Intent.Load)

        // expectations
        assertThat(
            lastState() == initialState.copy(
                isRoboflowFeatureEnabled = false
            )
        )
    }

    @Test
    fun `roboflow feature returns true should change state to true`() {
        whenever(roboflowEnabled.execute()).thenReturn(Observable.just(true))

        val initialState = lastState()
        // provide intent
        pushIntent(CustomerContract.Intent.Load)

        // expectations
        assertThat(
            lastState() == initialState.copy(
                isRoboflowFeatureEnabled = true
            )
        )
    }

    @Test
    fun `load google pay enabled state true`() {
        whenever(canShowCollectWithGPay.execute()).thenReturn(Observable.just(true))

        val initialState = lastState()
        // provide intent
        pushIntent(CustomerContract.Intent.LoadGooglePay)

        // expectations
        assertThat(
            lastState() == initialState.copy(
                showCollectionWithGpay = true
            )
        )
    }

    @Test
    fun `load google pay enabled state false`() {
        whenever(canShowCollectWithGPay.execute()).thenReturn(Observable.just(false))

        val initialState = lastState()
        // provide intent
        pushIntent(CustomerContract.Intent.LoadGooglePay)

        // expectations
        assertThat(
            lastState() == initialState.copy(
                showCollectionWithGpay = false
            )
        )
    }

//    @Test
//    fun `showSetupCollectionDialog() on due date changed if getCollectionNudgeOnSetDueDate returns true`() {
//        // setup
//        val customerId = "1234"
//
//        val customer: Customer = mock()
//        val captureDate = MonthView.CapturedDate(mock(), MonthView.CapturedDate.DateStatus.ADDED)
//        val dueDate: Pair<MonthView.CapturedDate, Customer> = captureDate to customer
//        val requestDueUpdate: UpdateDueInfo.Request = UpdateDueInfo.Request(dueDate)
//        // create Presenter
//        val viewModel = createViewModel(initialState, customerId)
//
//        // observe state
//        val stateObserver = TestObserver<CustomerContract.State>()
//        viewModel.state().subscribe(stateObserver)
//
//        // observe view effect
//        val viewEffectObserver = TestObserver<CustomerContract.ViewEvent>()
//        viewModel.viewEvent().subscribe(viewEffectObserver)
//
// //        whenever(captureDate.dateStatus).thenReturn(MonthView.CapturedDate.DateStatus.ADDED)
//        whenever(updateDueInfo.execute(requestDueUpdate)).thenReturn(Observable.just(Result.Success(Unit)))
//        whenever(getCollectionNudgeOnSetDueDate.execute(Unit)).thenReturn(Observable.just(Result.Success(true)))
//
//        // provide intent
//        viewModel.attachIntents(
//            Observable.just(CustomerContract.Intent.OnDueDateChange(dueDate))
//        )
//
//        // expectations
//        assertThat(
//            lastState() == initialState
//        )
//
//        assertThat(
//            viewEffectObserver.values().last() == CustomerContract.ViewEvent.ShowSetupCollectionDialog
//        )
//
//
//    }

    @Test
    fun `makeUpdateMobileAndRemind() should remind on success `() {
        pushIntent(CustomerContract.Intent.UpdateMobileAndRemind)

        assertLastViewEvent<CustomerContract.ViewEvent.ForceRemind>()
        assertLastValue { it.customer == TestData.CUSTOMER }
    }

    @Test
    fun `makeUpdateMobileAndRemind() on error should do nothing `() {
        val initialState = lastState()
        pushIntent(CustomerContract.Intent.UpdateMobileAndRemind)
        assertLastValue { it.customer == TestData.CUSTOMER }
    }

    @Test
    fun `makeForceRemind() `() {
        pushIntent(CustomerContract.Intent.ForceRemind)
        assertLastViewEvent<CustomerContract.ViewEvent.ForceRemind>()
    }

//    @Test
//    fun `syncDueInfo() should emit show auto due date`() {
//        val initialState = lastState()
//        val customerId = "customer_id"
//        val dueInfo = DueInfo(
//            customerId = "customer_id",
//            isDueActive = true,
//            activeDate = DateTime.now().plusDays(3),
//            isCustomDateSet = false,
//            isAutoGenerated = true
//        )
//        whenever(checkAutoDueDateGenerated.execute(customerId)).thenReturn(Observable.just(dueInfo))
//        pushIntent(CustomerContract.Intent.SyncDueInfo)
//        assertThat(
//            viewEffectObserver.values().last() == CustomerContract.ViewEvent.ShowAutoDueDateDialog(dueInfo.activeDate!!)
//        ).isTrue()
//        assertThat(lastState()).isEqualTo(
//            initialState.copy(dueInfo = dueInfo)
//        )
//
//        stateObserver.dispose()
//    }

    @Test
    fun `getKycRisks() should return no risk `() {
        val status = KycStatus.NOT_SET
        val response = KycRisk(KycRiskCategory.NO_RISK, false, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        pushIntent(CustomerContract.Intent.LoadKycDetails)

        assertLastValue {
            it.kycStatus == status &&
                it.kycRiskCategory == response.kycRiskCategory &&
                it.isKycLimitReached == response.isLimitReached
        }
    }

    @Test
    fun `getKycRisks() should return low risk`() {
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.LOW, false, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        pushIntent(CustomerContract.Intent.LoadKycDetails)

        assertLastValue {
            it.kycStatus == status &&
                it.kycRiskCategory == response.kycRiskCategory &&
                it.isKycLimitReached == response.isLimitReached
        }
    }

    @Test
    fun `getKycRisks() should return high risk`() {
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.HIGH, false, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        pushIntent(CustomerContract.Intent.LoadKycDetails)
        assertLastValue {
            it.kycStatus == status &&
                it.kycRiskCategory == response.kycRiskCategory &&
                it.isKycLimitReached == response.isLimitReached
        }
    }

    @Test
    fun `getKycRisks() should return low risk with limit reached`() {
        val initialState = lastState()
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.LOW, true, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        pushIntent(CustomerContract.Intent.LoadKycDetails)
        assertLastValue {
            it.kycStatus == status &&
                it.kycRiskCategory == response.kycRiskCategory &&
                it.isKycLimitReached == response.isLimitReached
        }
    }

    @Test
    fun `getKycRisks() should return high risk with limit reached`() {
        val initialState = lastState()
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.HIGH, true, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        pushIntent(CustomerContract.Intent.LoadKycDetails)

        assertLastValue {
            it.kycStatus == status && it.kycRiskCategory == response.kycRiskCategory && it.isKycLimitReached == response.isLimitReached
        }
    }

    @Test
    fun `reportFromBalanceWidgetExpt() should return true if exp enabled and variant is CONTROL`() {
        whenever(reportFromBalanceWidgetExpt.execute()).thenReturn(Observable.just(true))
        pushIntent(CustomerContract.Intent.LoadReportFromBalanceWidgetExpt)

        assertLastViewEvent<CustomerContract.ViewEvent.EnableReportFromBalanceWidgetExp>()
    }

    override fun createViewModel() = CustomerScreenViewModel(
        initialState = CustomerContract.State(),
        customerId = TestData.CUSTOMER.id,
        reactivate = false,
        txnId = "",
        customerName = "",
        sourceScreen = "",
        collectionId = "",
        getActiveBusiness = { getActiveBusiness },
        getCustomer = { getCustomer },
        context = { context },
        defaultPreferences = { defaultPreferences },
        collectionRepository = { collectionRepository },
        markCollectionShared = { markCollectionShared },
        reactivateCustomer = { reactivateCustomer },
        isSupplierCreditEnabledCustomer = { isSupplierCreditEnabledCustomer },
        getReferralLink = { getReferralLink },
        getCustomerStatement = { getCustomerStatement },
        checkNetworkHealth = { checkNetworkHealth },
        ab = { ab },
        tracker = { tracker },
        updateDueInfo = { updateDueInfo },
        submitVoiceInput = { submitVoiceInput },
        getCustomerDueInfo = { getCustomerDueinfo },
        updateCustomer = { updateCustomer },
        getCustomerCollectionProfile = { getCustomerCollectionProfile },
        getPaymentReminderIntent = { getPaymentReminderIntent },
        markCustomerAsSeen = { markCustomerAsSeen },
        getCanShowCollectionDate = mock(),
        checkOnlineEducationToShow = mock(),
        getCanShowChatNewStickerLazy = mock(),
        getChatUnreadMessages = mock(),
        nullifyDueDate = mock(),
        getLastTransactionAddedByCustomer = mock(),
        getCanShowBuyerTxnAlert = mock(),
        canShowCreditPaymentLayout = mock(),
        closeReferralTargetBanner = mock(),
        transactionInitiated = mock(),
        getReferralTarget = { getReferralTarget },
        subscriptionFeatureEnabled = { subscriptionFeatureEnabled },
        getAccountsTotalBills = mock(),
        getCollectionNudgeForCustomerScreen = { getCollectionNudgeForCustomerScreen },
        getCollectionNudgeOnSetDueDate = { getCollectionNudgeOnSetDueDate },
        getCollectionNudgeOnDueDateCrossed = { getCollectionNudgeOnDueDateCrossed },
        isRoboflowFeatureEnabled = { roboflowEnabled },
        getPaymentOutLinkDetail = { getPaymentOutLinkDetail },
        getRiskDetails = { getRiskDetails },
        analyticsEvents = { supplierAnalyticsEvents },
        getKycStatus = { getKycStatus },
        isCustomerCashbackFeatureEnabled = { isCustomerCashbackFeatureEnabled },
        getCashbackMessageDetails = { getCashbackMessageDetails },
        getKycRiskCategory = { getKycRiskCategory },
        checkAutoDueDateGenerated = { checkAutoDueDateGenerated },
        reportFromBalanceWidgetExpt = { reportFromBalanceWidgetExpt },
        showCollectWithGPay = { canShowCollectWithGPay },
        sendCollectionEvent = { collectionEvent },
        communicationApi = { communicationApi },
        getHelpNumber = { getSupportNumber },
        triggerMerchantPayout = { triggerMerchantPayout },
        getContextualHelpIds = { getContextualHelpIds },
        showCustomerPaymentIntentTrigger = { showCustomerPaymentIntentTrigger },
        showCollectionContextualTrigger = { showCollectionContextualTrigger },
        customerImmutableHelper = mock(),
        referralEducationPreference = { referralEducationPreference },
        getStatusForTargetedReferralCustomer = { getStatusForTargetedReferralCustomer },
        updateCustomerReferralLedgerSeen = mock(),
        getBlindPayLinkId = { getBlindPayLinkId },
        getTransaction = { getTransaction },
        getEligibilityOnboardingNudges = mock(),
        collectionEventTracker = mock(),
        getCustomerMenuOptions = mock(),
        putNotificationReminder = { putNotificationReminder },
        getCustomerSupportType = mock(),
        getCustomerSupportPreference = mock(),
        setCashbackBannerClosed = mock(),
        getCashbackBannerClosed = mock(),
        accountingEventTracker = mock(),
        syncCustomerTransactionOrCollection = mock(),
        syncTransaction = mock(),
        collectionSyncer = mock(),
        getSupplierCollectionProfileWithSync = mock(),
        getCollectionsOfCustomerOrSupplier = { getCollectionsOfCustomerOrSupplier },
        businessScopedPreferenceWithActiveBusinessId = mock(),
        canShowPreNetworkOnboardingBanner = mock(),
        getActiveBusinessId = { getActiveBusinessId },
        referralSignupTracker = mock(),
        getCustomerScreenSortSelection = mock(),
        setCustomerScreenSortSelection = mock(),
    )
}
