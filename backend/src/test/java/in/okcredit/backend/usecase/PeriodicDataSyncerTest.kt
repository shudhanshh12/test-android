package `in`.okcredit.backend.usecase

import `in`.okcredit.backend._offline.usecase.LinkDevice
import `in`.okcredit.backend._offline.usecase.SuggestedCustomersForAddTransactionShortcutSyncer
import `in`.okcredit.backend._offline.usecase.SyncContactsWithAccount
import `in`.okcredit.backend._offline.usecase.SyncCustomerTxnAlert
import `in`.okcredit.backend.contract.CheckAuth
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.communication_inappnotification.contract.InAppNotificationRepository
import `in`.okcredit.installedpackges.InstalledPackagesRepository
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.rewards.contract.RewardsSyncer
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.bills.BillRepository
import tech.okcredit.contacts.contract.ContactsRepository

class PeriodicDataSyncerTest {
    private val checkAuth: CheckAuth = mock()
    private val businessApi: BusinessRepository = mock()
    private val collectionSyncer: CollectionSyncer = mock()
    private val rewardsSyncer: RewardsSyncer = mock()
    private val ab: AbRepository = mock()
    private val referralRepository: ReferralRepository = mock()
    private val contactsRepository: ContactsRepository = mock()
    private val syncContactsWithAccount: SyncContactsWithAccount = mock()
    private val syncCustomerTxnAlert: SyncCustomerTxnAlert = mock()
    private val installedPackagesRepository: InstalledPackagesRepository = mock()
    private val inAppNotificationRepository: InAppNotificationRepository = mock()
    private val suggestedCustomersForAddTransactionShortcutSyncer:
        SuggestedCustomersForAddTransactionShortcutSyncer = mock()
    private val billRepository: BillRepository = mock()
    private val linkDevice: LinkDevice = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val supplierCreditRepository: SupplierCreditRepository = mock()

    private val periodicDataSyncer = PeriodicDataSyncer(
        { checkAuth },
        { businessApi },
        { collectionSyncer },
        { rewardsSyncer },
        { ab },
        { referralRepository },
        { contactsRepository },
        { syncContactsWithAccount },
        { syncCustomerTxnAlert },
        { installedPackagesRepository },
        { inAppNotificationRepository },
        { suggestedCustomersForAddTransactionShortcutSyncer },
        { billRepository },
        { linkDevice },
        { supplierCreditRepository },
        { getActiveBusinessId }
    )

    @Test
    fun `when user is logged in should schedule and execute all sync calls`() {
        // given
        val businessId = "business-id"
        whenever(checkAuth.execute()).thenReturn(Observable.just(true))
        whenever(getActiveBusinessId.thisOrActiveBusinessId(businessId)).thenReturn(Single.just(businessId))
        whenever(businessApi.executeSyncBusiness(businessId)).thenReturn(Completable.complete())
        doNothing().whenever(collectionSyncer).scheduleSyncCollections(0, "periodic_syncer", businessId)
        doNothing().whenever(collectionSyncer).scheduleCollectionProfile("periodic_syncer", businessId)
        whenever(rewardsSyncer.scheduleEverything(businessId)).thenReturn(Completable.complete())
        whenever(ab.sync(businessId, "periodic")).thenReturn(Completable.complete())
        whenever(referralRepository.syncReferralLink(any())).thenReturn(Completable.complete())
        whenever(referralRepository.sync(businessId)).thenReturn(Completable.complete())
        whenever(contactsRepository.scheduleUploadContactsWorker()).thenReturn(Completable.complete())
        whenever(contactsRepository.scheduleCheckForContactsInOkcNetwork()).thenReturn(Completable.complete())
        whenever(syncContactsWithAccount.schedule(businessId)).thenReturn(Completable.complete())
        whenever(syncCustomerTxnAlert.schedule(businessId)).thenReturn(Completable.complete())
        whenever(installedPackagesRepository.syncInstalledPackagesToServer(businessId)).thenReturn(Completable.complete())
        whenever(inAppNotificationRepository.scheduleSyncCompletable(businessId)).thenReturn(Completable.complete())
        whenever(suggestedCustomersForAddTransactionShortcutSyncer.schedule(businessId)).thenReturn(Completable.complete())
        whenever(billRepository.scheduleBillSync(businessId)).thenReturn(Completable.complete())
        whenever(linkDevice.schedule()).thenReturn(Completable.complete())
        whenever(supplierCreditRepository.syncNotificationReminder(businessId)).thenReturn(Completable.complete())

        // when
        val testObserver = periodicDataSyncer.execute(businessId).test()

        // then
        verify(getActiveBusinessId).thisOrActiveBusinessId(businessId)
        verify(businessApi).executeSyncBusiness(businessId)
        verify(collectionSyncer).scheduleSyncCollections(CollectionSyncer.SYNC_ALL, "periodic_syncer", businessId)
        verify(collectionSyncer).scheduleCollectionProfile("periodic_syncer", businessId)
        verify(rewardsSyncer).scheduleEverything(businessId)
        verify(ab).sync(businessId, "periodic")
        verify(referralRepository).syncReferralLink(businessId)
        verify(referralRepository).sync(businessId)
        verify(contactsRepository).scheduleUploadContactsWorker()
        verify(contactsRepository).scheduleCheckForContactsInOkcNetwork()
        verify(syncContactsWithAccount).schedule(businessId)
        verify(syncCustomerTxnAlert).schedule(businessId)
        verify(installedPackagesRepository).syncInstalledPackagesToServer(businessId)
        verify(inAppNotificationRepository).scheduleSyncCompletable(businessId)
        verify(suggestedCustomersForAddTransactionShortcutSyncer).schedule(businessId)
        verify(billRepository).scheduleBillSync(businessId)
        verify(linkDevice).schedule()
        verify(supplierCreditRepository).syncNotificationReminder(businessId)
        testObserver.assertComplete()
    }

    @Test
    fun `when user is not logged in should not schedule and execute any sync calls`() {
        val businessId = "business-id"
        // given
        whenever(checkAuth.execute()).thenReturn(Observable.just(false))

        // when
        val testObserver = periodicDataSyncer.execute().test()

        // then
        verify(checkAuth).execute()
        verify(businessApi, times(0)).executeSyncBusiness(businessId)
        verify(collectionSyncer, times(0)).scheduleSyncCollections(0, "periodic_syncer", businessId)
        verify(collectionSyncer, times(0)).scheduleCollectionProfile("periodic_syncer", businessId)
        verify(rewardsSyncer, times(0)).scheduleEverything(businessId)
        verify(ab, times(0)).sync(businessId, "periodic")
        verify(referralRepository, times(0)).syncReferralLink(any())
        verify(referralRepository, times(0)).sync(businessId)
        verify(contactsRepository, times(0)).scheduleUploadContactsWorker()
        verify(contactsRepository, times(0)).scheduleCheckForContactsInOkcNetwork()
        verify(syncContactsWithAccount, times(0)).schedule(businessId)
        verify(syncCustomerTxnAlert, times(0)).schedule(businessId)
        verify(installedPackagesRepository, times(0)).syncInstalledPackagesToServer(businessId)
        verify(inAppNotificationRepository, times(0)).scheduleSyncCompletable(businessId)
        verify(suggestedCustomersForAddTransactionShortcutSyncer, times(0)).schedule(businessId)
        verify(billRepository, times(0)).scheduleBillSync(businessId)
        verify(linkDevice, times(0)).schedule()
        verify(getActiveBusinessId, times(0)).execute()
        verify(supplierCreditRepository, times(0)).syncNotificationReminder(businessId)
        testObserver.assertComplete()
    }
}
